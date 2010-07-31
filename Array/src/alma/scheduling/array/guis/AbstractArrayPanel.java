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

import javax.swing.JOptionPane;
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
 * $Id: AbstractArrayPanel.java,v 1.3 2010/07/31 00:17:38 dclarke Exp $
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
	/** The access to the project models */
	private ModelAccessor models = null;
	/** The access to the Array for which we are a panel */
	// Will become an ArrayAccessor in due course 
	private ArrayOperations         array = null;
	/** Is this panel running in control mode (or monitor mode)? */
	private boolean                 controlPanel = false;
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
		this.controlPanel = !restricted;
		return restricted;
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
		
		arrayAvailable();
		modelsAvailable();
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
		try {
			models = new ModelAccessor();
		} catch (Exception e) {
			models = null;
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					String.format("Cannot connect to project data - %s", e.getMessage()),
					"Initialisation Error,",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	/* End Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Stuff to do with being a superclass
	 * ================================================================
	 */
	/**
	 * Lets subclass instances know that the array is has become
	 * available.
	 */
	protected abstract void arrayAvailable();
	
	/**
	 * Lets subclass instances know that the project models have become
	 * available.
	 */
	protected abstract void modelsAvailable();
	
	/**
	 * Get access to the array for which we are a panel.
	 * @return
	 */
	protected ArrayOperations getArray() {
		return array;
	}
	
	/**
	 * Get access to the project models
	 * @return
	 */
	protected ModelAccessor getModels() {
		return models;
	}
	
	/**
	 * Are we a controlling panel or a monitor panel?
	 * @return <code>true</code> if we're a control panel, and
	 *         <code>false</code> if we're a monitor panel.
	 */
	protected boolean isControl() {
		return controlPanel;
	}
	/* End Stuff to do with being a superclass
	 * ============================================================= */
}
