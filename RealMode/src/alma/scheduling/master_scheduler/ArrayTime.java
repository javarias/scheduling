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
 * File ArrayTime.java
 */
 
package alma.scheduling.master_scheduler;

/**
 * Description 
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public class ArrayTime {

	/* 
		Time - absolute time in 100 ns since 1582-10-15 00:00:00 
		see OMG Time Service Specification,  
   		ftp://ftp.omg.org/pub/docs/formal/97-12-21.pdf. 
   		Currently, Time is handled as longlong. May change after 
   		ACS Time system is revised.
   		
   		typedef unsigned long long Time;
  	*/
      
	private long time;

	/**
	 * 
	 */
	public ArrayTime() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
	}
}
