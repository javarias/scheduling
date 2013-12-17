package alma.archive.xml.dao;

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.omg.CORBA.UserException;

import alma.SchedulingMasterExceptions.wrappers.AcsJSchedulingInternalExceptionEx;
import alma.acs.entityutil.EntityException;
import alma.archive.database.helpers.ArchiveConfiguration;
import alma.archive.database.helpers.DBConfiguration;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.xml.ObsProjectEntity;
import alma.archive.xml.ObsProposalEntity;
import alma.archive.xml.SchedBlockEntity;
import alma.archive.xml.XmlEntity;

/**
 * Implements some methods to read from xml store using hibernate and oracle DB according ALMA's setup.
 * 
 * @author javarias
 * @since ALMA-9.1.4
 */
public class HibernateXmlStoreDaoImpl implements XmlStoreReaderDao {
	
	private SessionFactory sf = null;
	
	private static final String APDM_XSD_NAMESPACES = "xmlns:ent=\"Alma/CommonEntity\" "
			+ "xmlns:val=\"Alma/ValueTypes\" " 
			+ "xmlns:prp=\"Alma/ObsPrep/ObsProposal\" "
			+ "xmlns:orv=\"Alma/ObsPrep/ObsReview\" "
			+ "xmlns:ps=\"Alma/ObsPrep/ProjectStatus\" "
			+ "xmlns:oat=\"Alma/ObsPrep/ObsAttachment\" "
			+ "xmlns:prj=\"Alma/ObsPrep/ObsProject\" "
			+ "xmlns:sbl=\"Alma/ObsPrep/SchedBlock\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	
	
	public HibernateXmlStoreDaoImpl() throws DatabaseException {
		if (sf == null)
			initializeDBSessionFactory();
	}

	public ObsProposalEntity getObsproposal(String id) {
		Vector<String> uid = new Vector<String>();
		uid.add(id);
		List<ObsProposalEntity> retList = getObsProposals(uid);
		if (retList.size() > 0)
			return retList.get(0);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see alma.archive.xml.dao.XmlStoreDao#getObsProposals(java.lang.String)
	 */
	@Override
	public List<ObsProposalEntity> getObsProposals(String XPathQuery) {
		openSession();
		Transaction t = sf.getCurrentSession().beginTransaction();
		SQLQuery q = sf.getCurrentSession().createSQLQuery("SELECT * from xml_obsproposal_entities where " +
		"existsnode(XML,'"+ XPathQuery + "', '"+ APDM_XSD_NAMESPACES +"') = 1");
		q.addEntity(ObsProposalEntity.class);
		@SuppressWarnings("unchecked")
		List<ObsProposalEntity> retVal = q.list();
		t.commit();
		closeSession();
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see alma.archive.xml.dao.XmlStoreDao#getObsProjects(java.lang.String)
	 */
	@Override
	public List<ObsProjectEntity> getObsProjects(String XPathQuery) {
		openSession();
		Transaction t = sf.getCurrentSession().beginTransaction();
		SQLQuery q = sf.getCurrentSession().createSQLQuery("SELECT * from xml_obsproject_entities where " +
		"existsnode(XML,'"+ XPathQuery + "', '"+ APDM_XSD_NAMESPACES +"') = 1");
		q.addEntity(ObsProposalEntity.class);
		@SuppressWarnings("unchecked")
		List<ObsProjectEntity> retVal = q.list();
		t.commit();
		closeSession();
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see alma.archive.xml.dao.XmlStoreDao#getSchedBlocks(java.lang.String)
	 */
	@Override
	public List<SchedBlockEntity> getSchedBlocks(String XPathQuery) {
		openSession();
		Transaction t = sf.getCurrentSession().beginTransaction();
		SQLQuery q = sf.getCurrentSession().createSQLQuery("SELECT * from xml_schedblock_entities where " +
		"existsnode(XML,'"+ XPathQuery + "', '"+ APDM_XSD_NAMESPACES +"') = 1");
		q.addEntity(ObsProposalEntity.class);
		@SuppressWarnings("unchecked")
		List<SchedBlockEntity> retVal = q.list();
		t.commit();
		closeSession();
		return retVal;
	}
	
	/**
	 * 
	 * @see XmlStoreDaoImpl#closeActiveTransaction()
	 * 
	 * @param XPathQuery A valid XPath query 
	 * @return ScrollResult with the results of the query
	 */
	public Iterable<ObsProposalEntity> getObsProposalsIterator(String XPathQuery) {
		openSession();
		SQLQuery q = sf.getCurrentSession().createSQLQuery("SELECT * from xml_obsproposal_entities where " +
		"existsnode(XML,'"+ XPathQuery + "', '"+ APDM_XSD_NAMESPACES +"') = 1");
		q.addEntity(ObsProposalEntity.class);
		return new QueryIterable<ObsProposalEntity>(q);
	}
	
	/**
	 * The iterator
	 * must be manually closed by the user.
	 * 
	 * 
	 * @param XPathQuery A valid XPath query 
	 * @return ScrollResult with the results of the query
	 */
	public Iterable<ObsProjectEntity> getObsProjectsIterator(String XPathQuery) {
		openSession();
		SQLQuery q = sf.getCurrentSession().createSQLQuery("SELECT * from xml_obsproject_entities where " +
		"existsnode(XML,'"+ XPathQuery + "', '"+ APDM_XSD_NAMESPACES +"') = 1");
		q.addEntity(ObsProjectEntity.class);
		return new QueryIterable<ObsProjectEntity>(q);
	}
	
	/**
	 * The iterator must be manually closed by the user.
	 * 
	 * 
	 * @param XPathQuery A valid XPath query 
	 * @return ScrollResult with the results of the query
	 */
	public Iterable<SchedBlockEntity> getSchedBlocksIterator(String XPathQuery) {
		openSession();
		SQLQuery q = sf.getCurrentSession().createSQLQuery("SELECT * from xml_schedblock_entities where " +
	    "existsnode(XML,'"+ XPathQuery + "', '"+ APDM_XSD_NAMESPACES +"') = 1");
		q.addEntity(SchedBlockEntity.class);
		return new QueryIterable<SchedBlockEntity>(q);
	}
	
	/* (non-Javadoc)
	 * @see alma.archive.xml.dao.XmlStoreDao#getObsProposals(java.util.Collection)
	 */
	@Override
	public List<ObsProposalEntity> getObsProposals(Collection<String> uids) {
		openSession();
		Criteria c = sf.getCurrentSession().createCriteria(ObsProposalEntity.class);
		if (!(uids == null || uids.size() == 0)) {
			c.add(Restrictions.in("uid", uids));
		}
		@SuppressWarnings("unchecked")
		List<ObsProposalEntity> retVal =  c.list();
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see alma.archive.xml.dao.XmlStoreDao#getObsProjects(java.util.Collection)
	 */
	@Override
	public List<ObsProjectEntity> getObsProjects(Collection<String> uids) {
		openSession();
		Transaction t = null;
		Criteria c = sf.getCurrentSession().createCriteria(ObsProjectEntity.class);
		if (!(uids == null || uids.size() == 0)) {
			c.add(Restrictions.in("uid", uids));
		}
		t = sf.getCurrentSession().beginTransaction();
		@SuppressWarnings("unchecked")
		List<ObsProjectEntity> retVal =  c.list();
		t.commit();
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see alma.archive.xml.dao.XmlStoreDao#getSchedBlocks(java.util.Collection)
	 */
	@Override
	public List<SchedBlockEntity> getSchedBlocks(Collection<String> uids) {
		openSession();
		Transaction t = null;
		Criteria c = sf.getCurrentSession().createCriteria(SchedBlockEntity.class);
		if (!(uids == null || uids.size() == 0)) {
			c.add(Restrictions.in("uid", uids));
		}
		t = sf.getCurrentSession().beginTransaction();
		@SuppressWarnings("unchecked")
		List<SchedBlockEntity> retVal =  c.list();
		t.commit();
		return retVal;
	}
	
	/**
	 * Returns the Entities according to the given uids. 
	 * 
	 * @param uids the uids to look into the table. if it is null, this method will retrieve all elements in the table
	 * @return 
	 */
	public Iterable<ObsProposalEntity> getObsProposalsIterator(Collection<String> uids) {
		openSession();
		Criteria c = sf.getCurrentSession().createCriteria(ObsProposalEntity.class);
		if (!(uids == null || uids.size() == 0)) {
			c.add(Restrictions.in("uid", uids));
		}
		return new CriteriaIterable<ObsProposalEntity>(c);
	}
	
	/**
	 * Returns the Entities according to the given uids. 
	 * 
	 * @param uids the uids to look into the table. if it is null, this method will retrieve all elements in the table
	 * @return 
	 */
	public Iterable<ObsProjectEntity> getObsProjectsIterator(Collection<String> uids) {
		openSession();
		Criteria c = sf.getCurrentSession().createCriteria(ObsProjectEntity.class);
		if (!(uids == null || uids.size() == 0)) {
			c.add(Restrictions.in("uid", uids));
		}
		return new CriteriaIterable<ObsProjectEntity>(c);
	}
	
	/**
	 * Returns the Entities according to the given uids. 
	 * 
	 * @param uids the uids to look into the table. if it is null, this method will retrieve all elements in the table
	 * @return 
	 */
	public Iterable<SchedBlockEntity> getSchedBlocksIterator(Collection<String> uids) {
		openSession();
		Criteria c = sf.getCurrentSession().createCriteria(SchedBlockEntity.class);
		if (!(uids == null || uids.size() == 0)) {
			c.add(Restrictions.in("uid", uids));
		}
		return new CriteriaIterable<SchedBlockEntity>(c);
	}
	
	private void initializeDBSessionFactory() throws DatabaseException {
		String archive_db_connection = null;
		String archive_oracle_user = null;
		String archive_oracle_passwd = null;
		
		archive_db_connection = System.getProperty("archive.db.connection");
		archive_oracle_user = System.getProperty("archive.oracle.user");
		archive_oracle_passwd = System.getProperty("archive.oracle.passwd");
		if (archive_db_connection == null 
				|| archive_oracle_user == null
				|| archive_oracle_passwd == null) {
			DBConfiguration archiveConf = null;
			archiveConf = ArchiveConfiguration.instance(Logger
					.getAnonymousLogger());
			if (!archiveConf.get("archive.db.mode").equals("operational"))
				throw new RuntimeException(
						"Archive config is using eXist for the xmlstore. Use the \"operational\" configuration in archiveConfig.properties instead");
			archive_db_connection = archiveConf.get("archive.db.connection");
			archive_oracle_user = archiveConf.get("archive.oracle.user");
			archive_oracle_passwd = archiveConf.get("archive.oracle.passwd");
		}
		System.out.println(archive_db_connection + " " + archive_oracle_user
				+ " " + archive_oracle_passwd);
		Configuration hibConf = new Configuration().configure("alma/archive/hibernate.config.xml");
		hibConf.setProperty("hibernate.connection.url", archive_db_connection);
		hibConf.setProperty("hibernate.connection.username", archive_oracle_user);
		hibConf.setProperty("hibernate.connection.password", archive_oracle_passwd);
		
		sf = hibConf.buildSessionFactory();
	}
	
	private void openSession() {
		if (sf.getCurrentSession() != null && sf.getCurrentSession().isOpen())
			sf.openSession();
	}
	
	public void closeSession() {
		if (sf.getCurrentSession() != null && sf.getCurrentSession().isOpen())
			sf.close();
	}

	@Override
	public void cleanUp() {
		closeSession();
	}
	
	@SuppressWarnings("unchecked")
	public <CT> CT genericRetrieval(String id,
			Class<? extends XmlEntity> hClass, Class<CT> cClass)
			throws EntityException, UserException {
		CT retVal = null;
		Transaction  tx = null;
		try {
			openSession();
			tx = sf.getCurrentSession().beginTransaction();
			XmlEntity hRet = (XmlEntity) sf.getCurrentSession().get(hClass, id);
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
		} finally {
			if (tx != null)
				tx.commit();
			closeSession();
		}
		return retVal;
	}
}
