/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.ws.wscompile;

import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.ws.Invoker;

import javax.annotation.processing.Filer;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provide common jaxws tool options.
 *
 * @author Vivek Pandey
 */
public class Options {
    /**
     * -verbose
     */
    public boolean verbose;

    /**
     * - quite
     */
    public boolean quiet;

    /**
     * -keep
     */
    public boolean keep;

    

    /**
     * -d
     */
    public File destDir = new File(".");


    /**
     * -s
     */
    public File sourceDir;

    /**
     * The filer that can use used to write out the generated files
     */
    public Filer filer;

    /**
     * -encoding
     */
    public String encoding;

    public String classpath = System.getProperty("java.class.path");


    /**
     * -Xnocompile
     */
    public boolean nocompile;

    public enum Target {
        V2_0, V2_1, V2_2;

        /**
         * Returns true if this version is equal or later than the given one.
         */
        public boolean isLaterThan(Target t) {
            return this.ordinal() >= t.ordinal();
        }

        /**
         * Parses "2.0" and "2.1" into the {@link Target} object.
         *
         * @return null for parsing failure.
         */
        public static Target parse(String token) {
            if (token.equals("2.0"))
                return Target.V2_0;
            else if (token.equals("2.1"))
                return Target.V2_1;
            else if (token.equals("2.2"))
                return Target.V2_2;
            return null;
        }

        /**
         * Gives the String representation of the {@link Target}
         */
        public String getVersion(){
            switch(this){
            case V2_0:
                return "2.0";
            case V2_1:
                return "2.1";
            case V2_2:
                return "2.2";
            default:
                return null;
            }
        }

        public static Target getDefault() {
            return V2_2;
        }

        public static Target getLoadedAPIVersion() {
            return LOADED_API_VERSION;
        }
        
        private static final Target LOADED_API_VERSION;

        static {
            // check if we are indeed loading JAX-WS 2.2 API
            if (Invoker.checkIfLoading22API()) {
                LOADED_API_VERSION = Target.V2_2;
            } // check if we are indeed loading JAX-WS 2.1 API
            else if (Invoker.checkIfLoading21API()) {
                LOADED_API_VERSION = Target.V2_1;
            } else {
                LOADED_API_VERSION = Target.V2_0;
            }
        }
    }

    public Target target = Target.V2_2;

    /**
     * strictly follow the compatibility rules specified in JAXWS spec
     */
    public static final int STRICT = 1;

    /**
     * loosely follow the compatibility rules and allow the use of vendor
     * binding extensions
     */
    public static final int EXTENSION = 2;

    /**
     * this switch determines how carefully the compiler will follow
     * the compatibility rules in the spec. Either <code>STRICT</code>
     * or <code>EXTENSION</code>.
     */
    public int compatibilityMode = STRICT;

    public boolean isExtensionMode() {
        return compatibilityMode == EXTENSION;
    }

    /**
     * Target direcoty when producing files.
     */
    public File targetDir = new File(".");



    public boolean debug = false;

    /**
     * -Xdebug - gives complete stack trace
     */
    public boolean debugMode = false;


    private final List<File> generatedFiles = new ArrayList<File>();
    private ClassLoader classLoader;


    /**
     * Remember info on  generated source file generated so that it
     * can be removed later, if appropriate.
     */
    public void addGeneratedFile(File file) {
        generatedFiles.add(file);
    }

    /**
     * Remove generated files
     */
    public void removeGeneratedFiles(){
        for(File file : generatedFiles){
            if (file.getName().endsWith(".java")) {
                file.delete();
            }
        }
        generatedFiles.clear();        
    }

    /**
     * Return all the generated files and its types.
     */
    public Iterable<File> getGeneratedFiles() {
        return generatedFiles;
    }

    /**
     * Delete all the generated source files made during the execution
     * of this environment (those that have been registered with the
     * "addGeneratedFile" method).
     */
    public void deleteGeneratedFiles() {
        synchronized (generatedFiles) {
            for (File file : generatedFiles) {
                if (file.getName().endsWith(".java")) {
                    file.delete();
                }
            }
            generatedFiles.clear();
        }
    }

    /**
     * Parses arguments and fill fields of this object.
     *
     * @exception BadCommandLineException
     *      thrown when there's a problem in the command-line arguments
     */
    public void parseArguments( String[] args ) throws BadCommandLineException {

        for (int i = 0; i < args.length; i++) {
            if(args[i].length()==0)
                throw new BadCommandLineException();
            if (args[i].charAt(0) == '-') {
                int j = parseArguments(args,i);
                if(j==0)
                    throw new BadCommandLineException(WscompileMessages.WSCOMPILE_INVALID_OPTION(args[i]));
                i += (j-1);
            } else {
                addFile(args[i]);
            }
        }
        if(destDir == null)
            destDir = new File(".");
        if(sourceDir == null)
            sourceDir = destDir;
    }


    /**
     * Adds a file from the argume
     *
     * @param arg a file, could be a wsdl or xsd or a Class
     */
    protected void addFile(String arg) throws BadCommandLineException {}

    /**
     * Parses an option <code>args[i]</code> and return
     * the number of tokens consumed.
     *
     * @return
     *      0 if the argument is not understood. Returning 0
     *      will let the caller report an error.
     * @exception BadCommandLineException
     *      If the callee wants to provide a custom message for an error.
     */
    protected int parseArguments(String[] args, int i) throws BadCommandLineException {
        if (args[i].equals("-g")) {
            debug = true;
            return 1;
        } else if (args[i].equals("-Xdebug")) {
            debugMode = true;
            return 1;
        } else if (args[i].equals("-Xendorsed")) {
            // this option is processed much earlier, so just ignore.
            return 1;
        } else if (args[i].equals("-verbose")) {
            verbose = true;
            return 1;
        } else if (args[i].equals("-quiet")) {
            quiet = true;
            return 1;
        } else if (args[i].equals("-keep")) {
            keep = true;
            return 1;
        }  else if (args[i].equals("-target")) {
            String token = requireArgument("-target", args, ++i);
            target = Target.parse(token);
            if(target == null)
                throw new BadCommandLineException(WscompileMessages.WSIMPORT_ILLEGAL_TARGET_VERSION(token));
            return 2;
        } else if (args[i].equals("-classpath") || args[i].equals("-cp")) {
            classpath = requireArgument("-classpath", args, ++i) + File.pathSeparator + System.getProperty("java.class.path");
            return 2;
        } else if (args[i].equals("-d")) {
            destDir = new File(requireArgument("-d", args, ++i));
            if (!destDir.exists())
                throw new BadCommandLineException(WscompileMessages.WSCOMPILE_NO_SUCH_DIRECTORY(destDir.getPath()));
            return 2;
        } else if (args[i].equals("-s")) {
            sourceDir = new File(requireArgument("-s", args, ++i));
            keep = true;
            if (!sourceDir.exists()) {
                throw new BadCommandLineException(WscompileMessages.WSCOMPILE_NO_SUCH_DIRECTORY(sourceDir.getPath()));
            }
            return 2;
        } else if (args[i].equals("-extension")) {
            compatibilityMode = EXTENSION;
            return 1;
        } else if (args[i].startsWith("-help")) {
            WeAreDone done = new WeAreDone();
            done.initOptions(this);
            throw done;
        } else if (args[i].equals("-Xnocompile")) {
            // -nocompile implies -keep. this is undocumented switch.
            nocompile = true;
            keep = true;
            return 1;
        } else if (args[i].equals("-encoding")) {
            encoding = requireArgument("-encoding", args, ++i);
            try {
                if (!Charset.isSupported(encoding)) {
                    throw new BadCommandLineException(WscompileMessages.WSCOMPILE_UNSUPPORTED_ENCODING(encoding));
                }
            } catch (IllegalCharsetNameException icne) {
                throw new BadCommandLineException(WscompileMessages.WSCOMPILE_UNSUPPORTED_ENCODING(encoding));
            }
            return 2;
        }
        return 0;
    }

    /**
     * Obtains an operand and reports an error if it's not there.
     */
    public String requireArgument(String optionName, String[] args, int i) throws BadCommandLineException {
        //if (i == args.length || args[i].startsWith("-")) {
        if (args[i].startsWith("-")) {
            throw new BadCommandLineException(WscompileMessages.WSCOMPILE_MISSING_OPTION_ARGUMENT(optionName));
        }
        return args[i];
    }



    /**
     * Used to signal that we've finished processing.
     */
    public static final class WeAreDone extends BadCommandLineException {}

    /**
     * Get a URLClassLoader from using the classpath
     */
    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader =
                new URLClassLoader(pathToURLs(classpath),
                    this.getClass().getClassLoader());
        }
        return classLoader;
    }

    /**
     * Utility method for converting a search path string to an array
     * of directory and JAR file URLs.
     *
     * @param path the search path string
     * @return the resulting array of directory and JAR file URLs
     */
    public static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        URL[] urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            URL url = fileToURL(new File(st.nextToken()));
            if (url != null) {
                urls[count++] = url;
            }
        }
        if (urls.length != count) {
            URL[] tmp = new URL[count];
            System.arraycopy(urls, 0, tmp, 0, count);
            urls = tmp;
        }
        return urls;
    }

    /**
     * Returns the directory or JAR file URL corresponding to the specified
     * local file name.
     *
     * @param file the File object
     * @return the resulting directory or JAR file URL, or null if unknown
     */
    public static URL fileToURL(File file) {
        String name;
        try {
            name = file.getCanonicalPath();
        } catch (IOException e) {
            name = file.getAbsolutePath();
        }
        name = name.replace(File.separatorChar, '/');
        if (!name.startsWith("/")) {
            name = "/" + name;
        }

        // If the file does not exist, then assume that it's a directory
        if (!file.isFile()) {
            name = name + "/";
        }
        try {
            return new URL("file", "", name);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("file");
        }
    }

}
