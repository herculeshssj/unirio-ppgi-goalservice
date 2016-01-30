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
package ie.deri.wsmx.discovery.dummy;

import ie.deri.wsmx.discovery.*;

import java.util.*;

import org.apache.log4j.*;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.execution.common.exception.*;
import org.wsmo.service.*;

import com.ontotext.wsmo4j.common.*;

public class DummyWebServiceDiscovery {
	
	String[][] pairs = { {"http://deri.org/dip/goal/demoVTA1#GoalMediateTime1","http://deri.org/demoVTA/WSMediateTime"},
						 {"http://deri.org/dip/goal/demoVTA2#GoalMediateTime2","http://deri.org/demoVTA/WSMediateTime"},
						 {"http://www.wsmx.org/ontologies/rosetta/MoonGoal","http://www.example.org/ontologies/sws-challenge/MoonWS"},
						 {"http://www.wsmx.org/ontologies/rosetta/MoonGoal2","http://www.example.org/ontologies/sws-challenge/MoonWS2"},
						 {"http://www.ip-super.org/ontologies/prereview#goalGetURL","http://www.wsmo.org/ws/wsGenerateURL#wsGenerateURL"},
						 {"http://www.example.org/TestCreateBothGoal_UoM","http://swing.brgm.fr/repository/webservices_fto/DEVmanual-UC1-ASM/current#ConsumptionProductionMap"},
						 {"http://swing.brgm.fr/repository/testUc2#Uc2Goal","http://swing.brgm.fr/repository/webservices_fto/DEVmanual-UC2-ASM/current#ConstraintMapService"},
						 

						 
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfVariation","http://users.isoco.net/~slosada/ontologies/bankinter/WSExecuteIfVariation"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfRecomm","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfRecomm"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalVariationAndRecommendation","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSVariationAndRecommendation"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfQuotationMoreThan","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfQuotationMoreThan"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfValueRisesAbs","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfValueRisesAbs"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfValueDownAbs","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfValueDownAbs"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIn5Best","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIn5Best"},
      {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfIndiceMoreThan","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfIndiceMoreThan"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfIndiceRises","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfIndiceRises"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfIndiceDownAbs","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfIndiceDownAbs"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfYearRange","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfYearRange"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfDailyVolumeMoreThan","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfDailyVolumeMoreThan"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfDailyVolumeLessThan","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfDailyVolumeLessThan"},						  
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfQuotationMoreThanAndLessThan","http://users.isoco.net/~lcicurel/ontologies/bankinter/WSExecuteIfQuotationMoreThanAndLessThan"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfQuotationMoreThanAndRisesAbs","http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfQuotationMoreThanAndRisesAbs"},
	  {"http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfQuotationMoreThanAndIndiceMoreThan","http://users.isoco.net/~lcicurel/ontologies/bankinter/GoalExecuteIfQuotationMoreThanAndIndiceMoreThan"},

	  //dip review
	  
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetCurrencyRate.wsml#GoalGetCurrencyRate","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSGetCurrencyRateStrikeIron.wsml#WSGetCurrencyRateStrikeIron"},	  
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetHistoricalData.wsml#GoalGetHistoricalData","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSGetHistoricalDataXIgnite.wsml#WSGetHistoricalDataXIgnite"},
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetHistoricalDataIndex.wsml#GoalGetHistoricalDataIndex","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSGetHistoricalDataIndexXIgnite.wsml#WSGetHistoricalDataIndexXIgnite"},
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetIndex.wsml#GoalGetIndex","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSGetIndexStrikeIron.wsml#WSGetIndexStrikeIron"},	  
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetNews.wsml#GoalGetNews","file:///c:/wsmx/resources/qosdiscovery/ontologies/bankinter/SWS/WSGetNewsXignite.wsml#WSGetNewsXignite"},
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetQuote.wsml#GoalGetQuote","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSGetQuoteBankinter.wsml#WSGetQuoteBankinter"},
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetRecommendation.wsml#GoalGetRecommendation","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSGetRecommendationBankinter.wsml#WSGetRecommendationBankinter"},
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetTop.wsml#GoalGetTop","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSGetTopBankinter.wsml#WSGetTopBankinter"},
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalPerformBuySell.wsml#GoalPerformBuySell","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSPerformBuySellBankinter.wsml#WSPerformBuySellBankinter"},
	  {"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalSendAlert.wsml#GoalSendAlert","file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/WSSendAlertStrikeIron.wsml#WSSendAlertStrikeIron"},
	  
	  };
	  
	protected static Logger logger = Logger.getLogger(DummyWebServiceDiscovery.class);
	Map <String,IRI> map = new HashMap<String,IRI>();  
	
    public DummyWebServiceDiscovery() {
		super();
		for (int i=0; i < pairs.length; i++){
//			System.out.println(pairs[i][0]+ " -  - " +pairs[i][1]);
			map.put(pairs[i][0].toLowerCase(), new IRIImpl(pairs[i][1]));
		}
			
		
	}

    public static void main(String[] args){
    	DummyWebServiceDiscovery d = new DummyWebServiceDiscovery();
    }
    
	public List<WebService> discover(Goal goal, List<WebService> webServices) {
        List<Identifier> identifiers = new ArrayList<Identifier>();
        List<WebService> listWS = new ArrayList<WebService>();
        
        Identifier id = map.get(goal.getIdentifier().toString().toLowerCase());
        if (id != null){
        	identifiers.add(id);
        	listWS = retrieveWebService(identifiers,webServices);
        }
        return listWS;
    }

    public Map<Map<WebService, Interface>, Identifier> discover(Goal goal, Ontology rankingOntology) throws ComponentException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    //returns a list of webservices that have an identifiers from the list
    private static List<WebService> retrieveWebService(List<Identifier> identifiers, List<WebService> webServices) 
    {
        List<WebService> foundWS = new ArrayList<WebService>();

    	logger.debug("Identifiers space: ");
    	for (Identifier i : identifiers){
    		logger.error( ((IRI)i).toString());
    	}
    	
        
        for (WebService ws : webServices)
        {
        	logger.debug("WS identifiers: "+ws.getIdentifier());
        	for (Identifier i : identifiers){
        		if (i.toString().toLowerCase().equals(ws.getIdentifier().toString().toLowerCase()))
        			foundWS.add(ws);
        	}
        	
        }
        return foundWS;
    }
}