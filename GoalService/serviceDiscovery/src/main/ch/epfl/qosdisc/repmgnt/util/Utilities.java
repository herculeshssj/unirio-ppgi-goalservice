package ch.epfl.qosdisc.repmgnt.util;

import java.sql.Timestamp;

public final class Utilities {

	/**
	 * From a string representing a timeStamp, get the timepoint in terms of milliseconds, with time origin is the
	 * current time starting a program
	 * @param timeStampStr
	 * @return
	 */
	public static int getTimePoint(Timestamp timeStamp) {
		
		int timePoint=(int)((timeStamp.getTime()-Constants.STARTING_TIME_POINT));
		
		return timePoint;

	}

	/**
	 * Create a string represented a timestamp to be inserted in the DBMS
	 * @param timePoint 
	 * @return
	 */
	public static String timeStampStr(int timePoint){
		
		Timestamp newTimeStamp=new Timestamp((long)timePoint+Constants.STARTING_TIME_POINT);
		
		String newTimeStampStr= "'" + newTimeStamp.toString() +"'"; 

		//System.out.println("timePoint="+timePoint+", getTimePoint="+Utilities.getTimePoint(newTimeStamp));
		
		return newTimeStampStr;
	}


	/**
	 * Create a timestamp to be inserted in the DBMS
	 * @param timePoint 
	 * @return
	 */
	public static Timestamp getTimeStamp(int timePoint){
		
		return new Timestamp((long)timePoint+Constants.STARTING_TIME_POINT);
		
	}

    
	
	
}
