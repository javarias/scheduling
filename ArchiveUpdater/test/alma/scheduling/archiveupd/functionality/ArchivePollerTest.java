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
package alma.scheduling.archiveupd.functionality;

import java.util.logging.Logger;

import junit.framework.TestCase;
import alma.scheduling.archiveupd.functionality.ArchivePoller;
import alma.scheduling.datamodel.obsproject.dao.ArchiveObsProjectDaoTest;
import alma.scheduling.utils.LoggerFactory;

public class ArchivePollerTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ArchiveObsProjectDaoTest.class);
    
    public ArchivePollerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testArchivePoller() throws Exception {
    	ArchivePoller ap = new ArchivePoller(logger);
    	boolean keepGoing = true;
    	
//    	while (keepGoing) {
    		ap.pollArchive();
//    		int r = javax.swing.JDialog.showConfirmDialog(
//    				null, "Archive Poll has run. Do more?",
//    				"Keep Polling", javax.swing.JOptionPane.YES_NO_OPTION);
//    		keepGoing = (r == javax.swing.JOptionPane.YES_OPTION);
//     	}
    }
}
