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
/**
 * 
 */
package alma.scheduling.utils;

/**
 * @author dclarke
 *
 */
public abstract class SchedulingProperties {

	private static String PROP_autoPopupPlugin      = "scheduling.autoPopupPlugin";
	private static String PROP_convertPhase2ToReady = "scheduling.convertPhase2ToReady";
	private static String PROP_davidTesting      = "scheduling.dclarke";
	private static String PROP_jorgeTesting      = "scheduling.javarias";
	
	/**
	 * This should enable the import of XML projects.
	 */
	public static String PROP_PMS_XML_PROJECT_IMPORT = "scheduling.pms.xmlProjects";
	
	public static String autoPopupPluginPropertyName() {
		return PROP_autoPopupPlugin;
	}
	
	public static boolean isAutoPopupArrayPlugin() {
		final String env = System.getProperty(PROP_autoPopupPlugin);
		return env != null;
	}
	
	public static String convertPhase2ToReadyPropertyName() {
		return PROP_convertPhase2ToReady;
	}
	
	public static boolean isConvertPhase2ToReady() {
		final String env = System.getProperty(PROP_convertPhase2ToReady);
		return env != null;
	}
	
	public static boolean isDavidTesting() {
		final String env = System.getProperty(PROP_davidTesting);
		return env != null;
	}
	
	public static boolean isJorgeTesting() {
		final String env = System.getProperty(PROP_jorgeTesting);
		return env != null;
	}
	
	public static boolean isTesting() {
		return isDavidTesting() || isJorgeTesting();
	}
	
	public static boolean isPMSUsingXMLProjects() {
		final String env = System.getProperty(PROP_PMS_XML_PROJECT_IMPORT);
		return env != null;
	}
}
