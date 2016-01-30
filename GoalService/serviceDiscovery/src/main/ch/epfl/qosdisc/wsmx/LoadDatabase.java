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

package ch.epfl.qosdisc.wsmx;

import ch.epfl.qosdisc.database.*;
import ch.epfl.qosdisc.operators.*;
import ch.epfl.qosdisc.repmgnt.*;

//import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.commons.Helper;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import lhvu.qos.utils.Constants;

import org.apache.log4j.Logger;
import org.omwg.ontology.Ontology;
import org.wsmo.common.IRI;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

/**
 * Loads the initial state of the database from the provided example ontologies.
 * Additional elements can be added below in the main function.
 * 
 * @author Sebastian Gerlach
 */
public class LoadDatabase {
	
	static Logger log = Logger.getLogger(LoadDatabase.class);
	
    /**
     * Deletes a directory.
     * 
     * @param dir The name of the directory to delete.
     * @return true on success.
     */
    public static boolean deleteDir(File dir) {
    	
    	// If this is a directory, recursively delete all its contents.
        if (dir.isDirectory()) {
        	
        	// Get all contained files.
            String[] files = dir.list();
            
            // And delete them.
            for (int i=0; i<files.length; i++) {

            	if(!deleteDir(new File(dir, files[i])))
                	return false;
            }
        }
    
        // Delete the directory now that it is empty.
        return dir.delete();
    }

    /**
	 * Reloads all databases from scratch.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
        
		// Load the properties.
		PropertySet.setup(System.getProperty("user.dir")+File.separator+"resources"+File.separator+"qosdiscovery");
		
//		PropertySet.setup(".");

		// Set to true to delete old database prior to starting.
		boolean fullReset = true;
		
		// Destroy exisiting database
//		if(fullReset) {
//			String dbName = PropertySet.getPath()+Connection.name;
//			deleteDir(new File(dbName));
//		}
        
		// Now build the new database.
		try {
			PropertySet.props.setProperty("db.create", "true");
			Properties p = PropertySet.props;
			
//			 Open connection.
			Connection.open(PropertySet.props);
			
			// Execute create table script.
			if(fullReset){
				Connection.executeFile("dbinit/clear.txt");
				Connection.executeFile("dbinit/tables.txt");
			} else
				Connection.executeFile("dbinit/clear.txt");
			
			Set<Ontology> ontos = Helper.getAllOntologies();
			WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
			IRI qosIRI = wsmoFactory.createIRI("http://www.wsmo.org/discovery/qos");
			
			Map<String, TopEntity[]> nsTopEnt = Helper.getAllNamespaceTopEntities();
			WSMLStore.importTopEntities(nsTopEnt.remove("file:///c:/WSMX/resources/qosdiscovery/ontologies/Common/QoSBase.wsml#"),false);
			WSMLStore.importTopEntities(nsTopEnt.remove("file:///c:/WSMX/resources/qosdiscovery/ontologies/Common/Ranking.wsml#"),false);
			
			TopEntity[] te = null;
			
			//optional SUPER Nexcom use-case
			if ((te=nsTopEnt.remove("http://www.super-ip.org/ontologies/nexcom/VoIPQoSBase#")) != null) WSMLStore.importTopEntities(te,false);
			if ((te=nsTopEnt.remove("http://www.super-ip.org/services/nexcom/WSWholesaleSupplier1#")) != null) WSMLStore.importTopEntities(te,false);
			if ((te=nsTopEnt.remove("http://www.super-ip.org/services/nexcom/WSWholesaleSupplier2#")) != null) WSMLStore.importTopEntities(te,false);
			if ((te=nsTopEnt.remove("http://www.super-ip.org/services/nexcom/WSWholesaleSupplier3#")) != null) WSMLStore.importTopEntities(te,false);
			if ((te=nsTopEnt.remove("http://www.super-ip.org/services/nexcom/WSWholesaleSupplier4#")) != null) WSMLStore.importTopEntities(te,false);
			if ((te=nsTopEnt.remove("http://www.super-ip.org/services/nexcom/WSWholesaleSupplier5#")) != null) WSMLStore.importTopEntities(te,false);
			if ((te=nsTopEnt.remove("http://www.super-ip.org/services/nexcom/WSWholesaleSupplier6#")) != null) WSMLStore.importTopEntities(te,false);
			
			//get entities by their namespaces
			for ( Entry<String, TopEntity[]> e : nsTopEnt.entrySet())
			{
				for (TopEntity topEnt: e.getValue()){
					Set theNFPs = topEnt.listNFPValues(qosIRI);
					if (topEnt instanceof Ontology && !theNFPs.isEmpty() && theNFPs.iterator().next().toString().toLowerCase().equals("true")) {
						WSMLStore.importTopEntities(e.getValue(), false);
						break;
					}
				}
			}
		
			// Store base ontologies.
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Common/QoSBase.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Common/Ranking.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Common/Location.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Common/StockMarketQoSBase.wsml");
//			
//			//common domain ontologies
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/bankinter/StockMarket.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/bankinter/StockMarketProcess.wsml");
//			
//			//domain QoS and ranking ontologies
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/bankinter/SWSQoS/WS-QoS1.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/bankinter/SWSQoS/WS-QoS2.wsml");
//			
//			//store goal ontologies as well
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/bankinter/GoalsQoS/Goal-QoS1.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/bankinter/GoalsQoS/Goal-QoS2.wsml");
//			
//			//Web service
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/bankinter/SWS/WSGetCurrencyRateStrikeIron.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/bankinter/SWS/WSGetCurrencyRateXignite.wsml");
			
			
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Lite/FileQoSBase.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Lite/HotelQoSBase.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Lite/Hotel0.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Lite/Service0.wsml");
//			WSMLStore.importWSMLFromURL("file:///$(local)/ontologies/Lite/Service1.wsml");
//			for(int i=2;i<20;++i)
//				LiteServiceCreator.createService(i);
			
			// Add some users. Name origin: most popular names for births in 1880 in 
			// the USA, the first year for which statistics are available on the SSA web site.
//			int[] uid = new int[6];
//			uid[0] = WSMLStore.addUser("Anna");
//			uid[1] = WSMLStore.addUser("Emma");
//			uid[2] = WSMLStore.addUser("James");
//			uid[3] = WSMLStore.addUser("John");
//			uid[4] = WSMLStore.addUser("Mary");
//			uid[5] = WSMLStore.addUser("William");

//			System.setProperty("user.dir", "C:/WSMX/resources/qosdiscovery");
			
			// Run QoS data preparation
			ReputationDataPreparation.runWithin(fullReset);
			
			int[] uid = new int[6];
			uid[0] = WSMLStore.addUser("Anna",Constants.USER_BEHAVIOR_TRUSTED);
			uid[1] = WSMLStore.addUser("Emma");
			uid[2] = WSMLStore.addUser("James");
			uid[3] = WSMLStore.addUser("John");
			uid[4] = WSMLStore.addUser("Mary");
			uid[5] = WSMLStore.addUser("William");
			
			// Create Catalog for Codims
//			CatalogManager.setup();
			
			// Close database connection.
			Connection.close();
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
		}
	}

	
	
}
