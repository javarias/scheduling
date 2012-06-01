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
package alma.scheduling.datamodel.obsproject.dao;

import java.rmi.Remote;
import java.util.Iterator;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.Target;

@Transactional(readOnly = true)
public class ObsProjectDaoImpl extends GenericDaoImpl implements ObsProjectDao, Remote {

    private static Logger logger = LoggerFactory.getLogger(ObsProjectDaoImpl.class);
    
    @Override
    public void hydrateSchedBlocks(ObsProject prj) {
        logger.trace("hydrating ObsProject");
        getHibernateTemplate().lock(prj, LockMode.NONE);
        Long id = prj.getObsUnit().getId();
        ObsUnit ou = null;
        try {
        	ou = (ObsUnitSet) getHibernateTemplate().get(ObsUnitSet.class, id);
        } catch (org.hibernate.ObjectNotFoundException ex) {
            ou = (SchedBlock) getHibernateTemplate().get(SchedBlock.class, id);        	
        }
        // ATTENTION -- WEIRD CODE ALARM -- ATTENTION -- WEIRD CODE ALARM -- ATTENTION
        prj.setObsUnit(ou); // replace the java_assist proxy by the real thing
        // ATTENTION -- WEIRD CODE ALARM -- ATTENTION -- WEIRD CODE ALARM -- ATTENTION
        hydrateObsUnit(ou);
    }


    public ObsUnit getObsUnitForProject(ObsProject prj) {
        Long id = prj.getObsUnit().getId();
        ObsUnit ou = null;
        try {
        	ou = (ObsUnitSet) getHibernateTemplate().get(ObsUnitSet.class, id);
        } catch (org.hibernate.ObjectNotFoundException ex) {
            ou = (SchedBlock) getHibernateTemplate().get(SchedBlock.class, id);        	
        }
        hydrateObsUnit(ou);
        return ou;
    }
    
    private void hydrateObsUnit(ObsUnit ou) {
        getHibernateTemplate().lock(ou, LockMode.NONE);
        
        logger.trace("hydrating ObsUnit");
        logger.debug("ObsUnit class: " + ou.getClass().getName());
        if (ou == null)
            logger.warn("ObsUnit is null");
        if (ou instanceof SchedBlock) {
            logger.trace("hydrating SchedBlock");
            SchedBlock sb = (SchedBlock) ou;
            sb.getSchedulingConstraints().getMaxAngularResolution();
            if( sb.getWeatherConstraints() != null )
            	sb.getWeatherConstraints().getMaxOpacity();
            for (Iterator<ObservingParameters> iter = sb.getObservingParameters().iterator(); iter.hasNext();) {
	            ObservingParameters params = iter.next();
	            params.getId();
	        }
            for( Iterator<Target> iter = sb.getTargets().iterator(); iter.hasNext(); ){
            	Target tar = iter.next();
            	tar.getId();
                for (Iterator<ObservingParameters> iter2 = tar.getObservingParameters().iterator(); iter2.hasNext();) {
    	            ObservingParameters params = iter2.next();
    	            params.getId();
    	        }
                tar.getSource().getId();
				tar.getSource().getName();

            }

            logger.debug("successfully casted SchedBlock");
            return;
        } else if (ou instanceof ObsUnitSet) {
            logger.debug("hydrating ObsUnitSet");
            ObsUnitSet ous = (ObsUnitSet) ou;
            logger.debug("# of ObsUnits in ObsUnitSet: " + ous.getObsUnits().size());
            for (ObsUnit sou : ous.getObsUnits()) {
                hydrateObsUnit(sou);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ObsProject> getObsProjectsOrderBySciRank() {
        Query query = getSession().createQuery("from ObsProject prj order by prj.scienceRank");
        return query.list();
    }

    @Override
    @Transactional(readOnly=false, isolation=Isolation.SERIALIZABLE)
    public void saveOrUpdate(ObsProject prj) {
        super.saveOrUpdate(prj);
    }
    
    @Override
    @Transactional(readOnly=false, isolation=Isolation.SERIALIZABLE, propagation=Propagation.REQUIRES_NEW)
    public void refreshProject(ObsProject prj) {
    	logger.debug("Refreshing project with entity ID: " + prj.getUid());
    	ObsProject ret = findByEntityId(prj.getUid());
    	if (ret != null) {
    		delete(prj.getObsUnit());
    		delete(ret);
    	}
    	super.saveOrUpdate(prj);
    }
	
    @Override
	@Transactional(readOnly=false)
	public void refreshProjects(List<ObsProject> list) {
		for (ObsProject p: list) {
			refreshProject(p);
		}
		
	}

    @Override
    @Transactional(readOnly=true)
    public ObsProject getObsProject(ObsUnit ou) {
        ObsUnit oun = ou;
        while (oun.getParent() != null) {
            oun = oun.getParent();
        }
        if(oun.getProject() != null)
            return oun.getProject();
        Query query = getSession().createQuery("select p from ObsProject p join p.obsUnit ou where ou.id = ?");
        query.setParameter(0, oun.getId());
        return (ObsProject) query.uniqueResult();
    }

    @Transactional(readOnly=true)
    public ObsProject findByEntityId(String entityId) {
        Query query = null;
        query = getSession().createQuery("from ObsProject op " +
        		"where op.uid = ?");
        query.setParameter(0, entityId);
        ObsProject prj = (ObsProject)query.uniqueResult();
        if (prj != null) {
        	prj.getObsUnit();
        }
        return prj;
    }


	@Override
	public int countAll() {
        Query query = null;
        query = getSession().createQuery("select count(x) from ObsProject x ");
        return ((Long)query.uniqueResult()).intValue();
	}


	@Override
	@Transactional(readOnly=false, isolation=Isolation.SERIALIZABLE, propagation=Propagation.REQUIRES_NEW)
	public  synchronized void deleteAll() {
		getSession().createQuery("delete from " + ObsProject.class.getCanonicalName()).executeUpdate();
		getSession().createQuery("delete from " + ObsUnit.class.getCanonicalName()).executeUpdate();
		getSession().createQuery("delete from " + SchedBlock.class.getCanonicalName()).executeUpdate();
	}

}
