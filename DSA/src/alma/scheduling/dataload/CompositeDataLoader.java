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
 * "@(#) $Id: CompositeDataLoader.java,v 1.9 2012/11/01 21:55:14 javarias Exp $"
 */
package alma.scheduling.dataload;

import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

public class CompositeDataLoader implements DataLoader {
	
	private static Logger logger = LoggerFactory.getLogger(CompositeDataLoader.class);

    private List<DataLoader> loaders;
        
    public void setLoaders(List<DataLoader> loaders) {
        this.loaders = loaders;
    }

    @Override
    @Transactional(readOnly=false)
    public void load() throws Exception {
        for (Iterator<DataLoader> iter = loaders.iterator(); iter.hasNext(); ) {
        	DataLoader it = iter.next();
        	logger.info("Loading data from " + it.getClass().getName());
            try{            	
            	it.load();
            }catch( Exception e ){
            	logger.error("Bean: " + it.getClass());
            	throw e;
            }
        }
    }
    
    @Override
    @Transactional
    public void clear() {
        for (Iterator<DataLoader> iter = loaders.iterator(); iter.hasNext(); ) {
            iter.next().clear();
        }
    }
}
