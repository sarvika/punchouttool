package io.sarvika.potool.common.cxml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Text;

public class CXMLParser {
	private static final Log logger = LogFactory.getLog(CXMLParser.class);
	
	private Element element;

	public CXMLParser(Element element) {
		this.element = element;
    }

	public CXMLParser get(String tagName) {
		if (element == null) {
			logger.warn("Element is null, ignore the search for tag "+tagName);
			return null;
		}
		Element e = element.element(tagName);
	    return e==null?null:new CXMLParser(e);
    }
	
	public List<CXMLParser> getAll(String tagName) {
		List<CXMLParser> els = new ArrayList<CXMLParser>();
		for(Iterator<Element> iter = element.elementIterator(tagName);iter.hasNext();) {
			els.add(new CXMLParser(iter.next()));
		}
		return els;
	}

	public String getTextValue() {
		if (element == null) return null;
		return (element.content().size()>0) ?
		 ((Text)(element.content().get(0))).getStringValue():
		 "";
    }

	public String getAttribute(String name) {
		if (element == null) {
			logger.warn("Element is null, ignore the search for attribute "+name);
			return null;
		}
		return element.attributeValue(name);
	}
}
