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

public class DistanceDetector extends SensorObserverAdapter implements IFlightListener {

  public DistanceDetector() {
	super();
	Flight.addFlightListener(this);
  }

  static private Logger ourLogger = Logger.getLogger(DistanceDetector.class.getName());

  DistanceHandler myHandler;
  SummaryStatistics myStatistics;
  String myUnit;

  @Override
  public Pattern getSensorNamePattern() {
	return Pattern.compile(".*distance|.*entfernung", Pattern.CASE_INSENSITIVE);
  }

  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
	myHandler = null;
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {
	ourLogger.info("" + aDescr);
	myHandler = new DistanceHandler(this, aDescr);
	addValueHandler(myHandler);
	myUnit = aDescr.getUnit();
  }

  @Override
  public void endOfLog() {
	// nothing to do
  }

  @Override
  public void flightStart() {
	myStatistics = new SummaryStatistics();
  }

  @Override
  public void flightEnd(Flight aFlight) {
	if (myStatistics.getN() > 10) {
	  aFlight.addAttribute(NLSKey.CO_GEN_DISTANCE, myUnit, new Integer((int) myStatistics.getMin()),
		  new Integer((int) myStatistics.getMax()), new Integer((int) myStatistics.getMean()), true);
	}
  }

  public void setValue(SensorValue aValue) {
	if (null == myStatistics)
	  return;
	myStatistics.addValue(aValue.getValue());
  }
}

class DistanceHandler extends SensorValueHandlerAdapter {
  DistanceDetector myDetector;

  public DistanceHandler(DistanceDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	this.myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setValue(aValue);
  }
}