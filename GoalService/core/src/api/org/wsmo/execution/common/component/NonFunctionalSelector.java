/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *                    Open University, Milton Keynes
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package org.wsmo.execution.common.component;

import java.util.List;

import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.service.WebService;

/**
 * Non Functional Selection. The intention of this component is to allow the selection of the most appropriate
 * Web service in the case where Discovery has returned multiple matching service descriptions. The selection is based
 * on the knowledge contained in the service descriptions and possibly preferences that are configured in the architecture.
 * This component and API are under-specified in the current release.  
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maitiu_moran $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/NonFunctionalSelector.java,v $,
 * </pre>
 *
 * @author Michal Zaremba
 * @author Liliana Cabral 
 * @author John Domingue
 * @author David Aiken
 * @author Emilia Cimpian
 * @author Thomas Haselwanter
 * @author Mick Kerrigan
 * @author Adrian Mocan
 * @author Matthew Moran
 * @author Brahmananda Sapkota
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-12-20 14:50:09 $
 */

public interface NonFunctionalSelector {

	/**
	 * Select the Web service description that best fits the specified Goal description
     * @param list of Web service descriptions provided as output from the discovery component.
     * @return the selected Web service description
     * @throws ComponentException throws an exception if there is a problem reported during the selection process
     * @throws UnsupportedOperationException throws an exception if the list of Web services provided is invalid 
	 */
   public WebService select(List<WebService> webServices)
        throws ComponentException, UnsupportedOperationException;
    
}
