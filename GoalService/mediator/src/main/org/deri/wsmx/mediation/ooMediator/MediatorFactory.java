/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian <br>
 *
 * MediatorFactory.java, Apr 29, 2004, 11:24:02 AM
 *
 **/
package org.deri.wsmx.mediation.ooMediator;

import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.deri.wsmx.mediation.ooMediator.util.CONSTANTS;
import org.deri.wsmx.mediation.ooMediator.wsml.WSMLDataMediatorFactory;



/**
 * This class offers the factory method as a way of creating mediator objects. The factory method is
 * a parameterized one and according to this parameter returns different types of mediators.   
 *
 **/
public abstract class MediatorFactory{
	
	/**
	 * Provides a dummy mediator.
	 * @return a dummy mediator 
	 */
	public static Mediator createMediator(){
		return new DummyMediator();
	}
	
	/**
	 * Return a mediator according to the specified payloadType 
	 * @param payloadType specifies the payload type. The allowed values are: CONSTANTS.FLORA_PAYLOAD
	 * @return the corresponding mediator
	 */
	public static Mediator createMediator(int payloadType, Loader loader){
		//if (payloadType==CONSTANTS.FLORA_DATA_MEDIATOR || payloadType==CONSTANTS.FLORA_DATA_MEDIATOR_DEBUGGER)
		//	return FloraMediatorFactory.createMediator();
		if (payloadType==CONSTANTS.WSML_DATA_MEDIATOR || payloadType==CONSTANTS.WSML_DATA_MEDIATOR_DEBUGGER)
			return WSMLDataMediatorFactory.createMediator(loader);
		return null;
	}
}
