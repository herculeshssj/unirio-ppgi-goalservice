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

package ie.deri.wsmx.core.configuration.descriptor;

import ie.deri.wsmx.core.WSMXKernel;
import ie.deri.wsmx.core.codebase.CodebaseUnloadableException;
import ie.deri.wsmx.core.codebase.ComponentClassLoader;
import ie.deri.wsmx.core.configuration.ComponentConfiguration;
import ie.deri.wsmx.exceptions.WSMXConfigurationException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;


/**
 * XMLConfigurationFile for the component configuration format.
 * Loads configuration from <code>Files</code> and <code>URLs</code>
 * with explicit or implicit schema.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/descriptor/ComponentXMLConfigurationFile.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-21 16:28:56 $
 */ 
public class ComponentXMLConfigurationFile {
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	private static final String DEFAULT_SCHEMA = "/META-INF/componentconfiguration.xsd";
	private static final String CONFIGURATION_PATH = "/META-INF/config.xml";
	
	private InputStream instanceStream;
	private InputStream schemaStream;
	private String contextCodebase;
	private ComponentClassLoader componentLoader;
	
	/**
	 * Constructs a XMLConfigurationFile. It is primed with a <code>File</code>,
	 * where it will attempt to read the configuration from.
	 * This constructor defaults to schema embedded in the jar.
	 * 
	 * @param instance
	 *            the <code>File</code> pointing to a configuration instance 
	 * @throws CodebaseUnloadableException 
	 */
	public ComponentXMLConfigurationFile(File archive) throws WSMXConfigurationException, CodebaseUnloadableException {
		super();
		if (archive == null)
			throw new WSMXConfigurationException("File that points to archive may not be null.");
		
		contextCodebase = archive.getAbsolutePath();
		componentLoader = new ComponentClassLoader(archive.getAbsolutePath(), WSMXKernel.class.getClassLoader());

		
		instanceStream  = componentLoader.getResourceAsStream(CONFIGURATION_PATH);
		if (instanceStream  == null)
			throw new WSMXConfigurationException("Stream that points to configuration instance may not be null.");
	
		try {
			schemaStream = getClass().getResource(DEFAULT_SCHEMA).openStream();	
		} catch (IOException ioe) {
			throw new WSMXConfigurationException("Failed to open input stream. (" + DEFAULT_SCHEMA + ")");
		}		
		if (schemaStream == null)
			throw new WSMXConfigurationException("Failed to open input stream. (" + DEFAULT_SCHEMA + ")");
	}
	
	/**
	 * Constructs a XMLConfigurationFile. It is primed with a <code>URL</code>,
	 * where it will attempt to read the configuration from.
	 * This constructor defaults to schema embedded in the jar.
	 * 
	 * @param instance
	 *            the <code>URL</code> pointing to a configuration instance 
	 */
	public ComponentXMLConfigurationFile(URL instance) throws WSMXConfigurationException {
		super();
		if (instance == null)
			throw new WSMXConfigurationException("URL that points to configuration instance may not be null.");

		InputStream inStream = null;
		try {
			inStream = instance.openStream();	
		} catch (IOException ioe) {
			throw new WSMXConfigurationException("Failed to open input stream. (" + instance.getPath() + ")");
		}		
		if (inStream == null)
			throw new WSMXConfigurationException("Failed to open input stream. (" + instance.getPath() + ")");
		
		this.instanceStream = inStream;

		try {
			inStream = getClass().getResource(DEFAULT_SCHEMA).openStream();	
		} catch (IOException ioe) {
			throw new WSMXConfigurationException("Failed to open input stream. (" + DEFAULT_SCHEMA + ")");
		}		
		if (inStream == null)
			throw new WSMXConfigurationException("Failed to open input stream. (" + DEFAULT_SCHEMA + ")");
		
		this.schemaStream = inStream;
	}
	
	/**
	 * Constructs a XMLConfigurationFile. It is primed with <code>Files</code>,
	 * where it will attempt to read the configuration from.
	 * 
	 * @param instance
	 *            the <code>File</code> pointing to a configuration instance 
	 * @param schema
	 *            the <code>File</code> pointing to a configuration schema 
	 */
	public ComponentXMLConfigurationFile(File instance, File schema) throws WSMXConfigurationException {
		super();
		if (instance == null)
			throw new WSMXConfigurationException("File that points to configuration instance may not be null.");
		if (schema == null)
			throw new WSMXConfigurationException("File that points to configuration schema may not be null.");

		try {
			this.instanceStream = new BufferedInputStream(new FileInputStream(instance));
		} catch (FileNotFoundException fnfe) {
			throw new WSMXConfigurationException("Configuration file was not found.");
		}	
		
		try {
			this.schemaStream = new BufferedInputStream(new FileInputStream(schema));
		} catch (FileNotFoundException fnfe) {
			throw new WSMXConfigurationException("Configuration file was not found.");
		}	
		
	}
	
	/**
	 * Constructs a XMLConfigurationFile. It is primed with <code>URLs</code>,
	 * where it will attempt to read the configuration from.
	 * 
	 * @param instance
	 *            the <code>URL</code> pointing to a configuration instance file
	 * @param schema
	 *            the <code>URL</code> pointing to a configuration schema file
	 */
	public ComponentXMLConfigurationFile(URL instance, URL schema) throws WSMXConfigurationException {
		super();
		if (instance == null)
			throw new WSMXConfigurationException("URL that points to configuration instance may not be null.");
		if (schema == null)
			throw new WSMXConfigurationException("URL that points to configuration schema may not be null.");
			
		InputStream inStream = null;
		try {
			inStream = instance.openStream();	
		} catch (IOException ioe) {
			throw new WSMXConfigurationException("Failed to open input stream. (" + instance.getPath() + ")");
		}		
		if (inStream == null)
			throw new WSMXConfigurationException("Failed to open input stream. (" + instance.getPath() + ")");
		
		this.instanceStream = inStream;
		
		inStream = null;
		try {
			inStream = schema.openStream();	
		} catch (IOException ioe) {
			throw new WSMXConfigurationException("Failed to open input stream. (" + schema.getPath() + ")");
		}		
		if (inStream == null)
			throw new WSMXConfigurationException("Failed to open input stream. (" + schema.getPath() + ")");
		
		this.schemaStream = inStream;
	}

	/**
	 * Constructs a XMLConfigurationFile. It is primed with a classloader.
	 * This constructor defaults to schema embedded in the jar.
	 */
	public ComponentXMLConfigurationFile(ComponentClassLoader classloader) throws WSMXConfigurationException {
		super();
		if (classloader  == null)
			throw new WSMXConfigurationException("ClassLoader may not be null.");
		componentLoader = classloader;
		//FIXME file instead of string
		contextCodebase = classloader.getCodebase().toString();
		instanceStream  = componentLoader.getResourceAsStream(CONFIGURATION_PATH);
		if (instanceStream  == null)
			throw new WSMXConfigurationException("Stream that points to configuration instance may not be null.");
	
		try {
			schemaStream = getClass().getResource(DEFAULT_SCHEMA).openStream();	
		} catch (IOException ioe) {
			throw new WSMXConfigurationException("Failed to open input stream. (" + DEFAULT_SCHEMA + ")");
		}		
		if (schemaStream == null)
			throw new WSMXConfigurationException("Failed to open input stream. (" + DEFAULT_SCHEMA + ")");		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.core.XMLConfigurationFile#load()
	 */
	public List<ComponentConfiguration> load() throws WSMXConfigurationException {
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			parserFactory.setValidating(true);
			SAXParser parser = parserFactory.newSAXParser();
			parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			// overriding the schema since we won't be parsing
			// anything but configurations here
			parser.setProperty(JAXP_SCHEMA_SOURCE, schemaStream);
			ComponentConfigurationHandler handler = new ComponentConfigurationHandler(componentLoader, contextCodebase);
			//TODO buffering?
			parser.parse(instanceStream, handler);
			return handler.getComponentConfigurations();
		} catch (ParserConfigurationException pce) {
			throw new WSMXConfigurationException("Parser could not be built: "
					+ pce.getMessage());
		} catch (SAXNotRecognizedException snre) {
			throw new WSMXConfigurationException(
					"The parser implementation does not provide sufficient support: " + snre.getMessage());
		} catch (SAXException se) {
			Exception origin = se;
			// check wether the SAXException wraps another exception
			// if it does we prefer the wrapped exception, because it will
			// reveal more detailed information about the code ultimately
			// reponsible for the error condition
			if (se.getException() != null)
				origin = se.getException();
			throw new WSMXConfigurationException(
					"General error while parsing the configuration: "
							+ origin.getMessage());
		} catch (IOException ioe) {
			throw new WSMXConfigurationException(
					"Could not read configuration: " + ioe.getMessage());
		}
	}

}
