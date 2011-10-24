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

import alma.scheduling.persistence.HibernateUtil;

import junit.framework.TestCase;

public class FieldSourceTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(FieldSourceTest.class);
    private Session session;
    
    public FieldSourceTest(String name) {
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
 
    public void testSimpleFieldSourceCreation() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            FieldSource src = new FieldSource("0000+000", new SkyCoordinates(0.0, 0.0), 0.0, 0.0);
            session.save(src);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }
    
}
