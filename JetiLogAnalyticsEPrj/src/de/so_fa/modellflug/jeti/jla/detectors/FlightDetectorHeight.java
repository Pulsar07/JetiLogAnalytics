package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.IFlightCreationObserver;
import de.so_fa.modellflug.jeti.jla.datamodel.Model;
import de.so_fa.modellflug.jeti.jla.datamodel.Flight.FlightDetection;
import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.log.SensorValue;
import de.so_fa.modellflug.jeti.jla.log.SensorValueDescription;

public class FlightDetectorHeight implements ISensorObserver, IFlightDetector, IFlightCreationObserver {
  private static Logger ourLogger = Logger.getLogger(FlightDetectorHeight.class.getName());

  final static double NOFLIGHT_HEIGHT_LIMIT = 10.0;

  private static final double MAX_VALID_START_HEIGHT = 200;
  private static final double MAX_VALID_HEIGHT = 1499;
  private Map<Long, SensorValueDescription> myValueDescription;
  private float myCorrectionFactor;

  public FlightDetectorHeight() {
	super();
	Flight.addFlightCreationObserver(this);
  }

  @Override
  public Pattern getSensorNamePattern() {
	// Example:
	// 000000000;4364922868;3;Rel. altit;m
	// 000000000;4199312918;5;Altitude;m
	// 000000000;4199312918;4;Rel. Altit;m
	// 000000000;4199312918;5;Altitude;m
	// 000000000;4199312918;4;Hoehe;m
	// 000000000;4199312918;5;Abs. Hoehe;m
	// this method shall return a Pattern fitting to the string in the JETI log
	// header, the sensor value to be examined

	return Pattern.compile(".*hoehe|.*alti.*|.*height");
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {
	myCorrectionFactor = 1.0f;
	String lcname = aDescr.getName().toLowerCase();
	// ourLogger.severe("check descr: " + aDescr);
	// there are more than one sensor values fitting the height pattern.
	// absolute height: is not what we want
	// height: is what we want, but only if relatvie height is not exiting
	// relative height, is exactly what we, want
	if (lcname.matches("\\s*abs.*hoehe|\\s*abs.*alti.*|\\s*abs.*height")) {
	  // absolute height, cannot be used for flight detection
	  return;
	}
	if (lcname.matches("\\s*rel.*hoehe|\\s*rel.*alti.*|\\s*rel.*height")) {
	  // relative height, will be used if existing
	  myValueDescription.put(aDescr.getId(), aDescr);
	  if (aDescr.getUnit().equals("ft")) {
		myCorrectionFactor = 0.3048f;
	  } 
	  return;
	}

	if (lcname.matches("\\s*hoehe|\\s*alti.*|\\s*height")) {
	  // height, will be used if existing, if relative is not exiting
	  if (myValueDescription.containsKey(aDescr.getId()) && myValueDescription.get(aDescr.getId()).getName()
		  .toLowerCase().matches("\\s*rel.*hoehe|\\s*rel.*alti.*|\\s*rel.*height")) {
		return;
	  }
	  if (myValueDescription.containsKey(aDescr.getId())
		  && myValueDescription.get(aDescr.getId()).getName().toLowerCase().matches("\\s*hoehe|\\s*height")) {
		return;
	  }
	  myValueDescription.put(aDescr.getId(), aDescr);
	  if (aDescr.getUnit().equals("ft")) {
		myCorrectionFactor = 0.3048f;
	  } 
	  return;
	}
  }

  public String toString() {
	StringBuffer retVal = new StringBuffer();
	retVal.append(FlightDetectorHeight.class.getSimpleName());
	retVal.append("<");
	for (SensorValueDescription descr : myValueDescription.values()) {
	  retVal.append(descr);
	  retVal.append(",");
	}
	retVal.deleteCharAt(retVal.length() - 1);
	retVal.append(">");
	return retVal.toString();
  }

  @Override
  public void newLogData(JetiLogDataScanner aLogData) {
	myValueDescription = new HashMap<Long, SensorValueDescription>();
	myStartTimestamp = 0;
	myEndTimestamp = 0;
  }

  @Override
  public Collection<SensorValueDescription> getSensorDescr() {
	return myValueDescription.values();
  }

  long myCurrentTimestamp;
  long myStartTimestamp;
  long myEndTimestamp;
  long myNoFlightTimestamp;
  int myMaxHeight;
  int myFlightCounter = 0;
  int myFlightDurationSum = 0;

  @Override
  public void valueMatch(SensorValue aValue) {
	double height = aValue.getValue() * myCorrectionFactor;
	myCurrentTimestamp = aValue.getTime();

	// ignore invalid values while initialization of sensors
	if (height > MAX_VALID_START_HEIGHT && myCurrentTimestamp < 20000) {
	  height = 0;
	}
	if (height > MAX_VALID_HEIGHT) {
	  height = 0;
	}

	// start condition to count flight time
	if (myStartTimestamp == 0 && height > NOFLIGHT_HEIGHT_LIMIT) {
//		  ourLogger.severe("start detected :" + myStartTimestamp + " height " + height);
	  myStartTimestamp = myCurrentTimestamp;
	  myNoFlightTimestamp = 0;
	  myMaxHeight = 0;
	}

	// if height gets to low, start testing if flight has ended
	if (height < NOFLIGHT_HEIGHT_LIMIT) {
	  if (myNoFlightTimestamp == 0) {
		myNoFlightTimestamp = myCurrentTimestamp;
	  }

	  if (NOFLIGHT_TIME_RANGE_LIMIT_IN_MS < (myCurrentTimestamp - myNoFlightTimestamp)) {
		finalizeFlight(false);
	  }

	} else {
	  myNoFlightTimestamp = 0;
	  if (height > myMaxHeight) {
		myMaxHeight = (int) height;
	  }
	}

	return;

  }

  private void finalizeFlight(boolean aIsFinalCall) {

	// ourLogger.severe("" + aIsFinalCall + " " + myCurrentTimestamp);
	if (myStartTimestamp != 0) {

	  if (aIsFinalCall) {
		myEndTimestamp = myCurrentTimestamp;
	  } else {
		myEndTimestamp = myNoFlightTimestamp;
	  }

	  myFlightCounter++;
	  int flightDuration = (int) (myEndTimestamp - myStartTimestamp) / 1000;

	  myFlightDurationSum += flightDuration;
	  myStartTimestamp = 0;
	}
  }

  @Override
  public void endOfLog() {
	finalizeFlight(true);
  }

  @Override
  public void notifyNewFlight(Flight aFlight) {
	aFlight.setMaxHeight(myMaxHeight);
	myMaxHeight = 0;
  }

}
