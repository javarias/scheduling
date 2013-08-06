package alma.scheduling.psm.web.timeline;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import alma.scheduling.psm.sim.TimeEvent;


public class TimelineCollector {

	private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	static{
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	private static TimelineCollector INSTANCE = null;
	private Document doc = null;
	private Element root = null;
	
	private TimelineCollector() {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			doc = docBuilder.newDocument();
			root = doc.createElement("data");
			doc.appendChild(root);
		} catch (ParserConfigurationException ex) {
			new RuntimeException("Could not create Timeline Collector", ex);
		}
	}
	
	public void addEvent(TimeEvent ev) {
		Element xmlEvent =  doc.createElement("event");
		switch (ev.getType()){
		case ARRAY_CREATION:
			xmlEvent.setAttribute("start", format.format(ev.getTime()));
			xmlEvent.setAttribute("title", "Array" + ev.getArray().getId() + " was created.");
			//TODO: put text here
			break;
		case ARRAY_DESTRUCTION:
			xmlEvent.setAttribute("start", format.format(ev.getTime()));
			xmlEvent.setAttribute("title", "Array" + ev.getArray().getId() + " was destroyed.");
			//TODO: put text here
			break;
		case SCHEDBLOCK_EXECUTION_FINISH:
			xmlEvent.setAttribute("start", format.format(new Date(ev.getTime().getTime() - ev.getDuration())));
			xmlEvent.setAttribute("end", format.format(ev.getTime()));
			String title = "SchedBlock ";
			if (ev.getSb().getUid() == null) 
				title+=ev.getSb().getId();
			else
				title+=ev.getSb().getUid(); 
			title+=" was observed.";
			//TODO: put text here
			xmlEvent.setAttribute("title", title);
			break;
		default:
			return;
		}
		synchronized(doc) {
			root.appendChild(xmlEvent);
		}
	}
	
	public String getXML() {
		String retVal;
		synchronized(doc){
			retVal = getStringFromDoc(doc);
		}
		return retVal;
	}

	private static String getStringFromDoc(org.w3c.dom.Document doc) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) doc
				.getImplementation();
		LSSerializer lsSerializer = domImplementation.createLSSerializer();
		return lsSerializer.writeToString(doc);
	}
	
	public static TimelineCollector getInstance() {
		if (INSTANCE == null)
			INSTANCE = new TimelineCollector();
		return INSTANCE;
	}
	
	/** 
	 * Creates an new (empty) xml document for the instance
	 * */
	public void reset() {
		synchronized (doc) {
			try {
				DocumentBuilderFactory dbfac = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
				INSTANCE.doc = docBuilder.newDocument();
				INSTANCE.root = doc.createElement("data");
				doc.appendChild(root);
			} catch (ParserConfigurationException ex) {
				new RuntimeException("Could not create a new XML document", ex);
			}
		}
	}
}
