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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import alma.archive.database.helpers.wrappers.DbConfigException;
import alma.archive.database.helpers.wrappers.RelationalDbConfig;
import alma.obops.dam.config.Ph1mContextFactory;
import alma.obops.dam.ph1m.constants.LetterGrade;
import alma.obops.dam.ph1m.dao.Ph1mDao;
import alma.obops.dam.ph1m.domain.Proposal;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;

public class Ph1mSynchronizerImpl extends PsmContext implements
		Ph1mSynchronizer {

	private static Logger logger = Logger.getLogger(Ph1mSynchronizerImpl.class
			.getName());
	private Ph1mDao ph1mDao;
	private ObsProjectDao obsProjDao;
	private DataLoader linker;
	private ApplicationContext simCtx;

	
	public Ph1mSynchronizerImpl(String workDir) {
		super(workDir);

		// Scheduling context init
		simCtx = new FileSystemXmlApplicationContext(this.getContextFile());
		obsProjDao = (ObsProjectDao) simCtx.getBean("obsProjectDao");
		linker = (DataLoader) simCtx.getBean("dataLinker");

		// Ph1m context init
		RelationalDbConfig ph1mDbConfig;
		try {
			ph1mDbConfig = new RelationalDbConfig(logger);
			Ph1mContextFactory.INSTANCE.init("/ph1mContext.xml", ph1mDbConfig);

		} catch (DbConfigException e) {
			throw new RuntimeException(e);
		}
		ph1mDao = Ph1mContextFactory.INSTANCE.getPh1mDao();
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
		Proposal prop = null;
		try {
			prop = ph1mDao.findProposalByArchiveUid(p.getUid());
		} catch (javax.persistence.NonUniqueResultException e) {
			p.setStatus("CANCELLED");
			obsProjDao.saveOrUpdate(p);
			System.out.println(e.getMessage());
		}
		if (prop == null){
			p.setStatus("CANCELLED");
			obsProjDao.saveOrUpdate(p);
			throw new NullPointerException(
					"Proposal retrieved from Phase1m with uid: " + p.getUid()
							+ " is null");
		}
		if (prop.getCancelled()){
			p.setStatus("CANCELLED");
			obsProjDao.saveOrUpdate(p);
			return;
		}
		p.setScienceRank(prop.getAssessment().getAprcRank());
		p.setScienceScore(prop.getAssessment().getAprcScore().floatValue());
		p.setLetterGrade(ScienceGrade.valueOf(prop.getAssessment().getAprcLetterGrade().toString()));
		if (prop.getAssessment().getAprcScore() == null || 
				prop.getAssessment().getAprcRank() == null || 
				prop.getAssessment().getAprcLetterGrade() == null ){
			p.setStatus("CANCELLED");
			obsProjDao.saveOrUpdate(p);
			throw new NullPointerException("aprc Score is null");
		}
		obsProjDao.saveOrUpdate(p);
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
		try {
			linker.load();
		} catch (Exception e) {
			e.printStackTrace();
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
			Proposal prop = null;
			try {
				prop = ph1mDao.findProposalByArchiveUid(p.getUid());
			} catch (javax.persistence.NonUniqueResultException e) {
				System.out.println(e.getMessage());
			}
			if (prop == null)
				continue;
			if (prop.getCancelled())
				continue;
			
			propC.setEntityID(p.getUid());

			if (prop.getAssessment() == null) {
				System.out
						.println("ERROR: Ph1m return null on prop.getAssessment()");
				continue;
			}
			if (prop.getAssessment().getAprcScore() == null) {
				System.out
						.println("ERROR: Ph1m return null on prop.getAssessment().getAprcScore()");
				propC.setPh1mScore(9999);
				continue;
			}
			propC.setPh1mScore(prop.getAssessment().getAprcScore());
			propC.setLocalScore(p.getScienceScore());
			propC.setPh1mRank(prop.getAssessment().getAprcRank());
			propC.setLocalRank(p.getScienceRank());
			propC.setPh1mGrade(ScienceGrade.valueOf(prop.getAssessment().getAprcLetterGrade().toString()));
			propC.setLocalGrade(p.getLetterGrade());
			retList.add(propC);
			String line = p.getUid() + "\t";
			line += prop.getAssessment().getAprcScore() + "\t";
			line += prop.getAssessment().getAprcRank() + "\t";
			line += prop.getAssessment().getAprcLetterGrade() + "\t";
			System.out.println(line);
		}
		return retList;
	}

}
