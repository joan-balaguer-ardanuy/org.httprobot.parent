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

package com.sun.tools.ws.wscompile;

import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.ws.api.WsgenExtension;
import com.sun.tools.ws.api.WsgenProtocol;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.binding.SOAPBindingImpl;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

/**
 * @author Vivek Pandey
 */
public class WsgenOptions extends Options {
    /**
     * -servicename
     */
    public QName serviceName;

    /**
     * -portname
     */
    public QName portName;

    /**
     * -r
     */
    public File nonclassDestDir;


    /**
     * -wsdl
     */
    public boolean genWsdl;

    /**
     * -inlineSchemas
     */
    public boolean inlineSchemas;

    /**
     * protocol value
     */
    public String protocol = "soap1.1";

    public Set<String> protocols = new LinkedHashSet<String>();
    public Map<String, String> nonstdProtocols = new LinkedHashMap<String, String>();

    /**
     * -XwsgenReport
     */
    public File wsgenReport;

    /**
     * -Xdonotoverwrite
     */
    public boolean doNotOverWrite;

    /**
     * Tells if user specified a specific protocol
     */
    public boolean protocolSet = false;

    private static final String SERVICENAME_OPTION = "-servicename";
    private static final String PORTNAME_OPTION = "-portname";
    private static final String HTTP   = "http";
    private static final String SOAP11 = "soap1.1";
    public static final String X_SOAP12 = "Xsoap1.2";

    public WsgenOptions() {
        protocols.add(SOAP11);
        protocols.add(X_SOAP12);
        nonstdProtocols.put(X_SOAP12, SOAPBindingImpl.X_SOAP12HTTP_BINDING);
        ServiceFinder<WsgenExtension> extn = ServiceFinder.find(WsgenExtension.class);
        for(WsgenExtension ext : extn) {
            Class clazz = ext.getClass();
            WsgenProtocol pro = (WsgenProtocol)clazz.getAnnotation(WsgenProtocol.class);
            protocols.add(pro.token());
            nonstdProtocols.put(pro.token(), pro.lexical());
        }
    }

    @Override
    protected int parseArguments(String[] args, int i) throws BadCommandLineException {

        int j = super.parseArguments(args, i);
        if (args[i].equals(SERVICENAME_OPTION)) {
            serviceName = QName.valueOf(requireArgument(SERVICENAME_OPTION, args, ++i));
            if (serviceName.getNamespaceURI() == null || serviceName.getNamespaceURI().length() == 0) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_SERVICENAME_MISSING_NAMESPACE(args[i]));
            }
            if (serviceName.getLocalPart() == null || serviceName.getLocalPart().length() == 0) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_SERVICENAME_MISSING_LOCALNAME(args[i]));
            }
            return 2;
        } else if (args[i].equals(PORTNAME_OPTION)) {
            portName = QName.valueOf(requireArgument(PORTNAME_OPTION, args, ++i));
            if (portName.getNamespaceURI() == null || portName.getNamespaceURI().length() == 0) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_PORTNAME_MISSING_NAMESPACE(args[i]));
            }
            if (portName.getLocalPart() == null || portName.getLocalPart().length() == 0) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_PORTNAME_MISSING_LOCALNAME(args[i]));
            }
            return 2;
        } else if (args[i].equals("-r")) {
            nonclassDestDir = new File(requireArgument("-r", args, ++i));
            if (!nonclassDestDir.exists()) {
                throw new BadCommandLineException(WscompileMessages.WSCOMPILE_NO_SUCH_DIRECTORY(nonclassDestDir.getPath()));
            }
            return 2;
        } else if (args[i].startsWith("-wsdl")) {
            genWsdl = true;
            //String value = requireArgument("-wsdl", args, ++i).substring(5);
            String value = args[i].substring(5);
            int index = value.indexOf(':');
            if (index == 0) {
                value = value.substring(1);
                index = value.indexOf('/');
                if (index == -1) {
                    protocol = value;
                } else {
                    protocol = value.substring(0, index);
                }
                protocolSet = true;
            }
            return 1;
        } else if (args[i].equals("-XwsgenReport")) {
            // undocumented switch for the test harness
            wsgenReport = new File(requireArgument("-XwsgenReport", args, ++i));
            return 2;
        } else if (args[i].equals("-Xdonotoverwrite")) {
            doNotOverWrite = true;
            return 1;
        } else if (args[i].equals("-inlineSchemas")) {
            inlineSchemas = true;
            return 1;
        }

        return j;
    }


    @Override
    protected void addFile(String arg) {
        endpoints.add(arg);
    }

    List<String> endpoints = new ArrayList<String>();

    public Class endpoint;


    private boolean isImplClass;
    private boolean noWebServiceEndpoint;

    public void validate() throws BadCommandLineException {
        if(nonclassDestDir == null)
            nonclassDestDir = destDir;

        if (!protocols.contains(protocol)) {
            throw new BadCommandLineException(WscompileMessages.WSGEN_INVALID_PROTOCOL(protocol, protocols));
        }

        if (endpoints.isEmpty()) {
            throw new BadCommandLineException(WscompileMessages.WSGEN_MISSING_FILE());
        }
        if (protocol == null || protocol.equalsIgnoreCase(X_SOAP12) && !isExtensionMode()) {
            throw new BadCommandLineException(WscompileMessages.WSGEN_SOAP_12_WITHOUT_EXTENSION());
        }

        if (nonstdProtocols.containsKey(protocol) && !isExtensionMode()) {
            throw new BadCommandLineException(WscompileMessages.WSGEN_PROTOCOL_WITHOUT_EXTENSION(protocol));
        }
        if (inlineSchemas && !genWsdl) {
            throw new BadCommandLineException(WscompileMessages.WSGEN_INLINE_SCHEMAS_ONLY_WITH_WSDL());
        }

        validateEndpointClass();
        validateArguments();
    }
    /**
     * Get an implementation class annotated with @WebService annotation.
     */
    private void validateEndpointClass() throws BadCommandLineException {
        Class clazz = null;
        for(String cls : endpoints){
            clazz = getClass(cls);
            if (clazz == null)
                continue;

            if (clazz.isEnum() || clazz.isInterface() ||
                clazz.isPrimitive()) {
                continue;
            }
            isImplClass = true;
            WebService webService = (WebService) clazz.getAnnotation(WebService.class);
            if(webService == null)
                continue;
            break;
        }
        if(clazz == null){
            throw new BadCommandLineException(WscompileMessages.WSGEN_CLASS_NOT_FOUND(endpoints.get(0)));
        }
        if(!isImplClass){
            throw new BadCommandLineException(WscompileMessages.WSGEN_CLASS_MUST_BE_IMPLEMENTATION_CLASS(clazz.getName()));
        }
        if(noWebServiceEndpoint){
            throw new BadCommandLineException(WscompileMessages.WSGEN_NO_WEBSERVICES_CLASS(clazz.getName()));
        }
        endpoint = clazz;
        validateBinding();
    }

    private void validateBinding() throws BadCommandLineException {
        if (genWsdl) {
            BindingID binding = BindingID.parse(endpoint);
            if ((binding.equals(BindingID.SOAP12_HTTP) ||
                 binding.equals(BindingID.SOAP12_HTTP_MTOM)) &&
                    !(protocol.equals(X_SOAP12) && isExtensionMode())) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_CANNOT_GEN_WSDL_FOR_SOAP_12_BINDING(binding.toString(), endpoint.getName()));
            }
            if (binding.equals(BindingID.XML_HTTP)) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_CANNOT_GEN_WSDL_FOR_NON_SOAP_BINDING(binding.toString(), endpoint.getName()));
            }
        }
    }

    private void validateArguments() throws BadCommandLineException {
        if (!genWsdl) {
            if (serviceName != null) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_WSDL_ARG_NO_GENWSDL(SERVICENAME_OPTION));
            }
            if (portName != null) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_WSDL_ARG_NO_GENWSDL(PORTNAME_OPTION));
            }
        }
    }

    BindingID getBindingID(String protocol) {
        if (protocol.equals(SOAP11))
            return BindingID.SOAP11_HTTP;
        if (protocol.equals(X_SOAP12))
            return BindingID.SOAP12_HTTP;
        String lexical = nonstdProtocols.get(protocol);
        return (lexical != null) ? BindingID.parse(lexical) : null;
    }


    private Class getClass(String className) {
        try {
            return getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
