package de.so_fa.modellflug.jeti.jla.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * This class extends from OutputStream to redirect output to a JTextArrea
 * 
 * @author www.codejava.net
 *
 */
public class CustomOutputStream extends OutputStream {
  public static Logger ourLogger = Logger.getLogger(CustomOutputStream.class.getName());
  private TextArea myTextArea;
  private String myEncoding;
  private ArrayList<Byte> myByteBuffer = new ArrayList<Byte>();

  public CustomOutputStream(TextArea aTextArea) {
	myEncoding = System.getProperty("file.encoding");
	this.myTextArea = aTextArea;
	ourLogger.info("system encoding: " + myEncoding);
  }

  @Override
  public void write(int b) throws IOException {
	// redirects data to the text area

	if (b == '\r')
	  return;

	if (b == '\n') {
	  byte[] data = new byte[myByteBuffer.size()];
	  for (int i = 0; i < data.length; i++) {
		data[i] = (byte) myByteBuffer.get(i);
	  }

	  String x = new String(data, myEncoding);		
	  Platform.runLater(new Runnable() {
        @Override
        public void run() {
           myTextArea.appendText(x);
           myTextArea.appendText("\n");
        }
     });
	  myByteBuffer.clear();
	  return;

	}
	myByteBuffer.add((Byte.valueOf((byte) b)));
  }
}