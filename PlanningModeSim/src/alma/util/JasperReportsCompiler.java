package alma.util;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

public class JasperReportsCompiler {

	public static void main(String[] args){
		if(args.length == 0)
			throw new IllegalArgumentException("You must specify at least one report");
		for(int i = 0 ; i < args.length; i++)
			compile(args[i]);
	}
	
	private static void compile(String path){
		try {
			String output = path;
			output = output.substring(0, output.length() - 5) + "jasper";
			JasperCompileManager.compileReportToFile(path, output);
		} catch (JRException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
