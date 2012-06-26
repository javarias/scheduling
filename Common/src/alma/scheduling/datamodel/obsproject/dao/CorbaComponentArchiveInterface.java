/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.datamodel.obsproject.dao;

import org.omg.CORBA.UserException;

import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsreview.ObsReview;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.projectlifecycle.StateSystemOperations;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.OperationalOperations;

public class CorbaComponentArchiveInterface extends AbstractArchiveInterface
		implements ArchiveInterface {


	public CorbaComponentArchiveInterface(OperationalOperations archive,
			StateSystemOperations stateSystem,
			EntityDeserializer entityDeserializer,
			EntitySerializer entitySerializer) {
		super(archive, stateSystem, entityDeserializer, entitySerializer);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getObsProject(java.lang.String)
	 */
	@Override
	public ObsProject getObsProject(String id)
								throws EntityException, UserException {
		ObsProject result = null;
		if (hasObsProject(id)) {
			result = cachedObsProject(id);
		} else {
			final XmlEntityStruct xml = archive.retrieve(id);
			result = (ObsProject) entityDeserializer.
				deserializeEntity(xml, ObsProject.class);
			obsProjects.put(id, result);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getObsProposal(java.lang.String)
	 */
	@Override
	public ObsProposal getObsProposal(String id)
								throws EntityException, UserException {
		ObsProposal result = null;
		if (hasObsProposal(id)) {
			result = cachedObsProposal(id);
		} else {
			final XmlEntityStruct xml = archive.retrieve(id);
			result = (ObsProposal) entityDeserializer.
				deserializeEntity(xml, ObsProposal.class);
			obsProposals.put(id, result);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getObsReview(java.lang.String)
	 */
	@Override
	public ObsReview getObsReview(String id)
								throws EntityException, UserException {
		ObsReview result = null;
		if (hasObsReview(id)) {
			result = cachedObsReview(id);
		} else {
			final XmlEntityStruct xml = archive.retrieve(id);
			result = (ObsReview) entityDeserializer.
				deserializeEntity(xml, ObsReview.class);
			obsReviews.put(id, result);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getSchedBlock(java.lang.String)
	 */
	@Override
	public SchedBlock getSchedBlock(String id)
								throws EntityException, UserException {
		SchedBlock result = null;
		if (hasSchedBlock(id)) {
			result = cachedSchedBlock(id);
		} else {
			final XmlEntityStruct xml = archive.retrieve(id);
			result = (SchedBlock) entityDeserializer.
				deserializeEntity(xml, SchedBlock.class);
			schedBlocks.put(id, result);
		}
		return result;
	}
    
}
