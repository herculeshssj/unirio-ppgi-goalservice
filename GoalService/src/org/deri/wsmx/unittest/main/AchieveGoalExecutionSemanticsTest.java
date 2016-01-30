package org.deri.wsmx.unittest.main;

import org.apache.log4j.Logger;
import org.deri.wsmx.unittest.util.WSMXExecution;

public class AchieveGoalExecutionSemanticsTest {
	protected static Logger logger = Logger.getLogger(AchieveGoalExecutionSemanticsTest.class);
	public static WSMXExecution run = new WSMXExecution();

	/**
	 * Classe de teste que irá invocar um serviço Web a partir de um objetivo
	 * estabelecido usando os recursos semânticos oferecidos pelo WSMX.
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String goalStr = "goal";
		
		String goalIRIStr     = "http://www.uniriotec.br/aspect/goal/logging#SimpleLogGoal";
		String goalOntoIRIStr = "http://www.uniriotec.br/aspect/goal/logging#SimpleLogGoalOntology";

		//run.runDiscovery(goalIRIStr, goalOntoIRIStr);
		run.runDiscoveryDataMediationAndChoreography(goalIRIStr, goalOntoIRIStr);
		
	}
}
