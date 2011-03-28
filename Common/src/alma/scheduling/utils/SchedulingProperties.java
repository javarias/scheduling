/**
 * 
 */
package alma.scheduling.utils;

/**
 * @author dclarke
 *
 */
public abstract class SchedulingProperties {

	private static String PROP_autoPopupPlugin      = "scheduling.autoPopupPlugin";
	private static String PROP_convertPhase2ToReady = "scheduling.convertPhase2ToReady";
	
	public static String autoPopupPluginPropertyName() {
		return PROP_autoPopupPlugin;
	}
	
	public static boolean isAutoPopupArrayPlugin() {
		final String env = System.getProperty(PROP_autoPopupPlugin);
		return env != null;
	}
	
	public static String convertPhase2ToReadyPropertyName() {
		return PROP_convertPhase2ToReady;
	}
	
	public static boolean isConvertPhase2ToReady() {
		final String env = System.getProperty(PROP_convertPhase2ToReady);
		return env != null;
	}
}
