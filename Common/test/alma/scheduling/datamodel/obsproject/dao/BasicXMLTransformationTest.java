package alma.scheduling.datamodel.obsproject.dao;

import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.w3c.dom.Document;

import alma.archive.xml.ObsProposalEntity;
import alma.entity.xmlbinding.obsproposal.ObsProposal;

public class BasicXMLTransformationTest extends MockObjectTestCase {

	public void testProblem() throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new File("test/problematic_obs_proposal.xml"));
		ObsProposalEntity op =  new ObsProposalEntity();
		op.setXmlDoc(doc);
		ObsProposal.unmarshalObsProposal(new StringReader(op.domToString()));
	}
}
