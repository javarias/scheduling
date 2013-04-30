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

package alma.scheduling.formatting;

import alma.scheduling.ArrayDescriptor;

/**
 *
 * @author dclarke
 */
public class ArrayDescriptorFormatter extends EntityFormatter {
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
//	public ArrayDescriptorFormatter() {
//	}
	/* End Fields
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
	private void formatName(String arrayName) {
		h2(arrayName);
	}
	
	private void formatAntennas(String[] antennas, int perLine) {
		int onLine = 0;
		
		h3("Antennas");
		startTable();
		startTR();
		for (String antenna : antennas) {
			if (onLine == perLine) {
				endTR();
				startTR();
				onLine = 0;
			}
			td(antenna);
			onLine ++;
		}
		while (onLine < perLine) {
			td(" ");
			onLine ++;
		}
		endTR();
		endTable();
	}
	private void formatTheRest(ArrayDescriptor details) {
		startTable();
		
		startTR();
		th("Photonic References");
		if (details.photonicsList.length != 0) {
			td(Format.formatArray(details.photonicsList));
		} else {
			td("None");
		}
		endTR();

		startTR();
		th("Correlator Type");
		td(details.corrType.toString());
		endTR();

		startTR();
		th("Scheduling Mode");
		td(details.schedulingMode.toString());
		endTR();

		startTR();
		th("Scheduling Policy");
		if (details.policyName != null &&
				details.policyName.length() > 0) {
			final int hyphenPos = details.policyName.lastIndexOf('-');
			if (hyphenPos >= 0) {
				final String humanPart = details.policyName.substring(hyphenPos+1);
				td(humanPart + " (" + details.policyName + ")");
			} else {
				td(details.policyName);
			}
		} else {
			td("NONE");
		}
		endTR();

		endTable();
	}
	/* End Utilities
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Formatting - external interface
	 * ================================================================
	 */
	/**
	 * Format an Array
	 * 
	 * @param arrayName
	 * @param details
	 * @return An HTML string denoting the Array
	 */
	public static String formatted(String arrayName, ArrayDescriptor details) {
		final ArrayDescriptorFormatter f = new ArrayDescriptorFormatter();
		
		f.startHTML();
		f.formatName(arrayName);
		f.formatAntennas(details.antennaIdList, 6);
		f.br();
		f.br();
		f.formatTheRest(details);
		f.endHTML();
		
		return f.toString();
	}
	/* End Formatting - external interface
	 * ============================================================= */
}
