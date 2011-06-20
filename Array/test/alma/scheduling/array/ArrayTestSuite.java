package alma.scheduling.array;

import alma.scheduling.array.executor.ExecutorUnitTests;
import junit.framework.TestSuite;

public class ArrayTestSuite {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ExecutorUnitTests.class);
		junit.textui.TestRunner.run(suite);
		System.exit(0);
	}

}
