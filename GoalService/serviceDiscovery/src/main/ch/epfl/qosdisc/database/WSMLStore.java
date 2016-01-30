/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.database;

import org.wsmo.factory.*;
import org.wsmo.common.*;
import org.wsmo.datastore.WsmoRepository;
import org.wsmo.wsml.*;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.*;
import org.wsmo.service.*;

import org.apache.log4j.*;
import org.deri.wsmo4j.io.parser.wsml.ParserImpl;

import ch.epfl.qosdisc.operators.PropertySet;

import ie.deri.wsmx.commons.Helper;

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import lhvu.qos.utils.Constants;


/**
 * Store WSML information within the database.
 * 
 * @author Sebastian Gerlach
 */
public class WSMLStore {

	/**
	 * Logger for this class.
	 */
	private static Logger log = Logger.getLogger(WSMLStore.class);
	
	/**
	 * The top entities currently in memory, by IRI. An IRI always identifies
	 * a single entity.
	 */
	private static HashMap<String, TopEntity> entitiesByIRI = new HashMap<String,TopEntity>();
	
	/**
	 * The top entities currently in memory, by namespace. A single namespace
	 * may contain several entities.
	 */
	private static HashMap<String, TopEntity[]> entitiesByNamespace = new HashMap<String, TopEntity[]>();
	
	/**
	 * Use for getEntities in order to not limit results.
	 */
	public static final int ANY = 0;
	
	/**
	 * Use for getEntities in order to limit results to ontologies.
	 */
	public static final int ONTOLOGY = 1;

	/**
	 * Use for getEntities in order to limit results to services.
	 */
	public static final int SERVICE = 2;

	/**
	 * Use for getEntities in order to limit results to goals.
	 */
	public static final int GOAL = 3;
	
	/**
	 * The qos root ontology containing the queries used for finding the
	 * qos parameter concepts and units.
	 */
	//FIXME 
	public static String qosRootOntology = "file:///c:/WSMX/resources/qosdiscovery/ontologies/Common/QoSBase.wsml";
//	public static String qosRootOntology = "file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/QoSBase.wsml";	
	
	/**
	 * The list of external repositories to query before trying the IRI.
	 */
	private static Vector<WsmoRepository> repositories = new Vector<WsmoRepository>();
	
	/**
	 * Add a new repository for queries.
	 * 
	 * @param repository The repository to add.
	 */
	public static void addWsmoRepository(WsmoRepository repository) {
		
		repositories.add(repository);
	}
	
	/**
	 * Retrieve an entity from the store. If the entity is not in the store,
	 * it will be downloaded from the URL indicated in the IRI.
	 *  
	 * @param iri IRI of the ontology to retrieve.
	 * @return The ontology, or null if it could not be located
	 */
	public static Collection<TopEntity> getEntities(String iri) {
		
		return getEntities(iri, WSMLStore.ANY, false);
	}
	
	/**
	 * Retrieve an entity from the store. If the entity is not in the store,
	 * it will be downloaded from the URL indicated in the IRI.
	 *  
	 * @param iri IRI of the ontology to retrieve.
	 * @param limitTo Select only entities of a certain type.
	 * @return The ontology, or null if it could not be located
	 */
	public static Collection<TopEntity> getEntities(String iri, int limitTo) {
		
		return getEntities(iri, limitTo, false);
	}
	
	/**
	 * Changes the $(local) component of an IRI if present.
	 * 
	 * @param iri The IRI to change.
	 * @return The IRI with substitution performed.
	 */
	public static String fixIRI(String iri) {
		
		// Replace local directory if it is used.
		return iri.replace("$(local)",PropertySet.getPath());			
	}
	
	/**
	 * Get the WSML text for an item.
	 * 
	 * @param iri The IRI to retrieve.
	 * @return A string containing the full WSML text.
	 */
	public static String getWSMLText(String iri) {
		
		
		// Change iri as required.
		iri = WSMLStore.fixIRI(iri);
		
		log.debug("Requested "+iri);
		
		try {
			
			// Split into namespace and local name.
			int ndx = iri.indexOf('#');	
			String namespace = iri + '#';
			if(ndx != -1) {
				
				// Split on the # sign, keeping the # in the namespace.
				namespace = iri.substring(0,ndx+1);
			}
			
			if(Connection.available()) {
				
				log.debug("Not in memory, trying database.");
			
				// The namespace is not available in memory, we need to obtain it.
				ResultSet rs = Connection.executeQuery("select contents from wsmlfile where namespace like '"+namespace+"'");
				
				if(rs != null && rs.next()) {
					
					// We have the clob, now parse the thing in order to get the entities.					
					Clob clob = rs.getClob(1);
					int l =(int)clob.length();
					char[] txt = new char[l];
					clob.getCharacterStream().read(txt);
					
					// Close result set.
					rs.close();
					
					return new String(txt);
				}
			} 
		} catch(Exception ex) {
			
			ex.printStackTrace();
		}
		return "";
	}
		
	/**
	 * Retrieve an entity from the store. If the entity is not in the store,
	 * it will be downloaded from the URL indicated in the IRI.
	 *  
	 * @param iri IRI of the ontology to retrieve.
	 * @param limitTo Select only entities of a certain type.
	 * @param temp If true, do not store entity in database.
	 * @return The ontology, or null if it could not be located
	 */
	public static Collection<TopEntity> getEntities(String iri, int limitTo, boolean temp) {
		
		// Change iri as required.
		iri = WSMLStore.fixIRI(iri);
		
		log.debug("Requested "+iri);
		
		try {
			
			// Split into namespace and local name.
			int ndx = iri.indexOf('#');	
			String namespace, localName;
			if(ndx == -1) {
				
				// The # is part of the namespace, so this should usually never happen.
				namespace = iri+'#';
				localName = "";
			} else {
				
				// Split on the # sign, keeping the # in the namespace.
				namespace = iri.substring(0,ndx+1);
				localName = iri.substring(ndx+1);
			}
			
			TopEntity[] entities = null;
			if(localName.length()>0) {

				// If we have a local name, we can just attempt to find the IRI
				// in our storage.
				TopEntity e = entitiesByIRI.get(iri);
				TopEntity e2 = Helper.getTopEntity(iri);
				log.fatal("loaded: "+ e2.toString() );
				
				if ( (e == null) && (e2 != null) ){
					//Add the entities to the loaded set.
					entitiesByNamespace.put(((IRI)e2.getIdentifier()).getNamespace(), new TopEntity[]{e2});
					entitiesByIRI.put(e2.getIdentifier().toString(), e2);
					e = e2;
				}
				if(e == null) {
					
					// The IRI is not known, check whether the namespace actually exists.
					TopEntity[] f = entitiesByNamespace.get(namespace);
					
					if(f!=null) {
						
						// The namespace is loaded, therefore the requested entity does
						// not exist.
						log.error("Namespace loaded, but entity could not be found.");
						return null;
					}
				} else {
					
					// We found what we were looking for.
					entities = new TopEntity[] { e };
				}
			} else {
				
				// We only have the namespace, query everything that is in that namespace.
				entities = entitiesByNamespace.get(namespace);
			}
			
			if(entities == null) {
				
				if(Connection.available()) {
					
					log.debug("Not in memory, trying database.");
				
					// The namespace is not available in memory, we need to obtain it.
					ResultSet rs = Connection.executeQuery("select contents from wsmlfile where namespace like '"+namespace+"'");
					if(rs == null || (!rs.next())) {
						
						// Unknown in the database, we need to load it.
						if(rs != null)
							rs.close();
						
						// First of all try all the WSMO repositories we have.
						IRI wsmo4jIri = Factory.createWsmoFactory(null).createIRI(iri);
						List<TopEntity> re = new ArrayList<TopEntity>();
						for(WsmoRepository rep : repositories) {
							
							try {
								Ontology o = rep.getOntology(wsmo4jIri);
								if(o!=null)
									re.add(o);									
							} catch(Exception ex) { }
							try {
								Goal g = rep.getGoal(wsmo4jIri);
								if(g!=null)
									re.add(g);									
							} catch(Exception ex) { }
							try {
								WebService s = rep.getWebService(wsmo4jIri);
								if(s!=null)
									re.add(s);									
							} catch(Exception ex) { }
						}
						if(re.size()>0) {
							
							// We actually managed to get something. Return that.
							entities = (TopEntity[]) re.toArray(new TopEntity[re.size()]);
						} else {
						
							// Last resort.
							log.debug("Not in database, trying URL.");
							
							// Load the thing, removing the # from namespace in order to obtain URL.					
							entities = importWSMLFromURL(namespace.substring(0,namespace.length()-1), temp);
						}
						
					} else {
						
						// We have the clob, now parse the thing in order to get the entities.
						Clob clob = rs.getClob(1);
						Parser parser = getParser();
						entities = parser.parse(clob.getCharacterStream());
						
						// Add the entities to the loaded set.
						entitiesByNamespace.put(((IRI)entities[0].getIdentifier()).getNamespace(), entities);
						for(TopEntity t : entities)
							entitiesByIRI.put(t.getIdentifier().toString(), t);
						
						// Close result set.
						rs.close();
					}
				} else {
					
					log.debug("Database not available, trying URL.");
					
					// Load the thing, removing the # from namespace in order to obtain URL.					
					entities = importWSMLFromURL(namespace.substring(0,namespace.length()-1), true);
				}

				// We have our entities now, if a specific IRI was requested, filter them.
				if(localName.length() > 0) {
					
					TopEntity e = null;
					for(TopEntity t : entities) {
						if(t.getIdentifier().toString().equals(iri))
							e = t;
					}
					
					// The iri was not found, just give up.
					if(e == null)
						return null;
					
					// Store the entity alone.
					entities = new TopEntity[] { e };
				}
				
			}
			
			// Last but not least, if there is a filter requested, apply it now.
			Vector<TopEntity> results = new Vector<TopEntity>();
			for(TopEntity t : entities) {
				if(limitTo == 0)
					results.add(t);
				else if(limitTo == ONTOLOGY && Ontology.class.isAssignableFrom(t.getClass()))
					results.add(t);
				else if(limitTo == SERVICE && WebService.class.isAssignableFrom(t.getClass()))
					results.add(t);
				else if(limitTo == GOAL && Goal.class.isAssignableFrom(t.getClass()))
					results.add(t);
			}
			
			// Return the results.
			return results;
			
		} catch(Exception ex) {
			
			// Print the exception. This is usually fairly harmless stuff, for
			// instance a URL that can't be accessed or invalid WSML format. Mostly
			// we can live with that and continue.
			log.warn("Error when trying to load "+iri+": "+ex.toString());
		}
		return null;
	}
	
	/**
	 * Remove entities from the in-memory store.
	 * 
	 * @param iri The IRI of the entity to remove, or a namespace to remove everything from that namespace.
	 */
	public static void removeEntities(String iri) {
		
		// Change iri as required.
		iri = WSMLStore.fixIRI(iri);
		
		log.debug("Removing "+iri);
		
		try {
			
			// Split into namespace and local name.
			int ndx = iri.indexOf('#');	
			String namespace, localName;
			if(ndx == -1) {
				
				// The # is part of the namespace, so this should usually never happen.
				namespace = iri+'#';
				localName = "";
			} else {
				
				// Split on the # sign, keeping the # in the namespace.
				namespace = iri.substring(0,ndx+1);
				localName = iri.substring(ndx+1);
			}

			if(localName.length()>0) {

				// If we have a local name, we attempt to find the IRI in our storage, and
				// Get the namespace as well.
				TopEntity e = entitiesByIRI.get(iri);
				TopEntity[] f = entitiesByNamespace.get(namespace);
				
				// The entity is not there, so we have nothing to remove.
				if(e == null)
					return;
				
				// If this is the last item from the namespace, remove it as well.
				if(f.length==1) {
					entitiesByNamespace.remove(f);
					log.debug("Removed namespace "+namespace);
				}
			
				// Remove the entity.
				entitiesByIRI.remove(e);
				log.debug("Removed namespace "+iri);
			} else {

				// Get the namespace entities.
				TopEntity[] f = entitiesByNamespace.get(namespace);
				
				// Remove all entities.
				for(TopEntity e : f) {
					entitiesByIRI.remove(e);
					log.debug("Removed entity "+e.getIdentifier().toString());
				}
				
				// Remove the namespace.
				entitiesByNamespace.remove(f);
				log.debug("Removed namespace "+namespace);
			}
		} catch(Exception ex) {
			
			// Print the exception. This is usually fairly harmless stuff, for
			// instance a URL that can't be accessed or invalid WSML format. Mostly
			// we can live with that and continue.
			log.warn("Error when trying to remove "+iri+": "+ex.toString());
			ex.printStackTrace();
		}
	}
	
	/**
	 * Get all the web services currently in the database.
	 * 
	 * @return A collection of web service objects.
	 */
	public static synchronized Collection<WebService> getAllWebServices() throws Exception {
		
		// Create a new vector of web services.
		Vector<WebService> services = new Vector<WebService>();
		Vector<String> serviceIRIs = new Vector<String>();
		
		// Query the database.
		ResultSet rs = Connection.executeQuery("select iri from service");
        while(rs.next()) {
        	
        	// Get the service IRI and add it to list.
        	serviceIRIs.add(rs.getString(1));
        }
        rs.close();
        
        for(String iri : serviceIRIs) {
        	
        	// Load the service and add it to the output list.
        	Collection<TopEntity> e = getEntities(iri,WSMLStore.SERVICE);
        	if(e != null)
        		services.add((WebService)e.iterator().next());        	
        }
		
		return services;
	}
	
	/**
	 * Creates a new WSML parser.
	 * 
	 * @return The parser.
	 */
	private static Parser getParser() {
		
		// Creation parameters for the parser
		HashMap<String, Object> createParams = new HashMap<String, Object>();
		createParams.put(Factory.PROVIDER_CLASS,
				"org.deri.wsmo4j.factory.LogicalExpressionFactoryImpl");
		LogicalExpressionFactory leFactory = (LogicalExpressionFactory) Factory
				.createLogicalExpressionFactory(createParams);
		WsmoFactory factory = Factory.createWsmoFactory(null);
		createParams = new HashMap<String, Object>();
		createParams.put(Factory.WSMO_FACTORY, factory);
		createParams.put(Factory.LE_FACTORY, leFactory);

		// Create parser.
//		return new ParserImpl(Factory.createParser(createParams);
		return new ParserImpl(new HashMap<String,String>());
	}
	
	/**
	 * Imports an entity from a URL.
	 * @param url The URL from where to load the entity. 
	 * @throws Exception
	 */
	public static TopEntity[] importWSMLFromURL(String url) throws Exception {
		
		return importWSMLFromURL(url, false);
	}
		
	/**
	 * Imports an entity from a URL.
	 * 
	 * @param url The URL from where to load the entity. 
	 * @param temp If true, do not store entity in database.
	 * @return An array of TopEntity containing the entities in the WSML file. 
	 * @throws Exception
	 */
	public static TopEntity[] importWSMLFromURL(String url, boolean temp) throws Exception {
		
		// Change iri as required.
		url = WSMLStore.fixIRI(url);
		log.debug("Loading from URL "+url);
		
		// Read the WSML text from the file.
		BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		StringBuffer buffer = new StringBuffer();
		String line;
		while((line = r.readLine()) != null) {
			buffer.append(line);
			buffer.append("\n");
		}
		r.close();
		
		return importWSMLFromString(buffer, temp);
	}
	
	
	/**
	 * Imports an entity from a string.
	 * 
	 * @param buffer The string buffer containing the WSML text.
	 * @param temp If true, do not store entity in database.
	 * @return An array of TopEntity containing the entities in the WSML file. 
	 * @throws Exception
	 */
	public static void importTopEntities(TopEntity[] entities, boolean temp) throws Exception {

		// If empty, just give up.
		if(entities == null)
			return;
		
		// Get the namespace from the entities.
		//String namespace = entities[0].getDefaultNamespace().getIRI().getNamespace();		
		String namespace = ((IRI)entities[0].getIdentifier()).getNamespace();
		String defNamespace = entities[0].getDefaultNamespace().getIRI().toString();

		if(namespace == null || (!namespace.equals(defNamespace)))
			namespace = defNamespace;
		else {
			for(TopEntity e : entities) {
				if(!namespace.equals(((IRI)e.getIdentifier()).getNamespace())) {
					log.error("Two distinct namespaces found in single WSML file: "+namespace+" and "+((IRI)e.getIdentifier()).getNamespace());
					throw new Exception("Two distinct namespaces found in single WSML file: "+namespace+" and "+((IRI)e.getIdentifier()).getNamespace());
				}
			}
		}
		
		// Add the entities to the loaded set.
		entitiesByNamespace.put(namespace, entities);
		for(TopEntity t : entities)
			entitiesByIRI.put(t.getIdentifier().toString(), t);		

		// If this is a temporary entity or the database is not available, just return it now.
		if(temp || (!Connection.available()))
			return;
		
		// Store the WSML file in the database.
		log.debug("Storing WSML file "+namespace);
		
		PreparedStatement ps = Connection.prepareStatement("insert into wsmlfile (namespace, contents) values ('"+namespace+"', ?)");
		ps.setString(1,Helper.serializeTopEntity(entities[0]));
		ps.execute();
		
		// Recover the id.
		ResultSet rs = Connection.executeQuery("select id_wsmlfile from wsmlfile where namespace like '"+namespace+"'");
        if(!rs.next())
        	throw new Exception("Failed database insertion");
        int wsmlId = rs.getInt(1);
        rs.close();
        log.debug("Added WSML "+ namespace +" id: "+wsmlId);
        
        // Store service identifiers as they are found.
        HashMap<Integer,WebService> services = new HashMap<Integer,WebService>();
        
        // Insert all the other top entities into the appropriate tables.
        for(TopEntity e : entities) {

        	// We are not interested in everything. Keep that which we like.
        	if(Ontology.class.isAssignableFrom(e.getClass())) {
        		
        		// Import ontology things, like concepts, quality parameters, and 
        		// measurement units.
        		importOntology((Ontology)e, wsmlId);
        	} else if(Goal.class.isAssignableFrom(e.getClass())) {
        	} else if(WebService.class.isAssignableFrom(e.getClass())) {
        		
        		// Import the service and all attached interfaces.
        		int srvId = importService((WebService)e, wsmlId);
        		services.put(new Integer(srvId), (WebService)e);
        	}         	
        }
        
        // If we have loaded any new services, we actually need to make sure that any
        // referenced QoS ontologies are also around in order to store the advertised
        // values.
        for(Map.Entry<Integer,WebService> e : services.entrySet()) {

        	// Import the service and all attached interfaces.
       		importServiceParameters(e.getValue(), e.getKey().intValue());
        }
        
	}
	
	/**
	 * Imports an entity from a string.
	 * 
	 * @param buffer The string buffer containing the WSML text.
	 * @param temp If true, do not store entity in database.
	 * @return An array of TopEntity containing the entities in the WSML file. 
	 * @throws Exception
	 */
	public static TopEntity[] importWSMLFromString(StringBuffer buffer, boolean temp) throws Exception {

		// Read the Entities from the buffer.
		TopEntity[] entities = getParser().parse(buffer);
		
		// If that returned nothing, just give up.
		if(entities == null)
			return null;
		
		// Get the namespace from the entities.
		//String namespace = entities[0].getDefaultNamespace().getIRI().getNamespace();		
		String namespace = ((IRI)entities[0].getIdentifier()).getNamespace();
		String defNamespace = entities[0].getDefaultNamespace().getIRI().toString();

		if(namespace == null || (!namespace.equals(defNamespace)))
			namespace = defNamespace;
		else {
			for(TopEntity e : entities) {
				if(!namespace.equals(((IRI)e.getIdentifier()).getNamespace())) {
					log.error("Two distinct namespaces found in single WSML file: "+namespace+" and "+((IRI)e.getIdentifier()).getNamespace());
					throw new Exception("Two distinct namespaces found in single WSML file: "+namespace+" and "+((IRI)e.getIdentifier()).getNamespace());
				}
			}
		}
		
		// Add the entities to the loaded set.
		entitiesByNamespace.put(namespace, entities);
		for(TopEntity t : entities)
			entitiesByIRI.put(t.getIdentifier().toString(), t);		

		// If this is a temporary entity or the database is not available, just return it now.
		if(temp || (!Connection.available()))
			return entities;
		
		// Store the WSML file in the database.
		log.debug("Storing WSML file "+namespace);
		PreparedStatement ps = Connection.prepareStatement("insert into wsmlfile (namespace, contents) values ('"+namespace+"', ?)");
		ps.setString(1,buffer.toString());
		ps.execute();
		
		// Recover the id.
		ResultSet rs = Connection.executeQuery("select id_wsmlfile from wsmlfile where namespace like '"+namespace+"'");
        if(!rs.next())
        	throw new Exception("Failed database insertion");
        int wsmlId = rs.getInt(1);
        rs.close();
        log.debug("Added WSML "+ namespace +" id: "+wsmlId);
        
        // Store service identifiers as they are found.
        HashMap<Integer,WebService> services = new HashMap<Integer,WebService>();
        
        // Insert all the other top entities into the appropriate tables.
        for(TopEntity e : entities) {

        	// We are not interested in everything. Keep that which we like.
        	if(Ontology.class.isAssignableFrom(e.getClass())) {
        		
        		// Import ontology things, like concepts, quality parameters, and 
        		// measurement units.
        		importOntology((Ontology)e, wsmlId);
        	} else if(Goal.class.isAssignableFrom(e.getClass())) {
        	} else if(WebService.class.isAssignableFrom(e.getClass())) {
        		
        		// Import the service and all attached interfaces.
        		int srvId = importService((WebService)e, wsmlId);
        		services.put(new Integer(srvId), (WebService)e);
        	}         	
        }
        
        // If we have loaded any new services, we actually need to make sure that any
        // referenced QoS ontologies are also around in order to store the advertised
        // values.
        for(Map.Entry<Integer,WebService> e : services.entrySet()) {

        	// Import the service and all attached interfaces.
       		importServiceParameters(e.getValue(), e.getKey().intValue());
        }
        
        // Return the loaded entities.
        return entities;
	}
	
	/**
	 * Get the database ID of a concept from its IRI.
	 * 
	 * @param iri The IRI of the concept to find.
	 * @return The identifier of the concept.
	 * @throws Exception
	 */
	private static int getConceptID(String iri) throws Exception {
		
		// Recover the id.
		ResultSet rs = Connection.executeQuery("select id_concept from concept where iri like '"+iri+"'");
        if(!rs.next())
        	throw new Exception("Concept "+iri+" not found in database.");
        int conceptId = rs.getInt(1);
        rs.close();
        
        return conceptId;
	}
	
	/**
	 * Get the database ID of a concept from its IRI.
	 * 
	 * @param iri The IRI of the concept to find.
	 * @return The identifier of the concept.
	 * @throws Exception
	 */
	private static int getUnitID(String iri) throws Exception {
		
		// Recover the id.
		ResultSet rs = Connection.executeQuery("select id_unit from measurementunit where iri like '"+iri+"'");
        if(!rs.next())
        	throw new Exception("Unit "+iri+" not found in database.");
        int conceptId = rs.getInt(1);
        rs.close();
        
        return conceptId;
	}
	
	/**
	 * Get the database ID of an interface from its IRI.
	 * 
	 * @param iri The IRI of the interface to find.
	 * @return The identifier of the interface.
	 * @throws Exception
	 */
	private static int getInterfaceID(String iri) throws Exception {
		
		// Recover the id.
		ResultSet rs = Connection.executeQuery("select id_interface from interface where iri like '"+iri+"'");
        if(!rs.next())
        	throw new Exception("Interface "+iri+" not found in database.");
        int interfaceId = rs.getInt(1);
        rs.close();
        
        return interfaceId;
	}
	
	/**
	 * Import items from the ontology into the database. Also performs the queries
	 * required in order to locate QoS parameters and units.
	 * 
	 * @param ont The ontology from which to retrieve elements.
	 * @param wsmlId The indentfier of the wsml file in the database.
	 * @return The identifier of the ontology in the database.
	 * @throws Exception
	 */
	private static int importOntology(Ontology ont, int wsmlId) throws Exception {
		
		// Attempt to obtain description of ontology from NFPs.
		IRI ontologyIRI = (IRI)ont.getIdentifier();		
		String description = "";
		for(Object o : ont.listNFPValues().entrySet()) {
			
			Map.Entry e = (Map.Entry)o;
			if(((IRI)e.getKey()).getLocalName().equals("qosdefinition")) {
				
				// The NFP was found, keep only the first value.
				Set values = (Set)e.getValue();
				if(values.size()>=1) {
					description = values.iterator().next().toString();
				}
			}
		}

		// Insert the ontology.
		Connection.execute("insert into ontology (id_wsmlfile, iri, localname, description) values ("+wsmlId+", '"+ontologyIRI.toString()+"', '"+ontologyIRI.getLocalName()+"', '"+description+"')");
		
		// Recover the id.
		ResultSet rs;
		rs = Connection.executeQuery("select id_ontology from ontology where iri like '"+ontologyIRI.toString()+"'");
        if(!rs.next())
        	throw new Exception("Failed database insertion");
        int ontId = rs.getInt(1);
        rs.close();
        log.debug("Added Ontology "+ontologyIRI.getLocalName()+"("+description+") id: "+ontId);
		
        // Now insert all the concepts.
        for(Iterator it = ont.listConcepts().iterator();it.hasNext();) {
        	
        	// Get the concept.
            Concept concept = (Concept)it.next();
    		IRI conceptIRI = (IRI)concept.getIdentifier();
    		Connection.execute("insert into concept (id_ontology, iri, localname) values ("+ontId+", '"+conceptIRI.toString()+"', '"+conceptIRI.getLocalName()+"')");
    		
    		// Recover the id.
    		rs = Connection.executeQuery("select id_concept from concept where iri like '"+conceptIRI.toString()+"'");
            if(!rs.next())
            	throw new Exception("Failed database insertion");
            int conceptId = rs.getInt(1);
            rs.close();
            log.debug("Added Concept "+conceptIRI.getLocalName()+" id: "+conceptId);
        }
        
        // Next we want to perform a few queries in order to discover the useful elements
        // from within the ontology. Create a reasoner and add the ontology we are loading.       
		Reasoner rc = new Reasoner();
		rc.addOntology(ontologyIRI.toString());
        
        
        // The first step is to find all the units.
		Vector<Map<String,Term>> results;
		String query = "_\""+qosRootOntology+"#SelectUnit\"(?x,?y,?z)";
		results = rc.execute("_\""+qosRootOntology+"#SelectUnit\"(?x,?y,?z)");
		for(Map<String,Term> result : results) {
			
			// The results come as parameter, comparison pairs.
			try {
				IRI unitIRI = (IRI)result.get("?x");
				IRI unitConceptIRI = (IRI)result.get("?y");
				double conv = Double.parseDouble(rc.termToString(result.get("?z")));

				if(unitIRI.getNamespace().equals(ontologyIRI.getNamespace())) {

					// Get the concept IDs for the IRIs.
					int unitConceptId = getConceptID(unitConceptIRI.toString());
					
					// Now put it into the database
					Connection.execute("insert into measurementunit (id_concept, iri, localname, conversion) values ("+unitConceptId+", '"+unitIRI.toString()+"', '"+unitIRI.getLocalName()+"', "+conv+")");
					log.debug("Added unit "+unitIRI.getLocalName()+" ("+unitConceptIRI.getLocalName()+" = "+conv+")");
				}
			} catch(Exception ex) {
				
				// Should not happen in normal situations. However, the query might return
				// some weird results in some cases, and we do not want to completely fail
				// because of that.
				log.warn("Unexpected error on measurement unit query: "+ex.getMessage());
			}
		}
		
        // The second step is to find all the Quality parameters.
		for(int isEnv = 0; isEnv <= 1; ++isEnv) {

			if(isEnv == 0)
				results = rc.execute("_\""+qosRootOntology+"#SelectQuality\"(?x,?y,?z)");
			else
				results = rc.execute("_\""+qosRootOntology+"#SelectEnvironment\"(?x,?y,?z)");
			for(Map<String,Term> result : results) {
				
				// The results come as parameter, comparison pairs.
				try {
					IRI parameterIRI = (IRI)result.get("?x");
					IRI comparisonIRI = (IRI)result.get("?y");
					IRI unitIRI = (IRI)result.get("?z");
					
					if ( !(comparisonIRI.toString().contains("HigherBetter") || comparisonIRI.toString().contains("LowerBetter") ))
						continue;
					
					int parameterId = getConceptID(parameterIRI.toString());
					
					// Also try to find the absolute minima and maxima.
					Double absMin = null, absMax = null, defaultValue = null;
					Vector<Map<String,Term>> sresults;
					sresults = rc.execute("_\""+qosRootOntology+"#QualityRangeMin\"(_\""+parameterIRI.toString()+"\",?y)");
					if(sresults.size() > 0)					
						absMin = new Double(rc.termToString(sresults.elementAt(0).get("?y")));
					sresults = rc.execute("_\""+qosRootOntology+"#QualityRangeMax\"(_\""+parameterIRI.toString()+"\",?y)");
					if(sresults.size() > 0)					
						absMax = new Double(rc.termToString(sresults.elementAt(0).get("?y")));
					sresults = rc.execute("_\""+qosRootOntology+"#QualityRangeDefault\"(_\""+parameterIRI.toString()+"\",?y)");
					if(sresults.size() > 0)
						defaultValue = new Double(rc.termToString(sresults.elementAt(0).get("?y")));
		
					if(parameterIRI.getNamespace().equals(ontologyIRI.getNamespace())) {
	
						// Get the concept IDs for the IRIs.
						int comparisonId = getConceptID(comparisonIRI.toString());
						int unitId = getConceptID(unitIRI.toString());
						
						// Now put it into the database
						Connection.execute("insert into qosparameter (id_concept, id_comparison, id_unit, vl_absmin, vl_absmax, vl_defaultvalue, vl_isenvironment) "+
								"values ("+parameterId+", "+comparisonId+", "+unitId+", "+(absMin==null?"null":absMin.toString())+
								", "+(absMax==null?"null":absMax.toString())+", "+(defaultValue==null?"null":defaultValue.toString())+
								", "+isEnv+")");
						log.debug("Added parameter "+parameterIRI.getLocalName()+" ("+comparisonIRI.getLocalName()+" of "+unitIRI.getLocalName()+") "+(isEnv!=0 ? "Environment" : "Quality"));
						if(defaultValue!=null)
							log.debug("Default value is "+defaultValue.toString());
					}
					
					if(description.length()>0) {
						// Create the parameter group entry as well
						Connection.execute("insert into qosparametergroup (id_parameter, id_ontology) values ("+parameterId+", "+ontId+")");
						log.debug("Added parameter group entry "+parameterIRI.getLocalName()+" for "+description+"("+ontologyIRI.getLocalName()+")");
					}
					
				} catch(Exception ex) {
					
					// Should not happen in normal situations. However, the query might return
					// some weird results in some cases, and we do not want to completely fail
					// because of that.
					log.warn("Unexpected error on QoS parameter query: "+ex.getMessage());
				}
			}
		}

		// Finally clean up the reasoning context.
		rc.clean();
		
		// Return the ontology identifier.
		return ontId;
	}
	
	/**
	 * Import items from the web service into the database. 
	 * 
	 * @param srv The service from which to retrieve elements.
	 * @param wsmlId The indentfier of the wsml file in the database.
	 * @return The identifier of the service in the database.
	 * @throws Exception
	 */
	private static int importService(WebService srv, int wsmlId) throws Exception {
		
		// Insert the ontology.
		IRI serviceIRI = (IRI)srv.getIdentifier();
		Connection.execute("insert into service (id_wsmlfile, iri, localname) values ("+wsmlId+", '"+serviceIRI.toString()+"', '"+serviceIRI.getLocalName()+"')");
		
		// Recover the id.
		ResultSet rs = Connection.executeQuery("select id_service from service where iri like '"+serviceIRI.toString()+"'");
        if(!rs.next())
        	throw new Exception("Failed database insertion");
        int srvId = rs.getInt(1);
        rs.close();
        log.debug("Added Service "+serviceIRI.getLocalName()+" id: "+srvId);
		
        // Now insert all the concepts.
        for(Iterator it = srv.listInterfaces().iterator();it.hasNext();) {
        	
        	// Get the concept.
            Interface iface = (Interface)it.next();
    		IRI ifaceIRI = (IRI)iface.getIdentifier();
    		Connection.execute("insert into interface (id_service, iri, localname) values ("+srvId+", '"+ifaceIRI.toString()+"', '"+ifaceIRI.getLocalName()+"')");
    		
    		// Recover the id.
    		rs = Connection.executeQuery("select id_interface from interface where iri like '"+ifaceIRI.toString()+"'");
            if(!rs.next())
            	throw new Exception("Failed database insertion");
            int ifaceId = rs.getInt(1);
            rs.close();
            log.debug("Added Interface "+ifaceIRI.getLocalName()+" id: "+ifaceId);
        }
        
        // Return service identifier.
        return srvId;
	}
	
	/**
	 * Import parameters related to a web service.
	 * 
	 * @param src The web service.
	 * @param srvId The identifier of the web service.
	 * @throws Exception
	 */
	private static void importServiceParameters(WebService srv, int srvId) throws Exception {
		
		// Walk through the interfaces exposed by the service.
		for(Object i : srv.listInterfaces()) {
			
			// Get the interface identifier and imported ontologies.
			Interface iface = (Interface)i;
			int iid = getInterfaceID(iface.getIdentifier().toString());
			Set onts = ((Interface)i).listOntologies();

			// Next we want to perform a few queries in order to discover the useful elements
	        // from within the ontology. Create a reasoner and add the ontologies we are loading.       
			Reasoner rc = new Reasoner();
			for(Object o : onts)
				rc.addOntology(((Ontology)o).getIdentifier().toString());
	        
//			System.out.println(Helper.serializeTopEntity(rc.));
			
	        // Find all the Quality parameters.
			Vector<Map<String,Term>> results = rc.execute("_\""+qosRootOntology+"#ServiceSpecList\"(?z,?v)");
			for(Map<String,Term> result : results) {
				
				try {
					
					// Get the parameter id and value.
					IRI paramIRI = (IRI)result.get("?z");
					int paramId = getConceptID(paramIRI.toString());
					String vStr = rc.termToString(result.get("?v"));
					double value = Double.parseDouble(vStr);
					String cmpOp = getComparisonOperator(paramIRI.toString());
						
					// Now put it into the database
					Timestamp now = new Timestamp(System.currentTimeMillis());
					if(cmpOp.equals("HigherBetter"))
						Connection.execute("insert into advertisedvalue (id_parameter, id_interface, dt_timestart, dt_timeend, vl_lowerbound, vl_higherbetter) values ("+paramId+", "+iid+", '"+now+"', '"+now+"', "+value+", 1)");						
					else
						Connection.execute("insert into advertisedvalue (id_parameter, id_interface, dt_timestart, dt_timeend, vl_upperbound, vl_higherbetter) values ("+paramId+", "+iid+", '"+now+"', '"+now+"', "+value+", 0)");
					
					log.debug("Added value "+paramIRI.getLocalName()+" ("+value+ ") ["+paramId+","+iid+"]");
					
				} catch(Throwable ex) {
					
					// Should not happen in normal situations. However, the query might return
					// some weird results in some cases, and we do not want to completely fail
					// because of that.
					log.warn("Unexpected error on advertised value query: "+ex.getMessage());
				}
				
			}
			
			// Finally clean up the reasoning context.
			rc.clean();			
		}
	}
	
	/**
	 * Add a new user to the database.
	 * 
	 * @param username The name of the user.
	 * @return The identifier of the user.
	 */
	public static int addUser(String username) throws Exception {
		
		// Insert the user.
		Connection.execute("insert into qosuser (name) values ('"+username+"')");
		
		// Recover the id.
		ResultSet rs = Connection.executeQuery("select id_user from qosuser where name like '"+username+"'");
        if(!rs.next())
        	throw new Exception("Failed database insertion");        
        int userId = rs.getInt(1);
        rs.close();
        log.debug("Added User "+username+" id: "+userId);
		
        // Return user identifier.
        return userId;
	}
	
	/**
	 * Hung: Add a new user with a specific real behavior to the database.
	 * 
	 * @param username The name of the user.
	 * @param behavior The behavior of the user.
	 * @return The identifier of the user.
	 */
	public static int addUser(String username, int userBehavior) throws Exception {
		String sqlString="insert into qosuser (name,ds_realCredibility) values ('"+username+"'," +
	     																		 userBehavior+")";
		
		if(userBehavior==Constants.USER_BEHAVIOR_TRUSTED)
		{
			sqlString="insert into qosuser (name,ds_estimatedCredibility,ds_realCredibility) " +
						"values ('"+username+"'," +userBehavior+","+ userBehavior+")";
		}
		
		// Insert the user.
		Connection.execute(sqlString);
		
		// Recover the id.
		ResultSet rs = Connection.executeQuery("select id_user from qosuser where name like '"+username+"'");
        if(!rs.next())
        	throw new Exception("Failed database insertion");        
        int userId = rs.getInt(1);
        rs.close();
        log.debug("Added User "+username+" id: "+userId);
		
        // Return user identifier.
        return userId;
	}
	
	
	/**
	 * Retreives all the QoS domains defined in the database.
	 * 
	 * @return A vector of QoS domains.
	 * @throws Exception
	 */
	public static synchronized Vector<QoSDomain> getQoSDomains() throws Exception {
		
		Vector<QoSDomain> domains = new Vector<QoSDomain>();
		QoSDomain current = null;
		
		// Before we start, we first collect all units.
		HashMap<Integer, Vector<QoSUnit>> units = new HashMap<Integer, Vector<QoSUnit>>(); 
		ResultSet rs = Connection.executeQuery("select id_concept, iri, localname, conversion from measurementunit order by id_concept");
        while(rs.next()) {
        	
        	// Find unit concept in hash map, create if not found.
        	Integer i = new Integer(rs.getInt(1));
        	if(!units.containsKey(i)) {
        		units.put(i,new Vector<QoSUnit>());
        	}
        	
        	// Add the unit to the hash map.
        	Vector<QoSUnit> u = units.get(i);
        	u.add(new QoSUnit(rs.getString(2),rs.getString(3),rs.getDouble(4)));
        }
        rs.close();

		// Recover all parameters and groups.
		rs = Connection.executeQuery(
			"select ontology.id_ontology, ontology.iri, ontology.description, " +
			"concept.id_concept, concept.iri, concept.localname," +
			"qosparameter.id_unit, c2.iri, qosparameter.vl_defaultvalue, qosparameter.vl_isenvironment " +
			"from qosparametergroup, ontology, concept, concept as c2, qosparameter " +
			"where qosparametergroup.id_parameter = concept.id_concept " +
			"and qosparametergroup.id_ontology = ontology.id_ontology " +
			"and qosparametergroup.id_parameter = qosparameter.id_concept " +
			"and c2.id_concept = qosparameter.id_comparison " +
			"order by ontology.iri");
		
        while(rs.next()) {
        	
        	// Create a new domain if required.
        	int ontId = rs.getInt(1);
        	if(current == null || current.id != ontId) {
        		current = new QoSDomain();
        		current.id = ontId;
        		current.iri = rs.getString(2);
        		current.description=rs.getString(3);
        		domains.add(current);
        	}
        	
        	// Create the parameter
        	QoSParameter param = new QoSParameter();
        	param.id = rs.getInt(4);
        	param.iri = rs.getString(5);
        	param.description = rs.getString(6);
        	param.units = units.get(new Integer(rs.getInt(7)));
        	param.comparator = rs.getString(8);
        	param.defaultValue = rs.getDouble(9);
        	if(rs.getInt(10)==0)
        		current.parameters.add(param);
        	else
        		current.requirements.add(param);
        }
        rs.close();        
		
		return domains;
	}

	/**
	 * Get all users from database.
	 * 
	 * @return Vector of users.
	 * @throws Exception
	 */
	public static synchronized Vector<QoSUser> getUsers() throws Exception {

		Vector<QoSUser> users = new Vector<QoSUser>();

		// Query for the users.
		ResultSet rs = Connection.executeQuery("select id_user, name, ds_realCredibility, ds_estimatedCredibility from QosUser");
		while(rs.next()) {
			
			// Store the user info.
			QoSUser u = new QoSUser();
			u.id=rs.getInt(1);
			u.name = rs.getString(2);
			u.realCredibility=rs.getInt(3);
			u.estimatedCredibility=rs.getInt(4);
			users.add(u);
		}
		rs.close();
		
		return users;
	}
	
	/**
	 * Obtain service-related information from the database.
	 * 
	 * @param iri IRI of the interface we are interested in.
	 * @param returnReports If true, all reports made on this service are also returned.
	 * @return The service information.
	 * @throws Exception
	 */
	public static synchronized QoSServiceInfo getServiceInfo(String iri, boolean returnReports) throws Exception {
		
		QoSServiceInfo si = new QoSServiceInfo();
		
		// Get interface identifier.
		si.iri = iri;
		si.id = getInterfaceID(iri);
		si.parameters = new Vector<QoSServiceParameter>();
		
		// Get all advertised parameters exposed by this interface.
		ResultSet rs = Connection.executeQuery("select id_parameter, vl_upperBound, vl_lowerBound, vl_higherBetter from advertisedvalue where " +
				"id_interface = "+si.id);
		while(rs.next()) {
			
			// Store the parameter info.
			QoSServiceParameter p = new QoSServiceParameter();
			p.id=rs.getInt(1);
			if(rs.getInt(4)==1)
				p.advertisedValue=rs.getDouble(3);
			else
				p.advertisedValue=rs.getDouble(2);
			si.parameters.add(p);
		}
		rs.close();

		// Now complete this with the estimated values if available.
		rs = Connection.executeQuery("select id_parameter, value from estimatedvalue where " +
				"id_interface = "+si.id);
		while(rs.next()) {
			
			// Store the info.
			for(QoSServiceParameter p : si.parameters) {
				if(p.id==rs.getInt(1)) {
					p.estimatedValue=rs.getDouble(2);
					break;
				}
			}
		}
		rs.close();
		
		if(returnReports) {
			
			// And finally get all the reports
			rs = Connection.executeQuery("select reportedvalue.id_parameter, vl_value, id_user, serviceusage.dt_timestart, vl_realcredibility, vl_estimatedcredibility from ReportedValue, ServiceUsage where " +
					"serviceusage.id_interface = "+si.id+" and serviceusage.id_serviceusage = reportedvalue.id_serviceusage");
			while(rs.next()) {

				QoSReport r = new QoSReport();
				r.id = rs.getInt(1);
				r.value=rs.getDouble(2);
				r.userId = rs.getInt(3);
				r.date = new java.util.Date(rs.getTimestamp(4).getTime());
				r.realCredibility=rs.getInt(5);
				r.estimatedCredibility=rs.getInt(6);
				
				si.reports.add(r);
			}
			rs.close();
		}
		
		return si;
	}
	
	/**
	 * Submit a user report on a service.
	 * 
	 * @param iri The IRI of the interface on which we report.
	 * @param reports The report to submit.
	 * @return true if successful.
	 */
	public static boolean submitReport(String iri, Vector<QoSReport> reports) {
		
		try {
			
			// First create a service usage entry.
			int iid = getInterfaceID(iri);
			QoSReport r0 = reports.elementAt(0);
			Timestamp now = new Timestamp(System.currentTimeMillis());
			Connection.execute("insert into serviceusage (id_user, id_interface, dt_timestart, dt_timeend) values ("+r0.userId+", "+iid+", '"+now+"', '"+now+"')");

			// Recover the id.
			ResultSet rs = Connection.executeQuery("select id_serviceusage from serviceusage where id_user="+r0.userId+" and id_interface="+iid+" and dt_timestart='"+now+"' and dt_timeend='"+now+"'");
	        if(!rs.next())
	        	throw new Exception("Failed database insertion");
	        int id = rs.getInt(1);
	        rs.close();
	        log.debug("Added ServiceUsage for "+iri+" id: "+id);
			
			for(QoSReport r : reports) {
				
				// Insert the report.
				Connection.execute("insert into reportedvalue (id_serviceusage, id_parameter, vl_value) values ("+id+", "+r.id+", "+(r.value)+")");				
			}
		} catch(Exception ex) {
			
			return false;
		}
		return true;
	}
	
	/**
	 * Get the expected value for a parameter from the database.
	 * 
	 * @param iid Identifier of the interface.
	 * @param paramId Identifier of the parameter.
	 * @return The expected value of the paramter, or 0 if not found.
	 */
	public static double getExpectedValue(int iid, int paramId) {

		double rval = 0;
		try {
			
			// Get the expected value for the specified parameter.
			ResultSet rs = Connection.executeQuery("select vl_expectedvalue from advertisedvalue "+
					"where id_parameter="+paramId+" and id_interface="+iid);
			if(rs.next()) 
				rval = rs.getDouble(1);
			rs.close();
			
			
		} catch(Exception ex) {
			
			log.debug(ex.getMessage());
		}
		return rval;
	}
	
	/**
	 * Get QoS parameter estimates for a given interface.
	 * 
	 * @param iri The IRI of the interface.
	 * @return Map of parameters to values.
	 */
	public static HashMap<String,Double> queryEstimates(String iri) {
		
		HashMap<String, Double> ests = new HashMap<String, Double>();
		try {
			
			int iid = getInterfaceID(iri);
			
			// Get all advertised parameters exposed by this interface.
//			ResultSet rs = Connection.executeQuery("select iri, value from concept, estimatedvalue "+
//					"where id_interface="+iid+" and concept.id_concept=estimatedvalue.id_parameter");
			
			ResultSet rs = Connection.executeQuery("select iri, vl_expectedvalue from concept, advertisedvalue "+
					"where id_interface="+iid+" and concept.id_concept=advertisedvalue.id_parameter");
			
			while(rs.next()) {
				
				// Store the parameter info.
				ests.put(rs.getString(1),new Double(rs.getDouble(2)));
			}
			rs.close();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return ests;
	}
	
	/**
	 * Get the comparison operator for the specified parameter.
	 * 
	 * @param iri IRI of the QoS parameter
	 * @return The short name of the comparison operator.
	 */
	public static String getComparisonOperator(String iri) {

		String rval = "";
		try {
			
			// Get the comparison operator for the specified parameter.
			ResultSet rs = Connection.executeQuery("select c2.iri from concept as c1, concept as c2, qosparameter "+
					"where c1.id_concept=qosparameter.id_concept and c2.id_concept=qosparameter.id_comparison "+
					"and c1.iri='"+iri+"'");
			if(rs.next()) 
				rval = rs.getString(1);
			rs.close();
			
			int ndx = rval.indexOf('#');
			if(ndx != -1)
				rval = rval.substring(ndx+1);
			
		} catch(Exception ex) {
			
			log.warn(ex.getMessage());
			ex.printStackTrace();
		}
		return rval;
	}
}
