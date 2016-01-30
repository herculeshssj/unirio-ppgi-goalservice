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

package ie.deri.wsmx.core.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 17.04.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/annotation/WSMXComponent.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-21 16:28:56 $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface WSMXComponent {
	public static final String NO_EVENTS = "none";
	public static final String NO_NOTIFICATIONS = "none";
	public static final String NO_NOTIFICATION_FILTER = "none";	
	
	String domain()				default "components";
	String name();
	String[] events()			default NO_EVENTS;			
	String[] notifications()	default NO_NOTIFICATIONS;
	String notificationFilter() default NO_NOTIFICATION_FILTER;
	//TODO notifiction filters could determined using a seperate type annotation
	String description()		default "No description available.";
}
