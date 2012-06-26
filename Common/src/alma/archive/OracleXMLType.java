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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class OracleXMLType implements UserType, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8763427481619440266L;
	private static final Class returnedClass = Document.class;
	private static final int[] SQL_TYPES = new int[] { oracle.xdb.XMLType._SQL_TYPECODE };

	@Override
	public Object assemble(Serializable _cached, Object arg1)
			throws HibernateException {
		try {
			return OracleXMLType.stringToDom((String) _cached);
		} catch (Exception e) {
			throw new HibernateException("Could not assemble String to Document", e);
		} 
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
        if (value == null)
            return null;
 
        return (Document) ((Document) value).cloneNode(true);
	}

	@Override
	public Serializable disassemble(Object _obj) throws HibernateException {
		try {
			return OracleXMLType.domToString((Document) _obj);
		} catch (TransformerException e) {
			throw new HibernateException("Could not disassemble Document to serializable", e);
		}
	}

	@Override
	public boolean equals(Object arg0, Object arg1) throws HibernateException {
        if (arg0 == null && arg1 == null)
            return true;
        else if (arg0 == null && arg1 != null)
            return false;
        else
            return arg0.equals(arg1);
	}

	@Override
	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object arg2)
            throws HibernateException, SQLException {
        XMLType xmlType = null;
        Document doc = null;
        try {
            OPAQUE op = null;
            OracleResultSet ors = null;
            if (rs instanceof OracleResultSet) {
                ors = (OracleResultSet) rs;
            } else {
                throw new UnsupportedOperationException(
                        "ResultSet needs to be of type OracleResultSet");
            }
            op = ors.getOPAQUE(names[0]);
            if (op != null) {
                xmlType = XMLType.createXML(op);
            }
            doc = xmlType.getDOM();
        } finally {
            if (null != xmlType) {
                xmlType.close();
            }
        }
        return doc;
    }

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		try {
            XMLType xmlType = null;
            if (value != null) {
                xmlType = XMLType.createXML(st.getConnection(),
                        OracleXMLType.domToString((Document) value));
            } else {
                xmlType = XMLType.createXML(st.getConnection(), "");
            }
 
            ((OraclePreparedStatement) st).setObject(index, xmlType);
        } catch (Exception e) {
            throw new SQLException(
                    "Could not convert Document to String for storage");
        }
    }

	@Override
	public Object replace(Object _orig, Object _tar, Object _owner)
			throws HibernateException {
		return deepCopy(_orig);
	}

	@Override
	public Class returnedClass() {
		return returnedClass;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	protected static String domToString(Document _document)
            throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(_document);
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();
    }
 
    protected static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                xmlSource.getBytes("UTF-8"));
        Document document = builder.parse(inputStream);
        return document;
    }
}
