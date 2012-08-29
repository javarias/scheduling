/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.array.executor;

import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.PipelineProcessingRequestT;
import alma.entity.xmlbinding.ousstatus.SessionT;
import alma.entity.xmlbinding.sbstatus.SBStatus;

/**
 * @author rhiriart
 *
 */
public class Utils {

//	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//
//    public static String genPartId() {
//    	final Date d = new Date();
//    	return dateFormat.format(d);
//    }
	
    public static String genPartId(OUSStatus ouss) {
    	int numSessions = ouss.getSessionCount();
    	return String.format("X%08x", numSessions);
    }
    
    public static String genPartId(SBStatus sbStatus) {
    	int numExecStatus = sbStatus.getExecStatusCount();
    	return String.format("X%08x",numExecStatus);
    }
    
    public static String genPPRId(OUSStatus ouss) {
    	// There's only 1 PPR per OUS, use a value which is never
    	// going to be hit by the sessions count
    	return "Xffffffff";
    }
    
    public static void main(String args[]) {
    	final OUSStatus s1 = new OUSStatus();
    	final OUSStatus s2 = new OUSStatus();
		System.out.format("%s\t%s%n", genPartId(s1), genPartId(s2));
		for (int i = 0; i < 256; i++) {
    		s1.addSession(new SessionT());
    		if (i % 2 == 0) {
    			s2.addSession(new SessionT());
    		}
    		System.out.format("%s\t%s%n", genPartId(s1), genPartId(s2));
    	}
		s1.setPipelineProcessingRequest(new PipelineProcessingRequestT());
		System.out.format("%n%s\t%s%n", genPPRId(s1), genPPRId(s2));
    }
}
