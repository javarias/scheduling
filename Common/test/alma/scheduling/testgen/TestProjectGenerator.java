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

package alma.scheduling.testgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import alma.scheduling.SchedulingException;

/**
 * The ugliest class in the world. Generates scripts to convert
 * template projects into masses of real projects.
 * 
 * @author dclarke
 * $Id: TestProjectGenerator.java,v 1.1 2011/07/29 15:52:46 dclarke Exp $
 */
public class TestProjectGenerator {

	private enum Band {
		ALMA_RB_01( 31.3, 45),
		ALMA_RB_02( 67,   90),
		ALMA_RB_03( 84,  116),
		ALMA_RB_04(125,  163),
		ALMA_RB_05(163,  211),
		ALMA_RB_06(211,  275),
		ALMA_RB_07(275,  373),
		ALMA_RB_08(385,  500),
		ALMA_RB_09(602,  720),
		ALMA_RB_10(787,  950);
		
		public double min;
		public double max;
		
		Band (double min, double max) {
			this.min = min;
			this.max = max;
		}
		
		boolean contains(double ghz) {
			return (min <= ghz) && (ghz <= max);
		}
	}

	private enum Option {
		NAME("-name", "-n", false),
		TEMPLATE("-template", "-t", false),
		SCRIPT("-script", "-s", false),
		RA("-ra", "-r", true),
		DEC("-dec", "-d", true),
		MINHA("-minha", "-mi", true),
		MAXHA("-maxha", "-ma", true),
		FREQUENCY("-freq", "-f", true),
		GRADE("-grade", "-g", true),
		EXECUTIVE("-exec", "-e", true),
		HELP("-help", "-h", false);
		
		public String minimum;
		public String full;
		public boolean compulsory;
		
		Option (String full, String min, boolean compulsory) {
			this.full       = full;
			this.minimum    = min;
			this.compulsory = compulsory;
		}
	}

	private enum Executive {
		CL("chile", "cl"),
		EA("eastasia", "ea"),
		EU("europe", "eu"),
		NA("northamerica", "na"),
		OT("other", "ot");
		
		
		public String full;
		public String abbr;
		
		Executive (String full, String abbr) {
			this.full = full;
			this.abbr = abbr;
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			inner("-name", "Various_RA_Dec_%03d",
					"-template", "R81Template",
					"-script",   "Various_RA_Dec.script",
					"-ra", "0:359:45",
					"-dec", "60,35:-70:-35,-85",
					"-minHA", "0",
					"-maxHA", "24",
					"-freq", "100",
					"-grade", "B",
					"-exec", "cl");
			inner("-name", "Various_HA_%03d",
					"-template", "R81Template",
					"-script",   "Various_HA.script",
					"-ra", "0",
					"-dec", "-23",
					"-minHA", "0:6,18:23",
					"-maxHA", "1:6,18:24",
					"-freq", "100.0",
					"-grade", "B",
					"-exec", "na");
			inner("-name", "Various_Freq_Grade_%03d",
					"-template", "R81Template",
					"-script",   "Various_Freq_Grade.script",
					"-ra", "90",
					"-dec", "-65,-75,-85",
					"-minHA", "0",
					"-maxHA", "24",
					"-freq", "38.15,78.5,100,144,187,243,324,442.5,661,868.5",
					"-grade", "abc",
					"-exec", "eu");
		} else {
			inner(args);
		}
	}
	
	/**
	 * @param args
	 */
	private static void inner(String... args) {
		final Set<Option> got = new HashSet<Option>();
		final RangeParser rp = new RangeParser();
		
		String name     = "test%04d";
		String template = "R81Template";
		String script   = "scriptFile";
		Collection<Double> ras = null;
		Collection<Double> decs = null;
		Collection<Double> minHAs = null;
		Collection<Double> maxHAs = null;
		Collection<Double> freqs = null;
		Collection<Character> grades = null;
		Executive executive = Executive.CL;
		
		int ap = 0;
		while (ap < args.length) {
			final Option option = findOption(args[ap].toLowerCase());
			if (option == null) {
				bomb("Unknown option %s", args[ap]);
			}
			if (got.contains(option)) {
				bomb("Duplicate option %s", args[ap]);
			}
			got.add(option);
			try {
				switch (option) {
				case HELP:
					usage(System.out);
					System.exit(0);
					break;
				case NAME:      name     = args[++ap];                     break;
				case TEMPLATE:  template = args[++ap];                     break;
				case SCRIPT  :  script   = args[++ap];                     break;
				case RA:        ras      = rp.expandedDoubles(args[++ap]); break;
				case DEC:       decs     = rp.expandedDoubles(args[++ap]); break;
				case MINHA:     minHAs   = rp.expandedDoubles(args[++ap]); break;
				case MAXHA:     maxHAs   = rp.expandedDoubles(args[++ap]); break;
				case FREQUENCY: freqs    = rp.expandedDoubles(args[++ap]); break;
				case GRADE:     grades   = rp.expandedChars(args[++ap]);   break;
				case EXECUTIVE:
					executive = findExecutive(args[++ap]);
					if (executive == null) {
						bomb("Unknown executive %s", args[ap]);
					}
					break;
				default:
					bomb("Unhandled option %s", option);
					break;
				}
			} catch (IndexOutOfBoundsException e) {
				bomb("Missing argument for %s option", args[ap-1]);
			} catch (SchedulingException e) {
				bomb(e);
			}
			ap ++;
		}
		for (final Option option : Option.values()) {
			if (option.compulsory && !got.contains(option)) {
				bomb("Missing option %s", option.full);
			}
		}
		
		checkValid("RA", 0, 360, ras);
		checkValid("DEC", -90, 90, decs);
		checkValid("HA", 0, 24, minHAs);
		checkValid("HA", 0, 24, maxHAs);
		checkFrequencies(freqs);
		checkValid("grades", 'A', 'D', grades);
		
		int numProjects = 0;
		
		File file = new File(script);
		PrintStream stream = null;
		try {
			stream = new PrintStream(file);
		} catch (FileNotFoundException e) {
			bomb(e);
		}
		
		for (final double minHA : minHAs) {			
			for (final double maxHA : maxHAs) {
				if (minHA < maxHA) {
					for (final double ra : ras) {			
						for (final double dec : decs) {			
							for (final double freq : freqs) {			
								for (final char grade : grades) {
									generateProject(
											stream,
											String.format(name, ++numProjects),
											template,
											ra, dec, minHA, maxHA, freq, grade,
											executive);
								}
							}
						}
					}
				} else {
					System.out.format("Skipping minHA %f, maxHA %f as the minHA is greater than the maxHA%n",
							minHA, maxHA);
				}
			}
		}
		
		stream.close();
	}
	
	private static void generateProject(PrintStream scriptFile,
			                            String    name,
			                            String    template,
			                            double    ra,
			                            double    dec,
			                            double    minHA,
			                            double    maxHA,
			                            double    freq,
			                            char      grade,
			                            Executive exec) {
		final Band band = findBand(freq);
		final String note = String.format("RA %f deg, Dec %f deg, minHA %f, maxHA %f, freq %f GHz (%s), grade %c",
				ra, dec, minHA, maxHA, freq, band, grade);
		System.out.format("%s from %s: %s%n", name, template, note);
		final Map<String, String> values = new HashMap<String, String>();
		
		values.put("_NAME_",  name);
		values.put("_NOTE_",  note);
		values.put("_GRADE_", String.valueOf(grade));
		values.put("_FREQ_",  String.valueOf(freq));
		values.put("_BAND_",  band.toString());
		values.put("_RA_",    String.valueOf(ra));
		values.put("_DEC_",   String.valueOf(dec));
		values.put("_CLFRAC_", (exec==Executive.CL)? "1.0": "0.0");
		values.put("_EAFRAC_", (exec==Executive.EA)? "1.0": "0.0");
		values.put("_EUFRAC_", (exec==Executive.EU)? "1.0": "0.0");
		values.put("_NAFRAC_", (exec==Executive.NA)? "1.0": "0.0");
		values.put("_OTFRAC_", (exec==Executive.OT)? "1.0": "0.0");
		
		final String scripts = createScript(values);
		
		final String files[] = {"ObsProject.xml",
				                "ObsProposal.xml",
				                "SchedBlock0.xml"};
		
		scriptFile.format("mkdir %s%n", name);
		for (final String file : files) {
			scriptFile.format("sed %s %s/%s > %s/%s%n",
					scripts, template, file, name, file);
		}
		scriptFile.format("zip -Drj %s.aot %s%n", name, name);
		scriptFile.format("rm -rf %s%n", name);
		
	}

	private static String createScript(Map<String, String> values) {
		final StringBuilder sb = new StringBuilder();
		final Formatter     f  = new Formatter(sb);

		for (final String keyword : values.keySet()) {
			final String value = values.get(keyword);
			f.format(" -e 's/%s/%s/g'", keyword, value);
		}
		return sb.toString();
	}

	private static void checkValid(String label, char min, char max,
			Collection<Character> chars) {
		for (final char c : chars) {
			if (c < min || c > max) {
				bomb("%s %c out of range [%c:%c]",
						label, c, min, max);
			}
		}
	}

	private static void checkValid(String label, double min, double max,
			Collection<Double> doubles) {
		for (final double d : doubles) {
			if (d < min || d > max) {
				bomb("%s %f out of range [%f:%f]",
						label, d, min, max);
			}
		}
	}

	private static Band findBand(double ghz) {
		for (final Band b : Band.values()) {
			if (b.contains(ghz)) {
				return b;
			}
		}
		return null;
	}

	private static void checkFrequencies(Collection<Double> doubles) {
		for (final double d : doubles) {
			final Band b = findBand(d);
			if (b == null) {
				bomb("invalid frequency %f GHz", d);
			}
		}
	}

	private static void usage(PrintStream str) {
		str.format("Options are:%n");
		for (final Option option : Option.values()) {
			str.format("   %s%n", option.full);
		}
	}
	
	private static void bomb(Throwable t) {
		t.printStackTrace(System.err);
		System.err.println();
		usage(System.err);
		System.exit(-1);
	}
	
	private static void bomb(String format, Object... args) {
		System.err.format(format, args);
		System.err.println();
		System.err.println();
		usage(System.err);
		System.exit(-1);
	}

	private static Option findOption(String string) {
		for (final Option option : Option.values()) {
			if (string.startsWith(option.minimum)) {
				return option;
			}
		}
		return null;
	}

	private static Executive findExecutive(String string) {
		for (final Executive exec : Executive.values()) {
			if (string.equals(exec.full) || string.equals(exec.abbr)) {
				return exec;
			}
		}
		return null;
	}

}
