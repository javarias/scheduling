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
 * File Dispatcher.java
 */
 
package alma.scheduling.master_scheduler;

import alma.scheduling.define.STime;
/**
 * Description 
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public interface Dispatcher {

	/**
	 *  Tells the control system to execute the SB with 'id' and its start
	 *  time is 'time'.
	 *  @param id
	 *  @param time
	 */
	public void sendToControl(String id, STime time);

}
