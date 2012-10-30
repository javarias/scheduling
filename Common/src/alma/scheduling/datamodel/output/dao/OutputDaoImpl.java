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
 * "@(#) $Id: OutputDaoImpl.java,v 1.6 2012/10/30 22:31:06 javarias Exp $"
 */
package alma.scheduling.datamodel.output.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.output.Results;

public class OutputDaoImpl extends GenericDaoImpl implements OutputDao{

    @Override
    @Transactional
    public void saveResults(Results results) {
        saveOrUpdate(results);
    }

    @Override
    @Transactional
    public void saveResults(Collection<Results> results) {
        saveOrUpdate(results);
       
    }

    @Override
    @Transactional(readOnly=true)
    public List<Results> getResults() {
        return findAll(Results.class);
    }

    @Override
    public void deleteAll() {
    	getSession().createQuery("delete from " + Results.class.getCanonicalName()).executeUpdate();
    }
    
    @Override
    public Results getResult(long id) {
    	Query q = getSession().createQuery("from Results r where r.id = " + id);
    	Results retVal = (Results) q.uniqueResult();
    	return retVal;
    }
    
    @Override
    public Results getLastResult() {
    	Query q = getSession().createQuery("select r.id from Results r order by r.id desc");
    	long id = (Long) q.list().get(0);
    	return getResult(id);
    }
    
}
