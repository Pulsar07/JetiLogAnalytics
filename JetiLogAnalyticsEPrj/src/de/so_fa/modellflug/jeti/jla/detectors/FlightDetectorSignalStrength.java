package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.Model;
import de.so_fa.modellflug.jeti.jla.jetilog.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValue;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValueDescription;

public class FlightDetectorSignalStrength extends SensorObserverAdapter implements IFlightDetector {
  private static Logger ourLogger = Logger.getLogger(FlightDetectorSignalStrength.class.getName());
//  private List<IFlightListener> myFlightListeners;

  private enum FlightState {
	FLIGHT, NOFLIGHT
  }

  int mySignalStrength_A1 = -10;
  int mySignalStrength_A2 = -10;
  double mySmoothedSignalStrength = 18;
  boolean myFlightDetect = false;
  long myStartTS = 0;
  long myEndTS = 0;
  long myNoFlightTS = 0;

  private long myCurrentTimestamp;
  private A1Handler myA1Handler;
  private A2Handler myA2Handler;
  static float ourSensitivityCorrectionFactor = 0.0f;
  private static float ourSmoothFactor;
  private static float ourLevelSignalSumStrength;
	
  // # Radical
  // 000000000;4291922503;0;Tx;
  // 000000000;4379323544;0;Rx ;
  // 000000000;4379323544;1;U Rx;V
  // 000000000;4379323544;2;A1;
  // 000000000;4379323544;3;A2;
  // 000000000;4379323544;4;Q;%
  // 000000000;4199312918;0;VarioGPS;

  private void init() {
	mySignalStrength_A1 = -10;
	mySignalStrength_A2 = -10;
	mySmoothedSignalStrength = 18;
	myFlightDetect = false;
	myStartTS = 0;
	myEndTS = 0;
	myNoFlightTS = 0;
  }

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
	return Pattern.compile("A[1|2]", Pattern.CASE_INSENSITIVE);
  }

  void nameMatch(SensorValueDescription aDescr) {
	// this is called each time a new log file is scanned, so force initialization
	// of local variables
	init();
	
	if (aDescr.getName().equals("A1")) {
	  myA1Handler = new A1Handler(this, aDescr);
	  addValueHandler(myA1Handler);
	}

	if (aDescr.getName().equals("A2")) {
	  myA2Handler = new A2Handler(this, aDescr);
	  addValueHandler(myA2Handler);
	}

  }

  public FlightDetectorSignalStrength() {
	super();
	setDetectionStrengthSensitivityVaule(ourSensitivityCorrectionFactor);
  }


  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
	myA1Handler = null;
	myA2Handler = null;
  }
  
  void flightDetectionBySignalStrength() {

	double strength = mySignalStrength_A1 + mySignalStrength_A2;
	if (strength < 0) {
	  // ignore values if not all values are read
	  return;
	}	

	// Vario Filter
	// IIR Low Pass Filter
	// y[i] := α * x[i] + (1-α) * y[i-1]
	// := α * x[i] + (1 * y[i-1]) - (α * y[i-1])
	// := α * x[i] + y[i-1] - α * y[i-1]
	// := α * ( x[i] - y[i-1]) + y[i-1]
	// := y[i-1] + α * (x[i] - y[i-1])
	// mit α = 1- β
	// := y[i-1] + (1-ß) * (x[i] - y[i-1])
	// := y[i-1] + 1 * (x[i] - y[i-1]) - ß * (x[i] - y[i-1])
	// := y[i-1] + x[i] - y[i-1] - ß * x[i] + ß * y[i-1]
	// := x[i] - ß * x[i] + ß * y[i-1]
	// := x[i] + ß * y[i-1] - ß * x[i]
	// := x[i] + ß * (y[i-1] - x[i])
	// see:
	// https://en.wikipedia.org/wiki/Low-pass_filter#Simple_infinite_impulse_response_filter
	mySmoothedSignalStrength = strength + ourSmoothFactor * (mySmoothedSignalStrength - strength);
//	 ourLogger.severe(" signal strength: " + mySmoothedSignalStrength +
//		   " at: "
//		   + TimeDuration.getString(myCurrentTimestamp / 1000));
	if (mySmoothedSignalStrength < ourLevelSignalSumStrength && myFlightDetect == false) {
//	   ourLogger.severe(" signal strength flight ON: " + mySmoothedSignalStrength +
//	   " at: "
//	   + TimeDuration.getString(myCurrentTimestamp / 1000));
	  flightDetection(myCurrentTimestamp, FlightState.FLIGHT, false);
	}
	if (mySmoothedSignalStrength > 17.9 && myFlightDetect == true) {
	  // ourLogger.severe(" signal strength flight OFF: " + mySmoothedSignalStrength +
	  // " at: "
	  // + TimeDuration.getString(myCurrentTimestamp / 1000));
	  flightDetection(myCurrentTimestamp, FlightState.NOFLIGHT, false);

	}
  }

  private void flightDetection(long aTS, FlightState aNewState, boolean aEndFlightImmediately) {
	if (aNewState == FlightState.FLIGHT && myFlightDetect == false) {
	  // flight start conditions are valid
	  myFlightDetect = true;
	  myStartTS = aTS;
	  Flight.potentialFlightStart();
	}
	if (aNewState == FlightState.FLIGHT) {
	  myEndTS = myNoFlightTS = 0;
	}
	if (aNewState == FlightState.NOFLIGHT && myFlightDetect == true) {
	  // flight end conditions are valid
	  if (aEndFlightImmediately) {
		myEndTS = aTS;
	  } else {
		if (myEndTS == 0) {
		  myEndTS = aTS;
		}
		if ((aTS - myEndTS) < 20000) {
		  return;
		}
		myFlightDetect = false;
	  }
	  int duration = (int) (myEndTS - myStartTS) / 1000;
	  if (duration < NOFLIGHT_TIME_RANGE_LIMIT_IN_MS / 1000) {
		return;
	  }

	  Flight f = Model.get(myLogData.getModelName()).addFlight(Flight.FlightDetection.SIGNAL,
		  myLogData.getLogTime().plusSeconds(myStartTS / 1000), duration, myLogData);
	}

  }

  @Override
  public void endOfLog() {
	flightDetection(myCurrentTimestamp, FlightState.NOFLIGHT, true);
  }

  public void setA1(SensorValue aValue) {
	myCurrentTimestamp = aValue.getTime();
	mySignalStrength_A1 = (int) aValue.getValue();
	flightDetectionBySignalStrength();
  }

  public void setA2(SensorValue aValue) {
	myCurrentTimestamp = aValue.getTime();
	mySignalStrength_A2 = (int) aValue.getValue();
	flightDetectionBySignalStrength();
  }
  
  public static void setDetectionStrengthSensitivityVaule(float aValue) {
	ourSensitivityCorrectionFactor = aValue;
	ourSmoothFactor = 0.96f - 0.039f*ourSensitivityCorrectionFactor;
	ourLevelSignalSumStrength = 15.0f + 2.6f * ourSensitivityCorrectionFactor;

	ourLogger.info("flight detection factors: " + ourSensitivityCorrectionFactor + "/" + ourSmoothFactor + "/" + ourLevelSignalSumStrength);
  }
}

class A1Handler extends SensorValueHandlerAdapter {
  static private Logger ourLogger = Logger.getLogger(A1Handler.class.getName());

  FlightDetectorSignalStrength myDetector;

  public A1Handler(FlightDetectorSignalStrength aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	this.myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setA1(aValue);
  }
}

class A2Handler extends SensorValueHandlerAdapter {
  static private Logger ourLogger = Logger.getLogger(A2Handler.class.getName());

  FlightDetectorSignalStrength myDetector;

  public A2Handler(FlightDetectorSignalStrength aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	this.myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setA2(aValue);
  }
}
