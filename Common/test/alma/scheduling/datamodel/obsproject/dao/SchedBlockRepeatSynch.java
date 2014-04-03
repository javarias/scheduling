package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import org.omg.CORBA.UserException;
import org.springframework.context.ApplicationContext;

import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.valuetypes.types.TimeTUnitType;
import alma.scheduling.datamodel.helpers.ConversionException;
import alma.scheduling.datamodel.helpers.TimeConverter;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.DSAContextFactory;

public class SchedBlockRepeatSynch {

	private FileArchiveInterface archive;
	private SchedBlockDao sbDao;
	
	public SchedBlockRepeatSynch() {
		archive = new FileArchiveInterface("./export");
		ApplicationContext ctx = DSAContextFactory.getContext();
		sbDao = (SchedBlockDao) ctx.getBean("sbDao");
	}
	
	public void synchSB(SchedBlock sb) {
		try {
			alma.entity.xmlbinding.schedblock.SchedBlock apdmSB = archive.getSchedBlock(sb.getUid());
			sb.getSchedBlockControl().setExecutionCount(apdmSB.getSchedBlockControl().getExecutionCount());
			sb.getSchedBlockControl().setSbMaximumTime(TimeConverter.convertedValue(
					apdmSB.getSchedBlockControl().getSBMaximumTime(),
					TimeTUnitType.H));
			sbDao.saveOrUpdate(sb);
			SchedBlock savedSB = sbDao.findByEntityId(sb.getUid());
			System.out.println("Comparison: ec: " + sb.getSchedBlockControl().getExecutionCount() + " - " + savedSB.getSchedBlockControl().getExecutionCount() + 
					" maxTime:" + sb.getSchedBlockControl().getSbMaximumTime() + " - "  + savedSB.getSchedBlockControl().getSbMaximumTime());
		} catch (EntityException e) {
			e.printStackTrace();
		} catch (UserException e) {
			e.printStackTrace();
		} catch (ConversionException e) {
			e.printStackTrace();
		}
	}
	
	public void synchSBs() {
		List<SchedBlock> sbList = sbDao.findAll();
		for(SchedBlock sb: sbList) {
			System.out.print("Reloading SB: " + sb.getUid() + " ...");
			System.out.println("Done.");
			synchSB(sb);
		}
	}
	
	public static void main(String[] args) {
		SchedBlockRepeatSynch synch = new SchedBlockRepeatSynch();
		synch.synchSBs();
	}
}
