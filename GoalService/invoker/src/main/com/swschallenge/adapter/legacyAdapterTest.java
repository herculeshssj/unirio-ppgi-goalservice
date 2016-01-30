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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  */

package com.swschallenge.adapter;


import ie.deri.wsmx.commons.Helper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.omwg.ontology.Instance;
import org.wsmo.common.Entity;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.factory.Factory;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;


/**
 * Test the SWS Challenge adapters - replace the fileWithWSMLInstance string with the location of the
 * file conatining the WSML instance to be tested
 *
 * <pre>
 * Created on 03-May-2006
 * Committed by $Author: maciejzaremba $
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/com/swschallenge/adapter/legacyAdapterTest.java,v $,
 * </pre>
 *
 * @author Matthew Moran
 *
 * @version $Revision: 1.14 $ $Date: 2007/12/13 16:48:52 $
 */

public class legacyAdapterTest extends TestCase {

	private Factory wsmoFactory;

	static public void main(String[] args) {
		legacyAdapterTest l = new legacyAdapterTest();
		String fileWithWSMLInstance = ""; 
		fileWithWSMLInstance = "C:\\DERI\\SWSChallenge\\ontologies\\instances\\searchCustomerInstance.wsml"; 
		fileWithWSMLInstance = "C:\\DERI\\SWSChallenge\\ontologies\\instances\\createNewOrder.wsml";
		fileWithWSMLInstance = "C:\\DERI\\SWSChallenge\\ontologies\\instances\\addNewLineItem.wsml";
		fileWithWSMLInstance = "C:\\DERI\\SWSChallenge\\ontologies\\instances\\closeOrderInstance.wsml";
		
		l.testGetWSML(fileWithWSMLInstance);
	}
	
	/*
	 * Test method for 'com.swschallenge.adapter.legacyAdapter.getWSML(String)'
	 */
	public void testGetWSML(String testWSMLFile) {
		TopEntity[] topEntities = null;
		List<Entity> inputInstances;
		
        // Load the background ontology
		try {
			Parser parser = wsmoFactory.createParser(new HashMap<String, Object>());
		    topEntities = parser.parse(new FileReader(testWSMLFile));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		inputInstances = Helper.getInstances(topEntities);
		LegacyAdapter adapter = new LegacyAdapter();
		adapter.getXML((Instance)inputInstances.get(0));
	}

	/*
	 * Test method for 'com.swschallenge.adapter.legacyAdapter.legacyAdapter(String)'
	 */
	public void testLegacyAdapterString() {

	}

	/*
	 * Test method for 'com.swschallenge.adapter.legacyAdapter.legacyAdapter()'
	 */
	public void testLegacyAdapter() {

	}

	/*
	 * Test method for 'com.swschallenge.adapter.legacyAdapter.getXML(Instance)'
	 */
	public void testGetXML() {

	}

}

