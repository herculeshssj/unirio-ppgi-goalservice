/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package ie.deri.wsmx.core.codebase;

import ie.deri.wsmx.core.codebase.wsmx.Handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

/**
 * Classloader for a WSMX component archive (.wsmx). Supports loading embedded
 * libraries (jars inside jars) without extracting the containing jar. This is
 * achieved by loading the embedded jars into memory. <br>
 * The internal structure of a WSMX component archive looks like this:
 * 
 * Assemblyroot 
 *   -META-INF
 *     -manifest.mf
 *     -component.xml
 *   -classes
 *     -ie
 *       -deri
 *         -wsmx
 *           -...
 *   -lib
 *     -library.jar
 *     -tools.jar
 * 
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/codebase/ComponentClassLoader.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.17 $ $Date: 2005-10-13 15:50:28 $
 */ 
public class ComponentClassLoader extends ClassLoader implements ComponentClassLoaderMBean {
   
    public final static String JAVA_CLASS_PATH = "java.class.path";

    public final static String LIBRARY_DIRECTORY_NAME = "lib/";

    public final static String CLASSES_DIRECTORY_NAME = "classes/";

    public final static String CLASS = ".class";

    public final static String JAVA_PROTOCOL_HANDLER = "java.protocol.handler.pkgs";

    static Logger logger = Logger.getLogger(ComponentClassLoader.class);
	
	public final File codebase;
	public Manifest manifest = null;
	
	private int classLoadRequests = 0;
	private int resourceStreamRequests = 0;
	private int resourceURLRequests = 0;
	
  
	static {
        String handlerPackage = System.getProperty(JAVA_PROTOCOL_HANDLER);
        if (handlerPackage == null)
            handlerPackage = "";
        if (handlerPackage.length() > 0)
            handlerPackage = "|" + handlerPackage;
        handlerPackage = "ie.deri.wsmx.core.codebase" + handlerPackage;
        System.setProperty(JAVA_PROTOCOL_HANDLER, handlerPackage);
        logger.debug("JAVA_PROTOCOL_HANDLER: " + System.getProperty(JAVA_PROTOCOL_HANDLER));
    }

    protected Map<String, ByteCode> byteCode = new HashMap<String, ByteCode>();

    protected Map<String, ProtectionDomain> pdCache = Collections
            .synchronizedMap(new HashMap<String, ProtectionDomain>());


    protected boolean delegateToParent;

    /**
     * Datastructure that holds the bytecode of a class or
     * resource along with complimentary information.
     */
    protected class ByteCode {
        public byte bytes[];
        public String name, original, codebase;
        
        public ByteCode(String name, String original, byte bytes[],
                String codebase) {
            this.name = name;
            this.original = original;
            this.bytes = bytes;
            this.codebase = codebase;
        }
    }

    /**
     * Create a non-delegating classloader.
     * @throws CodebaseUnloadableException 
     */
    public ComponentClassLoader(String systemcodebase,
            String componentcodebase) throws CodebaseUnloadableException {
        this(systemcodebase, componentcodebase, null);
    }
	
    /**
     * Create a non-delegating classloader.
     * @throws CodebaseUnloadableException 
     */
    public ComponentClassLoader(String codebase) throws CodebaseUnloadableException {
        this(codebase, (ClassLoader)null);
    }

    /**
     * Create a delegating classloader.
     * 
     * @param systemcodebase
     * @param componentcodebase
     * @param parent
     * @throws CodebaseUnloadableException 
     */
    public ComponentClassLoader(String systemcodebase,
            String componentcodebase, ClassLoader parent) throws CodebaseUnloadableException {
    	this(concat(systemcodebase, componentcodebase), parent);
    }
	
    /**
     * Create a delegating classloader from a codebase given as a <code>String</code>.
     * 
     * @param codebase
     * @param parent
     * @throws CodebaseUnloadableException 
     */
    public ComponentClassLoader(String codebase, ClassLoader parent) throws CodebaseUnloadableException {
        this(new File(codebase), parent);
    }
    
    /**
     * Create a delegating classloader from a codebase given as a <code>File</code>.
     * 
     * @param codebase
     * @param parent
     * @throws CodebaseUnloadableException 
     */
    public ComponentClassLoader(File codebase, ClassLoader parent) throws CodebaseUnloadableException {
        super(parent);
        if (parent != null)
            delegateToParent = true;
        else
            delegateToParent = false;
        if (codebase == null) {
            logger.warn("Path to codebase is null.");
            throw new CodebaseUnloadableException("");
        }
        if (!codebase.canRead()) {
            logger.warn("Codebase not readable.");
            throw new CodebaseUnloadableException("Codebase not readable.");
        }        
        this.codebase = codebase;
        try {
            JarFile jarFile = new JarFile(codebase);	
			loadWSMXArchive(jarFile);
        } catch (IOException ioe) {
            logger.warn("Unable to load WSMX component " + codebase +": " + ioe);
            throw new CodebaseUnloadableException("Unable to load WSMX component " + codebase + ".", ioe);
        }
    }

    /**
     * Loads the component bytecode from /classes, opens jars in /lib and loads
     * the bytecode of classes within these doubly jarred archives as well.
     * 
     * @param jar
     *            the archive to load from
     * @throws IOException
     *             if the jar can't deliver inputstreams
     */
    private void loadWSMXArchive(JarFile jar) throws IOException {
        logger.debug("Loading WSMX archive " + jar.getName());		
		manifest = jar.getManifest();
		Enumeration enumeration = jar.entries();
        // iterate throug all entries in our .wsmx archive
        while (enumeration.hasMoreElements()) {
            JarEntry entry = (JarEntry) enumeration.nextElement();
            if (entry.isDirectory())
                continue;
            String entryName = entry.getName();
            if (entryName.startsWith(LIBRARY_DIRECTORY_NAME)) {
                logger.debug("Loading library " + entryName);
                InputStream is = jar.getInputStream(entry);
                if (is == null)
                    throw new IOException("Unable to load library " + entryName);
                loadEmbeddedJar(is, entryName);
            } else if (entryName.startsWith(CLASSES_DIRECTORY_NAME)
                    && entryName.endsWith(CLASS)) {
                logger.debug("Loading component class: " + entryName);
                InputStream is = jar.getInputStream(entry);
                if (is == null)
                    throw new IOException("Unable to load component class " + entryName);
                loadByteCode(entry, is, jar.getName());
            } else if (entryName.startsWith("META-INF/")) {
	                logger.debug("Loading meta resource: " + entryName);
	                InputStream is = jar.getInputStream(entry);
	                if (is == null)
	                    throw new IOException("Unable to meta resource " + entryName);
	                loadByteCode(entry, is, jar.getName());				
			} else if (entryName.endsWith(CLASS)) {
                loadByteCode(entry, jar.getInputStream(entry), "/");
			}
        }
        return;
    }

    /**
     * Loads embedded jars, like for example libraries, by
     * iterating through its entries and loading the bytecodes
     * of encountered classes and resources into memory.
     * 
     * @param is
     * @param jar
     * @throws IOException
     */
    protected void loadEmbeddedJar(InputStream is, String jar)
            throws IOException {
        JarInputStream jis = new JarInputStream(is);
        JarEntry entry = null;

        while ((entry = jis.getNextJarEntry()) != null) {
            if (entry.isDirectory())
                continue;
            loadByteCode(entry, jis, jar);
        }
    }

    /**
     * Loads the bytecode of classes and resources into memory.
     * Classnames must be unique per classloader, so duplicate
     * classes are not loaded. To be able to load resources with
     * the same name from different embedded jars, we prefix
     * the resourcename with the path of the jar entry of the
     * containing jar, in addition to giving it the resourcename.
     * In simple cases we're left with two redundant options to refer to the
     * same resource, but if a conflict should appear we can resolve it.
     * 
     * @param entry
     * @param is
     * @param classRoot
     * @throws IOException
     */
    protected void loadByteCode(JarEntry entry, InputStream is, String classRoot)
            throws IOException {
        String entryName = entry.getName().replace('/', '.');
        String fileExtension = null;
        int index = 0;
        if ((index = entryName.lastIndexOf('.')) != -1) {
            fileExtension = entryName.substring(index);
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();       
        byte[] buf = new byte[1024];
        while (true) {
            int len = is.read(buf);
            if (len < 0)
                break;
            baos.write(buf, 0, len);
        }
        byte[] bytes = baos.toByteArray();
        
        if (fileExtension != null && fileExtension.equals(".class")) {
            String className = new String(entryName);
            if (entryName.startsWith("classes"))
                className = className.substring(8);
            logger.debug("Loading class " + className);
            if (isLoaded(className, classRoot, bytes))
                return;
            byteCode.put(className, new ByteCode(className, entry.getName(),
                    bytes, classRoot)); 
        } else {
            String localName = classRoot + "/" + entryName;
            logger.debug("Loading resource " + localName);
            byteCode.put(localName, new ByteCode(localName, entry.getName(),
                    bytes, classRoot));
            logger.debug("Loading resource " + entryName);
            if (isLoaded(entryName, classRoot, bytes))
                return;
            byteCode.put(entryName, new ByteCode(entryName, entry.getName(),
                    bytes, classRoot));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    @Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
    	classLoadRequests++;
		logger.debug("Finding class: " + name);
		Class cls = findLoadedClass(name);
        if (cls != null)
            return cls;

        String cache = name.replace('/', '.') + ".class";
        logger.debug("Cache name: " + name);
        ByteCode bytecode = byteCode.get(cache);
        if (bytecode != null) {

            ProtectionDomain pd = pdCache.get(bytecode.codebase);
            if (pd == null) {
                ProtectionDomain cd = ComponentClassLoader.class
                        .getProtectionDomain();
                URL url = cd.getCodeSource().getLocation();
                try {
                    url = new URL("jar:" + url + "!/" + bytecode.codebase);
                } catch (MalformedURLException mux) {
                    logger.fatal("ProtectionDomain:", mux);
                }

                CodeSource source = new CodeSource(url, (Certificate[]) null);
                pd = new ProtectionDomain(source, null, this, null);
                pdCache.put(bytecode.codebase, pd);
            }

            byte bytes[] = bytecode.bytes;
            logger.debug("Found class " + name);
            return defineClass(name, bytes, pd);
        }
        logger.debug("Failed to find class " + name);
        throw new ClassNotFoundException(name);
    }

    /**
     * Converts an array of bytes into an instance of a class.
     * 
     * @param name
     * @param bytes
     * @param pd
     * @return
     * @throws ClassFormatError
     */
    protected Class<?> defineClass(String name, byte[] bytes, ProtectionDomain pd)
            throws ClassFormatError {
        return defineClass(name, bytes, 0, bytes.length, pd);
    }

    /**
     * Opens a stream on a resource.
     * 
     * @param resource
     * @return
     */
    public InputStream getByteStream(String resource) {
        InputStream stream = null;
        ByteCode bytecode = byteCode.get(resource);
        if (bytecode == null)
            bytecode = byteCode.get(resolve(resource));
        if (bytecode != null)
            stream = new ByteArrayInputStream(bytecode.bytes);
        if (stream == null && delegateToParent)
            stream = (getParent()).getResourceAsStream(resource);
        logger.debug("Getting byte stream from " + resource + ": " + stream);
        return stream;
    }

    /* (non-Javadoc)
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    @Override
	public InputStream getResourceAsStream(String name) {
    	resourceStreamRequests++;
        return getByteStream(name);
    }
    /**
     * For a given resource name this method tries to locate
     * this resource. By looking at the current stack-frame
     * we can see who requested the resource, and if the
     * requester was loaded out of an embedded jar, we look
     * inside that jar for the resource, otherwise we try to
     * find it in the containing jar.
     * 
     * @param resource
     * @return
     */
    protected String resolve(String resource) {

        if (resource.startsWith("/"))
            resource = resource.substring(1);
        resource = resource.replace('/', '.');
        String resolvedResource = null;
        String caller = getCaller();
        ByteCode callerCode = byteCode.get(caller + ".class");

        if (callerCode != null) {
            String candidate = callerCode.codebase + "/" + resource;
            if (byteCode.get(candidate) != null) {
                resolvedResource = candidate;
            }
        }
        if (resolvedResource == null) {
            if (byteCode.get(resource) != null)
                resolvedResource = resource;
        }
        return resolvedResource;
    }

    /**
     * Checks if a class or resource has already been loaded previously.
     * Since we are dealing with embedded jars, it's possibly that resources
     * have not been loaded before but that they are shadowed by resources
     * with the same name from different archives and wich different bytecodes.
     * 
     * @param name
     * @param jar
     * @param bytes
     * @return
     */
    protected boolean isLoaded(String name, String jar, byte[] bytes) {
        ByteCode namesake = byteCode.get(name);
        if (namesake != null) {
            if (!Arrays.equals(namesake.bytes, bytes) && !name.startsWith("/META-INF")) {
                logger.warn(namesake.name + " from " + jar + " is shadowed by "
                        + namesake.codebase + " and has different bytecode.");
            } else {
                logger.debug(namesake.name + " from " + jar + " is shadowed by "
                        + namesake.codebase + " and has idendical bytecode.");
            }
            return true;
        }
        return false;
    }

    /**
     * Takes the current stack-frame and searches bottom-up 
     * for a class that has previously been loaded by this
     * classloader.
     * 
     * @return
     */
    protected String getCaller() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String caller = null;
        for (int i = 0; i < stack.length; i++) {
            if (byteCode.get(stack[i].getClassName() + ".class") != null) {
                caller = stack[i].getClassName();
                break;
            }
        }
        return caller;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    @Override
	protected URL findResource(String resource) {
        try {
            String resolvedResource = resolve(resource);
            if (resolvedResource != null) {
                return new URL(Handler.PROTOCOL + ":" + codebase + "!" + resolvedResource);
            }
            return null;
        } catch (MalformedURLException mue) {
            logger.warn("Unable to locate resource: " + mue);
        }
        return null;
    }
	
	


    private static final char SYSTEM_SEPARATOR = File.separatorChar;
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char OTHER_SEPARATOR;

    static {
        if (SYSTEM_SEPARATOR == WINDOWS_SEPARATOR) {
            OTHER_SEPARATOR = UNIX_SEPARATOR;
        } else {
            OTHER_SEPARATOR = WINDOWS_SEPARATOR;
        }
    }

    public static String concat(String basePath, String fullFilenameToAdd) {
        int prefix = getPrefixLength(fullFilenameToAdd);
        if (prefix < 0) {
            return null;
        }
        if (prefix > 0) {
            return normalize(fullFilenameToAdd);
        }
        if (basePath == null) {
            return null;
        }
        int len = basePath.length();
        if (len == 0) {
            return normalize(fullFilenameToAdd);
        }
        if (isSeparator(basePath.charAt(len - 1))) {
            return normalize(basePath + fullFilenameToAdd);
        }
		return normalize(basePath + '/' + fullFilenameToAdd);
    }

    private static boolean isSeparator(char ch) {
        return (ch == UNIX_SEPARATOR) || (ch == WINDOWS_SEPARATOR);
    }

    public static String normalize(String filename) {
        if (filename == null) {
            return null;
        }
        int size = filename.length();
        if (size == 0) {
            return filename;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }

        char[] array = new char[size + 2]; // +1 for possible extra slash, +2
                                            // for arraycopy
        filename.getChars(0, filename.length(), array, 0);

        // fix separators throughout
        for (int i = 0; i < array.length; i++) {
            if (array[i] == OTHER_SEPARATOR) {
                array[i] = SYSTEM_SEPARATOR;
            }
        }
        if (isSeparator(array[size - 1]) == false) {
            array[size++] = SYSTEM_SEPARATOR;
        }

        // adjoining slashes
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == SYSTEM_SEPARATOR
                    && array[i - 1] == SYSTEM_SEPARATOR) {
                System.arraycopy(array, i, array, i - 1, size - i);
                size--;
                i--;
            }
        }
        // dot slash
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == '.'
                    && (i == prefix + 1 || array[i - 2] == SYSTEM_SEPARATOR)) {
                System.arraycopy(array, i + 1, array, i - 1, size - i);
                size -= 2;
                i--;
            }
        }
        // double dot slash
        outer: for (int i = prefix + 2; i < size; i++) {
            if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == '.'
                    && array[i - 2] == '.'
                    && (i == prefix + 2 || array[i - 3] == SYSTEM_SEPARATOR)) {
                if (i == prefix + 2) {
                    return null;
                }
                int j;
                for (j = i - 4; j >= prefix; j--) {
                    if (array[j] == SYSTEM_SEPARATOR) {
                        System.arraycopy(array, i + 1, array, j + 1, size - i);
                        size -= (i - j);
                        i = j + 1;
                        continue outer;
                    }
                }
                System.arraycopy(array, i + 1, array, prefix, size - i);
                size -= (i + 1 - prefix);
                i = prefix + 1;
            }
        }

        if (size <= 0) { // should never be less than 0
            return "";
        }
        if (size <= prefix) { // should never be less than prefix
            return new String(array, 0, size);
        }
        return new String(array, 0, size - 1);
    }

    public static int getPrefixLength(String filename) {
        if (filename == null) {
            return -1;
        }
        int len = filename.length();
        if (len == 0) {
            return 0;
        }
        char ch0 = filename.charAt(0);
        if (ch0 == ':') {
            return -1;
        }
        if (len == 1) {
            if (ch0 == '~') {
                return -1;
            }
            return (isSeparator(ch0) ? 1 : 0);
        }
		if (ch0 == '~') {
		    int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
		    int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
		    if ((posUnix == -1) && (posWin == -1)) {
		        return -1;
		    }
		    posUnix = posUnix == -1 ? posWin : posUnix;
		    posWin = posWin == -1 ? posUnix : posWin;
		    return Math.min(posUnix, posWin) + 1;
		}
		char ch1 = filename.charAt(1);
		if (ch1 == ':') {
		    ch0 = Character.toUpperCase(ch0);
		    if ((ch0 < 'A') || (ch0 > 'Z') || (len == 2)
		            || (isSeparator(filename.charAt(2)) == false)) {
		        return -1;
		    }
		    return 3;

		} else if (isSeparator(ch0) && isSeparator(ch1)) {
		    int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
		    int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
		    if ((posUnix == -1) && (posWin == -1) || (posUnix == 2)
		            || (posWin == 2)) {
		        return -1;
		    }
		    posUnix = posUnix == -1 ? posWin : posUnix;
		    posWin = posWin == -1 ? posUnix : posWin;
		    return Math.min(posUnix, posWin) + 1;
		} else {
		    return isSeparator(ch0) ? 1 : 0;
		}
    }

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#getResource(java.lang.String)
	 */
	@Override
	public URL getResource(String name) {
		resourceURLRequests++;
		//TODO consider embedded jars
		return findResource(name);
	}

	/**
	 * @return Returns the codebase.
	 */
	public File getCodebase() {
		return codebase;
	}

	/**
	 * @return Returns the manifest of the containing jar.
	 * Manifests of embedded have to be retrieved manually.
	 */
	public Manifest getManifest() {
		return manifest;
	}

	public int getClassLoadRequests() {
		return classLoadRequests;
	}

	public int getResourceStreamRequests() {
		return resourceStreamRequests;
	}

	public int getResourceURLRequests() {
		return resourceURLRequests;
	}
	
	public int getNumberOfByteCodes() {
		return byteCode.size();
	}
	
	
}
