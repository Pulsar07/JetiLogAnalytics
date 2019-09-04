package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.log.SensorValue;
import de.so_fa.modellflug.jeti.jla.log.SensorValueDescription;

public class SpeedDetector extends SensorObserverAdapter implements IFlightListener {

  public SpeedDetector() {
	super();
	Flight.addFlightListener(this);
  }

  static private Logger ourLogger = Logger.getLogger(SpeedDetector.class.getName());

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
	System.out.println("new VDistro");

	myVDistro = new int[300];
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {

	String lcname = aDescr.getName().toLowerCase();
	if (lcname.matches("gps.*speed")) {
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
	if (lcname.matches("air.*speed|pivot.*speed")) {
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
	  // ourLogger.severe("air speed: " + valueInKMH);
	}
	if (aValue.is(myValueDescrMap.get("GPS" + aValue.getSensorId()))) {
	  valueInKMH = valueInKMH * myGpsSpeedFactor;
	  myAvgGpsSpeed = getAvgSpeed(myAvgGpsSpeed, valueInKMH, myGpsSpeedCnt);
	  myGpsSpeedCnt++;
	  // ourLogger.severe("gps speed: " + valueInKMH);
	}
	setVDistro((int) valueInKMH);
	myMaxSpeed = Math.max(myMaxSpeed, (int) valueInKMH);
	// ourLogger.severe("value: " + aValue.getValueIdx() + ":" + aValue.getValue());
  }

  
  private int[] myVDistro;
  private void setVDistro(int aValueInKMH) {
	if (aValueInKMH > 0 && aValueInKMH < 300) {
	  myVDistro[aValueInKMH]++;
	}
  }
  
  private int getVDistroMax() {
	int retVal = 0;
	int maxVal=0;
	for (int i=0; i<myVDistro.length; i++) {
	  if (maxVal < myVDistro[i]) {
		maxVal = myVDistro[i];
		retVal = i;
	  }
//	  if (i > 20 && i < 100) {
//		System.out.println("V["+ i+"] = " + myVDistro[i]);
//	  }
	}
//	System.out.println("Max = " + retVal);
	return retVal;
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
  public void flightStart() {
	// TODO Auto-generated method stub
	
  }

  @Override
  public void flightEnd(Flight aFlight) {
	aFlight.setMaxSpeed(getMaxSpeed());
	aFlight.setAvgSpeed(getAvgSpeed());
	aFlight.setVnorm(getVDistroMax());
	myMaxSpeed = 0;
	myAvgGpsSpeed = 0.0f;
	myAvgAirSpeed = 0.0f;
	myAirSpeedCnt = myGpsSpeedCnt = 0;
	// System.out.println("new VDistro");
	myVDistro = new int[300];

  }

}
