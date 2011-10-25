package alma.scheduling.algorithm;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class PolicyValidatorTests extends TestCase {

	
	@Override
	protected void setUp() throws Exception {
	}

	public void testConvertPolicyString() throws TransformerException, SAXException, IOException {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria></SelectionCriteria>" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		String xmlContext = SchedulingPolicyValidator.convertPolicyString(xmlToLoad);
		ApplicationContext ctx = new GenericApplicationContext(new XmlBeanFactory(new ByteArrayResource(xmlContext.getBytes())));
	}
	
	public void testConvertPolicyStringValidationFailure() throws TransformerException, IOException {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria></SelectionCriteria>" +
				"<Scorers></SchedulingPolicy></Policies>";
		try {
		String xmlContext = SchedulingPolicyValidator.convertPolicyString(xmlToLoad);
		} catch (SAXException ex){
			System.out.println("Expected exception " + ex.getClass().getName());
		}
	}
}
