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
 * File WeatherTrend.java
 */
 
package alma.Scheduling.Define;

/**
 * The WeatherTrend class create a structure for housing weather 
 * data in a circular buffer.  The size of the buffer may be specified. 
 * (The default is an array of 50 items.)  There are two major methods,
 * store and get.  The store method stores weather data in
 * increasing time order, in a circular buffer.  The get method
 * returns all accumulated weather data in the form of an array
 * of increasing times.
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class WeatherTrend {

	private static final int BUFFERSIZE = 50;
	
	private WeatherData[] item;
	private int pos = 0;
	private int size = 0;
	private int buffersize = BUFFERSIZE;

	public WeatherTrend() {
		this(BUFFERSIZE);
	}

	public WeatherTrend(int buffersize) {
		this.buffersize = buffersize;
		item = new WeatherData [buffersize];
		for (int i = 0; i < buffersize; ++i)
			item[i] = null;	
	}
	
	public void store(WeatherData x) {
		// Make sure the time increases.
		if (size > 0) {
			int n = pos - 1;
			if (n == -1)
				n = buffersize -1;
			if (x.getTime().getStart().lt(item[n].getTime().getStart()))
				throw new IllegalArgumentException(
					"WeatherTrend: Time out of sync in attempt to store weather data. Old: " +
					item[n].getTime().getStart() + " New: " + x.getTime().getStart());
		}
		item[pos] = x;
		++pos;
		++size;
		if (pos == buffersize)
			pos = 0;
	}
	
	public WeatherData[] get() {
		int n = (size <= buffersize) ? size : buffersize;
		WeatherData[] x = new WeatherData [n];
		if (size <= buffersize) {
			System.arraycopy(item,0,x,0,n);
		} else {
			System.arraycopy(item,pos,x,0,(buffersize - pos));
			System.arraycopy(item,0,x,(buffersize - pos),pos);
		}	
		return x;
	}
	
}
