/*
 * Copyright (c) 2008 National University of Ireland, Galway
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

package ie.deri.wsmx.resourcemanager.inmemory;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.core.configuration.annotation.Exposed;
import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;
import ie.deri.wsmx.scheduler.Environment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.omwg.mediation.language.export.Exporter;
import org.omwg.mediation.language.export.omwg.OmwgSyntaxFormat;
import org.omwg.mediation.language.objectmodel.api.MappingDocument;
import org.omwg.mediation.parser.alignment.XpathParser;
import org.omwg.ontology.Ontology;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.execution.common.component.resourcemanager.GoalResourceManager;
import org.wsmo.execution.common.component.resourcemanager.MappingResourceManager;
import org.wsmo.execution.common.component.resourcemanager.MediatorResourceManager;
import org.wsmo.execution.common.component.resourcemanager.NonWSMOResourceManager;
import org.wsmo.execution.common.component.resourcemanager.OMWGMLResourceManager;
import org.wsmo.execution.common.component.resourcemanager.OntologyResourceManager;
import org.wsmo.execution.common.component.resourcemanager.WSDLResourceManager;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.DiscoveryType;
import org.wsmo.execution.common.nonwsmo.MessageId;
import org.wsmo.execution.common.nonwsmo.WSDLDocument;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.mediator.Mediator;
import org.wsmo.mediator.OOMediator;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;
import org.xml.sax.SAXException;


/**
 * In memory, non-persistant Resource Manager.
 *
 * <pre>
 * Created on 11-May-2005
 * Committed by $Author$
 * $Source$
 * </pre>
 * 
 * @author Maciej Zaremba
 *
 * @version $Revision$ $Date$
 */ 
@WSMXComponent(name   = "ResourceManager",
			   events = "RESOURCEMANAGER",
			   description = "A fully functional, pure in-memory resource manager with no persistence." +
			   				 " It has exposed methods that allow to view the contents in the internal datastructures.")
public class InMemoryRM implements OntologyResourceManager, MediatorResourceManager, WebServiceResourceManager, GoalResourceManager, NonWSMOResourceManager, WSDLResourceManager, OMWGMLResourceManager, MappingResourceManager, Serializable {
	private static final long serialVersionUID = -9163270336667966869L;

	static Logger logger = Logger.getLogger(InMemoryRM.class);  
    
    private Set <Ontology> ontologies = new HashSet <Ontology> ();
    private Set <Mediator> mediators = new HashSet <Mediator> ();
    private Set <WebService> allWebServices = new HashSet <WebService> ();

    private Set<WebService> functionalWebServices = new HashSet<WebService>();
    private Set<WebService> lightweightRuleWebServices = new HashSet<WebService>();
    private Set<WebService> heavyweightRuleWebServices = new HashSet<WebService>();
    private Set<WebService> qosServices = new HashSet<WebService>();
    private Set<WebService> instanceServices = new HashSet<WebService>();
    private Set<WebService> instanceComposeServices = new HashSet<WebService>();
    
    private Set <Goal> goals = new HashSet <Goal> ();
    
    //maps WSMO elements to file representation
    
    //Map< TopEntity IRI, fileName > 
    static Map<String, String> mapOfIRIs;

    //Map< fileName, List<TopEntity> >
    static Map <String, List<TopEntity> > map;

    private Map <Context, Map <MessageId, String>> nonWSMOObjects = new HashMap <Context, Map <MessageId, String>> ();
    private Map <Context, Map <Goal, WSDLDocument>> goalWSDLs = new HashMap <Context, Map <Goal, WSDLDocument>> ();
    private Map <WebService, WSDLDocument> webServiceWSDLs = new HashMap <WebService, WSDLDocument> ();
    
    // distinguishes mapping from wsml-based documents (for initial file loading) 
    protected enum ResourceType { 
    	WSML    (".wsml"), 
    	MAPPING (".map");    	
    	public final String fileSuffix;
    	private ResourceType(String fileSuffix) { this.fileSuffix = fileSuffix; }
    }; 
    
    WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap());

    IRI lwRuleWebServiceDiscoveryIRI = null;
    IRI hwRuleWebServiceDiscoveryIRI = null;
    IRI qosServDiscIRI = null;
    IRI instanceServDiscIRI = null;
    IRI instanceComposeServDiscIRI = null;
    
    // WSMO Resource Manager Store Methods - initial read of WSs and ontologies
    public InMemoryRM() {
		super();
		
		lwRuleWebServiceDiscoveryIRI =	wsmoFactory.createIRI("http://www.wsmo.org/webservice/discovery/rule");
		hwRuleWebServiceDiscoveryIRI =	wsmoFactory.createIRI("http://www.wsmo.org/webservice/discovery/rule/extendedPlugin");
		qosServDiscIRI = 				wsmoFactory.createIRI("http://www.wsmo.org/webservice/discovery/qos");
		instanceServDiscIRI = 			wsmoFactory.createIRI("http://www.wsmo.org/webservice/discovery/instancebased");
		instanceComposeServDiscIRI = 	wsmoFactory.createIRI("http://www.wsmo.org/webservice/discovery/instancebased/composeServices"); 
		
		try {
			Properties config = Environment.getConfiguration();
			
			String ontosStr = config.getProperty("wsmx.resourcemanager.ontologies");
			if (ontosStr!=null) {
				logger.info("Loading ontologies:");
				loadDirectories(getDirectories(ontosStr), ResourceType.WSML);
				logger.info(getDirectories(ontosStr));
			}

			String webservicesStr = config.getProperty("wsmx.resourcemanager.webservices");
			if (webservicesStr!=null) {
				logger.info("Loading Web services:");
				loadDirectories(getDirectories(webservicesStr), ResourceType.WSML);
				logger.info(getDirectories(webservicesStr));
			}

			String goalsServerModeStr = config.getProperty("wsmx.resourcemanager.goals");
			if (goalsServerModeStr!=null && Environment.isCore()) {
				logger.info("Loading goals:");
				loadDirectories(getDirectories(goalsServerModeStr), ResourceType.WSML);
				logger.info(getDirectories(goalsServerModeStr));
			}
			
			String goalsOfflineModeStr = config.getProperty("wsmx.resourcemanager.goals.offline");
			if (goalsOfflineModeStr!=null && !Environment.isCore()) {
				logger.info("Loading offline goals:");
				loadDirectories(getDirectories(goalsOfflineModeStr),ResourceType.WSML);
			}
			
			String mediatorsStr = config.getProperty("wsmx.resourcemanager.mediators");
			if (mediatorsStr!=null) {
				logger.info("Loading mediators:");
				loadDirectories(getDirectories(mediatorsStr), ResourceType.WSML);
			}
			
			String extrasStr = config.getProperty("wsmx.resourcemanager.extras");
			if (extrasStr!=null) {
				logger.info("Loading extra WSMO entities:");
				loadDirectories(getDirectories(extrasStr), ResourceType.WSML);
			}
			
			String mappingsStr = config.getProperty("wsmx.resourcemanager.mappings");			
			if (mappingsStr!=null) {
				logger.info("Loading mapping documents:");
				loadDirectories(getDirectories(mappingsStr), ResourceType.MAPPING);
			} 

			logger.info("Loaded Ontologies: " + Helper.getAllOntologies().size());
			logger.info("Loaded Web services: " + Helper.getAllWebServices().size());
			logger.info("Loaded Goals: " + Helper.getAllGoals().size());
			logger.info("Loaded Mediators: " + Helper.getAllMediators().size());
			logger.info("Loaded Mappings: " + Helper.getAllMappings().size());
			
		} catch (Throwable t) {
			t.printStackTrace();
			logger.warn("Background resources loading failed.", t);
		}
	}
    
    /* Tokenizes String according to the following convention:
     * "dir1"; "dir2"; ...  
    */
    private ArrayList<String> getDirectories(String propertyString){
		ArrayList<String> dirs = new ArrayList<String>();
    	StringTokenizer st = new StringTokenizer(propertyString,";");
		while (st.hasMoreElements()){
			String dir = st.nextToken();
			if (dir == null || dir.equals("") || dir.indexOf("\"") == -1) 
				continue;
			dir = dir.substring(dir.indexOf("\"")+1);
			if (dir == null || dir.equals("") || dir.indexOf("\"") == -1)
				continue;
			dir = dir.substring(0, dir.indexOf("\""));
			dirs.add(dir);
		}
		return dirs; 
    }
    
    private void loadDirectories(ArrayList<String> directoriesStr, ResourceType resourceType) {
    	for (String dirStr : directoriesStr){
    		loadDirectory(dirStr, resourceType);
    	}
    }
    
    private void loadDirectory(String directoryStr, final ResourceType resourceType) {
    	String resourceStr = "";
    	if (Environment.isCore()){
    		resourceStr = Environment.getKernelLocation().getPath()+File.separator+"resources";
    	} else {
    		resourceStr = "resources";
    	}
    	
    	directoryStr = directoryStr.replace("$(resources)", resourceStr);
		logger.info(directoryStr);
	
		File wsmlDir = new File(directoryStr);
				
		if (!wsmlDir.exists()) {
			logger.debug("Background "+ directoryStr+ " directory does not exist. Skipping loading of background files.");
			return;
		} 
		
		if (!wsmlDir.canRead()) {
			logger.warn("Background " + directoryStr + " directory exist but is not readable. " +
					"Check file system permissions.");			
			return;
		}
		
		File[] files = wsmlDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith( resourceType.fileSuffix );
			}
		});
		
		switch(resourceType) {
			case WSML:    
				for (File file : files) storeTopEntity(file); break;
			case MAPPING:
				for (File file : files) storeMapping(file); break;
		}	
    }
    
    private void storeTopEntity(File file) {
    	try {

			TopEntity[] topEntities = Helper.parse(file);
		
			for (TopEntity topEntity : topEntities) {
				if (topEntity instanceof Ontology) {
					storeOntology((Ontology)topEntity);
					logger.info("Stored ontology " + topEntity.getIdentifier() + " from " + file.getName());
				}
				else if (topEntity instanceof Goal) {
					storeGoal((Goal)topEntity);		
					logger.info("Stored goal " + topEntity.getIdentifier() + " from " + file.getName());
				}
				else if (topEntity instanceof WebService) {
					storeWebService((WebService)topEntity);
					logger.info("Stored webservice " + topEntity.getIdentifier() + " from " + file.getName());
				}
				else if (topEntity instanceof Mediator) {
					storeMediator((Mediator)topEntity);	
					logger.info("Stored mediator " + topEntity.getIdentifier() + " from " + file.getName());
				}
//				else
//					logger.warn("Parsed a TopEntity other than {Ontology, Goal, WebService, Mediator} "
//							+ "and don't know what to do with it: " + topEntity.getIdentifier() + " :: " +
//							topEntity.getClass().getSimpleName());
			}					
		
		} catch (UnsupportedOperationException e) {
			logger.warn("Failed to store " + file.getAbsolutePath() + ": Operation not supported by ResourceManager", e);
		} catch (ComponentException e) {
			logger.warn("Failed to store " + file.getAbsolutePath() + ".", e);
		}
    }
    
    private void storeMapping(File file) {
    	try {
	    	MappingDocument mapdoc = XpathParser.parse(file);			
			Helper.storeMapping(mapdoc);
			
			logger.info("Stored mapping document " + mapdoc.getId().plainText() + " from " + file.getName() 
					+ "\n  with source ontology: " + mapdoc.getSource().getUri().plainText() 
					+ "\n  and  target ontology: " + mapdoc.getTarget().getUri().plainText());			
		
		} catch (UnsupportedOperationException e) {
			logger.warn("Failed to store " + file.getAbsolutePath() + ": Operation not supported by ResourceManager.", e);
		} catch (SAXException e) {
			logger.warn("Failed to store " + file.getAbsolutePath() + ": Error during parsing of mapping file.", e);
		} catch (Exception e) {
			logger.warn("Failed to store " + file.getAbsolutePath() + ".", e);
		} 
    }

    @Exposed(description = "Stores Ontology.")
	public void storeOntology(Ontology ontology) throws ComponentException, UnsupportedOperationException {
        this.ontologies.add(ontology);
    }
    
    @Exposed(description = "Stores Web service.")
    public void storeWebService(WebService webService) throws ComponentException, UnsupportedOperationException {
    	
    	Set lwRuleNFPs = webService.getCapability().listNFPValues(lwRuleWebServiceDiscoveryIRI);
    	Set hwRuleNFPs = webService.getCapability().listNFPValues(hwRuleWebServiceDiscoveryIRI);
		Set qoSNFPs = webService.getCapability().listNFPValues(qosServDiscIRI);    	
		Set instNFPs = webService.getCapability().listNFPValues(instanceServDiscIRI);
		Set instCompNFPs = webService.getCapability().listNFPValues(instanceComposeServDiscIRI);

		//if composeServices add only services which allow it
		if (!instCompNFPs.isEmpty() && instCompNFPs.iterator().next().toString().toLowerCase().equals("true") ){
			this.instanceComposeServices.add(webService);
		}
		else if (!instNFPs.isEmpty() && instNFPs.iterator().next().toString().toLowerCase().equals("true") )
			this.instanceServices.add(webService);
		else if (!qoSNFPs.isEmpty() && qoSNFPs.iterator().next().toString().toLowerCase().equals("true") ){
			this.qosServices.add(webService);
			this.functionalWebServices.add(webService);
		}
		else if (!lwRuleNFPs.isEmpty() && lwRuleNFPs.iterator().next().toString().toLowerCase().equals("true") )
			this.lightweightRuleWebServices.add(webService);
		else if (!hwRuleNFPs.isEmpty() && hwRuleNFPs.iterator().next().toString().toLowerCase().equals("true") )
			this.heavyweightRuleWebServices.add(webService);
		else
			//only functional discovery
			this.functionalWebServices.add(webService);

		this.allWebServices.add(webService);
    }
    
    @Exposed(description = "Stores Goal.")
    public void storeGoal(Goal goal) throws ComponentException, UnsupportedOperationException {
        this.goals.add(goal);
    }
    
    @Exposed(description = "Stores Mediator.")
    public void storeMediator(Mediator mediator) throws ComponentException, UnsupportedOperationException {
        this.mediators.add(mediator);
    }
    
    // WSMO Resource Manager Remove Methods
    @Exposed(description = "Removes Ontology.")
    public void removeOntology(Ontology ontology) throws ComponentException, UnsupportedOperationException {
        this.ontologies.remove(ontology);
    }
    
    @Exposed(description = "Removes Web service.")
    public void removeWebService(WebService webService) throws ComponentException, UnsupportedOperationException {
    	this.functionalWebServices.remove(webService);
    	this.lightweightRuleWebServices.remove(webService);
    	this.heavyweightRuleWebServices.remove(webService);
    	this.instanceComposeServices.remove(webService);
    	this.instanceServices.remove(webService);
    	this.qosServices.remove(webService);
        this.allWebServices.remove(webService);
    }
    
    @Exposed(description = "Removes Goal.")
    public void removeGoal(Goal goal) throws ComponentException, UnsupportedOperationException {
        this.goals.remove(goal);
    }
    
    @Exposed(description = "Removes Mediator.")
    public void removeMediator(Mediator mediator) throws ComponentException, UnsupportedOperationException {
        this.mediators.remove(mediator);
    }

    // WSMO Resource Manager Retrieve Methods
    
    @Exposed(description = "Returns all stored ontologies.")
    public Set<Ontology> retrieveOntologies() throws ComponentException, UnsupportedOperationException {
        return ontologies;
    }
    
    @Exposed(description = "Returns all stored ontologies from a given namespace.")
    public Set<Ontology> retrieveOntologies(Namespace namespace) throws ComponentException, UnsupportedOperationException {
        Set<Ontology> result = new HashSet<Ontology>();
        for (Ontology ontology : ontologies){
            if (ontology.getDefaultNamespace().getIRI().toString().equals(namespace.getIRI().toString())){
                result.add(ontology);
            }
        }
        return result;
    }
    
    @Exposed(description = "Returns ontology with a given identifier.")
    public Ontology retrieveOntology(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        for (Ontology o : ontologies){
            if (o.getIdentifier().equals(identifier)){
                return o;
            }
        }
        return null;
    }
    
    @Exposed(description = "Returns all stored web services.")
    public Set<WebService> retrieveWebServices() throws ComponentException, UnsupportedOperationException {
        return allWebServices;
    }
    
    @Exposed(description = "Returns all Web services according to specified category.")
    public Set<WebService> retrieveWebServices(int discoveryType) throws ComponentException, UnsupportedOperationException {
    	
    	Set<WebService> response = new HashSet<WebService>();
    	if (discoveryType == DiscoveryType.SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY)
    		response.addAll(instanceComposeServices);
    	else if (discoveryType == DiscoveryType.SERVICE_INSTANCEBASED_DISCOVERY)
    		response.addAll(instanceServices);
    	else if (discoveryType == DiscoveryType.SERVICE_QOS_DISCOVERY)
    		response.addAll(qosServices);
    	else if (discoveryType == DiscoveryType.WEBSERVICE_LIGHTWEIGHT_RULE_DISCOVERY)
    		response.addAll(lightweightRuleWebServices);
    	else if (discoveryType == DiscoveryType.WEBSERVICE_HEAVYWEIGHT_DISCOVERY)
    		response.addAll(heavyweightRuleWebServices);
    	else 
    		response.addAll(functionalWebServices);
    	
        return response;
    }
    
    @Exposed(description = "Returns all Web services referring specified ontology.")
	public Set<WebService> retrieveWebServicesReferringOntology(Identifier ontoIdentifer) throws ComponentException,	UnsupportedOperationException {
    	Set<WebService> result = new HashSet<WebService>();
    	
    	Set<WebService> all = Helper.getAllWebServices();
    	for (WebService ws: all){
    		Set<Interface> interfs = ws.listInterfaces();
   			Set <Ontology> ontos = ws.listOntologies();
   			for (Ontology onto : ontos){
   				if (onto.getIdentifier().toString().equals(ontoIdentifer.toString())) {
   					result.add(ws);
   					break;
   				}
   			}
    	}
		return result;
	}
    
    @Exposed(description = "Returns all stored web services from a given namespace.")
    public Set<WebService> retrieveWebServices(Namespace namespace) throws ComponentException, UnsupportedOperationException {
        Set<WebService> result = new HashSet<WebService>();
        for (WebService webService : allWebServices){
            if (webService.getDefaultNamespace().equals(namespace)){
                result.add(webService);
            }
        }
        return result;
    }
    
    @Exposed(description = "Returns web service with a given identifier.")
    public WebService retrieveWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        for (WebService ws : allWebServices){
            if (ws.getIdentifier().equals(identifier)){
                return ws;
            }
        }
        return null;
    }
    
    @Exposed(description = "Returns all stored goals.")
    public Set<Goal> retrieveGoals() throws ComponentException, UnsupportedOperationException {
        return goals;
    }
    
    @Exposed(description = "Returns all stored goals from a given namespace.")
    public Set<Goal> retrieveGoals(Namespace namespace) throws ComponentException, UnsupportedOperationException {
        Set<Goal> result = new HashSet<Goal>();
        for (Goal goal : goals){
            if (goal.getDefaultNamespace().equals(namespace)){
                result.add(goal);
            }
        }
        return result;
    }
    
    @Exposed(description = "Returns Goal with a given identifier.")
    public Goal retrieveGoal(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        for (Goal g : goals){
            if (g.getIdentifier().equals(identifier)){
                return g;
            }
        }
        return null;
    }
    
    @Exposed(description = "Returns all stored mediators.")
    public Set<Mediator> retrieveMediators() throws ComponentException, UnsupportedOperationException {
        return mediators;
    }
    
    @Exposed(description = "Returns all stored mediators from a given namespace.")
    public Set<Mediator> retrieveMediators(Namespace namespace) throws ComponentException, UnsupportedOperationException {
        Set<Mediator> result = new HashSet<Mediator>();
        for (Mediator mediator : mediators){
            if (mediator.getDefaultNamespace().equals(namespace)){
                result.add(mediator);
            }
        }
        return result;
    }
    
    @Exposed(description = "Returns mediator with a given identifier.")
    public Mediator retrieveMediator(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        for (Mediator m : mediators){
            if (m.getIdentifier().equals(identifier)){
                return m;
            }
        }
        return null;
    }
    
    @Exposed(description="Returns OOMediator for mediation from source ontology to target ontology")
	public OOMediator retrieveOOMediator(IRI sourceOntologyIRI, IRI targetOntologyIRI) throws ComponentException, UnsupportedOperationException {
		return Helper.retrieveOOMediator(sourceOntologyIRI, targetOntologyIRI);
	}
    
    // WSMO resource Manager get Namespaces methods
    @Exposed(description = "Returns all namespaces used by stored Ontologies.")
    public Set<Namespace> getOntologyNamespaces() throws ComponentException, UnsupportedOperationException{
        Set<Namespace> result = new HashSet<Namespace> ();
        for (Ontology ontology : ontologies){
            if (ontology.getDefaultNamespace() != null){
                result.add(ontology.getDefaultNamespace());
            }
        }
        return result;
    }
    
    @Exposed(description = "Returns all namespaces used by stored Web services.")
    public Set<Namespace> getWebServiceNamespaces() throws ComponentException, UnsupportedOperationException{
        Set<Namespace> result = new HashSet<Namespace> ();
        for (WebService webService : allWebServices){
            if (webService.getDefaultNamespace() != null){
                result.add(webService.getDefaultNamespace());
            }
        }
        return result;
    }
    
    @Exposed(description = "Returns all namespaces used by stored Goals.")
    public Set<Namespace> getGoalNamespaces() throws ComponentException, UnsupportedOperationException{
        Set<Namespace> result = new HashSet<Namespace> ();
        for (Goal goal : goals){
            if (goal.getDefaultNamespace() != null){
                result.add(goal.getDefaultNamespace());
            }
        }
        return result;
    }
    
    @Exposed(description = "Returns all namespaces used by stored Mediators.")
    public Set<Namespace> getMediatorNamespaces() throws ComponentException, UnsupportedOperationException{
        Set<Namespace> result = new HashSet<Namespace> ();
        for (Mediator mediator : mediators){
            if (mediator.getDefaultNamespace() != null){
                result.add(mediator.getDefaultNamespace());
            }
        }
        return result;
    }
    
    // WSMO Resource Manager get Identifiers methods
    @Exposed(description = "Returns the identifiers of all stored ontologies.")
    public Set <Identifier> getOntologyIdentifiers() throws ComponentException, UnsupportedOperationException {
        Set <Identifier> result = new HashSet <Identifier> ();
        for (Ontology o : ontologies){
            result.add(o.getIdentifier());
        }
        return result;
    }

    @Exposed(description = "Returns the identifiers of all stored ontologies with the given namespace.")
    public Set<Identifier> getOntologyIdentifiers(Namespace namespace){
        Set <Identifier> result = new HashSet <Identifier> ();
        for (Ontology o : ontologies){
            if (o.getDefaultNamespace().equals(namespace)){
                result.add(o.getIdentifier());
            }
        }
        return result;
    }

    @Exposed(description = "Returns the identifiers of all stored mediators.")
    public Set<Identifier> getMediatorIdentifiers() throws ComponentException, UnsupportedOperationException {
        Set <Identifier> result = new HashSet <Identifier> ();
        for (Mediator m : mediators){
            result.add(m.getIdentifier());
        }
        return result;
    }

    @Exposed(description = "Returns the identifiers of all stored mediators with the given namespace.")
    public Set<Identifier> getMediatorIdentifiers(Namespace namespace){
        Set <Identifier> result = new HashSet <Identifier> ();
        for (Mediator m : mediators){
            if (m.getDefaultNamespace().equals(namespace)){
                result.add(m.getIdentifier());
            }
        }
        return result;
    }
    
    @Exposed(description = "Returns the identifiers of all stored web services")
    public Set<Identifier> getWebServiceIdentifiers() throws ComponentException, UnsupportedOperationException {
        Set <Identifier> result = new HashSet <Identifier> ();
        for (WebService ws : allWebServices){
            result.add(ws.getIdentifier());
        }
        return result;
    }

    @Exposed(description = "Returns the identifiers of all stored web services with the given namespace.")
    public Set<Identifier> getWebServiceIdentifiers(Namespace namespace){
        Set <Identifier> result = new HashSet <Identifier> ();
        for (WebService ws : allWebServices){
            if (ws.getDefaultNamespace().equals(namespace)){
                result.add(ws.getIdentifier());
            }
        }
        return result;
    }

    @Exposed(description = "Returns the identifiers of all stored goals.")
    public Set<Identifier> getGoalIdentifiers() throws ComponentException, UnsupportedOperationException {
        Set <Identifier> result = new HashSet <Identifier> ();
        for (Goal g : goals){
            result.add(g.getIdentifier());
        }
        return result;
    }

    @Exposed(description = "Returns the identifiers of all stored goals with the given namespace.")
    public Set<Identifier> getGoalIdentifiers(Namespace namespace){
        Set <Identifier> result = new HashSet <Identifier> ();
        for (Goal g : goals){
            if (g.getDefaultNamespace().equals(namespace)){
                result.add(g.getIdentifier());
            }
        }
        return result;
    }
    
    // WSMO resource Manager contains methods
    
    @Exposed(description = "Checks if ontology of given identifier exists in the storage.")
    public boolean containsOntology(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        boolean found = false;
        for (Ontology o : ontologies){
            if (o.getIdentifier().equals(identifier)){
                found = true;
                break;
            }
        }
        return found;
    }
    
    @Exposed(description = "Checks if web service of given identifier exists in the storage.")
    public boolean containsWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        boolean found = false;
        for (WebService ws : allWebServices){
            if (ws.getIdentifier().equals(identifier)){
                found = true;
                break;
            }
        }
        return found;
    }
    
    @Exposed(description = "Checks if goal of given identifier exists in the storage.")
    public boolean containsGoal(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        boolean found = false;
        for (Goal g : goals){
            if (g.getIdentifier().equals(identifier)){
                found = true;
                break;
            }
        }
        return found;
    }
    
    @Exposed(description = "Checks if mediator of given identifier exists in the storage.")
    public boolean containsMediator(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        boolean found = false;
        for (Mediator m : mediators){
            if (m.getIdentifier().equals(identifier)){
                found = true;
                break;
            }
        }
        return found;
    }
    
    @Exposed(description="Checks if there is an OOMediator for mediation from source ontology to target ontology")
    public boolean containsOOMediator(IRI sourceOntologyIRI, IRI targetOntologyIRI) throws ComponentException, UnsupportedOperationException {
		return (retrieveOOMediator(sourceOntologyIRI, targetOntologyIRI) != null);
	}

    //Methods for Non WSMO Resource Manager
    
    public void saveMessage(Context Context, MessageId messageId, String message) throws ComponentException, UnsupportedOperationException {
        if (!nonWSMOObjects.containsKey(Context)){
            nonWSMOObjects.put(Context, new HashMap <MessageId, String> ());
        }
        nonWSMOObjects.get(Context).put(messageId, message);
    }
    
    @Exposed(description = "Returns the set of all stored contexts.")
    public Set<Context> getContexts() throws ComponentException, UnsupportedOperationException {
        return nonWSMOObjects.keySet();
    }

    public Set<MessageId> getMessageIds(Context Context) throws ComponentException, UnsupportedOperationException {
        if (!nonWSMOObjects.containsKey(Context)){
            return new HashSet <MessageId> ();
        }
        return nonWSMOObjects.get(Context).keySet();
    }

    public Map<Context, Set<MessageId>> getMessageIds(Set<Object> searchTerms, boolean conjunctive) throws ComponentException, UnsupportedOperationException {
        Map <Context, Set<MessageId>> result = new HashMap <Context, Set<MessageId>> ();
        for (Context Context : nonWSMOObjects.keySet()){
            for (MessageId messageId : nonWSMOObjects.get(Context).keySet()){
                String message = nonWSMOObjects.get(Context).get(messageId);
                boolean allmatches = true;
                boolean onematches = false;
                for (Object term : searchTerms){
                    if (term instanceof String){
                        String st = (String) term;
                        boolean matches = message.contains(st);
                        allmatches = allmatches && matches;
                        onematches = onematches || matches;
                    }
                }
                if ((conjunctive && allmatches) || (!conjunctive && onematches)){
                    if (!result.containsKey(Context)){
                        result.put(Context, new HashSet <MessageId> ());
                    }
                    result.get(Context).add(messageId);
                }
            }
        }
        return result;
    }

    public Set<MessageId> getMessageIds(Context Context, Set<Object> searchTerms, boolean conjunctive) throws ComponentException, UnsupportedOperationException {
        Set<MessageId> result = new HashSet <MessageId> ();
        if (nonWSMOObjects.containsKey(Context)){
            for (MessageId mid : nonWSMOObjects.get(Context).keySet()){
                String message = nonWSMOObjects.get(Context).get(mid);
                boolean allmatches = true;
                boolean onematches = false;
                for (Object term : searchTerms){
                    if (term instanceof String){
                        String st = (String) term;
                        boolean matches = message.contains(st);
                        allmatches = allmatches && matches;
                        onematches = onematches || matches;
                    }
                }
                if ((conjunctive && allmatches) || (!conjunctive && onematches)){
                    result.add(mid);
                }
            }
        }
        return result;
    }

    public Map<MessageId, String> load(Context Context) throws ComponentException, UnsupportedOperationException {
        return nonWSMOObjects.get(Context);
    }

    public String load(Context Context, MessageId messageId) throws ComponentException, UnsupportedOperationException {
        if (!nonWSMOObjects.containsKey(Context)){
            return null;
        }
        return nonWSMOObjects.get(Context).get(messageId);
    }

    @Exposed(description = "Returns all backups of the received WSML messages, packaged in two maps and accessed by Context and MessageId.")
    public Map<Context, Map<MessageId, String>> loadAll() throws ComponentException, UnsupportedOperationException {
        return nonWSMOObjects;
    }

    public void registerWSDL(Context Context, Goal goal, WSDLDocument wsdlDocument) throws ComponentException, UnsupportedOperationException {
        if (!goalWSDLs.containsKey(Context)){
            goalWSDLs.put(Context, new HashMap <Goal, WSDLDocument> ());
        }
        goalWSDLs.get(Context).put(goal, wsdlDocument);
    }

    public WSDLDocument getWSDL(Context Context, Goal goal) throws ComponentException, UnsupportedOperationException {
        if (!goalWSDLs.containsKey(Context)){
            return null;
        }
        return goalWSDLs.get(Context).get(goal);
    }

    //Methods from WSDL Resource Manager
    public void registerWSDL(WebService webService, WSDLDocument wsdlDocument) throws ComponentException, UnsupportedOperationException {
        webServiceWSDLs.put(webService, wsdlDocument);
    }

    public WSDLDocument getWSDL(WebService webService) throws ComponentException, UnsupportedOperationException {
        return webServiceWSDLs.get(webService);
    }
    
    public void deregisterWSDL(WebService webService) throws ComponentException, UnsupportedOperationException {
        webServiceWSDLs.remove(webService);
    }
    
    // Methods from MappingResourceManager
    
    @Exposed(description = "Stores a mapping document.")
	public void storeMapping(MappingDocument mapdoc) throws ComponentException, UnsupportedOperationException {	
    	Helper.storeMapping(mapdoc);
	}

    @Exposed(description = "Removes a mapping document based on its id.")
	public void removeMapping(org.omwg.mediation.language.objectmodel.api.IRI iri) throws ComponentException, UnsupportedOperationException {
		Helper.removeMapping(iri);		
	}
	
	@Exposed(description = "Retrieves a mapping document based on its id.")
	public MappingDocument retrieveMapping(org.omwg.mediation.language.objectmodel.api.IRI iri) throws ComponentException, UnsupportedOperationException {
		return Helper.getMapping(iri);
	}

	@Exposed(description = "Retrieves a mapping document based on source and target ontology IRI.")
	public MappingDocument retrieveMapping(IRI sourceOntologyIRI, IRI targetOntologyIRI) throws ComponentException, UnsupportedOperationException {
		return Helper.getMapping(sourceOntologyIRI, targetOntologyIRI);
	}

	@Exposed(description = "Retrieves all mapping documents in storage.")
	public Collection<MappingDocument> retrieveMappings() throws ComponentException, UnsupportedOperationException {
		return Helper.getAllMappings();
	}
	
	public boolean containsMapping(org.omwg.mediation.language.objectmodel.api.IRI iri) throws ComponentException, UnsupportedOperationException {
		return Helper.containsMapping(iri);
	}

	public boolean containsMapping(IRI sourceOntologyIRI, IRI targetOntologyIRI) throws ComponentException, UnsupportedOperationException {
		return Helper.containsMapping(sourceOntologyIRI, targetOntologyIRI);
	}
	
	@Exposed(description = "Stores a mapping document (convenience method with string parameters).")
	public void storeMapping(String mapdocAsString) throws ComponentException, UnsupportedOperationException {
		if (mapdocAsString == null)
			throw new IllegalArgumentException("Failed to store mapping document (must not be null)!");
		try {
			InputStream is = new ByteArrayInputStream( mapdocAsString.getBytes() );			
			storeMapping( XpathParser.parse(is) );
		} catch (Exception e) {	
			logger.error("Failed to store mapping document", e);
			throw new ComponentException("Failed to store mapping document", e);
		}
	}
	
    @Exposed(description = "Removes a mapping document based on its id (convenience method with string parameters).")
	public void removeMapping(String iriAsString) throws ComponentException, UnsupportedOperationException {
    	if (iriAsString == null)
			throw new IllegalArgumentException("Failed to remove mapping document (IRI must not be null)!");
    	removeMapping(new org.omwg.mediation.language.objectmodel.api.IRI(iriAsString));
	}
    
    @Exposed(description = "Retrieves a mapping document based on its id (convenience method with string parameters).")
	public String retrieveMapping(String iriAsString) throws ComponentException, UnsupportedOperationException {
		MappingDocument mapdoc = Helper.getMapping( new org.omwg.mediation.language.objectmodel.api.IRI(iriAsString) );
		return (mapdoc == null) ? null : new OmwgSyntaxFormat(true, "\t").export(mapdoc);
	}

	@Exposed(description = "Retrieves a mapping document based on source and target ontology IRI (convenience method with string parameters).")
	public String retrieveMapping(String sourceOntologyIRIAsString, String targetOntologyIRIAsString) throws ComponentException, UnsupportedOperationException {
		MappingDocument mapdoc = Helper.getMapping( 
				wsmoFactory.createIRI(sourceOntologyIRIAsString), 
				wsmoFactory.createIRI(targetOntologyIRIAsString) );
		return (mapdoc == null) ? null : new OmwgSyntaxFormat(true, "\t").export(mapdoc);
	}

	
}