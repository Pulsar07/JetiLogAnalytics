package de.so_fa.modellflug.jeti.jla.log;

public class SensorValueDescription {

  private long myId;
  private int myIndex;
  private String myName;
  private String myUnit;

  public SensorValueDescription(long aId, int aIdx, String aName, String aUnit) {
	myId = aId;
	myIndex = aIdx;
	myName = aName.trim();
	if (aUnit != null) {	  
	  myUnit = aUnit.trim();
	}
  }

  public long getId() {
    return this.myId;
  }

  public int getIndex() {
    return this.myIndex;
  }

  public String getName() {
    return this.myName;
  }

  public String getUnit() {
    return this.myUnit;
  }
  
  public String toString() {
	StringBuffer retVal = new StringBuffer();
	retVal.append(SensorValueDescription.class.getSimpleName());
	retVal.append("<");
	retVal.append(myName);
	retVal.append(",");
	retVal.append(myId);
	retVal.append(",");
	retVal.append(myIndex);
	retVal.append(",");
	retVal.append(myUnit);
	retVal.append(">");
	return retVal.toString();
  }

}
