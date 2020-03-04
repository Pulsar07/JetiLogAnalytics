package de.so_fa.modellflug.jeti.jla.datamodel;

import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;

public class SensorAttribute {
  NLSKey myNameKey;
  String myUnit;
  Object myMinValue = null;
  Object myMaxValue = null;
  Object myAvgValue = null;
  boolean myDoAddToModel = false;

  public SensorAttribute(NLSKey aName, String aUnit, Object aMinValue, Object aMaxValue, Object aAvgValue, boolean aAddToModel) {
	myNameKey = aName;
	myUnit = aUnit;
	myMinValue = aMinValue;
	myMaxValue = aMaxValue;
	myAvgValue = aAvgValue;
	myDoAddToModel = aAddToModel;
  }

  public SensorAttribute(SensorAttribute aAttr) {
	myNameKey = aAttr.myNameKey;
	myUnit = aAttr.myUnit;
	myMinValue = aAttr.myMinValue;
	myMaxValue = aAttr.myMaxValue;
	myAvgValue = aAttr.myAvgValue;
	myDoAddToModel = aAttr.myDoAddToModel;
  }

  public String getUnit() {
	return myUnit;
  }
  
  public boolean getDoAddToModel() {
	return myDoAddToModel;
  }

  public NLSKey getNameKey() {
	return myNameKey;
  }

  public Object getMinValue() {
	return myMinValue;
  }

  public Object getMaxValue() {
	return myMaxValue;
  }

  public Object getAvgValue() {
	return myAvgValue;
  }

  public String getName() {
	StringBuffer descrBuf = new StringBuffer();
	if (myMinValue != null) {
	  descrBuf.append(NLS.get(NLSKey.CO_GEN_MIN));
	  descrBuf.append("/");
	}
	if (myMaxValue != null) {
	  descrBuf.append(NLS.get(NLSKey.CO_GEN_MAX));
	  descrBuf.append("/");
	}
	if (myAvgValue != null) {
	  descrBuf.append(NLS.get(NLSKey.CO_GEN_AVG));
	  descrBuf.append("/");

	}
	descrBuf.setLength(descrBuf.length() - 1);

	String retVal = NLS.get(myNameKey);
	retVal = NLS.get(myNameKey) + " (" + descrBuf.toString() + ")";
	return retVal;
  }

  public String getValueString() {
	StringBuffer valBuf = new StringBuffer();
	if (myMinValue != null) {
	  valBuf.append(myMinValue.toString());
//	  valBuf.append(getUnit());
	  valBuf.append("/");
	}
	if (myMaxValue != null) {
	  valBuf.append(myMaxValue.toString());
//	  valBuf.append(getUnit());
	  valBuf.append("/");
	}
	if (myAvgValue != null) {
	  valBuf.append(myAvgValue.toString());
//	  valBuf.append(getUnit());
	  valBuf.append("/");
	}
	
	valBuf.setLength(valBuf.length() - 1);
	valBuf.append(" (in "+getUnit()+")");

	String retVal = valBuf.toString();
	return retVal;
  }

  public void merge(SensorAttribute aAttr) {
	if (myMinValue != null) {
	  if (myMinValue instanceof Integer) {
		myMinValue = new Integer(Math.min((Integer) myMinValue, (Integer) aAttr.myMinValue));
	  }
	  if (myMinValue instanceof Float) {
		myMinValue = new Float(Math.min((Float) myMinValue, (Float) aAttr.myMinValue));
	  }
	}
	if (myMaxValue != null) {
	  if (myMaxValue instanceof Integer) {
		myMaxValue = new Integer(Math.max((Integer) myMaxValue, (Integer) aAttr.myMaxValue));
	  }
	  if (myMaxValue instanceof Float) {
		myMaxValue = new Float(Math.max((Float) myMaxValue, (Float) aAttr.myMaxValue));
	  }
	}
	myAvgValue = null;
  }

  public void noAvgValue() {
	myAvgValue = null;
  }

}
