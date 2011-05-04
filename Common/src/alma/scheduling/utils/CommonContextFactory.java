package alma.scheduling.utils;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * Handle Spring context singleton for the Scheduling Common Data Access Objects and
 * DAO's names. This class creates Spring context based on the <b>alma.scheduling.CommonContext.xml</b>
 * context file configuration.
 * 
 * @since ALMA 8.0.0
 * @author javarias
 *
 */
public class CommonContextFactory {
	
	private static AbstractApplicationContext context = null;
	
	/** URL for Scheduling/Common Spring context configuration*/
	protected static final String SCHEDULING_COMMON_SPRING_CONFIG = "classpath:alma/scheduling/CommonContext.xml";
	public static final String SCHEDULING_OBSPROJECT_DAO_BEAN = "obsProjectDao";
	public static final String SCHEDULING_SCHEDBLOCK_DAO_BEAN = "sbDao";
	public static final String SCHEDULING_CONFIGURATION_DAO_BEAN = "configDao";
	public static final String SCHEDULING_EXECUTIVE_DAO_BEAN = "execDao";
	public static final String SCHEDULING_ATM_DAO_BEAN = "atmDao";
	
	/**
	 * Create the instance of the context just for the Common subset of Scheduling
	 * define in {@code alma.scheduling.CommonContext.xml} </br>
	 * The user can create new ContextFactory using the class {@code SchedulingContextFactory}
	 * 
	 * @return the instance of the context created
	 */
	public static synchronized AbstractApplicationContext getContext(){
		System.out.println(CommonContextFactory.class);
		if(context == null){
			context = SchedulingContextFactory.getContext(SCHEDULING_COMMON_SPRING_CONFIG);
		}
		return context;
	}
}
