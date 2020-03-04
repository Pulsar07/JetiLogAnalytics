package de.so_fa.modellflug.jeti.jla.jetilog;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SensorValueDescription {
  private final static Logger ourLogger = Logger.getLogger(SensorValueDescription.class.getName());

  static Map<Long, String> ourSensorDevices;
  private long myId;
  private int myIndex;
  private String myName;
  private String myUnit;

  public static void newHeader() {
	ourSensorDevices = new HashMap<Long, String>();
  }

  public SensorValueDescription(long aId, int aIdx, String aName, String aUnit) {
	myId = aId;
	myIndex = aIdx;
	myName = aName.trim();
	if (aUnit != null) {
	  myUnit = aUnit.trim();
	}
  }

  public long getId() {
	return this.myId;
  }

  public int getIndex() {
	return this.myIndex;
  }

  public String getName() {
	return this.myName;
  }

  public String getUnit() {
	return this.myUnit;
  }

  public String toString() {
	StringBuffer retVal = new StringBuffer();
	retVal.append(SensorValueDescription.class.getSimpleName());
	retVal.append("<");
	retVal.append(myName);
	retVal.append(",");
	retVal.append(myId);
	retVal.append(",");
	retVal.append(myIndex);
	retVal.append(",");
	retVal.append(myUnit);
	retVal.append(">");
	return retVal.toString();
  }

  public String getSensorDevice() {
	return SensorValueDescription.getSensorDevice(this);
  }
  
  public static String getSensorDevice(SensorValueDescription aDescr) {
	return ourSensorDevices.get(aDescr.myId);
  }

  public static void updateSensorDevice(SensorValueDescription aDescr) {
	// this is the one of sometimes more lines for a sensor device
	String sensorDevice = SensorValueDescription.getSensorDevice(aDescr);
	if (sensorDevice == null) {
	  ourSensorDevices.put(aDescr.getId(), aDescr.getName());
	  ourLogger.info("new sensor device:" + aDescr.getId() + ":" + ourSensorDevices.get(aDescr.getId()));
	} else {
	  ourSensorDevices.put(aDescr.getId(), sensorDevice + " " + aDescr.getName());
	  ourLogger.info("extended sensor device:" + aDescr.getId() + ":" + ourSensorDevices.get(aDescr.getId()));
	}
  }
}
