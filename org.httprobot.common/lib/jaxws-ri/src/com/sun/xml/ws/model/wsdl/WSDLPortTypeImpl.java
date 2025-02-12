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

package com.sun.xml.ws.model.wsdl;

import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.Hashtable;
import java.util.Map;

/**
 * Provides implementation of {@link WSDLPortType}
 *
 * @author Vivek Pandey
 */
public final class WSDLPortTypeImpl  extends AbstractExtensibleImpl implements WSDLPortType {
    private QName name;
    private final Map<String, WSDLOperationImpl> portTypeOperations;
    private WSDLModelImpl owner;

    public WSDLPortTypeImpl(XMLStreamReader xsr,WSDLModelImpl owner, QName name) {
        super(xsr);
        this.name = name;
        this.owner = owner;
        portTypeOperations = new Hashtable<String, WSDLOperationImpl>();
    }

    public QName getName() {
        return name;
    }

    public WSDLOperationImpl get(String operationName) {
        return portTypeOperations.get(operationName);
    }

    public Iterable<WSDLOperationImpl> getOperations() {
        return portTypeOperations.values();
    }

    /**
     * Populates the Map that holds operation name as key and {@link WSDLOperation} as the value.
     * @param opName Must be non-null
     * @param ptOp  Must be non-null
     * @throws NullPointerException if either opName or ptOp is null
     */
    public void put(String opName, WSDLOperationImpl ptOp){
        portTypeOperations.put(opName, ptOp);
    }

    WSDLModelImpl getOwner(){
        return owner;
    }

    void freeze() {
        for(WSDLOperationImpl op : portTypeOperations.values()){
            op.freez(owner);
        }
    }
}
