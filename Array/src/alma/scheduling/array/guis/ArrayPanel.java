package alma.scheduling.array.guis;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArraySchedulerMode;

/**
 * This class is the main panel shown by the OMC. It contains a
 * {@code CurrentActivityPanel} and a {@code InteractivePanel}
 * 
 * @author Jorge Avarias <javarias[at]nrao.edu>
 *
 */
public class ArrayPanel extends AbstractArrayPanel {

	private static final long serialVersionUID = -7832483714456316810L;
	private CurrentActivityPanel cup = null;
	private InteractivePanel ip = null;
	private JSplitPane splitPane = null;
	private String arrayMode = null;
	private JLabel title;
	
	
	public ArrayPanel(String arrayName) {
		super(arrayName);
		System.out.format("%s (ArrayPanel).ArrayPanel(%s)%n",
				this.getClass().getSimpleName(),
				arrayName);
		this.arrayMode = "";
		this.setLayout(new BorderLayout());
		initialize();
	}
	
	private void initialize(){
		cup = new CurrentActivityPanel(this.arrayName);
		ip = new InteractivePanel(this.arrayName);
		
		JPanel left = new JPanel(new BorderLayout());
		JPanel right = new JPanel(new BorderLayout());
		right.add(cup, BorderLayout.CENTER);
		left.add(ip, BorderLayout.CENTER);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(450);
		cup.setSize(400, 800);
		ip.setSize(400, 800);
		this.add(splitPane, BorderLayout.CENTER);
		
		title = new JLabel();
		setTitle(arrayName, "Initializing");
		this.add(title, BorderLayout.NORTH);
	}
	
	private void setTitle(String arrayName, String arrayMode) {
		title.setText(String.format(
				"<html><font size=\"5\" color=%s>%s %s</font></html>",
				this.TitleColour,
				arrayMode,
				arrayName
		));
	}


	/* (non-Javadoc)
	 * @see alma.exec.extension.subsystemplugin.SubsystemPlugin#setServices(alma.exec.extension.subsystemplugin.PluginContainerServices)
	 */
	@Override
	public void setServices(PluginContainerServices services) {
		System.out.format("%s.setServices, arrayName = %s%n",
				this.getClass().getSimpleName(),
				services.getSessionProperties().getArrayName());
		super.setServices(services);
		cup.setServices(services);
		ip.setServices(services);
	}

	private void setModesInTitle(final ArraySchedulerMode[] modes) {
		final String suffix = "_I";
		final StringBuilder b = new StringBuilder();
		String sep = "";
		for (final ArraySchedulerMode mode : modes) {
			String m = mode.toString();
			final int right = m.lastIndexOf(suffix);
			if (right > 0) {
				m = m.substring(0, right);
			}
			b.append(sep);
			b.append(m.charAt(0));
			b.append(m.substring(1).toLowerCase());
			sep = ", ";
		}
		this.arrayMode = b.toString();
		setTitle(this.arrayName, this.arrayMode);
	}
	
	@Override
	protected void arrayAvailable() {
		System.out.format("%s (ArrayPanel).arrayAvailable() - %s is %s @ %h%n",
				this.getClass().getSimpleName(),
				arrayName,
				array.getClass().getSimpleName(),
				array.hashCode());
		setModesInTitle(array.getModes());
		cup.setArray(array);
		cup.arrayAvailable();
		ip.setArray(array);
		ip.arrayAvailable();
	}

	@Override
	protected void modelsAvailable() {
		cup.setModelAccessor(models);
		cup.modelsAvailable();
		ip.setModelAccessor(models);
		ip.modelsAvailable();
	}
	
	private static ArrayPanel createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Array Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        ArrayPanel panel = new ArrayPanel("Array");
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
        return panel;
    }
	
	public static void main(String[] args) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
    			ArrayPanel p = createAndShowGUI();
    			try {
    				p.runRestricted(false);
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	});
    }

}
