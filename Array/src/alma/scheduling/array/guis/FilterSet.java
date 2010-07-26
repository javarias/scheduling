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

package alma.scheduling.array.guis;

import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

/**
 * A set of filters for the columns of a TableModel
 * @author dclarke
 * $Id: FilterSet.java,v 1.1 2010/07/26 16:36:19 dclarke Exp $
 */
public class FilterSet {
	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
	private static String defaultFilter = ".*";
	/* End Constants
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private TableModel    table;
	private List<String>  filters;
	private List<Pattern> patterns;
	private List<Boolean> inUse;
	
	private Set<ChangeListener> subscribers;
	/* End Fields
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Constructors
	 * ================================================================
	 */
	public FilterSet(TableModel table) {
		this.table = table;
		initialise();
		subscribers = new HashSet<ChangeListener> ();
	}
	
	/**
	 * Clear all the filters for this set
	 */
	public void initialise() {
		filters  = new Vector<String>(numCols());
		patterns = new Vector<Pattern>(numCols());
		inUse    = new Vector<Boolean>(numCols());
		for (int i = 0; i < numCols(); i++) {
			filters.add(defaultFilter);
			patterns.add(Pattern.compile(defaultFilter));
			inUse.add(false);
		}
	}
	/* End Constructors
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Utility methods
	 * ================================================================
	 */
	private int numCols() {
		return table.getColumnCount();
	}
	
	/**
	 * Clear the given filter for this set
	 */
	public void resetNoPublish(int col) {
		filters.set(col, defaultFilter);
		patterns.set(col, Pattern.compile(defaultFilter));
		inUse.set(col, false);
	}
	/* End Utility methods
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Public Interface
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder b = new StringBuilder();
		Formatter     f = new Formatter(b);
		boolean doneSome = false;

		b.append("<html>");
		for (int i = 0; i < numCols(); i++) {
			if (inUse.get(i)) {
				if (doneSome) {
					b.append("; ");
				}
				f.format("<font color=#7f7fff>%s:</font> %s",
						table.getColumnName(i),
						filters.get(i));
				doneSome = true;
			}
		}
		if (!doneSome) {
			b.append("<em>&lt;No filtering&gt;</em>");
		}
		b.append("</html>");
		return b.toString();
	}
	
	/**
	 * Clear the given filter for this set
	 */
	public void reset(int col) {
		resetNoPublish(col);
		publish();
	}
	
	/**
	 * Clear all the filters for this set
	 */
	public void reset() {
		for (int i = 0; i < numCols(); i++) {
			resetNoPublish(i);
		}
		publish();
	}
	
	/**
	 * Set the given filter
	 */
	public void set(int col, String regexp)
										throws PatternSyntaxException {
		final Pattern p = Pattern.compile(regexp);
		
		filters.set(col, regexp);
		patterns.set(col, p);
		inUse.set(col, true);
		publish();
	}
	
	/**
	 * Get the name of the given filter
	 */
	public String getName(int col) {
		return table.getColumnName(col);
	}
	
	/**
	 * Get the given filter
	 */
	public String getFilter(int col) {
		return filters.get(col);
	}
	/* End Public Interface
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Publisher/Subscriber
	 * ================================================================
	 */
	public void addChangeListener(ChangeListener listener) {
		subscribers.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		subscribers.remove(listener);
	}
	
	private void publish() {
		final ChangeEvent e = new ChangeEvent(this);
		for (final ChangeListener listener : subscribers) {
			listener.stateChanged(e);
		}
	}
	/* End Public Interface
	 * ============================================================= */
}
