package ch.epfl.qosdisc.repmgnt.util;

import java.io.File;
import java.util.Calendar;

/**
 * @author Le-Hung Vu
 *
 * Contains list of all constants to be used throughout the program
 *
 *
 */
public interface Constants {

	/**
	 * Connection string and user, password
	 */
	static final String DBNAME=System.getProperty("user.dir") + File.separator+ "codims-home" + File.separator+ "QoSDiscoveryDB";

	static final String  DBMS_CONNECTION_STRING="jdbc:oracle:thin:@//lbdsun7.epfl.ch:1521/lbd10.epfl.ch";
	static final String DEFAULT_DATA_SOURCE_ID="IRI_OPERATORS_LBD7";
	static final String  USER_DB="CODIMSD";
	static final String  PASSWD_DB="CODIMSD";

	static final long STARTING_TIME_POINT = 1160646481956L;// = that of 2006-12-Oct 11:48:00 (Calendar.getInstance().getTimeInMillis();)
	static final long MILLISECONDS_PER_SECONDS=1000;
	/**
	 * The name of the column (in the operator Metadata) 
	 * which contains the corresponding column 
	 * The string has the form of TableName_ColumnName
	 */
	static final String  QoSREPORT_ID_QOSREPORT="QoSREPORT_ID_QOSREPORT";
	static final String  QoSREPORT_DT_TIMESTART="QoSREPORT_DT_TIMESTART";
	static final String  QoSREPORT_DT_TIMEEND="QoSREPORT_DT_TIMEEND";
	
	static final String  SERVICEINTERFACE_ID_INTERFACE="SERVICEINTERFACE_ID_INTERFACE";
	static final String  SERVICEINTERFACE_ID_WEBSERVICE="SERVICEINTERFACE_ID_WEBSERVICE";
	static final String  SERVICEINTERFACE_AD_URI="SERVICEINTERFACE_AD_URI";
	
	/**
	 * List of request types to send to the CODIMSD query processing system
	 */
	static final int REQUEST_TYPE_SERVICE_DISCOVERY=0;
	static final int REQUEST_TYPE_SERVICE_DISCOVERY_EXTREP=1;
	static final int REQUEST_TYPE_SERVICE_QOS_INDEXING=2;
	static final int REQUEST_TYPE_DISHONEST_DETECTION=3;
	static final int REQUEST_TYPE_REPORT_CLUSTERING=4;
	static final int REQUEST_TYPE_PREDICT_SERVICE_QOS=5;
	static final int REQUEST_TYPE_GET_REPUTATION_INFO=6;
	
	/**
	 * The position in the list of metadata objects of an operator
	 * where we can find the names of the columns.
	 * Usually, one operator have only one metadata object, thus
	 * this string has value 0 by default
	 */
	static final int METADATA_INDEX_FOR_COLUMN_NAMES = 0;
	

	/**
	 * Some predefine match levels used in the QoSMatchResult.matchingLevel
	 */
	static final double QOS_EXACT_MATCH=1.0;
	static final double QOS_NOT_MATCH=0.0;
	
   /**
    * 
    */
	static final byte DEFAULT_TRANSACTION_PURPOSE=0;
	
	/**
	 * Default values for cluster centroid reports
	 */
	static final int CENTROID_REPORT_DEFAULT_UID = -9999;
	static final int CENTROID_REPORT_DEFAULT_RID = -9999;
	static final byte CENTROID_REPORT_DEFAULT_CREDIBILITY = lhvu.qos.utils.Constants.REPORT_UNCERTAINED;
	

}
