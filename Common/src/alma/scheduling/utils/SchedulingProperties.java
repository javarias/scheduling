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
 * Centralised place for querying the scheduling properties.
 * 
 * @author dclarke
 * $Id: SchedulingProperties.java,v 1.8 2012/06/15 19:49:28 dclarke Exp $
 */
package alma.scheduling.utils;

public abstract class SchedulingProperties {

	/*
	 * ================================================================
	 * The properties we know about
	 * ================================================================
	 */

	// Properties which we call blah-blah-Flag are booleans. If they
	// are set, they are interpreted as being "true", regardless of
	// the value to which they are set.
	// Properties which we call blah-blah-Value have their values
	// parsed.

	/** Do we automatically pop up the Scheduling panel when we start up? (test only) */
	private static String PROP_autoPopupPluginFlag      = "scheduling.autoPopupPlugin";
	
	/** Do we automatically convert Phase2Submitted projects to Ready? (test only) */
	private static String PROP_convertPhase2ToReadyFlag = "scheduling.convertPhase2ToReady";
	
	/** Is this David running tests? */
	private static String PROP_davidTestingFlag      = "scheduling.dclarke";
	
	/** Is this Jorge running tests? */
	private static String PROP_jorgeTestingFlag      = "scheduling.javarias";
	
	/** Where should we look for the Phase1 SBs? */
	private static String PROP_phase1SBSourceValue   = "scheduling.phase1SBsource";
	
	/** This should enable the import of XML projects. */
	public static String PROP_PMS_XML_PROJECT_IMPORT_FLAG = "scheduling.pms.xmlProjects";
	
	/* End of the properties we know about
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Boolean properties
	 * ================================================================
	 */
	public static boolean isAutoPopupArrayPlugin() {
		final String env = System.getProperty(PROP_autoPopupPluginFlag);
		return env != null;
	}
	
	public static boolean isConvertPhase2ToReady() {
		final String env = System.getProperty(PROP_convertPhase2ToReadyFlag);
		return env != null;
	}
	
	public static boolean isDavidTesting() {
		final String env = System.getProperty(PROP_davidTestingFlag);
		return env != null;
	}
	
	public static boolean isJorgeTesting() {
		final String env = System.getProperty(PROP_jorgeTestingFlag);
		return env != null;
	}
	
	public static boolean isTesting() {
		return isDavidTesting() || isJorgeTesting();
	}
	
	public static boolean isPMSUsingXMLProjects() {
		final String env = System.getProperty(PROP_PMS_XML_PROJECT_IMPORT_FLAG);
		return env != null;
	}
	/* End of Boolean properties
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Exception class
	 * ================================================================
	 */
	@SuppressWarnings("serial")
	public static class InvalidPropertyValueException extends Exception {

		public InvalidPropertyValueException(String property,
				                             String value,
				                             String possibilities) {
			super(String.format("Invalid value for property %s. Value supplied is %s, valid values are %s",
					property, value, possibilities));
		}

		public InvalidPropertyValueException(String property,
				                             String value,
				                             String possibilities,
				                             Throwable t) {
			super(String.format("Invalid value for property %s. Value supplied is %s, valid values are %s",
					property, value, possibilities), t);
		}
	}
	/* End of Exception class
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Phase 1 SB Source property
	 * ================================================================
	 */
	/**
	 * An enumeration of the possible values for this property.
	 */
	public enum Phase1SBSourceValue {
		REVIEW_ONLY("ObsReview"),
		PROPOSAL_ONLY("ObsProposal"),
		REVIEW_THEN_PROPOSAL("ObsReview_Then_ObsProposal"),
		PROPOSAL_THEN_REVIEW("ObsProposal_Then_ObsReview");
		
		private String value;
		
		private Phase1SBSourceValue(String value) {
			this.value = value;
		}
		
		public String toString() {
			return value;
		}

		public static Phase1SBSourceValue getDefault() {
			return PROPOSAL_ONLY;
		}
		
		public static String possibilities() {
			StringBuffer sb  = new StringBuffer();
			String       sep = "";
			
			for (Phase1SBSourceValue val : values()) {
				sb.append(sep);
				sb.append(val.toString());
				sep = ", ";
			}
			
			return sb.toString();
		}
	}
		
	/**
	 * Get the value of the Phase 1 SB Source flag. If an invalid value
	 * has been specified, then return null. <em>Should <b>not</b> be
	 * used unless you know that the checked version has already been
	 * called and possible errors from that have been handled and
	 * reported</em> - which really means that any code which calls
	 * this should not be invoked unless it has already got through the
	 * checked version without that exception having been thrown.
	 * 
	 * @return The value of the property (which might be the default
	 *         value if the user has not specified anything.
	 * @throws InvalidPropertyValueException
	 */
	public static Phase1SBSourceValue getUncheckedPhase1SBSource() {
		final String env = System.getProperty(PROP_phase1SBSourceValue);
		if (env == null) {
			return Phase1SBSourceValue.getDefault();
		}
		for (Phase1SBSourceValue val : Phase1SBSourceValue.values()) {
			if (env.equalsIgnoreCase(val.toString())) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * Get the value of the Phase 1 SB Source flag. If an invalid value
	 * has been specified, then throw an exception.
	 * 
	 * @return The value of the property (which might be the default
	 *         value if the user has not specified anything.
	 * @throws InvalidPropertyValueException
	 */
	public static Phase1SBSourceValue getPhase1SBSource() throws InvalidPropertyValueException {
		final String env = System.getProperty(PROP_phase1SBSourceValue);
		if (env == null) {
			return Phase1SBSourceValue.getDefault();
		}
		for (Phase1SBSourceValue val : Phase1SBSourceValue.values()) {
			if (env.equalsIgnoreCase(val.toString())) {
				return val;
			}
		}
		throw new InvalidPropertyValueException(PROP_phase1SBSourceValue, env, Phase1SBSourceValue.possibilities());
	}
	
	/* End of Phase 1 SB Source property
	 * ============================================================= */
}
