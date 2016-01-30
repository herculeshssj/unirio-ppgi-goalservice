package ie.deri.wsmx.orchestration.asm;

import java.util.List;

import org.omwg.ontology.Instance;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

public interface MachineBasedInvoker {

	/**
	 * Assumes messages to be wrapped in an
	 * instance of a particular message concept
	 * that is defined for this endpoint, so that we
	 * only deal with a single message instance, which may
	 * contains multiple instances that are component parts
	 * of the transmitted information.
	 * 
	 * @return the service response
	 */
	public Instance invoke(WebService service, Instance message);

}
