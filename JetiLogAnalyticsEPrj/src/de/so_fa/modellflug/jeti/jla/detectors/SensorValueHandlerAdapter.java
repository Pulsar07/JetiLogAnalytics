package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.so_fa.modellflug.jeti.jla.jetilog.SensorValueDescription;

public abstract class SensorValueHandlerAdapter implements ISensorValueHandler {

  List<SensorValueDescription> myValueDescrList = new ArrayList<SensorValueDescription>();

  public SensorValueHandlerAdapter() {
	super();
  }

  public SensorValueHandlerAdapter(SensorValueDescription aDescr) {
	super();
	addSensorDescr(aDescr);
  }

  @Override
  public Collection<SensorValueDescription> getSensorDescr() {
	return myValueDescrList;
  }

  public void addSensorDescr(SensorValueDescription aDescr) {
	myValueDescrList.add(aDescr);
  }

}
