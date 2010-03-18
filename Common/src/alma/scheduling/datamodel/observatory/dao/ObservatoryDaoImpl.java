package alma.scheduling.datamodel.observatory.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.observatory.AntennaInstallation;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;

@Transactional
public class ObservatoryDaoImpl extends GenericDaoImpl implements ObservatoryDao {

    @Override
    public List<ArrayConfiguration> findArrayConfigurations() {
        // hydrate the antenna installations
        List<ArrayConfiguration> arrCnfs = findAll(ArrayConfiguration.class);
        for (Iterator<ArrayConfiguration> iter = arrCnfs.iterator(); iter.hasNext();) {
            ArrayConfiguration ac = iter.next();
            Set<AntennaInstallation> ais = ac.getAntennaInstallations();
            for (Iterator<AntennaInstallation> iter2 = ais.iterator(); iter2.hasNext();) {
                AntennaInstallation ai = iter2.next();
                ai.getAntenna().getDiameter();
                ai.getPad();
            }
        }
        return arrCnfs;
    }

}
