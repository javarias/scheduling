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
import alma.obops.dam.ph1m.dao.Ph1mDao;
import alma.obops.dam.ph1m.domain.Proposal;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;

public class Ph1mSynchronizer extends PsmContext {
    
	private static Logger logger = Logger.getLogger(Ph1mSynchronizer.class.getName());
    private Ph1mDao ph1mDao;
    private ObsProjectDao obsProjDao;
    private DataLoader linker;
    private ApplicationContext simCtx;
    
    public Ph1mSynchronizer(String workDir){
    	super(workDir);
    	
        //Scheduling context init
        simCtx = new FileSystemXmlApplicationContext( this.getContextFile() );
        obsProjDao = (ObsProjectDao) simCtx.getBean("obsProjectDao");
        linker = (DataLoader) simCtx.getBean("dataLinker");
        
        //Ph1m context init
        RelationalDbConfig ph1mDbConfig;
        try {
            ph1mDbConfig = new RelationalDbConfig(logger);
            Ph1mContextFactory.INSTANCE.init("/ph1mContext.xml", ph1mDbConfig);
            
        } catch (DbConfigException e) {
            e.printStackTrace();
            System.exit(1);
        }
        ph1mDao = Ph1mContextFactory.INSTANCE.getPh1mDao();
    }
    
    /**
     * 
     * @param p the ObsProject to be synchronized
     * @throws IllegalArgumentException if the project is null of the uid is invalid
     * @throws NullPointerException if the proposal retrieved is null
     */
    public void syncrhonizeProject(ObsProject p) throws IllegalArgumentException, NullPointerException {
        logger.info("Synchronizing proposal: " + p.getUid());
        if(p == null || p.getUid() == null)
            throw new IllegalArgumentException("Obs Project is null or project uid is null ");
        Proposal prop = ph1mDao.findProposalByArchiveUid(p.getUid());
        if(prop == null)
            throw new NullPointerException("Proposal retrieved from Phase1m with uid: " + p.getUid() + " is null");
        p.setScienceRank(prop.getAssessment().getAprcRank());
        if (prop.getAssessment().getAprcScore() == null)
            throw new NullPointerException("aprc Score is null");
        p.setScienceScore(prop.getAssessment().getAprcScore().floatValue());
        if(prop.getCancelled())
        	p.setStatus("CANCELLED");
        obsProjDao.saveOrUpdate(p);
    }
    
    public void synchPh1m() throws IllegalArgumentException {
        List<ObsProject> prjs = obsProjDao.findAll(ObsProject.class);
        for(ObsProject p: prjs){
            try{
                syncrhonizeProject(p);
            }catch(NullPointerException e){
                logger.info("Project " + p.getUid() + " cannot be retrieved from ph1m. Reason: " + e.getCause());
            }
        }
        try {
            linker.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<ProposalComparison> listPh1mProposals(){
    	ArrayList<ProposalComparison> retList = new ArrayList<ProposalComparison>();
        System.out.println("Project UID\tAPRC Score\tAPRC Rank");
        List<ObsProject> prjs = obsProjDao.findAll(ObsProject.class);
        ProposalComparison propC = null;
        for(ObsProject p: prjs){
        	propC = new ProposalComparison();
            Proposal prop = ph1mDao.findProposalByArchiveUid(p.getUid());
            if(prop == null)
                continue;
            propC.setEntityID(p.getUid());
            propC.setPh1mScore(prop.getAssessment().getAprcScore());
            propC.setLocalScore(p.getScienceScore());
            propC.setPh1mRank(prop.getAssessment().getAprcRank());
            propC.setLocalRank(p.getScienceRank());
            retList.add(propC);
            String line =p.getUid() + "\t";
            line += prop.getAssessment().getAprcScore() + "\t";
            line += prop.getAssessment().getAprcRank();
            System.out.println(line);
        }
        return retList;
    }
    
    

}
