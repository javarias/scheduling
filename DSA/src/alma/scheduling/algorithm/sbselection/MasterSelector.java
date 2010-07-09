package alma.scheduling.algorithm.sbselection;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class MasterSelector implements SchedBlockSelector {

    protected Collection<SchedBlockSelector> selectors; 
    protected SchedBlockDao sbDao;
    
    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    public Collection<SchedBlockSelector> getSelectors() {
        return selectors;
    }

    public void setSelectors(Collection<SchedBlockSelector> selectors) {
        this.selectors = selectors;
    }

    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
        Conjunction conj = Restrictions.conjunction();
        for (SchedBlockSelector selector : selectors) {
            Criterion c = selector.getCriterion(ut, arrConf);
            if (c == null)
                System.out.println(selector.toString()
                        + " has a null Criterion");
            else
                conj.add(c);
        }
        return conj;
    }

    @Override
    public Collection<SchedBlock> select() throws NoSbSelectedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<SchedBlock> select(Date ut) throws NoSbSelectedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<SchedBlock> select(ArrayConfiguration arrConf)
            throws NoSbSelectedException {

        return null;
    }
    
    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        Date t1= new Date();
        List<SchedBlock> sbs = sbDao.findSchedBlocks(getCriterion(ut, arrConf));
        Date t2 = new Date();
        System.out.println("Size of criteria query: " + sbs.size() );
        System.out.println("Time used: " + (t2.getTime() - t1.getTime()) + " ms");
        return sbs;
    }

    @Override
    public boolean canBeSelected(SchedBlock sb) {
        return false;
    }

}
