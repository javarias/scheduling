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
 * "@(#) $Id: ExecutiveDaoImpl.java,v 1.19 2010/07/27 22:38:00 ahoffsta Exp $"
 */
package alma.scheduling.datamodel.executive.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.SchedBlockExecutorImpl;
import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.psm.sim.Simulator;

public class ExecutiveDaoImpl extends GenericDaoImpl implements ExecutiveDAO {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveDaoImpl.class);
    
    @Override
    @Transactional(readOnly=true)
    public List<Executive> getAllExecutive() {
        List<Executive> execs =  findAll(Executive.class);
        for (Executive exec : execs) {
            for (ExecutivePercentage ep : exec.getExecutivePercentage()) {
                ObservingSeason o = ep.getSeason();
                o.getStartDate();
            }            
        }
        return execs;
    }
    
    
    @Override
    @Transactional(readOnly=true)
    public List<ObservingSeason> getAllObservingSeason() {
        return this.findAll(ObservingSeason.class);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PI> getAllPi() {
        List<PI> pis = this.findAll(PI.class);
        for(PI pi: pis)
            for(PIMembership pim :pi.getPIMembership())
                pim.getExecutive();
        return pis;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public ObservingSeason getCurrentSeason() {
        List<ObservingSeason> os;
        Query query = getSession().createQuery("from ObservingSeason as os " +
                "order by os.startDate desc");
        query.setMaxResults(1);
        os = query.list();
        return os.get(0);
    }

    @Override
    @Transactional(readOnly=true)
    public Executive getExecutive(String piEmail) {
        PI pi = findById(PI.class, piEmail);
        Executive exec = pi.getPIMembership().iterator().next().getExecutive();
        for (ExecutivePercentage ep : exec.getExecutivePercentage()) {
            ObservingSeason o = ep.getSeason();
            o.getStartDate();
        }
        return exec;
    }

    @Override
    @Transactional(readOnly=true)
    public PI getPIFromEmail(String piEmail) {
        PI pi = findById(PI.class, piEmail);
        for( PIMembership pim : pi.getPIMembership() ){
        	pim.getMembershipPercentage();
        }
        return pi;
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex,
            ObservingSeason os) {
        Object[] args= new Object[2];
        args[0] = os.getStartDate();
        args[1] = ex.getName();
        return this.executeNamedQuery(
                "ExecutiveTimeSpent.findBySeasonAndExecutive", args);
    }
    
    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete PIMembership");        
        getHibernateTemplate().bulkUpdate("delete PI");
        getHibernateTemplate().bulkUpdate("delete ExecutivePercentage");
        getHibernateTemplate().bulkUpdate("delete ExecutiveTimeSpent");
        getHibernateTemplate().bulkUpdate("delete Executive");
        getHibernateTemplate().bulkUpdate("delete ObservingSeason");
    }


    @Override
    @Transactional
    public void saveObservingSeasonsAndExecutives(List<ObservingSeason> seasons,
            List<Executive> executives) {
        List<ExecutivePercentage> eps = new ArrayList<ExecutivePercentage>();
        ArrayList<ObservingSeason> ss = new ArrayList<ObservingSeason>();
        List<Executive> execs = new ArrayList<Executive>();
        for (ObservingSeason season : seasons) {
            saveOrUpdate(season);
            for (ExecutivePercentage ep : season.getExecutivePercentage()) {
                saveOrUpdate(ep.getExecutive());
                ss.add(season);
                execs.add(ep.getExecutive());
                eps.add(ep);
            }
        }
        // A new ExecutivePercentage needs to be created after the ObservingSeason
        // and the Executive have been saved so its composite ID is setup
        // with non-null references to these objects.
        // The Lists need to be converted to arrays to avoid a concurrent modification
        // exception, which results from ExecutivePercentage constructor modification of
        // the ObservingSeason and ExecutivePercentage.
        ObservingSeason[] ssarr = ss.toArray(new ObservingSeason[0]);
        Executive[] execsarr = execs.toArray(new Executive[0]);
        ExecutivePercentage[] eparr = eps.toArray(new ExecutivePercentage[0]);
        for (int i = 0; i < ssarr.length; i++) {
            ObservingSeason s = ssarr[i];
            Executive e = execsarr[i];
            ExecutivePercentage ep = eparr[i];
            saveOrUpdate(new ExecutivePercentage(s, e, ep.getPercentage(), ep.getTotalObsTimeForSeason()));
        }
        // Save the executives
        for (Executive e : executives) {
            saveOrUpdate(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public ExecutivePercentage getExecutivePercentage(Executive exec, ObservingSeason os) {
    	logger.info("Passed x0");
        List<ExecutivePercentage> ep;
        Query query = getSession().createQuery("from ExecutivePercentage as ep " +
                "where ep.season = ? and ep.executive = ?");
        logger.info("Passed x1");
        query.setMaxResults(1);
        logger.info("Passed x2");
        query.setParameter(0, os);
        logger.info("Passed x3: observing season: " + os.getId() );
        query.setParameter(1, exec);
        logger.info("Passed x4: executive: " + exec );
        ep = query.list();
        logger.info("Passed x4.5: Return size of query: " + ep.size() );
        logger.info("Passed x5");
        return ep.get(0);        
    }
}