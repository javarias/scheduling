package alma.scheduling.psm.web;

import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Window;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zkplus.spring.SpringUtil;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;

public class ObsProjectController extends GenericForwardComposer implements Initiator {

	private static final long serialVersionUID = -2202514334580824679L;

	@Override
	public void doAfterCompose(Page arg0) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("doAfterCompose(page) called");

	}

	@Override
	public boolean doCatch(Throwable arg0) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doFinally() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void doInit(Page arg0, Map arg1) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("doInit(page, Map) called");
		ObsProjectDao obsProjectDao = (ObsProjectDao) SpringUtil.getBean("obsProjectDao");
		List list = obsProjectDao.getObsProjectsOrderBySciRank();
		arg0.setVariable("allObsProjects", list);
		
	}
}