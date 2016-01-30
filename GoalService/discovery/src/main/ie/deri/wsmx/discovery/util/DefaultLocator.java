/*
 * Copyright (c) 2006, University of Innsbruck, Austria.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ie.deri.wsmx.discovery.util;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.*;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.factory.*;
import org.wsmo.locator.*;
import org.wsmo.service.*;
import org.wsmo.wsml.*;


/**
 * A default locator. Retrieves ontologies, Web Services and goals from their specified IRIs.
 * 
 * @author Adina Sirbu
 * @version $Revision: 1.2 $ $Date: 2006/09/21 08:45:58 $
 */
public class DefaultLocator implements Locator {

	protected static Logger logger = Logger.getLogger(DefaultLocator.class);
	
	private Parser parser = Factory.createParser(null);
	
	public Entity lookup(Identifier id, Class type) throws SynchronisationException {
		Entity entity = null;
		
		if (type.equals(Ontology.class) || type.equals(WebService.class) || 
				type.equals(Goal.class)) {
			try {
				URL url = new URL(id.toString());
				Reader reader = new InputStreamReader(url.openStream());
				
				TopEntity[] entities = parser.parse(reader);
				for (TopEntity topEntity : entities)
					if (type.isInstance(topEntity)) {
						entity = topEntity;
						break;
					}			
			} catch (MalformedURLException e) {
				logger.error("Invalid " + type.getSimpleName() + " identifier " + id, e);
			} catch (IOException e) {
				logger.error("IOException while retrieving " + type.getSimpleName() + " " + id, e);
			} catch (ParserException e) {
				logger.error("Failed to parse " + type.getSimpleName() + " " + id, e);
			} catch (InvalidModelException e) {
				logger.error("Invalid " + type.getSimpleName() + " model for " + id, e);
			}
		}
		return entity;
	}

	public Set lookup(Identifier arg0) throws SynchronisationException {
		return null;
	}
	
}