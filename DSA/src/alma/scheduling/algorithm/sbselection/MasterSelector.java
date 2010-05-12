package alma.scheduling.algorithm.sbselection;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class MasterSelector extends HibernateDaoSupport implements SchedBlockSelector {

    private Collection<SchedBlockSelector> selectors; 
    
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

    @SuppressWarnings("unchecked")
    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        Criteria criteria = this.getSession().createCriteria(SchedBlock.class);
        criteria
                .createAlias("schedulingConstraints.representativeTarget", "rt")
                .setFetchMode("rt", FetchMode.JOIN).createAlias(
                        "schedulingConstraints.representativeTarget.source",
                        "s").setFetchMode("s", FetchMode.JOIN).createAlias(
                        "executive", "exec").setFetchMode("exec",
                        FetchMode.JOIN).createAlias(
                        "executive.executivePercentage", "ep").setFetchMode(
                        "ep", FetchMode.JOIN);
        criteria.add(getCriterion(ut, arrConf));
        Date t1= new Date();
        List<SchedBlock> sbs = criteria.list();
        Date t2 = new Date();
        System.out.println("Size of criteria query: " + sbs.size() );
        System.out.println("Time used: " + (t2.getTime() - t1.getTime()) + " ms");
        return sbs;
    }

}
