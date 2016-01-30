package org.deri.wsmx.mediation.ooMediator.wsmx;

import ie.deri.wsmx.scheduler.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.deri.wsmx.mediation.ooMediator.wsml.MediationFlags;
import org.deri.wsmx.mediation.ooMediator.wsml.WSMXFacade;
import org.wsmo.execution.common.component.DataMediator;

public class WSMXFacadeImpl implements WSMXFacade {	
	
	static Logger logger = Logger.getLogger(WSMXFacadeImpl.class);
	
	public Map<String, Boolean> getMediationFlagsFromConfig() {
		if (logger.isInfoEnabled()) 
			logger.info("Getting flags for data mediator from WSMX config");
		Properties wsmxCfg = Environment.getConfiguration();				
		Map<String,Boolean> flags = new HashMap<String, Boolean>();		
		for (FlagConfig cfg : FLAGS_CONFIG) {
			String val = wsmxCfg.getProperty(cfg.key, cfg.defaultVal);
			flags.put(cfg.flag, Boolean.parseBoolean(val));
		}	
		return flags;		
	}

	public Loader getRMLoader(DataMediator dm) {
		return new RMLoader(dm);
	}
	
	
	/** default mediation flags configuration and wsmx properties key names */
	protected static final FlagConfig[] FLAGS_CONFIG = new FlagConfig[]{
		new FlagConfig(
				MediationFlags.FILTER_MAPPINGS_BASED_ON_INPUT,
				"wsmx.datamediator.filter_mappings_based_on_input", "false"),
		new FlagConfig(
				MediationFlags.TRANSFORM_ONLY_CONNECTED_INSTANCES, 
				"wsmx.datamediator.transform_only_connected_instances", "false"),
		new FlagConfig(
				MediationFlags.PRINT_ONTOLOGIES_TO_CONSOLE, 
				"wsmx.datamediator.log.ontologies", "true"),
		new FlagConfig(
				MediationFlags.PRINT_QUERIES_TO_CONSOLE, 
				"wsmx.datamediator.log.queries", "true"),
		new FlagConfig(
				MediationFlags.PRINT_TIMING_INFORMATION_TO_CONSOLE, 
				"wsmx.datamediator.log.timing", "false"),
		new FlagConfig(
				MediationFlags.WRITE_MERGED_ONTOLOGY_TO_FILE, 
				"wsmx.datamediator.write_merged_ontology_to_file", "false")
	};
	
	/**
	 * stores flag name, wsmx properties key, default value
	 */
	protected static class FlagConfig {
		public final String flag, key, defaultVal;
		public FlagConfig(String flag, String key, String defaultVal){
			this.flag = flag;
			this.key = key;
			this.defaultVal = defaultVal;
		}
	}

	
}
