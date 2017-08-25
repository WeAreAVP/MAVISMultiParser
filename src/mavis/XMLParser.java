/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mavis;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author rimsha@geeks
 */
public class XMLParser {

    private File xmlFile = null;
    private Document doc = null;
    private Object carriers = null;
    private int cCount = 0;
    private int cCurrent = 0;
    private String carValue = "";
    private boolean isPreferred = false;
    private int totalComponents = 0;

    public XMLParser(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    public Map parseXML() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.setErrorHandler(new SimpleErrorHandler());
//            File fXmlFile = new File(this.xmlFile);
            doc = dBuilder.parse(this.xmlFile);

            doc.getDocumentElement().normalize();

            NodeList resultNode = doc.getChildNodes();

            HashMap result = new HashMap();
            MyNodeList tempNodeList = new MyNodeList();

            String emptyNodeName = null, emptyNodeValue = null;

            for (int index = 0; index < resultNode.getLength(); index++) {
                Node tempNode = resultNode.item(index);

                if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                    tempNodeList.addNode(tempNode);
                }
                emptyNodeName = tempNode.getNodeName();
                emptyNodeValue = tempNode.getNodeValue();
            }

            if (tempNodeList.getLength() == 0 && emptyNodeName != null
                    && emptyNodeValue != null) {
                result.put(emptyNodeName, emptyNodeValue);
                return result;
            }

            this.parseXMLNode(tempNodeList, result);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void parseXMLNode(NodeList nList, HashMap result) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE
                    && nNode.hasChildNodes()
                    && nNode.getFirstChild() != null
                    && (nNode.getFirstChild().getNextSibling() != null
                    || nNode.getFirstChild().hasChildNodes())) {
                NodeList childNodes = nNode.getChildNodes();
                MyNodeList tempNodeList = new MyNodeList();
                for (int index = 0; index < childNodes.getLength(); index++) {
                    Node tempNode = childNodes.item(index);
                    if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                        tempNodeList.addNode(tempNode);
                    }
                }
                HashMap dataHashMap = new HashMap();
                if (result.containsKey(nNode.getNodeName()) && result.get(nNode.getNodeName()) instanceof List) {
                    List mapExisting = (List) result.get(nNode.getNodeName());
                    mapExisting.add(dataHashMap);
                } else if (result.containsKey(nNode.getNodeName())) {
                    List counterList = new ArrayList();
                    counterList.add(result.get(nNode.getNodeName()));
                    counterList.add(dataHashMap);
                    result.put(nNode.getNodeName(), counterList);
                } else {
                    result.put(nNode.getNodeName(), dataHashMap);
                }
                if (nNode.getAttributes().getLength() > 0) {
                    Map attributeMap = new HashMap();
                    for (int attributeCounter = 0;
                            attributeCounter < nNode.getAttributes().getLength();
                            attributeCounter++) {
                        attributeMap.put(
                                nNode.getAttributes().item(attributeCounter).getNodeName(),
                                nNode.getAttributes().item(attributeCounter).getNodeValue()
                        );
                    }
                    dataHashMap.put("attributes", attributeMap);
                }
                this.parseXMLNode(tempNodeList, dataHashMap);
            } else if (nNode.getNodeType() == Node.ELEMENT_NODE
                    && nNode.hasChildNodes() && nNode.getFirstChild() != null
                    && nNode.getFirstChild().getNextSibling() == null) {
                this.putValue(result, nNode);
            } else if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                this.putValue(result, nNode);
            }
        }
    }

    private void putValue(HashMap result, Node nNode) {

        HashMap attributeMap = new HashMap();
        Object nodeValue = null;
        if (nNode.getFirstChild() != null) {
            nodeValue = nNode.getFirstChild().getNodeValue();
            if (nodeValue != null) {
                nodeValue = nodeValue.toString().trim();
            }
        }
        HashMap nodeMap = new HashMap();
        nodeMap.put("value", nodeValue);
        Object putNode = nodeValue;
        if (nNode.getAttributes().getLength() > 0) {
            for (int attributeCounter = 0;
                    attributeCounter < nNode.getAttributes().getLength();
                    attributeCounter++) {
                attributeMap.put(
                        nNode.getAttributes().item(attributeCounter).getNodeName(),
                        nNode.getAttributes().item(attributeCounter).getNodeValue()
                );
            }
            nodeMap.put("attributes", attributeMap);
            putNode = nodeMap;
        }
        if (result.containsKey(nNode.getNodeName()) && result.get(nNode.getNodeName()) instanceof List) {
            List mapExisting = (List) result.get(nNode.getNodeName());
            mapExisting.add(putNode);
        } else if (result.containsKey(nNode.getNodeName())) {
            List counterList = new ArrayList();
            counterList.add(result.get(nNode.getNodeName()));
            counterList.add(putNode);
            result.put(nNode.getNodeName(), counterList);
        } else {
            result.put(nNode.getNodeName(), putNode);
        }
    }

    private void getTotalCOmponents(Object components) {
        Map maa = (Map) components;
        Iterator ite = maa.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry pairs = (Map.Entry) ite.next();
            Object avalue = pairs.getValue();
            if (avalue instanceof List) {
                this.totalComponents = this.totalComponents + ((List) avalue).size();
            } else {
                this.totalComponents = this.totalComponents + 1;
            }
        }
    }

    private Object getComponent(Object components, int index) {
        Map maa = (Map) components;
        Iterator ite = maa.entrySet().iterator();
        int i = 1;
        Object value = null;
        while (ite.hasNext()) {
            Map.Entry pairs = (Map.Entry) ite.next();
            Object avalue = pairs.getValue();
            if (avalue instanceof List) {
                List list = (List) avalue;
                for (Integer in = 0; in < list.size(); in++) {
                    Object val = list.get(in);
                    if (i == index) {
                        return val;
                    }
                    i++;
                }
                i--;
            } else if (i == index) {
                return avalue;
            }
            i++;
        }
        return value;
    }

    private void setCarriers(Object components) {
        Iterator audioItr = ((Map) components).entrySet().iterator();
        while (audioItr.hasNext()) {
            Map.Entry pairsA = (Map.Entry) audioItr.next();
            String key = pairsA.getKey().toString();
            Object svalue = pairsA.getValue();
            if (key.equals("carriers")) {
                this.carriers = svalue;
                Iterator mm = ((Map) svalue).entrySet().iterator();
                while (mm.hasNext()) {
                    Map.Entry pair = (Map.Entry) mm.next();
                    Object value = pair.getValue();
                    if (value instanceof Map) {
                        this.cCount = 1;
                    } else if (value instanceof List) {
                        this.cCount = ((List) value).size();
                    }
                }
            }
        }
    }

    private void getCarriers(Element ele) {
        String[] ignores = new String[]{"audiovisualItemType", "component", "enteredDate", "exactLength", "modifiedDate",
            "whoEntered", "whoModified", "work", "workGroupEntered"
        };
        Element carrier = this.doc.createElement("carriers");
        ele.appendChild(carrier);

        Iterator mm = ((Map) this.carriers).entrySet().iterator();
        while (mm.hasNext()) {
            Map.Entry pair = (Map.Entry) mm.next();
            Object value = pair.getValue();
            String key = pair.getKey().toString();
            Element cType = this.doc.createElement(key);
            carrier.appendChild(cType);
            if (value instanceof Map) {
                processMap(value, key, key, cType, ignores);
                setCarrierTitle(value);
            } else if (value instanceof List) {
                List l = (List) value;
                processMap((Object) l.get(this.cCurrent), key, key, cType, ignores);
                setCarrierTitle((Object) l.get(this.cCurrent));
            }
        }
    }

    private boolean loopThroughObject(Map ObjectIdentifier) {
        Map iType = (Map) ObjectIdentifier;
        Object identifierType = iType.get("identifierType");
        if (identifierType instanceof Map) {
            Map iVal = (Map) identifierType;
            Object val = iVal.get("value");
            if (val.equals("ORIGINAL_CONTROL_NO") || val.equals("AFCI")) {
                Map iidentifier = (Map) ObjectIdentifier;
                Object identifier = iidentifier.get("identifier");
                this.carValue = identifier.toString();
                return true;
            }
        }
        return false;
    }

    private void setCarrierTitle(Object obj) {
        Map tNode = (Map) obj;
        Object objectIdentifiers = tNode.get("objectIdentifiers");
        if (objectIdentifiers != null) {
            Map tNode1 = (Map) objectIdentifiers;
            Object ObjectIdentifier = tNode1.get("ObjectIdentifier");
            if (ObjectIdentifier instanceof Map) {
                this.loopThroughObject((Map) ObjectIdentifier);
            } else if (ObjectIdentifier instanceof List) {
                List list = (List) ObjectIdentifier;
                for (Integer index = 0; index < list.size(); index++) {
                    if (this.loopThroughObject((Map) list.get(index))) {
                        break;
                    }
                }
            }
        } else {
            this.carValue = "";
        }
    }

    private String getComponentType(Object components, int index) {

        Map maa = (Map) components;
        Iterator ite = maa.entrySet().iterator();
        int i = 1;
        while (ite.hasNext()) {
            Map.Entry pairs = (Map.Entry) ite.next();
            Object avalue = pairs.getValue();
            String key = pairs.getKey().toString();
            if (avalue instanceof List) {
                List list = (List) avalue;
                for (Integer in = 0; in < list.size(); in++) {
                    if (i == index) {
                        return key;
                    }
                    i++;
                }
                i--;
            } else if (i == index) {
                return key;
            }
            i++;
        }
        return "";
    }

    public String createXML(Map xmlMap, String path) {
        try {
            String[] attributes = new String[]{"database", "version", "organisation", "xmlns:xl", "xmlns"};
            Object value = xmlMap.get("mavis");
            Map mavis = (Map) value;
            Object mAttibutes = mavis.get("attributes");
            Map m = (Map) mAttibutes;
            Map TWork = (Map) value;
            Object TitleWork = TWork.get("TitleWork");
            Map tNode = (Map) TitleWork;
            Object objectIdentifiers = tNode.get("objectIdentifiers");
            Object mediums = tNode.get("mediums");
            Object components = tNode.get("components");
            Object preferredTitle = tNode.get("preferredTitle");
            this.getTotalCOmponents(components);
            String name = FilenameUtils.getBaseName(this.xmlFile.getPath()) + "_" + System.currentTimeMillis() + ".xml";
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            this.doc = docBuilder.newDocument();
            // root element
            Element temElement = doc.createElement("mavis");
            this.doc.appendChild(temElement);

            Iterator it = m.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String key = pairs.getKey().toString();
                Object svalue = pairs.getValue();
                if (Arrays.asList(attributes).contains(key)) {
                    Attr attr = this.doc.createAttribute(key);
                    attr.setValue(Normalizer.normalize(svalue.toString(), Normalizer.Form.NFD).toString());
                    temElement.setAttributeNode(attr);
                }
            }
            int looping = 1;
            while (looping <= this.totalComponents) {
                this.cCount = 0;
                this.cCurrent = 0;
                Object com = getComponent(components, looping);
                setCarriers(com);
                while (this.cCount > this.cCurrent) {
                    // TitleWork
                    Element TitleWork1 = this.doc.createElement("TitleWork");
                    temElement.appendChild(TitleWork1);

                    //objectIdentifiers           
                    Element objectIdentifiers1 = this.doc.createElement("objectIdentifiers");
                    TitleWork1.appendChild(objectIdentifiers1);

                    String[] ignores = new String[]{};
                    processMap(objectIdentifiers, "objectIdentifiers", "objectIdentifiers", objectIdentifiers1, ignores);

                    // components
                    ignores = new String[]{"accessionDate", "acquisitionCode", "audiovisualItemType", "autographed", "deaccessionRecommended", "enteredDate", "exactLength", "isComplete", "isRegistration", "modifiedDate", "obligationMet", "pictureDisc", "pictureSleeve", "purchasePrice", "receiptRequired", "receivedDate", "source", "valuation",
                        "whoEntered", "whoModified", "work", "workGroupEntered"
                    };
                    Element components1 = this.doc.createElement("components");
                    TitleWork1.appendChild(components1);
                    Element comType = this.doc.createElement(getComponentType(components, looping));
                    components1.appendChild(comType);

                    processMap(com, "components", "components", comType, ignores);
                    // mediums
                    Element mediums1 = this.doc.createElement("mediums");
                    TitleWork1.appendChild(mediums1);
                    processMap(mediums, "mediums", "mediums", mediums1, ignores);
                    ignores = new String[]{};
                    this.isPreferred = true;
                    Element preferredTitle1 = this.doc.createElement("preferredTitle");
                    TitleWork1.appendChild(preferredTitle1);
                    processMap(preferredTitle, "preferredTitle", "preferredTitle", preferredTitle1, ignores);
                    this.isPreferred = false;
                    String[] ignoreNodes = new String[]{"award", "contentSortIndex", "enteredDate", "isCompilation", "objectIdentifiers", "mediums", "components", "preferredTitle",
                        "modifiedDate", "nomination", "notForPublic", "sound", "whoEntered", "workStatus", "whoModified", "workGroupEntered", "workStatus"
                    };
                    processMap(TitleWork, "TitleWork", "TitleWork", TitleWork1, ignoreNodes);
                    this.cCurrent++;
                }
                looping++;
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(this.doc);

            StreamResult result = new StreamResult(new File(path + File.separator + name));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (ParserConfigurationException ex) {
            System.out.println("Something went wrong please try again");
        } catch (Exception ex) {
            System.out.println("Something went wrong please try again");
        }

        return "done";
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private void processMap(Object obj, String key1, String parent, Element ele, String[] ignores) {
        Map maa = (Map) obj;
        Iterator ite = maa.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry pairs = (Map.Entry) ite.next();
            String key = pairs.getKey().toString();
            Object svalue = pairs.getValue();
            if (!Arrays.asList(ignores).contains(key)) {
                if (key.equals("carriers")) {
                    getCarriers(ele);
                    continue;
                }
                if (svalue instanceof Map) {
                    if (!key.equals("attributes")) {
                        if (!isInteger(key)) {
                            Element newEle = this.doc.createElement(key);
                            ele.appendChild(newEle);
                            processMap((Map) svalue, key, key, newEle, ignores);
                        } else {
                            Element newEle = this.doc.createElement(key1);
                            ele.appendChild(newEle);
                            processMap((Map) svalue, key, key, newEle, ignores);
                        }
                    } else {
                        processMap((Map) svalue, key, parent, ele, ignores);
                    }
                } else if (svalue instanceof List) {
                    processMap((List) svalue, key, key, ele, ignores);
                } else if (key1.equals("attributes")) {
                    String[] nodes = new String[]{"identifierType", "acquisition", "collection", "party", "role"};
                    if (Arrays.asList(nodes).contains(parent)) {
                        Attr attri = this.doc.createAttribute(key);
                        attri.setValue(Normalizer.normalize(svalue.toString(), Normalizer.Form.NFD).toString());
                        ele.setAttributeNode(attri);
                    }
                } else if (key.equals("value")) {
                    if (svalue != null && !svalue.toString().equals("")) {
                        ele.appendChild(this.doc.createTextNode(Normalizer.normalize(svalue.toString(), Normalizer.Form.NFD).toString()));
                    }
                } else {
                    String newVal = "";
                    if (svalue != null) {
                        newVal = Normalizer.normalize(String.valueOf(svalue), Normalizer.Form.NFD).toString();
                        if (key.equals("descr") && this.isPreferred && this.carValue != "") {
                            newVal = newVal + " " + this.carValue;
                        }
                    }
                    Element newEle = this.doc.createElement(key);
                    if (!newVal.equals("")) {
                        newEle.appendChild(this.doc.createTextNode(newVal));
                    }
                    ele.appendChild(newEle);

                }
            }
        }
    }

    private void processMap(List list, String key, String parent, Element ele, String[] ignores) {
        for (Integer index = 0; index < list.size(); index++) {
            Object value = list.get(index);
            if (!Arrays.asList(ignores).contains(index.toString())) {
                if (key.equals("carriers")) {
                    getCarriers(ele);
                    continue;
                }
                if (value instanceof Map) {
                    if (!index.toString().equals("attributes")) {
                        if (!isInteger(index.toString())) {
                            Element newEle = this.doc.createElement(index.toString());
                            ele.appendChild(newEle);
                            processMap((Map) value, index.toString(), index.toString(), newEle, ignores);
                        } else {
                            Element newEle = this.doc.createElement(key);
                            ele.appendChild(newEle);
                            processMap((Map) value, key, key, newEle, ignores);
                        }
                    } else {
                        processMap((Map) value, index.toString(), parent, ele, ignores);
                    }
                } else if (value instanceof List) {
                    processMap((List) value, index.toString(), index.toString(), ele, ignores);
                } else {
                    System.out.println(index.toString() + ": " + value);
                }
            }
        }
    }
}
