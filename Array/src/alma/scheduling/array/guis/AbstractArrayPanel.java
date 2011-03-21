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

import java.awt.Dimension;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.exec.extension.subsystemplugin.SessionProperties;
import alma.exec.extension.subsystemplugin.SubsystemPlugin;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.utils.DSAContextFactory;


/**
 * Abstract superclass for panels associated with a single array.
 * 
 * @author dclarke
 * $Id: AbstractArrayPanel.java,v 1.10 2011/03/21 17:25:18 javarias Exp $
 */
@SuppressWarnings("serial")
public abstract class AbstractArrayPanel extends JPanel
										   implements SubsystemPlugin {

    /*
     * ================================================================
     * Constants
     * ================================================================
     */
    public final static String NormalColour = "#000000";
    public final static String TitleColour  = "#3f3fff";
    public final static String DetailColour = "#bf00bf";
    public final static Color NormalColor = Color.BLACK;
    public final static Color TitleColor  = new Color( 63,  63, 255);
    public final static Color DetailColor = new Color(191,   0, 191);
    /* End Constants
     * ============================================================= */
    
	
    
    /*
     * ================================================================
     * Fields
     * ================================================================
     */
    protected PluginContainerServices services = null;
    /** The access to the project models */
    protected ModelAccessor<DSAContextFactory> models = null;
    /** The access to the Array for which we are a panel */
    protected ArrayAccessor array = null;
    /** Is this panel running in control mode (or monitor mode)? */
    private boolean                 controlPanel = true;
    /** Name of the Array to be controlled */
    protected String arrayName;
    /** Are we in Manual Mode? */
    private boolean manualMode = false;
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
		System.out.format("%s (AbstractArrayPanel).setServices, new arrayName = %s, old arrayName = %s%n",
				this.getClass().getSimpleName(),
				services.getSessionProperties().getArrayName(),
				this.arrayName);
		this.services = services;
		final String newArrayName = services.getSessionProperties().getArrayName();
		if ((this.arrayName == null) || (this.arrayName.length() == 0)) {
			// No existing array name..
			if ((newArrayName != null) && (newArrayName.length() > 0)) {
				// ..and there is one in the services
				this.arrayName = newArrayName;
			}
		}
	}

	/* (non-Javadoc)
	 * @see alma.exec.extension.subsystemplugin.SubsystemPlugin#start()
	 */
	@Override
	public void start() throws Exception {
		
		if (arrayName == null ){
			Exception ex = new Exception("arrayName is null. Please set an array value name before start");
			ex.printStackTrace();
			throw ex;
		}
		try {
			models = new ModelAccessor();
			modelsAvailable();
		} catch (Exception e) {
			models = null;
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					String.format("Cannot connect to project data - %s", e.getMessage()),
					"Initialisation Error,",
					JOptionPane.ERROR_MESSAGE);
		}
		try {
			array = new ArrayAccessor(services, arrayName);
			arrayAvailable();
		} catch (Exception e) {
			array = null;
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					String.format("Cannot connect to array - %s", e.getMessage()),
					"Initialisation Error,",
					JOptionPane.ERROR_MESSAGE);
		}
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
	protected AbstractArrayPanel(String arrayName) {
		super();
		this.arrayName = arrayName;
	}
	
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
	protected ArrayAccessor getArray() {
		return array;
	}
	
	/**
	 * Get access to the project models
	 * @return
	 */
	protected ModelAccessor<DSAContextFactory> getModels() {
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
	
	/**
	 * Is our array manual?
	 * @return <code>true</code> if we're in Manual Mode
	 *         <code>false</code> if we're not
	 */
	protected boolean isManual() {
		return manualMode;
	}
	
	protected void setArray(ArrayAccessor array){
		this.array = array;
		this.manualMode = array.isManual();
	}
	
	protected void setModelAccessor(ModelAccessor<DSAContextFactory> models){
		this.models = models;
	}

	protected void makeSameWidth(JComponent... components) {
		double maxWidth = -1;

		for (final JComponent c : components) {
			final double w = c.getPreferredSize().getWidth();
			if (w > maxWidth) {
				maxWidth = w;
			}
		}
		for (final JComponent c : components) {
			final Dimension d = c.getPreferredSize();
			d.setSize(maxWidth, d.getHeight());
			c.setMinimumSize(d);
			c.setPreferredSize(d);
			c.setMaximumSize(d);
		}
	}
	
	protected String getUserName() {
		try {
			final SessionProperties sp = services.getSessionProperties();
			try {
				String result = sp.getUserName();
				if (result == null) {
					result = "<i>No username</i>";
				}
				return result;
			} catch (NullPointerException e) {
				return "<i>No session properties</i>";
			}
		} catch (NullPointerException e) {
			return "<i>No container services</i>";
		}
	}
	
	protected String getUserRole() {
		try {
			final SessionProperties sp = services.getSessionProperties();
			try {
				String result = sp.getUserRole();
				if (result == null) {
					result = "<i>No user role</i>";
				}
				return result;
			} catch (NullPointerException e) {
				return "<i>No session properties</i>";
			}
		} catch (NullPointerException e) {
			return "<i>No container services</i>";
		}
	}
	/* End Stuff to do with being a superclass
	 * ============================================================= */
}
