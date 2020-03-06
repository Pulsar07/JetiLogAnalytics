package de.so_fa.modellflug.jeti.jla.jetilog;

import java.util.StringTokenizer;
import java.util.logging.Logger;

public class SensorValue {
  private static final Logger ourLogger = Logger.getLogger(SensorValue.class.getName());
  private static final int ALARM_TYPE = 16;
  boolean isValid;
  long myTime;
  long mySensorId;
  int myValueIdx;
  int myType;
  int myDecimalPlaces;
  long myRawVal;
  double myValue;
  String myAlarmValue;

  // 004691301;0000000000;15;16;0;900MHz Tx aktiviert;0;0
  // 002381367;4392122371;1;1;2;848;2;1;0;4;3;1;0;4;4;1;0;39
  // |.........|......... |.|.|.|
  // |.........|......... |.|.|.value
  // |.........|......... |.|.decimal places
  // |.........|......... |.type
  // |.........|......... value index
  // |.........sensor id
  // timestamp
 

  public SensorValue(long aTimeStamp, long aSensorId, StringTokenizer aTokenizer) {
	try {
	  isValid = false;
	  myAlarmValue = null;
	  myTime = aTimeStamp;
	  mySensorId = aSensorId;
	  myValueIdx = Integer.parseInt(aTokenizer.nextToken());
	  myType = Integer.parseInt(aTokenizer.nextToken());
	  myDecimalPlaces = Integer.parseInt(aTokenizer.nextToken());

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
//      16  String      used for alarms
	  switch (myType) {
	  case 0: // 0 int6_t Data type 6b (-31 ̧31)
	  case 1: // 1 int14_t Data type 14b (-8191 ̧8191)
	  case 4: // 4 int22_t Data type 22b (-2097151 ̧2097151)
	  case 8:
		myRawVal = Long.parseLong(aTokenizer.nextToken());
		if (myRawVal < 2097151) {
		  myValue = myRawVal / Math.pow(10, myDecimalPlaces);
		  isValid = true;
		} else {
		  myValue = (myRawVal - 4294967296L) / Math.pow(10, myDecimalPlaces);
		  isValid = true;
		}
		break;
	  case 9:
		myRawVal = Long.parseLong(aTokenizer.nextToken());
		isValid = true;
		break;
	  case ALARM_TYPE:
		if (mySensorId == 0 && myValueIdx == 15) {
		  myAlarmValue = aTokenizer.nextToken();
		  // ourLogger.severe("alarm:" + myAlarmValue);
		  isValid = true;
		}
		break;
	  default:
		// ourLogger.warning("unsupportet value type: " + myType);
		aTokenizer.nextToken();
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
	if (!isValid || myAlarmValue != null) {
	  throw new RuntimeException("cannot getValue() for a invalid DataValue: " + this);
	}
	return myValue;
  }

  public boolean isValid() {
	return isValid;
  }

  public String getAlarm() {
	if (!isValid || myAlarmValue == null) {
	  throw new RuntimeException("cannot getValue() for a invalid DataValue: " + this);
	}
	return myAlarmValue;
  }

  /**
   * @return true if the given description fits to the this value, else return
   *         false
   */
  public boolean is(SensorValueDescription aSensorDescr) {
	boolean retVal = false;
	if (aSensorDescr != null && mySensorId == aSensorDescr.getId() && myValueIdx == aSensorDescr.getIndex()) {
	  retVal = true;
	}
	return retVal;
  }

  public String toString() {
	StringBuffer retVal = new StringBuffer();
	retVal.append(SensorValue.class.getSimpleName());
	retVal.append("[");
	retVal.append(mySensorId);
	retVal.append(",");
	retVal.append(myValueIdx);
	retVal.append(",");
	retVal.append(myType);
	retVal.append(", raw: ");
	retVal.append(myRawVal);
	retVal.append(", ok: ");
	retVal.append(myValue);
	retVal.append("]");
	return retVal.toString();
  }

}
