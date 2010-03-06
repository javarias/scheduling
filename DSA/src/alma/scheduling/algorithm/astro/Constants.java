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
 * "@(#) $Id: Constants.java,v 1.1 2010/03/06 00:57:17 rhiriart Exp $"
 */
package alma.scheduling.algorithm.astro;

/**
 * Constants used for system temperature and sensitiviy calculations.
 */
public class Constants {
    /** Boltzmann constant (Jy/K) */
    static final double BOLTZMANN = 1.38e-16 * 1.0e23;
    /** Plank constant (J*s) */
    static final double PLANCK    = 6.626e-34;
    /** Speed of light (m/s) */
    static final double LIGHT_SPEED= 2.99792458e8;
}
