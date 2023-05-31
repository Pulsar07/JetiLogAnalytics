package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.IFlightListener;
import de.so_fa.modellflug.jeti.jla.datamodel.SensorAttribute;
import de.so_fa.modellflug.jeti.jla.jetilog.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValue;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValueDescription;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;

public class VoltageDetector extends SensorObserverAdapter implements IFlightListener {

  public VoltageDetector() {
	super();
	Flight.addFlightListener(this);
  }

  static private Logger ourLogger = Logger.getLogger(VoltageDetector.class.getName());

  SummaryStatistics myStatistics;
  VoltageHandler myHandler;
  String myUnit;

  @Override
  public Pattern getSensorNamePattern() {
	return Pattern.compile("u rx", Pattern.CASE_INSENSITIVE);
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {

	if (aDescr.getSensorDevice().startsWith("RxB")) {
	  // also use the B Receiver return;
	}
	ourLogger.info("add voltage handler for: " + aDescr);
	myHandler = new VoltageHandler(this, aDescr);
	addValueHandler(myHandler);
	myUnit = aDescr.getUnit();
  }

  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
	myHandler = null;
  }

  public void setValue(SensorValue aValue) {
	if (null == myStatistics)
	  return;
	myStatistics.addValue(aValue.getValue());
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
	  aFlight.addAttribute(NLSKey.CO_GEN_VOLTAGE, myUnit, new Float((float) (myStatistics.getMin())),new Float((float) (myStatistics.getMax())), new Float((float) (myStatistics.getMean())), true);
	}
  }
}

class VoltageHandler extends SensorValueHandlerAdapter {
  private VoltageDetector myDetector;

  public VoltageHandler(VoltageDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setValue(aValue);
  }
}