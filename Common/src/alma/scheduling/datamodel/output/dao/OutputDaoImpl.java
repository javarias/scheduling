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
 * "@(#) $Id: OutputDaoImpl.java,v 1.2 2010/03/03 21:22:12 javarias Exp $"
 */
package alma.scheduling.datamodel.output.dao;

import java.util.Collection;
import java.util.List;

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

}
