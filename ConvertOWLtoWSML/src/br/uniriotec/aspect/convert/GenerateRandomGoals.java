package br.uniriotec.aspect.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GenerateRandomGoals {

	public static void main(String[] args) {
		
		createRandomGoalsFromWholeOntology();
		createRandomGoalsFromRepository();
	}

	private static void createRandomGoalsFromRepository() {
		try {
			
			System.out.println("Loading concepts from repository 3... ");
			
			// Read the files of OWL WS folder
			File swsFolder = new File("C:\\CustomServiceWorkspace\\unirio-ppgi-webservices\\SWS-WSML\\Services\\Repository3");
			File[] listOfServices = swsFolder.listFiles();
			
			List<String> concepts = new ArrayList<>();
			
			// Iterate the list of files to get the concepts
			for (int i = 0; i < listOfServices.length; i++) {
				File swsFile = listOfServices[i];
				
				// Verify if we have a file or a folder 
				if (swsFile.isFile()) {
					// Read the content of SWS file
					String swsContent = new Scanner(swsFile).useDelimiter("\\Z").next();
					
					// Read all lines of SWS file to extract the concepts used on
					// variables
					BufferedReader reader = new BufferedReader(new StringReader(swsContent));
					String line = reader.readLine();
					
					while (line != null) {
						if (line.contains("memberOf")) {
							String[] temp1 = line.split("\"");							
							for (String s : temp1) {
								
								if (s.contains("Concepts")) {
									concepts.add(s.substring(s.indexOf("#")+1));
								}
								
							}							
						}
						line = reader.readLine();
					}
					
				}
			}
			
			System.out.print("Creating goals with one variable...");
			// Generate 10 goals with one variable on postconditions
			int i = 41;
			List<String> parameters = new ArrayList<>();
			while (i <= 50) {
				
				parameters.clear();
				
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(concepts.size()-1)));
				
				new CreateWsmoFile("Goal"+i, parameters, i).createGoal();
				
				i++;
			}
			System.out.println("OK!");
			
			System.out.print("Creating goals with two variable...");
			// Generate 10 goals with two variables on postconditions
			while (i <= 60) {
				
				parameters.clear();
				
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(concepts.size()-1)));
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(concepts.size()-1)));
				
				new CreateWsmoFile("Goal"+i, parameters, i).createGoal();
				
				i++;
			}
			System.out.println("OK!");
			
			System.out.print("Creating goals with three variable...");
			// Generate 10 goals with three variable on postconditions
			while (i <= 70) {
				
				parameters.clear();
				
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(concepts.size()-1)));
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(concepts.size()-1)));
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(concepts.size()-1)));
				
				new CreateWsmoFile("Goal"+i, parameters, i).createGoal();
				
				i++;
			}
			System.out.println("OK!");
		
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private static void createRandomGoalsFromWholeOntology() {
		try {
			
			System.out.print("Loading concepts from ontology... ");
			
			// Open ontology Concepts.wsml
			File convertedOntology = new File("C:\\CustomServiceWorkspace\\unirio-ppgi-webservices\\SWS-WSML\\Ontology\\Concepts.wsml");
	
			// Read the content of file
			String wsmlContent = new Scanner(convertedOntology).useDelimiter("\\Z").next();
			
			// Read all lines of wsml file to extract all concepts
			BufferedReader reader = new BufferedReader(new StringReader(wsmlContent));
			String line = reader.readLine();
			
			List<String> concepts = new ArrayList<>();
			
			while (line != null) {
				
				if (line.contains("concept")) {
					String[] temp = line.split(" ");
					concepts.add(temp[1]);
				}
				
				line = reader.readLine();
			}
			
			System.out.println("OK!");
			
			System.out.print("Creating goals with one variable...");
			// Generate 10 goals with one variable on postconditions
			int i = 11;
			List<String> parameters = new ArrayList<>();
			while (i <= 20) {
				
				parameters.clear();
				
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				
				new CreateWsmoFile("Goal"+i, parameters, i).createGoal();
				
				i++;
			}
			System.out.println("OK!");
			
			System.out.print("Creating goals with two variable...");
			// Generate 10 goals with two variables on postconditions
			while (i <= 30) {
				
				parameters.clear();
				
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				
				new CreateWsmoFile("Goal"+i, parameters, i).createGoal();
				
				i++;
			}
			System.out.println("OK!");
			
			System.out.print("Creating goals with three variable...");
			// Generate 10 goals with three variable on postconditions
			while (i <= 40) {
				
				parameters.clear();
				
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				parameters.add("http://www.uniriotec.br/wsmo/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				
				new CreateWsmoFile("Goal"+i, parameters, i).createGoal();
				
				i++;
			}
			System.out.println("OK!");
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/*
	 * Generate random number, from 1 to limit
	 */
	private static int generateRandomNumber(int limit) {
		
		int random = new Random().nextInt(limit);
		
		return random;
	}
	
}
