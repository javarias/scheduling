/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.scheduling.array.util;

import alma.scheduling.Define.SchedulingException;

/**
 * Utilities for handling the translation of names from one arena to
 * another, e.g. component names to and from array names.
 * 
 * @author dclarke
 * $Id: NameTranslator.java,v 1.1 2010/07/26 16:36:19 dclarke Exp $
 */
public abstract class NameTranslator {
	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
	/**
	 * Exception class for translation errors
	 */
	@SuppressWarnings("serial")
	public static class TranslationException extends SchedulingException {
		public TranslationException() { super(); }
		public TranslationException(String s) { super(s); }
		public TranslationException(String s, Throwable t) { super(s, t); }
		public TranslationException(Throwable t) { super(t); }
	}

	private static String stripPrefix(String prefix, String from)
										  throws TranslationException {
		String result;
		if (from.startsWith(prefix)) {
			result = from.substring(prefix.length());
		} else {
			throw new TranslationException(
					String.format("%s doesn't have expected prefix %s",
							from, prefix));
		}
		return result;
	}
	
	private static String addPrefix(String prefix, String to)
	  									  throws TranslationException {
		String result;
		if (to.startsWith(prefix)) {
			throw new TranslationException(
					String.format("%s already has prefix %s",
							to, prefix));
		} else {
			result = prefix + to;
		}
		return result;
	}
	/* End Utilities
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Array names to/from Component names
	 * ================================================================
	 */
	public final static String COMPONENT_PREFIX = "Scheduling/";
	public final static String CONTROL_COMPONENT_PREFIX = "CONTROL/";
	
	/**
	 * Translate the given array name into the corresponding Scheduling
	 * component name.
	 * 
	 * @param arrayName
	 * @return String
	 * @throws TranslationException - if the supplied name already
	 *         suspiciously like a component name.
	 */
	public static String arrayToComponentName(String arrayName)
										  throws TranslationException {
		String result;
		try {
			result = stripPrefix(COMPONENT_PREFIX, arrayName);
		} catch (TranslationException e) {
			throw new TranslationException(String.format(
					"Error translating array name %s to Scheduling component name - %s",
					arrayName, e.getMessage()));
		}
		return result;
	}
	
	
	/**
	 * Translate the given Scheduling component name into the
	 * corresponding array name.
	 * 
	 * @param componentName
	 * @return String
	 * @throws TranslationException - if the supplied name already
	 *         suspiciously like an array name.
	 */
	public static String componentToArray(String componentName)
										  throws TranslationException {
		String result;
		try {
			result = addPrefix(COMPONENT_PREFIX, componentName);
		} catch (TranslationException e) {
			throw new TranslationException(String.format(
					"Error translating Scheduling component name %s to array name - %s",
					componentName, e.getMessage()));
		}
		return result;
	}
	
	/**
	 * Translate the given array name into the corresponding Control
	 * component name.
	 * 
	 * @param arrayName
	 * @return String
	 * @throws TranslationException - if the supplied name already
	 *         suspiciously like a component name.
	 */
	public static String arrayToControlComponentName(String arrayName)
										  throws TranslationException {
		String result;
		try {
			result = stripPrefix(CONTROL_COMPONENT_PREFIX, arrayName);
		} catch (TranslationException e) {
			throw new TranslationException(String.format(
					"Error translating array name %s to Control component name - %s",
					arrayName, e.getMessage()));
		}
		return result;
	}
	
	
	/**
	 * Translate the given Control component name into the
	 * corresponding array name.
	 * 
	 * @param componentName
	 * @return String
	 * @throws TranslationException - if the supplied name already
	 *         suspiciously like an array name.
	 */
	public static String controlComponentToArray(String componentName)
										  throws TranslationException {
		String result;
		try {
			result = addPrefix(CONTROL_COMPONENT_PREFIX, componentName);
		} catch (TranslationException e) {
			throw new TranslationException(String.format(
					"Error translating Control component name %s to array name - %s",
					componentName, e.getMessage()));
		}
		return result;
	}
	/* End Array names to/from Component names
	 * ============================================================= */
}
