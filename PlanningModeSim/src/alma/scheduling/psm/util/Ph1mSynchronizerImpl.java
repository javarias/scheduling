/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */

package alma.scheduling.psm.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;

import alma.archive.exceptions.general.DatabaseException;
import alma.archive.xml.dao.HibernateXmlStoreDaoImpl;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.psm.util.ProposalComparison;
import alma.scheduling.psm.util.PsmContext;
import alma.scheduling.utils.DSAContextFactory;

public class Ph1mSynchronizerImpl extends PsmContext implements Ph1mSynchronizer {

	private static Logger logger = Logger.getLogger(Ph1mSynchronizerImpl.class
			.getName());
	private ObsProjectDao obsProjDao;
	private DataLoader linker;
	private ApplicationContext simCtx;
	private HibernateXmlStoreDaoImpl archiveDao;

	
	public Ph1mSynchronizerImpl(String workDir) {
		super(workDir);

		// Scheduling context init
		simCtx = DSAContextFactory.getContext();
		obsProjDao = (ObsProjectDao) simCtx.getBean("obsProjectDao");
		linker = (DataLoader) simCtx.getBean("dataLinker");
		try {
			archiveDao = new HibernateXmlStoreDaoImpl();
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void setWorkDir(String workDir){
		
	}

	/**
	 * 
	 * @param p
	 *            the ObsProject to be synchronized
	 * @throws IllegalArgumentException
	 *             if the project is null of the uid is invalid
	 * @throws NullPointerException
	 *             if the proposal retrieved is null
	 */
	@Override
	public void syncrhonizeProject(ObsProject p)
			throws IllegalArgumentException, NullPointerException {
		logger.info("Synchronizing proposal: " + p.getUid());
		if (p == null || p.getUid() == null)
			throw new IllegalArgumentException(
					"Obs Project is null or project uid is null ");
		SynchResult rs = null;
		try {
			rs = getProposalInformation(p.getUid());
			if (rs == null)
				return;
			String letterGrade = rs.getLetterGrade();
			Integer rank = rs.getRank();
			if (rank != null)
				p.setScienceRank(rank);
//			p.setScienceScore(prop.getArpScore().floatValue());
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
		} catch (SQLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void synchPh1m() throws IllegalArgumentException {
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
	public List<ProposalComparison> listPh1mProposals() {
		ArrayList<ProposalComparison> retList = new ArrayList<ProposalComparison>();
		System.out.println("Project UID\tAPRC Score\tAPRC Rank\tGrade");
		List<ObsProject> prjs = obsProjDao.findAll(ObsProject.class);
		ProposalComparison propC = null;
		for (ObsProject p : prjs) {
			propC = new ProposalComparison();
			SynchResult rs = null;
			try {
				rs = getProposalInformation(p.getUid());
			if (rs == null)
				continue;
			
			propC.setEntityID(p.getUid());
			String letterGrade = rs.getLetterGrade();
			Integer rank = rs.getRank();

			propC.setPh1mScore(0);
			propC.setLocalScore(p.getScienceScore());
			if (rank != null)
				propC.setPh1mRank(rank);
			else
				propC.setPh1mRank(99999);
			propC.setLocalRank(p.getScienceRank());
			if (letterGrade != null) {
				if (letterGrade.equals("A") || letterGrade.equals("B") || letterGrade.equals("C"))
					propC.setPh1mGrade(ScienceGrade.valueOf(letterGrade));
				else
					propC.setPh1mGrade(ScienceGrade.valueOf("D"));
			} else
				propC.setPh1mGrade(ScienceGrade.valueOf("D"));
			propC.setLocalGrade(p.getLetterGrade());
			retList.add(propC);
			String line = p.getUid() + "\t";
			line += rank + "\t";
			line += letterGrade + "\t";
			System.out.println(line);
			} catch (SQLException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return retList;
	}
	
	private SynchResult getProposalInformation(String uid) throws SQLException {
		Connection conn = archiveDao.getConnection();
		Statement stmt = null;
		SynchResult res = null;
		try {
			stmt = conn.createStatement();
			String qString = "select APRC_RANK, APRC_LETTER_GRADE from proposal where ARCHIVE_UID = '" + uid +"'";
			System.out.println("Query: " + qString);
			ResultSet rs = stmt.executeQuery(qString);
			if (!rs.next())
				return res;
			res = new SynchResult(rs.getString("APRC_LETTER_GRADE"), rs.getInt("APRC_RANK"), 1.0);
		} finally {
			if (stmt != null)
				stmt.close();
			archiveDao.closeSession();
		}
		return res;
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
	
	private static class SynchResult {
		private String letterGrade;
		private Integer rank;
		private Double score;
		
		public SynchResult(String letterGrade, Integer rank, Double score) {
			super();
			this.letterGrade = letterGrade;
			this.rank = rank;
			this.score = score;
		}
		
		public String getLetterGrade() {
			return letterGrade;
		}
		public void setLetterGrade(String letterGrade) {
			this.letterGrade = letterGrade;
		}
		public Integer getRank() {
			return rank;
		}
		public void setRank(Integer rank) {
			this.rank = rank;
		}
		public Double getScore() {
			return score;
		}
		public void setScore(Double score) {
			this.score = score;
		}
		
		
	}

}
