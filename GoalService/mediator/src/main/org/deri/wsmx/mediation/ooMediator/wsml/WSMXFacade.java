package org.deri.wsmx.mediation.ooMediator.wsml;

import java.util.Map;

import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.wsmo.execution.common.component.DataMediator;

/**
 * Facade for WSMX specific functionality. 
 * 
 * (Used to minimalize dependencies of the data mediator to WSMX core
 *  - org.deri.wsmx.mediation.ooMediator.wsml.wsmx package is not
 *  needed outside of WSMX.)
 * 
 * @author Max
 *
 */
public interface WSMXFacade {
	
	/**
	 * Get a storage loader that uses WSMX Resource Manager
	 * 
	 * @param dm the data mediator component that uses the loader
	 * @return
	 */
	public Loader getRMLoader(DataMediator dm);
	
	/**
	 * Get data mediator configuration flags from main WSMX configuration
	 * 
	 * @return
	 */
	public Map<String, Boolean> getMediationFlagsFromConfig();	
	
}
