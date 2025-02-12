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

package com.sun.xml.ws.util.xml;

import com.sun.istack.NotNull;
import com.sun.istack.SAXParseException2;
import com.sun.istack.XMLStreamReaderToContentHandler;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

/**
 * A JAXP {@link javax.xml.transform.Source} implementation that wraps
 * the specified {@link javax.xml.stream.XMLStreamReader} or
 * {@link javax.xml.stream.XMLEventReader} for use by applications that
 * expext a {@link javax.xml.transform.Source}.
 *
 * <p>
 * The fact that StAXSource derives from SAXSource is an implementation
 * detail. Thus in general applications are strongly discouraged from
 * accessing methods defined on SAXSource. In particular:
 *
 * <ul>
 *   <li> The setXMLReader and setInputSource methods shall never be called.</li>
 *   <li> The XMLReader object obtained by the getXMLReader method shall
 *        be used only for parsing the InputSource object returned by
 *        the getInputSource method.</li>
 *   <li> The InputSource object obtained by the getInputSource method shall
 *        be used only for being parsed by the XMLReader object returned by
 *        the getXMLReader method.</li>
 * </ul>
 *
 * <p>
 * Example:
 *
 * <pre>
    // create a StAXSource
    XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(args[0]));
    Source staxSource = new StAXSource(reader);

    // createa StreamResult
    Result streamResult = new StreamResult(System.out);

    // run the transform
    TransformerFactory.newInstance().newTransformer().transform(staxSource, streamResult);
 * </pre>
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
public class StAXSource extends SAXSource {

    // StAX to SAX converter that will read from StAX and produce SAX
    // this object will be wrapped by the XMLReader exposed to the client
    private final XMLStreamReaderToContentHandler reader;

    private final XMLStreamReader staxReader;

    // SAX allows ContentHandler to be changed during the parsing,
    // but JAXB doesn't. So this repeater will sit between those
    // two components.
    private XMLFilterImpl repeater = new XMLFilterImpl();

    // this object will pretend as an XMLReader.
    // no matter what parameter is specified to the parse method,
    // it will just read from the StAX reader.
    private final XMLReader pseudoParser = new XMLReader() {
        public boolean getFeature(String name) throws SAXNotRecognizedException {
            throw new SAXNotRecognizedException(name);
        }

        public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
            // Should support these two features according to XMLReader javadoc.
            if (name.equals("http://xml.org/sax/features/namespaces") && value) {
                // Ignore for now
            } else if (name.equals("http://xml.org/sax/features/namespace-prefixes") && !value) {
                // Ignore for now
            } else {
                throw new SAXNotRecognizedException(name);
            }
        }

        public Object getProperty(String name) throws SAXNotRecognizedException {
            if( "http://xml.org/sax/properties/lexical-handler".equals(name) ) {
                return lexicalHandler;
            }
            throw new SAXNotRecognizedException(name);
        }

        public void setProperty(String name, Object value) throws SAXNotRecognizedException {
            if( "http://xml.org/sax/properties/lexical-handler".equals(name) ) {
                this.lexicalHandler = (LexicalHandler)value;
                return;
            }
            throw new SAXNotRecognizedException(name);
        }

        private LexicalHandler lexicalHandler;

        // we will store this value but never use it by ourselves.
        private EntityResolver entityResolver;
        public void setEntityResolver(EntityResolver resolver) {
            this.entityResolver = resolver;
        }
        public EntityResolver getEntityResolver() {
            return entityResolver;
        }

        private DTDHandler dtdHandler;
        public void setDTDHandler(DTDHandler handler) {
            this.dtdHandler = handler;
        }
        public DTDHandler getDTDHandler() {
            return dtdHandler;
        }

        public void setContentHandler(ContentHandler handler) {
            repeater.setContentHandler(handler);
        }
        public ContentHandler getContentHandler() {
            return repeater.getContentHandler();
        }

        private ErrorHandler errorHandler;
        public void setErrorHandler(ErrorHandler handler) {
            this.errorHandler = handler;
        }
        public ErrorHandler getErrorHandler() {
            return errorHandler;
        }

        public void parse(InputSource input) throws SAXException {
            parse();
        }

        public void parse(String systemId) throws SAXException {
            parse();
        }

        public void parse() throws SAXException {
            // parses from a StAX reader and generates SAX events which
            // go through the repeater and are forwarded to the appropriate
            // component
            try {
                reader.bridge();
            } catch( XMLStreamException e ) {
                // wrap it in a SAXException
                SAXParseException se =
                    new SAXParseException2(
                        e.getMessage(),
                        null,
                        null,
                        e.getLocation() == null ? -1 : e.getLocation().getLineNumber(),
                        e.getLocation() == null ? -1 : e.getLocation().getColumnNumber(),
                        e);

                // if the consumer sets an error handler, it is our responsibility
                // to notify it.
                if(errorHandler!=null)
                    errorHandler.fatalError(se);

                // this is a fatal error. Even if the error handler
                // returns, we will abort anyway.
                throw se;

            } finally {
                try {
                    staxReader.close();
                } catch(XMLStreamException xe) {
                    //falls through. Not much can be done.
                }
            }
        }
    };

    /**
     * Creates a new {@link javax.xml.transform.Source} for the given
     * {@link XMLStreamReader}.
     *
     * @param reader XMLStreamReader that will be exposed as a Source
     * @param eagerQuit if true, when the conversion is completed, leave the cursor to the last
     *                  event that was fired (such as end element)
     * @see #StAXSource(XMLStreamReader, boolean, String[])
     */
    public StAXSource(XMLStreamReader reader, boolean eagerQuit) {
        this(reader, eagerQuit, new String[0]);
    }

    /**
     * Creates a new {@link javax.xml.transform.Source} for the given
     * {@link XMLStreamReader}.
     *
     * The XMLStreamReader must be pointing at either a
     * {@link javax.xml.stream.XMLStreamConstants#START_DOCUMENT} or
     * {@link javax.xml.stream.XMLStreamConstants#START_ELEMENT} event.
     *
     * @param reader XMLStreamReader that will be exposed as a Source
     * @param eagerQuit if true, when the conversion is completed, leave the cursor to the last
     *                  event that was fired (such as end element)
     * @param inscope inscope Namespaces
     *                array of the even length of the form { prefix0, uri0, prefix1, uri1, ... }
     * @throws IllegalArgumentException iff the reader is null
     * @throws IllegalStateException iff the reader is not pointing at either a
     * START_DOCUMENT or START_ELEMENT event
     */
    public StAXSource(XMLStreamReader reader, boolean eagerQuit, @NotNull String[] inscope) {
        if( reader == null )
            throw new IllegalArgumentException();
        this.staxReader = reader;

        int eventType = reader.getEventType();
        if (!(eventType == XMLStreamConstants.START_DOCUMENT)
            && !(eventType == XMLStreamConstants.START_ELEMENT)) {
            throw new IllegalStateException();
        }

        this.reader = new XMLStreamReaderToContentHandler(reader,repeater,eagerQuit,false,inscope);

        super.setXMLReader(pseudoParser);
        // pass a dummy InputSource. We don't care
        super.setInputSource(new InputSource());
    }

//    /**
//     * Creates a new {@link javax.xml.transform.Source} for the given
//     * {@link XMLEventReader}.
//     *
//     * The XMLEventReader must be pointing at either a
//     * {@link javax.xml.stream.XMLStreamConstants#START_DOCUMENT} or
//     * {@link javax.xml.stream.XMLStreamConstants#START_ELEMENT} event.
//     *
//     * @param reader XMLEventReader that will be exposed as a Source
//     * @throws IllegalArgumentException iff the reader is null
//     * @throws IllegalStateException iff the reader is not pointing at either a
//     * START_DOCUEMENT or START_ELEMENT event
//     */
//    public StAXSource(XMLEventReader reader) {
//        if( reader == null )
//            throw new IllegalArgumentException();
//
//        // TODO: detect IllegalStateException for START_ELEMENT|DOCUMENT
//        // bugid 5046340 - peek not implemented
//        // XMLEvent event = staxEventReader.peek();
//
//        this.reader =
//            new XMLEventReaderToContentHandler(
//                reader,
//                repeater);
//
//        super.setXMLReader(pseudoParser);
//        // pass a dummy InputSource. We don't care
//        super.setInputSource(new InputSource());
//    }

}
