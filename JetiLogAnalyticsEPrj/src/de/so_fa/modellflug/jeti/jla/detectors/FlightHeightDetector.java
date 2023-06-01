package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
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

  List<HeightHandler> myHeightHandlerList;
  String myUnit = "m";

  public FlightHeightDetector() {
	super();
	Flight.addFlightListener(this);
  }

  Pattern getSensorNamePattern() {
	// Example:
	// 000000000;4364922868;3;Rel. altit;m
	// 000000000;4199312918;5;Altitude;m
	// 000000000;4199312918;4;Rel. Altit;m
	// 000000000;4199312918;5;Altitude;m
	// 000000000;4199312918;4;Hoehe;m
	// 000000000;4199312918;5;Abs. Hoehe;m
	// this method shall return a Pattern fitting to the string in the JETI log
	// header, the sensor value to be examined
	return Pattern.compile("hoehe.*|.*alti.*de|height.*");
  }
  
  Pattern getSensorNameExclusionPattern() {
	// 000000000;4173112678;3;Hoehengew.;m
	return Pattern.compile("hoehe.*gew.*|.*height.*gain.*");
  }
  
  @Override
  public void registerSensor(SensorValueDescription aDescr) {
	Pattern p = getSensorNamePattern();
	if (null != p) {
	  Matcher m = p.matcher(aDescr.getName().toLowerCase());
	  Matcher me = getSensorNameExclusionPattern().matcher(aDescr.getName().toLowerCase());
	  if (m.matches() && !me.matches()) {
		nameMatch(aDescr);
	  }
	}
  }

  void nameMatch(SensorValueDescription aDescr) {

	ourLogger.info("adding height description: " + aDescr);
	HeightHandler handler = new HeightHandler(this, aDescr);
	addValueHandler(handler);
	if (aDescr.getUnit().equals("ft")) {
	  handler.setCorrectionFactor(0.3048f);
	}
	myHeightHandlerList.add(handler);

//	String lcname = aDescr.getName().toLowerCase();
	// ourLogger.severe("check descr: " + aDescr);
	// there are more than one sensor values fitting the height pattern.
	// absolute height: is not what we want
	// height: is what we want, but only if relative height is not exiting
	// relative height, is exactly what we, want
//	if (lcname.matches("abs.*hoehe|abs.*alti.*|abs.*height")) {
//	  // absolute height, cannot be used for flight detection
//	  ourLogger.severe("abs height: " + aDescr);
//	  HeightHandler handler = new HeightHandler(this, aDescr);
//	  addValueHandler(handler);
//	  if (aDescr.getUnit().equals("ft")) {
//		handler.setCorrectionFactor(0.3048f);
//	  }
//	  myHeightHandlerList.add(handler);
//	  return;
//	}
//	if (lcname.matches("rel.*hoehe|rel.*alti.*|rel.*height")) {
//	  // relative height, will be used if existing
//	  ourLogger.severe("rel height: " + aDescr);
//	  myHeightHandlerRelative = new HeightHandler(this, aDescr);
//	  addValueHandler(myHeightHandlerRelative);
//	  if (aDescr.getUnit().equals("ft")) {
//		myHeightHandlerRelative.setCorrectionFactor(0.3048f);
//	  }
//	  return;
//	}
//	if (lcname.matches("hoehe|alti.*|height")) {
//	  // relative height, will be used if existing
//	  ourLogger.severe("height: " + aDescr);
//	  myHeightHandler = new HeightHandler(this, aDescr);
//	  addValueHandler(myHeightHandler);
//	  if (aDescr.getUnit().equals("ft")) {
//		myHeightHandler.setCorrectionFactor(0.3048f);
//	  }
//	  return;
//	}
  }

  @Override
  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
	myHeightHandlerList = new ArrayList<HeightHandler>();
  }

  @Override
  public void endOfLog() {
  }

  @Override
  public void flightStart() {
	ourLogger.info("reset result statistic and offset values");
	for (HeightHandler handler : myHeightHandlerList) {
	  handler.reset();
	}
  }

  @Override
  public void flightEnd(Flight aFlight) {
	SummaryStatistics s = null;
	for (HeightHandler handler : myHeightHandlerList) {
	  ourLogger.info(handler.toString() + "  " + handler.getStatistics().toString());
	  if (handler.getStatistics().getN() < 100) {
		continue;
	  }
	  if (s == null) {
		ourLogger.info("first: " + handler.toString() + "  " + handler.getStatistics().toString());
		s = handler.getStatistics();
	  } else {
		float q = ((float) s.getN()) / handler.getStatistics().getN();
		if (q < 0.5f) {
		  ourLogger.info("more content: " + handler.toString() + "  " + handler.getStatistics().toString());
		  SummaryStatistics.copy(handler.getStatistics(), s);
		} else if (handler.getStatistics().getN() > 100 && handler.getStatistics().getSum() < s.getSum()) {
		  ourLogger.info("better content: " + handler.toString() + "  " + handler.getStatistics().toString());
		  SummaryStatistics.copy(handler.getStatistics(), s);
		}
	  }
	}

	if (s != null && s.getN() > 10) {
	  ourLogger.info("result : " + s.toString());
	  aFlight.addAttribute(NLSKey.CO_GEN_HEIGHT, myUnit, new Integer((int) s.getMin()), new Integer((int) s.getMax()),
		  new Integer((int) s.getMean()), true);
	}
  }
}

class HeightHandler extends SensorValueHandlerAdapter {
  private static Logger ourLogger = Logger.getLogger(HeightHandler.class.getName());
  SummaryStatistics myStatistic;

  public SummaryStatistics getStatistics() {
	if (myStatistic == null) {
	  return new SummaryStatistics();
	}
	return myStatistic;
  }

  public void reset() {
	myStatistic = null;
  }

  public HeightHandler(FlightHeightDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
  }

  @Override
  public void handle(SensorValue aValue) {
	if (null == myStatistic) {
	  myStatistic = new SummaryStatistics();
	}
	myStatistic.addValue(aValue.getValue());
	// ourLogger.severe(this + " " + aValue);
  }
}
