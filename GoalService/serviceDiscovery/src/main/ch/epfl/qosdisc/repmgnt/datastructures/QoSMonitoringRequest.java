/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.datastructures;

import java.sql.Time;
import java.util.Vector;

/**
 * @author Le-Hung Vu
 *
 */
public class QoSMonitoringRequest {
     
    public ServiceInterfaceExt serviceInterface;
    
    /**
     * The list of {@link QoSParameter} objects that need to be monitored
     */    
    public Vector qosParametersList;    
    public Time startTime;
    public Time endTime;
     
    
    /**
     * @param serviceInterface
     * @param qosParametersList
     * @param startTime
     * @param endTime
     */
    public QoSMonitoringRequest(ServiceInterfaceExt serviceInterface,
            Vector qosParametersList, Time startTime, Time endTime) {
        this.serviceInterface = serviceInterface;
        this.qosParametersList = qosParametersList;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
}
