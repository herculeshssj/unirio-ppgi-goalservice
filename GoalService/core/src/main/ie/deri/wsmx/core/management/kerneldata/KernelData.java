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

package ie.deri.wsmx.core.management.kerneldata;

import ie.deri.wsmx.exceptions.KernelDataException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;


/**
 * This class is a facade for the kerneldata package and provides information about the
 * current state of the microkernel, structured as XML documents. For this packages clients
 * flexibility and convenience the backend permits instance as well as static use, wich is
 * also reflected in the facade.
 * <p>
 * The static methods of this class can be used without having to hand down the
 * object reference to every class that needs kernel data, but of coarse it then does
 * need a reference to the <code>MBeanServer</code> it wants the kerneldata from. If
 * the client class has neither a <code>KernelData</code> nor an <code>MBeanServer</code>
 * reference this class can still provide kernel data, but it will query an arbitrarily selected
 * <code>MBeanServer</code> of all <code>MBeanServers</code> registered with the JVM the
 * <code>KernelData</code> instance lives in.
 * <p>
 * If the client class wants to query kernel data from multiple <code>MBeanServers</code>
 * registered within the same JVM, it either has to provide the <code>MBeanServer</code>
 * reference with every call to the static method or it can create one <code>KernelData</code>
 * instance per <code>MBeanServer</code> and prime it with the <code>MBeanServer</code> reference.
 * Subsequently this allows to invoke instance methods on the <code>KernelData</code> objects without
 * the need to know the <code>MBeanServer</code> reference.
 * <p>
 * In order to ensure easy extensibility this class uses Reflection to locate the requested view
 * and invoke the neccessary methods on it, allowing the facade to remain unchanged
 * when new views are added, because it discovers these views at runtime.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/kerneldata/KernelData.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.6 $ $Date: 2005-07-01 16:45:19 $
 */ 
public class KernelData {

	private static DocumentBuilder staticbuilder;
	private static MBeanServer staticMBeanServer;
	static Logger logger = Logger.getLogger(KernelData.class);
	
	private DocumentBuilder instanceBuilder;
	private MBeanServer instanceMBeanServer;
	
	Map<String, Viewable> instanceCache;
	
	static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
        	staticbuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e)
        {
            logger.fatal("Failed to create DocumentBuilder.", e);
        }		
	}

	public KernelData() {
		super();
		instanceCache = new HashMap<String, Viewable>();
	}
	
	public KernelData(MBeanServer mBeanServer) {
		super();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		instanceCache = new HashMap<String, Viewable>();
		instanceMBeanServer = mBeanServer;
        try {
        	staticbuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e)
        {
            logger.fatal("Failed to create DocumentBuilder.", e);
        }		
	}

	public KernelData(MBeanServer mBeanServer, DocumentBuilder builder) {
		super();
		instanceCache = new HashMap<String, Viewable>();
		instanceBuilder = builder;
		instanceMBeanServer = mBeanServer;
	}
	

	/** 
	 * Provides information about the current state of the microkernel, structured as XML documents.
	 * Uses the <code>MBeanServer</code> reference given to the <code>Kerneldata</code> constructor.
	 * If the null constructor was used this method will query an arbitrarily selected 
	 * <code>MBeanServer</code> from all <code>MBeanServers</code> registered with the JVM.
	 * Once a view has been requested, the it's builder instance will be cached.
	 * 
     * <p>
     * Possible keys include:
     * <table summary="Shows keys and associated views">
     * <tr>
     * <th>Key</th>
     * <th>Description of Associated Value</th>
     * </tr>
     * <tr>
     * <td><code>wsmx.core.kernel</code></td>
     * <td>Pulls together the server, domains and mbeans views.</td>
     * </tr>
     * <tr>
     * <td><code>wsmx.core.server</code></td>
     * <td>Provides information about the server.</td>
     * </tr>
     * <tr>
     * <td><code>wsmx.core.domains</code></td>
     * <td>Provides information about which domains exist and which MBeans live in which domain.</td>
     * </tr>
     * <tr>
     * <td><code>wsmx.core.mbeans</code></td>
     * <td>Provides detailed information about a certain MBean. Use <code>args</code> to specify the MBean ObjectName.</td>
     * </tr>
     * </table>
     * 
     * @param key
     * 			the key of the requested view
	 * @param args
	 * 			arbitrary number of arguments that are passed to the builder of the view
	 * 
     * @return the requested view
     */
	public Document getViewForName(String key, String ... args) throws KernelDataException {
	    Document rv = null;
        try {
		    if (instanceCache.containsKey(key))
	            return instanceCache.get(key).getView(args);
	        Constructor constructor = Class.forName(keyToClass(key)).getConstructor(new Class[]{MBeanServer.class, DocumentBuilder.class});
	        Viewable viewable = (Viewable)constructor.newInstance(new Object[]{instanceMBeanServer, instanceBuilder});
        
	        instanceCache.put(key, viewable);
	        rv = viewable.getView(args);
	        
	    } catch (InstantiationException ie) {
            String msg = "Reflection operation: Failed to instantiate class.";
	        logger.fatal(msg, ie);
            throw new KernelDataException(msg, ie);
	    } catch (NoSuchMethodException nsme) {
	        String msg = "Reflection operation: Failed to invoke method.";
	        logger.fatal(msg, nsme);
	        throw new KernelDataException(msg, nsme);
	    } catch (InvocationTargetException ite) {
	        String msg = "Reflection operation: Failure during method invocation.";
	        logger.fatal(msg, ite);	
	        throw new KernelDataException(msg, ite);
	    } catch (JMException jme) {
	        String msg = "Java Management Extensions.";
	        logger.fatal(msg, jme);
	        throw new KernelDataException(msg, jme);
	    } catch (ClassNotFoundException cnfe) {
	        String msg = "Reflection operation: Class not found.";
	        logger.fatal(msg, cnfe);	
	        throw new KernelDataException(msg, cnfe);
	    } catch (IllegalAccessException iae) {
	        String msg = "Reflection operation: Illegal Access.";
	        logger.fatal(msg, iae);
	        throw new KernelDataException(msg, iae);
	    } catch (ParserConfigurationException pce) {
	        String msg = "Could not create DocumentBuilder.";
	        logger.fatal(msg, pce);
	        throw new KernelDataException(msg, pce);
        }	    
	    
	    return rv;
	    
	}
	
	/**
	 * Convenience method. By default it will query an arbitrarily selected
	 * <code>MBeanServer</code> of all <code>MBeanServers</code> 
	 * registered with the JVM the <code>KernelData</code> instance
	 * lives in. If the client {@link #setMBeanServer(MBeanServer) primed
	 * the static MBeansServer reference}, then that <code>MBeanServer</code>
	 * will be used. Note that this technique works only if queries
	 * are limited to a single <code>MBeanServer</code> per JVM at any given
	 * time slice. If there are multiple <code>MBeanServers</code> that have
	 * to be dealt with and maintaining lists of references is not acceptable
	 * you have to use instance methods.
	 * 
     * @param key
     * 			the key of the requested view
	 * @param args
	 * 			arbitrary number of arguments that are passed to the builder of the view
	 * 
     * @return the requested view
	 * @throws KernelDataException
	 */
	public static Document getViewForNameStatically(String key, String ... args) throws KernelDataException {
	    return getViewForNameStatically(key, staticMBeanServer, args);     
	}
	

	/**
	 * Requests a view from a given <code>MBeanServer</code>. If the reference to it
	 * is null, this method will fallback to an arbitrarily selected
	 * <code>MBeanServer</code> of all <code>MBeanServers</code> 
	 * registered with the JVM.
	 * 
     * @param key
     * 			the key of the requested view
	 * @param mBeanServer
	 * 			the <code>MBeanServer</code> that will be queried
	 * @param args
	 * 			arbitrary number of arguments that are passed to the builder of the view
	 * 
     * @return the requested view
	 * @throws KernelDataException
	 */
	public static Document getViewForNameStatically(String key, MBeanServer mBeanServer, String ... args) throws KernelDataException {
	    if (key == null) 
	        throw new NullPointerException("Key may not be null.");
	    Document rv = null;    
	    try {
	        Constructor constructor = Class.forName(keyToClass(key)).getConstructor(new Class[]{MBeanServer.class, DocumentBuilder.class});
	        Viewable viewable = (Viewable)constructor.newInstance(new Object[]{mBeanServer, staticbuilder});
        
	        rv = viewable.getView(args);	
	        
	    } catch (InstantiationException ie) {
            String msg = "Reflection operation: Failed to instantiate class.";
	        logger.fatal(msg, ie);
            throw new KernelDataException(msg, ie);
	    } catch (NoSuchMethodException nsme) {
	        String msg = "Reflection operation: Failed to invoke method.";
	        logger.fatal(msg, nsme);
	        throw new KernelDataException(msg, nsme);
	    } catch (InvocationTargetException ite) {
	        String msg = "Reflection operation: Failure during method invocation.";
	        logger.fatal(msg, ite);	
	        throw new KernelDataException(msg, ite);
	    } catch (JMException jme) {
	        String msg = "Java Management Extensions Exception.";
	        logger.fatal(msg, jme);
	        throw new KernelDataException(msg, jme);
	    } catch (ClassNotFoundException cnfe) {
	        String msg = "Reflection operation: Class not found.";
	        logger.fatal(msg, cnfe);	
	        throw new KernelDataException(msg, cnfe);
	    } catch (IllegalAccessException iae) {
	        String msg = "Reflection operation: Illegal Access.";
	        logger.fatal(msg, iae);
	        throw new KernelDataException(msg, iae);
	    } catch (ParserConfigurationException pce) {
	        String msg = "Could not create DocumentBuilder.";
	        logger.fatal(msg, pce);
	        throw new KernelDataException(msg, pce);
        }	    
	    
	    return rv;	    
	}
	
	//TODO handle non-core cases, the hooks are there: m.group(2)
	private static String keyToClass(String key) throws KernelDataException {
        Matcher m = Pattern.compile("(wsmx\\.)(\\w*\\.)(\\w*)").matcher(key);
        if(!m.matches()) {
            String msg = "Key is not valid.";
        	logger.warn(msg);
        	throw new KernelDataException(msg);
        }
        key = m.group(3);
        //capitalize
	    char[] chars = key.toCharArray();
	    chars[0]=Character.toUpperCase(chars[0]);
	    String name = new String(chars);
	    return "ie.deri.wsmx.core.management.kerneldata." + name + "View";
	}
	
	/**
	 * This method allows clients of the <code>KernelData</code> class to
	 * set a static <code>MBeanServer</code> reference,
	 * which is used for {@link #getViewForNameStatically(String, String[]) getting views statically}.
	 * This reference is <code>null</code> by default which
	 * means that this class will try to locate an <code>MBeanServer</code>
	 * running in the same JVM and select one arbitrarily if multiples are
	 * found. If the reference is set to an object that implements <code>MBeanServer</code>,
	 * then that instance will be queried.
	 * <p>
	 * Note that this method is a convenience method that is only usefull clients,
	 * who use the static methods exclusively <i>and</i> who operate in an environment with
	 * multiple <code>MBeanServers</code>. It avoids passing <code>MBeanServer</code>
	 * references with every single invocation. If the environment houses only a single
	 * <code>MBeanServer</code>, the default <code>null</code> reference is sufficient.
	 * 
	 * @param mBeanServer the <code>MBeanServer</code> to set
	 */
	public synchronized static void setMBeanServer (MBeanServer mBeanServer) {
		staticMBeanServer = mBeanServer;
	}	
	
}
