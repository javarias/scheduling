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
import alma.entity.xmlbinding.valuetypes.AngularVelocityT;
import alma.entity.xmlbinding.valuetypes.FrequencyT;
import alma.entity.xmlbinding.valuetypes.SensitivityT;
import alma.entity.xmlbinding.valuetypes.UserAngleT;
import alma.entity.xmlbinding.valuetypes.types.AngleTUnitType;
import alma.entity.xmlbinding.valuetypes.types.AngularVelocityTUnitType;
import alma.entity.xmlbinding.valuetypes.types.FrequencyTUnitType;
import alma.entity.xmlbinding.valuetypes.types.SensitivityTUnitType;
import alma.entity.xmlbinding.valuetypes.types.UserAngleTUserUnitType;


public class ConverterTest extends TestCase  {

	
	private Map<AngleTUnitType, Double> threeSixtyRegular;
	private Map<UserAngleTUserUnitType, Double> threeSixtyUser;
	private Map<AngularVelocityTUnitType, Double> spin;
	private Map<FrequencyTUnitType, Double> radio4LongWave;
	private Map<SensitivityTUnitType, Double> sense;
	
	public ConverterTest(String name) {
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
        
    	spin = new HashMap<AngularVelocityTUnitType, Double>();
    	spin.put(AngularVelocityTUnitType.ARCSEC_S, 360.0 * 60.0 * 60.0);
    	spin.put(AngularVelocityTUnitType.ARCMIN_S, 360.0 * 60.0);
    	spin.put(AngularVelocityTUnitType.DEG_S,    360.0);
    	spin.put(AngularVelocityTUnitType.MAS_YR,   360.0 * 60.0 * 60.0 * 1000.0 * 60.0 * 60.0 * 24.0 * 365.25);

    	radio4LongWave = new HashMap<FrequencyTUnitType, Double>();
        radio4LongWave.put(FrequencyTUnitType.HZ, 198.0 * 1000.0);
        radio4LongWave.put(FrequencyTUnitType.KHZ, 198.0);
        radio4LongWave.put(FrequencyTUnitType.MHZ, 198.0 / 1000.0);
        radio4LongWave.put(FrequencyTUnitType.GHZ, 198.0 / (1000.0 * 1000.0));

    	sense = new HashMap<SensitivityTUnitType, Double>();
        sense.put(SensitivityTUnitType.MJY, 1.0 * 1000.0);
        sense.put(SensitivityTUnitType.JY, 1.0);
}

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    /**
     * @param args
     */
    public void testAngleRegularToRegularConversions() throws Exception{
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
    public void testAngleUserToUserConversions() throws Exception{
    	
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
    public void testAngleRegularToUserConversions() throws Exception{
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
    public void testAngleUserToRegularConversions() throws Exception{
    	
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

    public void testAngularVelocityConversions() throws Exception{
    	
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
    
    /**
     * @param args
     */
    public void testFrequencyConversions() throws Exception{
    	
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

    /**
     * @param args
     */
    public void testSensitivityConversions() throws Exception{
    	
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
