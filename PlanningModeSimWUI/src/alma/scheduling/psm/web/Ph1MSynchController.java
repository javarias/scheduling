package alma.scheduling.psm.web;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Grid;

import alma.scheduling.psm.util.Ph1mSynchronizer;
import java.util.List;
import alma.scheduling.psm.util.ProposalComparison;

public class Ph1MSynchController extends GenericForwardComposer {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 123786487338L;
	
	public void onOpen$windowPh1MSynch(Event event){
		Ph1mSynchronizer ph1mSync = (Ph1mSynchronizer)SpringUtil.getBean("ph1mSynchronizerService");
		List<ProposalComparison> propList = ph1mSync.listPh1mProposals();
		String[][] model = new String[propList.size()][6];
		for(int i = 0; i < propList.size(); i++){
			model[i][0]=propList.get(i).getEntityID();
			model[i][1]=Double.toString(propList.get(i).getLocalScore());
			model[i][3]=Integer.toString(propList.get(i).getLocalRank());
			model[i][3]=propList.get(i).getEntityID();
			model[i][4]=Double.toString(propList.get(i).getPh1mScore());
			model[i][5]=Integer.toString(propList.get(i).getPh1mRank());
		}
		
		 ListModel strset = new SimpleListModel(model);
		 List<Component> children = event.getTarget().getChildren();
		 Grid grid = null;
		 for(Component c: children){
			 if(c.getId().compareTo("grid") == 0){
				 grid = (Grid) c;
				 break;
			 }
		 }
		 if(grid == null)
			 return;
		 grid.setModel(strset);
		 grid.setRowRenderer(new alma.scheduling.psm.web.Ph1MSychGridRowRenderer());
		
	}

	

}
