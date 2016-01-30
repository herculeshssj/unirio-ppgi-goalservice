/*
 * Copyright (c) 2005 National University of Ireland, Galway
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
package org.wsmo.execution.common.component.resourcemanager;

import org.wsmo.common.Identifier;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.VersionIdentifier;
import org.wsmo.execution.common.nonwsmo.VersionInformation;
import org.wsmo.service.WebService;

/**
 * Interface or class description
 * 
 * <pre>
 *  Created on 16-Jun-2005
 *  Committed by $Author: morcen $
 *  $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/resourcemanager/VersionResourceManager.java,v $,
 * </pre>
 * 
 * @author Michal Zaremba
 * @author Thomas Haselwanter
 * 
 * @version $Revision: 1.2 $ $Date: 2005-11-22 16:49:18 $
 */

public interface VersionResourceManager {

    public VersionInformation saveWebService(WebService webService) throws ComponentException, UnsupportedOperationException;

    public WebService loadWebService(Identifier webServiceIdentifier, VersionIdentifier versionIdentifier) throws ComponentException, UnsupportedOperationException;
}
