package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.so_fa.modellflug.jeti.jla.jetilog.SensorValue;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValueDescription;

public abstract class SensorValueHandlerAdapter implements ISensorValueHandler {

  List<SensorValueDescription> myValueDescrList = new ArrayList<SensorValueDescription>();
  float myCorrectionFactor=1.0f;

  public float getCorrectionFactor() {
    return this.myCorrectionFactor;
  }

  public void setCorrectionFactor(float aCorrectionFactor) {
    this.myCorrectionFactor = aCorrectionFactor;
  }

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
  
  public SensorValueDescription getSensorDescr(SensorValue aValue) {
	for (SensorValueDescription descr: myValueDescrList) {
	  if (aValue.is(descr)) {
		return descr;
	  }
	}
	return null;
  }


  public void addSensorDescr(SensorValueDescription aDescr) {
	myValueDescrList.add(aDescr);
  }
  public String toString() {
	return this.getClass().getSimpleName() + ":" + myValueDescrList;
  }

}
