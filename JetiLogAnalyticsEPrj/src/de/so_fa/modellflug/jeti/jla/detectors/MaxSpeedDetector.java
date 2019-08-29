package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.IFlightCreationObserver;
import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.log.SensorValue;
import de.so_fa.modellflug.jeti.jla.log.SensorValueDescription;

public class MaxSpeedDetector extends SensorObserverAdapter implements IFlightCreationObserver {

  public MaxSpeedDetector() {
	super();
	Flight.addFlightCreationObserver(this);
  }

  static private Logger ourLogger = Logger.getLogger(MaxSpeedDetector.class.getName());

  int myMaxSpeed;
  float myAvgGpsSpeed;
  float myGpsSpeedFactor;
  int myAirSpeedCnt;
  int myGpsSpeedCnt;
  float myAvgAirSpeed;
  float myAirSpeedFactor;

  @Override
  public Pattern getSensorNamePattern() {
	return Pattern.compile(".*speed");
  }

  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {

	String lcname = aDescr.getName().toLowerCase();
	if (lcname.matches("\\s*gps.*speed")) {
	  myValueDescrMap.put("GPS" + aDescr.getId(), aDescr);
	  ourLogger.info("storing sensor value name: " + aDescr);
	  myGpsSpeedFactor = 1.0f;
	  if (aDescr.getUnit().equals("m/s")) {
		myGpsSpeedFactor = 3.6f;
	  } else if (aDescr.getUnit().equals("mph")) {
		myGpsSpeedFactor = 1.609f;
	  }
	  return;
	}
	if (lcname.matches("\\s*air.*speed|\\s*pivot.*speed")) {
	  myValueDescrMap.put("AIR" + aDescr.getId(), aDescr);
	  ourLogger.info("storing sensor value name: " + aDescr);
	  myAirSpeedFactor = 1.0f;
	  if (aDescr.getUnit().equals("m/s")) {
		myAirSpeedFactor = 3.6f;
	  } else if (aDescr.getUnit().equals("mph")) {
		myAirSpeedFactor = 1.609f;
	  }
	  return;
	}
  }

  @Override
  public void valueMatch(SensorValue aValue) {
	float valueInKMH = (float) aValue.getValue();
	if (aValue.is(myValueDescrMap.get("AIR" + aValue.getSensorId()))) {
	  valueInKMH = valueInKMH * myAirSpeedFactor;
	  myAvgAirSpeed = getAvgSpeed(myAvgAirSpeed, valueInKMH, myAirSpeedCnt);
	  myAirSpeedCnt++;
	}
	if (aValue.is(myValueDescrMap.get("GPS" + aValue.getSensorId()))) {
	  valueInKMH = valueInKMH * myGpsSpeedFactor;
	  myAvgGpsSpeed = getAvgSpeed(myAvgGpsSpeed, valueInKMH, myGpsSpeedCnt);
	  myGpsSpeedCnt++;
	}
	myMaxSpeed = Math.max(myMaxSpeed, (int) valueInKMH);
	// ourLogger.severe("value: " + aValue.getValueIdx() + ":" + aValue.getValue());
  }

  float getAvgSpeed(float prev_avg, float x, int n) {
	return (prev_avg * n + x) / (n + 1);
  }

  public int getMaxSpeed() {
	return myMaxSpeed;
  }

  public int getAvgSpeed() {
	return Math.max((int) myAvgAirSpeed, (int) myAvgGpsSpeed);
  }

  @Override
  public void endOfLog() {
	// nothing to do

  }

  @Override
  public void notifyNewFlight(Flight aFlight) {
	aFlight.setMaxSpeed(getMaxSpeed());
	aFlight.setAvgSpeed(getAvgSpeed());
	myMaxSpeed = 0;
	myAvgGpsSpeed = 0.0f;
	myAvgAirSpeed = 0.0f;
	myAirSpeedCnt = myGpsSpeedCnt = 0;
  }

}
