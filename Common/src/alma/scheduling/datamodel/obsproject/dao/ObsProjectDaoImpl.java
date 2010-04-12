package alma.scheduling.datamodel.obsproject.dao;

import org.hibernate.LockMode;
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
        ObsUnit ou = (ObsUnit) getHibernateTemplate().get(ObsUnit.class, id);
        hydrateObsUnit(ou);
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
    
}
