package br.uniriotec.aspect.simulation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper created for grouping useful methods for simulation
 * 
 * @author Hércules S. S. José
 *
 */
public class SimulationHelper {
	
	private static String repository; 
	private static String goal;
	private static List<String> identifiedSWS = new ArrayList<>(); 
	private static List<String> selectedSWS = new ArrayList<>();

	public static void main(String[] args) {
		System.out.println(SimulationHelper.raffleNumbers(10, 60));
	}

	// static class
	private SimulationHelper() {}
	
	/*
	 * Generate random number, from 1 to limit
	 */
	private static int generateRandomNumber(int limit) {
		
		int random = new Random().nextInt(limit);
		
		return random;
	}
	
	/*
	 * Raffle 'quantity' random numbers, from 1 to 'limit'
	 */
	private static String raffleNumbers(int quantity, int limit) {
		
		Set<Integer> raffledNumbers = new TreeSet<>();
		
		do {
			int number = SimulationHelper.generateRandomNumber(limit);
			
			raffledNumbers.add(number);
			
		} while (raffledNumbers.size() < quantity);
		
		return raffledNumbers.toString();
		
	}
	
	public static void saveSimulationData() {
		try {
			Class.forName("org.postresql.Driver");
		    Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/simulation?user=simulation&password=simulation");
		    
		    String sql = "insert into horario (repository, goal, identified_sws, selected_sws, qtd_identified_sql, qtd_selected_sql) values (?,?,?,?,?,?)";
		    String sqlDelete = "delete from simulation";
		    
		    // Delete the table
		    PreparedStatement pstmDelete = conn.prepareStatement(sqlDelete);
		    pstmDelete.executeQuery();
		    
		    PreparedStatement pstm = conn.prepareStatement(sql);
		    
		    pstm.setString(1, repository);
		    pstm.setString(2, goal);
		    pstm.setString(3, identifiedSWS.toString());
		    pstm.setString(4, selectedSWS.toString());
		    pstm.setInt(5, identifiedSWS.size());
		    pstm.setInt(6, selectedSWS.size());
		    
		    pstm.close();
		    pstmDelete.close();
		    conn.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// clear variables
		identifiedSWS.clear();
		selectedSWS.clear();
	}

	public static void setRepository(String repository) {
		SimulationHelper.repository = repository;
	}

	public static void setGoal(String goal) {
		SimulationHelper.goal = goal;
	}

	public static void setIdentifiedSWS(List<String> identifiedSWS) {
		SimulationHelper.identifiedSWS = identifiedSWS;
	}

	public static void setSelectedSWS(List<String> selectedSWS) {
		SimulationHelper.selectedSWS = selectedSWS;
	}

	public static String getGoal() {
		return goal;
	}	
}
