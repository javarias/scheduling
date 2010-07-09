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
 * "@(#) $Id: GenericDaoImpl.java,v 1.11 2010/07/09 17:14:05 javarias Exp $"
 */
package alma.scheduling.datamodel;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link GenericDao}
 * 
 * @author javarias
 *
 */
@Transactional
public abstract class GenericDaoImpl extends HibernateDaoSupport implements GenericDao {

    @Override
    public <T> void delete(T obj) {
        getHibernateTemplate().delete(obj);
    }

    @Override
    public <T> void deleteAll(Collection<T> objs) {
        getHibernateTemplate().deleteAll(objs);
    }
    
    @Override
    public <T> void saveOrUpdate(T obj) {
        getHibernateTemplate().saveOrUpdate(obj);
    }

    @Override
    @Transactional(readOnly=false)
    public <T> void saveOrUpdate(Collection<T> objs) {
        getHibernateTemplate().saveOrUpdateAll(objs);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    /* The implementation uses "load" instead "get". This enable the lazy loading, saving
     * memory during the runtime, but using more the DB access. 
     */
    public <T, PK extends Serializable> T findById(Class<T> obj, PK key) {
        return (T) getHibernateTemplate().load(obj, key);
        //return (T) getHibernateTemplate().get(obj, key);
    }

    
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public <T> List<T> executeNamedQuery(String queryName, Object[] queryArgs) {
        Query namedQuery = getSession().getNamedQuery(queryName);
        for(int i = 0; i< queryArgs.length; i++){
            Object arg = queryArgs[i];
            namedQuery.setParameter(i, arg);
        }
        return (List<T>)namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public <T> List<T> findAll(Class<T> obj) {
        return (List<T>)getHibernateTemplate().find("from " + obj.getName());
    }

}
