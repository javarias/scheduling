package alma.scheduling.algorithm;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;



import org.springframework.context.support.AbstractApplicationContext;
import org.xml.sax.SAXException;

import alma.scheduling.utils.DSAContextFactory;

public class SchedulingPolicyValidator {

	private URL schemaURL = getClass().getClassLoader().getResource("alma/scheduling/algorithm/SchedulingPolicy.xsd");
	private URL xslURL = getClass().getClassLoader().getResource("alma/scheduling/algorithm/SchedulingPolicy.xsl");
	private SchemaFactory sFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
	private TransformerFactory tFactory = TransformerFactory.newInstance();
	
	/**
	 * @param args: The Scheduling Policy files to be validated with this tool
	 * 
	 */
	public static void main(String[] args) {
		SchedulingPolicyValidator validator = new SchedulingPolicyValidator();
		if(args.length > 0) {
			for(String file: args){
				if (validator.validate(file)){
					String outFile = validator.convert(file);
					if(outFile != null)
						validator.loadContext(outFile);
				}
			}
		}
		else {
			help();
		}
	}
	
	private void loadContext(String file) {
		System.out.println("Loading DSA application context file ... ");
		AbstractApplicationContext context = DSAContextFactory.getContext(file);
		System.out.println("Available Algorithm Policies: ");
		for (String policyName: DSAContextFactory.getPolicyNames()) {
			System.out.println("   * " + policyName);
			context.getBean(policyName);
		}
		
	}

	private String convert(String file) {
		System.out.print("Generating DSA application context file" + file +" ... ");
		try {
			Transformer transformer = tFactory.newTransformer(new StreamSource(xslURL.toString()));
			transformer.transform(new StreamSource(file), new StreamResult(file +".context.xml"));
			System.out.println("	SUCCESS. Output file: " + file + ".context.xml");
			return file + ".context.xml";
		} catch (TransformerConfigurationException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
			return null;
		} catch (TransformerException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
			return null;
		}
	}

	private boolean validate(String file) {
		System.out.print("Validating " + file +" ... ");
		try {
			Schema schema = sFactory.newSchema(schemaURL);
			Validator validator = schema.newValidator();
			Source source = new StreamSource(file);
			validator.validate(source);
			System.out.println("SUCCESS.");
			return true;
		} catch (SAXException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
		}
		return false;
	}

	private static void help() {
		System.out.println("Usage: "
				+ SchedulingPolicyValidator.class.toString() + " <FILE>...");
		System.out
				.println("This tool must be used for validation and conversion ");
		System.out.println("of the scheduling policy files.");
		System.out
				.println("This tool will not concatenate the file received as parameter");
	}

}
