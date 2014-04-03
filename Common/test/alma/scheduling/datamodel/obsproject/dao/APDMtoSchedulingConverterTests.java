package alma.scheduling.datamodel.obsproject.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;
import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.bookkeeping.Bookkeeper;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.APDMtoSchedulingConverter;
import alma.scheduling.datamodel.obsproject.dao.APDMtoSchedulingConverter.Phase;
import alma.scheduling.datamodel.obsproject.dao.AbstractXMLStoreProjectDao.XMLStoreImportNotifier;
import alma.scheduling.utils.DSAContextFactory;

public class APDMtoSchedulingConverterTests extends MockObjectTestCase {
	{
		setImposteriser(ClassImposteriser.INSTANCE);
	}
	private FileArchiveInterface archive;
	private ObsProjectDao prjDao;
	private SchedBlockDao sbDao;
	private XMLStoreImportNotifier notifier = mock(XMLStoreImportNotifier.class);
	private Bookkeeper bookie = mock(Bookkeeper.class);
	
	public APDMtoSchedulingConverterTests() {
		if (System.getProperty("alma.scheduling.properties") == null) {
			System.setProperty("alma.scheduling.properties", "Common/src/scheduling.properties");
		}
		ApplicationContext ctx = DSAContextFactory.getContext();
		prjDao = (ObsProjectDao) ctx.getBean("obsProjectDao");
		sbDao = (SchedBlockDao) ctx.getBean("sbDao");
	}
	
	@Override
	protected void setUp() throws Exception {
		checking(new Expectations() {{
			allowing(notifier);
		}});
		super.setUp();
		archive = new FileArchiveInterface("./Common/test/projects");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testPhase1Conversion() throws Exception {
		APDMtoSchedulingConverter converter = new APDMtoSchedulingConverter(archive, Phase.PHASE1, Logger.getAnonymousLogger(), notifier, bookie);
		List<ObsProject> prjs = converter.convertAPDMProjectsToDataModel();
		int i = 0;
		for (ObsProject p: prjs) 
			for (SchedBlock sb: getAllSchedBlocks(p.getObsUnit())) {
				if (sb.getTemporalConstraints() != null && sb.getTemporalConstraints().size() > 0)
					i++;
			}
		System.out.println("Number of SBs with temporal Constraint: " + i);
	}
	
	public void testTemporalConstraintsConversion() throws Exception {
		APDMtoSchedulingConverter converter = new APDMtoSchedulingConverter(archive, Phase.PHASE1, Logger.getAnonymousLogger(), notifier, bookie);
		List<ObsProject> prjs = converter.convertAPDMProjectsToDataModel("uid://A001/X10e/X78e");
		for (ObsProject p: prjs) 
			for (SchedBlock sb: getAllSchedBlocks(p.getObsUnit())) {
				if (sb.getTemporalConstraints() != null && sb.getTemporalConstraints().size() > 0)
					System.out.println(sb.getUid());
			}
	}
	
	public void testSBRepetitions() throws Exception {
		APDMtoSchedulingConverter converter = new APDMtoSchedulingConverter(archive, Phase.PHASE1, Logger.getAnonymousLogger(), notifier, bookie);
		List<ObsProject> prjs = converter.convertAPDMProjectsToDataModel("uid://A001/X113/X42a");
		for (ObsProject p: prjs) 
			for (SchedBlock sb: getAllSchedBlocks(p.getObsUnit())) {
				if (sb.getUid().equals("uid://A001/X12a/X1920")) {
					assertEquals(false, sb.getSchedBlockControl().getIndefiniteRepeat().booleanValue());
					assertEquals(63, sb.getSchedBlockControl().getExecutionCount().intValue());
					assertEquals(2.0, sb.getSchedBlockControl().getSbMaximumTime());
				}
				System.out.println(sb.getUid() + " ir:" + sb.getSchedBlockControl().getIndefiniteRepeat()
						+ " ec:" + sb.getSchedBlockControl().getExecutionCount() +
						" time:" + sb.getSchedBlockControl().getSbMaximumTime());
			}
		prjDao.saveOrUpdate(prjs);
		SchedBlock sb = sbDao.findByEntityId("uid://A001/X12a/X1920");
		assertEquals(false, sb.getSchedBlockControl().getIndefiniteRepeat().booleanValue());
		assertEquals(63, sb.getSchedBlockControl().getExecutionCount().intValue());
		assertEquals(2.0, sb.getSchedBlockControl().getSbMaximumTime());
		
	}
	
	
	private Set<SchedBlock> getAllSchedBlocks(ObsUnit ou) {
		HashSet<SchedBlock> ret = new HashSet<SchedBlock>();
		if(ou instanceof ObsUnitSet) {
			ObsUnitSet ous = (ObsUnitSet) ou;
			for (ObsUnit ouc: ous.getObsUnits())
				ret.addAll(getAllSchedBlocks(ouc));
		} else if(ou instanceof SchedBlock)
			ret.add((SchedBlock) ou);
		else 
			throw new RuntimeException(ou.getClass() + " should not be part of ObsUnits");
		return ret;
	}
}
