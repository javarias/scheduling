package alma.scheduling.planning_mode_sim.gui.simpreparation;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;

import alma.scheduling.planning_mode_sim.dao.weatherSourcesDAO;

import com.toedter.calendar.JDateChooser;

public class SPDataSources extends JPanel{

	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JComboBox jComboBox1 = null;
	private JComboBox jComboBox2 = null;
	private JComboBox jComboBox3 = null;
	private JComboBox jComboBox4 = null;
	private JButton jButton1 = null;
	private JButton jButton2 = null;
	private JButton jButton3 = null;
	private JButton jButton4 = null;
	private JButton jButton5 = null;
	private JButton jButton6 = null;
	private JButton jButton7 = null;
	private JButton jButton8 = null;
	private JSeparator jSeparator0 = null;
	private JSeparator jSeparator1 = null;
	private JSeparator jSeparator2 = null;
	private JSeparator jSeparator3 = null;
	private JSeparator jSeparator4 = null;
	private JLabel jl0 = null;
	private JLabel jl1 = null;
	private JLabel jl2 = null;
	private JLabel jl3 = null;
	private JLabel jl4 = null;
	
	private JLabel FileNameJLabel = null;
	private JLabel SimStartDateJLabel= null;
	private JLabel SimEndDateJLabel = null;
	private JLabel CommentJLabel= null;
	private JTextField fileNameJTextField = null;
	private JDateChooser simStartJDateChooser = null;
	private JDateChooser simStopJDateChooser = null;
	private JTextArea commentJTextArea = null;

	/**
	 * This method initializes 
	 * 
	 */
	public SPDataSources() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		
		int row = 0;
		
		this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(330, 393));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
        GridBagConstraints cL = new GridBagConstraints();
        cL.insets = new Insets(4,4,4,4);
        cL.weightx = 0; cL.weighty = 0;
        cL.gridx = 0;
        
        GridBagConstraints cC = new GridBagConstraints();
        cC.insets = new Insets(4,4,4,4);
        cC.weightx = 1; cC.weighty = 0;
        cC.gridx = 1; 
        cC.gridwidth = 2;
        cC.fill = GridBagConstraints.HORIZONTAL;
        cC.anchor = GridBagConstraints.LINE_END;
        
        GridBagConstraints cB1 = new GridBagConstraints();
        cB1.insets = new Insets(4,4,4,4);
        cB1.weightx = 0; cB1.weighty = 0;
        cB1.gridx = 1;
        cB1.anchor = GridBagConstraints.LINE_END;
        
        GridBagConstraints cB2 = new GridBagConstraints();
        cB2.insets = new Insets(4,4,4,4);
        cB2.weightx = 0; cB2.weighty = 0;
        cB2.gridx = 2;
        cB2.anchor = GridBagConstraints.LINE_END;
        
        GridBagConstraints cSep = new GridBagConstraints();
        cSep.insets = new Insets(24,4,4,4);
        cSep.weightx = 1; cSep.weighty = 0;
        cSep.gridx = 0;
        cSep.gridwidth = 3;
        cSep.fill = GridBagConstraints.HORIZONTAL;
        
        GridBagConstraints cLSep = new GridBagConstraints();
        cLSep.insets = new Insets(24,4,4,4);
        cLSep.weightx = 0; cLSep.weighty = 0;
        cLSep.gridx = 0;
        cLSep.anchor = GridBagConstraints.LINE_START;
        
        jl0 = new JLabel("<html><font color='#285fc4'><b>Simulation </b></font></html>");
        jl0.setOpaque(true);
        jSeparator0 = new JSeparator(SwingConstants.HORIZONTAL);
        FileNameJLabel = new JLabel("Filename: ");
        CommentJLabel = new JLabel("Comment: ");
        SimStartDateJLabel = new JLabel("Start Date");
        SimEndDateJLabel = new JLabel("Stop Date");
        fileNameJTextField = new JTextField();
        fileNameJTextField.setEnabled(false);
        commentJTextArea = new JTextArea();
        commentJTextArea.setLineWrap(true);
        commentJTextArea.setWrapStyleWord(true);

        simStartJDateChooser = new JDateChooser();
        simStopJDateChooser = new JDateChooser();
        
        jl1 = new JLabel("<html><font color='#285fc4'><b>Observatory  </b></font></html>");
        jl1.setOpaque(true);
        jSeparator1 = new JSeparator(SwingConstants.HORIZONTAL);        
        jLabel1 = new JLabel("Data Source:");
        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jButton1 = new JButton("Modify...");
        jButton2 = new JButton("Preview...");
        
        jl2 = new JLabel("<html><font color='#285fc4'><b>Array Configuration  </b></font></html>");
        jl2.setOpaque(true);
        jSeparator2 = new JSeparator(SwingConstants.HORIZONTAL);
        jLabel2 = new JLabel("Data Source:");
        jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        jButton3 = new JButton("Modify...");
        jButton4 = new JButton("Preview...");
        
        jl3 = new JLabel("<html><font color='#285fc4'><b>Observation Projects  </b></font></html>");
        jl3.setOpaque(true);
        jSeparator3 = new JSeparator(SwingConstants.HORIZONTAL);
        jLabel3 = new JLabel("Data Source:");
        jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        jButton5 = new JButton("Modify...");
        jButton6 = new JButton("Preview...");
        
        jl4 = new JLabel("<html><font color='#285fc4'><b>Weather  </b></font></html>");
        jl4.setOpaque(true);
        jSeparator4 = new JSeparator(SwingConstants.HORIZONTAL);
        jLabel4 = new JLabel("Data Source:");
        jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        jButton7 = new JButton("Modify...");
        jButton8 = new JButton("Preview...");
        
        cLSep.gridy = row; this.add(jl0, cLSep);
        cSep.gridy = row; this.add(jSeparator0, cSep);
        cL.gridy = ++row; this.add(FileNameJLabel, cL);
        cC.gridy = row; this.add(fileNameJTextField, cC);
        cL.gridy = ++row; this.add(CommentJLabel, cL);
        cC.gridy = row; this.add(commentJTextArea, cC);
        cL.gridy = ++row; this.add(SimStartDateJLabel, cL);
        cC.gridy = row; this.add(simStartJDateChooser, cC);
        cL.gridy = ++row; this.add(SimEndDateJLabel, cL);
        cC.gridy = row; this.add(simStopJDateChooser, cC);
            
        cLSep.gridy = ++row; this.add(jl1, cLSep); 
        cSep.gridy = row; this.add(jSeparator1, cSep);
        cL.gridy = ++row; this.add(jLabel1, cL);
        cB1.gridy = ++row; this.add(jButton1, cB1);
        cB2.gridy = row; this.add(jButton2, cB2);
        
        cLSep.gridy = ++row; this.add(jl2, cLSep);
        cSep.gridy = row; this.add(jSeparator2, cSep);
        cL.gridy = ++row; this.add(jLabel2, cL);
        cB1.gridy = ++row; this.add(jButton3, cB1);
        cB2.gridy = row; this.add(jButton4, cB2);
        
        cLSep.gridy = ++row; this.add(jl3, cLSep);
        cSep.gridy = row; this.add(jSeparator3, cSep);
        cL.gridy = ++row; this.add(jLabel3, cL);
        cC.gridy = row; this.add(getJComboBox3(), cC);
        cB1.gridy = ++row; this.add(jButton5, cB1);
        cB2.gridy = row; this.add(jButton6, cB2);
        
        cLSep.gridy = ++row; this.add(jl4, cLSep);
        cSep.gridy = row; this.add(jSeparator4, cSep);
        cL.gridy = ++row; this.add(jLabel4, cL);  
        cB1.gridy = ++row; this.add(jButton7, cB1);
        cB2.gridy = row; this.add(jButton8, cB2);
			
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox3() {
		if (jComboBox3 == null) {
			jComboBox3 = new JComboBox();
			for( String s: weatherSourcesDAO.getSourcesNames() ){
				jComboBox3.addItem(s);
			}
		}
		
		return jComboBox3;
	}

}  //  @jve:decl-index=0:visual-constraint="186,88"
