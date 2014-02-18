package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

import alma.archive.xml.dao.HibernateXmlStoreDaoImpl;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.AbstractXMLStoreProjectDao.XMLStoreImportNotifier;

public class Phase1XMLStoreProjectDaoTest extends MockObjectTestCase {
	{
		setImposteriser(ClassImposteriser.INSTANCE);
	}
	
	private Phase1XMLStoreProjectDao prjDao = null;
	private ArchiveInterface archive = mock(ArchiveInterface.class);
	private XMLStoreImportNotifier notifier = mock(XMLStoreImportNotifier.class);
	private HibernateXmlStoreDaoImpl hibDao = mock(HibernateXmlStoreDaoImpl.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		checking(new Expectations() { {
			allowing(notifier);
		}});
		prjDao = new Phase1XMLStoreProjectDao(archive, notifier, hibDao);
	}

	public void testCycle2Import() throws Exception {
		checking(new Expectations() {{
			oneOf(hibDao).getObsProposalsIterator(with(equal("/prp:ObsProposal[prp:cycle=\"2012.1\"]")));
			allowing(hibDao).closeSession();
		}});
		List<ObsProject> projects = prjDao.getAllObsProjects();
	}
}
