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

package com.sun.xml.ws.api.config.management.policy;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.resources.ManagementMessages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * The server-side ManagedService policy assertion.
 *
 * @author Fabian Ritzmann
 */
public class ManagedServiceAssertion extends ManagementAssertion {

    public static final QName MANAGED_SERVICE_QNAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ManagedService");

    private static final QName COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "CommunicationServerImplementations");
    private static final QName COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "CommunicationServerImplementation");
    private static final QName CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfiguratorImplementation");
    private static final QName CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfigSaverImplementation");
    private static final QName CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfigReaderImplementation");
    private static final QName CLASS_NAME_ATTRIBUTE_QNAME = new QName("className");
    /**
     * The name of the endpointDisposeDelay attribute.
     */
    private static final QName ENDPOINT_DISPOSE_DELAY_ATTRIBUTE_QNAME = new QName("endpointDisposeDelay");

    private static final Logger LOGGER = Logger.getLogger(ManagedServiceAssertion.class);

    /**
     * Return ManagedService assertion if there is one associated with the endpoint.
     *
     * @param endpoint The endpoint. Must not be null.
     * @return The policy assertion if found. Null otherwise.
     * @throws WebServiceException If computing the effective policy of the endpoint failed.
     */
    public static ManagedServiceAssertion getAssertion(WSEndpoint endpoint) throws WebServiceException {
        LOGGER.entering(endpoint);
        // getPolicyMap is deprecated because it is only supposed to be used by Metro code
        // and not by other clients.
        @SuppressWarnings("deprecation")
        final PolicyMap policyMap = endpoint.getPolicyMap();
        final ManagedServiceAssertion assertion = ManagementAssertion.getAssertion(MANAGED_SERVICE_QNAME,
                policyMap, endpoint.getServiceName(), endpoint.getPortName(), ManagedServiceAssertion.class);
        LOGGER.exiting(assertion);
        return assertion;
    }

    public ManagedServiceAssertion(AssertionData data, Collection<PolicyAssertion> assertionParameters)
            throws AssertionCreationException {
        super(MANAGED_SERVICE_QNAME, data, assertionParameters);
    }

    /**
     * Returns the value of the management attribute. True if unset or set to "true"
     * or "on". False otherwise.
     *
     * @return The value of the management attribute.
     */
    public boolean isManagementEnabled() {
        final String management = this.getAttributeValue(MANAGEMENT_ATTRIBUTE_QNAME);
        boolean result = true;
        if (management != null) {
            if (management.trim().toLowerCase().equals("on")) {
                result = true;
            }
            else {
                result = Boolean.parseBoolean(management);
            }
        }
        return result;
    }

    /**
     * Returns the value of the endpointDisposeDelay attribute or the default value
     * otherwise.
     *
     * @param defaultDelay The default value that is returned if this attribute is
     *   not set
     * @return The value of the endpointDisposeDelay attribute or the default value
     *   otherwise.
     */
    public long getEndpointDisposeDelay(final long defaultDelay) throws WebServiceException {
        long result = defaultDelay;
        final String delayText = getAttributeValue(ENDPOINT_DISPOSE_DELAY_ATTRIBUTE_QNAME);
        if (delayText != null) {
            try {
                result = Long.parseLong(delayText);
            } catch (NumberFormatException e) {
                throw LOGGER.logSevereException(new WebServiceException(
                        ManagementMessages.WSM_1008_EXPECTED_INTEGER_DISPOSE_DELAY_VALUE(delayText), e));
            }
        }
        return result;
    }

    /**
     * A list of CommunicationServerImplementation elements that were set as
     * parameters of this assertion.
     *
     * @return A list of CommunicationServerImplementation elements that were set as
     * parameters of this assertion. May be empty.
     */
    public Collection<ImplementationRecord> getCommunicationServerImplementations() {
        final Collection<ImplementationRecord> result = new LinkedList<ImplementationRecord>();
        final Iterator<PolicyAssertion> parameters = getParametersIterator();
        while (parameters.hasNext()) {
            final PolicyAssertion parameter = parameters.next();
            if (COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME.equals(parameter.getName())) {
                final Iterator<PolicyAssertion> implementations = parameter.getParametersIterator();
                if (!implementations.hasNext()) {
                    throw LOGGER.logSevereException(new WebServiceException(
                            ManagementMessages.WSM_1005_EXPECTED_COMMUNICATION_CHILD()));
                }
                while (implementations.hasNext()) {
                    final PolicyAssertion implementation = implementations.next();
                    if (COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME.equals(implementation.getName())) {
                        result.add(getImplementation(implementation));
                    }
                    else {
                        throw LOGGER.logSevereException(new WebServiceException(
                                ManagementMessages.WSM_1004_EXPECTED_XML_TAG(
                                COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME, implementation.getName())));
                    }
                }
            }
        }
        return result;
    }

    /**
     * The ConfiguratorImplementation that was set as parameter of this assertion.
     *
     * @return The ConfiguratorImplementation that was set as parameter of this assertion.
     *   May be null.
     */
    public ImplementationRecord getConfiguratorImplementation() {
        return findImplementation(CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME);
    }

    /**
     * The ConfigSaverImplementation that was set as parameter of this assertion.
     *
     * @return The ConfigSaverImplementation that was set as parameter of this assertion.
     *   May be null.
     */
    public ImplementationRecord getConfigSaverImplementation() {
        return findImplementation(CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME);
    }

    /**
     * The ConfigReaderImplementation that was set as parameter of this assertion.
     *
     * @return The ConfigReaderImplementation that was set as parameter of this assertion.
     *   May be null.
     */
    public ImplementationRecord getConfigReaderImplementation() {
        return findImplementation(CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME);
    }

    private ImplementationRecord findImplementation(QName implementationName) {
        final Iterator<PolicyAssertion> parameters = getParametersIterator();
        while (parameters.hasNext()) {
            final PolicyAssertion parameter = parameters.next();
            if (implementationName.equals(parameter.getName())) {
                return getImplementation(parameter);
            }
        }
        return null;
    }

    private ImplementationRecord getImplementation(PolicyAssertion rootParameter) {
        final String className = rootParameter.getAttributeValue(CLASS_NAME_ATTRIBUTE_QNAME);
        final HashMap<QName, String> parameterMap = new HashMap<QName, String>();
        final Iterator<PolicyAssertion> implementationParameters = rootParameter.getParametersIterator();
        final Collection<NestedParameters> nestedParameters = new LinkedList<NestedParameters>();
        while (implementationParameters.hasNext()) {
            final PolicyAssertion parameterAssertion = implementationParameters.next();
            final QName parameterName = parameterAssertion.getName();
            if (parameterAssertion.hasParameters()) {
                final Map<QName, String> nestedParameterMap = new HashMap<QName, String>();
                final Iterator<PolicyAssertion> parameters = parameterAssertion.getParametersIterator();
                while (parameters.hasNext()) {
                    final PolicyAssertion parameter = parameters.next();
                    String value = parameter.getValue();
                    if (value != null) {
                        value = value.trim();
                    }
                    nestedParameterMap.put(parameter.getName(), value);
                }
                nestedParameters.add(new NestedParameters(parameterName, nestedParameterMap));
            }
            else {
                String value = parameterAssertion.getValue();
                if (value != null) {
                    value = value.trim();
                }
                parameterMap.put(parameterName, value);
            }
        }
        return new ImplementationRecord(className, parameterMap, nestedParameters);
    }


    /**
     * Return the implementation class name along with all parameters for the
     * implementation.
     */
    public static class ImplementationRecord {

        private final String implementation;
        private final Map<QName, String> parameters;
        private final Collection<NestedParameters> nestedParameters;

        protected ImplementationRecord(String implementation, Map<QName, String> parameters,
                Collection<NestedParameters> nestedParameters) {
            this.implementation = implementation;
            this.parameters = parameters;
            this.nestedParameters = nestedParameters;
        }

        public String getImplementation() {
            return this.implementation;
        }

        /**
         * The parameters that were set for this implementation element.
         *
         * @return The parameters that were set for this implementation element.
         *   May be null.
         */
        public Map<QName, String> getParameters() {
            return this.parameters;
        }

        /**
         * Implementation elements may contain element parameters that contain
         * further parameters.
         *
         * @return The nested parameters that were set for this implementation element.
         *   May be null.
         */
        public Collection<NestedParameters> getNestedParameters() {
            return this.nestedParameters;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ImplementationRecord other = (ImplementationRecord) obj;
            if ((this.implementation == null) ?
                (other.implementation != null) : !this.implementation.equals(other.implementation)) {
                return false;
            }
            if (this.parameters != other.parameters && (this.parameters == null || !this.parameters.equals(other.parameters))) {
                return false;
            }
            if (this.nestedParameters != other.nestedParameters &&
                    (this.nestedParameters == null || !this.nestedParameters.equals(other.nestedParameters))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + (this.implementation != null ? this.implementation.hashCode() : 0);
            hash = 53 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
            hash = 53 * hash + (this.nestedParameters != null ? this.nestedParameters.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder("ImplementationRecord: ");
            text.append("implementation = \"").append(this.implementation).append("\", ");
            text.append("parameters = \"").append(this.parameters).append("\", ");
            text.append("nested parameters = \"").append(this.nestedParameters).append("\"");
            return text.toString();
        }

    }


    /**
     * The nested parameters that may be set as part of an implementation element.
     */
    public static class NestedParameters {

        private final QName name;
        private final Map<QName, String> parameters;

        private NestedParameters(QName name, Map<QName, String> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public QName getName() {
            return this.name;
        }

        public Map<QName, String> getParameters() {
            return this.parameters;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NestedParameters other = (NestedParameters) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.parameters != other.parameters && (this.parameters == null || !this.parameters.equals(other.parameters))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 59 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder("NestedParameters: ");
            text.append("name = \"").append(this.name).append("\", ");
            text.append("parameters = \"").append(this.parameters).append("\"");
            return text.toString();
        }

    }

}
