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


package com.sun.xml.ws.resources;

import com.sun.istack.localization.Localizable;
import com.sun.istack.localization.LocalizableMessageFactory;
import com.sun.istack.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class PolicyMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.ws.resources.policy");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSP_1007_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL() {
        return messageFactory.getMessage("WSP_1007_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL");
    }

    /**
     * WSP1007: Policy exception occurred when finishing WSDL parsing.
     * 
     */
    public static String WSP_1007_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL() {
        return localizer.localize(localizableWSP_1007_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL());
    }

    public static Localizable localizableWSP_1002_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE() {
        return messageFactory.getMessage("WSP_1002_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE");
    }

    /**
     * WSP1002: Unable to marshall policy or it's reference. See original exception for more details.
     * 
     */
    public static String WSP_1002_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE() {
        return localizer.localize(localizableWSP_1002_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE());
    }

    public static Localizable localizableWSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED", arg0, arg1);
    }

    /**
     * WSP1015: Server side assertion validation failed for "{0}" assertion. Assertion was evaluated as "{1}".
     * 
     */
    public static String WSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED(Object arg0, Object arg1) {
        return localizer.localize(localizableWSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED(arg0, arg1));
    }

    public static Localizable localizableWSP_1017_MAP_UPDATE_FAILED() {
        return messageFactory.getMessage("WSP_1017_MAP_UPDATE_FAILED");
    }

    /**
     * WSP1048: Policy map setup failed - exception occurred while trying to modify policy map content.
     * 
     */
    public static String WSP_1017_MAP_UPDATE_FAILED() {
        return localizer.localize(localizableWSP_1017_MAP_UPDATE_FAILED());
    }

    public static Localizable localizableWSP_1010_NO_POLICIES_DEFINED() {
        return messageFactory.getMessage("WSP_1010_NO_POLICIES_DEFINED");
    }

    /**
     * WSP1010: No policies defined.
     * 
     */
    public static String WSP_1010_NO_POLICIES_DEFINED() {
        return localizer.localize(localizableWSP_1010_NO_POLICIES_DEFINED());
    }

    public static Localizable localizableWSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL(Object arg0) {
        return messageFactory.getMessage("WSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL", arg0);
    }

    /**
     * WSP1008: Not marshalling policy, wsdl subject is null for "{0}".
     * 
     */
    public static String WSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL(Object arg0) {
        return localizer.localize(localizableWSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL(arg0));
    }

    public static Localizable localizableWSP_1009_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL() {
        return messageFactory.getMessage("WSP_1009_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL");
    }

    /**
     * WSP1009: Policy map was null, not marshalling any policies.
     * 
     */
    public static String WSP_1009_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL() {
        return localizer.localize(localizableWSP_1009_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL());
    }

    public static Localizable localizableWSP_1020_DUPLICATE_ID(Object arg0) {
        return messageFactory.getMessage("WSP_1020_DUPLICATE_ID", arg0);
    }

    /**
     * WSP1020: Found two policies in one document with the same id: "{0}".
     * 
     */
    public static String WSP_1020_DUPLICATE_ID(Object arg0) {
        return localizer.localize(localizableWSP_1020_DUPLICATE_ID(arg0));
    }

    public static Localizable localizableWSP_1003_UNABLE_TO_CHECK_ELEMENT_NAME(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSP_1003_UNABLE_TO_CHECK_ELEMENT_NAME", arg0, arg1);
    }

    /**
     * WSP1003: Unable to check element name for class "{0}" and WSDL name "{1}".
     * 
     */
    public static String WSP_1003_UNABLE_TO_CHECK_ELEMENT_NAME(Object arg0, Object arg1) {
        return localizer.localize(localizableWSP_1003_UNABLE_TO_CHECK_ELEMENT_NAME(arg0, arg1));
    }

    public static Localizable localizableWSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT(Object arg0) {
        return messageFactory.getMessage("WSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT", arg0);
    }

    /**
     * WSP1013: Exception occurred while reading policy element. Following was read so far: {0}.
     * 
     */
    public static String WSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT(Object arg0) {
        return localizer.localize(localizableWSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT(arg0));
    }

    public static Localizable localizableWSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL() {
        return messageFactory.getMessage("WSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL");
    }

    /**
     * WSP1006: Policy map extender can not be null.
     * 
     */
    public static String WSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL() {
        return localizer.localize(localizableWSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL());
    }

    public static Localizable localizableWSP_1018_FAILED_TO_MARSHALL_POLICY(Object arg0) {
        return messageFactory.getMessage("WSP_1018_FAILED_TO_MARSHALL_POLICY", arg0);
    }

    /**
     * WSP1018: Failed to marshal policy "{0}".
     * 
     */
    public static String WSP_1018_FAILED_TO_MARSHALL_POLICY(Object arg0) {
        return localizer.localize(localizableWSP_1018_FAILED_TO_MARSHALL_POLICY(arg0));
    }

    public static Localizable localizableWSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST(Object arg0) {
        return messageFactory.getMessage("WSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST", arg0);
    }

    /**
     * WSP1005: Failed to find policy referenced by URI "{0}".
     * 
     */
    public static String WSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST(Object arg0) {
        return localizer.localize(localizableWSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST(arg0));
    }

    public static Localizable localizableWSP_1016_POLICY_ID_NULL_OR_DUPLICATE(Object arg0) {
        return messageFactory.getMessage("WSP_1016_POLICY_ID_NULL_OR_DUPLICATE", arg0);
    }

    /**
     * WSP1016: The policy is not added because it has no ID or a policy with the same ID was already added: {0}.
     * 
     */
    public static String WSP_1016_POLICY_ID_NULL_OR_DUPLICATE(Object arg0) {
        return localizer.localize(localizableWSP_1016_POLICY_ID_NULL_OR_DUPLICATE(arg0));
    }

    public static Localizable localizableWSP_1014_CAN_NOT_FIND_POLICY(Object arg0) {
        return messageFactory.getMessage("WSP_1014_CAN_NOT_FIND_POLICY", arg0);
    }

    /**
     * WSP1014: Can not find policy "{0}" that is referenced in from the WSDL. Please, check your policy references in the WSDL.
     * 
     */
    public static String WSP_1014_CAN_NOT_FIND_POLICY(Object arg0) {
        return localizer.localize(localizableWSP_1014_CAN_NOT_FIND_POLICY(arg0));
    }

    public static Localizable localizableWSP_1012_FAILED_CONFIGURE_WSDL_MODEL() {
        return messageFactory.getMessage("WSP_1012_FAILED_CONFIGURE_WSDL_MODEL");
    }

    /**
     * WSP1012: Failed to configure wsdl model.
     * 
     */
    public static String WSP_1012_FAILED_CONFIGURE_WSDL_MODEL() {
        return localizer.localize(localizableWSP_1012_FAILED_CONFIGURE_WSDL_MODEL());
    }

    public static Localizable localizableWSP_1021_FAULT_NOT_BOUND(Object arg0) {
        return messageFactory.getMessage("WSP_1021_FAULT_NOT_BOUND", arg0);
    }

    /**
     * WSP1021: Fault "{0}" not bound. Check names in port and binding definitions.
     * 
     */
    public static String WSP_1021_FAULT_NOT_BOUND(Object arg0) {
        return localizer.localize(localizableWSP_1021_FAULT_NOT_BOUND(arg0));
    }

    public static Localizable localizableWSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT(Object arg0) {
        return messageFactory.getMessage("WSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT", arg0);
    }

    /**
     * WSP1011: Failed to retrieve effective policy for subject: {0}.
     * 
     */
    public static String WSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT(Object arg0) {
        return localizer.localize(localizableWSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT(arg0));
    }

    public static Localizable localizableWSP_1019_CREATE_EMPTY_POLICY_MAP() {
        return messageFactory.getMessage("WSP_1019_CREATE_EMPTY_POLICY_MAP");
    }

    /**
     * WSP1019: Failed to find any configuration file. Creating new empty policy map.
     * 
     */
    public static String WSP_1019_CREATE_EMPTY_POLICY_MAP() {
        return localizer.localize(localizableWSP_1019_CREATE_EMPTY_POLICY_MAP());
    }

    public static Localizable localizableWSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE() {
        return messageFactory.getMessage("WSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE");
    }

    /**
     * WSP1001: XMLStreamException occurred when reading policy reference element.
     * 
     */
    public static String WSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE() {
        return localizer.localize(localizableWSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE());
    }

    public static Localizable localizableWSP_1004_POLICY_URIS_CAN_NOT_BE_NULL() {
        return messageFactory.getMessage("WSP_1004_POLICY_URIS_CAN_NOT_BE_NULL");
    }

    /**
     * WSP1004: Policy URIs can not be null.
     * 
     */
    public static String WSP_1004_POLICY_URIS_CAN_NOT_BE_NULL() {
        return localizer.localize(localizableWSP_1004_POLICY_URIS_CAN_NOT_BE_NULL());
    }

}
