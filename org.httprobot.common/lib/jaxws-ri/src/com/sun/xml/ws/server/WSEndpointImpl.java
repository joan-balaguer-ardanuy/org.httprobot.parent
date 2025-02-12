/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.addressing.EPRSDDocumentFilter;
import com.sun.xml.ws.addressing.WSEPRExtension;
import com.sun.xml.ws.api.Component;
import com.sun.xml.ws.api.ComponentFeature;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.model.wsdl.WSDLDirectProperties;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortProperties;
import com.sun.xml.ws.model.wsdl.WSDLProperties;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.resources.HandlerMessages;
import com.sun.xml.ws.util.Pool;
import com.sun.xml.ws.util.Pool.TubePool;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.wsdl.OperationDispatcher;
import org.glassfish.gmbal.ManagedObjectManager;
import org.w3c.dom.Element;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link WSEndpoint} implementation.
 * 
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public /*final*/ class WSEndpointImpl<T> extends WSEndpoint<T> implements LazyMOMProvider.WSEndpointScopeChangeListener {
        
    private static final Logger logger = Logger.getLogger(com.sun.xml.ws.util.Constants.LoggingDomain + ".server.endpoint");
    
    private final @NotNull QName serviceName;
    private final @NotNull QName portName;
    protected final WSBinding binding;
    private final SEIModel seiModel;
    private final @NotNull Container container;
    private final WSDLPort port;

    protected final Tube masterTubeline;
    private final ServiceDefinitionImpl serviceDef;
    private final SOAPVersion soapVersion;
    private final Engine engine;
    private final @NotNull Codec masterCodec;
    private final @NotNull PolicyMap endpointPolicy;
    private final Pool<Tube> tubePool;
    private final OperationDispatcher operationDispatcher;
    private @NotNull ManagedObjectManager managedObjectManager;
    private boolean managedObjectManagerClosed = false;
    private final Object managedObjectManagerLock = new Object();
    private LazyMOMProvider.Scope lazyMOMProviderScope = LazyMOMProvider.Scope.STANDALONE;
    private final @NotNull ServerTubeAssemblerContext context;

    private Map<QName, WSEndpointReference.EPRExtension> endpointReferenceExtensions = new HashMap<QName, WSEndpointReference.EPRExtension>();
        /**
     * Set to true once we start shutting down this endpoint.
     * Used to avoid running the clean up processing twice.
         * 
         * @see #dispose()
         */
        private boolean disposed;

        private final Class<T> implementationClass;
    private final @NotNull WSDLProperties wsdlProperties;
        private final Set<Component> componentRegistry = new CopyOnWriteArraySet<Component>();

    protected WSEndpointImpl(@NotNull QName serviceName, @NotNull QName portName, WSBinding binding,
                   Container container, SEIModel seiModel, WSDLPort port,
                   Class<T> implementationClass,
                   @Nullable ServiceDefinitionImpl serviceDef,
                   EndpointAwareTube terminalTube, boolean isSynchronous,
                   PolicyMap endpointPolicy) {
                this.serviceName = serviceName;
                this.portName = portName;
                this.binding = binding;
                this.soapVersion = binding.getSOAPVersion();
                this.container = container;
                this.port = port;
                this.implementationClass = implementationClass;
                this.serviceDef = serviceDef;
                this.seiModel = seiModel;
        this.endpointPolicy = endpointPolicy;

        LazyMOMProvider.INSTANCE.registerEndpoint(this);
        initManagedObjectManager();

        if (serviceDef != null) {
            serviceDef.setOwner(this);
        }

        ComponentFeature cf = binding.getFeature(ComponentFeature.class);
        if (cf != null) {
            switch (cf.getTarget()) {
                case ENDPOINT:
                    componentRegistry.add(cf.getComponent());
                    break;
                case CONTAINER:
                    container.getComponents().add(cf.getComponent());
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        TubelineAssembler assembler = TubelineAssemblerFactory.create(
                Thread.currentThread().getContextClassLoader(), binding.getBindingId(), container);
        assert assembler != null;

        this.operationDispatcher = (port == null) ? null : new OperationDispatcher(port, binding, seiModel);

        context = createServerTubeAssemblerContext(terminalTube, isSynchronous);
        this.masterTubeline = assembler.createServer(context);

        Codec c = context.getCodec();
        if (c instanceof EndpointAwareCodec) {
            // create a copy to avoid sharing the codec between multiple endpoints 
            c = c.copy();
            ((EndpointAwareCodec) c).setEndpoint(this);
        }
        this.masterCodec = c;

        tubePool = new TubePool(masterTubeline);
        terminalTube.setEndpoint(this);
        engine = new Engine(toString(), container);
        wsdlProperties = (port == null) ? new WSDLDirectProperties(serviceName, portName, seiModel) : new WSDLPortProperties(port, seiModel);

        Map<QName, WSEndpointReference.EPRExtension> eprExtensions = new HashMap<QName, WSEndpointReference.EPRExtension>();
        try {
            if (port != null) {
                //gather EPR extrensions from WSDL Model
                WSEndpointReference wsdlEpr = ((WSDLPortImpl) port).getEPR();
                if (wsdlEpr != null) {
                    for (WSEndpointReference.EPRExtension extnEl : wsdlEpr.getEPRExtensions()) {
                        eprExtensions.put(extnEl.getQName(), extnEl);
                    }
                }
            }

            EndpointReferenceExtensionContributor[] eprExtnContributors = ServiceFinder.find(EndpointReferenceExtensionContributor.class).toArray();
            for(EndpointReferenceExtensionContributor eprExtnContributor :eprExtnContributors) {
                WSEndpointReference.EPRExtension wsdlEPRExtn = eprExtensions.remove(eprExtnContributor.getQName());
                    WSEndpointReference.EPRExtension endpointEprExtn = eprExtnContributor.getEPRExtension(this,wsdlEPRExtn);
                    if (endpointEprExtn != null) {
                        eprExtensions.put(endpointEprExtn.getQName(), endpointEprExtn);
                    }
            }
            for (WSEndpointReference.EPRExtension extn : eprExtensions.values()) {
                endpointReferenceExtensions.put(extn.getQName(), new WSEPRExtension(
                        XMLStreamBuffer.createNewBufferFromXMLStreamReader(extn.readAsXMLStreamReader()),extn.getQName()));
            }
        } catch (XMLStreamException ex) {
            throw new WebServiceException(ex);
        }
        if(!eprExtensions.isEmpty()) {
            serviceDef.addFilter(new EPRSDDocumentFilter(this));
        }
  }

  protected ServerTubeAssemblerContext createServerTubeAssemblerContext(
            EndpointAwareTube terminalTube, boolean isSynchronous) {
    ServerTubeAssemblerContext ctx = new ServerPipeAssemblerContext(
        seiModel, port, this, terminalTube, isSynchronous);
    return ctx;
  }

        protected WSEndpointImpl(@NotNull QName serviceName, @NotNull QName portName, WSBinding binding, Container container,
                        SEIModel seiModel, WSDLPort port,
                        Tube masterTubeline) {
                this.serviceName = serviceName;
                this.portName = portName;
                this.binding = binding;
                this.soapVersion = binding.getSOAPVersion();
                this.container = container;
                this.endpointPolicy = null;
                this.port = port;
                this.seiModel = seiModel;
                this.serviceDef = null;
                this.implementationClass = null;
                this.masterTubeline = masterTubeline;
                this.masterCodec = ((BindingImpl) this.binding).createCodec();

        LazyMOMProvider.INSTANCE.registerEndpoint(this);
        initManagedObjectManager();

        this.operationDispatcher = (port == null) ? null : new OperationDispatcher(port, binding, seiModel);
            this.context = new ServerPipeAssemblerContext(
                seiModel, port, this, null /* not known */, false);

                tubePool = new TubePool(masterTubeline);
                engine = new Engine(toString(), container);
                wsdlProperties = (port == null) ? new WSDLDirectProperties(serviceName, portName, seiModel) : new WSDLPortProperties(port, seiModel);
  }

    public Collection<WSEndpointReference.EPRExtension> getEndpointReferenceExtensions() {
        return endpointReferenceExtensions.values();
    }
    
    /**
     * Nullable when there is no associated WSDL Model
     * @return
     */
    public @Nullable OperationDispatcher getOperationDispatcher() {
        return operationDispatcher;
    }

    public PolicyMap getPolicyMap() {
            return endpointPolicy;
    }

    public @NotNull Class<T> getImplementationClass() {
                return implementationClass;
        }

    public @NotNull WSBinding getBinding() {
                return binding;
        }

    public @NotNull Container getContainer() {
                return container;
        }

        public WSDLPort getPort() {
                return port;
        }

        @Override
    public @Nullable SEIModel getSEIModel() {
                return seiModel;
        }

        public void setExecutor(Executor exec) {
                engine.setExecutor(exec);
        }
        
        @Override
        public Engine getEngine() {
                return engine;
        }

    public void schedule(final Packet request, final CompletionCallback callback, FiberContextSwitchInterceptor interceptor) {
        processAsync(request, callback, interceptor, true);
    }

    private void processAsync(final Packet request,
            final CompletionCallback callback,
            FiberContextSwitchInterceptor interceptor, boolean schedule) {
        Container old = ContainerResolver.getDefault().enterContainer(container);
        try {
            request.endpoint = WSEndpointImpl.this;
            request.addSatellite(wsdlProperties);

            Fiber fiber = engine.createFiber();
            if (interceptor != null) {
                fiber.addInterceptor(interceptor);
            }
            final Tube tube = tubePool.take();
            Fiber.CompletionCallback cbak = new Fiber.CompletionCallback() {
                public void onCompletion(@NotNull Packet response) {
                    tubePool.recycle(tube);
                    if (callback != null) {
                        callback.onCompletion(response);
                    }
                }

                public void onCompletion(@NotNull Throwable error) {
                    // let's not reuse tubes as they might be in a wrong state,
                    // so not
                    // calling tubePool.recycle()
                    // Convert all runtime exceptions to Packet so that
                    // transport doesn't
                    // have to worry about converting to wire message
                    // TODO XML/HTTP binding
                    Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(
                            soapVersion, null, error);
                    Packet response = request.createServerResponse(faultMsg,
                            request.endpoint.getPort(), null,
                            request.endpoint.getBinding());
                    if (callback != null) {
                        callback.onCompletion(response);
                    }
                }
            };

            fiber.start(tube, request, cbak,
                    binding.isFeatureEnabled(SyncStartForAsyncFeature.class)
                            || !schedule);
        } finally {
            ContainerResolver.getDefault().exitContainer(old);
        }
    }

    @Override
    public void process(final Packet request, final CompletionCallback callback, FiberContextSwitchInterceptor interceptor) {
        processAsync(request, callback, interceptor, false);
    }

    public @NotNull
    PipeHead createPipeHead() {
        return new PipeHead() {
            private final Tube tube = TubeCloner.clone(masterTubeline);

            public @NotNull
            Packet process(Packet request, WebServiceContextDelegate wscd,
                    TransportBackChannel tbc) {
                Container old = ContainerResolver.getDefault().enterContainer(container);
                try {
                    request.webServiceContextDelegate = wscd;
                    request.transportBackChannel = tbc;
                    request.endpoint = WSEndpointImpl.this;
                    request.addSatellite(wsdlProperties);

                    Fiber fiber = engine.createFiber();
                    Packet response;
                    try {
                        response = fiber.runSync(tube, request);
                    } catch (RuntimeException re) {
                        // Catch all runtime exceptions so that transport
                        // doesn't
                        // have to worry about converting to wire message
                        // TODO XML/HTTP binding
                        Message faultMsg = SOAPFaultBuilder
                                .createSOAPFaultMessage(soapVersion, null, re);
                        response = request.createServerResponse(faultMsg,
                                request.endpoint.getPort(), null,
                                request.endpoint.getBinding());
                    }
                    return response;
                } finally {
                    ContainerResolver.getDefault().exitContainer(old);
                }
            }
        };
    }

        public synchronized void dispose() {
                if (disposed)
                        return;
                disposed = true;

    masterTubeline.preDestroy();

                for (Handler handler : binding.getHandlerChain()) {
                        for (Method method : handler.getClass().getMethods()) {
                                if (method.getAnnotation(PreDestroy.class) == null) {
                                        continue;
                                }
                                try {
                                        method.invoke(handler);
                                } catch (Exception e) {
                    logger.log(Level.WARNING, HandlerMessages.HANDLER_PREDESTROY_IGNORE(e.getMessage()), e);
                                }
                                break;
                        }
                }
        closeManagedObjectManager();
            LazyMOMProvider.INSTANCE.unregisterEndpoint(this);
        }

        public ServiceDefinitionImpl getServiceDefinition() {
                return serviceDef;
        }

        public Set<EndpointComponent> getComponentRegistry() {
                Set<EndpointComponent> sec = new EndpointComponentSet();
                for (Component c : componentRegistry) {
                        sec.add(c instanceof EndpointComponentWrapper ?
                                ((EndpointComponentWrapper) c).component :
                                new ComponentWrapper(c));
                }
                return sec;
        }
        
        private class EndpointComponentSet extends HashSet<EndpointComponent> {

                @Override
                public Iterator<EndpointComponent> iterator() {
                        final Iterator<EndpointComponent> it = super.iterator();
                        return new Iterator<EndpointComponent>() {
                                private EndpointComponent last = null;
                                
                                public boolean hasNext() {
                                        return it.hasNext();
                                }

                                public EndpointComponent next() {
                                        last = it.next();
                                        return last;
                                }

                                public void remove() {
                                        it.remove();
                                        if (last != null) {
                                                componentRegistry.remove(last instanceof ComponentWrapper ?
                                                                ((ComponentWrapper) last).component : 
                                                                new EndpointComponentWrapper(last));
                                        }
                                        last = null;
                                }
                        };
                }

                @Override
                public boolean add(EndpointComponent e) {
                        boolean result = super.add(e);
                        if (result) {
                                componentRegistry.add(new EndpointComponentWrapper(e));
                        }
                        return result;
                }

                @Override
                public boolean remove(Object o) {
                        boolean result = super.remove(o);
                        if (result) {
                                componentRegistry.remove(o instanceof ComponentWrapper ?
                                                ((ComponentWrapper) o).component : 
                                                new EndpointComponentWrapper((EndpointComponent)o));
                        }
                        return result;
                }
                
        }
        
        private static class ComponentWrapper implements EndpointComponent {
                private final Component component;
                
                public ComponentWrapper(Component component) {
                        this.component = component;
                }

                public <S> S getSPI(Class<S> spiType) {
                        return component.getSPI(spiType);
                }

                @Override
                public int hashCode() {
                        return component.hashCode();
                }

                @Override
                public boolean equals(Object obj) {
                    return component.equals(obj);
                }
        }

        private static class EndpointComponentWrapper implements Component {
                private final EndpointComponent component;

                public EndpointComponentWrapper(EndpointComponent component) {
                        this.component = component;
                }
                
                public <S> S getSPI(Class<S> spiType) {
                        return component.getSPI(spiType);
                }

                @Override
                public int hashCode() {
                        return component.hashCode();
                }

                @Override
                public boolean equals(Object obj) {
                        return component.equals(obj);
                }
        }

        @Override
        public @NotNull Set<Component> getComponents() {
                return componentRegistry;
        }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, String address, String wsdlAddress, Element... referenceParameters) {
        List<Element> refParams = null;
        if (referenceParameters != null) {
            refParams = Arrays.asList(referenceParameters);
        }
        return getEndpointReference(clazz, address, wsdlAddress, null, refParams);
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz,
            String address, String wsdlAddress, List<Element> metadata,
            List<Element> referenceParameters) {
        QName portType = null;
        if (port != null) {
            portType = port.getBinding().getPortTypeName();
        }

        AddressingVersion av = AddressingVersion.fromSpecClass(clazz);
        return new WSEndpointReference(
                av, address, serviceName, portName, portType, metadata, wsdlAddress, referenceParameters, endpointReferenceExtensions.values(), null).toSpec(clazz);

    }

    public @NotNull QName getPortName() {
                return portName;
        }


    public @NotNull Codec createCodec() {
                return masterCodec.copy();
        }

    public @NotNull QName getServiceName() {
                return serviceName;
        }
    
    private void initManagedObjectManager() {
        synchronized (managedObjectManagerLock) {
            if (managedObjectManager == null) {
                switch (this.lazyMOMProviderScope) {
                    case GLASSFISH_NO_JMX:
                        managedObjectManager = new WSEndpointMOMProxy(this);
                        break;
                    default:
                        managedObjectManager = obtainManagedObjectManager();
                }
            }
        }
    }

    public @NotNull ManagedObjectManager getManagedObjectManager() {
        return managedObjectManager;
    }

    /**
     * Obtains a real instance of {@code ManagedObjectManager} class no matter what lazyMOMProviderScope is this endpoint in (or if the
     * Gmbal API calls should be deferred).
     *
     * @see com.sun.xml.ws.api.server.LazyMOMProvider.Scope
     * @return an instance of {@code ManagedObjectManager}
     */
    @NotNull ManagedObjectManager obtainManagedObjectManager() {
        final MonitorRootService monitorRootService = new MonitorRootService(this);
        final ManagedObjectManager mOM = monitorRootService.createManagedObjectManager(this);

        // ManagedObjectManager was suspended due to root creation (see MonitorBase#initMOM)
        mOM.resumeJMXRegistration();

        return mOM;
    }

    public void scopeChanged(LazyMOMProvider.Scope scope) {
        synchronized (managedObjectManagerLock) {
            if (managedObjectManagerClosed) {
                return;
            }
            
            this.lazyMOMProviderScope = scope;

            // possible lazyMOMProviderScope change can be LazyMOMProvider.Scope.GLASSFISH_NO_JMX or LazyMOMProvider.Scope.GLASSFISH_JMX
            if (managedObjectManager == null) {
                if (scope != LazyMOMProvider.Scope.GLASSFISH_NO_JMX) {
                    managedObjectManager = obtainManagedObjectManager();
                } else {
                    managedObjectManager = new WSEndpointMOMProxy(this);
                }
            } else {
                // if ManagedObjectManager for this endpoint has already been created and is uninitialized proxy then
                // fill it with a real instance
                if (managedObjectManager instanceof WSEndpointMOMProxy
                        && !((WSEndpointMOMProxy)managedObjectManager).isInitialized()) {
                    ((WSEndpointMOMProxy)managedObjectManager).setManagedObjectManager(obtainManagedObjectManager());
                }
            }
        }
    }

    // This can be called independently of WSEndpoint.dispose.
    // Example: the WSCM framework calls this before dispose.
    public void closeManagedObjectManager() {
        synchronized (managedObjectManagerLock) {
            if (managedObjectManagerClosed == true) {
                return;
            }
            if (managedObjectManager != null) {
                boolean close = true;

                // ManagedObjectManager doesn't need to be closed because it exists only as a proxy
                if (managedObjectManager instanceof WSEndpointMOMProxy
                        && !((WSEndpointMOMProxy)managedObjectManager).isInitialized()) {
                    close = false;
                }
                
                if (close) {
                    // no further notification on scope change
                    MonitorBase.closeMOM(managedObjectManager);
                }
            }
            managedObjectManagerClosed = true;
        }
    }

    public @NotNull ServerTubeAssemblerContext getAssemblerContext() {
        return context;
    }
}

