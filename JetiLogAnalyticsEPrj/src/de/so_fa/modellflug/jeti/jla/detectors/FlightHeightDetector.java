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

public class FlightHeightDetector extends SensorObserverAdapter implements IFlightListener {
  private static Logger ourLogger = Logger.getLogger(FlightHeightDetector.class.getName());

  private float myCorrectionFactor;
  HeightHandler myHeightHandler;
  SummaryStatistics myStatistics;
  String myUnit = "m";

  public FlightHeightDetector() {
	super();
	Flight.addFlightListener(this);
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
	return Pattern.compile(".*hoehe.*|.*alti.*|.*height.*");
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {

	myCorrectionFactor = 1.0f;
	String lcname = aDescr.getName().toLowerCase();
	// ourLogger.severe("check descr: " + aDescr);
	// there are more than one sensor values fitting the height pattern.
	// absolute height: is not what we want
	// height: is what we want, but only if relative height is not exiting
	// relative height, is exactly what we, want
	if (lcname.matches("abs.*hoehe|abs.*alti.*|abs.*height")) {
	  // absolute height, cannot be used for flight detection
	  return;
	}
	if (lcname.matches("rel.*hoehe|rel.*alti.*|rel.*height")) {
	  // relative height, will be used if existing
	  ourLogger.info("rel height: " + aDescr);
	  myHeightHandler = new HeightHandler(this, aDescr);
	  addValueHandler(myHeightHandler);
	  if (aDescr.getUnit().equals("ft")) {
		myCorrectionFactor = 0.3048f;
	  }
	  return;
	}
	if (lcname.matches("hoehe|alti.*|height")) {
	  // height, will be used if existing, if relative is not exiting
	  if (myHeightHandler == null) {
		ourLogger.info("height: " + aDescr);
		myHeightHandler = new HeightHandler(this, aDescr);
		addValueHandler(myHeightHandler);
		if (aDescr.getUnit().equals("ft")) {
		  myCorrectionFactor = 0.3048f;
		}
	  }
	  return;
	}
  }

  @Override
  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
	myHeightHandler = null;
  }

  @Override
  public void endOfLog() {
  }

  @Override
  public void flightStart() {
	myStatistics = new SummaryStatistics();
  }

  @Override
  public void flightEnd(Flight aFlight) {
	if (myStatistics.getN() > 10) {
	  aFlight.addAttribute(NLSKey.CO_GEN_HEIGHT, myUnit, new Integer((int) myStatistics.getMin()),
		  new Integer((int) myStatistics.getMax()), new Integer((int) myStatistics.getMean()), true);
	}
  }

  public void setHeight(SensorValue aValue) {
	if (null == myStatistics)
	  return;
	myStatistics.addValue(aValue.getValue() * myCorrectionFactor);
  }

}

class HeightHandler extends SensorValueHandlerAdapter {
  private FlightHeightDetector myDetector;

  public HeightHandler(FlightHeightDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setHeight(aValue);
  }
}
