package ie.deri.wsmx.orchestration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.deri.wsmo4j.choreography.ChoreographyFactoryRI;
import org.omwg.ontology.Concept;
import org.wsmo.factory.ChoreographyFactory;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;
import org.wsmo.service.orchestration.Orchestration;
import org.wsmo.service.signature.GroundedMode;
import org.wsmo.service.signature.Mode;

public class ServiceLocator {

	private Set<WebService> services = new HashSet<WebService>();
    protected WsmoFactory factory;
    protected LogicalExpressionFactory leFactory;
    protected ChoreographyFactory cFactory;
	protected DataFactory dataFactory;	

	public ServiceLocator() {
		super();
		setupFactories();
	}

	@SuppressWarnings("unchecked")
	public WebService locateService(Concept concept) {
		for (WebService service : services) {
			for (Interface intrface : (Collection<Interface>)service.listInterfaces()) {
				Orchestration orchestration = (Orchestration) intrface.getOrchestration();
				for (Mode mode : orchestration.getStateSignature()) {
					if (mode instanceof GroundedMode) {
						mode.getConcept().equals(concept);
						return service;
					}
				}
			}
		}
		return null;
	}

	public String locateEndpoint(Concept concept) {
		WebService service = locateService(concept);
		Set set = service.listNFPValues(factory.createIRI("http://wsmo.org/grounding/wsdl/endpoint"));
		if (set.size() > 0)
			return (String) set.iterator().next();
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void setupFactories() {
		leFactory = Factory.createLogicalExpressionFactory(null);
		factory = Factory.createWsmoFactory(null);
		cFactory = new ChoreographyFactoryRI();
		dataFactory = Factory.createDataFactory(null);
	}	

	public Set<WebService> getServices() {
		return services;
	}

	public void addService(WebService service) {
		services.add(service);
	}

	public void addServices(Collection<WebService> services) {
		this.services.addAll(services);
	}

}
