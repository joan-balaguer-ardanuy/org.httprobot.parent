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

package com.sun.xml.ws.api.server;

import java.util.concurrent.Executor;

/**
 * ContainerResolver based on {@link ThreadLocal}.
 * <p>
 * The ThreadLocalContainerResolver is the default implementation available
 * from the ContainerResolver using {@link ContainerResolver#getDefault()}.  Code
 * sections that run with a Container must use the following pattern:
 * <pre>
 *   public void m() {
 *     Container old = ContainerResolver.getDefault().enterContainer(myContainer);
 *     try {
 *       // ... method body
 *     } finally {
 *       ContainerResolver.getDefault().exitContainer(old);
 *     }
 *   }
 * </pre>
 * @since 2.2.7
 */
public class ThreadLocalContainerResolver extends ContainerResolver {
    private ThreadLocal<Container> containers = new ThreadLocal<Container>() {
        @Override
        protected Container initialValue() {
            return Container.NONE;
        }
    };
    
    public Container getContainer() {
        return containers.get();
    }
    
    /**
     * Enters container
     * @param container Container to set
     * @return Previous container; must be remembered and passed to exitContainer
     */
    public Container enterContainer(Container container) {
        Container old = containers.get();
        containers.set(container);
        return old;
    }
    
    /**
     * Exits container
     * @param old Container returned from enterContainer
     */
    public void exitContainer(Container old) {
        containers.set(old);
    }
    
    /**
     * Used by {@link Engine} to wrap asynchronous {@link Fiber} executions
     * @param container Container
     * @param ex Executor to wrap
     * @return an Executor that will set the container during executions of Runnables
     */
    public Executor wrapExecutor(final Container container, final Executor ex) {
        if (ex == null)
            return null;
        
        return new Executor() {
            @Override
            public void execute(final Runnable command) {
                ex.execute(new Runnable() {
                    @Override
                    public void run() {
                        Container old = enterContainer(container);
                        try {
                            command.run();
                        } finally {
                            exitContainer(old);
                        }
                    }
                });
            }
        };
    }
}
