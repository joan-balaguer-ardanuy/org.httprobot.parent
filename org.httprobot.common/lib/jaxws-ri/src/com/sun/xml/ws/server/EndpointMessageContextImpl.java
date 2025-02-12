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

package com.sun.xml.ws.server;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Attachment;

import java.util.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceContext;
import javax.activation.DataHandler;

/**
 * Implements {@link WebServiceContext}'s {@link MessageContext} on top of {@link Packet}.
 *
 * <p>
 * This class creates a {@link Map} view for APPLICATION scoped properties that
 * gets exposed to endpoint implementations during the invocation
 * of web methods. The implementations access this map using
 * WebServiceContext.getMessageContext().
 *
 * <p>
 * Some of the {@link Map} methods requre this class to
 * build the complete {@link Set} of properties, but we
 * try to avoid that as much as possible.
 *
 *
 * @author Jitendra Kotamraju
 */
public final class EndpointMessageContextImpl extends AbstractMap<String,Object> implements MessageContext {

    /**
     * Lazily computed.
     */
    private Set<Map.Entry<String,Object>> entrySet;
    private final Packet packet;

    /**
     * @param packet
     *      The {@link Packet} to wrap.
     */
    public EndpointMessageContextImpl(Packet packet) {
        this.packet = packet;
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public Object get(Object key) {
        if (packet.supports(key)) {
            return packet.get(key);    // strongly typed
        }
        if (packet.getHandlerScopePropertyNames(true).contains(key)) {
            return null;            // no such application-scope property
        }
        Object value =  packet.invocationProperties.get(key);

        //add the attachments from the Message to the corresponding attachment property
        if(key.equals(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS) ||
                key.equals(MessageContext.INBOUND_MESSAGE_ATTACHMENTS)){
            Map<String, DataHandler> atts = (Map<String, DataHandler>) value;
            if(atts == null)
                atts = new HashMap<String, DataHandler>();
            AttachmentSet attSet = packet.getMessage().getAttachments();
            for(Attachment att : attSet){
                atts.put(att.getContentId(), att.asDataHandler());
            }
            return atts;
        }
        return value;
    }

    @Override
    public Object put(String key, Object value) {
        if (packet.supports(key)) {
            return packet.put(key, value);     // strongly typed
        }
        Object old = packet.invocationProperties.get(key);
        if (old != null) {
            if (packet.getHandlerScopePropertyNames(true).contains(key)) {
                throw new IllegalArgumentException("Cannot overwrite property in HANDLER scope");
            }
            // Overwrite existing APPLICATION scoped property
            packet.invocationProperties.put(key, value);
            return old;
        }
        // No existing property. So Add a new property
        packet.invocationProperties.put(key, value);
        return null;
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public Object remove(Object key) {
         if (packet.supports(key)) {
             return packet.remove(key);
        }
        Object old = packet.invocationProperties.get(key);
        if (old != null) {
            if (packet.getHandlerScopePropertyNames(true).contains(key)) {
                throw new IllegalArgumentException("Cannot remove property in HANDLER scope");
            }
            // Remove existing APPLICATION scoped property
            packet.invocationProperties.remove(key);
            return old;
        }
        // No existing property.
        return null;
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    public void setScope(String name, MessageContext.Scope scope) {
        throw new UnsupportedOperationException(
                "All the properties in this context are in APPLICATION scope. Cannot do setScope().");
    }

    public MessageContext.Scope getScope(String name) {
        throw new UnsupportedOperationException(
                "All the properties in this context are in APPLICATION scope. Cannot do getScope().");
    }

    private class EntrySet extends AbstractSet<Map.Entry<String, Object>> {

        public Iterator<Map.Entry<String, Object>> iterator() {
            final Iterator<Map.Entry<String, Object>> it = createBackupMap().entrySet().iterator();

            return new Iterator<Map.Entry<String, Object>>() {
                Map.Entry<String, Object> cur;

                public boolean hasNext() {
                    return it.hasNext();
                }

                public Map.Entry<String, Object> next() {
                    cur = it.next();
                    return cur;
                }

                public void remove() {
                    it.remove();
                    EndpointMessageContextImpl.this.remove(cur.getKey());
                }
            };
        }

        public int size() {
            return createBackupMap().size();
        }

    }

    private Map<String, Object> createBackupMap() {
        Map<String, Object> backupMap = new HashMap<String, Object>();
        backupMap.putAll(packet.createMapView());
        Set<String> handlerProps = packet.getHandlerScopePropertyNames(true);
        for(Map.Entry<String, Object> e : packet.invocationProperties.entrySet()) {
            if (!handlerProps.contains(e.getKey())) {
                backupMap.put(e.getKey(), e.getValue());
            }
        }
        return backupMap;
    }

}
