package de.so_fa.utils.config;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class GenericConfigItem {
  private static Logger ourLogger = Logger
      .getLogger(GenericConfigItem.class.getName());
  private static SecretKeySpec ourCipherKey;

  static {
    ourCipherKey = getAESKey("SoFa-key-1992");
  }

  enum Type {
    STRING, INTEGER, ENCRYPT, BOOLEAN
  }

  private Type myType;
  private String myKey;
  private String myLabel;

  private String myDescription;
  private String myValue; // decrypted value
  private String myEncryptedValue;

  public static SecretKeySpec getAESKey(String aPassphrase) {
    SecretKeySpec secretKeySpec = null;
    try {
      String keyStr = aPassphrase;
      // byte-Array erzeugen
      byte[] key;
      key = (keyStr).getBytes("UTF-8");
      // aus dem Array einen Hash-Wert erzeugen mit MD5 oder SHA
      MessageDigest sha = MessageDigest.getInstance("SHA-256");
      key = sha.digest(key);
      // nur die ersten 128 bit nutzen
      key = Arrays.copyOf(key, 16);
      // der fertige Schluessel
      secretKeySpec = new SecretKeySpec(key, "AES");
      return secretKeySpec;
    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
    }
    return secretKeySpec;
  }

  public String getKey() {
    return myKey;
  }

  public void setKey(String aKey) {
    myKey = aKey;
  }

  public GenericConfigItem(String aKeyName, Type aType, String aRawValue) {
    myKey = aKeyName;
    myType = aType;
    if (myType == Type.ENCRYPT) {
      if (null != aRawValue) {
        myEncryptedValue = aRawValue;
        myValue = decryptValue(myEncryptedValue);
      }
    } else {
      myValue = aRawValue;
    }
    ourLogger.info("new item: " + this);
  }

  public void setValue(String aValue) {
    myValue = aValue;
    if (myType == Type.ENCRYPT) {
      myEncryptedValue = encyptValue(aValue);
      ourLogger.info("setValue: " + this + "/" + myEncryptedValue);
    } else {
      ourLogger.info("setValue: " + this);
    }
  }

  public void setValue(int aValue) {
    if (Type.INTEGER != myType) {
      throw new RuntimeException("conversion error " + this);
    }
    setValue(new Integer(aValue).toString());
  }
  
  public void setValue(boolean aValue) {
    if (Type.BOOLEAN != myType) {
      throw new RuntimeException("conversion error " + this);
    }
    setValue(new Boolean(aValue).toString());
  }


  private String encyptValue(String aValue) {
    String retVal = null;
    try {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, ourCipherKey);
      byte[] encrypted = cipher.doFinal(aValue.getBytes());

      // bytes zu Base64-String konvertieren (dient der Lesbarkeit)
      Base64.Encoder myEncoder = Base64.getEncoder();
      retVal = myEncoder.encodeToString(encrypted);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException
        | InvalidKeyException | IllegalBlockSizeException
        | BadPaddingException e) {
      ourLogger.severe("cannot encrypt value for : " + this + ", reason :" + e);
      throw new RuntimeException("cannot encrypt value for : " + this);

    }
    return retVal;
  }

  private String decryptValue(String aValue) {
    String retVal = null;
    try {
      // BASE64 String zu Byte-Array konvertieren
      Base64.Decoder myDecoder = Base64.getDecoder();
      byte[] crypted = myDecoder.decode(aValue);

      // Entschluesseln
      Cipher cipher;
      cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, ourCipherKey);
      byte[] cipherData = cipher.doFinal(crypted);
      retVal = new String(cipherData);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException
        | InvalidKeyException | IllegalBlockSizeException
        | BadPaddingException e) {
      ourLogger.severe("cannot decrypt value for : " + this + ", reason :" + e);
      throw new RuntimeException("cannot decrypt value for : " + this);
    }
    return retVal;

  }

  public String getValue() {
    return myValue;
  }

  public String getXMLValue() {
    String retVal = myValue;
    if (Type.ENCRYPT == myType) {
      retVal = myEncryptedValue;
    }
    return retVal;
  }

  public boolean getBooleanValue() {
    if (Type.BOOLEAN != myType) {
      throw new RuntimeException("conversion error " + this);
    }
    return new Boolean(myValue).booleanValue();    
  }
  
  public int getIntValue() {
    if (Type.INTEGER != myType) {
      throw new RuntimeException("conversion error " + this);
    }
    return new Integer(myValue).intValue();
  }

  public Type getType() {
    return myType;
  }

  public String getLabel() {
    return myLabel;
  }

  public String getDescription() {
    return myDescription;
  }

  public void setLabel(String aLabel) {
    myLabel = aLabel;
  }

  public void setDescription(String aDescription) {
    myDescription = aDescription;
  }

  public boolean equals(Object aValue) {
    if (aValue instanceof GenericConfigItem) {
      GenericConfigItem val = (GenericConfigItem) aValue;
      return (myKey == val.myKey && myType == val.myType
          && myValue.equals(val.myValue));
    } else {
      return false;
    }
  }
  
  public boolean check() {
    return ( null != myValue && null != myKey && null != myType && null != myDescription && null != myLabel); 
  }

  public String toString() {
    String val = myValue;
    if (Type.ENCRYPT == getType()) {
      val = "****";
    }
    return myKey + "[" + myType + "]" + "=" + val + "/" + myEncryptedValue + "/" + myLabel + "/" + myDescription ;
  }

}
