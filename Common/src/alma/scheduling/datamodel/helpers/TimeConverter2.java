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
package alma.scheduling.datamodel.helpers;

public class TimeConverter2 {
   public static double toHours(double value, String unit) {
       double FACTOR = 1.0; // to seconds first
       if (unit.equals("ns")) {
               FACTOR = 1.0E-9;
       } else if (unit.equals("us")) {
               FACTOR = 1.0E-6;
       } else if (unit.equals("ms")) {
               FACTOR = 1.0E-3;
       } else if (unit.equals("s")) {
               FACTOR = 1.0;
       } else if (unit.equals("min")) {
               FACTOR = 60.0;
       } else if (unit.equals("h")) {
               FACTOR = 3600.0;
       }
       return FACTOR * value / 3600.0;
   }
}
