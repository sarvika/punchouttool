package io.sarvika.potool.common.cxml;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class XMLParser {
	private static final Log logger = LogFactory.getLog(XMLParser.class);

	public static Document convertToDocument(String requestMessage) {
		org.dom4j.Document doc = null;
		EntityResolver entityResolver = null;
		try {
			List<SAXParseException> errors = new ArrayList<SAXParseException>();
			doc = createSAXReader(errors, entityResolver ).read(new ByteArrayInputStream(requestMessage.getBytes()));
			if ( errors.size() != 0 ) {
				logger.error("XMLParser : Can not parse incoming Message!", errors.get(0));
				doc = null;
			}
		}
		catch (Exception e) {
			logger.error("XMLParser : Can not Parse incoming message!"+requestMessage);
		}
		return doc;
    }
	
	private static SAXReader createSAXReader(List<SAXParseException> errorsList, EntityResolver entityResolver) {
		SAXReader saxReader =  new SAXReader();
		saxReader.setEntityResolver(entityResolver);
		saxReader.setErrorHandler( (new XMLParser()).new ErrorLogger(errorsList) );
		saxReader.setMergeAdjacentText(true);
//		saxReader.setValidation(true);
		return saxReader;
	}
	
	private class ErrorLogger implements ErrorHandler {
		private String file;
		private List<SAXParseException> errors;
		ErrorLogger(List<SAXParseException> errors) {
			this.file="Request CXML";
			this.errors = errors;
		}
		public void error(SAXParseException error) {
			logger.error( "Error parsing XML: " + file + '(' + error.getLineNumber() + ") " + error.getMessage() );
			errors.add(error);
		}
		public void fatalError(SAXParseException error) {
			error(error);
		}
		public void warning(SAXParseException warn) {
			logger.warn( "Warning parsing XML: " + file + '(' + warn.getLineNumber() + ") " + warn.getMessage() );
		}
	}

}
