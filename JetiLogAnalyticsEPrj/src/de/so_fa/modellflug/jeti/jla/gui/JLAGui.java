package de.so_fa.modellflug.jeti.jla.gui;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.JetiLogAnalytics;
import de.so_fa.utils.config.GenericConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class JLAGui extends Application {
  static private GenericConfig ourConfig;
  public static final String CFG_JETI_LOG_PATH = "JetiLogPath";
  public static final String CFG_FROM_DATE = "FromDate";
  public static final String CFG_TO_DATE = "ToDate";

  public static Logger ourLogger = Logger.getLogger(JLAGui.class.getName());
  private JLAGuiController myFXMLController;
  private static ResourceBundle ourResourceBundle;

  static JLAGui myInstance = null;
  
  @Override
  public void init() {
	int i = 5;
  }
  

  public static JLAGui getInstance() {
	return myInstance;
  }
  
  public static void initConfig() {
    ourConfig = GenericConfig.getInstance(JetiLogAnalytics.APP_NAME);
    
    // read config values from config file ~/<ourAppName>/config/config.xml
    // or set default values if not existing
    ourConfig.getValue(CFG_JETI_LOG_PATH, System.getProperty("user.home"), "Jeti log path",
        "path to the Jeti log folder");
    ourConfig.getValue(CFG_FROM_DATE, null, "FromDate",
        "date starting the analysis");
    ourConfig.getValue(CFG_TO_DATE, null, "ToDate",
        "date ending the analysis");

    
  }

  @Override
  public void start(Stage aStage) throws Exception {
	myInstance = this;
	URL url = getClass().getResource("JLA.fxml");
	if (url == null) {
	  System.out.println("cannot find fxml file");
	  System.exit(-1);
	}
	
	FXMLLoader loader = new FXMLLoader(getClass().getResource("JLA.fxml"));

	ourResourceBundle = ResourceBundle.getBundle("de.so_fa.modellflug.jeti.jla.gui.JLAGui", Locale.getDefault());
	loader.setResources(ourResourceBundle);
	
	Parent root = loader.load();
	myFXMLController = (JLAGuiController) loader.getController();

	Scene scene = new Scene(root, 1000, 600);
	//set icon of the application
    Image applicationIcon = new Image(getClass().getResourceAsStream("glidersymbol.png"));
    aStage.getIcons().add(applicationIcon);

	aStage.setTitle("JETI Log Analytics (by RS)");
	aStage.setScene(scene);
	initConfig();
	myFXMLController.init();
	myFXMLController.setVersion("Version: " + JetiLogAnalytics.VERSION);
	myFXMLController.setJetiLogPath(GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).getValue(CFG_JETI_LOG_PATH));
	
	aStage.show();
  }

  @Override
  public void stop() {
  }

  public static void startGUI(String[] aArgs) {
	Application.launch(aArgs);
  }
  
  void showLinkInBrowser(String aLink) {
	if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
	    try {
		  Desktop.getDesktop().browse(new URI(aLink));
		} catch (Exception e) {
		  ourLogger.log(Level.WARNING, "cannot open link", e);
		}
	}
  }
 
}