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

package com.sun.xml.ws.encoding.xml;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.message.*;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.ws.encoding.ContentType;
import com.sun.xml.ws.encoding.MimeMultipartParser;
import com.sun.xml.ws.encoding.XMLHTTPBindingCodec;
import com.sun.xml.ws.message.AbstractMessageImpl;
import com.sun.xml.ws.message.EmptyMessageImpl;
import com.sun.xml.ws.message.MimeAttachmentSet;
import com.sun.xml.ws.message.source.PayloadSourceMessage;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.StreamUtils;
import static com.sun.xml.ws.binding.WebServiceFeatureList.getFeature;  
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.activation.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Jitendra Kotamraju
 */
public final class XMLMessage {

    private static final int PLAIN_XML_FLAG      = 1;       // 00001
    private static final int MIME_MULTIPART_FLAG = 2;       // 00010
    private static final int FI_ENCODED_FLAG     = 16;      // 10000
    private WebServiceFeature[] features;

    /*
     * Construct a message given a content type and an input stream.
     */
    public static Message create(final String ct, InputStream in, WSFeatureList f) {
        Message data;
        try {
            in = StreamUtils.hasSomeData(in);
            if (in == null) {
                return Messages.createEmpty(SOAPVersion.SOAP_11);
            }

            if (ct != null) {
                final ContentType contentType = new ContentType(ct);
                final int contentTypeId = identifyContentType(contentType);
                if ((contentTypeId & MIME_MULTIPART_FLAG) != 0) {
                    data = new XMLMultiPart(ct, in, f);
                } else if ((contentTypeId & PLAIN_XML_FLAG) != 0) {
                    data = new XmlContent(ct, in, f);
                } else {
                    data = new UnknownContent(ct, in);
                }
            } else {
                // According to HTTP spec 7.2.1, if the media type remain
                // unknown, treat as application/octet-stream
                data = new UnknownContent("application/octet-stream", in);
            }
        } catch(Exception ex) {
            throw new WebServiceException(ex);
        }
        return data;
    }


    public static Message create(Source source) {
        return (source == null) ? 
            Messages.createEmpty(SOAPVersion.SOAP_11) : 
            Messages.createUsingPayload(source, SOAPVersion.SOAP_11);
    }

    public static Message create(DataSource ds, WSFeatureList f) {
        try {
            return (ds == null) ? 
                Messages.createEmpty(SOAPVersion.SOAP_11) : 
                create(ds.getContentType(), ds.getInputStream(), f);
        } catch(IOException ioe) {
            throw new WebServiceException(ioe);
        }
    }

    public static Message create(Exception e) {
        return new FaultMessage(SOAPVersion.SOAP_11);
    }

    /*
     * Get the content type ID from the content type.
     */
    private static int getContentId(String ct) {    
        try {
            final ContentType contentType = new ContentType(ct);
            return identifyContentType(contentType);
        } catch(Exception ex) {
            throw new WebServiceException(ex);
        }
    }
    
    /**
     * Return true if the content uses fast infoset.
     */
    public static boolean isFastInfoset(String ct) {    
        return (getContentId(ct) & FI_ENCODED_FLAG) != 0;
    }
    
    /*
     * Verify a contentType.
     *
     * @return
     * MIME_MULTIPART_FLAG | PLAIN_XML_FLAG
     * MIME_MULTIPART_FLAG | FI_ENCODED_FLAG;
     * PLAIN_XML_FLAG
     * FI_ENCODED_FLAG
     *
     */
    public static int identifyContentType(ContentType contentType) {
        String primary = contentType.getPrimaryType();
        String sub = contentType.getSubType();

        if (primary.equalsIgnoreCase("multipart") && sub.equalsIgnoreCase("related")) {
            String type = contentType.getParameter("type");
            if (type != null) {
                if (isXMLType(type)) {
                    return MIME_MULTIPART_FLAG | PLAIN_XML_FLAG;
                } else if (isFastInfosetType(type)) {
                    return MIME_MULTIPART_FLAG | FI_ENCODED_FLAG;
                }
            }
            return 0;
        } else if (isXMLType(primary, sub)) {
            return PLAIN_XML_FLAG;
        } else if (isFastInfosetType(primary, sub)) {
            return FI_ENCODED_FLAG;
        }
        return 0;
    }

    protected static boolean isXMLType(@NotNull String primary, @NotNull String sub) {
        return (primary.equalsIgnoreCase("text") && sub.equalsIgnoreCase("xml"))
                || (primary.equalsIgnoreCase("application") && sub.equalsIgnoreCase("xml"))
                || (primary.equalsIgnoreCase("application") && sub.toLowerCase().endsWith("+xml"));
    }

    protected static boolean isXMLType(String type) {
        String lowerType = type.toLowerCase();
        return lowerType.startsWith("text/xml")
                || lowerType.startsWith("application/xml")
                || (lowerType.startsWith("application/") && (lowerType.indexOf("+xml") != -1));
    }

    protected static boolean isFastInfosetType(String primary, String sub) {
        return primary.equalsIgnoreCase("application") && sub.equalsIgnoreCase("fastinfoset");
    }

    protected static boolean isFastInfosetType(String type) {
        return type.toLowerCase().startsWith("application/fastinfoset");
    }
    
    
    /**
     * Access a {@link Message} as a {@link DataSource}.
     * <p>
     * A {@link Message} implementation will implement this if the 
     * messages is to be access as data source.
     * <p>
     * TODO: consider putting as part of the API.
     */
    public static interface MessageDataSource {
        /**
         * Check if the data source has been consumed.
         * @return true of the data source has been consumed, otherwise false.
         */
        boolean hasUnconsumedDataSource();
        
        /**
         * Get the data source.
         * @return the data source.
         */
        DataSource getDataSource();
    }

    /**
     * It's conent-type is some XML type
     *
     */
    private static class XmlContent extends AbstractMessageImpl implements MessageDataSource {
        private final XmlDataSource dataSource;
        private boolean consumed;
        private Message delegate;
        private final HeaderList headerList;
//      private final WSBinding binding;
        private WSFeatureList features;
        
        public XmlContent(String ct, InputStream in, WSFeatureList f) {
            super(SOAPVersion.SOAP_11);
            dataSource = new XmlDataSource(ct, in);
            this.headerList = new HeaderList(SOAPVersion.SOAP_11);
//            this.binding = binding;
            features = f;
        }

        private Message getMessage() {
            if (delegate == null) {
                InputStream in = dataSource.getInputStream();
                assert in != null;
                delegate = Messages.createUsingPayload(new StreamSource(in), SOAPVersion.SOAP_11);
                consumed = true;
            }
            return delegate;
        }

        public boolean hasUnconsumedDataSource() {
            return !dataSource.consumed()&&!consumed;
        }

        public DataSource getDataSource() {
            return hasUnconsumedDataSource() ? dataSource :
                XMLMessage.getDataSource(getMessage(), features);
        }

        public boolean hasHeaders() {
            return false;
        }

        public @NotNull HeaderList getHeaders() {
            return headerList;
        }

        public String getPayloadLocalPart() {
            return getMessage().getPayloadLocalPart();
        }

        public String getPayloadNamespaceURI() {
            return getMessage().getPayloadNamespaceURI();
        }

        public boolean hasPayload() {
            return true;
        }

        public boolean isFault() {
            return false;
        }

        public Source readEnvelopeAsSource() {
            return getMessage().readEnvelopeAsSource();
        }

        public Source readPayloadAsSource() {
            return getMessage().readPayloadAsSource();
        }

        public SOAPMessage readAsSOAPMessage() throws SOAPException {
            return getMessage().readAsSOAPMessage();
        }

        public SOAPMessage readAsSOAPMessage(Packet packet, boolean inbound) throws SOAPException {
            return getMessage().readAsSOAPMessage(packet, inbound);
        }

        public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
            return (T)getMessage().readPayloadAsJAXB(unmarshaller);
        }
        /** @deprecated */
        public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
            return getMessage().readPayloadAsJAXB(bridge);
        }

        public XMLStreamReader readPayload() throws XMLStreamException {
            return getMessage().readPayload();
        }


        public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
            getMessage().writePayloadTo(sw);
        }

        public void writeTo(XMLStreamWriter sw) throws XMLStreamException {
            getMessage().writeTo(sw);
        }

        public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
            getMessage().writeTo(contentHandler, errorHandler);
        }

        public Message copy() {
            return getMessage().copy();
        }

        protected void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException {
            throw new UnsupportedOperationException();
        }

    }



    /**
     * Data represented as a multi-part MIME message. 
     * <p>
     * The root part may be an XML or an FI document. This class
     * parses MIME message lazily.
     */
    public static final class XMLMultiPart extends AbstractMessageImpl implements MessageDataSource {
        private final DataSource dataSource;
        private final StreamingAttachmentFeature feature;
        private Message delegate;
        private HeaderList headerList;// = new HeaderList();
//      private final WSBinding binding;
        private final WSFeatureList features;

        public XMLMultiPart(final String contentType, final InputStream is, WSFeatureList f) {
            super(SOAPVersion.SOAP_11);
            headerList = new HeaderList(SOAPVersion.SOAP_11);
            dataSource = createDataSource(contentType, is);
            this.feature = f.get(StreamingAttachmentFeature.class);
            this.features = f;
        }

        private Message getMessage() {
            if (delegate == null) {
                MimeMultipartParser mpp;
                try {
                    mpp = new MimeMultipartParser(dataSource.getInputStream(),
                            dataSource.getContentType(), feature);
                } catch(IOException ioe) {
                    throw new WebServiceException(ioe);
                }
                InputStream in = mpp.getRootPart().asInputStream();
                assert in != null;
                delegate = new PayloadSourceMessage(headerList, new StreamSource(in), new MimeAttachmentSet(mpp), SOAPVersion.SOAP_11);
            }
            return delegate;
        }

        public boolean hasUnconsumedDataSource() {
            return delegate == null;
        }

        public DataSource getDataSource() {
            return hasUnconsumedDataSource() ? dataSource :
                XMLMessage.getDataSource(getMessage(), features);
        }

        public boolean hasHeaders() {
            return false;
        }

        public @NotNull HeaderList getHeaders() {
            return headerList;
        }

        public String getPayloadLocalPart() {
            return getMessage().getPayloadLocalPart();
        }

        public String getPayloadNamespaceURI() {
            return getMessage().getPayloadNamespaceURI();
        }

        public boolean hasPayload() {
            return true;
        }

        public boolean isFault() {
            return false;
        }

        public Source readEnvelopeAsSource() {
            return getMessage().readEnvelopeAsSource();
        }

        public Source readPayloadAsSource() {
            return getMessage().readPayloadAsSource();
        }

        public SOAPMessage readAsSOAPMessage() throws SOAPException {
            return getMessage().readAsSOAPMessage();
        }

        public SOAPMessage readAsSOAPMessage(Packet packet, boolean inbound) throws SOAPException {
            return getMessage().readAsSOAPMessage(packet, inbound);
        }

        public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
            return (T)getMessage().readPayloadAsJAXB(unmarshaller);
        }

        public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
            return getMessage().readPayloadAsJAXB(bridge);
        }

        public XMLStreamReader readPayload() throws XMLStreamException {
            return getMessage().readPayload();
        }

        public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
            getMessage().writePayloadTo(sw);
        }

        public void writeTo(XMLStreamWriter sw) throws XMLStreamException {
            getMessage().writeTo(sw);
        }

        public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
            getMessage().writeTo(contentHandler, errorHandler);
        }

        public Message copy() {
            return getMessage().copy();
        }

        protected void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isOneWay(@NotNull WSDLPort port) {
            return false;
        }

        public @NotNull AttachmentSet getAttachments() {
            return getMessage().getAttachments();
        }

    }

    private static class FaultMessage extends EmptyMessageImpl {

        public FaultMessage(SOAPVersion version) {
            super(version);
        }

        @Override
        public boolean isFault() {
            return true;
        }
    }

    
    /**
     * Don't know about this content. It's conent-type is NOT the XML types
     * we recognize(text/xml, application/xml, multipart/related;text/xml etc).
     *
     * This could be used to represent image/jpeg etc
     */
    public static class UnknownContent extends AbstractMessageImpl implements MessageDataSource {
        private final DataSource ds;
        private final HeaderList headerList;
        
        public UnknownContent(final String ct, final InputStream in) {
            this(createDataSource(ct,in));
        }
        
        public UnknownContent(DataSource ds) {
            super(SOAPVersion.SOAP_11);
            this.ds = ds;
            this.headerList = new HeaderList(SOAPVersion.SOAP_11);
        }

        /*
         * Copy constructor.
         */
        private UnknownContent(UnknownContent that) {
            super(that.soapVersion);
            this.ds = that.ds;
            this.headerList = HeaderList.copy(that.headerList);
        }

        public boolean hasUnconsumedDataSource() {
            return true;
        }

        public DataSource getDataSource() {
            assert ds != null;
            return ds;
        }

        protected void writePayloadTo(ContentHandler contentHandler, 
                ErrorHandler errorHandler, boolean fragment) throws SAXException {
            throw new UnsupportedOperationException();
        }

        public boolean hasHeaders() {
            return false;
        }
        
        public boolean isFault() {
            return false;
        }

        public HeaderList getHeaders() {
            return headerList;
        }

        public String getPayloadLocalPart() {
            throw new UnsupportedOperationException();
        }

        public String getPayloadNamespaceURI() {
            throw new UnsupportedOperationException();
        }

        public boolean hasPayload() {
            return false;
        }

        public Source readPayloadAsSource() {
            return null;
        }

        public XMLStreamReader readPayload() throws XMLStreamException {
            throw new WebServiceException("There isn't XML payload. Shouldn't come here.");
        }

        public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
            // No XML. Nothing to do
        }

        public Message copy() {
            return new UnknownContent(this);
        }

    }

    public static DataSource getDataSource(Message msg, WSFeatureList f) {
        if (msg == null)
            return null;
        if (msg instanceof MessageDataSource) {
            return ((MessageDataSource)msg).getDataSource();
        } else {
            AttachmentSet atts = msg.getAttachments();
            if (atts != null && !atts.isEmpty()) {
                final ByteArrayBuffer bos = new ByteArrayBuffer();
                try {
                    Codec codec = new XMLHTTPBindingCodec(f);
                    Packet packet = new Packet(msg);
                    com.sun.xml.ws.api.pipe.ContentType ct = codec.getStaticContentType(packet);
                    codec.encode(packet, bos);
                    return createDataSource(ct.getContentType(), bos.newInputStream());
                } catch(IOException ioe) {
                    throw new WebServiceException(ioe);
                }
                
            } else {
                final ByteArrayBuffer bos = new ByteArrayBuffer();
                XMLStreamWriter writer = XMLStreamWriterFactory.create(bos);
                try {
                    msg.writePayloadTo(writer);
                    writer.flush();
                } catch (XMLStreamException e) {
                    throw new WebServiceException(e);
                }
                return XMLMessage.createDataSource("text/xml", bos.newInputStream());
            }       
        }
    }
    
    public static DataSource createDataSource(final String contentType, final InputStream is) {
        return new XmlDataSource(contentType, is);
    }

    private static class XmlDataSource implements DataSource {
        private final String contentType;
        private final InputStream is;
        private boolean consumed;

        XmlDataSource(String contentType, final InputStream is) {
            this.contentType = contentType;
            this.is = is;
        }

        public boolean consumed() {
            return consumed;
        }

        public InputStream getInputStream() {
            consumed = !consumed;
            return is;
        }

        public OutputStream getOutputStream() {
            return null;
        }

        public String getContentType() {
            return contentType;
        }

        public String getName() {
            return "";
        }
    }
}
