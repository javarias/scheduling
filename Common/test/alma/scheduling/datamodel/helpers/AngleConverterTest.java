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
import alma.entity.xmlbinding.valuetypes.AngleT;
import alma.entity.xmlbinding.valuetypes.UserAngleT;
import alma.entity.xmlbinding.valuetypes.types.AngleTUnitType;
import alma.entity.xmlbinding.valuetypes.types.UserAngleTUserUnitType;


public class AngleConverterTest extends TestCase  {

	
	private Map<AngleTUnitType, Double> threeSixtyRegular;
	private Map<UserAngleTUserUnitType, Double> threeSixtyUser;

	public AngleConverterTest(String name) {
        super(name);
    }
	
	protected void setUp() throws Exception {
        super.setUp();
    	threeSixtyRegular = new HashMap<AngleTUnitType, Double>();
        threeSixtyRegular.put(AngleTUnitType.DEG, 360.0);
        threeSixtyRegular.put(AngleTUnitType.RAD, Math.PI*2.0);
        threeSixtyRegular.put(AngleTUnitType.ARCMIN, 360.0*60.0);
        threeSixtyRegular.put(AngleTUnitType.ARCSEC, 360.0*60.0*60.0);
        threeSixtyRegular.put(AngleTUnitType.MAS,    360.0*60.0*60.0*1000.0);
        
    	threeSixtyUser = new HashMap<UserAngleTUserUnitType, Double>();
        threeSixtyUser.put(UserAngleTUserUnitType.DEG, 360.0);
        threeSixtyUser.put(UserAngleTUserUnitType.RAD, Math.PI*2.0);
        threeSixtyUser.put(UserAngleTUserUnitType.ARCMIN, 360.0*60.0);
        threeSixtyUser.put(UserAngleTUserUnitType.ARCSEC, 360.0*60.0*60.0);
        threeSixtyUser.put(UserAngleTUserUnitType.MAS,    360.0*60.0*60.0*1000.0);
        threeSixtyUser.put(UserAngleTUserUnitType.FRACTION_OF_MAIN_BEAM, -1.0);
        threeSixtyUser.put(UserAngleTUserUnitType.H, 24.0);
        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    /**
     * @param args
     */
    public void testRegularToRegularConversions() throws Exception{
    	final AngleT u = new AngleT();
    	for (final AngleTUnitType startUnit : threeSixtyRegular.keySet()) {
    		u.setContent(threeSixtyRegular.get(startUnit));
    		u.setUnit(startUnit.toString());
        	for (final AngleTUnitType endUnit : threeSixtyRegular.keySet()) {
        		try {
        			final double endValue = AngleConverter.convertedValue(u, endUnit);
        			assertEquals(
        					String.format("Converting %f %s to %s", u.getContent(), u.getUnit(), endUnit),
        					threeSixtyRegular.get(endUnit), endValue, 0.001);
        		} catch (NeedsContextException e) {
        			assertTrue(String.format("Converting %f %s to %s and got a NeedsContextException",
        					u.getContent(), u.getUnit(), endUnit),
        					(threeSixtyRegular.get(startUnit) < 0) || (threeSixtyRegular.get(endUnit) < 0));
        		}
        	}
    	}
    }
    
    /**
     * @param args
     */
    public void testUserToUserAngleConversions() throws Exception{
    	
    	final UserAngleT u = new UserAngleT();
    	for (final UserAngleTUserUnitType startUnit : threeSixtyUser.keySet()) {
    		u.setContent(threeSixtyUser.get(startUnit));
    		u.setUnit(startUnit.toString());
        	for (final UserAngleTUserUnitType endUnit : threeSixtyUser.keySet()) {
        		try {
        			final double endValue = AngleConverter.convertedValue(u, endUnit);
        			assertEquals(
        					String.format("Converting %f %s to %s", u.getContent(), u.getUnit(), endUnit),
        					threeSixtyUser.get(endUnit), endValue, 0.001);
        		} catch (IllegalArgumentException e) {
        			// Skip tests where the from angle is illegal
        		} catch (NeedsContextException e) {
        			assertTrue(String.format("Converting %f %s to %s and got a NeedsContextException",
        					u.getContent(), u.getUnit(), endUnit),
        					(threeSixtyUser.get(startUnit) < 0) || (threeSixtyUser.get(endUnit) < 0));
        		}
        	}
    	}
    }

    
    /**
     * @param args
     */
    public void testRegularToUserConversions() throws Exception{
    	final AngleT u = new AngleT();
    	for (final AngleTUnitType startUnit : threeSixtyRegular.keySet()) {
    		u.setContent(threeSixtyRegular.get(startUnit));
    		u.setUnit(startUnit.toString());
        	for (final UserAngleTUserUnitType endUnit : threeSixtyUser.keySet()) {
        		try {
        			final double endValue = AngleConverter.convertedValue(u, endUnit);
        			assertEquals(
        					String.format("Converting %f %s to %s", u.getContent(), u.getUnit(), endUnit),
        					threeSixtyUser.get(endUnit), endValue, 0.001);
        		} catch (NeedsContextException e) {
        			assertTrue(String.format("Converting %f %s to %s and got a NeedsContextException",
        					u.getContent(), u.getUnit(), endUnit),
        					(threeSixtyRegular.get(startUnit) < 0) || (threeSixtyUser.get(endUnit) < 0));
        		}
        	}
    	}
    }
    
    /**
     * @param args
     */
    public void testUserToRegularAngleConversions() throws Exception{
    	
    	final UserAngleT u = new UserAngleT();
    	for (final UserAngleTUserUnitType startUnit : threeSixtyUser.keySet()) {
    		u.setContent(threeSixtyUser.get(startUnit));
    		u.setUnit(startUnit.toString());
        	for (final AngleTUnitType endUnit : threeSixtyRegular.keySet()) {
        		try {
        			final double endValue = AngleConverter.convertedValue(u, endUnit);
        			assertEquals(
        					String.format("Converting %f %s to %s", u.getContent(), u.getUnit(), endUnit),
        					threeSixtyRegular.get(endUnit), endValue, 0.001);
        		} catch (IllegalArgumentException e) {
        			// Skip tests where the from angle is illegal
        		} catch (NeedsContextException e) {
        			assertTrue(String.format("Converting %f %s to %s and got a NeedsContextException",
        					u.getContent(), u.getUnit(), endUnit),
        					(threeSixtyUser.get(startUnit) < 0) || (threeSixtyRegular.get(endUnit) < 0));
        		}
        	}
    	}
    }
}
