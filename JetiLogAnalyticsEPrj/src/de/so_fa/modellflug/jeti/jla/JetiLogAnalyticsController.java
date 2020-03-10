package de.so_fa.modellflug.jeti.jla;

import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class JetiLogAnalyticsController {
  private final static Logger ourLogger = Logger.getLogger(JetiLogAnalyticsController.class.getName());

  private static JetiLogAnalyticsController ourInstance = null;
  private LocalDate myFromRange = LocalDate.MIN;
  private LocalDate myToRange = LocalDate.now();
  private boolean myDoModelPrint = true;
  private boolean myDoFlightPrint = true;
  private boolean myDoDevicesPrint = true;
  private boolean myDoAlarmPrint = true;
  private Pattern myModelSearchPattern = Pattern.compile(".*");

  public static JetiLogAnalyticsController getInstance() {
	if (null == ourInstance) {
	  ourInstance = new JetiLogAnalyticsController();
	}
	return ourInstance;
  }

  public LocalDate getFromRange() {
	return this.myFromRange;
  }

  public void setFromRange(LocalDate aDate) {
	if (aDate == null) {
	  this.myFromRange = LocalDate.MIN;
	} else {
	  this.myFromRange = aDate;
	}
  }

  public LocalDate getToRange() {
	return this.myToRange;
  }

  public void setToRange(LocalDate aDate) {
	if (aDate == null) {
	  this.myToRange = LocalDate.MAX;
	} else {
	  this.myToRange = aDate;
	}
  }

  public long getRange() {
	long rangeinDays = myToRange.toEpochDay() - myFromRange.toEpochDay() +1;
	rangeinDays = Math.max(rangeinDays, 1);
	return rangeinDays;
  }

  public boolean isDoModelPrint() {
	return this.myDoModelPrint;
  }

  public void setDoModelPrint(boolean aDoModelPrint) {
	this.myDoModelPrint = aDoModelPrint;
  }

  public boolean isDoFlightPrint() {
	return this.myDoFlightPrint;
  }

  public void setDoFlightPrint(boolean aDoFlightPrint) {
	this.myDoFlightPrint = aDoFlightPrint;
  }

  public boolean isDoDevicesPrint() {
	return this.myDoDevicesPrint;
  }

  public void setDoDevicesPrint(boolean aDoDevicesPrint) {
	this.myDoDevicesPrint = aDoDevicesPrint;
  }

  private JetiLogAnalyticsController() {

  }

  public void setModelFilter(String aSearch) {
	myModelSearchPattern = Pattern.compile(".*" + aSearch.replaceAll("\\|", ".*|.*") + ".*");
	ourLogger.info(myModelSearchPattern.toString());
  }

  public Pattern getModelFilter() {
	return myModelSearchPattern;
  }

  public void setDoAlarmPrint(boolean aDoIt) {
	myDoAlarmPrint = aDoIt;
  }

  public boolean isDoAlarmPrint() {
	return myDoAlarmPrint;
  }
}
