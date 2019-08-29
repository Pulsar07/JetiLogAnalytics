package de.so_fa.modellflug.jeti.jla.log;

import java.util.StringTokenizer;

public class SensorValue {

	boolean isValid = false;
	long myTime;
	long mySensorId;
	int myValueIdx;
	int myType;
	double myValue;

	public SensorValue(long aTimeStamp, long aSensorId, StringTokenizer aTokenizer) {
		try {
		    myTime = aTimeStamp;
		    mySensorId = aSensorId;
			myValueIdx = Integer.parseInt(aTokenizer.nextToken());
			myType = Integer.parseInt(aTokenizer.nextToken());
			int decimalPlaces = Integer.parseInt(aTokenizer.nextToken());
			long value = Long.parseLong(aTokenizer.nextToken());
//		Data type Description Note
//		0	int6_t	Data type 6b (-31  ̧31)
//		1	int14_t	Data type 14b (-8191  ̧8191)
//		2	int14_t		Reserved
//		3	int14_t		Reserved
//		4	int22_t		Data type 22b (-2097151  ̧2097151)
//		5	int22_t		Special data type – time and date
//		6	int22_t		Reserved
//		7	int22_t		Reserved
//		8	int30_t		Data type 30b (-536870911  ̧536870911)
//		9	int30_t		Special data type – GPS coordinates
//		10	int30_t		Reserved
//		11	int30_t		Reserved
//		12	int38_t		Reserved
//		13	int38_t		Reserved
//		14	int38_t		Reserved
//		15	int38_t		Reserved
			switch (myType) {
			case 0: // 0 int6_t Data type 6b (-31 ̧31)
			case 1: // 1 int14_t Data type 14b (-8191 ̧8191)
			case 4: // 4 int22_t Data type 22b (-2097151 ̧2097151)
			case 8:
				if (Math.abs(value) < 2097151) {
					myValue = value / Math.pow(10, decimalPlaces);
					isValid = true;
				}
				break;

			}
		} catch (Exception e) {
			return;
		}
	}

	public long getTime() {
	  return this.myTime;
	}

	public long getSensorId() {
	  return this.mySensorId;
	}

	public int getValueIdx() {
		if (!isValid) {
			throw new RuntimeException("cannot getValueIdx() for a invalid DataValue: " + this);
		}
		return myValueIdx;
	}

	public double getValue() {
		if (!isValid) {
			throw new RuntimeException("cannot getValue() for a invalid DataValue: " + this);
		}
		return myValue;
	}

	public boolean isValid() {
		return isValid;
	}

	public boolean is(SensorValueDescription aSensorDescr) {
	  boolean retVal = false;
	  if (aSensorDescr != null && mySensorId == aSensorDescr.getId() && myValueIdx == aSensorDescr.getIndex()) {
		retVal = true;
	  }
	  return retVal;
	}

}
