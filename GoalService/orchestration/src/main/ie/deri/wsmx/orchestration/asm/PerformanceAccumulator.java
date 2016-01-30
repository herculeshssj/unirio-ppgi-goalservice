package ie.deri.wsmx.orchestration.asm;

import java.util.HashSet;
import java.util.Set;

import org.wsmo.service.choreography.rule.ChoreographyChoose;
import org.wsmo.service.choreography.rule.ChoreographyForAll;
import org.wsmo.service.choreography.rule.ChoreographyIfThen;
import org.wsmo.service.choreography.rule.ChoreographyPipedRules;
import org.wsmo.service.orchestration.rule.OrchestrationAchieveGoal;
import org.wsmo.service.orchestration.rule.OrchestrationApplyMediation;
import org.wsmo.service.orchestration.rule.OrchestrationChoose;
import org.wsmo.service.orchestration.rule.OrchestrationForAll;
import org.wsmo.service.orchestration.rule.OrchestrationIfThen;
import org.wsmo.service.orchestration.rule.OrchestrationInvokeService;
import org.wsmo.service.orchestration.rule.OrchestrationPipedRules;
import org.wsmo.service.orchestration.rule.Receive;
import org.wsmo.service.orchestration.rule.Send;
import org.wsmo.service.rule.Add;
import org.wsmo.service.rule.Delete;
import org.wsmo.service.rule.Update;
import org.wsmo.service.rule.Visitor;

public class PerformanceAccumulator implements Visitor {

	private Set<OrchestrationInvokeService> invokes = new HashSet<OrchestrationInvokeService>();
	
	public void visitAdd(Add rule) {
		// TODO Auto-generated method stub

	}

	public void visitChoreographyChoose(ChoreographyChoose rule) {
		// TODO Auto-generated method stub

	}

	public void visitChoreographyForAll(ChoreographyForAll rule) {
		// TODO Auto-generated method stub

	}

	public void visitChoreographyIfThen(ChoreographyIfThen rule) {
		// TODO Auto-generated method stub

	}

	public void visitChoreographyPipedRules(ChoreographyPipedRules rules) {
		// TODO Auto-generated method stub

	}

	public void visitDelete(Delete rule) {
		// TODO Auto-generated method stub

	}

	public void visitOrchestrationAchieveGoal(OrchestrationAchieveGoal rule) {
		// TODO Auto-generated method stub
		

	}

	public void visitOrchestrationApplyMediation(OrchestrationApplyMediation rule) {
		// TODO Auto-generated method stub

	}

	public void visitOrchestrationChoose(OrchestrationChoose rule) {
		// TODO Auto-generated method stub

	}

	public void visitOrchestrationForAll(OrchestrationForAll rule) {
		// TODO Auto-generated method stub

	}

	public void visitOrchestrationIfThen(OrchestrationIfThen rule) {
		// TODO Auto-generated method stub

	}

	public void visitOrchestrationInvokeService(OrchestrationInvokeService rule) {
		invokes.add(rule);
	}

	public void visitOrchestrationPipedRules(OrchestrationPipedRules rules) {
		// TODO Auto-generated method stub

	}

	public void visitReceive(Receive rule) {
		// TODO Auto-generated method stub

	}

	public void visitSend(Send rule) {
		// TODO Auto-generated method stub

	}

	public void visitUpdate(Update rule) {
		// TODO Auto-generated method stub

	}

	public Set<OrchestrationInvokeService> getInvokes() {
		return invokes;
	}

}
