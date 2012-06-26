/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.archive;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import alma.archive.xml.ObsProjectEntity;
import alma.archive.xml.XmlEntity;
import alma.entity.xmlbinding.obsproject.ObsProject;

public class TestXmlTypes {

	/**
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException {
		XPathExpression expr = null;
		NamespaceContext ctx = new NamespaceContext() {

			@Override
			public Iterator getPrefixes(String namespaceURI) {
				return null;
			}

			@Override
			public String getPrefix(String namespaceURI) {
				return null;
			}

			@Override
			public String getNamespaceURI(String prefix) {
				String uri;
				if (prefix.equals("prp"))
					uri = "Alma/ObsPrep/ObsProposal";
				else if (prefix.equals("prj"))
					uri = "Alma/ObsPrep/ObsProject";
				else if (prefix.equals("sbl"))
					uri = "Alma/ObsPrep/SchedBlock";
				else
					uri = null;
				return uri;
			}
		};
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(ctx);
		try {
			expr = xpath
					.compile("/prp:ObsProposal/prj:ObsPlan[@status='Phase1Submitted']");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		Session session = new Configuration()
				.configure("alma/archive/hibernate.config.xml")
				.buildSessionFactory().openSession();
		Transaction t = session.beginTransaction();
		Query q = session.createQuery("from SchedBlockEntity as ope");
		Date startTime = new Date();
		List<XmlEntity> l = q.list();
		t.commit();
		session.close();

		for (XmlEntity e : l) {
			NodeList result = null;
			try {
				result = (NodeList) expr.evaluate(e.getXmlDoc(),
						XPathConstants.NODESET);
			} catch (XPathExpressionException ex) {
				ex.printStackTrace();
			}
			if (result != null && result.getLength() > 0) {
				System.out.println(e.getUid());
			}
		}
		Date endTime = new Date();
		System.out.println("List size: " + l.size());
		System.out.println("Time to extract entities from xmlstore: "
				+ (endTime.getTime() - startTime.getTime()) + " ms.");
//		try {
//			alma.entity.xmlbinding.obsproject.ObsProject op = (ObsProject) Unmarshaller
//					.unmarshal(
//							alma.entity.xmlbinding.obsproject.ObsProject.class,
//							l.get(0).getXmlDoc().getFirstChild());
//			System.out.println(op);
//		} catch (MarshalException e1) {
//			e1.printStackTrace();
//		} catch (ValidationException e1) {
//			e1.printStackTrace();
//		}
	}

	public static String getStringFromDoc(org.w3c.dom.Document doc) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) doc
				.getImplementation();
		LSSerializer lsSerializer = domImplementation.createLSSerializer();
		return lsSerializer.writeToString(doc);
	}

}
