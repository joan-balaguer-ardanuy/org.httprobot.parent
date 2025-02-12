/*
 * Copyright (c) 2002-2007 Sun Microsystems, Inc.  All rights reserved.
 *  
 *  The Sun Project JXTA(TM) Software License
 *  
 *  Redistribution and use in source and binary forms, with or without 
 *  modification, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice, 
 *     this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must 
 *     include the following acknowledgment: "This product includes software 
 *     developed by Sun Microsystems, Inc. for JXTA(TM) technology." 
 *     Alternately, this acknowledgment may appear in the software itself, if 
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must 
 *     not be used to endorse or promote products derived from this software 
 *     without prior written permission. For written permission, please contact 
 *     Project JXTA at http://www.jxta.org.
 *  
 *  5. Products derived from this software may not be called "JXTA", nor may 
 *     "JXTA" appear in their name, without prior written permission of Sun.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SUN 
 *  MICROSYSTEMS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  JXTA is a registered trademark of Sun Microsystems, Inc. in the United 
 *  States and other countries.
 *  
 *  Please see the license information page at :
 *  <http://www.jxta.org/project/www/license.html> for instructions on use of 
 *  the license in source files.
 *  
 *  ====================================================================
 *  
 *  This software consists of voluntary contributions made by many individuals 
 *  on behalf of Project JXTA. For more information on Project JXTA, please see 
 *  http://www.jxta.org.
 *  
 *  This license is based on the BSD license adopted by the Apache Foundation. 
 */

package net.jxta.protocol;


import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.jxta.document.*;
import net.jxta.id.IDFactory;
import net.jxta.credential.Credential;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.WorldPeerGroupFactory;
import net.jxta.pipe.PipeID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.SignedAdvertisement;

import net.jxta.exception.PeerGroupException;

import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.impl.protocol.SignedAdv;
import org.junit.Ignore;


/**
 * */
@Ignore("JXTA Configurator required")
public class SignedAdvertisementTest extends TestCase {
    
    static PeerGroup pg;
    static Credential cred;
    
    public SignedAdvertisementTest() {
        try {
            synchronized (SignedAdvertisementTest.class) {
                if (null == pg) {
                    pg = PeerGroupFactory.newNetPeerGroup(new WorldPeerGroupFactory().getInterface());
                }
                               
                MembershipService membership = pg.getMembershipService();
                
                cred = membership.getDefaultCredential();
                
                if (null == cred) {
                    AuthenticationCredential authCred = new AuthenticationCredential(pg, "StringAuthentication", null);
                    
                    StringAuthenticator auth = null;

                    try {
                        auth = (StringAuthenticator) membership.apply(authCred);
                    } catch (Exception failed) {
                        ;
                    }
                    
                    if (null != auth) {
                        auth.setAuth1_KeyStorePassword("password".toCharArray());
                        auth.setAuth2Identity(pg.getPeerID());
                        auth.setAuth3_IdentityPassword("password".toCharArray());
                        
                        if (auth.isReadyForJoin()) {
                            membership.join(auth);
                        }
                    }
                }
                
                cred = membership.getDefaultCredential();
                
                if (null == cred) {
                    throw new IllegalStateException("Could not get credential");
                }
            }
        } catch (PeerGroupException failed) {
            throw new UndeclaredThrowableException(failed, "Could not initialize peergroup");
        }
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
        System.err.flush();
        System.out.flush();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SignedAdvertisementTest.class);

        return suite;
    }
    
    public void testSignedAdvertisement() {
        try {
            SignedAdvertisement signedAdv = (SignedAdvertisement) AdvertisementFactory.newAdvertisement(
                    SignedAdvertisement.getAdvertisementType());
            
            signedAdv.setAdvertisement(pg.getPeerAdvertisement());
            signedAdv.setSigner(cred);
            
            XMLDocument asDoc = (XMLDocument) signedAdv.getDocument(MimeMediaType.XMLUTF8);
            
            SignedAdvertisement signedAdv2 = (SignedAdvertisement) AdvertisementFactory.newAdvertisement(asDoc);
            
            PeerAdvertisement pa = (PeerAdvertisement) signedAdv2.getAdvertisement();
        } catch (Throwable caught) {
            caught.printStackTrace();
            fail("Throwable thrown : " + caught.getMessage());
        }
    }
}
