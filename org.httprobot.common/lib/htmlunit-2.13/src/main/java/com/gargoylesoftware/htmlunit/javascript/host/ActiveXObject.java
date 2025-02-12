/*
 * Copyright (c) 2002-2013 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.host;

import static com.gargoylesoftware.htmlunit.javascript.configuration.BrowserName.IE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.FunctionObject;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.configuration.ClassConfiguration;
import com.gargoylesoftware.htmlunit.javascript.configuration.JavaScriptConfiguration;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxSetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.WebBrowser;
import com.gargoylesoftware.htmlunit.javascript.host.xml.XMLDocument;
import com.gargoylesoftware.htmlunit.javascript.host.xml.XMLHttpRequest;

/**
 * This is the host object that allows JavaScript to instantiate java objects via the ActiveXObject
 * constructor. This host object enables a person to emulate ActiveXObjects in JavaScript with java
 * objects. See the <code>WebClient</code> class to see how ActiveXObject string parameter specifies
 * which java class is instantiated.
 *
 * @see com.gargoylesoftware.htmlunit.WebClient
 * @version $Revision: 8410 $
 * @author <a href="mailto:bcurren@esomnie.com">Ben Curren</a>
 * @author Ahmed Ashour
 * @author Chuck Dumont
 * @author Ronald Brill
 */
@JsxClass(browsers = @WebBrowser(IE))
public class ActiveXObject extends SimpleScriptable {

    private static final Log LOG = LogFactory.getLog(ActiveXObject.class);

    /**
     * The default constructor.
     */
    public ActiveXObject() {
    }

    /**
     * This method searches the map specified in the <code>WebClient</code> class for the java
     * object to instantiate based on the ActiveXObject constructor String.
     * @param cx the current context
     * @param args the arguments to the ActiveXObject constructor
     * @param ctorObj the function object
     * @param inNewExpr Is new or not
     * @return the java object to allow JavaScript to access
     */
    @JsxConstructor
    public static Scriptable jsConstructor(
            final Context cx, final Object[] args, final Function ctorObj,
            final boolean inNewExpr) {
        if (args.length < 1 || args.length > 2) {
            throw Context.reportRuntimeError(
                    "ActiveXObject Error: constructor must have one or two String parameters.");
        }
        if (args[0] == Context.getUndefinedValue()) {
            throw Context.reportRuntimeError("ActiveXObject Error: constructor parameter is undefined.");
        }
        if (!(args[0] instanceof String)) {
            throw Context.reportRuntimeError("ActiveXObject Error: constructor parameter must be a String.");
        }
        final String activeXName = (String) args[0];

        // quick and dirty hack
        // the js configuration should probably be extended to allow to specify something like
        // <class name="XMLHttpRequest"
        //   classname="com.gargoylesoftware.htmlunit.javascript.host.XMLHttpRequest"
        //   activeX="Microsoft.XMLHTTP">
        // and to build the object from the config
        if (isXMLHttpRequest(activeXName)) {
            return buildXMLHttpRequest();
        }

        if (isXMLDocument(activeXName)) {
            return buildXMLDocument(getWindow(ctorObj).getWebWindow());
        }

        if (isXMLTemplate(activeXName)) {
            return buildXSLTemplate();
        }

        final WebClient webClient = getWindow(ctorObj).getWebWindow().getWebClient();
        final Map<String, String> map = webClient.getActiveXObjectMap();
        if (map != null) {
            final Object mapValue = map.get(activeXName);
            if (mapValue != null) {
                final String xClassString = (String) mapValue;
                Object object = null;
                try {
                    final Class<?> xClass = Class.forName(xClassString);
                    object = xClass.newInstance();
                }
                catch (final Exception e) {
                    throw Context.reportRuntimeError("ActiveXObject Error: failed instantiating class " + xClassString
                            + " because " + e.getMessage() + ".");
                }
                return Context.toObject(object, ctorObj);
            }
        }
        if (webClient.getOptions().isActiveXNative() && System.getProperty("os.name").contains("Windows")) {
            try {
                return new ActiveXObjectImpl(activeXName);
            }
            catch (final Exception e) {
                LOG.warn("Error initiating Jacob", e);
            }
        }

        LOG.warn("Automation server can't create object for '" + activeXName + "'.");
        throw Context.reportRuntimeError("Automation server can't create object for '" + activeXName + "'.");
    }

    /**
     * Indicates if the ActiveX name is one flavor of XMLHttpRequest
     * @param name the ActiveX name
     * @return <code>true</code> if this is an XMLHttpRequest
     */
    static boolean isXMLHttpRequest(String name) {
        if (name == null) {
            return false;
        }
        name = name.toLowerCase(Locale.ENGLISH);
        return "microsoft.xmlhttp".equals(name) || name.startsWith("msxml2.xmlhttp");
    }

    /**
     * Indicates if the ActiveX name is one flavor of XMLDocument
     * @param name the ActiveX name
     * @return <code>true</code> if this is an XMLDocument
     */
    static boolean isXMLDocument(String name) {
        if (name == null) {
            return false;
        }
        name = name.toLowerCase(Locale.ENGLISH);
        return "microsoft.xmldom".equals(name)
            || name.matches("msxml\\d*\\.domdocument.*")
            || name.matches("msxml\\d*\\.freethreadeddomdocument.*");
    }

    /**
     * Indicates if the ActiveX name is one flavor of XMLTemplate.
     * @param name the ActiveX name
     * @return <code>true</code> if this is an XMLTemplate
     */
    static boolean isXMLTemplate(String name) {
        if (name == null) {
            return false;
        }
        name = name.toLowerCase(Locale.ENGLISH);
        return name.matches("msxml\\d*\\.xsltemplate.*");
    }

    private static Scriptable buildXMLHttpRequest() {
        final SimpleScriptable scriptable = new XMLHttpRequest(false);

        // Don't forget to update XMLHttpRequest.ALL_PROPERTIES_

        // the properties
        addProperty(scriptable, "onreadystatechange", true, true);
        addProperty(scriptable, "readyState", true, false);
        addProperty(scriptable, "responseText", true, false);
        addProperty(scriptable, "responseXML", true, false);
        addProperty(scriptable, "status", true, false);
        addProperty(scriptable, "statusText", true, false);

        // the functions
        addFunction(scriptable, "abort");
        addFunction(scriptable, "getAllResponseHeaders");
        addFunction(scriptable, "getResponseHeader");
        addFunction(scriptable, "open");
        addFunction(scriptable, "send");
        addFunction(scriptable, "setRequestHeader");

        return scriptable;
    }

    private static Scriptable buildXSLTemplate() {
        final SimpleScriptable scriptable = new XSLTemplate();

        addProperty(scriptable, "stylesheet", true, true);
        addFunction(scriptable, "createProcessor");

        return scriptable;
    }

    /**
     * Builds XMLDocument.
     * @param enclosingWindow the window
     * @return the document
     */
    public static XMLDocument buildXMLDocument(final WebWindow enclosingWindow) {
        final XMLDocument document = new XMLDocument(enclosingWindow);

        // the properties
        addProperty(document, "async", true, true);
        addProperty(document, "parseError", true, false);
        addProperty(document, "preserveWhiteSpace", true, true);
        addProperty(document, "xml", true, false);

        // the functions
        addFunction(document, "createNode");
        addFunction(document, "createCDATASection");
        addFunction(document, "createProcessingInstruction");
        addFunction(document, "getElementsByTagName");
        addFunction(document, "getProperty");
        addFunction(document, "load");
        addFunction(document, "loadXML");
        addFunction(document, "nodeFromID");
        addFunction(document, "selectNodes");
        addFunction(document, "selectSingleNode");
        addFunction(document, "setProperty");

        final JavaScriptConfiguration jsConfig = enclosingWindow.getWebClient()
            .getJavaScriptEngine().getJavaScriptConfiguration();

        for (String className = "Document"; StringUtils.isNotBlank(className);) {
            final ClassConfiguration classConfig = jsConfig.getClassConfiguration(className);
            for (final String function : classConfig.functionKeys()) {
                addFunction(document, function);
            }
            for (final Entry<String, ClassConfiguration.PropertyInfo> propertyEntry : classConfig.propertyEntries()) {
                final String propertyName = propertyEntry.getKey();
                final Method readMethod = propertyEntry.getValue().getReadMethod();
                final Method writeMethod = propertyEntry.getValue().getWriteMethod();
                addProperty(document, propertyName, readMethod != null, writeMethod != null);
            }
            className = classConfig.getExtendedClassName();

        }
        return document;
    }

    private static void addFunction(final SimpleScriptable scriptable, final String methodName) {
        final Method javaFunction = getMethod(scriptable.getClass(), methodName, JsxFunction.class);
        final FunctionObject fo = new FunctionObject(null, javaFunction, scriptable);
        scriptable.defineProperty(methodName, fo, READONLY);
    }

    /**
     * Adds a specific property to this object.
     * @param scriptable the scriptable
     * @param propertyName the property name
     * @param isGetter is getter
     * @param isSetter is setter
     */
    public static void addProperty(final SimpleScriptable scriptable, final String propertyName,
            final boolean isGetter, final boolean isSetter) {
        final String initialUpper = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        String getterName = null;
        if (isGetter) {
            getterName = "get" + initialUpper;
        }
        String setterName = null;
        if (isSetter) {
            setterName = "set" + initialUpper;
        }
        addProperty(scriptable, propertyName, getterName, setterName);
    }

    static void addProperty(final SimpleScriptable scriptable, final String propertyName,
            final String getterMethodName, final String setterMethodName) {
        scriptable.defineProperty(propertyName, null,
                getMethod(scriptable.getClass(), getterMethodName, JsxGetter.class),
                getMethod(scriptable.getClass(), setterMethodName, JsxSetter.class), PERMANENT);
    }

    /**
     * Gets the first method found of the class with the given name
     * and the correct annotation
     * @param clazz the class to search on
     * @param name the name of the searched method
     * @param annotationClass the class of the annotation required
     * @return <code>null</code> if not found
     */
    static Method getMethod(final Class<? extends SimpleScriptable> clazz,
            final String name, final Class<? extends Annotation> annotationClass) {
        if (name == null) {
            return null;
        }

        Method foundMethod = null;
        int foundByNameOnlyCount = 0;
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                if (null != method.getAnnotation(annotationClass)) {
                    return method;
                }
                foundByNameOnlyCount++;
                foundMethod = method;
            }
        }
        if (foundByNameOnlyCount > 1) {
            throw new IllegalArgumentException("Found " + foundByNameOnlyCount + " methods for name '"
                    + name + "' in class '" + clazz + "'.");
        }
        return foundMethod;
    }

    /**
     * Gets the name of the host object class.
     * @return the JavaScript class name
     */
    @Override
    public String getClassName() {
        return "ActiveXObject";
    }
}
