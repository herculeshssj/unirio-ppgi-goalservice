package br.uniriotec.aspect.simulation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Helper created for grouping useful methods for simulation
 * 
 * @author Hércules S. S. José
 *
 */
public class SimulationHelper {
	
	public static void saveCaseSimulation(String caseId, String goal, String repository) {
		try {
			Class.forName("org.postgresql.Driver");
		    Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/simulation?user=simulation&password=simulation");
		    
		    String sql = "insert into simulation (caseid, goal, repository, running) values (?,?,?,?)";
		    String sqlDelete = "delete from simulation where goal = ? and repository = ?";
		    String sqlRunning = "update simulation set running = false";
		    
		    PreparedStatement pstm = conn.prepareStatement(sql);
		    PreparedStatement pstmDelete = conn.prepareStatement(sqlDelete);
		    PreparedStatement pstmRunning = conn.prepareStatement(sqlRunning);
		    
		    pstmDelete.setString(1, goal);
		    pstmDelete.setString(2, repository);
		    
		    pstmDelete.executeUpdate();
		    pstmRunning.executeUpdate();
		    
		    pstm.setString(1, caseId);
		    pstm.setString(2, goal);
		    pstm.setString(3, repository);
		    pstm.setBoolean(4, true);
		    
		    pstm.executeUpdate();
		    
		    pstm.close();
		    pstmDelete.close();
		    pstmRunning.close();
		    conn.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String findGoal(String caseID) {
		String goal = null;
		try {
			Class.forName("org.postgresql.Driver");
		    Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/simulation?user=simulation&password=simulation");
		    
		    String sql = "select goal from simulation where caseid = ?";
		    
		    PreparedStatement pstm = conn.prepareStatement(sql);
		    
		    pstm.setString(1, caseID);
		    
		    ResultSet rs = pstm.executeQuery();
		    
		    while (rs.next()) {
		    	goal = rs.getString(1);
		    }
		    
		    rs.close();
		    pstm.close();
		    conn.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		return goal;
	}
	
	public static String findRunningGoal() {
		String goal = null;
		try {
			Class.forName("org.postgresql.Driver");
		    Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/simulation?user=simulation&password=simulation");
		    
		    String sql = "select goal from simulation where running = true";
		    
		    PreparedStatement pstm = conn.prepareStatement(sql);
		    
		    ResultSet rs = pstm.executeQuery();
		    
		    while (rs.next()) {
		    	goal = rs.getString(1);
		    }
		    
		    rs.close();
		    pstm.close();
		    conn.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		return goal;
	}
	
	public static void saveIdentifiedSWS(List<String> identifiedSWS) {
		try {
			Class.forName("org.postgresql.Driver");
		    Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/simulation?user=simulation&password=simulation");
		    
		    String sql = "update simulation set identified_sws = ?, qtd_identified_sws = ? where running = ?";
		    
		    PreparedStatement pstm = conn.prepareStatement(sql);
		    
		    pstm.setString(1, identifiedSWS.toString());
		    pstm.setInt(2, identifiedSWS.size());
		    pstm.setBoolean(3, true);
		    
		    pstm.executeUpdate();
		    
		    pstm.close();
		    conn.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void saveSimulationData(String caseID, List<String> seletedSWS) {
		try {
			Class.forName("org.postgresql.Driver");
		    Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/simulation?user=simulation&password=simulation");
		    
		    String sql = "update simulation set selected_sws = ?, qtd_selected_sws = ?, running = ? where caseid = ?";
		    
		    PreparedStatement pstm = conn.prepareStatement(sql);
		    
		    pstm.setString(1, seletedSWS.toString());
		    pstm.setInt(2, seletedSWS.size());
		    pstm.setBoolean(3, false);
		    pstm.setString(4, caseID);
		    
		    pstm.executeUpdate();
		    
		    pstm.close();
		    conn.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
