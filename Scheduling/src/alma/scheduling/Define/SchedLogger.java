/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * Copyright by ESO (in the framework of the ALMA collaboration),
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
 * File SchedLogger.java
 */

package alma.scheduling.Define;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
public class SchedLogger implements SchedLoggerIF {
    private Logger logger;
    protected String mode;

    public SchedLogger(Logger l){ 
        //super("SchedulingLogger",null);
        logger = l;
        mode = "offline";
       // log(Level.FINE,"SchedulingLogger Created");
    }

    public void configureLogger(String mode) {
        this.mode = mode;
        logger.log(Level.FINE,"SchedulingLogger Created");
    }

    public void log(Level l, String msg, String audience ){
        logger.log(l, audience +" : "+ msg);
    }
    
    public void log(Level l, String msg, String audience, String array ){
        logger.log(l, audience +" : "+"Array = "+array +" : "+ msg);
    }
    public void info(String msg) {
        logger.info(msg);
    }
    public void fine(String msg) {
        logger.fine(msg);
    }
    public void finer(String msg) {
        logger.finer(msg);
    }
    public void finest(String msg) {
        logger.finest(msg);
    }
    public void severe(String msg) {
        logger.severe(msg);
    }
    public void warning(String msg) {
        logger.warning(msg);
    }
    public void addHandler(Handler handler){
        logger.addHandler(handler);
    }
    public void setLevel(Level l){
        logger.setLevel(l);
    }
}
