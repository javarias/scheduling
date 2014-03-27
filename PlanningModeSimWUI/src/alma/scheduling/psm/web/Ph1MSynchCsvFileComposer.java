package alma.scheduling.psm.web;

import java.rmi.RemoteException;
import java.util.List;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Grid;

import alma.scheduling.psm.util.CsvFilePh1mSynchronizerImpl;
import alma.scheduling.psm.util.ProposalComparison;

public class Ph1MSynchCsvFileComposer extends GenericForwardComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3287239876348951L;
	
	private CsvFilePh1mSynchronizerImpl sync;
	
	Grid dataGrid;
	Button synchPh1mButton;
	Button loadFromCSVFileButton;
	
	
	public void onUpload$loadFromCSVFileButton(UploadEvent event) {
		Media media = event.getMedia();
		loadFromCSVFileButton.setDisabled(true);
		try {
			sync = new CsvFilePh1mSynchronizerImpl(System.getenv("APRC_WORK_DIR"), media.getReaderData());
			fillGrid();
		} finally {
			loadFromCSVFileButton.setDisabled(false);
		}
	}
	
	public void onClick$synchPh1mButton(Event event) {
		ph1mSyncronize();
	}
	
	private void fillGrid() {
		List<ProposalComparison> propList;
		try{
			propList = sync.listPh1mProposals();
		}catch(java.rmi.RemoteException ex){
			ex.printStackTrace();
			return;
		}
		
		String[][] model = new String[propList.size()][8];
		for(int i = 0; i < propList.size(); i++){
			model[i][0]=propList.get(i).getEntityID();
			model[i][1]=Double.toString(propList.get(i).getLocalScore());
			model[i][2]=Integer.toString(propList.get(i).getLocalRank());
			model[i][3]=propList.get(i).getLocalGrade().toString();
			model[i][4]=propList.get(i).getEntityID();
			model[i][5]=Double.toString(propList.get(i).getPh1mScore());
			model[i][6]=Integer.toString(propList.get(i).getPh1mRank());
			model[i][7]=propList.get(i).getPh1mGrade().toString();
		}
		
		 ListModel strset = new SimpleListModel(model);
		 dataGrid.setModel(strset);
		 dataGrid.setRowRenderer(new alma.scheduling.psm.web.Ph1MSychGridRowRenderer());
		 if(propList.size() > 0)
			 synchPh1mButton.setDisabled(false);
	}
	
	private void ph1mSyncronize(){
		try {
			sync.synchPh1m();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		fillGrid();
		synchPh1mButton.setDisabled(true);
	}
}
