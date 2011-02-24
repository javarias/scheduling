/**
 * 
 */
package alma.scheduling.utils;

/**
 * @author dclarke
 *
 */
public abstract class SchedulingProperties {

	private static String PROP_autoPopupPlugin = "scheduling.autoPopupPlugin";
	
	public static boolean isAutoPopupArrayPlugin() {
		final String env = System.getProperty(PROP_autoPopupPlugin);
		return env != null;
	}
}
