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

package alma.scheduling.planning_mode_sim.cli;

public class Console {

    private java.io.Console systemConsole = null;
    private static final String prompt ="> ";
    private static Console console = null;
    private static boolean requestedExit = false;
    private AprcTool aprc = null;
    
    private Console(AprcTool aprc){
        systemConsole = System.console();
        this.aprc = aprc;
    }
    
    public static Console getConsole(AprcTool aprc){
        if(console == null)
            return new Console(aprc);
        return console;
    }
    
    public void activate(){
        requestedExit = false;
        while(!requestedExit){
            systemConsole.printf(prompt, new Object[0]);
            interpret(systemConsole.readLine());
        }
    }
    
    private void interpret(String line){
        String[] lineParams = line.split(" ");
        if(lineParams[0].equals("exit"))
            System.exit(0);
        else if (lineParams[0].equals("step")){
            aprc.toBeInterrupted = true;
            requestedExit = true;
        }
        else if(lineParams[0].equals("run")){
            aprc.toBeInterrupted = false;
            requestedExit = true;
        }
    }
 
    public static void main(String[] args){
        Console console= Console.getConsole(null);
        console.activate();
    }
}
