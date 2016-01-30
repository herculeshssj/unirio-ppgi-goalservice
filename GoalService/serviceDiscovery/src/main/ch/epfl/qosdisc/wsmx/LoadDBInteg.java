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

import java.io.*;


import lhvu.qos.utils.Constants;

/**
 * Loads the initial state of the database from the provided example ontologies.
 * Additional elements can be added below in the main function.
 * 
 * @author Sebastian Gerlach & Le-Hung Vu
 */
public class LoadDBInteg {
	
	/**
	 * Reloads all databases from scratch.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Load the properties.
		PropertySet.setup("c:/wsmx/resources/qosdiscovery");
		
		// Set to true to delete old database prior to starting.
		boolean fullReset = false;
		
		// Destroy exisiting database.
		if(fullReset) {
			String dbName = PropertySet.getPath()+Connection.name;
			LoadDatabase.deleteDir(new File(dbName));
		}
        
		// Now build the new database.
		try {
			PropertySet.props.setProperty("db.create", "true");
			
//			 Open connection.
			Connection.open(PropertySet.props);

			// Execute create table script.
			if(fullReset){
				Connection.executeFile("c:/wsmx/resources/qosdiscovery/dbinit/clear.txt");
				Connection.executeFile("c:/wsmx/resources/qosdiscovery/dbinit/tables.txt");
			} else
				Connection.executeFile("c:/wsmx/resources/qosdiscovery/dbinit/clear.txt");
			
			// Store base QoS & ranking ontologies 
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/BankInter-Lite/Location.wsml");
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/Lite/QoSBase.wsml");
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/Common/Ranking.wsml");

			//Load domain upper ontologies for QoS
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/BankInter-Lite/StockMarketQoSBase.wsml");
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/Lite/FileQoSBase.wsml");
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/Lite/HotelQoSBase.wsml");
			

			//Load hotel reservation services 
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/Lite/Hotel0.wsml");
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/Lite/Hotel1.wsml");
			
			//Load some file hosting service descriptions 
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/Lite/Service0.wsml");
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/Lite/Service1.wsml");
			//Generate some artificial services
//			for(int i=2;i<20;++i)
//				LiteServiceCreator.createService(i);

			
			//Load Stock market information services
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/BankInter-Lite/WSGetNewsXignite.wsml");
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/BankInter-Lite/WSGetQuoteBankinter.wsml");
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/BankInter-Lite/WSGetNewsBankinter.wsml");
			//One more:
			WSMLStore.importWSMLFromURL("file:///$(local)ontologies/BankInter-Lite/WSExecuteIfQuotationMoreThan.wsml");
			
			
			// Run QoS data preparation
			//ReputationDataPreparation.runWithin(fullReset);

			// Modified by Hung: Add users after generating some of thems (with trusted & honest users included) 
			// Add some more users. Name origin: most popular names for births in 1880 in 
			// the USA, the first year for which statistics are available on the SSA web site.
			int[] uid = new int[6];
			uid[0] = WSMLStore.addUser("Anna",Constants.USER_BEHAVIOR_TRUSTED);
			uid[1] = WSMLStore.addUser("Emma");
			uid[2] = WSMLStore.addUser("James");
			uid[3] = WSMLStore.addUser("John");
			uid[4] = WSMLStore.addUser("Mary");
			uid[5] = WSMLStore.addUser("William");

			
			// Create Catalog for Codims
			//CatalogManager.setup();
			
			// Close database connection.
			Connection.close();
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
		}
	}

}
