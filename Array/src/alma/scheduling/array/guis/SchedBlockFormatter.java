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
import java.util.List;

import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.obsproject.Preconditions;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.utils.Format;

/**
 *
 * @author dclarke
 * $Id: SchedBlockFormatter.java,v 1.3 2011/03/28 23:32:55 dclarke Exp $
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
	
	private void formatScoresAndRanks(SBRank  currScore,
			                          Integer currRank,
			                          SBRank  prevScore,
			                          Integer prevRank) {
		final boolean noScores = (currScore == null) &&
		                           (prevScore == null);
		final boolean noRanks  = (currRank == null) &&
		                           (prevRank == null);
		
		if ( noScores && noRanks ) {
			// There's nothing to do
			return;
		}

		startTable();
		startTR();
		th("Score and Rank", 4);
		endTR();
		startTR();
		th("", 2);
		th("Current");
		th("Previous");
		endTR();
		
		if (!noRanks) {
			startTR();
			tdItalic("Rank", 2);
			if (currRank != null) {
				td(currRank);
			} else {
				td("n/a");
			}
			if (prevRank != null) {
				td(prevRank);
			} else {
				td("n/a");
			}
			endTR();
		}
		
		if (!noScores) {
			startTR();
			tdItalic("Overall", 2);
			try {
				td(currScore.getRank());
			} catch (NullPointerException e) {
				td("n/a");
			}
			try {
				td(prevScore.getRank());
			} catch (NullPointerException e) {
				td("n/a");
			}
			endTR();
			
			List<SBRank> currParts;
			List<SBRank> prevParts;
			List<SBRank> either = null;
			
			try {
				currParts = currScore.getBreakdownScore();
				either = currParts;
			} catch (NullPointerException e) {
				currParts = null;
			}
			try {
				prevParts = prevScore.getBreakdownScore();
				either = prevParts;
			} catch (NullPointerException e) {
				prevParts = null;
			}
			for (int i = 0; i < either.size(); i++) {
				final SBRank r = either.get(i);
				startTR();
				td("");
				tdItalic(r.getDetails());
				try {
					td(currParts.get(i).getRank());
				} catch (NullPointerException e) {
					td("n/a");
				}
				try {
					td(prevParts.get(i).getRank());
				} catch (NullPointerException e) {
					td("n/a");
				}
				endTR();
			}
		}

	}
	/* End Utilities
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Formatting
	 * ================================================================
	 */
	public void formatBasics(SchedBlock sb, SBStatus sbs) {
		startTable();
		
		formatName(sb);
		formatKnownStatus(sbs);
		formatNote(sb);
		startTR();
		th("Entity ID");
		td(sb.getUid(), 3);
		endTR();
		startTR();
		th("Status ID");
		td(sb.getStatusEntity().getEntityId(), 3);
		endTR();
		formatCoords(sb);
		formatHA(sb);
		endTable();

		startTable();
		formatExecutionCounts(sb, sbs);
		formatTimes(sbs);
		formatSensitivity(sbs);
		endTable();
		
		formatExecutions(sbs);
	}
	
	public void formatBasics(SchedBlock sb, String reason) {
		startTable();
		
		formatName(sb);
		formatUnknownStatus(reason);
		formatNote(sb);
		startTR();
		th("Entity ID");
		td(sb.getUid(), 3);
		endTR();
		formatCoords(sb);
		formatHA(sb);
		
		endTable();
	}
	/* End Formatting
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Formatting - external interface
	 * ================================================================
	 */
	/**
	 * Format a SchedBlock with the given status (but no scores/ranks).
	 * 
	 * @param sb
	 * @param sbs
	 * @return An HTML string denoting the SchedBlock
	 */
	public static String formatted(SchedBlock sb, SBStatus sbs) {
		final SchedBlockFormatter f = new SchedBlockFormatter();
		
		f.startHTML();
		f.formatBasics(sb, sbs);
		f.endHTML();
		
		return f.toString();
	}
	
	/**
	 * Format a SchedBlock with no status (for the given reason) and no
	 * scores/ranks.
	 * 
	 * @param sb
	 * @param reason - the reason there is no status
	 * @return An HTML string denoting the SchedBlock
	 */
	public static String formatted(SchedBlock sb, String reason) {
		final SchedBlockFormatter f = new SchedBlockFormatter();
		
		f.startHTML();
		f.formatBasics(sb, reason);
		f.endHTML();
		
		return f.toString();
	}
	
	
	/**
	 * Format a SchedBlock with the given status and with the given
	 * scores and ranks.
	 * 
	 * @param sb
	 * @param sbs
	 * @param currScore - the latest score for the SB
	 * @param currRank  - the latest rank of the SB. An
	 *                    <code>Integer</code> rather than an
	 *                    <code>int</code> so that we can use
	 *                    <code>null</code> to denote no rank.
	 * @param prevScore - the previous score for the SB
	 * @param prevRank  - the previous rank of the SB. An
	 *                    <code>Integer</code> rather than an
	 *                    <code>int</code> so that we can use
	 *                    <code>null</code> to denote no rank.
	 * @return An HTML string denoting the SchedBlock
	 */
	public static String formatted(SchedBlock sb,
			                       SBStatus   sbs,
			                       SBRank     currScore,
			                       Integer    currRank,
			                       SBRank     prevScore,
			                       Integer    prevRank) {
		final SchedBlockFormatter f = new SchedBlockFormatter();
		
		f.startHTML();
		f.formatBasics(sb, sbs);
		f.formatScoresAndRanks(currScore, currRank, prevScore, prevRank);
		f.endHTML();
		
		return f.toString();
	}
	
	/**
	 * Format a SchedBlock with no status (for the given reason) but
	 * with the given scores and ranks.
	 * 
	 * @param sb
	 * @param reason - the reason there is no status
	 * @param currScore - the latest score for the SB
	 * @param currRank  - the latest rank of the SB. An
	 *                    <code>Integer</code> rather than an
	 *                    <code>int</code> so that we can use
	 *                    <code>null</code> to denote no rank.
	 * @param prevScore - the previous score for the SB
	 * @param prevRank  - the previous rank of the SB. An
	 *                    <code>Integer</code> rather than an
	 *                    <code>int</code> so that we can use
	 *                    <code>null</code> to denote no rank.
	 * @return An HTML string denoting the SchedBlock
	 */
	public static String formatted(SchedBlock sb,
			                       String     reason,
			                       SBRank     currScore,
			                       Integer    currRank,
			                       SBRank     prevScore,
			                       Integer    prevRank) {
		final SchedBlockFormatter f = new SchedBlockFormatter();
		
		f.startHTML();
		f.formatBasics(sb, reason);
		f.formatScoresAndRanks(currScore, currRank, prevScore, prevRank);
		f.endHTML();
		
		return f.toString();
	}
	/* End Formatting - external interface
	 * ============================================================= */
}
