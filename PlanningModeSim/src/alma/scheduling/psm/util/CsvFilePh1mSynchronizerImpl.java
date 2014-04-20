package alma.scheduling.psm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.utils.DSAContextFactory;

public class CsvFilePh1mSynchronizerImpl extends PsmContext implements
		Ph1mSynchronizer {

	private static Logger logger = Logger.getLogger(CsvFilePh1mSynchronizerImpl.class.getName());
	private ObsProjectDao obsProjDao;
	private ApplicationContext simCtx;
	private Reader input;
	private Map<String, SynchResult> synchData = null;
	
	public CsvFilePh1mSynchronizerImpl(String workDir, Reader input) {
		super(workDir);

		// Scheduling context init
		simCtx = DSAContextFactory.getContext();
		obsProjDao = (ObsProjectDao) simCtx.getBean("obsProjectDao");
		this.input = input;
	}

	@Override
	public void setWorkDir(String workDir) {
	}

	@Override
	public void syncrhonizeProject(ObsProject p) throws RemoteException {
		logger.info("Synchronizing proposal: " + p.getUid());
		if (p == null || p.getUid() == null)
			throw new IllegalArgumentException(
					"Obs Project is null or project uid is null ");
		SynchResult rs = synchData.get(p.getCode());
		if (rs == null) {
			logger.severe("Not found data in the CSV file for project code: " + p.getCode());
			return;
		}
		String letterGrade = rs.getLetterGrade();
		Integer rank = rs.getRank();
		if (rank != null)
			p.setScienceRank(rank);
//		p.setScienceScore(prop.getArpScore().floatValue());
		if (letterGrade != null) {
			if (letterGrade.equals("A") || letterGrade.equals("B") || letterGrade.equals("C"))
				p.setLetterGrade(ScienceGrade.valueOf(letterGrade));
			else
				p.setLetterGrade(ScienceGrade.valueOf("D"));
		}
		System.out.println("Propagating grades to SBs");
		propagateChangesToObsUnit(p);
		System.out.println("Propagation to SBs done.");
		obsProjDao.saveOrUpdate(p);
	}

	@Override
	public void synchPh1m() throws RemoteException {
		List<ObsProject> prjs = obsProjDao.findAll(ObsProject.class);
		for (ObsProject p : prjs) {
			try {
				syncrhonizeProject(p);
			} catch (NullPointerException e) {
				logger.info("Project " + p.getUid()
						+ " cannot be retrieved from ph1m. Reason: "
						+ e.getCause());
			}
		}
	}

	@Override
	public List<ProposalComparison> listPh1mProposals() throws RemoteException {
		if(synchData == null)
			readFromInput();
		ArrayList<ProposalComparison> retList = new ArrayList<ProposalComparison>();
		System.out.println("Project UID\tAPRC Score\tAPRC Rank\tGrade");
		List<ObsProject> prjs = obsProjDao.findAll(ObsProject.class);
		ProposalComparison propC = null;
		for (ObsProject p : prjs) {
			propC = new ProposalComparison();
			SynchResult rs = synchData.get(p.getCode());
			if (rs == null) {
				logger.severe("Not found data in the CSV file for project code: " + p.getCode());
				continue;
			}
			propC.setEntityID(p.getUid());
			String letterGrade = rs.getLetterGrade();
			Integer rank = rs.getRank();

			propC.setPh1mScore(1.0);
			propC.setLocalScore(p.getScienceScore());
			propC.setLocalGrade(p.getLetterGrade());
			if (p.getScienceRank() != null)
				propC.setLocalRank(p.getScienceRank());
			propC.setPh1mGrade(ScienceGrade.valueOf(letterGrade));
			propC.setPh1mRank(rank);
			retList.add(propC);
			String line = p.getUid() + "\t";
			line += rank + "\t";
			line += letterGrade + "\t";
			System.out.println(line);
		}
		return retList;
	}
	
	private void readFromInput() {
		Ph1mCsvFileReader reader = new Ph1mCsvFileReader(input);
		try {
			synchData = reader.getSynchData();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				//Nothing to do
			}
		}
	}

	
	private void propagateChangesToObsUnit(ObsProject p) {
		obsProjDao.hydrateSchedBlocks(p);
		propagateChangesToObsUnit(p, p.getObsUnit());
	}
	
	private void propagateChangesToObsUnit(ObsProject p, ObsUnit ou) {
		if (ou instanceof ObsUnitSet) {
			for (ObsUnit ouc: ((ObsUnitSet)ou).getObsUnits())
				propagateChangesToObsUnit(p, ouc);
		} else if (ou instanceof SchedBlock) {
			SchedBlock sb = (SchedBlock) ou;
			sb.setLetterGrade(p.getLetterGrade());
			sb.setScienceRank(p.getScienceRank());
			sb.setScienceScore(p.getScienceScore());
		}
	}
	
	private static class Ph1mCsvFileReader extends BufferedReader {

		public Ph1mCsvFileReader(Reader in) {
			super(in);
		}
		
		public Map<String, SynchResult> getSynchData() throws IOException {
			HashMap<String, SynchResult> ret = new HashMap<>();
			String line;
			long lineno = -1;
			while ((line = readLine()) != null) {
				lineno++;
				if (lineno == 0)
					continue;
				String[] fields = line.split(",");
				String letterGrade = fields[19];
				if (!(letterGrade.equalsIgnoreCase("A") || letterGrade.equalsIgnoreCase("B") || letterGrade.equalsIgnoreCase("C")))
						letterGrade = "D";
				int rank = new Integer(fields[18]).intValue();
				ret.put(fields[1], new SynchResult(letterGrade, rank, 0.0));
			}
			return ret;
		}
	}

	
}
