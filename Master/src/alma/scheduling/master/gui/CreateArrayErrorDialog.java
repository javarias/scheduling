package alma.scheduling.master.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;


public class CreateArrayErrorDialog extends JDialog implements  PropertyChangeListener, ClipboardOwner{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4077550045164302300L;
	private final JPanel contentPanel = new JPanel();
	private JOptionPane headerPane;
	private String[] options = {"OK"};
	private JTextArea exceptionTextArea;

	/**
	 * Test it
	 */
	public static void main(String[] args) {
		String header = "Make sure these antennas are really available to "+
				"create this array.\nAlso check state of Control "+
				"System and its logs.\n\n";
		JFrame frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		try {
			CreateArrayErrorDialog dialog = new CreateArrayErrorDialog(frame, "Error Creating Array", header, "Error trace line 1 \n line 2 \n line 3 \n line 4 \n line 5 \n line 6 \n line 7 \n line8 \n line 9 \n line 10" +
		"Error trace line 1 \n line 2 \n line 3 \n line 4 \n line 5 \n line 6 \n line 7 \n line8 \n line 9 \n line 10");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public CreateArrayErrorDialog(Component parentComponent , String title, String header, String acsErrorTrace) {
		super(lookForRootWindow(parentComponent), title, ModalityType.APPLICATION_MODAL);
		this.setBounds(getCalculatedBounds(lookForRootWindow(parentComponent)));
		this.setTitle(title);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.SOUTH);
		{
			headerPane = new JOptionPane(convertTextToHtml(header), JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options);
//			JLabel headerLabel = new JLabel(convertTextToHtml(header));
			getContentPane().add(headerPane, BorderLayout.NORTH);
			exceptionTextArea = new JTextArea(acsErrorTrace);
			exceptionTextArea.setEditable(false);
			exceptionTextArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			getContentPane().add(new JScrollPane(exceptionTextArea), BorderLayout.CENTER);
			JPanel buttonPane = new JPanel();
			
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton copyButton = new JButton("Copy error trace to clipboard");
				buttonPane.add(copyButton);
				copyButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StringSelection stringSel = new StringSelection(exceptionTextArea.getText());
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(stringSel, CreateArrayErrorDialog.this);
					}
				});
			}
		}
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				headerPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
			
		});
		
		headerPane.addPropertyChangeListener(this);
	}
	
	public CreateArrayErrorDialog( String title, String header, String acsErrorTrace ) {
		this(JOptionPane.getRootFrame(), title, header, acsErrorTrace);
	}

	private static String convertTextToHtml(String input) {
		StringBuilder retval = new StringBuilder("<html>");
		retval.append(input.replace("\n", "<br/>"));
		retval.append("</html>");
		return retval.toString();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propName =  evt.getPropertyName();
		
		if (isVisible() && evt.getSource() == headerPane)
		if (JOptionPane.VALUE_PROPERTY.equals(propName)) {
			setVisible(false);
			dispose();
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		//do nothing
	}
	
	private Rectangle getCalculatedBounds(Window parent) {
		Rectangle retval = new java.awt.Rectangle();
		retval.width = 450;
		retval.height = 300;
		retval.x = parent.getBounds().width / 2 - retval.width / 2 + parent.getBounds().x;
		retval.y = parent.getBounds().height / 2 - retval.height / 2 + parent.getBounds().y;
		return retval;
	}
	
	private static Window lookForRootWindow(Component parentComponent) {
		//Look for the root window
		Component parentParentComponent = null;
		if(parentComponent != null) {
			parentParentComponent = parentComponent;
			do {
				if(parentParentComponent instanceof Frame || parentParentComponent instanceof Dialog)
					break;
				parentParentComponent = parentParentComponent.getParent();
			} while (parentParentComponent != null); 
		}
		if (parentParentComponent == null)
			parentParentComponent = JOptionPane.getRootFrame();
		
		return (Window) parentParentComponent;
	}

}
