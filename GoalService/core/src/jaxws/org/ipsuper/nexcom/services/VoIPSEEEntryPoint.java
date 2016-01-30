/*
 * Copyright (c) 2007 National University of Ireland, Galway
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

package org.ipsuper.nexcom.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.executionsemantic.ExecutionSemanticsFinalResponse;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.log4j.Logger;
import org.ipsuper.nexcom.datamodel.QoSParameters;
import org.ipsuper.nexcom.datamodel.QoSParametersMsg;
import org.omwg.ontology.Instance;
import org.wsmo.common.Entity;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 10 Jun 2007
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/jaxws/org/ipsuper/nexcom/services/VoIPSEEEntryPoint.java,v $, * @version $Revision: 1.1 $ $Date: 2007-10-11 14:32:17 $
 */
@WebService(name = "VoIPSEEEntryPoint", targetNamespace = "http://ip-super.org/usecase/nexcom/")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
public class VoIPSEEEntryPoint {

	private static WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap<String, Object>());
	private static MBeanServer mBeanServer = null;
	protected static Logger logger = Logger.getLogger(VoIPSEEEntryPoint.class);
	
	public static void setMBeanServer(MBeanServer m){
		mBeanServer = m;
	}
	
	@WebMethod(operationName="achieveVoIPGoal", action="achieveVoIPGoal")
	@WebResult(name="QoSSupplierSEEResponse", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public QoSParametersMsg achieveVoIPGoal(
   		 @WebParam(name="QoSSupplierSEERequest", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
		 QoSParametersMsg req){

		String goalStr = populateGoalTemplate(req);
		QoSParametersMsg qosResp = new QoSParametersMsg();

		try {
			Object response = invokeComponent("CommunicationManager", "achieveGoalFullResponse", 
					new Object[]{goalStr}, new String[]{"java.lang.String"});

			ExecutionSemanticsFinalResponse esResponse = (ExecutionSemanticsFinalResponse) response; 

			if (esResponse!=null && esResponse.getExecutionSemantic() != null && 
				esResponse.getReceivedMessages()!= null && esResponse.getReceivedMessages().size()>0) {
				Set<Entity> respMsg = esResponse.getReceivedMessages();
				qosResp = getQoSParameters(new ArrayList<Entity>(respMsg));
//				clean up after communication
				esResponse.getExecutionSemantic().cleanUp();
			}

			return qosResp;
		} catch (Exception e) {
			e.printStackTrace();
			return qosResp;
		}
	}


//	@WebMethod(operationName="achieveVoIPGoalString", action="achieveVoIPGoalString")
//    @WebResult(name="resp")
//	public String achieveVoIPGoalString(
//    		@WebParam(name="QoSSupplierSEERequest", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
//   		 	QoSParametersMsg req){
//		String goalStr = populateGoalTemplate(req);
//		QoSParametersMsg qosResp = new QoSParametersMsg();
//    		
//		try {
//			Object response = invokeComponent("CommunicationManager", "achieveGoalFullResponse", 
//					new Object[]{goalStr}, new String[]{"java.lang.String"});
//
//			ExecutionSemanticsFinalResponse esResponse = (ExecutionSemanticsFinalResponse) response; 
//			
//			System.out.println(esResponse.getMsg());
//			
//			//clean up after communication
//			if (esResponse!=null && esResponse.getExecutionSemantic() != null)
//				esResponse.getExecutionSemantic().cleanUp();
//			
//			return esResponse.getMsg();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "";
//		}
//	}
	
	private String populateGoalTemplate(QoSParametersMsg qos){
	
	String t = ""; 
	t+= "wsmlVariant _\"http://www.wsmo.org/wsml/wsml-syntax/wsml-rule\" \n";
	t+= "namespace { _\"http://www.super-ip.org/goals/nexcom/GoalWholesaleSupplier#\", \n"; 
	t+= "     dc _\"http://purl.org/dc/elements/1.1#\",  \n";
	t+= "     wsml _\"http://www.wsmo.org/2004/wsml#\",  \n";
	t+= "	  nx _\"http://www.ip-super.org/ontologies/Nexcom/20070514#\", \n"; 
	t+= "     rank _\"file:///c:/WSMX/resources/qosdiscovery/ontologies/Common/Ranking.wsml#\", \n";
	t+= "     qosreq _\"http://www.super-ip.org/discovery/ontologies/nexcom/GoalsQoS/GoalQoS#\", \n";
	t+= "	  qos _\"file:///c:/WSMX/resources/qosdiscovery/ontologies/Common/QoSBase.wsml#\", \n";     
	t+= "     vqos _\"http://www.super-ip.org/ontologies/nexcom/VoIPQoSBase#\" }\n";

	t+= " \n";
	t+= "goal GoalWholesaleSupplier[RANDOMID] \n";
	t+= "     nonFunctionalProperties \n";
	t+= "          dc#title hasValue \"Goal of finding wholesale VoIP provider\" \n";
	t+= "     endNonFunctionalProperties \n";
	t+= " \n";
	t+= "     importsOntology \n";
	t+= "     { nx#Nexcom } \n";
	t+= " \n";
	t+= "capability GoalWholesaleSupplierCapability \n";
	t+= "	nfp \n";
	t+= "		_\"http://www.wsmo.org/goal/discovery/qos\" hasValue \"true\" \n";
	t+= "	endnfp \n";
	t+= " \n";
	t+= "postcondition PostCond \n"; 
	t+= "     definedBy  \n";
	t+= "      ?QoS memberOf nx#QualityOfServiceParameter. \n";
	t+= " \n";
	t+= "interface GoalWholesaleSupplierInterface \n";
	t+= "        importsOntology {qosreq#GoalVoIPQoSParams[RANDOMID]} \n";
	t+= " \n";
	t+= "choreography GoalWholesaleSupplierChoreography \n";
	t+= "    stateSignature GoalWholesaleSupplier1Statesignature \n";
	t+= "      importsOntology {nx#Nexcom } \n";
	t+= "        in  nx#QoSSupplierSEERequest \n";
	t+= "	    out nx#QoSSupplierSEEResponse \n";
	t+= " \n";
	t+= "transitionRules GoalWholesaleSupplierTransitionRules \n";
	t+= " \n";
	t+= "	forall {?request} with \n";
	t+= "        (?request memberOf nx#QoSSupplierSEERequest \n";
	t+= "        ) do \n";
	t+= "          add(_#1 memberOf nx#QoSSupplierSEEResponse) \n";
	t+= "    endForall \n";
	t+= " \n";
	t+= "ontology qosreq#GoalVoIPQoSParams[RANDOMID] \n";
	t+= " nonFunctionalProperties \n";
	t+= "   _\"http://www.wsmo.org/discovery/qos\" hasValue \"true\" \n";
	t+= " endNonFunctionalProperties \n";
	t+= " \n";
	t+= " importsOntology { qos#QoSUpperOntology, \n";
	t+= " 				   vqos#VoIPQoSBase, \n";
	t+= "		   		   rank#QoSRankingOntology } \n";
	t+= " \n";
	t+= "instance qosreq#requiredPriceEUR[RANDOMID] memberOf {vqos#PriceEUR, qos#GoalRequirement} \n";
	t+= "    qos#value hasValue [PRICE] \n";
	t+= "    qos#unit hasValue vqos#PriceEURUnit \n";
	t+= " \n";
	t+= "instance qosreq#volumeOfTraffic[RANDOMID] memberOf {vqos#VolumeOfTraffic, qos#GoalRequirement} \n";
	t+= "    qos#value hasValue [VOLUMEOFTRAFFIC] \n";
	t+= "    qos#unit hasValue qos#TimeUnit \n";
	t+= " \n";
	t+= "instance qosreq#ccr[RANDOMID] memberOf  {vqos#CCR, qos#GoalRequirement} \n";
	t+= "    qos#value hasValue [CCR]  \n";
	t+= "    qos#unit hasValue qos#PercentageUnit \n";
	t+= " \n";
	t+= "instance qosreq#acd[RANDOMID] memberOf  {vqos#ACD, qos#GoalRequirement} \n";
	t+= "    qos#value hasValue [ACD]  \n";
	t+= "    qos#unit hasValue qos#TimeUnit \n";    
	t+= " \n";
	t+= "instance qosreq#ppd[RANDOMID] memberOf  {vqos#PPD, qos#GoalRequirement} \n";
	t+= "    qos#value hasValue [PPD]  \n";
	t+= "    qos#unit hasValue qos#TimeUnit \n";    
	t+= " \n";
	t+= "instance qosreq#cliSupport[RANDOMID] memberOf { vqos#CLISupport, qos#GoalSpec } \n";
	//FIXME
	//t+= " qos#value hasValue [CLISUPPORT] \n";
	t+= " qos#value hasValue {0.0, 1.0} \n";
	t+= " qos#unit hasValue vqos#Boolean \n";
	t+= " \n";
	t+= "instance qosreq#ranking[RANDOMID] memberOf rank#QoSRankingScoreThreshold \n";
	t+= " rank#hasHigherRankThreshold hasValue 0.15 // xi_h \n";
	t+= " rank#hasLowerRankThreshold  hasValue 0.15 // xi_l \n";
	t+= " rank#hasHigherPartialScore  hasValue 3.0  // rho_h \n";
	t+= " rank#hasEqualPartialScore   hasValue 1.0  // rho_e \n";
	t+= " rank#hasLowerPartialScore   hasValue 0.0  // rho_l \n";
	t+= " \n";
	t+= "instance qosreq#priceEURRank[RANDOMID] memberOf rank#QoSConceptConfiguration \n";
	t+= " rank#hasQoSConceptIRI      hasValue vqos#PriceEUR \n";
	t+= " rank#hasWeight             hasValue [PRICEWEIGHT] \n";
	t+= " rank#hasReputationScore    hasValue 1.0 \n";
	t+= " rank#hasMatchingThreshold  hasValue 0.0 \n";
	t+= " \n";
	t+= "instance qosreq#volumeOfTrafficRank[RANDOMID] memberOf rank#QoSConceptConfiguration \n";
	t+= " rank#hasQoSConceptIRI      hasValue vqos#VolumeOfTraffic \n";
	t+= " rank#hasWeight             hasValue [VOLUMEOFTRAFFICWEIGHT] \n";
	t+= " rank#hasReputationScore    hasValue 1.0 \n";
	t+= " rank#hasMatchingThreshold  hasValue 0.0 \n";
	t+= " \n";
	t+= " instance qosreq#ccrRank[RANDOMID] memberOf rank#QoSConceptConfiguration \n";
	t+= " rank#hasQoSConceptIRI      hasValue vqos#CCR \n";
	t+= " rank#hasWeight             hasValue [CCRWEIGHT] \n";
	t+= " rank#hasReputationScore    hasValue 1.0 \n";
	t+= " rank#hasMatchingThreshold  hasValue 1.0 \n";
	t+= "  \n";
	t+= " instance qosreq#acdRank[RANDOMID] memberOf rank#QoSConceptConfiguration \n";
	t+= " rank#hasQoSConceptIRI      hasValue vqos#ACD \n";
	t+= " rank#hasWeight             hasValue [ACDWEIGHT] \n";
	t+= " rank#hasReputationScore    hasValue 1.0 \n";
	t+= " rank#hasMatchingThreshold  hasValue 0.0 \n";
	t+= "  \n";
	t+= " instance qosreq#ppdRank[RANDOMID] memberOf rank#QoSConceptConfiguration \n";
	t+= " rank#hasQoSConceptIRI      hasValue vqos#PPD \n";
	t+= " rank#hasWeight             hasValue [PPDWEIGHT] \n";
	t+= " rank#hasReputationScore    hasValue 1.0 \n";
	t+= " rank#hasMatchingThreshold  hasValue 0.0 \n";
	t+= " \n";
	t+= "ontology GoalRequest[RANDOMID] \n";
	t+= " \n";
	t+= "importsOntology \n";
	t+= "            {nx#Nexcom} \n";
	t+= " \n";
	t+= "instance req[RANDOMID] memberOf nx#QoSSupplierSEERequest \n";
	t+= "	nx#hasTTID hasValue \"[TTID]\"	 \n";
	t+= " \n";

	t = t.replaceAll("\\[RANDOMID\\]", ""+Helper.getRandomLong());
	t = t.replaceAll("\\[TTID\\]", ""+qos.getTtID());
	t = t.replaceAll("\\[PRICE\\]", ""+qos.getQoSParmeters().getPrice());
	t = t.replaceAll("\\[VOLUMEOFTRAFFIC\\]", ""+qos.getQoSParmeters().getVolumeOfTraffic());
	t = t.replaceAll("\\[CCR\\]", ""+qos.getQoSParmeters().getCCR());
	t = t.replaceAll("\\[ACD\\]", ""+qos.getQoSParmeters().getACD());
	t = t.replaceAll("\\[PPD\\]", ""+qos.getQoSParmeters().getPPD());
	if (qos.getQoSParmeters().isCLI())
		t = t.replaceAll("\\[CLISUPPORT\\]", "1.0");
	else 
		t = t.replaceAll("\\[CLISUPPORT\\]", "0.0");
	t = t.replaceAll("\\[PRICEWEIGHT\\]", ""+qos.getQoSParmeters().getPriceWeight());
	t = t.replaceAll("\\[VOLUMEOFTRAFFICWEIGHT\\]", ""+qos.getQoSParmeters().getVolumeOfTrafficWeight());
	t = t.replaceAll("\\[CCRWEIGHT\\]", ""+qos.getQoSParmeters().getCCRWeight());
	t = t.replaceAll("\\[ACDWEIGHT\\]", ""+qos.getQoSParmeters().getACDWeight());
	t = t.replaceAll("\\[PPDWEIGHT\\]", ""+qos.getQoSParmeters().getPPDWeight());
	
	return t;
	}
	
    private Object invokeComponent(String componentName, String operationName, Object[] params, String[] paramsDataTypes) {
		//get a reference to MBeanService
			
		ObjectName componentObjectName = null;
		try {
			componentObjectName = new ObjectName("components:name="+componentName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//check if mBean has been registered
		boolean flag = mBeanServer.isRegistered(componentObjectName);
		if (!flag) {
			return null;
		}
			
		Object response;
		try {
			response = mBeanServer.invoke(componentObjectName, operationName, params, paramsDataTypes);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
		return response;
    }
    
	private QoSParametersMsg getQoSParameters(List<Entity> instances){
		QoSParametersMsg qosMsg = new QoSParametersMsg(); 
		QoSParameters qos = new QoSParameters();
		
    	List<Entity> ents = Helper.getInstancesOfConcept(instances, "http://www.ip-super.org/ontologies/Nexcom/20070514#QoSSupplierSEEResponse");
    	
    	if (ents==null || ents.size()==0)
    		return qosMsg;
		
		Instance inst = (Instance) ents.get(0);
		String hasVolumeOfTrafficStr = Helper.getAttribute(inst, "http://www.ip-super.org/ontologies/Nexcom/20070514#hasVolumeOfTraffic");
		String hasCCRStr = Helper.getAttribute(inst, "http://www.ip-super.org/ontologies/Nexcom/20070514#hasCCR");
		String hasACDStr = Helper.getAttribute(inst, "http://www.ip-super.org/ontologies/Nexcom/20070514#hasACD");
		String hasPPDStr = Helper.getAttribute(inst, "http://www.ip-super.org/ontologies/Nexcom/20070514#hasPPD");
		String hasPriceStr = Helper.getAttribute(inst, "http://www.ip-super.org/ontologies/Nexcom/20070514#hasPrice");
    	String hasCLIStr = Helper.getAttribute(inst, "http://www.ip-super.org/ontologies/Nexcom/20070514#hasCLI");
    	String hasTTIDStr = Helper.getAttribute(inst, "http://www.ip-super.org/ontologies/Nexcom/20070514#hasTTID");
		
    	qos.setVolumeOfTraffic(new Float(hasVolumeOfTrafficStr));
    	qos.setCCR(new Float(hasCCRStr));
    	qos.setACD(new Float(hasACDStr));
    	qos.setPPD(new Float(hasPPDStr));
    	qos.setPrice(new Float(hasPriceStr));

    	if (hasCLIStr.equalsIgnoreCase("true"))
    		qos.setCLI(true);
    	else 
    		qos.setCLI(false);
    	qosMsg.setTtID(hasTTIDStr);
    	qosMsg.setQoSParmeters(qos);
    	
		return qosMsg;
	}

}
 