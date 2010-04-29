package alma.scheduling.planning_mode_sim.cli;

public class Console {

    private java.io.Console systemConsole = null;
    private static final String prompt ="> ";
    private static Console console = null;
    private static boolean requestedExit = false;
    private AprcTool aprc = null;
    
    private Console(AprcTool aprc){
        systemConsole = System.console();
        this.aprc = aprc;
    }
    
    public static Console getConsole(AprcTool aprc){
        if(console == null)
            return new Console(aprc);
        return console;
    }
    
    public void activate(){
        requestedExit = false;
        while(!requestedExit){
            systemConsole.printf(prompt, new Object[0]);
            interpret(systemConsole.readLine());
        }
    }
    
    private void interpret(String line){
        String[] lineParams = line.split(" ");
        if(lineParams[0].equals("exit"))
            System.exit(0);
        else if (lineParams[0].equals("step")){
            aprc.toBeInterrupted = true;
            requestedExit = true;
        }
        else if(lineParams[0].equals("run")){
            aprc.toBeInterrupted = false;
            requestedExit = true;
        }
    }
 
    public static void main(String[] args){
        Console console= Console.getConsole(null);
        console.activate();
    }
}
