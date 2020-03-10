# <img src="doc/img/glidersymbol.png" alt="symbol" width="60"/> JETI Log Analytics  

a standalone application to parse and analyses JETI log files in a bulk matter.
The current version of JETI Log Analytics can be used in a command line mode or in a GUI based mode.

![](doc/img/JetiLogAnalytics_GUI_en.png)

## Details
The application is written to extract detailed data per flight, per model and total. One of the main features is a flight detection algorithm (based on the signal levels A1 and A2 given by any RX of JETI), which does provides extraction of detailed data only if the model is not in the near area of the transmitter (air field, workbench, ...).

JET Log Analytics needs only a base directory as argument to find the JETI log files in their date based folder structure. It scans all files and gives information about files, models, flights and overall data.
It is possible to give a date range to filter the given information to the need range.

So it is very easy to get the overall pure flight duration for the given files. In addition to the flight duration it will give some statistical data (if existing) per flight/model:
* modelname
* real flight duration
* log duration
* real number of flight (not count of log files)
* flight heights
* flight speeds (air- and GPS-speed)
* flight distance
* RX voltage minimum 
* list of sensors
* list of events / alarms 
* timestamps with the real start of flight
* signal-pulse-distance (in case the an RXQ sensor is available)

 Here an short example of a scan result:
	
		Reading Log-Files:
	  scanning log file: 20200301/14-05-54.log : model: X-Swift 3.2, number flights: 1
	  scanning log file: 20200303/12-06-22.log : model: X-Swift 3.2, number flights: 0
	  scanning log file: 20200303/15-24-31.log : model: X-Swift 3.2, number flights: 1
	
	model statistic (1 models):
	model                           : X-Swift 3.2
	  number flights                : 2
	  single flight (min/max)       : 00:35:58/00:52:26
	  flight duration total         : 01:28:24
	  log duration total            : 01:49:47
	  height (min/max)              : 0/270 (in m)
	  gps speed (min/max)           : 0/233 (in km/h)
	  signal-pulse-distance (max)   : 53 (in ms)
	  distance (min/max)            : 0/782 (in m)
	  Rx voltage (min)              : 7.1 (in V)
	  alarms:
	    900MHz Tx aktiviert         : 1
	    Alarm: Rx-Spannung          : 13
	    Schw. Signal: Q             : 6
	    Signalverlust               : 3
	  sensors                       : [Rx REX10A, RxB RSat900, Tx, VarioGPS]
	  flights / details             
	    flight / timestamp              : 2020-03-01 14:06:52
	      log file                      : 20200301/14-05-54.log
	      log duration total            : 00:36:59
	      flight duration               : 00:35:58
	      height (min/max/Ø)            : 4/270/97 (in m)
	      gps speed (min/max/Ø)         : 0/233/58 (in km/h)
	      signal-pulse-distance (max)   : 9 (in ms)
	      distance (min/max/Ø)          : 0/611/162 (in m)
	      Rx voltage (min)              : 7.1 (in V)
	      alarms:
	        Alarm: Rx-Spannung          : 13
	        Schw. Signal: Q             : 1
	        Signalverlust               : 3
	    flight / timestamp              : 2020-03-03 15:28:30
	      log file                      : 20200303/15-24-31.log
	      log duration total            : 00:57:32
	      flight duration               : 00:52:26
	      height (min/max/Ø)            : 0/216/99 (in m)
	      gps speed (min/max/Ø)         : 0/199/52 (in km/h)
	      signal-pulse-distance (max)   : 53 (in ms)
	      distance (min/max/Ø)          : 4/782/228 (in m)
	      Rx voltage (min)              : 7.37 (in V)
	      alarms:
	        900MHz Tx aktiviert         : 1
	        Schw. Signal: Q             : 5
	
	
	Statisic total                  
	  number logs total             : 3
	  log duration total            : 01:49:47
	  number models                 : 1
	  number flights total          : 2
	  flight duration total         : 01:28:24
	  alarms                        :
	    900MHz Tx aktiviert         : 1
	    Alarm: Rx-Spannung          : 13
	    Schw. Signal: Q             : 6
	    Signalverlust               : 3
	
	

Some more details in German and an browser based version of JetiLogAnalytics is available at [JetiLogAnalytics at Albatros].
## command line usage
	$ /remote/netdata/nas/JetiLogAnalytics.sh --help
	usage: JetiLogAnalytics [option]
	scans JETI log files found in folder and printout the results of total, model, flight statistic
	Example: java -jar JetiLogAnalytics-nls DE -nogui -dir ./testData/ 
	
	options:
	 --nogui                      commndline mode and textoutput only application
	 --dir <path to log-folder>   path used in command line mode
	 --from <YYYY-MM-DD>          date to start analysing log files, if omitted all log files found are analysed
	 --to <YYYY-MM-DD>            date to end analysing log files, if omitted all log files found are analysed
	
## Software
This git repository is a Eclipse workspace. So if you want to build the software for yourself, clone the project to a folder and set this folder as a Eclipse workspace.

JETI Log Analytics is a Java application (using JavaFX as GUI framework) build as a runnable jar file.
The required JRE is at least a 'Oracle Java 8' or 'OpenJDK 8´ (java-1.8.0-openjdk + java-1.8.0-openjdk-openjfx).
On Windows and Mac this JetiLogAnalytics.jar - file can be easily started by double clicking the file. On Linux the wrapper script JetiLogAnalytics.sh can be used to start the GUI based application.

The JetiLogAnalytics.jar/-.sh - files are provided in the bin folder in this repository. 
Attention: On GitHub downloading single files is not a easy thing. If you only want the runnable jar file and the wrapper script, use the green download button on the project page, select "Download ZIP" and extract only the two files in the bin folder.

## Development
The user interface is based on JavaFX and is designed with [Scene Builder] for Java 8

[Scene Builder]: https://gluonhq.com/products/scene-builder/  "Scene Builder: Drag & Drop,
Rapid Application Development."
[JetiLogAnalytics at Albatros]: http://www.so-fa.de/nh/JetiLogAnalytics "JETI Log Analytics German page Albatros"