package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.IFlightListener;
import de.so_fa.modellflug.jeti.jla.jetilog.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValue;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValueDescription;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;

public class SpeedDetector extends SensorObserverAdapter implements IFlightListener {

  public SpeedDetector() {
	super();
	Flight.addFlightListener(this);
  }

  static private Logger ourLogger = Logger.getLogger(SpeedDetector.class.getName());

  float myGpsSpeedFactor;
  float myAirSpeedFactor;
  GpsSpeedHandler myGpsSpeedHandler;
  AirSpeedHandler myAirSpeedHandler;
  SummaryStatistics myGpsSpeedStatistics;
  SummaryStatistics myAirSpeedStatistics;
  String myUnit = "km/h";

  @Override
  public Pattern getSensorNamePattern() {
	return Pattern.compile(".*speed");
  }

  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
	myGpsSpeedHandler = null;
	myAirSpeedHandler = null;
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {

	String lcname = aDescr.getName().toLowerCase();
	if (lcname.matches("gps.*speed")) {
	  ourLogger.info("" + aDescr);
	  myGpsSpeedHandler = new GpsSpeedHandler(this, aDescr);
	  addValueHandler(myGpsSpeedHandler);
	  myGpsSpeedFactor = 1.0f;
	  if (aDescr.getUnit().equals("m/s")) {
		myGpsSpeedFactor = 3.6f;
	  } else if (aDescr.getUnit().equals("mph")) {
		myGpsSpeedFactor = 1.609f;
	  }
	  return;
	}
	if (lcname.matches("air.*speed|pivot.*speed")) {
	  ourLogger.info("" + aDescr);
	  myAirSpeedHandler = new AirSpeedHandler(this, aDescr);
	  addValueHandler(myAirSpeedHandler);
	  myAirSpeedFactor = 1.0f;
	  if (aDescr.getUnit().equals("m/s")) {
		myAirSpeedFactor = 3.6f;
	  } else if (aDescr.getUnit().equals("mph")) {
		myAirSpeedFactor = 1.609f;
	  }
	  return;
	}
  }

  float getAvgValue(float aPreviousAvgValue, float aNewValue, int aValueCnt) {
	return (aPreviousAvgValue * aValueCnt + aNewValue) / (aValueCnt + 1);
  }

  @Override
  public void endOfLog() {
	// nothing to do

  }

  @Override
  public void flightStart() {
	myGpsSpeedStatistics = new SummaryStatistics();
	myAirSpeedStatistics = new SummaryStatistics();
  }

  @Override
  public void flightEnd(Flight aFlight) {
	ourLogger.info("stats:" + myGpsSpeedStatistics);

	if (myGpsSpeedStatistics.getN() > 10) {
	  aFlight.addAttribute(NLSKey.CO_GEN_GPS_SPEED, myUnit, new Integer((int) myGpsSpeedStatistics.getMin()),
		  new Integer((int) myGpsSpeedStatistics.getMax()), new Integer((int) myGpsSpeedStatistics.getMean()), true);
	}
	if (myAirSpeedStatistics.getN() > 10) {
	  aFlight.addAttribute(NLSKey.CO_GEN_AIR_SPEED, myUnit, new Integer((int) myAirSpeedStatistics.getMin()),
		  new Integer((int) myAirSpeedStatistics.getMax()), new Integer((int) myAirSpeedStatistics.getMean()), true);
	}
  }

  public void setAirSpeedValue(SensorValue aValue) {
	if (null == myAirSpeedStatistics)
	  return;
	myAirSpeedStatistics.addValue(aValue.getValue() * myAirSpeedFactor);
  }

  public void setGpsSpeedValue(SensorValue aValue) {
	if (null == myGpsSpeedStatistics)
	  return;
	myGpsSpeedStatistics.addValue(aValue.getValue() * myGpsSpeedFactor);
  }
}

class AirSpeedHandler extends SensorValueHandlerAdapter {
  // static private Logger ourLogger =
  // Logger.getLogger(AirSpeedHandler.class.getName());

  SpeedDetector myDetector;

  public AirSpeedHandler(SpeedDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	this.myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
//		ourLogger.severe("");
	myDetector.setAirSpeedValue(aValue);
  }
}

class GpsSpeedHandler extends SensorValueHandlerAdapter {
  SpeedDetector myDetector;

  public GpsSpeedHandler(SpeedDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	this.myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setGpsSpeedValue(aValue);
  }
}