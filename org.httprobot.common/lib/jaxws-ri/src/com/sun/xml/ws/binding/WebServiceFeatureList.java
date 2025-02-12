/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.binding;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.ImpliesWebServiceFeature;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.FeatureConstructor;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLFeaturedObject;
import com.sun.xml.ws.model.RuntimeModelerException;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.resources.ModelerMessages;
import com.sun.xml.bind.util.Which;

import javax.xml.ws.RespectBinding;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;

import org.jvnet.ws.EnvelopeStyleFeature;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a list of {@link WebServiceFeature}s that has bunch of utility
 * methods pertaining to web service features.
 *
 * @author Rama Pulavarthi
 */
public final class WebServiceFeatureList extends AbstractMap<Class<? extends WebServiceFeature>, WebServiceFeature> implements WSFeatureList {
    public static WebServiceFeatureList toList(Iterable<WebServiceFeature> features) {
        if (features instanceof WebServiceFeatureList)
            return (WebServiceFeatureList) features;
        WebServiceFeatureList w = new WebServiceFeatureList();
        if (features != null)
            w.addAll(features);
        return w;
    }

    private Map<Class<? extends WebServiceFeature>, WebServiceFeature> wsfeatures = new HashMap<Class<? extends WebServiceFeature>, WebServiceFeature>();

    public WebServiceFeatureList() {
    }

    /**
     * Delegate to this parent if non-null.
     */
    private @Nullable
	WSDLFeaturedObject parent;

    public WebServiceFeatureList(@NotNull WebServiceFeature... features) {
        if (features != null) {
            for (WebServiceFeature f : features) {
                add(f);
            }
        }
    }

    public WebServiceFeatureList(WebServiceFeatureList features) {
        if (features != null) {
            wsfeatures.putAll(features.wsfeatures);
            parent = features.parent;
        }
    }

    /**
     * Creates a list by reading featuers from the annotation on a class.
     */
    public WebServiceFeatureList(@NotNull Class<?> endpointClass) {
        parseAnnotations(endpointClass);
    }

    /**
     * Adds the corresponding features to the list for feature annotations(i.e
     * which have {@link WebServiceFeatureAnnotation} meta annotation)
     *
     * @param annIt collection of annotations(that can have non-feature annotations)
     */
    public void parseAnnotations(Iterable<Annotation> annIt) {
        for(Annotation ann : annIt) {
            WebServiceFeature feature = getFeature(ann);
            if (feature != null) {
                add(feature);
            }
        }
    }

    /**
     * Returns a corresponding feature for a feature annotation(i.e which has
     * {@link WebServiceFeatureAnnotation} meta annotation)
     *
     * @return corresponding feature for the annotation
     *         null, if the annotation is nota feature annotation
     */
    public static WebServiceFeature getFeature(Annotation a) {
        WebServiceFeature ftr = null;
        if (!(a.annotationType().isAnnotationPresent(WebServiceFeatureAnnotation.class))) {
            ftr = null;
        } else if (a instanceof Addressing) {
            Addressing addAnn = (Addressing) a;
            try {
                ftr = new AddressingFeature(addAnn.enabled(), addAnn.required(),addAnn.responses());
            } catch(NoSuchMethodError e) {
                //throw error. We can't default to Responses.ALL as we dont know if the user has not used 2.2 annotation with responses.
                throw new RuntimeModelerException(ModelerMessages.RUNTIME_MODELER_ADDRESSING_RESPONSES_NOSUCHMETHOD(toJar(Which.which(Addressing.class))));
            }
        } else if (a instanceof MTOM) {
            MTOM mtomAnn = (MTOM) a;
            ftr = new MTOMFeature(mtomAnn.enabled(), mtomAnn.threshold());
        } else if (a instanceof RespectBinding) {
            RespectBinding rbAnn = (RespectBinding) a;
            ftr = new RespectBindingFeature(rbAnn.enabled());
        } else {
            ftr = getWebServiceFeatureBean(a);
        }
        return ftr;
    }

    /**
     *
     * @param endpointClass web service impl class
     */
    public void parseAnnotations(Class<?> endpointClass) {
        for (Annotation a : endpointClass.getAnnotations()) {
            WebServiceFeature ftr = getFeature(a);
            if (ftr != null) {
                if (ftr instanceof MTOMFeature) {
                    // check conflict with @BindingType
                    BindingID bindingID = BindingID.parse(endpointClass);
                    MTOMFeature bindingMtomSetting = bindingID.createBuiltinFeatureList().get(MTOMFeature.class);
                    if (bindingMtomSetting != null && bindingMtomSetting.isEnabled() ^ ftr.isEnabled()) {
                        throw new RuntimeModelerException(
                            ModelerMessages.RUNTIME_MODELER_MTOM_CONFLICT(bindingID, ftr.isEnabled()));
                    }
                }
                add(ftr);
            }
        }
    }

    /**
     * Given the URL String inside jar, returns the URL to the jar itself.
     */
    private static String toJar(String url) {
        if(!url.startsWith("jar:"))
            return url;
        url = url.substring(4); // cut off jar:
        return url.substring(0,url.lastIndexOf('!'));    // cut off everything after '!'
    }

    private static WebServiceFeature getWebServiceFeatureBean(Annotation a) {
        WebServiceFeatureAnnotation wsfa = a.annotationType().getAnnotation(WebServiceFeatureAnnotation.class);
        Class<? extends WebServiceFeature> beanClass = wsfa.bean();
        WebServiceFeature bean;

        Constructor ftrCtr = null;
        String[] paramNames = null;
        for (Constructor con : beanClass.getConstructors()) {
            FeatureConstructor ftrCtrAnn = (FeatureConstructor) con.getAnnotation(FeatureConstructor.class);
            if (ftrCtrAnn != null) {
                if (ftrCtr == null) {
                    ftrCtr = con;
                    paramNames = ftrCtrAnn.value();
                } else {
                    throw new WebServiceException(
                        ModelerMessages.RUNTIME_MODELER_WSFEATURE_MORETHANONE_FTRCONSTRUCTOR(a, beanClass));
                }
            }
        }
        if (ftrCtr == null) {
            bean = getWebServiceFeatureBeanViaBuilder(a, beanClass);
            if (bean != null) {
                return bean;
            } else {
                throw new WebServiceException(
                    ModelerMessages.RUNTIME_MODELER_WSFEATURE_NO_FTRCONSTRUCTOR(a, beanClass));
            }
        }
        if (ftrCtr.getParameterTypes().length != paramNames.length) {
            throw new WebServiceException(
                ModelerMessages.RUNTIME_MODELER_WSFEATURE_ILLEGAL_FTRCONSTRUCTOR(a, beanClass));
        }

        try {
            Object[] params = new Object[paramNames.length];
            for (int i = 0; i < paramNames.length; i++) {
                Method m = a.annotationType().getDeclaredMethod(paramNames[i]);
                params[i] = m.invoke(a);
            }
            bean = (WebServiceFeature) ftrCtr.newInstance(params);
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
        return bean;
    }

    private static WebServiceFeature getWebServiceFeatureBeanViaBuilder(
        final Annotation annotation,
        final Class<? extends WebServiceFeature> beanClass)
    {
        try {
            final Method featureBuilderMethod = beanClass.getDeclaredMethod("builder");
            final Object builder = featureBuilderMethod.invoke(beanClass);
            final Method buildMethod = builder.getClass().getDeclaredMethod("build");

            for (Method builderMethod : builder.getClass().getDeclaredMethods()) {
                if (!builderMethod.equals(buildMethod)) {
                    final String methodName = builderMethod.getName();
                    final Method annotationMethod = annotation.annotationType().getDeclaredMethod(methodName);
                    final Object annotationFieldValue = annotationMethod.invoke(annotation);
                    final Object[] arg = { annotationFieldValue };
                    builderMethod.invoke(builder, arg);
                }
            }

            final Object result = buildMethod.invoke(builder);
            if (result instanceof WebServiceFeature) {
                return (WebServiceFeature) result;
            } else {
                throw new WebServiceException("Not a WebServiceFeature: " + result);
            }
        } catch (final NoSuchMethodException e) {
            return null;
        } catch (final IllegalAccessException e) {
            throw new WebServiceException(e);
        } catch (final InvocationTargetException e) {
            throw new WebServiceException(e);
        }
    }

    public Iterator<WebServiceFeature> iterator() {
        if (parent != null)
            return new MergedFeatures(parent.getFeatures());
        return wsfeatures.values().iterator();
    }

    public @NotNull
	WebServiceFeature[] toArray() {
        if (parent != null)
            return new MergedFeatures(parent.getFeatures()).toArray();
        return wsfeatures.values().toArray(new WebServiceFeature[] {});
    }

    public boolean isEnabled(@NotNull Class<? extends WebServiceFeature> feature) {
        WebServiceFeature ftr = get(feature);
        return ftr != null && ftr.isEnabled();
    }

    public boolean contains(@NotNull Class<? extends WebServiceFeature> feature) {
        WebServiceFeature ftr = get(feature);
        return ftr != null;
    }

    public @Nullable
	<F extends WebServiceFeature> F get(@NotNull Class<F> featureType) {
        WebServiceFeature f = featureType.cast(wsfeatures.get(featureType));
        if (f == null && parent != null) {
            return parent.getFeatures().get(featureType);
        }
        return (F) f;
    }

    /**
     * Adds a feature to the list if it's not already added.
     */
    public void add(@NotNull WebServiceFeature f) {
        if (!wsfeatures.containsKey(f.getClass())) {
            wsfeatures.put(f.getClass(), f);

            if (f instanceof ImpliesWebServiceFeature)
                ((ImpliesWebServiceFeature) f).implyFeatures(this);
        }
    }

    /**
     * Adds features to the list if it's not already added.
     */
    public void addAll(@NotNull Iterable<WebServiceFeature> list) {
        for (WebServiceFeature f : list)
            add(f);
    }

    /**
     * Sets MTOM feature, overriding any existing feature.  This is necessary for compatibility
     * with the existing {@link SOAPBinding.setMTOMEnabled}.
     * @param b if MTOM will be enabled
     */
    void setMTOMEnabled(boolean b) {
    	wsfeatures.put(MTOMFeature.class, new MTOMFeature(b));
    }

    public boolean equals(Object other) {
    	if (!(other instanceof WebServiceFeatureList))
            return false;

    	WebServiceFeatureList w = (WebServiceFeatureList) other;
    	return wsfeatures.equals(w.wsfeatures) && (parent == w.parent);
    }

    public String toString() {
        return wsfeatures.toString();
    }

    /**
     * Merges the extra features that are not already set on binding.
     * i.e, if a feature is set already on binding through some other API
     * the corresponding wsdlFeature is not set.
     *
     * @param features          Web Service features that need to be merged with already configured features.
     * @param reportConflicts   If true, checks if the feature setting in WSDL (wsdl extension or
     *                          policy configuration) conflicts with feature setting in Deployed Service and
     *                          logs warning if there are any conflicts.
     */
    public void mergeFeatures(@NotNull Iterable<WebServiceFeature> features, boolean reportConflicts) {
        for (WebServiceFeature wsdlFtr : features) {
            if (get(wsdlFtr.getClass()) == null) {
                add(wsdlFtr);
            } else if (reportConflicts) {
                if (isEnabled(wsdlFtr.getClass()) != wsdlFtr.isEnabled()) {
                    LOGGER.warning(ModelerMessages.RUNTIME_MODELER_FEATURE_CONFLICT(
                                       get(wsdlFtr.getClass()), wsdlFtr));
                }
            }
        }
    }

    public void mergeFeatures(WebServiceFeature[] features, boolean reportConflicts) {
        for (WebServiceFeature wsdlFtr : features) {
            if (get(wsdlFtr.getClass()) == null) {
                add(wsdlFtr);
            } else if (reportConflicts) {
                if (isEnabled(wsdlFtr.getClass()) != wsdlFtr.isEnabled()) {
                    LOGGER.warning(ModelerMessages.RUNTIME_MODELER_FEATURE_CONFLICT(
                                       get(wsdlFtr.getClass()), wsdlFtr));
                }
            }
        }
    }

    /**
     * Extracts features from {@link WSDLPortImpl#getFeatures()}. Extra features
     * that are not already set on binding. i.e, if a feature is set already on
     * binding through someother API the coresponding wsdlFeature is not set.
     *
     * @param wsdlPort
     *            WSDLPort model
     * @param honorWsdlRequired
     *            If this is true add WSDL Feature only if wsd:Required=true In
     *            SEI case, it should be false In Provider case, it should be
     *            true
     * @param reportConflicts
     *            If true, checks if the feature setting in WSDL (wsdl extension
     *            or policy configuration) colflicts with feature setting in
     *            Deployed Service and logs warning if there are any conflicts.
     */
    public void mergeFeatures(@NotNull WSDLPort wsdlPort,
                              boolean honorWsdlRequired, boolean reportConflicts) {
        if (honorWsdlRequired && !isEnabled(RespectBindingFeature.class))
            return;
        if (!honorWsdlRequired) {
            addAll(wsdlPort.getFeatures());
            return;
        }
        // Add only if isRequired returns true, when honorWsdlRequired is true
        for (WebServiceFeature wsdlFtr : wsdlPort.getFeatures()) {
            if (get(wsdlFtr.getClass()) == null) {
                try {
                    // if it is a WSDL Extension , it will have required
                    // attribute
                    Method m = (wsdlFtr.getClass().getMethod("isRequired"));
                    try {
                        boolean required = (Boolean) m.invoke(wsdlFtr);
                        if (required)
                            add(wsdlFtr);
                    } catch (IllegalAccessException e) {
                        throw new WebServiceException(e);
                    } catch (InvocationTargetException e) {
                        throw new WebServiceException(e);
                    }
                } catch (NoSuchMethodException e) {
                    // this wsdlFtr is not an WSDL extension, just add it
                    add(wsdlFtr);
                }
            } else if (reportConflicts) {
                if (isEnabled(wsdlFtr.getClass()) != wsdlFtr.isEnabled()) {
                    LOGGER.warning(ModelerMessages.RUNTIME_MODELER_FEATURE_CONFLICT(
                                       get(wsdlFtr.getClass()), wsdlFtr));
                }

            }
        }
    }

    /**
     * Set the parent features. Basically the parent feature list will be
     * overriden by this feature list.
     */
    public void setParentFeaturedObject(@NotNull WSDLFeaturedObject parent) {
        this.parent = parent;
    }

    public static @Nullable <F extends WebServiceFeature> F getFeature(@NotNull WebServiceFeature[] features,
                                                                       @NotNull Class<F> featureType) {
        for (WebServiceFeature f : features) {
            if (f.getClass() == featureType)
                return (F) f;
        }
        return null;
    }

    /**
     * A Union of this WebServiceFeatureList and the parent.
     */
    private final class MergedFeatures implements Iterator<WebServiceFeature> {
        private final Stack<WebServiceFeature> features = new Stack<WebServiceFeature>();

        public MergedFeatures(@NotNull WSFeatureList parent) {

            for (WebServiceFeature f : wsfeatures.values()) {
                features.push(f);
            }

            for (WebServiceFeature f : parent) {
                if (!wsfeatures.containsKey(f.getClass())) {
                    features.push(f);
                }
            }
        }

        public boolean hasNext() {
            return !features.empty();
        }

        public WebServiceFeature next() {
            if (!features.empty()) {
                return features.pop();
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (!features.empty()) {
                features.pop();
            }
        }

        public WebServiceFeature[] toArray() {
            return features.toArray(new WebServiceFeature[] {});
        }
    }

    private static final Logger LOGGER = Logger.getLogger(WebServiceFeatureList.class.getName());

    @Override
    public Set<java.util.Map.Entry<Class<? extends WebServiceFeature>, WebServiceFeature>> entrySet() {
        return wsfeatures.entrySet();
    }

    @Override
    public WebServiceFeature put(Class<? extends WebServiceFeature> key, WebServiceFeature value) {
        return wsfeatures.put(key, value);
    }
    
    static public SOAPVersion getSoapVersion(WSFeatureList features) {
        EnvelopeStyleFeature env = features.get(EnvelopeStyleFeature.class);
        return env != null ? SOAPVersion.from(env) : null;
    }
    
    static public boolean isFeatureEnabled(Class<? extends WebServiceFeature> type, WebServiceFeature[] features) {
        WebServiceFeature ftr = getFeature(features, type);
        return ftr != null && ftr.isEnabled();
    }
    
    static public WebServiceFeature[] toFeatureArray(WSBinding binding) {
        //TODO scchen convert BindingID  to WebServiceFeature[]
        if(!binding.isFeatureEnabled(EnvelopeStyleFeature.class)) {
            WebServiceFeature[] f = { binding.getSOAPVersion().toFeature() };
            binding.getFeatures().mergeFeatures(f, false);
        }
        return binding.getFeatures().toArray();
    }
}
