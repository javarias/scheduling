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
 * "@(#) $Id: XmlObsProjectDaoImpl.java,v 1.1 2010/02/17 21:39:01 rhiriart Exp $"
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.io.File;
import java.util.List;

import alma.scheduling.datamodel.obsproject.ObsProject;

/**
 * A DAO for ObsProject XML files.
 *
 */
public class XmlObsProjectDaoImpl implements ObsProjectDao {

    /** Input file or directory */
    private File input;
    
    public XmlObsProjectDaoImpl() { }
    
    @Override
    public List<ObsProject> getAllObsProjects() {
        // TODO Auto-generated method stub
        return null;
    }

}
