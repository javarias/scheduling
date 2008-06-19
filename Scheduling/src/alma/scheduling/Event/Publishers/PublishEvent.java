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
 * File PublishEvent.java
 */
package alma.scheduling.Event.Publishers;

import alma.acs.exceptions.AcsJException;
import alma.acs.logging.AcsLogger;
import alma.acs.nc.AbstractNotificationChannel;

/**
 * The PublishEvent class contains methods to handle publishing all
 * events in the Scheduling system. 
 * 
 * @version $Id: PublishEvent.java,v 1.7 2008/06/19 20:06:19 wlin Exp $
 * @author Allen Farris
 */
public class PublishEvent {

    protected AbstractNotificationChannel sched_nc;
    //protected SimpleSupplier sched_nc;
    protected final AcsLogger logger;
	/**
	 * 
	 */
	public PublishEvent(AcsLogger logger) {
		this.logger = logger;
	}

    public void publish(String s) {
    }

    public void disconnect() throws AcsJException {
    }

}
