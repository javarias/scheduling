package alma.scheduling.datamodel.weather.dao;

import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.weather.AtmParameters;

@Transactional
public class AtmParametersDaoImpl extends HibernateDaoSupport implements AtmParametersDao {

    @Override
    public void loadAtmParameter(Object domainObject) {
        getHibernateTemplate().save(domainObject);
    }

    @Override
    public Double[] getEnclosingPwvInterval(Double pwv) {
        Double[] retVal = new Double[2];
        Query query;
        query = getSession().getNamedQuery("AtmParameters.getPwvIntervalLowerBound");
        query.setParameter(0, pwv);
        retVal[0] = (Double) query.uniqueResult();
        query = getSession().getNamedQuery("AtmParameters.getPwvIntervalUpperBound");
        query.setParameter(0, pwv);
        retVal[1] = (Double) query.uniqueResult();
        if (retVal[0] == null) {
            retVal[0] = retVal[1];
        }
        return retVal;
    }

    @Override
    public AtmParameters[] getEnclosingIntervalForPwvAndFreq(Double pwv, Double freq) {
        AtmParameters[] retVal = new AtmParameters[2];
        Query query;
        query = getSession().getNamedQuery("AtmParameters.getIntervalLowerBound");
        query.setParameter(0, freq);
        query.setParameter(1, pwv);
        query.setMaxResults(1);
        retVal[0] = (AtmParameters) query.uniqueResult();
        query = getSession().getNamedQuery("AtmParameters.getIntervalUpperBound");
        query.setParameter(0, freq);
        query.setParameter(1, pwv);
        query.setMaxResults(1);
        retVal[1] = (AtmParameters) query.uniqueResult();        
        return retVal;
    }
    
}
