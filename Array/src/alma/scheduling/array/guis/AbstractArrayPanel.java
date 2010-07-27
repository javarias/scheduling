/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.array.guis;

import javax.swing.JPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.exec.extension.subsystemplugin.SessionProperties;
import alma.exec.extension.subsystemplugin.SubsystemPlugin;
import alma.scheduling.ArrayOperations;
import alma.scheduling.array.util.NameTranslator;
import alma.scheduling.array.util.NameTranslator.TranslationException;


/**
 * Abstract superclass for panels associated with a single array.
 * 
 * @author dclarke
 * $Id: AbstractArrayPanel.java,v 1.2 2010/07/27 16:43:13 rhiriart Exp $
 */
@SuppressWarnings("serial")
public abstract class AbstractArrayPanel extends JPanel
										   implements SubsystemPlugin {

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private PluginContainerServices services = null;
	private ArrayOperations         array = null;
	/* End Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * SubsystemPlugin implementation
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.exec.extension.subsystemplugin.SubsystemPlugin#runRestricted(boolean)
	 */
	@Override
	public boolean runRestricted(boolean restricted) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see alma.exec.extension.subsystemplugin.SubsystemPlugin#setServices(alma.exec.extension.subsystemplugin.PluginContainerServices)
	 */
	@Override
	public void setServices(PluginContainerServices services) {
		this.services = services;
		final SessionProperties properties = services.getSessionProperties();
		try {
			final String arrayCompName = NameTranslator.arrayToComponentName(properties.getArrayName());
		} catch (TranslationException e) {
			e.printStackTrace();
		}
		final String arrayName     = properties.getArrayName();
	}

	/* (non-Javadoc)
	 * @see alma.exec.extension.subsystemplugin.SubsystemPlugin#start()
	 */
	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.exec.extension.subsystemplugin.SubsystemPlugin#stop()
	 */
	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}
	/* End SubsystemPlugin implementation
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	protected AbstractArrayPanel() {
		super();
	}
	/* End Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Stuff to do with being a superclass
	 * ================================================================
	 */
	protected ArrayOperations getArray() {
		return array;
	}
	/* End Stuff to do with being a superclass
	 * ============================================================= */
}
