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

package com.sun.xml.ws.message.stream;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.istack.XMLStreamReaderToContentHandler;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.encoding.TagInfoset;
import com.sun.xml.ws.message.AbstractMessageImpl;
import com.sun.xml.ws.message.AttachmentUnmarshallerImpl;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.xml.DummyLocation;
import com.sun.xml.ws.util.xml.StAXSource;
import com.sun.xml.ws.util.xml.XMLStreamReaderToXMLStreamWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.*;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * {@link Message} implementation backed by {@link XMLStreamReader}.
 *
 * TODO: we need another message class that keeps {@link XMLStreamReader} that points
 * at the start of the envelope element.
 */
public class StreamMessage extends AbstractMessageImpl {
    /**
     * The reader will be positioned at
     * the first child of the SOAP body
     */
    private @NotNull XMLStreamReader reader;

    // lazily created
    private @Nullable HeaderList headers;

    /**
     * Because the StreamMessage leaves out the white spaces around payload
     * when being instantiated the space characters between soap:Body opening and
     * payload is stored in this field to be reused later (necessary for message security);
     * Instantiated after StreamMessage creation
     */
    private String bodyPrologue = null;

    /**
     * instantiated after writing message to XMLStreamWriter
     */
    private String bodyEpilogue = null;

    private final String payloadLocalName;

    private final String payloadNamespaceURI;

    /**
     * infoset about the SOAP envelope, header, and body.
     *
     * <p>
     * If the creater of this object didn't care about those,
     * we use stock values.
     */
    private @NotNull TagInfoset envelopeTag,headerTag,bodyTag;

    /**
     * Used only for debugging. This records where the message was consumed.
     */
    private Throwable consumedAt;

    /**
     * Default s:Envelope, s:Header, and s:Body tag infoset definitions.
     *
     * We need 3 for SOAP 1.1, 3 for SOAP 1.2.
     */
    private static final TagInfoset[] DEFAULT_TAGS;

    static {
        DEFAULT_TAGS = new TagInfoset[6];
        create(SOAPVersion.SOAP_11);
        create(SOAPVersion.SOAP_12);
    }
    
    public StreamMessage(SOAPVersion v) {
        super(v);
        payloadLocalName = null;
        payloadNamespaceURI = null;
    }
    /**
     * Creates a {@link StreamMessage} from a {@link XMLStreamReader}
     * that points at the start element of the payload, and headers.
     *
     * <p>
     * This method creaets a {@link Message} from a payload.
     *
     * @param headers
     *      if null, it means no headers. if non-null,
     *      it will be owned by this message.
     * @param reader
     *      points at the start element/document of the payload (or the end element of the &lt;s:Body>
     *      if there's no payload)
     */
    public StreamMessage(@Nullable HeaderList headers, @NotNull AttachmentSet attachmentSet, @NotNull XMLStreamReader reader, @NotNull SOAPVersion soapVersion) {
        super(soapVersion);
        this.headers = headers;
        this.attachmentSet = attachmentSet;
        this.reader = reader;

        if(reader.getEventType()== START_DOCUMENT)
            XMLStreamReaderUtil.nextElementContent(reader);

        //if the reader is pointing to the end element </soapenv:Body> then its empty message
        // or no payload
        if(reader.getEventType() == XMLStreamConstants.END_ELEMENT){
            String body = reader.getLocalName();
            String nsUri = reader.getNamespaceURI();
            assert body != null;
            assert nsUri != null;
            //if its not soapenv:Body then throw exception, we received malformed stream
            if(body.equals("Body") && nsUri.equals(soapVersion.nsUri)){
                this.payloadLocalName = null;
                this.payloadNamespaceURI = null;
            }else{ //TODO: i18n and also we should be throwing better message that this
                throw new WebServiceException("Malformed stream: {"+nsUri+"}"+body);
            }
        }else{
            this.payloadLocalName = reader.getLocalName();
            this.payloadNamespaceURI = reader.getNamespaceURI();
        }

        // use the default infoset representation for headers
        int base = soapVersion.ordinal()*3;
        this.envelopeTag = DEFAULT_TAGS[base];
        this.headerTag = DEFAULT_TAGS[base+1];
        this.bodyTag = DEFAULT_TAGS[base+2];
    }

    /**
     * Creates a {@link StreamMessage} from a {@link XMLStreamReader}
     * and the complete infoset of the SOAP envelope.
     *
     * <p>
     * See {@link #StreamMessage(HeaderList, AttachmentSet, XMLStreamReader, SOAPVersion)} for
     * the description of the basic parameters.
     *
     * @param headerTag
     *      Null if the message didn't have a header tag.
     *
     */
    public StreamMessage(@NotNull TagInfoset envelopeTag, @Nullable TagInfoset headerTag, @NotNull AttachmentSet attachmentSet, @Nullable HeaderList headers, @NotNull TagInfoset bodyTag, @NotNull XMLStreamReader reader, @NotNull SOAPVersion soapVersion) {
        this(envelopeTag, headerTag, attachmentSet, headers, null, bodyTag, null, reader, soapVersion);
    }

    public StreamMessage(@NotNull TagInfoset envelopeTag, @Nullable TagInfoset headerTag, @NotNull AttachmentSet attachmentSet, @Nullable HeaderList headers, @Nullable String bodyPrologue, @NotNull TagInfoset bodyTag, @Nullable String bodyEpilogue, @NotNull XMLStreamReader reader, @NotNull SOAPVersion soapVersion) {
        this(headers,attachmentSet,reader,soapVersion);
        if(envelopeTag == null ) {
            throw new IllegalArgumentException("EnvelopeTag TagInfoset cannot be null");
        }
        if(bodyTag == null ) {
            throw new IllegalArgumentException("BodyTag TagInfoset cannot be null");
        }
        this.envelopeTag = envelopeTag;
        this.headerTag = headerTag!=null ? headerTag : 
            new TagInfoset(envelopeTag.nsUri,"Header",envelopeTag.prefix,EMPTY_ATTS);
        this.bodyTag = bodyTag;
        this.bodyPrologue = bodyPrologue;
        this.bodyEpilogue = bodyEpilogue;
    }

    public boolean hasHeaders() {
        return headers!=null && !headers.isEmpty();
    }

    public HeaderList getHeaders() {
        if (headers == null) {
            headers = new HeaderList(getSOAPVersion());
        }
        return headers;
    }
    
    public String getPayloadLocalPart() {
        return payloadLocalName;
    }

    public String getPayloadNamespaceURI() {
        return payloadNamespaceURI;
    }

    public boolean hasPayload() {
        return payloadLocalName!=null;
    }

    public Source readPayloadAsSource() {
        if(hasPayload()) {
            assert unconsumed();
            return new StAXSource(reader, true, getInscopeNamespaces());
        } else
            return null;
    }

    /**
     * There is no way to enumerate inscope namespaces for XMLStreamReader. That means
     * namespaces declared in envelope, and body tags need to be computed using their
     * {@link TagInfoset}s.
     *
     * @return array of the even length of the form { prefix0, uri0, prefix1, uri1, ... }
     */
    private String[] getInscopeNamespaces() {
        NamespaceSupport nss = new NamespaceSupport();

        nss.pushContext();
        for(int i=0; i < envelopeTag.ns.length; i+=2) {
            nss.declarePrefix(envelopeTag.ns[i], envelopeTag.ns[i+1]);
        }

        nss.pushContext();
        for(int i=0; i < bodyTag.ns.length; i+=2) {
            nss.declarePrefix(bodyTag.ns[i], bodyTag.ns[i+1]);
        }

        List<String> inscope = new ArrayList<String>();
        for( Enumeration en = nss.getPrefixes(); en.hasMoreElements(); ) {
            String prefix = (String)en.nextElement();
            inscope.add(prefix);
            inscope.add(nss.getURI(prefix));
        }
        return inscope.toArray(new String[inscope.size()]);
    }

    public Object readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        if(!hasPayload())
            return null;
        assert unconsumed();
        // TODO: How can the unmarshaller process this as a fragment?
        if(hasAttachments())
            unmarshaller.setAttachmentUnmarshaller(new AttachmentUnmarshallerImpl(getAttachments()));
        try {
            return unmarshaller.unmarshal(reader);
        } finally{
            unmarshaller.setAttachmentUnmarshaller(null);
            XMLStreamReaderUtil.readRest(reader);
            XMLStreamReaderUtil.close(reader);
            XMLStreamReaderFactory.recycle(reader);
        }
    }
    /** @deprecated */
    public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
        if(!hasPayload())
            return null;
        assert unconsumed();
        T r = bridge.unmarshal(reader,
            hasAttachments() ? new AttachmentUnmarshallerImpl(getAttachments()) : null);
        XMLStreamReaderUtil.readRest(reader);
        XMLStreamReaderUtil.close(reader);
        XMLStreamReaderFactory.recycle(reader);
        return r;
    }

    public <T> T readPayloadAsJAXB(XMLBridge<T> bridge) throws JAXBException {
        if(!hasPayload())
            return null;
        assert unconsumed();
        T r = bridge.unmarshal(reader,
            hasAttachments() ? new AttachmentUnmarshallerImpl(getAttachments()) : null);
        XMLStreamReaderUtil.readRest(reader);
        XMLStreamReaderUtil.close(reader);
        XMLStreamReaderFactory.recycle(reader);
        return r;
    } 

    @Override
    public void consume() {
        assert unconsumed();
        XMLStreamReaderUtil.readRest(reader);
        XMLStreamReaderUtil.close(reader);
        XMLStreamReaderFactory.recycle(reader);
    }

    public XMLStreamReader readPayload() {
        if(!hasPayload())
            return null;
        // TODO: What about access at and beyond </soap:Body>
        assert unconsumed();
        return this.reader;
    }

    public void writePayloadTo(XMLStreamWriter writer)throws XMLStreamException {
        assert unconsumed();

        if(payloadLocalName==null) {
            return; // no body
        }

        if (bodyPrologue != null) {
            writer.writeCharacters(bodyPrologue);
        }

        XMLStreamReaderToXMLStreamWriter conv = new XMLStreamReaderToXMLStreamWriter();

        while(reader.getEventType() != XMLStreamConstants.END_DOCUMENT){
            String name = reader.getLocalName();
            String nsUri = reader.getNamespaceURI();

            // After previous conv.bridge() call the cursor will be at END_ELEMENT.
            // Check if its not soapenv:Body then move to next ELEMENT
            if(reader.getEventType() == XMLStreamConstants.END_ELEMENT){

                if (!isBodyElement(name, nsUri)){
                    // closing payload element: store epilogue for further signing, if applicable
                    // however if there more than one payloads exist - the last one is stored
                    String whiteSpaces = XMLStreamReaderUtil.nextWhiteSpaceContent(reader);
                    if (whiteSpaces != null) {
                        this.bodyEpilogue = whiteSpaces;
                        // write it to the message too
                        writer.writeCharacters(whiteSpaces);
                    }
                } else {
                    // body closed > exit
                    break;
                }

            } else {
                // payload opening element: copy payload to writer
                conv.bridge(reader,writer);
            }
        }

        XMLStreamReaderUtil.readRest(reader);
        XMLStreamReaderUtil.close(reader);
        XMLStreamReaderFactory.recycle(reader);
    }

    private boolean isBodyElement(String name, String nsUri) {
        return name.equals("Body") && nsUri.equals(soapVersion.nsUri);
    }

    public void writeTo(XMLStreamWriter sw) throws XMLStreamException{
        writeEnvelope(sw);
    }

    /**
     * This method should be called when the StreamMessage is created with a payload
     * @param writer
     */
    private void writeEnvelope(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument();
        envelopeTag.writeStart(writer);

        //write headers
        HeaderList hl = getHeaders();
        if(hl.size() > 0){
            headerTag.writeStart(writer);
            for(Header h:hl){
                h.writeTo(writer);
            }
            writer.writeEndElement();
        }
        bodyTag.writeStart(writer);
        if(hasPayload())
            writePayloadTo(writer);
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    public void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException {
        assert unconsumed();

        try {
            if(payloadLocalName==null)
                return; // no body

            if (bodyPrologue != null) {
                char[] chars = bodyPrologue.toCharArray();
                contentHandler.characters(chars, 0, chars.length);
            }

            XMLStreamReaderToContentHandler conv = new XMLStreamReaderToContentHandler(reader,contentHandler,true,fragment,getInscopeNamespaces());

            while(reader.getEventType() != XMLStreamConstants.END_DOCUMENT){
                String name = reader.getLocalName();
                String nsUri = reader.getNamespaceURI();

                // After previous conv.bridge() call the cursor will be at END_ELEMENT.
                // Check if its not soapenv:Body then move to next ELEMENT
                if(reader.getEventType() == XMLStreamConstants.END_ELEMENT){

                    if (!isBodyElement(name, nsUri)){
                        // closing payload element: store epilogue for further signing, if applicable
                        // however if there more than one payloads exist - the last one is stored
                        String whiteSpaces = XMLStreamReaderUtil.nextWhiteSpaceContent(reader);
                        if (whiteSpaces != null) {
                            this.bodyEpilogue = whiteSpaces;
                            // write it to the message too
                            char[] chars = whiteSpaces.toCharArray();
                            contentHandler.characters(chars, 0, chars.length);
                        }
                    } else {
                        // body closed > exit
                        break;
                    }

                } else {
                    // payload opening element: copy payload to writer
                    conv.bridge();
                }
            }
            XMLStreamReaderUtil.readRest(reader);
            XMLStreamReaderUtil.close(reader);
            XMLStreamReaderFactory.recycle(reader);
        } catch (XMLStreamException e) {
            Location loc = e.getLocation();
            if(loc==null)   loc = DummyLocation.INSTANCE;

            SAXParseException x = new SAXParseException(
                e.getMessage(),loc.getPublicId(),loc.getSystemId(),loc.getLineNumber(),loc.getColumnNumber(),e);
            errorHandler.error(x);
        }
    }        

    // TODO: this method should be probably rewritten to respect spaces between eelements; is it used at all?
    public Message copy() {
        try {
            assert unconsumed();
            consumedAt = null; // but we don't want to mark it as consumed
            MutableXMLStreamBuffer xsb = new MutableXMLStreamBuffer();
            StreamReaderBufferCreator c = new StreamReaderBufferCreator(xsb);

            // preserving inscope namespaces from envelope, and body. Other option
            // would be to create a filtering XMLStreamReader from reader+envelopeTag+bodyTag
            c.storeElement(envelopeTag.nsUri, envelopeTag.localName, envelopeTag.prefix, envelopeTag.ns);
            c.storeElement(bodyTag.nsUri, bodyTag.localName, bodyTag.prefix, bodyTag.ns);

            if (hasPayload()) {
                // Loop all the way for multi payload case
                while(reader.getEventType() != XMLStreamConstants.END_DOCUMENT){
                    String name = reader.getLocalName();
                    String nsUri = reader.getNamespaceURI();
                    if(isBodyElement(name, nsUri) || (reader.getEventType() == XMLStreamConstants.END_DOCUMENT))
                        break;
                    c.create(reader);

                    // Skip whitespaces in between payload and </Body> or between elements
                    // those won't be in the message itself, but we store them in field bodyEpilogue
                    if (reader.isWhiteSpace()) {
                        bodyEpilogue = XMLStreamReaderUtil.currentWhiteSpaceContent(reader);
                    } else {
                        // clear it in case the existing was not the last one
                        // (we are interested only in the last one?)
                        bodyEpilogue = null;
                    }
                }
            }
            c.storeEndElement();        // create structure element for </Body>
            c.storeEndElement();        // create structure element for </Envelope>
            c.storeEndElement();        // create structure element for END_DOCUMENT

            XMLStreamReaderUtil.readRest(reader);
            XMLStreamReaderUtil.close(reader);
            XMLStreamReaderFactory.recycle(reader);

            reader = xsb.readAsXMLStreamReader();
            XMLStreamReader clone = xsb.readAsXMLStreamReader();

            // advance to the start tag of the <Body> first child element
            proceedToRootElement(reader);
            proceedToRootElement(clone);

            return new StreamMessage(envelopeTag, headerTag, attachmentSet, HeaderList.copy(headers), bodyPrologue, bodyTag, bodyEpilogue, clone, soapVersion);
        } catch (XMLStreamException e) {
            throw new WebServiceException("Failed to copy a message",e);
        }
    }

    private void proceedToRootElement(XMLStreamReader xsr) throws XMLStreamException {
        assert xsr.getEventType()==START_DOCUMENT;
        xsr.nextTag();
        xsr.nextTag();
        xsr.nextTag();
        assert xsr.getEventType()==START_ELEMENT || xsr.getEventType()==END_ELEMENT;
    }

    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler ) throws SAXException {
        contentHandler.setDocumentLocator(NULL_LOCATOR);
        contentHandler.startDocument();
        envelopeTag.writeStart(contentHandler);
        headerTag.writeStart(contentHandler);
        if(hasHeaders()) {
            HeaderList headers = getHeaders();
            int len = headers.size();
            for( int i=0; i<len; i++ ) {
                // shouldn't JDK be smart enough to use array-style indexing for this foreach!?
                headers.get(i).writeTo(contentHandler,errorHandler);
            }
        }
        headerTag.writeEnd(contentHandler);
        bodyTag.writeStart(contentHandler);
        writePayloadTo(contentHandler,errorHandler, true);
        bodyTag.writeEnd(contentHandler);
        envelopeTag.writeEnd(contentHandler);
        contentHandler.endDocument();
    }

    /**
     * Used for an assertion. Returns true when the message is unconsumed,
     * or otherwise throw an exception.
     *
     * <p>
     * Calling this method also marks the stream as 'consumed'
     */
    private boolean unconsumed() {
        if(payloadLocalName==null)
            return true;    // no payload. can be consumed multiple times.

        if(reader.getEventType()!=XMLStreamReader.START_ELEMENT) {
            AssertionError error = new AssertionError("StreamMessage has been already consumed. See the nested exception for where it's consumed");
            error.initCause(consumedAt);
            throw error;
        }
        consumedAt = new Exception().fillInStackTrace();
        return true;
    }

    private static void create(SOAPVersion v) {
        int base = v.ordinal()*3;
        DEFAULT_TAGS[base  ] = new TagInfoset(v.nsUri,"Envelope","S",EMPTY_ATTS,"S",v.nsUri);
        DEFAULT_TAGS[base+1] = new TagInfoset(v.nsUri,"Header","S",EMPTY_ATTS);
        DEFAULT_TAGS[base+2] = new TagInfoset(v.nsUri,"Body","S",EMPTY_ATTS);
    }

    public String getBodyPrologue() {
        return bodyPrologue;
    }

    public String getBodyEpilogue() {
        return bodyEpilogue;
    }

    public XMLStreamReader getReader() {
        assert unconsumed();
        return reader;
    }
}
