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

import java.io.StringReader;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import org.omg.CORBA.UserException;

import alma.SchedulingMasterExceptions.wrappers.AcsJSchedulingInternalExceptionEx;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;
import alma.acs.entityutil.EntitySerializer;
import alma.archive.database.helpers.ArchiveConfiguration;
import alma.archive.database.helpers.DBConfiguration;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.xml.ObsProjectEntity;
import alma.archive.xml.ObsProposalEntity;
import alma.archive.xml.ObsReviewEntity;
import alma.archive.xml.SchedBlockEntity;
import alma.archive.xml.XmlEntity;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsreview.ObsReview;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.lifecycle.persistence.StateArchive;
import alma.xmlstore.OperationalOperations;

public final class HibernateArchiveInterface extends AbstractArchiveInterface
		implements ArchiveInterface {

	private static SessionFactory sf = null;
	private StatelessSession session = null;

	public HibernateArchiveInterface(OperationalOperations archive,
			StateArchive stateSystem,
			EntityDeserializer entityDeserializer,
			EntitySerializer entitySerializer) {
		super(archive, stateSystem, entityDeserializer, entitySerializer);
		//The session and the transaction are closed at the end of the thread execution. this is handled by Hibernate
		System.out.println("************************************************************************************");
		System.out.println("Using hibernate to access to xmlstore, some people just want to watch the world burn");
		System.out.println("************************************************************************************");
		if (session == null)
			initializeDBSession();
	}

	@Override
	public ObsProposal getObsProposal(String id) throws EntityException,
			UserException {
		ObsProposal prop = genericRetrieval(id, ObsProposalEntity.class, ObsProposal.class);
		obsProposals.put(id, prop);
		return prop;
	}

	@Override
	public ObsReview getObsReview(String id) throws EntityException,
			UserException {
		ObsReview or = genericRetrieval(id, ObsReviewEntity.class, ObsReview.class);
		obsReviews.put(id, or);
		return or;
	}

	@Override
	public ObsProject getObsProject(String id) throws EntityException,
			UserException {
		ObsProject op = genericRetrieval(id, ObsProjectEntity.class, ObsProject.class);
		obsProjects.put(id, op);
		return op;
	}

	@Override
	public SchedBlock getSchedBlock(String id) throws EntityException,
			UserException {
		SchedBlock sb = genericRetrieval(id, SchedBlockEntity.class, SchedBlock.class);
		schedBlocks.put(id, sb);
		return sb;
	}
	
	@SuppressWarnings("unchecked")
	private <CT> CT genericRetrieval(String id,
			Class<? extends XmlEntity> hClass, Class<CT> cClass)
			throws EntityException, UserException {
		CT retVal = null;
		try {
			XmlEntity hRet = (XmlEntity) session.get(hClass, id);
			retVal = (CT) Unmarshaller.unmarshal(cClass, new StringReader(hRet.domToString()));
		} catch (HibernateException e) {
			AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(
					e);
			throw ex.toSchedulingInternalExceptionEx();
		} catch (MarshalException e) {
			throw new EntityException(e);
		} catch (ValidationException e) {
			throw new EntityException(e);
		} catch (TransformerException e) {
			throw new EntityException(e);
		}
		return retVal;
	}

	private void initializeDBSession() {
		DBConfiguration archiveConf = null;
		try {
			archiveConf = ArchiveConfiguration.instance(Logger
					.getAnonymousLogger());
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		if (!archiveConf.get("archive.db.mode").equals("operational"))
			throw new RuntimeException(
					"Archive config is using eXist for the xmlstore. Use instead an operation configuration");
		String archive_db_connection = archiveConf.get("archive.db.connection");
		String archive_oracle_user = archiveConf.get("archive.oracle.user");
		String archive_oracle_passwd = archiveConf.get("archive.oracle.passwd");
		System.out.println(archive_db_connection + " " + archive_oracle_user
				+ " " + archive_oracle_passwd);
		Configuration hibConf = new Configuration().configure("alma/archive/hibernate.config.xml");
		hibConf.setProperty("hibernate.connection.url", archive_db_connection);
		hibConf.setProperty("hibernate.connection.username", archive_oracle_user);
		hibConf.setProperty("hibernate.connection.password", archive_oracle_passwd);
//		hibConf.setProperty("connection.driver_class",
//				"oracle.jdbc.driver.OracleDriver");
//		hibConf.setProperty("cache.provider_class",
//				"org.hibernate.cache.NoCacheProvider");
//		hibConf.setProperty("dialect",
//				"org.hibernate.dialect.ExtendedOracle10gDialect");
//		hibConf.setProperty("show_sql", "false");
//		hibConf.setProperty("hbm2ddl.auto", "validate");
//		hibConf.addResource("alma/archive/xmlstore.hbm.xml");
		sf = hibConf.buildSessionFactory();
		session = sf.openStatelessSession();
		session.beginTransaction();
	}
}
