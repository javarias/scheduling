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
package alma.scheduling.datamodel.weather;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.persistence.HibernateUtil;

public class AtmParametersTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(AtmParametersTest.class);
    private Session session;
    
    public AtmParametersTest(String name) {
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
 
    public void testSimpleRecordCreation() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            AtmParameters p1 = new AtmParameters();
            p1.setPWV(0.0);
            p1.setFreq(0.0);
            p1.setOpacity(0.0);
            p1.setAtmBrightnessTemp(0.0);
            session.save(p1);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
        try {
            tx = session.beginTransaction();
            session.createQuery("DELETE FROM AtmParameters").executeUpdate();
            tx.commit();            
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }    
}
