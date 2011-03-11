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

/**
 *
 * @author dclarke
 * $Id: EntityFormatter.java,v 1.1 2011/03/11 00:06:34 dclarke Exp $
 */
public abstract class EntityFormatter {

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	protected StringBuffer buffer;
	protected Formatter    format;
	/* End Fields
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public EntityFormatter() {
		buffer = new StringBuffer();
		format = new Formatter(buffer);
	}
	/* End Construction
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * HTML
	 * ================================================================
	 */
	public void startHTML() {
		buffer.append("<html>");
	}
	public void endHTML() {
		buffer.append("</html>");
	}
	public void startTable() {
		buffer.append("<table>");
	}
	public void endTable() {
		buffer.append("</table>");
	}
	public void startTR() {
		buffer.append("<tr>");
	}
	public void endTR() {
		buffer.append("</tr>");
	}
	public void startUL() {
		buffer.append("<ul>");
	}
	public void endUL() {
		buffer.append("</ul>");
	}
	public void startLI() {
		buffer.append("<li>");
	}
	public void endLI() {
		buffer.append("</li>");
	}
	public void li(String item) {
		startLI();
		buffer.append(item);
		endLI();
	}
	public void th(String contents) {
		buffer.append("<th align=\"left\">");
		buffer.append(contents);
		buffer.append("</th>");
	}
	public void th(String contents, int span) {
		format.format("<th align=\"left\" colspan=\"%d\">", span);
		buffer.append(contents);
		buffer.append("</th>");
	}
	public void td(String contents) {
		buffer.append("<td>");
		buffer.append(contents);
		buffer.append("</td>");
	}
	public void td(String contents, int span) {
		format.format("<td colspan=\"%d\">", span);
		buffer.append(contents);
		buffer.append("</td>");
	}
	public void tdItalic(String contents) {
		buffer.append("<td><i>");
		buffer.append(contents);
		buffer.append("</i></td>");
	}
	public void tdItalic(String contents, int span) {
		format.format("<td colspan=\"%d\"><i>", span);
		buffer.append(contents);
		buffer.append("</i></td>");
	}
	public void td(long contents) {
		buffer.append("<td>");
		format.format("%d", contents);
		buffer.append("</td>");
	}
	public void td(long contents, int span) {
		format.format("<td colspan=\"%d\">", span);
		format.format("%d", contents);
		buffer.append("</td>");
	}
	public void td(double contents) {
		buffer.append("<td>");
		format.format("%5.2f", contents);
		buffer.append("</td>");
	}
	public void td(double contents, int span) {
		format.format("<td colspan=\"%d\">", span);
		format.format("%5.2f", contents);
		buffer.append("</td>");
	}
	public void header(int level, String contents) {
		format.format("<h%d>", level);
		buffer.append(contents);
		format.format("</h%d>", level);
	}
	public void h1(String contents) {
		header(1, contents);
	}
	public void h2(String contents) {
		header(2, contents);
	}
	public void h3(String contents) {
		header(3, contents);
	}
	public void h4(String contents) {
		header(4, contents);
	}
	public void h5(String contents) {
		header(5, contents);
	}
	public void h6(String contents) {
		header(6, contents);
	}
	/* End HTML
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * utils
	 * ================================================================
	 */
	public String formatHHMMSS(long secs) {
		final long h = secs / 3600;
		secs = secs - (h*3600);
		final long m = secs / 60;
		secs = secs - (m*60);
		
		return String.format("%02d:%02d:%02d", h, m, secs);
	}
	/* End utils
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * toString
	 * ================================================================
	 */
	public String toString() {
		return buffer.toString();
	}
	/* End toString
	 * ============================================================= */
}
