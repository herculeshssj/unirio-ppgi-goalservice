package ie.deri.wsmx.orchestration.asm;

import java.util.List;

import org.omwg.ontology.Instance;
import org.wsmo.service.Goal;

public interface GoalAchiever {

	public List<Instance> achieve(Goal goal);

}
