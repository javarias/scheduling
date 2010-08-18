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
 * "@(#) $Id: ProjectIncrementalDao.java,v 1.3 2010/08/18 16:31:10 dclarke Exp $"
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import alma.scheduling.Define.DateTime;
import alma.scheduling.datamodel.DAOException;

/**
 * @author dclarke
 *
 */
public interface ProjectIncrementalDao extends ProjectDao {
    /**
     * Work out all the ObsProjects that have changed since the given
     * time. For new or modified projects, stick their entity ids into
     * <code>newOrModifiedIds</code> and for those which are deleted,
     * stick their entity ids into <code>deleted</code>.
     * 
     * @param since - the time of the last search, we're looking for
     *                changes since then.
     * @param newOrModifiedIds - used to return the entity ids of
     *                           projects which are new or have been
     *                           modified.
     * @param deletedIds - used to return the entity ids of projects
     *                     which have been deleted (or become inactive
     *                     in some way).
     * @throws DAOException - if there's a non-recoverable problem (but
     *                        not if individual projects fail).
     */
    void getObsProjectChanges(
    		final DateTime     since,
    		final List<String> newOrModifiedIds,
    		final List<String> deletedIds) throws DAOException;
}
