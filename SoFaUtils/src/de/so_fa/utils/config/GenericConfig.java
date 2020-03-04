package de.so_fa.utils.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.so_fa.utils.config.GenericConfigItem.Type;

public class GenericConfig {
  private static Logger ourLogger = Logger
      .getLogger(GenericConfig.class.getName());
  private static GenericConfig ourInstance = null;

  private File myConfigDir;
  private File myConfigFile;
  private Document myConfigDoc;
  private Element myXMLRootElement;
  private String myApplicationName;
  private Map<String, GenericConfigItem> myConfigMap = new TreeMap<String, GenericConfigItem>();

  public static GenericConfig getInstance() {
    return getInstance(null);
  }

  public static GenericConfig getInstance(String aAppName) {
    if (null == ourInstance) {
      ourInstance = new GenericConfig(aAppName);
    }
    return ourInstance;
  }

  private GenericConfig(String aApplicationName) {
    myApplicationName = aApplicationName;
    mkDirectories();
    String theConfig = System.getProperty("user.home") + File.separator
        + myApplicationName + File.separator + "config" + File.separator
        + "config.xml";
    myConfigFile = new File(theConfig);
    if (!myConfigDir.canWrite()) {
      throw new RuntimeException(
          "cannot write config directory: " + myConfigDir);
    }
    ourLogger.info("using configuration file : " + myConfigFile.toString());
    if (myConfigFile.exists()) {
      if (!myConfigFile.canRead()) {
        throw new RuntimeException(
            "cannot read configuration: " + myConfigFile);
      }
      if (!myConfigFile.canWrite()) {
        throw new RuntimeException(
            "configuration file is read only: " + myConfigFile);
      }
      readConfig();
    } else {
      createConfig();
    }
  }

  private void mkDirectories() {
    String baseDir = System.getProperty("user.home") + "/" + myApplicationName;
    myConfigDir = new File(baseDir + "/config/");
    myConfigDir.mkdirs();
  }

  private void createConfig() {
    StringBuffer initialDocument = new StringBuffer();

    initialDocument.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    initialDocument.append("<!-- This is a comment -->");
    initialDocument.append("<" + myApplicationName + ">");
    initialDocument.append("</" + myApplicationName + ">");

    try {
      myConfigDoc = new SAXBuilder()
          .build(new StringReader(initialDocument.toString()));
      myXMLRootElement = myConfigDoc.getRootElement();

    } catch (JDOMException | IOException e) {
      throw new RuntimeException("cannot parse config: " + myConfigFile, e);
    }
  }

  private void readConfig() {
    try {
      myConfigDoc = new SAXBuilder().build(myConfigFile);
    } catch (JDOMException | IOException e) {
      throw new RuntimeException("cannot parse config: " + myConfigFile, e);
    }

    myXMLRootElement = myConfigDoc.getRootElement();

    for (Element configItem : myXMLRootElement.getChildren()) {
      String name = configItem.getName();
      String value = configItem.getValue();
      if (null == configItem.getAttribute("type")) {
        boolean isInt = false;
        try {
          Integer.parseInt(value); 
          isInt = true;
        } catch(NumberFormatException e) {
        }
        if (isInt) {
          myConfigMap.put(name,
              new GenericConfigItem(name, Type.INTEGER, value));
        } else {
          myConfigMap.put(name,
              new GenericConfigItem(name, Type.STRING, value));
        }
        
      } else {
        String type = configItem.getAttribute("type").getValue();
        if (type.equals(Type.INTEGER.toString())) {
          myConfigMap.put(name,
              new GenericConfigItem(name, Type.INTEGER, value));
        } else if (type.equals(Type.ENCRYPT.toString())) {
          myConfigMap.put(name,
              new GenericConfigItem(name, Type.ENCRYPT, value));
        } else if (type.equals(Type.BOOLEAN.toString())) {
          myConfigMap.put(name,
              new GenericConfigItem(name, Type.BOOLEAN, value));
        } else {
          myConfigMap.put(name,
              new GenericConfigItem(name, Type.STRING, value));
        }
      }
      ourLogger.info("read config item : " + myConfigMap.get(name));
    }
  }

  public Set<String> getItems() {
    return myConfigMap.keySet();
  }

  public String getValue(String aKey) {
    return myConfigMap.get(aKey).getValue();
  }

  public String getValue(String aKey, String aDefaultValue, String aLabel,
      String aDescription) {
    GenericConfigItem item = myConfigMap.get(aKey);

    if (null == item) {
      item = new GenericConfigItem(aKey, Type.STRING, aDefaultValue);
    }
    if (null != aLabel) {
      item.setLabel(aLabel);
    }
    if (null != aDescription) {
      item.setDescription(aDescription);
    }
    storeItem(aKey, item);
    return item.getValue();
  }

  public int getIntValue(String aKey, int aDefaultValue, String aLabel,
      String aDescription) {
    GenericConfigItem item = myConfigMap.get(aKey);

    if (null == item) {
      item = new GenericConfigItem(aKey, Type.INTEGER,
          new Integer(aDefaultValue).toString());
    }
    if (null != aLabel) {
      item.setLabel(aLabel);
    }
    if (null != aDescription) {
      item.setDescription(aDescription);
    }
    if (item.getType() != Type.INTEGER) {
      throw new RuntimeException("wrong item type expected INTEGER : " + item);
    }
    storeItem(aKey, item);
    return item.getIntValue();
  }

  public int getIntValue(String aKey) {
    GenericConfigItem item = myConfigMap.get(aKey);
    if (item.getType() != Type.INTEGER) {
      throw new RuntimeException("wrong item type expected INTEGER : " + item);
    }
    return item.getIntValue();
  }

  public boolean getBooleanValue(String aKey, boolean aDefaultValue, String aLabel,
      String aDescription) {
    GenericConfigItem item = myConfigMap.get(aKey);

    if (null == item) {
      item = new GenericConfigItem(aKey, Type.BOOLEAN,
          new Boolean(aDefaultValue).toString());
    }
    if (null != aLabel) {
      item.setLabel(aLabel);
    }
    if (null != aDescription) {
      item.setDescription(aDescription);
    }
    if (item.getType() != Type.BOOLEAN) {
      throw new RuntimeException("wrong item type expected BOOLEAN : " + item);
    }
    storeItem(aKey, item);
    return item.getBooleanValue();
  }

  public boolean getBooleanValue(String aKey) {
    GenericConfigItem item = myConfigMap.get(aKey);
    if (item.getType() != Type.BOOLEAN) {
      throw new RuntimeException("wrong item type expected BOOLEAN : " + item);
    }
    return item.getBooleanValue();
  }

  public String getCipherValue(String aKey, String aDefaultValue, String aLabel,
      String aDescription) {
    GenericConfigItem item = myConfigMap.get(aKey);
    if (null == item) {
      item = new GenericConfigItem(aKey, Type.ENCRYPT, null);
      item.setValue(aDefaultValue);
    }
    if (null != aLabel) {
      item.setLabel(aLabel);
    }
    if (null != aDescription) {
      item.setDescription(aDescription);
    }
    if (item.getType() != Type.ENCRYPT) {
      throw new RuntimeException("wrong item type expected ENCRYPT : " + item);
    }
    storeItem(aKey, item);
    return item.getValue();
  }

  public String getCipherValue(String aKey) {
    GenericConfigItem item = myConfigMap.get(aKey);
    if (item.getType() != Type.ENCRYPT) {
      throw new RuntimeException("wrong item type expected ENCRYPT : " + item);
    }
    return item.getValue();
  }

  GenericConfigItem getGenericValue(String aKey) {
    return myConfigMap.get(aKey);
  }

  public void setValue(String aKey, String aValue) {
    GenericConfigItem item = myConfigMap.get(aKey);
    item.setValue(aValue);
    storeItem(aKey, item);
  }

  public void setValue(String aKey, int aValue) {
    GenericConfigItem item = myConfigMap.get(aKey);
    item.setValue(aValue);
    storeItem(aKey, item);
  }

  public void setIntValue(String aKey, int aValue) {
    GenericConfigItem item = myConfigMap.get(aKey);
    item.setValue(aValue);
    storeItem(aKey, item);
  }

  public void setValue(String aKey, boolean aValue) {
    GenericConfigItem item = myConfigMap.get(aKey);
    item.setValue(aValue);
    storeItem(aKey, item);
  }

  public void setBooleanValue(String aKey, boolean aValue) {
    GenericConfigItem item = myConfigMap.get(aKey);
    item.setValue(aValue);
    storeItem(aKey, item);
  }

  public void setCipherValue(String aKey, String aValue) {
    GenericConfigItem item = myConfigMap.get(aKey);
    item.setValue(aValue);
    storeItem(aKey, item);
  }

  void storeItem(String aKey, GenericConfigItem aValue) {
    ourLogger.log(Level.INFO, "changing config data: " + aValue);
    myConfigMap.replace(aKey, aValue);
    Element item = myXMLRootElement.getChild(aKey);
    if (null == item) {
      myConfigMap.put(aKey, aValue);
      item = new Element(aKey);
      item.setText(aValue.getXMLValue());
      item.setAttribute(new Attribute("type", aValue.getType().toString()));
      myXMLRootElement.addContent(item);
    } else {
      item.setAttribute(new Attribute("type", aValue.getType().toString()));
      item.setText(aValue.getXMLValue());
    }
  }

  public void saveConfig() {
    ourLogger.log(Level.INFO, "saveConfig");
    XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
    try {
      FileOutputStream configOut = new FileOutputStream(myConfigFile);
      out.output(myConfigDoc, configOut);
    } catch (IOException e) {
      ourLogger.log(Level.SEVERE, "problems saving XML document", e);
    }
  }

  public String getApplicationName() {
    return myApplicationName;
  }

}
