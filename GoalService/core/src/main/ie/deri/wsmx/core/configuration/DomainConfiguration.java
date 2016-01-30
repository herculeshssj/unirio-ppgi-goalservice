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

package ie.deri.wsmx.core.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO double-check if standard .equals() is really sufficient
/**
 * Represents the configuration state of a single domain.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/DomainConfiguration.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.9 $ $Date: 2006-06-04 03:36:13 $
 */ 
public class DomainConfiguration implements Serializable {

	private static final long serialVersionUID = -7345149966259503178L;
	private final String domainName;
	private final List<ComponentConfiguration> mBeans = new ArrayList<ComponentConfiguration>();
	
	private KernelConfiguration kernelConfiguration;
	
	/**
	 * Constructs a configuration for a single domain.
	 * 
	 * @param domainname the name of the domain
	 */
	public DomainConfiguration(final String domainname) {
		this(domainname, null);
	}
	
	/**
	 * Constructs a configuration for a single domain and
	 * attaches it to a <code>KernelConfiguration</code>
	 * 
	 * @param domainname the name of the domain
	 * @param kernelConfiguration the WSMX configuration this domain belongs to
	 */
	public DomainConfiguration(final String domainname,
			KernelConfiguration kernelConfiguration) {
		super();
		this.domainName = domainname;
		appendToKernelConfiguration(kernelConfiguration);
	}
	
	/**
	 * Returns the domainname under which all MBeans
	 * in this <code>DomainConfiguration</code> are
	 * to be registered. This is the part before the 
	 * collon in the <code>ObjectName</code>.
	 * 
	 * @return the domainname
	 */
	public String getDomainName() {
		return domainName;
	}
	
	/**
	 * Returns the <code>KernelConfiguration</code>
	 * this <code>DomainConfiguration</code> belongs to
	 * and <code>null</code> if there is no such parent configuartion.
	 * @return the kernelConfiguration
	 */
	public KernelConfiguration getKernelConfiguration() {
		return kernelConfiguration;
	}
	
	/**
	 * Appends this <code>DomainConfiguration</code> to the
	 * specified <code>KernelConfiguration</code>. In order to
	 * avoid code duplication a ease maintenance this method
	 * actually calls <code>appendDomain</code> on its parameter,
	 * allowing the bidirectional link logic to be in one place.
	 * 
	 * @param kernelConfiguration the <code>KernelConfiguration</code> to append to
	 */
	public void appendToKernelConfiguration(KernelConfiguration kernelConfiguration) {
	    if (kernelConfiguration != null)
	        kernelConfiguration.appendDomain(this);
	}
	
	
	/**
	 * This method is used by the logic that maintains
	 * the bidirectional links between
	 * <code>KernelConfiguration</code> and
	 * <code>DomainConfiguration</code>. It is not 
	 * intented for general use, therefore package access.
	 * 
	 * @param kernelConfiguration the <code>KernelConfiguration</code> to set
	 */
	void setKernelConfiguration(KernelConfiguration kernelConfiguration) {
		this.kernelConfiguration = kernelConfiguration;
	}
	
	
	
	/**
	 * Appends a <code>MBeanConfiguration</code> to this
	 * <code>DomainConfiguration</code>. This method
	 * ensures encapsulation of the internal datastructure
	 * implementation that holds the MBeans.
	 * 
	 * @param the <code>MBeanConfiguration</code> to append
	 */
	public void appendMBean(ComponentConfiguration mc) {
		mBeans.add(mc);
		mc.setDomain(this);
	}
	
	/**
 	 * Removes a <code>MBeanConfiguration</code> from this
	 * <code>DomainConfiguration</code>. This method
	 * ensures encapsulation of the internal datastructure
	 * implementation that holds the domains.
	 * Note that this method will remove the first 
	 * <code>MBeanConfiguration</code> it will encounter,
	 * considered to <code>equals()</code> the passed in
	 * <code>MBeanConfiguration</code>
	 * 
	 * @param dc the <code>DomainConfiguration</code> to remove
	 */
	public void removeMBean(ComponentConfiguration dc) {
		mBeans.remove(dc);
	}
	
	/**
	 * Returns the <code>List</code> of <code>MBeanConfiguration</code>
	 * objects of the currently loaded configuration. Note that
	 * the returned <code>List</code> is unmodifyable in order to
	 * properly encapsulate the internal implementation.
	 * 
	 * @return a list of MBeans
	 */
	public List<ComponentConfiguration> getMBeans() {
		return Collections.unmodifiableList(mBeans);
	}
}
