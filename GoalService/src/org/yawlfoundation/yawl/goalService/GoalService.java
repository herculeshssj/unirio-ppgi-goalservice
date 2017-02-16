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
			// Connect to engine in case do not exists previous connection
			if (!connected()) _handle = connect(engineLogonName, engineLogonPassword);
			
			// Checkout the WorkItem
			workItem = checkOut(workItem.getID(), _handle);
			
			System.out.println("Selected WorkItem: " + workItem.getCaseID() + " : " + workItem.getTaskName() + "\n");
			
			if (wsmxEnvironment == null) {
				System.out.println("WSMX Environment not started. Starting...\n");
				wsmxEnvironment = new WSMXExecution();
				System.out.println("WSMX Environment started!\n");
			} else {
				System.out.println("WSMX Environment yet started!\n");
			}
			
			String adviceGoal = getAdviceGoal(workItem.getSpecURI(), workItem.getTaskName());
			
			if (adviceGoal == null) {
				System.out.println("No goal defined to Advice. Quitting...\n");
				checkInWorkItem(workItem.getID(), workItem.getDataList(), getOutputData(workItem.getTaskID(), ""), null,  _handle);
				return;
			}
			
			System.out.println("Goal to the achieve by Advice: " + adviceGoal + "\n");
			
			/* In this point will be done the discovery and invocation of service */
			
			System.out.println("Discovering the service... \n");
			
			//String goalIRIStr     = "http://www.uniriotec.br/aspect#" + adviceGoal; // Proof of concept
			//String goalOntoIRIStr = "http://www.uniriotec.br/aspect#LogOntology"; // Proof of concept		
			
			String goalIRIStr     = "http://127.0.0.1/goals#" + adviceGoal; // Simulation
			String goalOntoIRIStr = "http://127.0.0.1/ontology/Concepts.owl"; // Simulation
			
			// Discovery the services that achieve the defined operational goal
			List<String> selectedServices = wsmxEnvironment.runDiscovery(goalIRIStr, goalOntoIRIStr);
			
			 
			if (selectedServices != null & !selectedServices.isEmpty()) {
				System.out.println("Services found that achieve the operational goal: \n");
				
				// List of found services
				for (String service : selectedServices) {
					System.out.println(service);
				}
				/* commented for execute simulations
				System.out.println("Doing the invocation of service... \n");
				
				wsmxEnvironment.runDataMediationAndChoreography(goalIRIStr, goalOntoIRIStr, selectedServices.get(0));
				
				System.out.println("Invocation of service completed! Continuing the main process...\n");
				*/ //commented for execute simulations
				
			} else {
				System.out.println("None service found! Continuing the main process...\n");
			}
			
			
			// Returning the control to YAWL Engine
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