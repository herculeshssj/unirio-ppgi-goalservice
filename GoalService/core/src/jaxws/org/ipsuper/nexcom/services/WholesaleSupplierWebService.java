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

import java.util.HashMap;
import java.util.List;

import ie.deri.wsmx.commons.Helper;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.jws.soap.SOAPBinding.ParameterStyle;

import org.ipsuper.nexcom.datamodel.QoSParameters;
import org.ipsuper.nexcom.datamodel.QoSParametersMsg;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 9 Jun 2007
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/jaxws/org/ipsuper/nexcom/services/WholesaleSupplierWebService.java,v $, * @version $Revision: 1.1 $ $Date: 2007-10-11 14:32:17 $
 */

@WebService(name = "VoIPWebServices", targetNamespace = "http://ip-super.org/usecase/nexcom/")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
public class WholesaleSupplierWebService {
	
	private static WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap<String, Object>());

	@WebMethod(operationName="confirmSLAOfferWS1", action="confirmSLAOfferWS1")
	@WebResult(name="QoSSupplierSEEResponse", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public QoSParametersMsg confirmSLAOfferWS1(
    		 @WebParam(name="QoSSupplierSEERequest", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
    		 QoSParametersMsg req){
    	
    	String tt = req.getTtID();
    	Ontology onto = (Ontology) Helper.getTopEntity("http://www.super-ip.org/qosdiscovery/SWSQoS/WS-QoS1.wsml#WSVoIPQoSParams1");
    	QoSParameters qos = getQoSParameters(onto);
    	QoSParametersMsg resp = new QoSParametersMsg();
    	resp.setTtID(tt);
    	resp.setQoSParmeters(qos);
    	
    	return resp; 
    }

	@WebMethod(operationName="confirmSLAOfferWS2", action="confirmSLAOfferWS2")
	@WebResult(name="QoSSupplierSEEResponse", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public QoSParametersMsg confirmSLAOfferWS2(
    		 @WebParam(name="QoSSupplierSEERequest", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
    		 QoSParametersMsg req){
    	
    	String tt = req.getTtID();
    	Ontology onto = (Ontology) Helper.getTopEntity("http://www.super-ip.org/qosdiscovery/SWSQoS/WS-QoS1.wsml#WSVoIPQoSParams2");
    	QoSParameters qos = getQoSParameters(onto);
    	QoSParametersMsg resp = new QoSParametersMsg();
    	resp.setTtID(tt);
    	resp.setQoSParmeters(qos);
    	
    	return resp; 
    }
	
	@WebMethod(operationName="confirmSLAOfferWS3", action="confirmSLAOfferWS3")
	@WebResult(name="QoSSupplierSEEResponse", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public QoSParametersMsg confirmSLAOfferWS3(
    		 @WebParam(name="QoSSupplierSEERequest", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
    		 QoSParametersMsg req){
    	
    	String tt = req.getTtID();
    	Ontology onto = (Ontology) Helper.getTopEntity("http://www.super-ip.org/qosdiscovery/SWSQoS/WS-QoS1.wsml#WSVoIPQoSParams3");
    	QoSParameters qos = getQoSParameters(onto);
    	QoSParametersMsg resp = new QoSParametersMsg();
    	resp.setTtID(tt);
    	resp.setQoSParmeters(qos);
    	
    	return resp; 
    }
	
	@WebMethod(operationName="confirmSLAOfferWS4", action="confirmSLAOfferWS4")
	@WebResult(name="QoSSupplierSEEResponse", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public QoSParametersMsg confirmSLAOfferWS4(
    		 @WebParam(name="QoSSupplierSEERequest", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
    		 QoSParametersMsg req){
    	
    	String tt = req.getTtID();
    	Ontology onto = (Ontology) Helper.getTopEntity("http://www.super-ip.org/qosdiscovery/SWSQoS/WS-QoS1.wsml#WSVoIPQoSParams4");
    	QoSParameters qos = getQoSParameters(onto);
    	QoSParametersMsg resp = new QoSParametersMsg();
    	resp.setTtID(tt);
    	resp.setQoSParmeters(qos);
    	
    	return resp; 
    }

	@WebMethod(operationName="confirmSLAOfferWS5", action="confirmSLAOfferWS5")
	@WebResult(name="QoSSupplierSEEResponse", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public QoSParametersMsg confirmSLAOfferWS5(
    		 @WebParam(name="QoSSupplierSEERequest", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
    		 QoSParametersMsg req){
    	
    	String tt = req.getTtID();
    	Ontology onto = (Ontology) Helper.getTopEntity("http://www.super-ip.org/qosdiscovery/SWSQoS/WS-QoS1.wsml#WSVoIPQoSParams5");
    	QoSParameters qos = getQoSParameters(onto);
    	QoSParametersMsg resp = new QoSParametersMsg();
    	resp.setTtID(tt);
    	resp.setQoSParmeters(qos);
    	
    	return resp; 
    }

	@WebMethod(operationName="confirmSLAOfferWS6", action="confirmSLAOfferWS6")
	@WebResult(name="QoSSupplierSEEResponse", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public QoSParametersMsg confirmSLAOfferWS6(
    		 @WebParam(name="QoSSupplierSEERequest", partName="QoSParametersMsg", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
    		 QoSParametersMsg req){
    	
    	String tt = req.getTtID();
    	Ontology onto = (Ontology) Helper.getTopEntity("http://www.super-ip.org/qosdiscovery/SWSQoS/WS-QoS1.wsml#WSVoIPQoSParams6");
    	QoSParameters qos = getQoSParameters(onto);
    	QoSParametersMsg resp = new QoSParametersMsg();
    	resp.setTtID(tt);
    	resp.setQoSParmeters(qos);
    	
    	return resp; 
    }


	private QoSParameters getQoSParameters(Ontology onto){
    	QoSParameters qos = new QoSParameters();
    	List<Entity> instances = Helper.getInstances(onto);
    	IRI valueIRI = wsmoFactory.createIRI("file:///c:/WSMX/resources/qosdiscovery/ontologies/Common/QoSBase.wsml#value");
    	
    	Instance inst = (Instance) Helper.getInstancesOfConcept(instances, "http://www.super-ip.org/ontologies/nexcom/VoIPQoSBase#PriceEUR").get(0);
    	qos.setPrice(new Float(Helper.getInstanceAttribute(inst,valueIRI)));
    	inst = (Instance) Helper.getInstancesOfConcept(instances, "http://www.super-ip.org/ontologies/nexcom/VoIPQoSBase#VolumeOfTraffic").get(0);
    	qos.setVolumeOfTraffic(new Float(Helper.getInstanceAttribute(inst,valueIRI)));

    	inst = (Instance) Helper.getInstancesOfConcept(instances, "http://www.super-ip.org/ontologies/nexcom/VoIPQoSBase#CCR").get(0);
    	qos.setCCR(new Float(Helper.getInstanceAttribute(inst,valueIRI)));

    	inst = (Instance) Helper.getInstancesOfConcept(instances, "http://www.super-ip.org/ontologies/nexcom/VoIPQoSBase#ACD").get(0);
    	qos.setACD(new Float(Helper.getInstanceAttribute(inst,valueIRI)));

    	inst = (Instance) Helper.getInstancesOfConcept(instances, "http://www.super-ip.org/ontologies/nexcom/VoIPQoSBase#PPD").get(0);
    	qos.setPPD(new Float(Helper.getInstanceAttribute(inst,valueIRI)));

    	inst = (Instance) Helper.getInstancesOfConcept(instances, "http://www.super-ip.org/ontologies/nexcom/VoIPQoSBase#CLISupport").get(0);
    	float f = new Float(Helper.getInstanceAttribute(inst,valueIRI));
    	if (f == 1.0) 
    		qos.setCLI(true);
    	else
    		qos.setCLI(false);

		return qos;
	}
	
}
 