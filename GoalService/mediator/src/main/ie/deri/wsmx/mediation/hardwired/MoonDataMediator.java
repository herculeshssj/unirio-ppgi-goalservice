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

package ie.deri.wsmx.mediation.hardwired;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.core.configuration.annotation.Exposed;
import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.wsmo.common.Entity;
import org.wsmo.execution.common.component.DataMediator;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

/** * Interface or class description
 * * @author Maciej Zaremba
 * 
 * @deprecated Hardwired mediation not used any more; use mapping files instead.
 *
 * Created on 2006-05-03
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/ie/deri/wsmx/mediation/hardwired/MoonDataMediator.java,v $, * @version $Revision: 1.2 $ $Date: 2007-06-19 17:03:17 $
 */
@WSMXComponent(name = "DataMediatior",
        events = "DATAMEDIATOR")
public class MoonDataMediator implements DataMediator {
	
	static Logger logger = Logger.getLogger(DataMediator.class);

	WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
    static {
    	logger.setLevel(Level.ALL);
    }
	
	DataFactory dataFactory = Factory.createDataFactory(new HashMap<String, Object>());
	
	//hardwired data mediation
	public Ontology mediate(Ontology sourceOntology){
	try {
		 String userID = "null";
		 if (!Helper.getInstancesOfConcept(Helper.getInstances(sourceOntology), "http://www.wsmx.org/ontologies/rosetta/purchaseorderrequest#PO_v1").isEmpty())
			 userID = "MaciejZaremba"; //v.1
		 else if (!Helper.getInstancesOfConcept(Helper.getInstances(sourceOntology), "http://www.wsmx.org/ontologies/rosetta/purchaseorderrequest#PO_v2").isEmpty())
			 userID = "MaciejZaremba"; //v.2
		 
		 //create target ontology
		 Ontology targetOnto = wsmoFactory.createOntology(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/moon/target"));
		 targetOnto.setWsmlVariant("http://www.wsmo.org/wsml/wsml-syntax/wsml-rule");
		 String defaultIRI = "http://www.wsmx.org/ontologies/moon/target#";
		 targetOnto.setDefaultNamespace(wsmoFactory.createIRI(defaultIRI));
		 targetOnto.addNamespace(wsmoFactory.createNamespace("moon",wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#")));
			 
		 List<Entity> instances = Helper.getInstances(sourceOntology);

		 //create Search customer message
		 List<Entity> fromRoleInsts = Helper.getInstancesOfConcept(instances, "http://www.wsmx.org/ontologies/rosetta/coreelements#FromRole");
		 Instance fromRole = (Instance)fromRoleInsts.get(0);
		 Instance partnerRoleDescription = (Instance) fromRole.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#partnerRoleDescription")).toArray()[0];
		 Instance partnerDescription = (Instance) partnerRoleDescription.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#partnerDescription")).toArray()[0];
		 Instance businessDescription = (Instance) partnerDescription.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#businessDescription")).toArray()[0];
		 String businessName = businessDescription.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#businessName")).toArray()[0].toString();
			 
		 Instance searchCustomerRequest = wsmoFactory.createInstance(wsmoFactory.createIRI(defaultIRI+"searchReq"),
					 						  wsmoFactory.getConcept(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#SearchCustomerRequest")));
		 searchCustomerRequest.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#searchString"),
					 						  dataFactory.createWsmlString(businessName));
		 targetOnto.addInstance(searchCustomerRequest);

		 //create CreateOrderRequest Message
		 Instance createOrderRequest = wsmoFactory.createInstance(wsmoFactory.createIRI(defaultIRI+"orderReq"),
				  wsmoFactory.getConcept(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#CreateOrderRequest")));
//		 createOrderRequest.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#authToken"),
//				 dataFactory.createWsmlString(userID)); 
			 
		 Instance contactInformation = (Instance) partnerDescription.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#contactInformation")).toArray()[0];
		 
		 Instance contact = wsmoFactory.createInstance(wsmoFactory.createIRI(defaultIRI+"contactInf"),
				  wsmoFactory.getConcept(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#Contact")));
		 Instance shipToAddress = wsmoFactory.createInstance(wsmoFactory.createIRI(defaultIRI+"shipToAddr"),
				  wsmoFactory.getConcept(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#Address")));
		 Instance billToAddress = wsmoFactory.createInstance(wsmoFactory.createIRI(defaultIRI+"billToAddr"),
				  wsmoFactory.getConcept(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#Address")));
			  
		 contact.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#contactName"),
		 		  (Value)contactInformation.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#contactName")).toArray()[0]);
		 contact.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#telephone"),
		 		  (Value)contactInformation.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#telephoneNumber")).toArray()[0]);
		 contact.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#email"),
		 		  (Value)contactInformation.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#emailAddress")).toArray()[0]);
			 
		 createOrderRequest.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#contact"),contact);
			 
		 Instance purchaseOrder = (Instance)Helper.getInstancesOfConcept(instances, "http://www.wsmx.org/ontologies/rosetta/purchaseorderrequest#PurchaseOrder").get(0);
		 Instance shipTo = (Instance) purchaseOrder.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/purchaseorderrequest#shipTo")).toArray()[0];
		 Instance shipToPartnerDescription = (Instance) shipTo.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#partnerDescription")).toArray()[0];
		 Instance shipToBusinessDescription = (Instance) shipToPartnerDescription.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#businessDescription")).toArray()[0];
		 String shipToBusinessName = shipToBusinessDescription.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#businessName")).toArray()[0].toString();
			 
		 Instance shipToPhysicalLocation = (Instance) shipToPartnerDescription.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#physicalLocation")).toArray()[0];
		 Instance shipToPhysicalAddress = (Instance) shipToPhysicalLocation.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#physicalAddress")).toArray()[0];
			 
		 String shipToAddr = shipToPhysicalAddress.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#addressLine")).toArray()[0].toString();
		 String shipToCityName = shipToPhysicalAddress.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#cityName")).toArray()[0].toString();
		 String shipToGlobalCC = shipToPhysicalAddress.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#globalCountryCode")).toArray()[0].toString();
		 String shipToNationalPC = shipToPhysicalAddress.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#nationalPostalCode")).toArray()[0].toString();
			 			 
		 shipToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#businessName"),
				  dataFactory.createWsmlString(shipToBusinessName));
		 shipToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#city"),
				  dataFactory.createWsmlString(shipToCityName));			 
		 shipToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#street"),
				  dataFactory.createWsmlString(shipToAddr));
		 shipToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#countryCode"),
				  dataFactory.createWsmlString(shipToGlobalCC));
		 shipToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#postalCode"),
				  dataFactory.createWsmlString(shipToNationalPC));

		 billToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#businessName"),
				  dataFactory.createWsmlString(shipToBusinessName));
		 billToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#city"),
				  dataFactory.createWsmlString(shipToCityName));			 
		 billToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#street"),
				  dataFactory.createWsmlString(shipToAddr));
		 billToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#countryCode"),
				  dataFactory.createWsmlString(shipToGlobalCC));
		 billToAddress.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#postalCode"),
			  dataFactory.createWsmlString(shipToNationalPC));
			 
		 createOrderRequest.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#shipTo"),shipToAddress);
		 createOrderRequest.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#billTo"),billToAddress);
		 
		 //create CloseOrderRequest Message
//		 Instance closeOrderRequest = wsmoFactory.createInstance(wsmoFactory.createIRI(defaultIRI+"closeReq"),
//				  wsmoFactory.getConcept(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#CloseOrderRequest")));
//		 closeOrderRequest.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#authToken"),
//				 dataFactory.createWsmlString(userID));

		 targetOnto.addInstance(contact);
		 targetOnto.addInstance(shipToAddress);
		 targetOnto.addInstance(billToAddress);
		 targetOnto.addInstance(createOrderRequest);
//		 targetOnto.addInstance(closeOrderRequest);
			 
		 //AddLineItem(s)
		 List<Entity> productLineItems = Helper.getInstancesOfConcept(instances, "http://www.wsmx.org/ontologies/rosetta/purchaseorderrequest#ProductLineItem");
			 
		 int i = 1;
		 for (Entity e: productLineItems){
			 Instance productLineItem = (Instance) e;
			 Instance productIdentification = (Instance) productLineItem.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/purchaseorderrequest#productIdentification")).toArray()[0];
			 String globalProductIdentifier = productIdentification.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#globalProductIdentifier")).toArray()[0].toString();
				 
			 Instance orderQuantity = (Instance) productLineItem.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/purchaseorderrequest#orderQuantity")).toArray()[0];
			 Instance requestedQuantity = (Instance) orderQuantity.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/purchaseorderrequest#requestedQuantity")).toArray()[0];
			 String productQuantity = requestedQuantity.listAttributeValues(wsmoFactory.createIRI("http://www.wsmx.org/ontologies/rosetta/coreelements#productQuantity")).toArray()[0].toString();
				 
			 Instance addLineItemReq = wsmoFactory.createInstance(wsmoFactory.createIRI(defaultIRI+"addLineItem"+i),
					  wsmoFactory.getConcept(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#AddLineItemRequest")));

//			 addLineItemReq.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#authToken"),
//			 		 dataFactory.createWsmlString(userID));
			 
			 Instance item = wsmoFactory.createInstance(wsmoFactory.createIRI(defaultIRI+"item"+i++),
					  wsmoFactory.getConcept(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#item")));
			 item.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#articleId"),
					  dataFactory.createWsmlString(globalProductIdentifier));
			 item.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#quantity"),
					  dataFactory.createWsmlInteger(productQuantity));
			 
			 addLineItemReq.addAttributeValue(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#lineItem"),
					 item);
			 
			 targetOnto.addInstance(item);
			 targetOnto.addInstance(addLineItemReq);
		 }
		 
		 return targetOnto;
	} catch (Exception e){
		e.printStackTrace();
	}
	return null;

	}


	/* (non-Javadoc)
	 * @see org.wsmo.execution.common.component.DataMediator#mediate(org.omwg.ontology.Ontology, org.omwg.ontology.Ontology, java.util.Set)
	 */
	@Exposed(description = "Moon Data Mediation for multiple instances input.")
	public Map<Entity, List<Entity>> mediate(Ontology sourceOntology, Ontology targetOntology, Set<Entity> data) throws ComponentException, UnsupportedOperationException {
		
		Ontology mediatedOnto = sourceOntology;
		if (sourceOntology.getIdentifier().toString().equalsIgnoreCase("http://www.wsmx.org/ontologies/rosetta/purchaseorderrequestInstances"))
				mediatedOnto = mediate(sourceOntology);
	
//		StringBuffer sb = new StringBuffer();
//		Serializer wsmlSerializer = new SerializerImpl(new HashMap());
//		wsmlSerializer.serialize(new TopEntity[]{mediatedOnto}, sb);
//		String finalRespStr = sb.toString();
//		logger.debug("Ontology after mediation------:" + finalRespStr +"-------");
				
		List<Entity> mediatedEnt = Helper.getInstances(mediatedOnto);
		
		Map<Entity, List<Entity>> response = new HashMap<Entity, List<Entity>>();
		response.put((Entity)null,mediatedEnt);
		return response;
	}


	/* (non-Javadoc)
	 * @see org.wsmo.execution.common.component.DataMediator#mediate(org.omwg.ontology.Ontology, org.omwg.ontology.Ontology, org.wsmo.common.Entity)
	 */
	@Exposed(description = "Moon Data Mediation multiple for single input.")
	public List<Entity> mediate(Ontology sourceOntology, Ontology targetOntology, Entity data) throws ComponentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
 