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
			int i = 0;
			List<String> parameters = new ArrayList<>();
			while (i < 10) {
				
				parameters.clear();
				
				parameters.add("http://127.0.0.1/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				
				new CreateWsmoFile("Goal"+i, parameters, i).createGoal();
				
				i++;
			}
			System.out.println("OK!");
			
			System.out.print("Creating goals with two variable...");
			// Generate 10 goals with two variables on postconditions
			while (i < 20) {
				
				parameters.clear();
				
				parameters.add("http://127.0.0.1/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				parameters.add("http://127.0.0.1/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				
				new CreateWsmoFile("Goal"+i, parameters, i).createGoal();
				
				i++;
			}
			System.out.println("OK!");
			
			System.out.print("Creating goals with three variable...");
			// Generate 10 goals with three variable on postconditions
			while (i < 30) {
				
				parameters.clear();
				
				parameters.add("http://127.0.0.1/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				parameters.add("http://127.0.0.1/ontology/Concepts.owl#" 
						+ concepts.get(GenerateRandomGoals.generateRandomNumber(240)));
				parameters.add("http://127.0.0.1/ontology/Concepts.owl#" 
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
