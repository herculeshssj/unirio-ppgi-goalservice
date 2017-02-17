package br.uniriotec.aspect.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.rpc.ServiceException;

import org.deri.tools.wsml.owl2wsml_translator.v0_1.OWL2WSMLTranslationWS;
import org.deri.tools.wsml.owl2wsml_translator.v0_1.OWL2WSMLTranslationWSService;
import org.deri.tools.wsml.owl2wsml_translator.v0_1.OWL2WSMLTranslationWSServiceLocator;

public class ConvertOWLtoWSML {

	public static void main(String[] args) {
		
		try {
			
			// Read the files of OWL WS folder
			File owlFolder = new File("C:\\CustomServiceWorkspace\\unirio-ppgi-webservices\\SWS-TC-1.1\\Services");
			File[] listOfOwlServices = owlFolder.listFiles();
			
			// Declaration of Web Service
			OWL2WSMLTranslationWSService service = new OWL2WSMLTranslationWSServiceLocator();
			OWL2WSMLTranslationWS proxy = service.getowl2wsmlTranslation();
			
			// OWL content
			String owlContent = "";
			
			// Conversion result
			String conversionResult;
			
			// Line of converted wsml
			String line;
			
			// Save WebService name
			String wsmoSWSName = "";
			
			// List of parameters of WebService
			List<String> wsmoSWSParameters = new ArrayList<>();
			
			// Iterate the list of files to convert one by one
			for (int i = 0; i < listOfOwlServices.length; i++) {
				File owlFile = listOfOwlServices[i];
				
				// Verify if we have a file or a folder 
				if (owlFile.isFile()) {
					
					// Message
					System.out.print("Converting " + owlFile.getName() + "...");
					
					// Read the content of OWL file
					owlContent = new Scanner(owlFile).useDelimiter("\\Z").next();
				}
				
				// Submit the content of OWL file to Web Method for conversion
				conversionResult = proxy.translateOWL2WSML_Attr(owlContent);
				
				// Read all lines of converted wsml to extract revelant information to
				// definitive file.
				BufferedReader reader = new BufferedReader(new StringReader(conversionResult));
				line = reader.readLine();
				
				while (line != null) {
					
					if (line.contains("namespace")) {
						String[] temp1 = line.split("/");
						String[] temp2 = temp1[4].split(".owl");
						wsmoSWSName = temp2[0];
					}
					
					if (line.contains("parameterType")) {
						String [] temp3 = line.split("\"");
						wsmoSWSParameters.add(temp3[1]);
					}
					
					line = reader.readLine();
				}
				
				// Create final WSML WebService file
				new CreateWsmoFile(wsmoSWSName, wsmoSWSParameters, i).create();
				
				wsmoSWSParameters.clear();
				
				// Finish
				System.out.println("OK!");
			}
			
		} catch (ServiceException | IOException e) {
			e.printStackTrace();
		}
	}

}
