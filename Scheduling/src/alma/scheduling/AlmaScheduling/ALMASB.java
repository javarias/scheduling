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
 * File ALMASB.java
 */
package alma.scheduling.AlmaScheduling;

import alma.scheduling.Define.SB;
import alma.scheduling.Define.Target;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Equatorial;

//import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.*;
import alma.entities.generalincludes.*;

/**
 * Used when a scheduling block (SchedBlock) is retrieved
 * from the archvie. The information required by the 
 * scheduling subsystem in "Real Scheduling Mode" is extracted
 * from the schedblock and used to populate the contents of 
 * the SB. 
 * @author Sohaila Roberts
 */
public class ALMASB extends SB {
    //Copy of the schedblock that came out of the archive.
    private SchedBlock almaSchedBlock;
    private TargetT[] almaObsTarget;
    private TargetT[] almaPhaseCalTarget;
    private TargetT[] almaPointingCalTarget;
    
    public ALMASB(SchedBlock sb, String id) {
        super(id);
        this.almaSchedBlock = sb;
        extractInfoFromSB();
        setReady(new DateTime(System.currentTimeMillis()));
        //System.out.println("in ALMASB: "+ getStatus().isReady());
    }

    private void extractInfoFromSB() {
      try {
        this.almaObsTarget = almaSchedBlock.getObsTarget();
        for(int i=0; i < almaObsTarget.length; i++) {
            double ra = almaObsTarget[i].getFieldSpec().getFieldSource().
                getSourceCoordinates().getLatitude().getContent();
            
            String ra_unit = almaObsTarget[i].getFieldSpec().getFieldSource().
                getSourceCoordinates().getLatitude().getUnit();
            //System.out.println(ra + " " + ra_unit);

            double dec = almaObsTarget[i].getFieldSpec().getFieldSource().
                getSourceCoordinates().getLongitude().getContent();
            String dec_unit = almaObsTarget[i].getFieldSpec().getFieldSource().
                getSourceCoordinates().getLongitude().getUnit();
            //System.out.println(dec + " " + dec_unit);
            FieldPatternT target_pattern = almaObsTarget[i].getFieldSpec().
                getFieldPattern();
            String type = target_pattern.getType().toString();
            //System.out.println(type);
            Target t;
            if(type.equals("rectangle")){
                RectangleT_SB rec = target_pattern.getRectangle();
                if(rec == null) {
                    //System.out.println("Rectangle info not there! rec == null!");
                }
                if(rec.getLatitudeLength() == null) {
                    //System.out.println("latitude length == null");
                }
                //System.out.println(rec.getLatitudeLength().getContent());
                //System.out.println(rec.getLatitudeLength().getUnit());
                //System.out.println(rec.getLongitudeLength().getContent());
                //System.out.println(rec.getLongitudeLength().getUnit());
                t = new Target(new Equatorial(ra,dec), 10,20);
                super.setTarget(t);
            } else if(type.equals("circle")) {
            } else if(type.equals("spiral")) {
            } else {
                // error?
            }
        }
        this.almaPhaseCalTarget = almaSchedBlock.getPhaseCalTarget(); 
        this.almaPointingCalTarget = almaSchedBlock.getPointingCalTarget(); 
      } catch(Exception e) {
          System.out.println("SCHEDULING: "+e.toString());
          e.printStackTrace();
      }
    }
    
}
