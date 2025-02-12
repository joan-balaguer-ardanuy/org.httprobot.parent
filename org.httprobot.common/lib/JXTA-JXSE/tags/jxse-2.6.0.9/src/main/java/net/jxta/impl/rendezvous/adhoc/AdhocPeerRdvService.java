/*
 * Copyright (c) 2001-2007 Sun Microsystems, Inc.  All rights reserved.
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

package net.jxta.impl.rendezvous.adhoc;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.Messenger;
import net.jxta.id.ID;
import net.jxta.impl.protocol.RdvConfigAdv;
import net.jxta.impl.rendezvous.RendezVousPropagateMessage;
import net.jxta.impl.rendezvous.RendezVousServiceImpl;
import net.jxta.impl.rendezvous.RendezVousServiceProvider;
import net.jxta.impl.rendezvous.rendezvousMeter.RendezvousMeterBuildSettings;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.Module;
import net.jxta.protocol.ConfigParams;
import net.jxta.rendezvous.RendezvousEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * A JXTA {@link net.jxta.rendezvous.RendezVousService} implementation which
 * implements the ad hoc portion of the standard JXTA Rendezvous Protocol (RVP).
 *
 * @see net.jxta.rendezvous.RendezVousService
 * @see <a href="https://jxta-spec.dev.java.net/nonav/JXTAProtocols.html#proto-rvp" target="_blank">JXTA Protocols Specification : Rendezvous Protocol</a>
 */
public class AdhocPeerRdvService extends RendezVousServiceProvider {

    /**
     * Log4J Logger
     */
    private final static transient Logger LOG = Logger.getLogger(AdhocPeerRdvService.class.getName());

    /**
     * Default Maximum TTL. This is minimum needed to bridge networks.
     */
    private static final int DEFAULT_MAX_TTL = 2;

    /**
     * Constructor
     *
     * @param g          the peergroup
     * @param rdvService the rendezvous service
     */
    public AdhocPeerRdvService(PeerGroup g, RendezVousServiceImpl rdvService) {

        super(g, rdvService);

        ConfigParams confAdv = g.getConfigAdvertisement();

        // Get the config. If we do not have a config, we're done; we just keep
        // the defaults (edge peer/no auto-rdv)
        if (confAdv != null) {
            Advertisement adv = null;

            try {
                XMLDocument configDoc = (XMLDocument) confAdv.getServiceParam(rdvService.getAssignedID());

                if (null != configDoc) {
                    // XXX 20041027 backwards compatibility
                    configDoc.addAttribute("type", RdvConfigAdv.getAdvertisementType());

                    adv = AdvertisementFactory.newAdvertisement(configDoc);
                }
            } catch (java.util.NoSuchElementException ignored) {// ignored
            }

            if (adv instanceof RdvConfigAdv) {
                RdvConfigAdv rdvConfigAdv = (RdvConfigAdv) adv;

                MAX_TTL = (-1 != rdvConfigAdv.getMaxTTL()) ? rdvConfigAdv.getMaxTTL() : DEFAULT_MAX_TTL;
            } else {
                MAX_TTL = DEFAULT_MAX_TTL;
            }
        } else {
            MAX_TTL = DEFAULT_MAX_TTL;
        }

        Logging.logCheckedInfo(LOG, "RendezVous Service is initialized for ", g.getPeerGroupID(), " as an ad hoc peer. ");
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int startApp(String[] arg) {

        super.startApp(arg);

        // The other services may not be fully functional but they're there
        // so we can start our subsystems.
        // As for us, it does not matter if our methods are called between init
        // and startApp().

        if (RendezvousMeterBuildSettings.RENDEZVOUS_METERING && (rendezvousMeter != null)) {
            rendezvousMeter.startEdge();
        }

        // FIXME: Declaring this peer as an EDGE is really questionable and misleading
        // we are nominally an edge peer
        rdvService.generateEvent(RendezvousEvent.BECAMEEDGE, group.getPeerID());

        return Module.START_OK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stopApp() {

        if (closed) {
            return;
        }

        closed = true;

        super.stopApp();

        if (RendezvousMeterBuildSettings.RENDEZVOUS_METERING && (rendezvousMeter != null)) {
            rendezvousMeter.stopEdge();
        }
    }

    /**
     * {@inheritDoc}
     * </p>By default an ADHOC peer is never connected to other peers.
     */
    @Override
    public Vector<ID> getConnectedPeerIDs() {

        return new Vector<ID>(0);
    }

    /**
     * {@inheritDoc}
     * </p>By default, ADHOC peers are never connected to Rendezvous peers.
     */
    @Override
    public boolean isConnectedToRendezVous() {
        return false;
    }

    /**
     * {@inheritDoc}
     * </p>This method does nothing. It should not be called on ADHOC peers.
     */
    @Override
    public void connectToRendezVous(EndpointAddress addr, Object hint) throws IOException {

        Logging.logCheckedWarning(LOG, "Invalid call to connectToRendezVous() on ADHOC peer");

    }

    /**
     * {@inheritDoc}
     * </p>This method does nothing. It should not be called on ADHOC peers.
     */
    @Override
    public void challengeRendezVous(ID peer, long delay) {

        Logging.logCheckedWarning(LOG, "Invalid call to challengeRendezVous() on ADHOC peer");

    }

    /**
     * {@inheritDoc}
     * </p>This method does nothing. It should not be called on ADHOC peers.
     */
    @Override
    public void disconnectFromRendezVous(ID peerId) {

        Logging.logCheckedWarning(LOG, "Invalid call to disconnectFromRendezVous() on ADHOC peer");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagate(Message msg, String serviceName, String serviceParam, int ttl) throws IOException {

        ttl = Math.min(ttl, MAX_TTL);

        RendezVousPropagateMessage propHdr = updatePropHeader(msg, getPropHeader(msg), serviceName, serviceParam, ttl);

        if (null != propHdr) {
            sendToNetwork(msg, propHdr);

            if (RendezvousMeterBuildSettings.RENDEZVOUS_METERING && (rendezvousMeter != null)) {
                rendezvousMeter.propagateToGroup();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateInGroup(Message msg, String serviceName, String serviceParam, int ttl) throws IOException {

        ttl = Math.min(ttl, MAX_TTL);

        RendezVousPropagateMessage propHdr = updatePropHeader(msg, getPropHeader(msg), serviceName, serviceParam, ttl);

        if (null != propHdr) {
            sendToNetwork(msg, propHdr);

            if (RendezvousMeterBuildSettings.RENDEZVOUS_METERING && (rendezvousMeter != null)) {
                rendezvousMeter.propagateToGroup();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagate(Enumeration<? extends ID> destPeerIDs, Message msg, String serviceName, String serviceParam, int ttl) {

        ttl = Math.min(ttl, MAX_TTL);

        RendezVousPropagateMessage propHdr = updatePropHeader(msg, getPropHeader(msg), serviceName, serviceParam, ttl);

        if (null != propHdr) {
            int numPeers = 0;

            try {

                while (destPeerIDs.hasMoreElements()) {

                    ID dest = destPeerIDs.nextElement();


                    EndpointAddress addr = mkAddress(dest, PropSName, PropPName);

                    Messenger messenger = rdvService.endpoint.getMessenger(addr);

                    if (null != messenger) {
                        try {
                            messenger.sendMessage(msg);
                            numPeers++;
                        } catch (IOException failed) {// ignored
                        }
                    }
                }
            } finally {
                if (RendezvousMeterBuildSettings.RENDEZVOUS_METERING && (rendezvousMeter != null)) {
                    rendezvousMeter.propagateToPeers(numPeers);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateToNeighbors(Message msg, String serviceName, String serviceParam, int ttl) throws IOException {

        ttl = Math.min(ttl, MAX_TTL);

        RendezVousPropagateMessage propHdr = updatePropHeader(msg, getPropHeader(msg), serviceName, serviceParam, ttl);

        if (null != propHdr) {
            try {
                sendToNetwork(msg, propHdr);

                if (RendezvousMeterBuildSettings.RENDEZVOUS_METERING && (rendezvousMeter != null)) {
                    rendezvousMeter.propagateToNeighbors();
                }
            } catch (IOException failed) {
                if (RendezvousMeterBuildSettings.RENDEZVOUS_METERING && (rendezvousMeter != null)) {
                    rendezvousMeter.propagateToNeighborsFailed();
                }

                throw failed;
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The definition of walk says that we should forward the message to the
     * most appropriate peer. Since we don't make any effort keep track of other
     * peers we don't have anywhere to send the message.
     */
    @Override
    public void walk(Message msg, String serviceName, String serviceParam, int ttl) throws IOException {
        // Do nothing. Really.
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Unlike the undirected walk we are told where to send the message so we
     * deliver it as requested.
     */
    @Override
    public void walk(Vector<? extends ID> destPeerIDs, Message msg, String serviceName, String serviceParam, int ttl) throws IOException {

        propagate(Collections.enumeration(destPeerIDs), msg, serviceName,
                  serviceParam, ttl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void repropagate(Message msg, RendezVousPropagateMessage propHdr, String serviceName, String serviceParam) {


        if (RendezvousMeterBuildSettings.RENDEZVOUS_METERING && (rendezvousMeter != null)) {
            rendezvousMeter.receivedMessageRepropagatedInGroup();
        }

        try {
            propHdr = updatePropHeader(msg, propHdr, serviceName, serviceParam, MAX_TTL);

            if (null != propHdr) {
                sendToNetwork(msg, propHdr);
            } else {


            }

        } catch (Exception ez1) {

            // Not much we can do
            if (propHdr != null) {
                Logging.logCheckedWarning(LOG, "Failed to repropagate ", msg, " (", propHdr.getMsgId(), ")\n", ez1);
            } else {
                Logging.logCheckedWarning(LOG, "Could to repropagate ", msg, "\n", ez1);
            }

        }
    }
}
