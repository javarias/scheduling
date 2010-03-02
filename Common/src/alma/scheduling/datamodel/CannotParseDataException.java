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
 * "@(#) $Id: CannotParseDataException.java,v 1.2 2010/03/02 17:09:02 javarias Exp $"
 */
package alma.scheduling.datamodel;

public class CannotParseDataException extends Exception {

    private static final long serialVersionUID = 4399567874855790838L;

    /**
     * 
     * @param Path of the file that cannot be parsed
     * @param ex The exception causing the problem
     */
    public CannotParseDataException(String file, Exception ex){
        super("Cannot parse the file: "+ file, ex);
    }
}
