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
package org.wsmo.execution.common.nonwsmo;

import java.io.Serializable;

/**
 * Interface or class description
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/nonwsmo/WSDLDocument.java,v $,
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
 * @version $Revision: 1.1 $ $Date: 2005-11-05 22:32:47 $
 */

public class WSDLDocument implements Serializable {

	private static final long serialVersionUID = -7341502980919884061L;
	private String content;
    
    public WSDLDocument(String theContent){
        this.content = theContent;
    }

    public String getContent() {
        return content;
    }
    

    public void setContent(String content) {
        this.content = content;
    }
    
}
