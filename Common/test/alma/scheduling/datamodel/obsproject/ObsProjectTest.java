/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.datamodel.obsproject;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.scheduling.persistence.HibernateUtil;

import junit.framework.TestCase;

public class ObsProjectTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ObsProjectTest.class);
    private Session session;
    
    public ObsProjectTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        session = HibernateUtil.getSessionFactory().openSession();
    }

    protected void tearDown() throws Exception {
        session.close();
        HibernateUtil.shutdown();        
        super.tearDown();
    }
 
    public void testSimpleObsProject() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            ObsProject prj = new ObsProject();
            prj.setScienceScore(3.14f);
            prj.setScienceRank(10);
            prj.setPrincipalInvestigator("me");
            prj.setStatus("ready");
            ProjectStatusEntityT statusEntity = new ProjectStatusEntityT();
            statusEntity.setEntityId("uid://X0/X0");
            statusEntity.setEntityIdEncrypted("iud0831k");
            statusEntity.setEntityTypeName("ProjectStatus");
            statusEntity.setDatamodelVersion("1.0");
            statusEntity.setDocumentVersion("1.0");
            statusEntity.setSchemaVersion("1.0");
            statusEntity.setTimestamp("0");
            prj.setStatusEntity(statusEntity);
            session.save(prj);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }

    public void testObsProjectWithObsUnit() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            ObsProject prj = new ObsProject();
            prj.setScienceScore(3.14f);
            prj.setScienceRank(10);
            prj.setPrincipalInvestigator("me");
            prj.setStatus("ready");
            SchedBlock sb = new SchedBlock();
            sb.setPiName("me");
            sb.setWeatherConstraints(new WeatherConstraints(0.0, 0.0, 0.0, 0.0));
            prj.setObsUnit(sb);
            sb.setProject(prj);
            session.save(prj);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }

}
