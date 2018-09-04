package io.sarvika.potool.controllers;

import io.sarvika.potool.common.cxml.CXMLParser;
import io.sarvika.potool.common.cxml.XMLParser;
import io.sarvika.potool.common.model.ItemIn;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@Controller
public class ApplicationController {

    @Value("${potool.cxml-template}")
    String templateFileName;

    @RequestMapping("/")
    public ModelAndView homeController() {
        return new ModelAndView("home");
    }

    @RequestMapping(value = "/post/cxml", method = RequestMethod.POST)
    public void cxmlPostController(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String url = request.getParameter("url");
        String domain = request.getParameter("domain");
        String networkid = request.getParameter("networkid");
        String sharedSecret = request.getParameter("shared");

        String template = FileUtils.readFileToString(new File(templateFileName));

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("potool.domain", domain);
        valueMap.put("potool.ident", networkid);
        valueMap.put("potool.secret", sharedSecret);
        valueMap.put("potool.cookie", UUID.randomUUID().toString());

        StrSubstitutor substitutor = new StrSubstitutor(valueMap);

        String query = substitutor.replace(template);

        URLConnection connection = new URL(url).openConnection();
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setRequestProperty("accept-charset", "UTF-8");
        connection.setRequestProperty("content-type", "text/xml");

        OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        outputStream.write(query.getBytes());
        outputStream.flush();

        InputStream inputStream = connection.getInputStream();

        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(inputStream, stringWriter);

        String cxmlResponseAsString = stringWriter.toString();

        Document doc = XMLParser.convertToDocument(cxmlResponseAsString);
        CXMLParser cxmlResponse = new CXMLParser(doc.getRootElement()).get("Response");

        String statusCode = cxmlResponse.get("Status").getAttribute("code");
        if (!"200".equals(statusCode)) {
            response.sendRedirect("/cxmlError?code" + statusCode + "&m=" + cxmlResponse.get("Status").getAttribute("text"));
            return;
        }

        String landpage = cxmlResponse.get("PunchOutSetupResponse").get("StartPage").get("URL").getTextValue();
        response.sendRedirect(landpage);
    }

    @PostMapping("/callback/cxml")
    public ModelAndView cxmlCallBack(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("cxmlcallback");

        String cxmlInResponse = request.getParameter("cxml-urlencoded");
        Document cxmlAsDoc = XMLParser.convertToDocument(cxmlInResponse);

        CXMLParser punchoutResponse = new CXMLParser(cxmlAsDoc.getRootElement()).get("Message").get("PunchOutOrderMessage");
        CXMLParser money = punchoutResponse.get("PunchOutOrderMessageHeader").get("Total").get("Money");

        String total = money.getTextValue();
        String currency = money.getAttribute("currency");

        List<ItemIn> itemIns = new ArrayList<>();
        for (CXMLParser itemIn : punchoutResponse.getAll("ItemIn")) {
            ItemIn item = new ItemIn();

            item.setQty(Integer.parseInt(itemIn.getAttribute("quantity")));
            item.setPartId(itemIn.get("ItemID").get("SupplierPartID").getTextValue());
            item.setPartAuxId(itemIn.get("ItemID").get("SupplierPartAuxiliaryID").getTextValue());

            CXMLParser itemDetail = itemIn.get("ItemDetail");

            item.setPrice(Double.parseDouble(itemDetail.get("UnitPrice").get("Money").getTextValue()));
            item.setDesc(itemDetail.get("Description").getTextValue());
            item.setUom(itemDetail.get("UnitOfMeasure").getTextValue());

            for (CXMLParser classification : itemDetail.getAll("Classification")) {
                item.getClassifications().put(classification.getAttribute("domain"), classification.getTextValue());
            }

            itemIns.add(item);
        }

        mv.getModel().put("total", total);
        mv.getModel().put("curreny", currency);
        mv.getModel().put("itemins", itemIns);
        mv.getModel().put("cxml", cxmlInResponse);

        return mv;
    }

    @PostMapping("/callback/oci")
    public ModelAndView ociCallBack(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("ocicallback");

        String ociInResponse = request.getParameter("cxml-urlencoded");

        return mv;
    }

}
