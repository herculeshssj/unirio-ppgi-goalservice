package org.yawlfoundation.yawl.goalService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.deri.wsmx.unittest.util.WSMXExecution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

/**
 * A simple service that invoke a semantic web service from WSMX
 *
 * @author Hercules S. S. Jose
 * @date 14/11/2015
 */

public class GoalService extends InterfaceBWebsideController {
	
	private String _handle = null;
	
	private static String _ruleFolder = "C:\\Java\\aspect\\rules\\";
	
	public static WSMXExecution wsmxEnvironment = null;
	

	@Override
	public void handleCancelledWorkItemEvent(WorkItemRecord arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEnabledWorkItemEvent(WorkItemRecord workItem) {
		System.out.println("/******************************************************/");
		System.out.println();
		try {
			// Conecta ao engine caso não exista conexão prévia
			if (!connected()) _handle = connect(engineLogonName, engineLogonPassword);
			
			// Faz o checkout do WorkItem
			workItem = checkOut(workItem.getID(), _handle);
			
			System.out.println("WorkItem selecionado: " + workItem.getCaseID() + " : " + workItem.getTaskName() + "\n");
			
			if (wsmxEnvironment == null) {
				System.out.println("Ambiente WSMX não inicializado. Inicializando...\n");
				wsmxEnvironment = new WSMXExecution();
				System.out.println("Ambiente WSMX inicializado!\n");
			} else {
				System.out.println("Ambiente WSMX já inicializado!\n");
			}
			
			String adviceGoal = getAdviceGoal(workItem.getSpecURI(), workItem.getTaskName());
			
			if (adviceGoal == null) {
				System.out.println("Nenhum objetivo definido ao Advice. Saindo...\n");
				checkInWorkItem(workItem.getID(), workItem.getDataList(), getOutputData(workItem.getTaskID(), ""), null,  _handle);
				return;
			}
			
			System.out.println("Objetivo a ser atingido pelo Advice: " + adviceGoal + "\n");
			
			/* A partir desse ponto será feito a descoberta e invocação do serviço */
			
			System.out.println("Realizando a descoberta do serviço... \n");
			
			String goalIRIStr     = "http://www.uniriotec.br/aspect#" + adviceGoal + "Goal";
			String goalOntoIRIStr = "http://www.uniriotec.br/aspect#" + adviceGoal + "GoalOntology";			
			
			// Realiza a descoberta dos serviços que atendem ao objetivo definido
			List<String> selectedServices = wsmxEnvironment.runDiscovery(goalIRIStr, goalOntoIRIStr);
			
			if (selectedServices != null & !selectedServices.isEmpty()) {
				System.out.println("Serviços encontrados que atendem ao objetivo: \n"); // Colocar o nome do serviço encontrado a partir do objetivo informado
				
				// Lista os serviços encontrados
				for (String service : selectedServices) {
					System.out.println(service);
				}
				
				System.out.println("Realizando a invocação do serviço... \n");
				
				wsmxEnvironment.runDataMediationAndChoreography(goalIRIStr, goalOntoIRIStr, selectedServices.get(0));
				
				System.out.println("Invocação do serviço finalizada! Continuando o processo principal...\n");
				
			} else {
				System.out.println("Nenhum serviço encontrado! Continuando o processo principal...\n");
			}
			
			// Retornando o controle a Engine
			checkInWorkItem(workItem.getID(), workItem.getDataList(), getOutputData(workItem.getTaskID(), ""), null,  _handle);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("/******************************************************/");
		System.out.println();
	}
	
	private String getAdviceGoal(String specName, String taskName) {
		String ruleFileName = _ruleFolder + "\\aobpm_goal.xml";
		String adviceGoal = null;
		
		File file = new File(ruleFileName);
		if (file.exists()) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;
			Document doc = null;
			
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(ruleFileName);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
			
			if ((dBuilder == null) || (doc == null)) {
				return null;
			}
			
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("advice");
			
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					if (specName.equals(eElement.getAttribute("process")) && taskName.equals(eElement.getAttribute("name"))) {
						adviceGoal = eElement.getAttribute("goal");
					}
					
				}
			}
		}
		
		return adviceGoal;
	}

	private org.jdom2.Element getOutputData(String taskName, String data) {
		org.jdom2.Element output = new org.jdom2.Element(taskName);
		org.jdom2.Element result = new org.jdom2.Element("result");
        result.setText(data);
        output.addContent(result);
        return output;
    }
	
	private boolean connected() throws IOException {
        return _handle != null && checkConnection(_handle);
    }
}