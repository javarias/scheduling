/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */

package alma.scheduling.datamodel.helpers;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import alma.entity.xmlbinding.valuetypes.SensitivityT;
import alma.entity.xmlbinding.valuetypes.types.SensitivityTUnitType;


public class SensitivityConverterTest extends TestCase  {


	public SensitivityConverterTest(String name) {
        super(name);
    }
	
	protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    /**
     * @param args
     */
    public void testSensitivityConversions() throws Exception{
    	
    	final Map<SensitivityTUnitType, Double> sense = new HashMap<SensitivityTUnitType, Double>();
        sense.put(SensitivityTUnitType.MJY, 1.0 * 1000.0);
        sense.put(SensitivityTUnitType.JY, 1.0);
        
    	final SensitivityT u = new SensitivityT();
    	for (final SensitivityTUnitType startUnit : sense.keySet()) {
    		u.setContent(sense.get(startUnit));
    		u.setUnit(startUnit.toString());
        	for (final SensitivityTUnitType endUnit : sense.keySet()) {
        		try {
        			final double endValue = SensitivityConverter.convertedValue(u, endUnit);
        			assertEquals(
        					String.format("Converting %f %s to %s", u.getContent(), u.getUnit(), endUnit),
        					sense.get(endUnit), endValue, 0.001);
        		} catch (NeedsContextException e) {
        			assertTrue(String.format("Converting %f %s to %s and got a NeedsContextException",
        					u.getContent(), u.getUnit(), endUnit),
        					(sense.get(startUnit) < 0) || (sense.get(endUnit) < 0));
        		}
        	}
    	}
    }
    
}
