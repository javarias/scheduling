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
import alma.entity.xmlbinding.valuetypes.FrequencyT;
import alma.entity.xmlbinding.valuetypes.types.FrequencyTUnitType;


public class FrequencyConverterTest extends TestCase  {


	public FrequencyConverterTest(String name) {
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
    public void testFrequencyConversions() throws Exception{
    	
    	final Map<FrequencyTUnitType, Double> radio4LongWave = new HashMap<FrequencyTUnitType, Double>();
        radio4LongWave.put(FrequencyTUnitType.HZ, 198.0 * 1000.0);
        radio4LongWave.put(FrequencyTUnitType.KHZ, 198.0);
        radio4LongWave.put(FrequencyTUnitType.MHZ, 198.0 / 1000.0);
        radio4LongWave.put(FrequencyTUnitType.GHZ, 198.0 / (1000.0 * 1000.0));
        
    	final FrequencyT u = new FrequencyT();
    	for (final FrequencyTUnitType startUnit : radio4LongWave.keySet()) {
    		u.setContent(radio4LongWave.get(startUnit));
    		u.setUnit(startUnit.toString());
        	for (final FrequencyTUnitType endUnit : radio4LongWave.keySet()) {
        		try {
        			final double endValue = FrequencyConverter.convertedValue(u, endUnit);
        			assertEquals(
        					String.format("Converting %f %s to %s", u.getContent(), u.getUnit(), endUnit),
        					radio4LongWave.get(endUnit), endValue, 0.001);
        		} catch (NeedsContextException e) {
        			assertTrue(String.format("Converting %f %s to %s and got a NeedsContextException",
        					u.getContent(), u.getUnit(), endUnit),
        					(radio4LongWave.get(startUnit) < 0) || (radio4LongWave.get(endUnit) < 0));
        		}
        	}
    	}
    }
    
}
