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

import ie.deri.wsmx.core.WSMXKernel;
import ie.deri.wsmx.core.configuration.ComponentConfiguration;
import ie.deri.wsmx.core.configuration.annotation.AnnotationConfigurationHandler;
import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;
import ie.deri.wsmx.core.configuration.descriptor.ComponentXMLConfigurationFile;
import ie.deri.wsmx.exceptions.WSMXConfigurationException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * Manages the systemcodebase, allowing drop-in deployment.
 * Employs a blacklist mechanism when injection failures or 
 * inspection failures occur. For injection failures a variation
 * of the 2PC protocol is used.
 *
 * <pre>
 * Created on 08.04.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/codebase/Codebase.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.31 $ $Date: 2006-03-20 01:16:17 $
 */
public class Codebase {
    String fileExtension = ".wsmx";
	private Map<ObjectName, File> deployedArchives = new HashMap<ObjectName, File>();
    private Map<File, ComponentClassLoader> componentCodebases = new HashMap<File, ComponentClassLoader>();
    private Map<ObjectName, File> candidateDeployedArchives = new HashMap<ObjectName, File>();
    private Map<File, ComponentClassLoader> candidateCodebases = new HashMap<File, ComponentClassLoader>();
    private File codebase;
	static Logger logger = Logger.getLogger(Codebase.class);
	static Set<File> blackListedArchives = new HashSet<File>(); 

	/**
	 * Constructs a new <code>Codebase</code> for a given
	 * path that references the systemcodebase of a WSMX instance.
	 * 
	 * @param path path to the systemcodebase
	 */
	public Codebase(File path) {
		super();
		codebase = path;
		fileExtension = ".wsmx"; //default
	}
	
	/**
	 * Checks if any new archives have been dropped into the
	 * systemcodebase. If this is the case, this method extracts
	 * the configurations either from annotations or configuration files
	 * and returns them to the caller.
	 * 
	 * @return a <code>List</code> of <code>ComponentConfigurations</code>
	 */
	public synchronized List<ComponentConfiguration> update() {
		List<ComponentConfiguration> candidateComponents = new ArrayList<ComponentConfiguration>();
		List<File> currentArchives;
		try {
			currentArchives = determineArchivesInSystemCodebase();
		} catch (CodebaseUnscanableException e) {
			logger.warn("Could not scan codebase at " + codebase, e);
			return candidateComponents;
		}		
        deblacklistRemovedArchives(currentArchives);        
		for (File archive : currentArchives) {				
			if (!deployedArchives.values().contains(archive) && !blackListedArchives.contains(archive)) {
				addArchiveToUpdateList(candidateComponents, archive);
			}
		}
		return candidateComponents;
	}

	/**
	 * Signals the willingness of external entities to commit the deployment
	 * operation for a given conponent.
	 * 
	 * @param configuration the component configuration which is to be comitted
	 * @throws MalformedObjectNameException if the configuration is invalid and results in a flawed objectname
	 */
	public synchronized void tagAsDeployed(ComponentConfiguration configuration) throws MalformedObjectNameException {
		//if the two datastructures with the canidates return null
		//they didn't contain them which means that we did not
		//meet the requirements for a commit
		
		//FIXME this doesn\t work during boot
		//if (candidateDeployedArchives.remove(configuration) != null)
			deployedArchives.put(configuration.getObjectName(), configuration.getCodeBase());
		//if (candidateCodebases.remove(configuration.getCodeBase()) != null)
			componentCodebases.put(configuration.getCodeBase(), configuration.getClassloader());		
	}

	/**
	 * Blacklists a particular archive, which prevents it from beeing scheduled
	 * for hot-deployment until it is deblacklisted.
	 * 
	 * @param archive the archive to be blacklisted
	 */
	public synchronized void blacklistArchive(File archive) {
		logger.warn(archive.getName() + " is blacklisted. Please remove this archive and replace with a valid one.");
		blackListedArchives.add(archive);
	}

	private synchronized void deblacklistRemovedArchives(List<File> currentArchives) {
		for (File blackListedArchive : blackListedArchives) {
			if (!currentArchives.contains(blackListedArchive)) {	
				blackListedArchives.remove(blackListedArchive);
				logger.info("Removing " + blackListedArchive.getName() + " from the black list.");
			}
		}
	}

	private synchronized void tagAsCandidate(File archive, List<ComponentConfiguration> configurations) throws MalformedObjectNameException {
		for(ComponentConfiguration configuration : configurations) {
		    candidateDeployedArchives.put(configuration.getObjectName(), archive);
		    candidateCodebases.put(archive, configuration.getClassloader());
		}
	}

	private List<File> determineArchivesInSystemCodebase() throws CodebaseUnscanableException {
		File[] currentArchives = codebase.listFiles(new FileFilter(){
				public boolean accept(File pathname) {
					if (pathname.getName().endsWith(fileExtension)) 
						return true;
					return false;
				}} );
	    if (currentArchives == null) //sanity check, listFiles() might return null for invalid paths or I/O errors
	        throw new CodebaseUnscanableException("Could not scan " + codebase);
		return Arrays.asList(currentArchives);
	}

	private Class<?> findMainComponentClass(ComponentClassLoader componentLoader) throws InvalidComponentManifestException {
		Manifest manifest = componentLoader.getManifest();		
		Class<?> componentClass = null;
		if (manifest != null) {
		    Attributes attributes = manifest.getMainAttributes();                        
		    if (attributes.containsKey(Attributes.Name.MAIN_CLASS)) { 
		        String componentClassName = attributes.getValue(Attributes.Name.MAIN_CLASS);
				componentClassName = componentClassName.replace('/','.'); //support slashstyle notation as well as dotstyle
				logger.debug("Manifest Main-Class entry points to " + componentClassName);
				try {
					componentClass = componentLoader.loadClass(componentClassName);
				} catch (ClassNotFoundException cnfe) {
					logger.warn("Class that the manifests Main-Class entry points to " +
							"does not exist. Trying to recover.", cnfe);
				}
			} else {
				logger.warn("The manifest does not contain a Main-Class entry.");
		    	throw new InvalidComponentManifestException("The manifest does not contain a Main-Class entry.");
			}
		} else {
		    logger.warn("The component archive does not contain a Manifest.");
			throw new InvalidComponentManifestException("The WSMX archive does not contain a Manifest.");
		}
		return componentClass;
	}

	private synchronized void addArchiveToUpdateList(List<ComponentConfiguration> candidateComponents, File archive) {
		logger.debug("Found new archive: " + archive);
		try {
		    String contextCodebase = archive.getAbsolutePath();
		    ComponentClassLoader componentLoader = new ComponentClassLoader(contextCodebase, WSMXKernel.class.getClassLoader());
		    Class<?> componentClass = findMainComponentClass(componentLoader);
			//annotation config overrides xmlfile config
			if (componentClass != null && componentClass.isAnnotationPresent(WSMXComponent.class)) {
				handleAnnotationConfiguration(candidateComponents, archive, componentLoader);
			} else {					
				handleDescriptorConfiguration(candidateComponents, archive, componentLoader);
			}			
		} catch (InvalidComponentManifestException icm) {
			//TODO include archive in icm exception and display it
			logger.warn("Component Deployment failed: " + icm.getMessage(), icm);
			blacklistArchive(archive);
		} catch (Throwable t) {
			logger.warn("Component Deployment failed. Failed to inspect component for hot-deployment " + archive, t);
			blacklistArchive(archive);
		}
	}

	private void handleAnnotationConfiguration(List<ComponentConfiguration> candidateComponents, File archive, ComponentClassLoader componentLoader) throws WSMXConfigurationException, MalformedObjectNameException {
		logger.debug("Metadata delivered as Annotation in " + archive);
		List<ComponentConfiguration> configurations = null;
		try {
			configurations = new AnnotationConfigurationHandler(componentLoader).load();
		} catch(WSMXConfigurationException wce) {
			logger.warn("Annotation configuration present but invalid.", wce);
			throw wce;
		}
		candidateComponents.addAll(configurations);
		tagAsCandidate(archive, configurations);
	}

	private void handleDescriptorConfiguration(List<ComponentConfiguration> candidateComponents, File archive, ComponentClassLoader componentLoader) throws WSMXConfigurationException, MalformedObjectNameException {
		logger.debug("Metadata delivered as descriptor file " + archive);
		List<ComponentConfiguration> configurations = null;
		try {
			configurations = new ComponentXMLConfigurationFile(componentLoader).load();
		} catch(WSMXConfigurationException wce) {
			logger.warn("No valid configuration present: " + wce.getMessage(), wce);
			throw wce;
		}
		candidateComponents.addAll(configurations);
		tagAsCandidate(archive, configurations);
	}
	
    /**
     * Removes a component. Any internal references will be removed
     * and the archive physically deleted from the systemcodebase. 
     * 
     * @param objectname the name of the object to be unregistered
     */
    public synchronized void unregisterComponent(ObjectName objectname) {
        if (deployedArchives.get(objectname) == null) 
        	logger.warn("Archive that was marked for deletion is not known.\n" +
        			    "Requested archive for "+ objectname + "\n" +
        			    "Known archives: " + deployedArchives);
        else {
        	deployedArchives.get(objectname).delete();
        	componentCodebases.remove(deployedArchives.get(objectname));
        }
        deployedArchives.remove(objectname);
    }

    public Map<File, ComponentClassLoader> getComponentCodebases() {
        return Collections.unmodifiableMap(componentCodebases);
    }

	public static Set<File> getBlackListedArchives() {
		return Collections.unmodifiableSet(blackListedArchives);
	}

	public Map<File, ComponentClassLoader> getCandidateCodebases() {
		return Collections.unmodifiableMap(candidateCodebases);
	}

	public Map<ObjectName, File> getCandidateDeployedArchives() {
		return Collections.unmodifiableMap(candidateDeployedArchives);
	}

	public Map<ObjectName, File> getDeployedArchives() {
		return Collections.unmodifiableMap(deployedArchives);
	}

	public File getCodebase() {
		return new File(codebase.toURI());
	}
}
