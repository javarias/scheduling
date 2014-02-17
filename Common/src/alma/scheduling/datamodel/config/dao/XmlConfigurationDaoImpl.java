/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * "@(#) $Id: XmlConfigurationDaoImpl.java,v 1.15 2011/04/20 22:09:40 javarias Exp $"
 */
package alma.scheduling.datamodel.config.dao;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlConfigurationDaoImpl extends BaseConfigurationDaoImpl implements ConfigurationDao {
    
    static Logger logger = LoggerFactory.getLogger(XmlConfigurationDaoImpl.class);
    
    /**
     * Not implemented 
     */
    @Override
    public void updateConfig() {}

    /**
     * Not implemented 
     */
    @Override
    public void deleteAll() {}

    /**
     * Not implemented 
     */
    @Override
    public void updateNextStep(Date nextStepTime) {}

    /**
     * Not implemented 
     */
    @Override
    public void updateSimStartTime(Date simStartTime) {}

    /**
     * Not implemented 
     */
	@Override
	public void updateConfig(Date lastUpdateTime) {
		
	}

    /**
     * Not implemented 
     */
	@Override
	public void updateConfig(String simStatus) {
		// TODO Auto-generated method stub
		
	}

    /**
     * Not implemented 
     */
	@Override
	public void deleteForSimulation() {
		// TODO Auto-generated method stub
		
	}
    
}
