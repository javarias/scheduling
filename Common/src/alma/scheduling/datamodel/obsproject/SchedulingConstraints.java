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
package alma.scheduling.datamodel.obsproject;

public class SchedulingConstraints {

    /** Maximum angular resolution (arcseconds) */
    private Double maxAngularResolution;
    
    /** Representative frequency (GHz) */
    private Double representativeFrequency;
    
    /** Representative band */
    private Integer representativeBand;
    
    private Target representativeTarget;
    
    private SchedBlockMode schedBlockMode;

	public SchedulingConstraints() { }
    
    public SchedulingConstraints(Double maxAngularResolution,
            Double representativeFrequency,
            Target representativeTarget) {
        this.maxAngularResolution = maxAngularResolution;
        this.representativeFrequency = representativeFrequency;
        this.representativeTarget = representativeTarget;
    }

    public Double getMaxAngularResolution() {
        return maxAngularResolution;
    }

    public void setMaxAngularResolution(Double maxAngularResolution) {
        this.maxAngularResolution = maxAngularResolution;
    }

    public Double getRepresentativeFrequency() {
        return representativeFrequency;
    }

    public void setRepresentativeFrequency(Double representativeFrequency) {
        this.representativeFrequency = representativeFrequency;
    }

    public Integer getRepresentativeBand() {
        return representativeBand;
    }

    public void setRepresentativeBand(Integer representativeBand) {
        this.representativeBand = representativeBand;
    }

    public Target getRepresentativeTarget() {
        return representativeTarget;
    }

    public void setRepresentativeTarget(Target representativeTarget) {
        this.representativeTarget = representativeTarget;
    }
    
	public SchedBlockMode getSchedBlockMode() {
		return schedBlockMode;
	}

	public void setSchedBlockMode(SchedBlockMode schedBlockMode) {
		this.schedBlockMode = schedBlockMode;
	}
}
