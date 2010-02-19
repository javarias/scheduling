package alma.scheduling.dataload.obsproject;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.XmlObsProjectDao;

@Transactional
public class ObsProjectDataLoader implements DataLoader {

    XmlObsProjectDao xmlDao;
    
    ObsProjectDao dao;
    
    public void setXmlDao(XmlObsProjectDao xmlDao) {
        this.xmlDao = xmlDao;
    }

    public void setDao(ObsProjectDao dao) {
        this.dao = dao;
    }

    @Override
    public void load() {
        List<ObsProject> projects = xmlDao.getAllObsProjects();
        dao.saveOrUpdate(projects);
    }

}
