package io.sarvika.potool.common.cxml;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class CXMLBuilder {
	private static final Log logger = LogFactory.getLog(CXMLBuilder.class);
	
	private static final String DTD_FILE = "http://xml.cxml.org/schemas/cXML/1.2.006/cXML.dtd";
	private static final String ROOT_TAG = "cXML";
	
	private StringWriter message = new StringWriter();
	private TransformerHandler handler;
	
	private Stack<String> currentTagName = new Stack<String>();
	
	public CXMLBuilder(boolean isRequest) throws TransformerConfigurationException, SAXException {
		PrintWriter pw = new PrintWriter(message); 
		StreamResult streamResult = new StreamResult(pw);
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		handler = tf.newTransformerHandler();
		Transformer serializer = handler.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");// Suitable for all languages
		serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, ROOT_TAG); //Replace this with something usefull
		serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, DTD_FILE);
		serializer.setOutputProperty(OutputKeys.METHOD,"xml");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes"); // So it looks pretty in VI
		handler.setResult(streamResult);
		handler.startDocument();
		Date now = Calendar.getInstance().getTime();
		if (isRequest) {
			this.startElement(ROOT_TAG, 
					"xml:lang","en", 
					"payloadID", generatePayloadID(now), 
					"timestamp", generateTimeStamp(now));
		}else
			handler.startElement("", "", ROOT_TAG, null);
	}

	private String generateTimeStamp(Date now) {
	    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(now);
    }
	
	private String generatePayloadID(Date now) {
	    return new SimpleDateFormat("yyyyMMdd:HH:mm.ss.SSS").format(now)+"@jgodara.github.io";
    }

	public void startElement(String tagName, String... attributes) throws SAXException {
		this.currentTagName.push(tagName);
		AttributesImpl attributesImpl = null;
		if (attributes!=null && attributes.length>0)
			attributesImpl =  createAttributes(attributes);

		handler.startElement("", "", tagName, attributesImpl);
	}
	
	private AttributesImpl createAttributes(String[] attributes) {
		AttributesImpl attributesImpl = new AttributesImpl();
		
		for(int i=0; i<attributes.length; i++,i++) {
			attributesImpl.addAttribute("", "", attributes[i], "CDATA", attributes[i+1]);
		}
	    return attributesImpl;
    }

	public void endElement(String name) throws SAXException {
		String tagName = this.currentTagName.pop();
		if (tagName==null || !tagName.equals(name)) {
			logger.error("CXMLBuilder error. expected name:"+tagName+" Input Name:"+name);
		}
		handler.endElement("", "", tagName);
	}
	public String getMessage() throws SAXException {
		handler.endElement("", "", ROOT_TAG);
		handler.endDocument();
	    return message.toString();
    }

	public void setTextContent(String content) throws SAXException {
		handler.characters(content.toCharArray(), 0, content.length());
    }
	
	

}
