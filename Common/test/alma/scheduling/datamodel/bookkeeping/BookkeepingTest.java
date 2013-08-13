package alma.scheduling.datamodel.bookkeeping;

import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ArchiveInterface;

public class BookkeepingTest extends MockObjectTestCase {
	{
		setImposteriser(ClassImposteriser.INSTANCE);
	}
	
	private ArchiveInterface archive = mock(ArchiveInterface.class);
	
	public void testICT1048() throws Exception {
		checking(new Expectations() { {
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A001/Xa0/Xb4a"))); 
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A001_Xa0_Xb4a.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6a1"))); 
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a1.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6a2"))); 
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a2.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6a3"))); 
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a3.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5a9a13/X6a4"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a4.xml")))));
			atLeast(1).of(archive).getObsProject(with(equal("uid://A001/Xa0/Xb45"))); 
			will(returnValue(ObsProject.unmarshalObsProject(new FileReader(new File("test/apdm/uid___A001_Xa0_Xb45.xml")))));
			atLeast(1).of(archive).write(with(any(OUSStatus.class)));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6a5"))); 
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a5.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6a6")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a6.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6a7")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a7.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5a9a13/X6a8"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a8.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5a9a13/X69b"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X69b.xml")))));
			atLeast(1).of(archive).write(with(any(SBStatus.class)));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6a9")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6a9.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6aa")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6aa.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6ab")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6ab.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5a9a13/X6ac"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6ac.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5a9a13/X69c"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X69c.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6ad")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6ad.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5a9a13/X6ae"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6ae.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5a9a13/X69d"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X69d.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6af")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6af.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5a9a13/X6b0"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6b0.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5a9a13/X69e"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X69e.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5a9a13/X6b1")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6b1.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5a9a13/X6b2"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X6b2.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5a9a13/X69f"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5a9a13_X69f.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/X6")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X6.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/X7")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X7.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/X8")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X8.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5d5f8a/X9"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X9.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5d5f8a/X1"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X1.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/Xa")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_Xa.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/Xb")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_Xb.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/Xc")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_Xc.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5d5f8a/Xd"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_Xd.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5d5f8a/X2"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X2.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/Xe")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_Xe.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/Xf")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_Xf.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/X10")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X10.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5d5f8a/X11"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X11.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5d5f8a/X3"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X3.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/X12")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X12.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X5d5f8a/X13"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X13.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X5d5f8a/X4"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X4.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/X14")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X14.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X6ac013/X4"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X6ac013_X4.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X6ac013/X1"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X6ac013_X1.xml")))));
			atLeast(1).of(archive).getOUSStatus(with(equal("uid://A002/X5d5f8a/X15")));
			will(returnValue(OUSStatus.unmarshalOUSStatus(new FileReader(new File("test/apdm/uid___A002_X5d5f8a_X15.xml")))));
			atLeast(1).of(archive).getSBStatus(with(equal("uid://A002/X6ac013/X5"))); 
			will(returnValue(SBStatus.unmarshalSBStatus(new FileReader(new File("test/apdm/uid___A002_X6ac013_X5.xml")))));
			atLeast(1).of(archive).getSchedBlock(with(equal("uid://A002/X6ac013/X2"))); 
			will(returnValue(SchedBlock.unmarshalSchedBlock(new FileReader(new File("test/apdm/uid___A002_X6ac013_X2.xml")))));
			atLeast(1).of(archive).write(with(any(ProjectStatus.class)));
		}});
		Bookkeeper bk = new Bookkeeper(archive, Logger.getAnonymousLogger());
		ProjectStatus ps = ProjectStatus.unmarshalProjectStatus(new FileReader(new File("test/apdm/obsproject_status_ICT-1048.xml")));
		bk.initialise(ps);
	}
}
