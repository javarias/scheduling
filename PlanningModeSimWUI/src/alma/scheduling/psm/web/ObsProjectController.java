package alma.scheduling.psm.web;

import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zkplus.spring.SpringUtil;

import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;

public class ObsProjectController extends GenericForwardComposer implements Initiator {

	private static final long serialVersionUID = -2202514334580824679L;

	public void doAfterCompose(Page arg0) throws Exception {
		System.out.println("doAfterCompose(page) called");

	}

	@Override
	public boolean doCatch(Throwable arg0) throws Exception {
		return false;
	}

	@Override
	public void doFinally() throws Exception {
	}

	public void doInit(Page arg0, Map arg1) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("doInit(page, Map) called");
		ObsProjectDao obsProjectDao = (ObsProjectDao) SpringUtil.getBean("obsProjectDao");
		List list = obsProjectDao.getObsProjectsOrderBySciRank();
		arg0.setVariable("allObsProjects", list);
		
	}
}