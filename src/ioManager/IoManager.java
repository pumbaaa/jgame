package ioManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tileMap.RoomMap;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class IoManager implements Serializable {
  private static final long serialVersionUID = 2;

  private RoomMap roomMap;
  private String fileName;

  private InputStream is;
  private OutputStream os;
  
  // private final String encoding = "UTF-8";

  private Document document;
  private DocumentBuilderFactory factory;
  private DocumentBuilder builder;

  public IoManager(String fileName) {
    roomMap = null;
    this.fileName = fileName;
  }

  public IoManager(RoomMap roomMap, String fileName) {
    this.roomMap = roomMap;
    this.fileName = fileName;
  }

  private void initInputFile(){
    is = getClass().getResourceAsStream(fileName);
  }
  
  private void initOutputFile(){
    try {
      File file = new File(fileName);
      os = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

    public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  public boolean save() {
    if (fileName != null) {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(false);
      saveRoom();

      return true;
    } else {
      return false;
    }
  }

  private boolean saveRoom() {
    try {
      builder = factory.newDocumentBuilder();
      document = builder.newDocument();
      if (roomMap != null) {
        // <room>
        // root - what type of xml-file is it
        Element eRoot = document.createElement("room");
        document.appendChild(eRoot);

        // <attributes>
        // information about the room
        Element eInfo = document.createElement("information");
        eRoot.appendChild(eInfo);

        // <name>
        // name
        Element eName = document.createElement("name");
        eName.appendChild(document.createTextNode(roomMap.getName()));
        eInfo.appendChild(eName);

        // <width>
        // width
        Element eWidth = document.createElement("width");
        eWidth.appendChild(document.createTextNode("" + roomMap.getColumns()));
        eInfo.appendChild(eWidth);

        // <height>
        // height
        Element eHeight = document.createElement("height");
        eHeight.appendChild(document.createTextNode("" + roomMap.getRows()));
        eInfo.appendChild(eHeight);

        // info
        Element eInformation = document.createElement("info");
        eInformation
            .appendChild(document.createTextNode("" + roomMap.getInfo()));
        eInfo.appendChild(eInformation);

        // <tiles>
        // information about the room
        Element eTiles = document.createElement("tiles");
        eRoot.appendChild(eTiles);

        int n = 0;
        for (int x = 0; x < roomMap.getColumns(); ++x) {
          for (int y = 0; y < roomMap.getRows(); ++y) {

            // id
            Element eTile = document.createElement("tile");
            eTiles.appendChild(eTile);
            eTile.setAttribute("id", "" + n);
            eTile.setAttribute("x", "" + x);
            eTile.setAttribute("y", "" + y);

            // x
            Element eX = document.createElement("x");
            eX.appendChild(document.createTextNode("" + x));
            eTile.appendChild(eX);

            // y
            Element eY = document.createElement("y");
            eY.appendChild(document.createTextNode("" + y));
            eTile.appendChild(eY);

            // image id
            Element eImageId = document.createElement("imageid");
            eImageId.appendChild(
                document.createTextNode("" + roomMap.getImageId(x, y)));
            eTile.appendChild(eImageId);

            // type
            Element eType = document.createElement("type");
            eType.appendChild(
                document.createTextNode("" + roomMap.getType(x, y)));
            eTile.appendChild(eType);

            // Tile set
            Element eSet = document.createElement("set");
            eSet.appendChild(
                document.createTextNode("" + roomMap.getSet(x, y)));
            eTile.appendChild(eSet);
            ++n;
          }
        }

      }

      // Transform the document to a xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
          "2");

      DOMSource source = new DOMSource(document);

      initOutputFile();
      StreamResult streamResult = new StreamResult(os);
      transformer.transform(source, streamResult);

      // Output to console (debug)
      // StreamResult streamResultConsole = new StreamResult(System.out);
      // transformer.transform(source, streamResultConsole);
    } catch (ParserConfigurationException | TransformerException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public boolean load() {
    if (fileName != null) {
      initInputFile();

      factory = DocumentBuilderFactory.newInstance();
      try {
        builder = factory.newDocumentBuilder();
        try {
          document = builder.parse(is);

          if (document.getDocumentElement().getNodeName().equals("room")) {
            if (document.hasChildNodes()) {
              processNode(document.getChildNodes());
            }

          }
        } catch (SAXException | IOException e) {
          e.printStackTrace();
        }
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      }

      return true;
    } else {
      return false;
    }
  }

  public RoomMap getRoom() {
    return roomMap;
  }

  private void processNode(NodeList nodes) {
    for (int i = 0; i < nodes.getLength(); ++i) {
      Node tNode = nodes.item(i);
      if (tNode.getNodeType() == Node.ELEMENT_NODE) {
        if (tNode.hasChildNodes()) {
          NodeList tNodes = tNode.getChildNodes();
          for (int j = 0; j < tNodes.getLength(); ++j) {
            Node rNode = tNodes.item(j);
            if (rNode.getNodeType() == Node.ELEMENT_NODE) {
              if (rNode.hasChildNodes()) {
                if (rNode.getNodeName().equals("information")) {
                  // Get the information about a room (w, h, name, info)
                  loadRoomInfo(rNode.getChildNodes());
                } else if (rNode.getNodeName().equals("tiles")) {
                  NodeList iNodes = rNode.getChildNodes();
                  for (int k = 0; k < iNodes.getLength(); ++k) {
                    Node iNode = iNodes.item(k);
                    if (iNode.getNodeType() == Node.ELEMENT_NODE) {
                      if (iNode.hasChildNodes()) {
                        // Get the information about a tile (n, type, from set)
                        loadToTile(iNode.getChildNodes());
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private void loadRoomInfo(NodeList nl) {
    int rows = 0;
    int columns = 0;
    String name = "";
    String info = "";
    int c = 0;
    for (int i = 0; i < nl.getLength(); ++i) {
      Node n = nl.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        if (n.getNodeName().equals("name")) {
          name = n.getTextContent();
          ++c;
        } else if (n.getNodeName().equals("width")) {
          columns = Integer.parseInt(n.getTextContent());
          ++c;
        } else if (n.getNodeName().equals("height")) {
          rows = Integer.parseInt(n.getTextContent());
          ++c;
        } else if (n.getNodeName().equals("info")) {
          info = n.getTextContent();
          ++c;
        }
      }
    }
    if (c > 3) {
      roomMap = new RoomMap(name, columns, rows, info);
    } else {
      roomMap = new RoomMap("newroom", 20, 16, "A New Room");
    }
  }

  private void loadToTile(NodeList nl) {
    int x = 0;
    int y = 0;
    int imageid = 0;
    int type = 0;
    int set = 0;
    for (int i = 0; i < nl.getLength(); ++i) {
      Node n = nl.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        if (n.getNodeName().equals("x")) {
          x = Integer.parseInt(n.getTextContent());
        } else if (n.getNodeName().equals("y")) {
          y = Integer.parseInt(n.getTextContent());
        } else if (n.getNodeName().equals("imageid")) {
          imageid = Integer.parseInt(n.getTextContent());
        } else if (n.getNodeName().equals("type")) {
          type = Integer.parseInt(n.getTextContent());
        } else if (n.getNodeName().equals("set")) {
          set = Integer.parseInt(n.getTextContent());
        }
      }
    }
    roomMap.setImageId(x, y, imageid);
    roomMap.setType(x, y, type);
    roomMap.setSet(x, y, set);
  }
}
