/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.algorithm.results.dao;

import java.util.Map;

import alma.scheduling.algorithm.results.Result;

/**
 * 
 * Interface for fetching results of a DSA run.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 */
public interface ResultsDao {

	/**
	 * Return the latest set of scores and ranks from the DSA for the
	 * given array.
	 * 
	 * @param arrayName - the array to which the results apply
	 * @return the current result
	 */
	public Result getCurrentResult(String arrayName);

	/**
	 * Return the previous set of scores and ranks from the DSA for the
	 * given array.
	 * 
	 * @param arrayName - the array to which the results applied
	 * @return the previous result
	 */
	public Result getPreviousResult(String arrayName);
	
	
	public void saveOrUpdate(Result result);
}
