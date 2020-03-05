package de.so_fa.modellflug.jeti.jla.gui;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.JetiLogAnalytics;
import de.so_fa.modellflug.jeti.jla.JetiLogAnalyticsController;
import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;
import de.so_fa.utils.config.GenericConfig;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class JLAGuiController {
  public static Logger ourLogger = Logger.getLogger(JLAGuiController.class.getName());

  @FXML
  private Label FX_Path;
  @FXML
  private AnchorPane FX_AchorPane;
  @FXML
  private Label FX_VersionLabel;
  @FXML
  private DatePicker FX_FromDate;
  @FXML
  private DatePicker FX_ToDate;
  @FXML
  private Button FX_StartAnalysis;
  @FXML
  private TextArea FX_ResultArea;
  @FXML
  private Button FX_BrowsePath;
  @FXML
  private CheckBox FX_CheckModel;
  @FXML
  private CheckBox FX_CheckFlight;
  @FXML
  private CheckBox FX_CheckAlarm;
  @FXML
  private CheckBox FX_CheckDevices;
  @FXML
  private TextField FX_TextFieldModelFilter;

  public void init() {
	String date;
	date = GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).getValue(JLAGui.CFG_FROM_DATE);
	if (date != null && !date.isEmpty()) {
	  ourLogger.info("to " + date);
	  FX_FromDate.setValue(LocalDate.parse(date));
	}
	date = GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).getValue(JLAGui.CFG_TO_DATE);
	if (date != null && !date.isEmpty()) {
	  ourLogger.info("to " + date);
	  FX_ToDate.setValue(LocalDate.parse(date));
	}
  }

  /**
   * Initializes the controller class.
   */
  public void initialize(URL url, ResourceBundle rb) {
	ourLogger.severe("initialize");
	FX_FromDate.setConverter(new StringConverter<LocalDate>() {
	  String pattern = "dd.MM.YY";
	  DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

	  {
		FX_FromDate.setPromptText(pattern.toLowerCase());
	  }

	  @Override
	  public String toString(LocalDate date) {
		if (date != null) {
		  return dateFormatter.format(date);
		} else {
		  return "";
		}
	  }

	  @Override
	  public LocalDate fromString(String string) {
		if (string != null && !string.isEmpty()) {
		  return LocalDate.parse(string, dateFormatter);
		} else {
		  return null;
		}
	  }
	});

  }

  @FXML
  protected void onAction_TextFieldModelFilter(ActionEvent aEvent) {
  }

  @FXML
  protected void onAction_CheckModel(ActionEvent aEvent) {
	CheckBox src = (CheckBox) aEvent.getSource();
	if (!src.isSelected()) {
	  FX_CheckFlight.setSelected(false);
	  FX_CheckAlarm.setSelected(false);
	  FX_CheckDevices.setSelected(false);
	}
  }

  @FXML
  protected void onAction_CheckFlight(ActionEvent aEvent) {
	CheckBox src = (CheckBox) aEvent.getSource();
	if (src.isSelected()) {
	  FX_CheckModel.setSelected(true);
	}
  }

  @FXML
  protected void onAction_CheckDevices(ActionEvent aEvent) {
	CheckBox src = (CheckBox) aEvent.getSource();
	if (src.isSelected()) {
	  FX_CheckModel.setSelected(true);
	}
  }

  @FXML
  protected void onAction_CheckAlarm(ActionEvent aEvent) {
	CheckBox src = (CheckBox) aEvent.getSource();
	if (src.isSelected()) {
	  FX_CheckModel.setSelected(true);
	}
  }

  @FXML
  protected void onAction_FromDate(ActionEvent aEvent) {
	LocalDate date = FX_FromDate.getValue();
	GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).setValue(JLAGui.CFG_FROM_DATE,
		date.format(DateTimeFormatter.ISO_LOCAL_DATE));
	GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).saveConfig();
  }

  @FXML
  protected void onAction_ToDate(ActionEvent aEvent) {
	LocalDate date = FX_ToDate.getValue();
	GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).setValue(JLAGui.CFG_TO_DATE,
		date.format(DateTimeFormatter.ISO_LOCAL_DATE));
	GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).saveConfig();

  }

  Thread myAnalysisThread;

  @FXML
  protected void onAction_StartAnalysis(ActionEvent aEvent) {
	if (myAnalysisThread != null && myAnalysisThread.isAlive()) {
	  JetiLogAnalytics.stop();
	  myAnalysisThread.interrupt();
	  FX_StartAnalysis.setText(NLS.get(NLSKey.FX_StartAnalysis));
	  return;
	}
	FX_ResultArea.clear();
	JetiLogAnalyticsController.getInstance().setDoModelPrint(FX_CheckModel.isSelected());
	JetiLogAnalyticsController.getInstance().setDoFlightPrint(FX_CheckFlight.isSelected());
	JetiLogAnalyticsController.getInstance().setDoAlarmPrint(FX_CheckAlarm.isSelected());
	JetiLogAnalyticsController.getInstance().setDoDevicesPrint(FX_CheckDevices.isSelected());
	JetiLogAnalyticsController.getInstance().setModelFilter(FX_TextFieldModelFilter.getText());
	FX_StartAnalysis.setText(NLS.get(NLSKey.FX_StopAnalysis));

	myAnalysisThread = new Thread(new Runnable() {
	  @Override
	  public void run() {
		PrintStream standardOut = System.out;
		PrintStream printStream = new PrintStream(new CustomOutputStream(FX_ResultArea));

		// re-assigns standard output stream and error output stream
		System.setOut(printStream);
		// System.setErr(printStream);
		LocalDate from = FX_FromDate.getValue();
		LocalDate to = FX_ToDate.getValue();
		JetiLogAnalytics.startAnalysis(
			new File(GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).getValue(JLAGui.CFG_JETI_LOG_PATH)), from,
			to);
		// re-assigns standard output stream and error output stream

		Platform.runLater(new Runnable() {
		  @Override
		  public void run() {
			FX_ResultArea.appendText("");
			// scrolls the text area to the end of data
//	           myTextArea.positionCaret(myTextArea.getLength());
//	           myTextArea.selectPositionCaret(myTextArea.getLength());
//	           myTextArea.deselect();
			FX_ResultArea.setScrollTop(Double.MAX_VALUE);
		  }
		});
		System.setOut(standardOut);
		Platform.runLater(new Runnable() {
		  @Override
		  public void run() {
			FX_StartAnalysis.setText(NLS.get(NLSKey.FX_StartAnalysis));
		  }
		});
	  }
	});
	myAnalysisThread.start();

  }

  @FXML
  protected void onAction_Help(ActionEvent aEvent) {
	Alert helpDialog = new Alert(AlertType.INFORMATION);
	helpDialog.setTitle(JetiLogAnalytics.APP_NAME + ": Info");
	helpDialog.setHeaderText(String.format(NLS.get(NLSKey.FX_HelpHeader), JetiLogAnalytics.APP_NAME));
	helpDialog.setContentText(String.format(NLS.get(NLSKey.FX_HelpContent), JetiLogAnalytics.VERSION));
	Image symbol = new Image(getClass().getResourceAsStream("glidersymbol.png"), 50, 50, true, true);
	ImageView imageView = new ImageView(symbol);
	helpDialog.setGraphic(imageView);
	helpDialog.setResizable(true);
	helpDialog.getDialogPane().setPrefSize(650, 500);

	helpDialog.showAndWait();
  }

  @FXML
  protected void onAction_Exit(ActionEvent aEvent) {
	System.exit(0);
  }

  @FXML
  protected void onAction_GitHubLink(ActionEvent aEvent) {
	JLAGui.getInstance().showLinkInBrowser("https://github.com/Pulsar07/JetiLogAnalytics");
  }

  @FXML
  protected void onAction_BrowsePath(ActionEvent aEvent) {
	DirectoryChooser fileChooser = new DirectoryChooser();
	fileChooser.setInitialDirectory(
		new File(GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).getValue(JLAGui.CFG_JETI_LOG_PATH)));
	fileChooser.setTitle("Open Resource File");

	Node source = (Node) aEvent.getSource();
	Stage stage = (Stage) source.getScene().getWindow();

	File file = fileChooser.showDialog(stage);
	FX_Path.setText(file.getPath());

	GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).setValue(JLAGui.CFG_JETI_LOG_PATH, file.getPath());
	GenericConfig.getInstance(JetiLogAnalytics.APP_NAME).saveConfig();
  }

  public void setVersion(String aVersion) {
	FX_VersionLabel.setText(aVersion);
  }

  public void setJetiLogPath(String aValue) {
	FX_Path.setText(aValue);
  }

}
