package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;

public class ProjectCodeSelector extends AbstractBaseSelector {

	private ObsProjectDao prjDao;
	private String code;
	
	public ProjectCodeSelector(String selectorName) {
		super(selectorName);
	}

	@Override
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		throw new RuntimeException("Not implemented");
		//return null;
	}

//	@Override
//	public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//		List<String> uids = prjDao.getObsProjectsUidsByCode(code);
//		if (uids.size() == 0)
//			return null;
//		return Restrictions.in("projectUid", uids);
//	}

	public ObsProjectDao getPrjDao() {
		return prjDao;
	}

	public void setPrjDao(ObsProjectDao prjDao) {
		this.prjDao = prjDao;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
