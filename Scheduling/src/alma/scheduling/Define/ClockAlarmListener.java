/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 * File ClockAlarmListener.java
 */
 
package alma.scheduling.Define;


/**
 * The ClockAlarmListener is an interface that is called
 * by the Clock class whenever an alarm expires.  The object
 * implementing this interface must be provided when setting an alarm.
 * 
 * @version $Id: ClockAlarmListener.java,v 1.5 2009/11/09 22:58:45 rhiriart Exp $
 * @author Allen Farris
 */
public interface ClockAlarmListener {
	public void wakeUp(DateTime time);
}
