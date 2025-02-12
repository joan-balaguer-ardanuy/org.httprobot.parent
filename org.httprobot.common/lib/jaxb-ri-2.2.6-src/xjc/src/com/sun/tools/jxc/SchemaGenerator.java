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

package com.sun.tools.jxc;

import com.sun.tools.jxc.ap.Options;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.xml.bind.util.Which;

import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.OptionChecker;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.bind.JAXBContext;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLI entry-point to the schema generator.
 *
 * @author Bhakti Mehta
 */
public class SchemaGenerator {
    /**
     * Runs the schema generator.
     */
    public static void main(String[] args) throws Exception {
        System.exit(run(args));
    }

    public static int run(String[] args) throws Exception {
        try {
            ClassLoader cl = SecureLoader.getClassClassLoader(SchemaGenerator.class);
            if (cl==null) {
                cl = SecureLoader.getSystemClassLoader();
            }
            return run(args, cl);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }

    /**
     * Runs the schema generator.
     *
     * @param classLoader
     *      the schema generator will run in this classLoader.
     *      It needs to be able to load annotation processing and JAXB RI classes. Note that
     *      JAXB RI classes refer to annotation processing classes. Must not be null.
     *
     * @return
     *      exit code. 0 if success.
     *
     */
    public static int run(String[] args, ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Options options = new Options();
        if (args.length ==0) {
            usage();
            return -1;
        }
        for (String arg : args) {
            if (arg.equals("-help")) {
                usage();
                return -1;
            }

            if (arg.equals("-version")) {
                System.out.println(Messages.VERSION.format());
                return -1;
            }

            if (arg.equals("-fullversion")) {
                System.out.println(Messages.FULLVERSION.format());
                return -1;
            }

        }

        try {
            options.parseArguments(args);
        } catch (BadCommandLineException e) {
            // there was an error in the command line.
            // print usage and abort.
            System.out.println(e.getMessage());
            System.out.println();
            usage();
            return -1;
        }

        Class schemagenRunner = classLoader.loadClass(Runner.class.getName());
        Method compileMethod = schemagenRunner.getDeclaredMethod("compile",String[].class,File.class);

        List<String> aptargs = new ArrayList<String>();

        if (options.encoding != null) {
            aptargs.add("-encoding");
            aptargs.add(options.encoding);
        }

        // make jaxb-api.jar visible to classpath
        File jaxbApi = findJaxbApiJar();
        if(jaxbApi!=null) {
            if(options.classpath!=null) {
                options.classpath = options.classpath+File.pathSeparatorChar+jaxbApi;
            } else {
                options.classpath = jaxbApi.getPath();
            }
        }

        aptargs.add("-cp");
        aptargs.add(options.classpath);

        if(options.targetDir!=null) {
            aptargs.add("-d");
            aptargs.add(options.targetDir.getPath());
        }

        aptargs.addAll(options.arguments);

        String[] argsarray = aptargs.toArray(new String[aptargs.size()]);
        return ((Boolean) compileMethod.invoke(null, argsarray, options.episodeFile)) ? 0 : 1;
    }

    /**
     * Computes the file system path of <tt>jaxb-api.jar</tt> so that
     * Annotation Processing will see them in the <tt>-cp</tt> option.
     *
     * <p>
     * In Java, you can't do this reliably (for that matter there's no guarantee
     * that such a jar file exists, such as in Glassfish), so we do the best we can.
     *
     * @return
     *      null if failed to locate it.
     */
    private static File findJaxbApiJar() {
        String url = Which.which(JAXBContext.class);
        if(url==null)       return null;    // impossible, but hey, let's be defensive

        if(!url.startsWith("jar:") || url.lastIndexOf('!')==-1)
            // no jar file
            return null;

        String jarFileUrl = url.substring(4,url.lastIndexOf('!'));
        if(!jarFileUrl.startsWith("file:"))
            return null;    // not from file system

        try {
            File f = new File(new URL(jarFileUrl).toURI());
            if (f.exists() && f.getName().endsWith(".jar")) { // see 6510966
                return f;
            }
            f = new File(new URL(jarFileUrl).getFile());
            if (f.exists() && f.getName().endsWith(".jar")) { // this is here for potential backw. compatibility issues
                return f;
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(SchemaGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SchemaGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static void usage( ) {
        System.out.println(Messages.USAGE.format());
    }

    public static final class Runner {
        public static boolean compile(String[] args, File episode) throws Exception {

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            JavacOptions options = JavacOptions.parse(compiler, fileManager, args);
            List<String> unrecognizedOptions = options.getUnrecognizedOptions();
            if (!unrecognizedOptions.isEmpty())
                Logger.getLogger(SchemaGenerator.class.getName()).log(Level.WARNING, "Unrecognized options found: {0}", unrecognizedOptions);
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(options.getFiles());
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options.getRecognizedOptions(),
                    options.getClassNames(),
                    compilationUnits);
            com.sun.tools.jxc.ap.SchemaGenerator r = new com.sun.tools.jxc.ap.SchemaGenerator();
            if (episode != null)
                r.setEpisodeFile(episode);
            task.setProcessors(Collections.singleton(r));
            return task.call();
        }
    }

    /**
          *  @author Peter von der Ahe
          */
    private static final class JavacOptions {
        private final List<String> recognizedOptions;
        private final List<String> classNames;
        private final List<File> files;
        private final List<String> unrecognizedOptions;

        private JavacOptions(List<String> recognizedOptions, List<String> classNames, List<File> files,
                             List<String> unrecognizedOptions) {
            this.recognizedOptions = recognizedOptions;
            this.classNames = classNames;
            this.files = files;
            this.unrecognizedOptions = unrecognizedOptions;
        }

        public static JavacOptions parse(OptionChecker primary, OptionChecker secondary, String... arguments) {
            List<String> recognizedOptions = new ArrayList<String>();
            List<String> unrecognizedOptions = new ArrayList<String>();
            List<String> classNames = new ArrayList<String>();
            List<File> files = new ArrayList<File>();
            for (int i = 0; i < arguments.length; i++) {
                String argument = arguments[i];
                int optionCount = primary.isSupportedOption(argument);
                if (optionCount < 0) {
                    optionCount = secondary.isSupportedOption(argument);
                }
                if (optionCount < 0) {
                    File file = new File(argument);
                    if (file.exists())
                        files.add(file);
                    else if (SourceVersion.isName(argument))
                        classNames.add(argument);
                    else
                        unrecognizedOptions.add(argument);
                } else {
                    for (int j = 0; j < optionCount + 1; j++) {
                        int index = i + j;
                        if (index == arguments.length) throw new IllegalArgumentException(argument);
                        recognizedOptions.add(arguments[index]);
                    }
                    i += optionCount;
                }
            }
            return new JavacOptions(recognizedOptions, classNames, files, unrecognizedOptions);
        }

        /**
                     * Returns the list of recognized options and their arguments.
                     *
                     * @return a list of options
                     */
        public List<String> getRecognizedOptions() {
            return Collections.unmodifiableList(recognizedOptions);
        }

        /**
                     * Returns the list of file names.
                     *
                     * @return a list of file names
                     */
        public List<File> getFiles() {
            return Collections.unmodifiableList(files);
        }

        /**
                     * Returns the list of class names.
                     *
                     * @return a list of class names
                     */
        public List<String> getClassNames() {
            return Collections.unmodifiableList(classNames);
        }

        /**
                     * Returns the list of unrecognized options.
                     *
                     * @return a list of unrecognized options
                     */
        public List<String> getUnrecognizedOptions() {
            return Collections.unmodifiableList(unrecognizedOptions);
        }

        @Override
        public String toString() {
            return String.format("recognizedOptions = %s; classNames = %s; " + "files = %s; unrecognizedOptions = %s", recognizedOptions, classNames, files, unrecognizedOptions);
        }
    }
}
