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

package com.sun.tools.ws.processor.model.jaxb;

import com.sun.tools.ws.processor.model.AbstractType;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 *
 * RPC Structure that will be used to create RpcLitPayload latter
 */
public class RpcLitStructure extends AbstractType {
    private List<RpcLitMember> members;
    private JAXBModel jaxbModel;

    /**
     *
     */
    public RpcLitStructure() {
        super();
        // TODO Auto-generated constructor stub
    }
    public RpcLitStructure(QName name, JAXBModel jaxbModel){
        setName(name);
        this.jaxbModel = jaxbModel;
        this.members = new ArrayList<RpcLitMember>();

    }
    public RpcLitStructure(QName name, JAXBModel jaxbModel, List<RpcLitMember> members){
        setName(name);
        this.members = members;
    }

    public void accept(JAXBTypeVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    public List<RpcLitMember> getRpcLitMembers(){
        return members;
    }

    public List<RpcLitMember> setRpcLitMembers(List<RpcLitMember> members){
        return this.members = members;
    }

    public void addRpcLitMember(RpcLitMember member){
        members.add(member);
    }
    /**
     * @return Returns the jaxbModel.
     */
    public JAXBModel getJaxbModel() {
        return jaxbModel;
    }
    /**
     * @param jaxbModel The jaxbModel to set.
     */
    public void setJaxbModel(JAXBModel jaxbModel) {
        this.jaxbModel = jaxbModel;
    }

    public boolean isLiteralType() {
        return true;
    }
}
