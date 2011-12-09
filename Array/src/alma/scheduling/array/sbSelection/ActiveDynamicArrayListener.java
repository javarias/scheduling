/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
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
package alma.scheduling.array.sbSelection;

import java.util.Observable;
import java.util.Observer;

import alma.scheduling.ArrayGUIOperation;
import alma.scheduling.ArrayOperations;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.algorithm.results.Result;
import alma.scheduling.algorithm.results.dao.ResultsDao;
import alma.scheduling.array.guis.ArrayGUINotification;
import alma.scheduling.utils.DSAContextFactory;

public class ActiveDynamicArrayListener implements Observer {

	private boolean active;
	private ArrayOperations array;
	private ResultsDao resultsDao = (ResultsDao) DSAContextFactory.getContext()
			.getBean(DSAContextFactory.SCHEDULING_DSA_RESULTS_DAO_BEAN);
	
	public ActiveDynamicArrayListener (ArrayOperations array) {
		active = false;
		this.array = array;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public void update(Observable o, Object arg) {
		try {
			ArrayGUINotification notification = (ArrayGUINotification) arg;
			if (!active)
				return;
			if (notification.getOperation().value() == ArrayGUIOperation._SCORESREADY) {
				Result r = resultsDao.getCurrentResult(array.getArrayName());
				SchedBlockQueueItem item = new SchedBlockQueueItem(
						System.nanoTime(), r.getScores().get(0)
								.getUid());
				array.push(item);
			}
		} catch (ClassCastException ex) {
			//Wrong event, do nothing
		}
		
	}

}
