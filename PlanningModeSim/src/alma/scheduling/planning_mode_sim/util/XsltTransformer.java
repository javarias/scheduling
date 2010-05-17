package alma.scheduling.planning_mode_sim.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XsltTransformer {
	
	static void transform(String xslInURI, String xmlInURI, String htmlOutURI){
		// Instantiate the TransformerFactory, and use it with a StreamSource
		// XSL stylesheet to create a translet as a Templates object.
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;
		Templates translet;
		try {
			translet = tFactory.newTemplates(new StreamSource(xslInURI));
			// For each thread, instantiate a new Transformer, and perform the
			// transformations on that thread from a StreamSource to a StreamResult;
			transformer = translet.newTransformer();
			transformer.transform(new StreamSource(xmlInURI), new StreamResult(new FileOutputStream(htmlOutURI)));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
