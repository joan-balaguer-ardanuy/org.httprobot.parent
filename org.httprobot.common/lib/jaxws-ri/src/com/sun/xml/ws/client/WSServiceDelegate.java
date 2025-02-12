/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.client;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.Closeable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.ComponentFeature;
import com.sun.xml.ws.api.ComponentFeature.Target;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.client.ServiceInterceptor;
import com.sun.xml.ws.api.client.ServiceInterceptorFactory;
import com.sun.xml.ws.api.databinding.DatabindingFactory;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.ContainerResolver;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.client.HandlerConfigurator.AnnotationConfigurator;
import com.sun.xml.ws.client.HandlerConfigurator.HandlerResolverImpl;
import com.sun.xml.ws.client.sei.SEIStub;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.developer.UsesJAXBContextFeature;
import com.sun.xml.ws.model.RuntimeModeler;
import com.sun.xml.ws.model.SOAPSEIModel;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.model.wsdl.WSDLServiceImpl;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.resources.DispatchMessages;
import com.sun.xml.ws.resources.ProviderApiMessages;
import com.sun.xml.ws.util.JAXWSUtils;
import com.sun.xml.ws.util.ServiceConfigurationError;
import com.sun.xml.ws.util.ServiceFinder;
import static com.sun.xml.ws.util.xml.XmlUtil.createDefaultCatalogResolver;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.soap.AddressingFeature;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * <code>Service</code> objects provide the client view of a Web service.
 *
 * <p><code>Service</code> acts as a factory of the following:
 * <ul>
 * <li>Proxies for a target service endpoint.
 * <li>Instances of <code>javax.xml.ws.Dispatch</code> for
 * dynamic message-oriented invocation of a remote
 * operation.
 * </li>
 *
 * <p>The ports available on a service can be enumerated using the
 * <code>getPorts</code> method. Alternatively, you can pass a
 * service endpoint interface to the unary <code>getPort</code> method
 * and let the runtime select a compatible port.
 *
 * <p>Handler chains for all the objects created by a <code>Service</code>
 * can be set by means of the provided <code>HandlerRegistry</code>.
 *
 * <p>An <code>Executor</code> may be set on the service in order
 * to gain better control over the threads used to dispatch asynchronous
 * callbacks. For instance, thread pooling with certain parameters
 * can be enabled by creating a <code>ThreadPoolExecutor</code> and
 * registering it with the service.
 *
 * @author WS Development Team
 * @see Executor
 * @since JAX-WS 2.0
 */
public class WSServiceDelegate extends WSService {
    /**
     * All ports.
     * <p>
     * This includes ports statically known to WSDL, as well as
     * ones that are dynamically added
     * through {@link #addPort(QName, String, String)}.
     * <p>
     * For statically known ports we'll have {@link SEIPortInfo}.
     * For dynamically added ones we'll have {@link PortInfo}.
     */
    private final Map<QName, PortInfo> ports = new HashMap<QName, PortInfo>();
    // For monitoring
    protected Map<QName, PortInfo> getQNameToPortInfoMap() { return ports; }

    /**
     * Whenever we create {@link BindingProvider}, we use this to configure handlers.
     */
    private @NotNull HandlerConfigurator handlerConfigurator = new HandlerResolverImpl(null);

    private final Class<? extends Service> serviceClass;
    
    private final WebServiceFeatureList features;

    /**
     * Name of the service for which this {@link WSServiceDelegate} is created for.
     */
    private final @NotNull QName serviceName;

    /**
     * Information about SEI, keyed by their interface type.
     */
   // private final Map<Class,SEIPortInfo> seiContext = new HashMap<Class,SEIPortInfo>();
   private final Map<QName,SEIPortInfo> seiContext = new HashMap<QName,SEIPortInfo>();

    // This executor is used for all the async invocations for all proxies
    // created from this service. But once the proxy is created, then changing
    // this executor doesn't affect the already created proxies.
    private volatile Executor executor;

    /**
     * The WSDL service that this {@link Service} object represents.
     * <p>
     * This field is null iff no WSDL is given to {@link Service}.
     * This fiels can be be null if the service is created without wsdl but later
     * the epr supplies a wsdl that can be parsed.
     */
    private  @Nullable WSDLServiceImpl wsdlService;

    private final Container container;
    /**
     * Multiple {@link ServiceInterceptor}s are aggregated into one.
     */
    /*package*/ final @NotNull ServiceInterceptor serviceInterceptor;


    public WSServiceDelegate(URL wsdlDocumentLocation, QName serviceName, Class<? extends Service> serviceClass, WebServiceFeature... features) {
        this(wsdlDocumentLocation, serviceName, serviceClass, new WebServiceFeatureList(features));
    }

    protected WSServiceDelegate(URL wsdlDocumentLocation, QName serviceName, Class<? extends Service> serviceClass, WebServiceFeatureList features) {
        this(
            wsdlDocumentLocation==null ? null : new StreamSource(wsdlDocumentLocation.toExternalForm()),
            serviceName,serviceClass, features);
    }

    /**
     * @param serviceClass
     *      Either {@link Service}.class or other generated service-derived classes.
     */
    public WSServiceDelegate(@Nullable Source wsdl, @NotNull QName serviceName, @NotNull final Class<? extends Service> serviceClass, WebServiceFeature... features) {
    	this(wsdl, serviceName, serviceClass, new WebServiceFeatureList(features));
    }
    
    /**
     * @param serviceClass
     *      Either {@link Service}.class or other generated service-derived classes.
     */
    protected WSServiceDelegate(@Nullable Source wsdl, @NotNull QName serviceName, @NotNull final Class<? extends Service> serviceClass, WebServiceFeatureList features) {
        this(wsdl, null, serviceName, serviceClass, features);
    }
    
    /**
     * @param serviceClass
     *      Either {@link Service}.class or other generated service-derived classes.
     */
    public WSServiceDelegate(@Nullable Source wsdl, @Nullable WSDLServiceImpl service, @NotNull QName serviceName, @NotNull final Class<? extends Service> serviceClass, WebServiceFeature... features) {
        this(wsdl, service, serviceName, serviceClass, new WebServiceFeatureList(features));
    }
    
    /**
     * @param serviceClass
     *      Either {@link Service}.class or other generated service-derived classes.
     */
    public WSServiceDelegate(@Nullable Source wsdl, @Nullable WSDLServiceImpl service, @NotNull QName serviceName, @NotNull final Class<? extends Service> serviceClass, WebServiceFeatureList features) {
        //we cant create a Service without serviceName
        if (serviceName == null)
            throw new WebServiceException(ClientMessages.INVALID_SERVICE_NAME_NULL(serviceName));

        this.features = features;
        
        InitParams initParams = INIT_PARAMS.get();
        INIT_PARAMS.set(null);  // mark it as consumed
        if(initParams==null)    initParams = EMPTY_PARAMS;

        this.serviceName = serviceName;
        this.serviceClass = serviceClass;
        Container tContainer = initParams.getContainer()!=null ? initParams.getContainer() : ContainerResolver.getInstance().getContainer();
        if (tContainer == Container.NONE) {
            tContainer = new ClientContainer();
        }
        this.container = tContainer;

        ComponentFeature cf = this.features.get(ComponentFeature.class);
        if (cf != null) {
            switch(cf.getTarget()) {
                case SERVICE:
                    getComponents().add(cf.getComponent());
                    break;
                case CONTAINER:
                    this.container.getComponents().add(cf.getComponent());
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        // load interceptor
        ServiceInterceptor interceptor = ServiceInterceptorFactory.load(this, Thread.currentThread().getContextClassLoader());
        ServiceInterceptor si = container.getSPI(ServiceInterceptor.class);
        if (si != null) {
            interceptor = ServiceInterceptor.aggregate(interceptor, si);
        }
        this.serviceInterceptor = interceptor;

        if (service == null) {
	        //if wsdl is null, try and get it from the WebServiceClient.wsdlLocation
	        if(wsdl == null){
	            if(serviceClass != Service.class){
	                WebServiceClient wsClient = AccessController.doPrivileged(new PrivilegedAction<WebServiceClient>() {
	                        public WebServiceClient run() {
	                            return serviceClass.getAnnotation(WebServiceClient.class);
	                        }
	                    });
	                String wsdlLocation = wsClient.wsdlLocation();
	                wsdlLocation = JAXWSUtils.absolutize(JAXWSUtils.getFileOrURLName(wsdlLocation));
	                wsdl = new StreamSource(wsdlLocation);
	            }
	        }
	        if (wsdl != null) {
	            try {
	                URL url = wsdl.getSystemId()==null ? null : JAXWSUtils.getEncodedURL(wsdl.getSystemId());
	                WSDLModelImpl model = parseWSDL(url, wsdl, serviceClass);
	                service = model.getService(this.serviceName);
	                if (service == null)
	                    throw new WebServiceException(
	                        ClientMessages.INVALID_SERVICE_NAME(this.serviceName,
	                            buildNameList(model.getServices().keySet())));
	                // fill in statically known ports
	                for (WSDLPort port : service.getPorts())
	                    ports.put(port.getName(), new PortInfo(this, port));
	            } catch (MalformedURLException e) {
	                throw new WebServiceException(ClientMessages.INVALID_WSDL_URL(wsdl.getSystemId()));
	            }
	        }
        }
        this.wsdlService = service;

        if (serviceClass != Service.class) {
            //if @HandlerChain present, set HandlerResolver on service context
            HandlerChain handlerChain =
                    AccessController.doPrivileged(new PrivilegedAction<HandlerChain>() {
                        public HandlerChain run() {
                            return serviceClass.getAnnotation(HandlerChain.class);
                        }
                    });
            if (handlerChain != null)
                handlerConfigurator = new AnnotationConfigurator(this);
        }

    }

    /**
     * Parses the WSDL and builds {@link com.sun.xml.ws.api.model.wsdl.WSDLModel}.
     * @param wsdlDocumentLocation
     *      Either this or <tt>wsdl</tt> parameter must be given.
     *      Null location means the system won't be able to resolve relative references in the WSDL,
     */
    private WSDLModelImpl parseWSDL(URL wsdlDocumentLocation, Source wsdlSource, Class serviceClass) {
        try {
            return RuntimeWSDLParser.parse(wsdlDocumentLocation, wsdlSource, createCatalogResolver(),
                true, getContainer(), serviceClass, ServiceFinder.find(WSDLParserExtension.class).toArray());
        } catch (IOException e) {
            throw new WebServiceException(e);
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        } catch (SAXException e) {
            throw new WebServiceException(e);
        } catch (ServiceConfigurationError e) {
            throw new WebServiceException(e);
        }
    }

    protected EntityResolver createCatalogResolver() {
    	return createDefaultCatalogResolver();
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public HandlerResolver getHandlerResolver() {
        return handlerConfigurator.getResolver();
    }

    /*package*/ final HandlerConfigurator getHandlerConfigurator() {
        return handlerConfigurator;
    }

    public void setHandlerResolver(HandlerResolver resolver) {
        handlerConfigurator = new HandlerResolverImpl(resolver);
    }

    public <T> T getPort(QName portName, Class<T> portInterface) throws WebServiceException {
        return getPort(portName, portInterface, EMPTY_FEATURES);
    }

    public <T> T getPort(QName portName, Class<T> portInterface, WebServiceFeature... features) {
        if (portName == null || portInterface == null)
            throw new IllegalArgumentException();
        WSDLServiceImpl tWsdlService = this.wsdlService;
        if (tWsdlService == null) {
            // assigning it to local variable and not setting it back to this.wsdlService intentionally
            // as we don't want to include the service instance with information gathered from sei
            tWsdlService = getWSDLModelfromSEI(portInterface);
            //still null? throw error need wsdl metadata to create a proxy
            if (tWsdlService == null) {
                throw new WebServiceException(ProviderApiMessages.NO_WSDL_NO_PORT(portInterface.getName()));
            }

        }
        WSDLPortImpl portModel = getPortModel(tWsdlService, portName);
        return getPort(portModel.getEPR(), portName, portInterface, new WebServiceFeatureList(features));
    }

    public <T> T getPort(EndpointReference epr, Class<T> portInterface, WebServiceFeature... features) {
        return getPort(WSEndpointReference.create(epr),portInterface,features);
    }

    public <T> T getPort(WSEndpointReference wsepr, Class<T> portInterface, WebServiceFeature... features) {
        //get the portType from SEI, so that it can be used if EPR does n't have endpointName
        QName portTypeName = RuntimeModeler.getPortTypeName(portInterface);
        //if port name is not specified in EPR, it will use portTypeName to get it from the WSDL model.
        QName portName = getPortNameFromEPR(wsepr, portTypeName);
        return getPort(wsepr,portName,portInterface,new WebServiceFeatureList(features));
    }

    protected <T> T getPort(WSEndpointReference wsepr, QName portName, Class<T> portInterface,
                          WebServiceFeatureList features) {
        ComponentFeature cf = features.get(ComponentFeature.class);
        if (cf != null && !Target.STUB.equals(cf.getTarget())) {
            throw new IllegalArgumentException();
        }
        features.addAll(this.features);

        SEIPortInfo spi = addSEI(portName, portInterface, features);
        return createEndpointIFBaseProxy(wsepr,portName,portInterface,features, spi);
    }
    
    public <T> T getPort(Class<T> portInterface, WebServiceFeature... features) {
        //get the portType from SEI
        QName portTypeName = RuntimeModeler.getPortTypeName(portInterface);
        WSDLServiceImpl wsdlService = this.wsdlService;
        if(wsdlService == null) {
            // assigning it to local variable and not setting it back to this.wsdlService intentionally
            // as we don't want to include the service instance with information gathered from sei
            wsdlService = getWSDLModelfromSEI(portInterface);
            //still null? throw error need wsdl metadata to create a proxy
            if(wsdlService == null) {
                throw new WebServiceException(ProviderApiMessages.NO_WSDL_NO_PORT(portInterface.getName()));
            }
        }
        //get the first port corresponding to the SEI
        WSDLPortImpl port = wsdlService.getMatchingPort(portTypeName);
        if (port == null)
                throw new WebServiceException(ClientMessages.UNDEFINED_PORT_TYPE(portTypeName));
        QName portName = port.getName();
        return getPort(portName, portInterface,features);
    }

    public <T> T getPort(Class<T> portInterface) throws WebServiceException {
        return getPort(portInterface, EMPTY_FEATURES);
    }
    
    public void addPort(QName portName, String bindingId, String endpointAddress) throws WebServiceException {
        if (!ports.containsKey(portName)) {
            BindingID bid = (bindingId == null) ? BindingID.SOAP11_HTTP : BindingID.parse(bindingId);
            ports.put(portName,
                    new PortInfo(this, (endpointAddress == null) ? null :
                            EndpointAddress.create(endpointAddress), portName, bid));
        } else
            throw new WebServiceException(DispatchMessages.DUPLICATE_PORT(portName.toString()));
    }


    public <T> Dispatch<T> createDispatch(QName portName, Class<T>  aClass, Service.Mode mode) throws WebServiceException {
        return createDispatch(portName, aClass, mode, EMPTY_FEATURES);
    }

    @Override
    public <T> Dispatch<T> createDispatch(QName portName, WSEndpointReference wsepr, Class<T> aClass, Service.Mode mode, WebServiceFeature... features) {
    	return createDispatch(portName, wsepr, aClass, mode, new WebServiceFeatureList(features));
    }
    
    public <T> Dispatch<T> createDispatch(QName portName, WSEndpointReference wsepr, Class<T> aClass, Service.Mode mode, WebServiceFeatureList features) {
        PortInfo port = safeGetPort(portName);
        
        ComponentFeature cf = features.get(ComponentFeature.class);
        if (cf != null && !Target.STUB.equals(cf.getTarget())) {
            throw new IllegalArgumentException();
        }
        features.addAll(this.features);
        
        BindingImpl binding = port.createBinding(features, null, null);
        binding.setMode(mode);
        Dispatch<T> dispatch = Stubs.createDispatch(port, this, binding, aClass, mode, wsepr);
        serviceInterceptor.postCreateDispatch((WSBindingProvider) dispatch);
        return dispatch;
    }

    public <T> Dispatch<T> createDispatch(QName portName, Class<T> aClass, Service.Mode mode, WebServiceFeature... features) {
    	return createDispatch(portName, aClass, mode, new WebServiceFeatureList(features));
    }
    
    public <T> Dispatch<T> createDispatch(QName portName, Class<T> aClass, Service.Mode mode, WebServiceFeatureList features) {
        WSEndpointReference wsepr = null;
        boolean isAddressingEnabled = false;
        AddressingFeature af = features.get(AddressingFeature.class);
        if (af == null) {
            af = this.features.get(AddressingFeature.class);
        }
        if (af != null && af.isEnabled())
            isAddressingEnabled = true;
        MemberSubmissionAddressingFeature msa = features.get(MemberSubmissionAddressingFeature.class);
        if (msa == null) {
            msa = this.features.get(MemberSubmissionAddressingFeature.class);
        }
        if (msa != null && msa.isEnabled())
            isAddressingEnabled = true;
        if(isAddressingEnabled && wsdlService != null && wsdlService.get(portName) != null) {
            wsepr = wsdlService.get(portName).getEPR();
        }
        return createDispatch(portName, wsepr, aClass, mode, features);
    }

    public <T> Dispatch<T> createDispatch(EndpointReference endpointReference, Class<T> type, Service.Mode mode, WebServiceFeature... features) {
        WSEndpointReference wsepr = new WSEndpointReference(endpointReference);
        QName portName = addPortEpr(wsepr);
        return createDispatch(portName, wsepr, type, mode, features);
    }

    /**
     * Obtains {@link PortInfo} for the given name, with error check.
     */
    public
    @NotNull
    PortInfo safeGetPort(QName portName) {
        PortInfo port = ports.get(portName);
        if (port == null) {
            throw new WebServiceException(ClientMessages.INVALID_PORT_NAME(portName, buildNameList(ports.keySet())));
        }
        return port;
    }

    private StringBuilder buildNameList(Collection<QName> names) {
        StringBuilder sb = new StringBuilder();
        for (QName qn : names) {
            if (sb.length() > 0) sb.append(',');
            sb.append(qn);
        }
        return sb;
    }

    public EndpointAddress getEndpointAddress(QName qName) {
    	PortInfo p = ports.get(qName);
        return p != null ? p.targetEndpoint : null;
    }

    public Dispatch<Object> createDispatch(QName portName, JAXBContext jaxbContext, Service.Mode mode) throws WebServiceException {
        return createDispatch(portName, jaxbContext, mode, EMPTY_FEATURES);
    }

    @Override
    public Dispatch<Object> createDispatch(QName portName, WSEndpointReference wsepr, JAXBContext jaxbContext, Service.Mode mode, WebServiceFeature... features) {
    	return createDispatch(portName, wsepr, jaxbContext, mode, new WebServiceFeatureList(features));
    }
    
    protected Dispatch<Object> createDispatch(QName portName, WSEndpointReference wsepr, JAXBContext jaxbContext, Service.Mode mode, WebServiceFeatureList features) {
        PortInfo port = safeGetPort(portName);

        ComponentFeature cf = features.get(ComponentFeature.class);
        if (cf != null && !Target.STUB.equals(cf.getTarget())) {
            throw new IllegalArgumentException();
        }
        features.addAll(this.features);
        
        BindingImpl binding = port.createBinding(features, null, null);
        binding.setMode(mode);
        Dispatch<Object> dispatch = Stubs.createJAXBDispatch(
                port, binding, jaxbContext, mode,wsepr);
         serviceInterceptor.postCreateDispatch((WSBindingProvider)dispatch);
         return dispatch;
    }

    @Override
    public @NotNull Container getContainer() {
        return container;
    }

    public Dispatch<Object> createDispatch(QName portName, JAXBContext jaxbContext, Service.Mode mode, WebServiceFeature... webServiceFeatures) {
    	return createDispatch(portName, jaxbContext, mode, new WebServiceFeatureList(webServiceFeatures));
    }
    
    protected Dispatch<Object> createDispatch(QName portName, JAXBContext jaxbContext, Service.Mode mode, WebServiceFeatureList features) {
        WSEndpointReference wsepr = null;
        boolean isAddressingEnabled = false;
        AddressingFeature af = features.get(AddressingFeature.class);
        if (af == null) {
            af = this.features.get(AddressingFeature.class);
        }
        if (af != null && af.isEnabled())
            isAddressingEnabled = true;
        MemberSubmissionAddressingFeature msa = features.get(MemberSubmissionAddressingFeature.class);
        if (msa == null) {
            msa = this.features.get(MemberSubmissionAddressingFeature.class);
        }
        if (msa != null && msa.isEnabled())
            isAddressingEnabled = true;
        if(isAddressingEnabled && wsdlService != null && wsdlService.get(portName) != null) {
            wsepr = wsdlService.get(portName).getEPR();
        }
        return createDispatch(portName, wsepr, jaxbContext, mode, features);
    }

    public Dispatch<Object> createDispatch(EndpointReference endpointReference, JAXBContext context, Service.Mode mode, WebServiceFeature... features) {
        WSEndpointReference wsepr = new WSEndpointReference(endpointReference);
        QName portName = addPortEpr(wsepr);
        return createDispatch(portName, wsepr, context, mode, features);
    }

    private QName addPortEpr(WSEndpointReference wsepr) {
        if (wsepr == null)
            throw new WebServiceException(ProviderApiMessages.NULL_EPR());
        QName eprPortName = getPortNameFromEPR(wsepr, null);
        //add Port, if it does n't exist;
        // TODO: what if it has different epr address?
        {
            PortInfo portInfo = new PortInfo(this, (wsepr.getAddress() == null) ? null : EndpointAddress.create(wsepr.getAddress()), eprPortName,
                    getPortModel(wsdlService, eprPortName).getBinding().getBindingId());
            if (!ports.containsKey(eprPortName)) {
                ports.put(eprPortName, portInfo);
            }
        }
        return eprPortName;
    }
    
    /**
     *
     * @param wsepr EndpointReference from which portName will be extracted.
     *      If EndpointName ( port name) is null in EPR, then it will try to get if from WSDLModel using portType QName
     * @param portTypeName
     *          should be null in dispatch case
     *          should be non null in SEI case
     * @return
     *      port name from EPR after validating various metadat elements.
     *      Also if service instance does n't have wsdl,
     *      then it gets the WSDL metadata from EPR and builds wsdl model.
     */
    private QName getPortNameFromEPR(@NotNull WSEndpointReference wsepr, @Nullable QName portTypeName) {
        QName portName;
        WSEndpointReference.Metadata metadata = wsepr.getMetaData();
        QName eprServiceName = metadata.getServiceName();
        QName eprPortName = metadata.getPortName();
        if ((eprServiceName != null ) && !eprServiceName.equals(serviceName)) {
            throw new WebServiceException("EndpointReference WSDL ServiceName differs from Service Instance WSDL Service QName.\n"
                    + " The two Service QNames must match");
        }
        if (wsdlService == null) {
            Source eprWsdlSource = metadata.getWsdlSource();
            if (eprWsdlSource == null) {
                throw new WebServiceException(ProviderApiMessages.NULL_WSDL());
            }
            try {
                WSDLModelImpl eprWsdlMdl = parseWSDL(new URL(wsepr.getAddress()), eprWsdlSource, null);
                wsdlService = eprWsdlMdl.getService(serviceName);
                if (wsdlService == null)
                    throw new WebServiceException(ClientMessages.INVALID_SERVICE_NAME(serviceName,
                            buildNameList(eprWsdlMdl.getServices().keySet())));
            } catch (MalformedURLException e) {
                throw new WebServiceException(ClientMessages.INVALID_ADDRESS(wsepr.getAddress()));
            }
        }
        portName = eprPortName;

        if (portName == null && portTypeName != null) {
            //get the first port corresponding to the SEI
            WSDLPortImpl port = wsdlService.getMatchingPort(portTypeName);
            if (port == null)
                throw new WebServiceException(ClientMessages.UNDEFINED_PORT_TYPE(portTypeName));
            portName = port.getName();
        }
        if (portName == null)
            throw new WebServiceException(ProviderApiMessages.NULL_PORTNAME());
        if (wsdlService.get(portName) == null)
            throw new WebServiceException(ClientMessages.INVALID_EPR_PORT_NAME(portName, buildWsdlPortNames()));

        return portName;

    }

    private WSDLServiceImpl getWSDLModelfromSEI(final Class sei) {
        WebService ws = AccessController.doPrivileged(new PrivilegedAction<WebService>() {
            public WebService run() {
                return (WebService) sei.getAnnotation(WebService.class);
            }
        });
        if (ws == null || ws.wsdlLocation().equals(""))
            return null;
        String wsdlLocation = ws.wsdlLocation();
        wsdlLocation = JAXWSUtils.absolutize(JAXWSUtils.getFileOrURLName(wsdlLocation));
        Source wsdl = new StreamSource(wsdlLocation);
        WSDLServiceImpl service = null;

        try {
            URL url = wsdl.getSystemId() == null ? null : new URL(wsdl.getSystemId());
            WSDLModelImpl model = parseWSDL(url, wsdl, sei);
            service = model.getService(this.serviceName);
            if (service == null)
                throw new WebServiceException(
                        ClientMessages.INVALID_SERVICE_NAME(this.serviceName,
                                buildNameList(model.getServices().keySet())));
        } catch (MalformedURLException e) {
            throw new WebServiceException(ClientMessages.INVALID_WSDL_URL(wsdl.getSystemId()));
        }
        return service;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public Iterator<QName> getPorts() throws WebServiceException {
        // KK: the spec seems to be ambigous about whether
        // this returns ports that are dynamically added or not.
        return ports.keySet().iterator();
    }

    public URL getWSDLDocumentLocation() {
        if(wsdlService==null)   return null;
        try {
            return new URL(wsdlService.getParent().getLocation().getSystemId());
        } catch (MalformedURLException e) {
            throw new AssertionError(e); // impossible
        }
    }

    private <T> T createEndpointIFBaseProxy(@Nullable WSEndpointReference epr,QName portName, Class<T> portInterface,
                                            WebServiceFeatureList webServiceFeatures, SEIPortInfo eif) {
        //fail if service doesnt have WSDL
        if (wsdlService == null)
            throw new WebServiceException(ClientMessages.INVALID_SERVICE_NO_WSDL(serviceName));

        if (wsdlService.get(portName)==null) {
            throw new WebServiceException(
                ClientMessages.INVALID_PORT_NAME(portName,buildWsdlPortNames()));
        }

        BindingImpl binding = eif.createBinding(webServiceFeatures,portInterface);
        InvocationHandler pis = getStubHandler(binding, eif, epr);

        // When creating the proxy, use a ClassLoader that can load classes
        // from both the interface class and also from this classes
        // classloader. This is necessary when this code is used in systems
        // such as OSGi where the class loader for the interface class may
        // not be able to load internal JAX-WS classes like 
        // "WSBindingProvider", but the class loader for this class may not
        // be able to load the interface class.
        ClassLoader loader = 
            getDelegatingLoader(portInterface.getClassLoader(),
                               WSServiceDelegate.class.getClassLoader());
        T proxy = portInterface.cast(Proxy.newProxyInstance(loader,
                new Class[]{portInterface, WSBindingProvider.class, Closeable.class}, pis));
        if (serviceInterceptor != null) {
            serviceInterceptor.postCreateProxy((WSBindingProvider)proxy, portInterface);
        }
        return proxy;
    }
    
    protected InvocationHandler getStubHandler(BindingImpl binding, SEIPortInfo eif, @Nullable WSEndpointReference epr) {
    	SEIPortInfo spi = (SEIPortInfo) eif;
    	return new SEIStub(eif, binding, eif.model, epr);
    }

    /**
     * Lists up the port names in WSDL. For error diagnostics.
     */
    private StringBuilder buildWsdlPortNames() {
        Set<QName> wsdlPortNames = new HashSet<QName>();
        for (WSDLPortImpl port : wsdlService.getPorts())
            wsdlPortNames.add(port.getName());
        return buildNameList(wsdlPortNames);
    }

    /**
     * Obtains a {@link WSDLPortImpl} with error check.
     *
     * @return guaranteed to be non-null.
     */
    public @NotNull WSDLPortImpl getPortModel(WSDLServiceImpl wsdlService, QName portName) {
        WSDLPortImpl port = wsdlService.get(portName);
        if (port == null)
            throw new WebServiceException(
                ClientMessages.INVALID_PORT_NAME(portName,buildWsdlPortNames()));
        return port;
    }

    /**
     * Contributes to the construction of {@link WSServiceDelegate} by filling in
     * {@link SEIPortInfo} about a given SEI (linked from the {@link Service}-derived class.)
     */
    //todo: valid port in wsdl
    private SEIPortInfo addSEI(QName portName, Class portInterface, WebServiceFeatureList features) throws WebServiceException {
        boolean ownModel = useOwnSEIModel(features);
        if (ownModel) {
            // Create a new model and do not cache it
            return createSEIPortInfo(portName, portInterface, features);
        }

        SEIPortInfo spi = seiContext.get(portName);
        if (spi == null) {
            spi = createSEIPortInfo(portName, portInterface, features);
            seiContext.put(spi.portName, spi);
            ports.put(spi.portName, spi);
        }
        return spi;
    }
    
    public SEIModel buildRuntimeModel(QName serviceName, QName portName, Class portInterface, WSDLPort wsdlPort, WebServiceFeatureList features) {
		DatabindingFactory fac = DatabindingFactory.newInstance();
		DatabindingConfig config = new DatabindingConfig();
		config.setContractClass(portInterface);
		config.getMappingInfo().setServiceName(serviceName);
		config.setWsdlPort(wsdlPort);
		config.setFeatures(features);
		config.setClassLoader(portInterface.getClassLoader());
		config.getMappingInfo().setPortName(portName);
		
		com.sun.xml.ws.db.DatabindingImpl rt = (com.sun.xml.ws.db.DatabindingImpl)fac.createRuntime(config);
		
		return rt.getModel();
    }

    private SEIPortInfo createSEIPortInfo(QName portName, Class portInterface, WebServiceFeatureList features) {
        WSDLPortImpl wsdlPort = getPortModel(wsdlService, portName);
        SEIModel model = buildRuntimeModel(serviceName, portName, portInterface, wsdlPort, features);
		
        return new SEIPortInfo(this, portInterface, (SOAPSEIModel) model, wsdlPort);
    }
    
    private boolean useOwnSEIModel(WebServiceFeatureList features) {
        return features.contains(UsesJAXBContextFeature.class);
    }

    public WSDLServiceImpl getWsdlService() {
        return wsdlService;
    }

     class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread daemonThread = new Thread(r);
            daemonThread.setDaemon(Boolean.TRUE);
            return daemonThread;
        }
    }

    protected static final WebServiceFeature[] EMPTY_FEATURES = new WebServiceFeature[0];
  
    private static ClassLoader getDelegatingLoader(ClassLoader loader1, ClassLoader loader2) {
    	if (loader1 == null) return loader2;
    	if (loader2 == null) return loader1;
    	return new DelegatingLoader(loader1, loader2);
    }
    
    private static final class DelegatingLoader extends ClassLoader {
        private final ClassLoader loader;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((loader == null) ? 0 : loader.hashCode());
            result = prime * result
                    + ((getParent() == null) ? 0 : getParent().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DelegatingLoader other = (DelegatingLoader) obj;
            if (loader == null) {
                if (other.loader != null)
                    return false;
            } else if (!loader.equals(other.loader))
                return false;
            if (getParent() == null) {
                if (other.getParent() != null)
                    return false;
            } else if (!getParent().equals(other.getParent()))
                return false;
            return true;
        }

        DelegatingLoader(ClassLoader loader1, ClassLoader loader2) {
            super(loader2);
            this.loader = loader1;
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            return loader.loadClass(name);
        }

        protected URL findResource(String name) {
            return loader.getResource(name);
        }
    }
}

