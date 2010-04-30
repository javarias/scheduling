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
import alma.entity.xmlbinding.valuetypes.AngularVelocityT;
import alma.entity.xmlbinding.valuetypes.types.AngularVelocityTUnitType;


public class AngularVelocityConverterTest extends TestCase  {


	public AngularVelocityConverterTest(String name) {
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
    public void testAngularVelocityConversions() throws Exception{
    	
    	final Map<AngularVelocityTUnitType, Double> spin = new HashMap<AngularVelocityTUnitType, Double>();
    	spin.put(AngularVelocityTUnitType.ARCSEC_S, 360.0 * 60.0 * 60.0);
    	spin.put(AngularVelocityTUnitType.ARCMIN_S, 360.0 * 60.0);
    	spin.put(AngularVelocityTUnitType.DEG_S,    360.0);
    	spin.put(AngularVelocityTUnitType.MAS_YR,   360.0 * 60.0 * 60.0 * 1000.0 * 60.0 * 60.0 * 24.0 * 365.25);
        
    	final AngularVelocityT u = new AngularVelocityT();
    	for (final AngularVelocityTUnitType startUnit : spin.keySet()) {
    		u.setContent(spin.get(startUnit));
    		u.setUnit(startUnit.toString());
        	for (final AngularVelocityTUnitType endUnit : spin.keySet()) {
        		try {
        			final double endValue = AngularVelocityConverter.convertedValue(u, endUnit);
        			assertEquals(
        					String.format("Converting %f %s to %s", u.getContent(), u.getUnit(), endUnit),
        					spin.get(endUnit), endValue, 0.001);
        		} catch (NeedsContextException e) {
        			assertTrue(String.format("Converting %f %s to %s and got a NeedsContextException",
        					u.getContent(), u.getUnit(), endUnit),
        					(spin.get(startUnit) < 0) || (spin.get(endUnit) < 0));
        		}
        	}
    	}
    }
    
}
