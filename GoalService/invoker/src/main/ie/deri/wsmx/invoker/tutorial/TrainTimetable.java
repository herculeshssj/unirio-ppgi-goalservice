/**
 * TrainTimetable.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package ie.deri.wsmx.invoker.tutorial;

public interface TrainTimetable extends java.rmi.Remote {
    public java.lang.String get_train_times(java.lang.String startCity, java.lang.String endCity, java.lang.String startDate) throws java.rmi.RemoteException;
}
