package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;

@Transactional
public class ObsProjectDaoImpl extends GenericDaoImpl implements ObsProjectDao {

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
    public void saveOrUpdate(ObsProject prj) {
        super.saveOrUpdate(prj);
    }


    @Override
    public ObsProject getObjsProject(ObsUnit ou) {
        ObsUnit oun = ou;
        while (oun.getParent() != null) {
            oun = oun.getParent();
        }
        Query query = getSession().createQuery("select p from ObsProject p join p.obsUnit ou where ou.id = ?");
        query.setParameter(0, oun.getId());
        return (ObsProject) query.uniqueResult();
    }
    
}
