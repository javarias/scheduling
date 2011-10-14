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

package alma.scheduling.array.sbSelection;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import alma.scheduling.algorithm.results.Result;
import alma.scheduling.algorithm.results.dao.ResultsDao;
import alma.scheduling.algorithm.sbranking.SBRank;

/**
 *
 * @author dclarke
 * $Id: ResultsLogger.java,v 1.1 2011/10/14 23:00:41 dclarke Exp $
 */
public class ResultsLogger implements Runnable {
    	
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** The logger onto which we put our output */
	private Logger logger = null;

	/** The DAO via which we get the results */
	private ResultsDao dao = null;

	/** The name of the Array */
	private String arrayName = null;

	/** The name of the most recently used scheduling policy */
	private String currPolicy = null;

	/** The scheduling policy used the previous time. If null,
	 *  then this is the first run */
	private String prevPolicy = null;

	/** How many results to log. */
	private int n = 10;
	/* End Fields
	 * ============================================================= */

    	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * Create a ResultsLogger which will log the current top N 
	 * and what has happened to the previous top n.
	 * 
	 * @param dao
	 * @param thisPolicy
	 * @param lastPolicy
	 * @param n
	 */
	public ResultsLogger(Logger logger,
			             ResultsDao dao,
			             String arrayName,
			             String currPolicy,
			             String prevPolicy,
			             int n) {
		this.logger = logger;
		this.dao = dao;
		this.arrayName = arrayName;
		this.currPolicy = currPolicy;
		this.prevPolicy = prevPolicy;
		this.n = n;
	}

	/**
	 * Create a ResultsLogger which will log the current top N 
	 * doesn't look for prior results (useful for the first run
	 * of the DSA).
	 * 
	 * @param dao
	 * @param thisPolicy
	 * @param n
	 */
	public ResultsLogger(Logger logger,
			             ResultsDao dao,
			             String arrayName,
			             String currPolicy,
			             int n) {
		this(logger, dao, arrayName, currPolicy, null, n);
	}
	/* End Construction
	 * ============================================================= */


	/*
	 * ================================================================
	 * Implementation of Runnable interface
	 * ================================================================
	 */
	@Override
	public void run() {
		final Result currResult = dao.getCurrentResult(arrayName);
		final List<SBRank> currList = currResult.getScores();

		Collections.sort(currList, comparator());

		if (prevPolicy == null) {
			logCurrentScores(currList, currResult.getTime());
		} else {
			final Result prevResult = dao.getPreviousResult(arrayName);
			final List<SBRank> prevList = prevResult.getScores();
			Collections.sort(prevList, comparator());
			logCurrentScores(currList, currResult.getTime(),
					prevList, prevResult.getTime());
			logPreviousScores(currList, currResult.getTime(),
					prevList, prevResult.getTime());
		}
	}
	/* End Implementation of Runnable interface
	 * ============================================================= */


	/*
	 * ================================================================
	 * Sorting & utilities
	 * ================================================================
	 */
	private Comparator<SBRank> _comparator = null;

	/**
	 * Create a Comparator for SBRanks which will result in high
	 * scores being placed first.
	 * 
	 * @return
	 */
	private Comparator<SBRank> comparator() {
		if (_comparator == null) {
			_comparator = new Comparator<SBRank>(){

				@Override
				public int compare(SBRank o1, SBRank o2) {
					// We want higher scores first, so reverse
					// the natural ordering.
					final int first = o2.compareTo(o1);
					if (first != 0) {
						return first;
					}
					return o2.getUid().compareTo(o1.getUid());
				}
			};
		}
		return _comparator;
	}

	private int findRank(List<SBRank> list, String uid) {
		int rank = 0;
		for (final SBRank score : list) {
			rank ++; // Increment first so returned rank will be 1..n
			if (score.getUid().equals(uid)) {
				return rank;
			}
		}
		return 999999;
	}

	public String generateThreadName() {
		return String.format("ResultsLogger_%s_%TT",
				arrayName,
				new Date());
	}
	/* End Sorting & utilities
	 * ============================================================= */


	/*
	 * ================================================================
	 * The actual logging
	 * ================================================================
	 */
	final String[] CommonSuffixes = { "Ranker", "Scorer" };

	private String stripSuffix(String details) {
		for (final String suffix : CommonSuffixes) {
			int pos = details.lastIndexOf(suffix);
			if (pos > 0) {
				// > 0 to avoid the suffix being at the start of the String
				return details.substring(0, pos);
			}
		}
		return details;
	}

	private String format(double d) {
		return String.format("%8.5f", d);
	}

	private String formatScore(SBRank overall) {
		StringBuilder b = new StringBuilder();
		String sep = " (";

		b.append("Overall Score: ");
		b.append(format(overall.getRank()));

		List<SBRank> parts = overall.getBreakdownScore();
		for (SBRank part : parts) {
			b.append(sep);
			b.append(stripSuffix(part.getDetails()));
			b.append(format(part.getRank()));
			sep = ", ";
		}
		b.append(')');

		return b.toString();
	}

	private void logPreviousScores(List<SBRank> currList,
			Date         currWhen,
			List<SBRank> prevList,
			Date         prevWhen) {
		int prevLimit = Math.min(n, prevList.size());
		StringBuilder b = new StringBuilder();

		logger.info(String.format(
				"DSA results at %TT with policy %s, for SchedBlocks in the previous top %d (when policy was %s)",
				currWhen, currPolicy, prevLimit, prevPolicy));
		for (int p = 0; p < prevLimit; p++) {
			b.setLength(0);

			final SBRank prevRank = prevList.get(p);
			final int c = findRank(currList, prevRank.getUid());

			b.append("SchedBlock: ");
			b.append(prevRank.getUid());
			b.append(", Previous rank: ");
			b.append(p+1);
			if ((c >= 0) && (c < currList.size())) {
				// Found a current rank for it
				b.append(" (current rank: ");
				b.append(c);
				b.append("), ");
				b.append(formatScore(currList.get(c)));
			} else {
				// Although previously ranked highly, the SB failed to be selected this time
				b.append(" - not selected for ranking this time");
			}
			logger.info(b.toString());
		}
	}

	private void logCurrentScores(List<SBRank> currList,
			Date         currWhen,
			List<SBRank> prevList,
			Date         prevWhen) {
		int limit = Math.min(n, currList.size());
		StringBuilder b = new StringBuilder();

		logger.info(String.format(
				"DSA results at %TT with policy %s, top %d score%s",
				currWhen, currPolicy, limit, (limit==1)? "": "s"));
		for (int i = 0; i < limit; i++) {
			final SBRank rank = currList.get(i);
			b.setLength(0);
			b.append("SchedBlock: ");
			b.append(rank.getUid());
			b.append(", Rank: ");
			b.append(i+1);
			b.append(" (previous rank: ");
			b.append(findRank(prevList, rank.getUid()));
			b.append("), ");
			b.append(formatScore(rank));
			logger.info(b.toString());
		}
	}

	private void logCurrentScores(List<SBRank> currList, Date currWhen) {
		int limit = Math.min(n, currList.size());
		StringBuilder b = new StringBuilder();

		logger.info(String.format(
				"DSA results at %TT with policy %s, top %d score%s",
				currWhen, currPolicy, limit, (limit==1)? "": "s"));
		for (int i = 0; i < limit; i++) {
			final SBRank rank = currList.get(i);
			b.setLength(0);
			b.append("SchedBlock: ");
			b.append(rank.getUid());
			b.append(", Rank: ");
			b.append(i+1);
			b.append(", ");
			b.append(formatScore(rank));
			logger.info(b.toString());
		}
	}
	/* End The actual logging
	 * ============================================================= */
}
