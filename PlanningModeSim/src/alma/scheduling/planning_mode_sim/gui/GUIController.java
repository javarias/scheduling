package alma.scheduling.planning_mode_sim.gui;

import java.net.URL;


public class GUIController implements Runnable {
    private GUI gui;

    public GUIController() {
    }
    public void run() {
        this.gui = new GUI(this);
    }
    protected URL getImage(String name) {
        return this.getClass().getClassLoader().getResource(
            "alma/scheduling/image/"+name);
    }
    public static void main(String args[]) {
        GUIController controller = new GUIController();
        Thread t = new Thread(controller);
        t.start();
    }
}
