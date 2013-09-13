package alma.scheduling.algorithm.weather;

import java.util.Date;

import alma.scheduling.algorithm.BaseAlgorithmTestCase;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherDependentVariables;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.DSAContextFactory;

public class TestWeatherSelectors extends BaseAlgorithmTestCase {

	public void testWeatherSelector() throws Exception{
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"<WeatherSelector><tsysVariation>0.2</tsysVariation></WeatherSelector></SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		
		WeatherDependentVariables w =  new WeatherDependentVariables();
		w.setProjectedTsys(10.0D);
		w.setTsys(12.0D);
		tmp.setWeatherDependentVariables(w);
		
		sbDao.saveOrUpdate(tmp);
		
		WeatherTsysSelector weatherSelector = new WeatherTsysSelector("weatherSelector");
		assertEquals(true, weatherSelector.canBeSelected(tmp, new Date()));
		
		alg.setArray(new ArrayConfiguration());
		alg.selectCandidateSB();
		alg.rankSchedBlocks();
		SchedBlock sb = alg.getSelectedSchedBlock();
		
		assertEquals("uid://A000/X0/X0", sb.getUid());
	}
	
	public void testOpacitySelector() throws Exception{
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"<OpacitySelector/></SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		
		WeatherDependentVariables w =  new WeatherDependentVariables();
		w.setZenithOpacity(0.06D);
		tmp.setWeatherDependentVariables(w);
		
		sbDao.saveOrUpdate(tmp);
		
		OpacitySelector opacitySelector = new OpacitySelector("opacitySelector");
		assertEquals(true, opacitySelector.canBeSelected(tmp, new Date()));
		
		tmp = createBasicSB();
		tmp.getSchedulingConstraints().setRepresentativeFrequency(380.0D);
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		
		w =  new WeatherDependentVariables();
		w.setZenithOpacity(0.03D);
		tmp.setWeatherDependentVariables(w);
		assertEquals(true, opacitySelector.canBeSelected(tmp, new Date()));
		
		tmp = createBasicSB();
		tmp.getSchedulingConstraints().setRepresentativeFrequency(100.0D);
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		
		w =  new WeatherDependentVariables();
		w.setZenithOpacity(0.5D);
		tmp.setWeatherDependentVariables(w);
		assertEquals(true, opacitySelector.canBeSelected(tmp, new Date()));
		
		alg.setArray(new ArrayConfiguration());
		alg.selectCandidateSB();
		alg.rankSchedBlocks();
		SchedBlock sb = alg.getSelectedSchedBlock();
		
		assertEquals("uid://A000/X0/X0", sb.getUid());
	}
}
