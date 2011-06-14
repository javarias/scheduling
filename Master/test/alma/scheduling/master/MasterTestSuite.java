package alma.scheduling.master;

import alma.scheduling.master.compimpl.MasterSchedulerUnitTests;
import junit.framework.TestSuite;

public class MasterTestSuite {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MasterSchedulerUnitTests.class);
		junit.textui.TestRunner.run(suite);
	}

}
