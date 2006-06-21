package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.text.View;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import com.sun.java.swing.plaf.windows.WindowsIconFactory;

public class SchedTabUI extends BasicTabbedPaneUI {
    protected static final int BUTTONSIZE = 15;
    protected static final int WIDTHDELTA = 5;
    private BufferedImage img;
    private BufferedImage img1;
    private JButton closeB;
    private int overTabIndex = -1;
    private boolean mousePressed = false;
    private boolean OVER_CLOSE = false;
        
    public SchedTabUI(){
        super();
        img = new BufferedImage(BUTTONSIZE, BUTTONSIZE, BufferedImage.TYPE_4BYTE_ABGR);
        //img1 = new BufferedImage(BUTTONSIZE, BUTTONSIZE, BufferedImage.TYPE_4BYTE_ABGR);
        closeB = new JButton(WindowsIconFactory.createFrameCloseIcon());
        closeB.setSize(BUTTONSIZE-1, BUTTONSIZE-1);
        closeB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    System.out.println("Close pressed!");
                }
                });
        //WindowsIconFactory.createFrameCloseIcon().paintIcon(closeB,
          //  img.createGraphics(), 0, 0);
    }

        	
    //make room for the button on the tab.
    protected int calculateTabWidth(int tabPlacement, int tabIndex,
            FontMetrics metrics) {

        int delta = 2;
        delta += BUTTONSIZE + WIDTHDELTA;
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + delta;
    }
        	
    //Arrange the text so that its placed nicely on tab
    protected void layoutLabel(int tabPlacement, FontMetrics metrics,
            int tabIndex, String title, Icon icon, Rectangle tabRect,
            Rectangle iconRect, Rectangle textRect, boolean isSelected) {

        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
        View v = getTextViewForTab(tabIndex);
        SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics,
                title, icon, SwingUtilities.CENTER, SwingUtilities.LEFT,
                SwingUtilities.CENTER, SwingUtilities.CENTER, tabRect,
                iconRect, textRect, textIconGap);
        iconRect.x = tabRect.x + 8;
        textRect.x = iconRect.x + iconRect.width + textIconGap;
    }
            
    //paint the tab how we want it
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
            int tabIndex, Rectangle iconRect, Rectangle textRect)  {
        		
        Rectangle tabRect = rects[tabIndex];
        int selectedIndex = tabPane.getSelectedIndex();
        boolean isSelected = selectedIndex == tabIndex;
        if(isSelected){
            paintTabBackground(g, tabPlacement, tabIndex, tabRect.x, tabRect.y,
                tabRect.width, tabRect.height, isSelected); 
        }
        paintTabBorder(g, tabPlacement, tabIndex, tabRect.x, tabRect.y,
                tabRect.width, tabRect.height, isSelected);
            
        String title = tabPane.getTitleAt(tabIndex);
        Font font = tabPane.getFont();
        FontMetrics metrics = g.getFontMetrics(font);
        Icon icon = getIconForTab(tabIndex);
        layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect,

            iconRect, textRect, isSelected);

        paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect,
            isSelected);
        //paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);

        paintFocusIndicator(g, tabPlacement, rects, tabIndex, iconRect,
            textRect, isSelected);

        int dx = tabRect.x + tabRect.width - BUTTONSIZE - WIDTHDELTA;
        int dy = (tabRect.y + tabRect.height) / 2 - 6;
        if(!title.equals("Main")) {
            closeB.paint(img.getGraphics());    
            g.drawImage(img, dx-1, dy-1, null);
        }
    }

    private void validateLayout(){
        if (!tabPane.isValid()) {
            tabPane.validate();
        }
        if (!tabPane.isValid()) {
            TabbedPaneLayout layout = (TabbedPaneLayout) tabPane.getLayout();
            layout.calculateLayoutInfo();
        }
    }

    private int getTabAtLocation(int x, int y){
        validateLayout();
        int tabCount = tabPane.getTabCount();
        for(int i=0; i< tabCount; i++){
            if(rects[i].contains(x,y)){
                return i;
            }
        }
        return -1;
    }
    private boolean checkOverClose(int x, int y){
        if(overTabIndex != -1) {
            
            Rectangle closeRect = new  Rectangle (
                rects[overTabIndex].x+rects[overTabIndex].width 
                    - BUTTONSIZE - WIDTHDELTA,
                (rects[overTabIndex].y+rects[overTabIndex].height)/2 -6,
                BUTTONSIZE, BUTTONSIZE );
            if(closeRect.contains(x,y)){
                if(!tabPane.getTitleAt(overTabIndex).equals("Main")){
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return  false;
        }

    }
    protected void updateOverTab(int x, int y){
          overTabIndex = getTabAtLocation(x,y);  
          OVER_CLOSE = checkOverClose(x,y);
    }
    public int getOverTabIndex() {
        return overTabIndex;
    }

    protected MouseListener createMouseListener(){
        return new TabMouseHandler();
    }

    class TabMouseHandler extends MouseHandler{
        public TabMouseHandler() {
            super();
        }
        public void mousePressed(MouseEvent e){
            super.mousePressed(e);
            updateOverTab(e.getX(), e.getY());
        }
        public void mouseClicked(MouseEvent e){
            super.mouseClicked(e);
            updateOverTab(e.getX(), e.getY());
        }
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            if(OVER_CLOSE)
                ((MainSchedTabPane)tabPane).closeTabEvent(e, overTabIndex);
        }
    }
    
    class TabMouseListener implements MouseMotionListener {
        public void mouseMoved(MouseEvent e) {
            mousePressed = false;
            updateOverTab(e.getX(), e.getY());
        }
        public void mouseDragged(MouseEvent e) {
            mousePressed = true;
            updateOverTab(e.getX(), e.getY());
        }
    }
}

