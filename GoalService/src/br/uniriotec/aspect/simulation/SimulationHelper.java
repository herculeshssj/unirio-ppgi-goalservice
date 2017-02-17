package br.uniriotec.aspect.simulation;

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
}
