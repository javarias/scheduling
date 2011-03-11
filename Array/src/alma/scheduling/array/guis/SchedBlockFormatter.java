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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.scheduling.datamodel.obsproject.Preconditions;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.utils.Format;

/**
 *
 * @author dclarke
 * $Id: SchedBlockFormatter.java,v 1.1 2011/03/11 00:06:34 dclarke Exp $
 */
public class SchedBlockFormatter extends EntityFormatter {
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	private SchedBlockFormatter() {
		super();
		this.countedExecutions = false;
    	this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}
	/* End Construction
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Execution counting
	 * ================================================================
	 */
	private boolean countedExecutions = false;
	private DateFormat dateFormat = null;
	private int totalEx      = 0;
	private int successfulEx = 0;
	private int failedEx     = 0;
	private long totalSecs      = 0;
	private long successfulSecs = 0;
	private long failedSecs     = 0;
	
	private void countExecutions(SBStatus sbs) {
		if (countedExecutions) {
			// Already done
			return;
		}
		for (final ExecStatusT es : sbs.getExecStatus()) {
			long secs = 0;
			try {
				final Date d0 = dateFormat.parse(es.getStatus().getStartTime());
				final Date d1 = dateFormat.parse(es.getStatus().getEndTime());
				secs = (d1.getTime() - d0.getTime()) / 1000;
			} catch (ParseException e) {
				System.out.format("Trying to parse %s and %s%n", 
						es.getStatus().getStartTime(),
						es.getStatus().getEndTime());
				e.printStackTrace();
			}
			totalEx ++;
			totalSecs += secs;
			if (es.getStatus().getState().equals(StatusTStateType.BROKEN)) {
				// It failed
				failedEx ++;
				failedSecs += secs;
			} else {
				successfulEx ++;
				successfulSecs += secs;
			}
		}
	}
	/* End Execution counting
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
	private void formatName(SchedBlock sb) {
		startTR();
		th("Name");
		td(sb.getName(), 3);
		endTR();
	}
	
	private void formatKnownStatus(SBStatus sbs) {
		startTR();
		th("Status");
		td(sbs.getStatus().getState().toString(), 3);
		endTR();
	}
	
	private void formatUnknownStatus(String reason) {
		startTR();
		td("Unknown status", 2);
		td(String.format("(%s)", reason), 2);
		endTR();
	}
	
	private void formatNote(SchedBlock sb) {
		final String note = sb.getNote();
		
		if ((note != null) && (note.length() != 0)) {
			startTR();
			th("Note");
			td(note, 3);
			endTR();
		}
	}
	
	private void formatCoords(SchedBlock sb) {
		final SkyCoordinates coords = sb.getRepresentativeCoordinates();
		startTR();
		th("RA");
		if (coords != null) {
			td(Format.formatRA(coords.getRA()));
		} else {
			td("n/a");
		}
		th("Dec");
		if (coords != null) {
			td(Format.formatDec(coords.getDec()));
		} else {
			td("n/a");
		}
		endTR();
	}
	
	private void formatHA(SchedBlock sb) {
		final Preconditions pre = sb.getPreConditions();
		startTR();
		th("Min HA");
		if (pre != null) {
			td(String.format("%4.1f", pre.getMinAllowedHourAngle()));
		} else {
			td("n/a");
		}
		th("Max HA");
		if (pre != null) {
			td(String.format("%4.1f", pre.getMaxAllowedHourAngle()));
		} else {
			td("n/a");
		}
		endTR();
	}
	
	private void formatExecutionCounts(SchedBlock sb, SBStatus sbs) {
		countExecutions(sbs);

		startTR();
		th("Executions", 2);
		th("", 3);
		endTR();
		startTR();
		td(""); // Empty cell
		tdItalic("Total:");
		td(totalEx);
		if (sb.getSchedBlockControl().getIndefiniteRepeat()) {
			tdItalic("Indefinite repeat", 2);
		} else {
			tdItalic("Remaining:");
			td(sbs.getExecutionsRemaining());
		}
		endTR();
		startTR();
		td(""); // Empty cell
		tdItalic("Success:");
		td(successfulEx);
		tdItalic("Fail:");
		td(failedEx);
		endTR();
	}
	
	private void formatTimes(SBStatus sbs) {
		countExecutions(sbs);

		startTR();
		th("Times", 2);
		th("", 3);
		endTR();
		startTR();
		td(""); // Empty cell
		tdItalic("Total:");
		td(formatHHMMSS(totalSecs));
		tdItalic("Remaining:");
		td("unknown");
		endTR();
		startTR();
		td(""); // Empty cell
		tdItalic("Success:");
		td(formatHHMMSS(successfulSecs));
		tdItalic("Fail:");
		td(formatHHMMSS(failedSecs));
		endTR();
	}
	
	private void formatSensitivity(SBStatus sbs) {
		countExecutions(sbs);

		startTR();
		th("Sensitivity", 2);
		th("", 3);
		endTR();
		startTR();
		td(""); // Empty cell
		tdItalic("Target:");
		td("unknown");
		tdItalic("Achieved:");
		td("unknown");
		endTR();
	}
	
	private void formatExecutions(SBStatus sbs) {
		if (sbs.getExecStatusCount() != 0) {
			startTable();
			startTR();
			th("Execution Details", 4);
			endTR();
			startTR();
			th("#");
			th("Status");
			th("Start");
			th("End");
			endTR();
			int exec = 1;
			for (final ExecStatusT es : sbs.getExecStatus()) {
				startTR();
				td(exec++);
				td(es.getStatus().getState().toString());
				td(es.getStatus().getStartTime());
				td(es.getStatus().getEndTime());
				endTR();
			}
			endTable();
		}
	}
	/* End Utilities
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Formatting
	 * ================================================================
	 */
	public static String formatted(SchedBlock sb, SBStatus sbs) {
		final SchedBlockFormatter f = new SchedBlockFormatter();
		
		f.startHTML();
		
		f.startTable();
		
		f.formatName(sb);
		f.formatKnownStatus(sbs);
		f.formatNote(sb);
		f.startTR();
		f.th("Entity ID");
		f.td(sb.getUid(), 3);
		f.endTR();
		f.startTR();
		f.th("Status ID");
		f.td(sb.getStatusEntity().getEntityId(), 3);
		f.endTR();
		f.formatCoords(sb);
		f.formatHA(sb);
		f.endTable();

		f.startTable();
		f.formatExecutionCounts(sb, sbs);
		f.formatTimes(sbs);
		f.formatSensitivity(sbs);
		f.endTable();
		
		f.formatExecutions(sbs);
		
		f.endHTML();
		
		return f.toString();
	}
	
	public static String formatted(SchedBlock sb, String reason) {
		final SchedBlockFormatter f = new SchedBlockFormatter();
		
		f.startHTML();
		
		f.startTable();
		
		f.formatName(sb);
		f.formatUnknownStatus(reason);
		f.formatNote(sb);
		f.formatCoords(sb);
		f.formatHA(sb);
		
		f.endTable();

		f.endHTML();
		
		return f.toString();
	}
	/* End Construction
	 * ============================================================= */
}
