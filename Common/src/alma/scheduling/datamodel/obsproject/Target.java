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
 * "@(#) $Id: Target.java,v 1.2 2010/04/30 22:44:36 dclarke Exp $"
 */
package alma.scheduling.datamodel.obsproject;

import java.util.HashSet;
import java.util.Set;

public class Target {

    private Long id;
    
    
    /** Polymorphic observing parameters, the type depends on the type of the observation */
    private Set<ObservingParameters> observingParameters = new HashSet<ObservingParameters>();
    
    private FieldSource source;

    public Target() { }

    public Target(Set<ObservingParameters> observingParameters, FieldSource source) {
        this.observingParameters = observingParameters;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Set<ObservingParameters> getObservingParameters() {
        return observingParameters;
    }

    public void setObservingParameters(Set<ObservingParameters> observingParameters) {
        this.observingParameters = observingParameters;
    }

    public void addObservingParameters(ObservingParameters observingParameters) {
        this.observingParameters.add(observingParameters);
    }
    
    public FieldSource getSource() {
        return source;
    }

    public void setSource(FieldSource source) {
        this.source = source;
    }
}
