package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.Model;
import de.so_fa.modellflug.jeti.jla.log.SensorValue;
import de.so_fa.modellflug.jeti.jla.log.SensorValueDescription;

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

  // # Radical
  // 000000000;4291922503;0;Tx;
  // 000000000;4379323544;0;Rx ;
  // 000000000;4379323544;1;U Rx;V
  // 000000000;4379323544;2;A1;
  // 000000000;4379323544;3;A2;
  // 000000000;4379323544;4;Q;%
  // 000000000;4199312918;0;VarioGPS;

  @Override
  public Pattern getSensorNamePattern() {
	return Pattern.compile("A[1|2]", Pattern.CASE_INSENSITIVE);
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {
	// ourLogger.severe("" + aDescr.getName());
	if (aDescr.getName().equals("A1")) {
	  myValueDescrMap.put("A1", aDescr);
	}

	if (aDescr.getName().equals("A2")) {
	  myValueDescrMap.put("A2", aDescr);
	}

  }

  public FlightDetectorSignalStrength() {
	super();
//	myFlightListeners = new ArrayList<IFlightListener>();
  }

  @Override
  public void valueMatch(SensorValue aValue) {
	// ourLogger.severe("");
	myCurrentTimestamp = aValue.getTime();
	if (aValue.is(myValueDescrMap.get("A1"))) {
	  mySignalStrength_A1 = (int) aValue.getValue();
	}
	if (aValue.is(myValueDescrMap.get("A2"))) {
	  mySignalStrength_A2 = (int) aValue.getValue();
	}
	flightDetectionBySignalStrength();
  }

  private void flightDetectionBySignalStrength() {
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
	mySmoothedSignalStrength = strength + 0.96 * (mySmoothedSignalStrength - strength);

	if (mySmoothedSignalStrength < 17 && myFlightDetect == false) {
	  // ourLogger.severe(" signal strenght flight ON: " + mySmoothedSignalStrength +
	  // " at: "
	  // + TimeDuration.getString(myCurrentTimestamp / 1000));
	  flightDetection(myCurrentTimestamp, FlightState.FLIGHT, false);
	}
	if (mySmoothedSignalStrength > 17.9 && myFlightDetect == true) {
	  // ourLogger.severe(" signal strenght flight OFF: " + mySmoothedSignalStrength +
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
//	  for (IFlightListener listener : myFlightListeners) {
//		listener.flightStart();
//	  }
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
		  myLogData.getLogTime().plusSeconds(myStartTS / 1000), duration);
//	  for (IFlightListener listener : myFlightListeners) {
//		listener.flightEnd(f);
//	  }
	  // ourLogger.severe(myLogData.getLogName() + " : flight detected: " + f);
	  // ourLogger.severe("new flight\n at: " +
	  // myLogData.getLogTime().plusSeconds(myStartTS / 1000) + "\n dura: " + duration
	  // );
	}

  }

  @Override
  public void endOfLog() {
	flightDetection(myCurrentTimestamp, FlightState.NOFLIGHT, true);
  }
}
