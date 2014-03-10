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
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.xml.ObsProjectEntity;
import alma.archive.xml.ObsProposalEntity;
import alma.archive.xml.ObsReviewEntity;
import alma.archive.xml.SchedBlockEntity;
import alma.archive.xml.dao.HibernateXmlStoreDaoImpl;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsreview.ObsReview;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.lifecycle.persistence.StateArchive;
import alma.xmlstore.OperationalOperations;

public final class HibernateArchiveInterface extends AbstractArchiveInterface
		implements ArchiveInterface {

	public HibernateXmlStoreDaoImpl dao;
	
	public HibernateArchiveInterface(OperationalOperations archive,
			StateArchive stateSystem,
			EntityDeserializer entityDeserializer,
			EntitySerializer entitySerializer) {
		super(archive, stateSystem, entityDeserializer, entitySerializer);
		try {
			dao = new HibernateXmlStoreDaoImpl();
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		System.out.println("************************************************************************************");
		System.out.println("Using hibernate to access to xmlstore, some people just want to watch the world burn");
		System.out.println("************************************************************************************");
		
	}

	@Override
	public ObsProposal getObsProposal(String id) throws EntityException,
			UserException {
		ObsProposal prop = dao.genericRetrieval(id, ObsProposalEntity.class, ObsProposal.class);
		obsProposals.put(id, prop);
		return prop;
	}

	@Override
	public ObsReview getObsReview(String id) throws EntityException,
			UserException {
		ObsReview or = dao.genericRetrieval(id, ObsReviewEntity.class, ObsReview.class);
		obsReviews.put(id, or);
		return or;
	}

	@Override
	public ObsProject getObsProject(String id) throws EntityException,
			UserException {
		ObsProject op = dao.genericRetrieval(id, ObsProjectEntity.class, ObsProject.class);
		obsProjects.put(id, op);
		return op;
	}

	@Override
	public SchedBlock getSchedBlock(String id) throws EntityException,
			UserException {
		SchedBlock sb = dao.genericRetrieval(id, SchedBlockEntity.class, SchedBlock.class);
		schedBlocks.put(id, sb);
		return sb;
	}

	@Override
	public void tidyUp() {
		dao.cleanUp();
	}
}
