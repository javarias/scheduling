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
 */
package alma.scheduling.datamodel.output.dao;

import java.util.Collection;
import java.util.List;

import alma.scheduling.datamodel.output.Results;

public interface OutputDao {

    /**
     * Save the result in the DB or XML
     * @param results
     */
    public void saveResults(Results results);
    
    /**
     * Save a batch of results in the DB. The implementor of this method should
     * define the entire function as Transactional 
     * @param The collection to be saved in the DB
     */
    public void saveResults(Collection<Results> results);
    
    /**
     * Get all the results of the stored in the DB. <br> 
     * <i>Note: </i> The user of this method should
     * define as Transactional the entire function that use this method if the 
     * implementor is using Spring and Hibernate
     * 
     * @return The results obtained from DB, can be null if there are no results in the DB
     */
    public List<Results> getResults();
    
    public void deleteAll();
    
    public Results getResult(long id);
    
    public Results getLastResult();
    
    public long getLastResultId();
}
