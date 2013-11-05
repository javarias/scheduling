/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.utils;

import org.springframework.context.ApplicationContext;

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
	
	private static ApplicationContext context = null;
	
	/** URI for Scheduling/Common Spring context configuration*/
	protected static final String SCHEDULING_COMMON_SPRING_CONFIG = "classpath:alma/scheduling/CommonContext.xml";
	public static final String SCHEDULING_OBSPROJECT_DAO_BEAN = "obsProjectDao";
	public static final String SCHEDULING_SCHEDBLOCK_DAO_BEAN = "sbDao";
	public static final String SCHEDULING_CONFIGURATION_DAO_BEAN = "configDao";
	public static final String SCHEDULING_EXECUTIVE_DAO_BEAN = "execDao";
	public static final String SCHEDULING_ATM_DAO_BEAN = "atmDao";
	public static final String SCHEDULING_WEATHER_DAO_BEAN = "weatherStationDao";
	public static final String SCHEDULING_OPACITY_INTERPOLATOR_BEAN = "opacityInterpolator";
	public static final String SCHEDULING_XML_OBSERVATORY_DAO = "xmlObservatoryDao";
	public static final String SCHEDULING_OBSERVATORY_DAO = "observatoryDao";
	public static final String SCHEDULING_OBSERVATION_DAO = "observationDao";
	
	/**
	 * Create the instance of the context just for the Common subset of Scheduling
	 * define in {@code alma.scheduling.CommonContext.xml} </br>
	 * The user can create new ContextFactory using the class {@code SchedulingContextFactory}
	 * 
	 * @return the instance of the context created
	 */
	public static synchronized ApplicationContext getContext(){
		System.out.println(CommonContextFactory.class);
		if(context == null){
			context = SchedulingContextFactory.getContext(SCHEDULING_COMMON_SPRING_CONFIG);
		}
		return context;
	}
}
