package de.so_fa.modellflug.jeti.jla;

public class JetiLogAnalyticsController {
  private static JetiLogAnalyticsController ourInstance = null;
  public static JetiLogAnalyticsController getInstance() {
	if (null == ourInstance) {
	  ourInstance = new JetiLogAnalyticsController();
	}
	return ourInstance;
  }
  
  private boolean myDoModelPrint = true;
  private boolean myDoFlightPrint = true;
  private boolean myDoDevicesPrint = true;
  
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

}
