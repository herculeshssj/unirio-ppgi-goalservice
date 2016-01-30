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

import java.util.Date;

/**
   * Interface or class description
   *
   * <pre>
   * Created on 16-Jun-2005
   * Committed by $Author: haselwanter $
   * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/nonwsmo/VersionInformation.java,v $,
   * </pre>
   *
   * @author Michal Zaremba
   * @author Thomas Haselwanter
   *
   * @version $Revision: 1.1 $ $Date: 2005-11-05 22:32:47 $
   */

public class VersionInformation {
    
    private VersionIdentifier   identifier;
    private String              userKey;
    private Date                date;
    private String              comment;
    private String              changeLog;
    private String              previousVersion;

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(String previousVersion) {
        this.previousVersion = previousVersion;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public VersionIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(VersionIdentifier identifier) {
        this.identifier = identifier;
    }
    
}
