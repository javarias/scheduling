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
 * $Id: FilterSet.java,v 1.2 2010/07/26 23:37:23 dclarke Exp $
 */
public class FilterSet {
	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
	private static String defaultFilter = ".*";
	private static Pattern defaultPattern
									  = Pattern.compile(defaultFilter);
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
	
	private String stripXMLTags(String s) {
		while (s.startsWith("<")) {
			s = s.substring(s.indexOf('>')+1);
		}
		while (s.endsWith(">")) {
			s = s.substring(0, s.lastIndexOf('<'));
		}
		return s;
	}
	/* End Utility methods
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Public Interface
	 * ================================================================
	 */
	/**
	 * A bit like toString(), but with some HTML tags to do a wee bit
	 * of formatting.
	 * 
	 * @return An HTML representation of this FilterSet, complete with
	 * <code>&lt;html&gt;</code> &amp; <code>&lt;/html&gt;</code>
	 */
	public String toHTML() {
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
						stripXMLTags(table.getColumnName(i)),
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean doneSome = false;

		for (int i = 0; i < numCols(); i++) {
			if (inUse.get(i)) {
				if (doneSome) {
					b.append("; ");
				}
				b.append(stripXMLTags(table.getColumnName(i)));
				doneSome = true;
			}
		}
		if (!doneSome) {
			b.append("No filtering");
		}
		return b.toString();
	}
	
	/**
	 * Set the given filter
	 */
	public void set(int col, String regexp)
										throws PatternSyntaxException {
		final Pattern p = Pattern.compile(regexp);
		
		filters.set(col, regexp);
		patterns.set(col, p);
		publish();
	}
	
	/**
	 * Reset the filter for the given column back to the default.
	 */
	public void reset(int col) {
		filters.set(col, defaultFilter);
		patterns.set(col, defaultPattern);
		publish();
	}
	
	/**
	 * Reset the filter for all columns back to the default.
	 */
	public void reset() {
		for (int col = 0; col < numCols(); col++) {
			filters.set(col, defaultFilter);
			patterns.set(col, defaultPattern);
		}
		publish();
	}
	
	/**
	 * Activate the filter in the given column
	 * 
	 * @param col
	 */
	public void activate(int col) {
		inUse.set(col, true);
		publish();
	}
	
	/**
	 * Deactivate the filter in the given column
	 * 
	 * @param col
	 */
	public void deactivate(int col) {
		inUse.set(col, false);
		publish();
	}
	
	/**
	 * Activate the filter in all the columns
	 */
	public void activate() {
		for (int col = 0; col < numCols(); col++) {
			inUse.set(col, true);
		}
		publish();
	}
	
	/**
	 * Deactivate the filter in all the columns
	 */
	public void deactivate() {
		for (int col = 0; col < numCols(); col++) {
			inUse.set(col, false);
		}
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
