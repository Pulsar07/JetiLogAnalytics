package de.so_fa.modellflug.jeti.jla.detectors;

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

public class RXQDetector extends SensorObserverAdapter implements IFlightListener {

  public RXQDetector() {
	super();
	Flight.addFlightListener(this);
  }

  static private Logger ourLogger = Logger.getLogger(RXQDetector.class.getName());

  SummaryStatistics myStatistics;
  MaxSigDuraHandler myMaxSigDuraHandler;
  SigDuraHandler mySigDuraHandler;
  String myUnit;
  
  @Override
  public void registerSensor(SensorValueDescription aDescr) {
	Pattern p = getSensorNamePattern();
	if (null != p) {
	  Matcher m = p.matcher(aDescr.getName().toLowerCase());
	  if (m.matches()) {
		nameMatch(aDescr);
	  }
	}
  }
  
  Pattern getSensorNamePattern() {
	return Pattern.compile("sigdura.*");
  }

  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
	myMaxSigDuraHandler = null;
	mySigDuraHandler = null;
  }

  void nameMatch(SensorValueDescription aDescr) {

	String lcname = aDescr.getName().toLowerCase();
	if (lcname.matches(".*max")) {
	  ourLogger.info("storing max sensor value name: " + aDescr);
	  myMaxSigDuraHandler = new MaxSigDuraHandler(this, aDescr);
	  addValueHandler(myMaxSigDuraHandler);
	  myUnit = aDescr.getUnit();
	  return;
	} else {
//	  ourLogger.info("storing sensor value name: " + aDescr);
//	  mySigDuraHandler = new SigDuraHandler(this, aDescr);
//	  addValueHandler(mySigDuraHandler);
//	  return;
	}
  }
  
  public void setSigDura(SensorValue aValue) {
  }
  
  public void setSigDuraMax(SensorValue aValue) {
	if  (null == myStatistics) return;
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
	aFlight.addAttribute(NLSKey.CO_SIG_DURA, myUnit,
		null, new Integer((int) myStatistics.getMax()), null, true);
	}
  }


}

class SigDuraHandler extends SensorValueHandlerAdapter {
  private RXQDetector myDetector;

  public SigDuraHandler(RXQDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setSigDura(aValue);
  }	  
}

class MaxSigDuraHandler extends SensorValueHandlerAdapter {
  private RXQDetector myDetector;

  public MaxSigDuraHandler(RXQDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setSigDuraMax(aValue);
  }	

}
