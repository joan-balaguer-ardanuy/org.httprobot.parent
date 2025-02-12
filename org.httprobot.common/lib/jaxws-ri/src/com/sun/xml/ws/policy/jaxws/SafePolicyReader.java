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

package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.api.policy.ModelUnmarshaller;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken;
import com.sun.xml.ws.resources.PolicyMessages;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/**
* Provides methods to unmarshal policies from a XMLStreamReader safely
*
* @author Fabian Ritzmann
*/
public class SafePolicyReader {

   private static final PolicyLogger LOGGER = PolicyLogger.getLogger(SafePolicyReader.class);

   // urls of xml docs policies were read from
   private final Set<String> urlsRead = new HashSet<String>();

   private final Set<String> qualifiedPolicyUris = new HashSet<String>();


   public final class PolicyRecord {
       PolicyRecord next;
       PolicySourceModel policyModel;
       Set<String> unresolvedURIs;
       private String uri;

       PolicyRecord() {
           // nothing to initialize
       }

       PolicyRecord insert(final PolicyRecord insertedRec) {
           if (null==insertedRec.unresolvedURIs || insertedRec.unresolvedURIs.isEmpty()) {
               insertedRec.next = this;
               return insertedRec;
           }
           final PolicyRecord head = this;
           PolicyRecord oneBeforeCurrent = null;
           PolicyRecord current;
           for (current = head ; null != current.next ; ) {
               if ((null != current.unresolvedURIs) && current.unresolvedURIs.contains(insertedRec.uri)) {
                   if (null == oneBeforeCurrent) {
                       insertedRec.next = current;
                       return insertedRec;
                   } else { // oneBeforeCurrent != null
                       oneBeforeCurrent.next = insertedRec;
                       insertedRec.next = current;
                       return head;
                   } // end-if-else oneBeforeCurrent == null
               }// end-if current record depends on inserted one
               if (insertedRec.unresolvedURIs.remove(current.uri) && (insertedRec.unresolvedURIs.isEmpty())) {
                   insertedRec.next = current.next;
                   current.next = insertedRec;
                   return head;
               } // end-if one of unresolved URIs resolved by current record and thus unresolvedURIs empty
               oneBeforeCurrent = current;
               current = current.next;
           } // end for (current = head; null!=current.next; )
           insertedRec.next = null;
           current.next = insertedRec;
           return head;
       }

       /**
        * Set the URI that identifies the policy.
        *
        * @param uri The fully qualified URI of the policy. May be a relative URI
        *   if JAX-WS did not pass on any system id.
        * @param id The short ID of the policy. Used for error reporting.
        * @throws PolicyException If there already is a policy recorded with the
        *   same id.
        */
       public void setUri(final String uri, final String id) throws PolicyException {
           if (qualifiedPolicyUris.contains(uri)) {
               throw LOGGER.logSevereException(new PolicyException(PolicyMessages.WSP_1020_DUPLICATE_ID(id)));
           }
           this.uri = uri;
           qualifiedPolicyUris.add(uri);
       }

       public String getUri() {
           return this.uri;
       }

       @Override
       public String toString() {
           String result = uri;
           if (null!=next) {
               result += "->" + next.toString();
           }
           return result;
       }
   }


   /**
    * Reads a policy expression from the XML stream.
    *
    * The XMLStreamReader should be in START_ELEMENT state and point to the policy element.
    * The content of the stream is copied and then the copy is unmarshalled. The result
    * is returned as a PolicyRecord.
    *
    * @param reader The XMLStreamReader should be in START_ELEMENT state and point to the policy element.
    * @param baseUrl The system id of the document read by the reader.
    * @return The policy that was read from the XML stream.
    */
   public PolicyRecord readPolicyElement(final XMLStreamReader reader, final String baseUrl) {
       if ((null == reader) || (!reader.isStartElement())) {
           return null;
       }
       final StringBuffer elementCode = new StringBuffer();
       final PolicyRecord policyRec = new PolicyRecord();
       final QName elementName = reader.getName();
       boolean insidePolicyReferenceAttr;
       int depth = 0;
       try{
           do {
               switch (reader.getEventType()) {
                   case XMLStreamConstants.START_ELEMENT:  // process start of next element
                       QName curName = reader.getName();
                       insidePolicyReferenceAttr = NamespaceVersion.resolveAsToken(curName) == XmlToken.PolicyReference;
                       if (elementName.equals(curName)) {  // it is our element !
                           depth++;                        // we are then deeper
                       }
                       final StringBuffer xmlnsCode = new StringBuffer();    // take care about namespaces as well
                       final Set<String> tmpNsSet = new HashSet<String>();
                       if ((null == curName.getPrefix()) || ("".equals(curName.getPrefix()))) {           // no prefix
                           elementCode
                                   .append('<')                     // start tag
                                   .append(curName.getLocalPart());
                           xmlnsCode
                                   .append(" xmlns=\"")
                                   .append(curName.getNamespaceURI())
                                   .append('"');

                       } else {                                    // prefix presented
                           elementCode
                                   .append('<')                     // start tag
                                   .append(curName.getPrefix())
                                   .append(':')
                                   .append(curName.getLocalPart());
                           xmlnsCode
                                   .append(" xmlns:")
                                   .append(curName.getPrefix())
                                   .append("=\"")
                                   .append(curName.getNamespaceURI())
                                   .append('"');
                           tmpNsSet.add(curName.getPrefix());
                       }
                       final int attrCount = reader.getAttributeCount();     // process element attributes
                       final StringBuffer attrCode = new StringBuffer();
                       for (int i=0; i < attrCount; i++) {
                           boolean uriAttrFlg = false;
                           if (insidePolicyReferenceAttr && "URI".equals(
                                   reader.getAttributeName(i).getLocalPart())) { // PolicyReference found
                               uriAttrFlg = true;
                               if (null == policyRec.unresolvedURIs) { // first such URI found
                                   policyRec.unresolvedURIs = new HashSet<String>(); // initialize URIs set
                               }
                               policyRec.unresolvedURIs.add(  // add the URI
                                       relativeToAbsoluteUrl(reader.getAttributeValue(i), baseUrl));
                           } // end-if PolicyReference attribute found
                           if ("xmlns".equals(reader.getAttributePrefix(i)) && tmpNsSet.contains(reader.getAttributeLocalName(i))) {
                               continue; // do not append already defined ns
                           }
                           if ((null == reader.getAttributePrefix(i)) || ("".equals(reader.getAttributePrefix(i)))) {  // no attribute prefix
                               attrCode
                                       .append(' ')
                                       .append(reader.getAttributeLocalName(i))
                                       .append("=\"")
                                       .append(uriAttrFlg ? relativeToAbsoluteUrl(reader.getAttributeValue(i), baseUrl) : reader.getAttributeValue(i))
                                       .append('"');
                           } else {                                        // prefix`presented
                               attrCode
                                       .append(' ')
                                       .append(reader.getAttributePrefix(i))
                                       .append(':')
                                       .append(reader.getAttributeLocalName(i))
                                       .append("=\"")
                                       .append(uriAttrFlg ? relativeToAbsoluteUrl(reader.getAttributeValue(i), baseUrl) : reader.getAttributeValue(i))
                                       .append('"');
                               if (!tmpNsSet.contains(reader.getAttributePrefix(i))) {
                                   xmlnsCode
                                           .append(" xmlns:")
                                           .append(reader.getAttributePrefix(i))
                                           .append("=\"")
                                           .append(reader.getAttributeNamespace(i))
                                           .append('"');
                                   tmpNsSet.add(reader.getAttributePrefix(i));
                               } // end if prefix already processed
                           }
                       } // end foreach attr
                       elementCode
                               .append(xmlnsCode)          // complete the start element tag
                               .append(attrCode)
                               .append('>');
                       break;
                       //case XMLStreamConstants.ATTRIBUTE:   Unreachable (I hope ;-)
                       //    break;
                       //case XMLStreamConstants.NAMESPACE:   Unreachable (I hope ;-)
                       //    break;
                   case XMLStreamConstants.END_ELEMENT:
                       curName = reader.getName();
                       if (elementName.equals(curName)) {  // it is our element !
                           depth--;                        // go up
                       }
                       elementCode
                               .append("</")                     // append appropriate XML code
                               .append("".equals(curName.getPrefix())?"":curName.getPrefix()+':')
                               .append(curName.getLocalPart())
                               .append('>');                        // complete the end element tag
                       break;
                   case XMLStreamConstants.CHARACTERS:
                       elementCode.append(reader.getText());           // append text data
                       break;
                   case XMLStreamConstants.CDATA:
                       elementCode
                               .append("<![CDATA[")                // append CDATA delimiters
                               .append(reader.getText())
                               .append("]]>");
                       break;
                   case XMLStreamConstants.COMMENT:    // Ignore any comments
                       break;
                   case XMLStreamConstants.SPACE:      // Ignore spaces as well
                       break;
               }
               if (reader.hasNext() && depth>0) {
                   reader.next();
               }
           } while (XMLStreamConstants.END_DOCUMENT!=reader.getEventType() && depth>0);
           policyRec.policyModel = ModelUnmarshaller.getUnmarshaller().unmarshalModel(
                   new StringReader(elementCode.toString()));
           if (null != policyRec.policyModel.getPolicyId()) {
               policyRec.setUri(baseUrl + "#" + policyRec.policyModel.getPolicyId(), policyRec.policyModel.getPolicyId());
           } else if (policyRec.policyModel.getPolicyName() != null) {
               policyRec.setUri(policyRec.policyModel.getPolicyName(), policyRec.policyModel.getPolicyName());
           }
       } catch(Exception e) {
           throw LOGGER.logSevereException(new WebServiceException(PolicyMessages.WSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT(elementCode.toString()), e));
       }
       urlsRead.add(baseUrl);
       return policyRec;
   }


   public Set<String> getUrlsRead() {
       return this.urlsRead;
   }


   /**
    * Reads policy reference element <wsp:PolicyReference/> and returns referenced policy URI as String
    *
    * @param reader The XMLStreamReader should be in START_ELEMENT state and point to the PolicyReference element.
    * @return The URI contained in the PolicyReference
    */
   public String readPolicyReferenceElement(final XMLStreamReader reader) {
       try {
           if (NamespaceVersion.resolveAsToken(reader.getName()) == XmlToken.PolicyReference) {     // "PolicyReference" element interests me
               for (int i = 0; i < reader.getAttributeCount(); i++) {
                   if (XmlToken.resolveToken(reader.getAttributeName(i).getLocalPart()) == XmlToken.Uri) {
                       final String uriValue = reader.getAttributeValue(i);
                       reader.next();
                       return uriValue;
                   }
               }
           }
           reader.next();
           return null;
       } catch(XMLStreamException e) {
           throw LOGGER.logSevereException(new WebServiceException(PolicyMessages.WSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE(), e));
       }
   }


   /**
    * Utility method to construct an absolute URL from a relative URI and a base URL.
    *
    * If the relativeUri already is an absolute URL, the method returns the relativeUri.
    *
    * @param relativeUri The relative URI
    * @param baseUri The base URL
    * @return The relative URI appended to the base URL. If relativeUri already is
    *   an absolute URL, the method returns the relativeUri.
    */
   public static String relativeToAbsoluteUrl(final String relativeUri, final String baseUri) {
       if ('#' != relativeUri.charAt(0)) {  // TODO: escaped char could be an issue?
           return relativeUri; // absolute already
       }
       return (null == baseUri) ? relativeUri : baseUri + relativeUri;
   }

}
