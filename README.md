# JETI Log Analytics

a standalone Java application to scan and analyses JETI log files in a bulk matter.

The application is written to extract flight data per model. There is a flight detection algorithm used, based on the signal levels A1 and A2 given by any RX of JETI.
If a model is not moving and the TX is very near to the model (as on the airfield or on the workbench), the values are near to the maximum.

JET Log Analytics needs only a base directory as argument to find the JETI log files in their date based folder structure. It scans all files and gives information about files, models, flights and overall data.

So it is very easy to get the overall pure flight duration for the given files. In addition to the flight duration it will give some statistical data (if existing) per flight/model:
* modelname
* max. height
* max speed
* avg speed
* number of flights
* flight duration
* log duration
* detection type (at the moment only SIGNAL)

 Here an short example of  scan result:

	JetiLogAnalytics (JLA) Version: 0.1.7
	
	Reading Log-Files:
	scanning log file: 20181006/10-54-44.log : model: ASW 17, number flights: 0
	scanning log file: 20181006/15-21-30.log : model: ASW 17, number flights: 1
	scanning log file: 20190815/14-47-18.log : model: FW-Swift 385, number flights: 1
	scanning log file: 20190827/14-54-26.log : model: SB14, number flights: 1
	scanning log file: 20190827/15-22-13.log : model: SB14, number flights: 2
	
	model statistic (3 models):
	model                       : SB14
	max. height (in m)        : 773
	max. speed (in km/h)      : 273
	number flights            : 3
	flight duration total     : 01:08:05
	log duration total        : 01:27:17
	flight                    : 2019-08-27 15:00:35
	detection type          : SIGNAL
	flight duration         : 00:21:16
	max. height (in m)      : 644
	avg. speed (in km/h)    : 48
	max. speed (in km/h)    : 273
	flight                    : 2019-08-27 15:28:22
	detection type          : SIGNAL
	flight duration         : 00:21:16
	max. height (in m)      : 177
	avg. speed (in km/h)    : 33
	max. speed (in km/h)    : 154
	flight                    : 2019-08-27 15:55:26
	detection type          : SIGNAL
	flight duration         : 00:25:33
	max. height (in m)      : 773
	avg. speed (in km/h)    : 61
	max. speed (in km/h)    : 219
	
	model                       : ASW 17
	max. height (in m)        : 283
	max. speed (in km/h)      : 216
	number flights            : 1
	flight duration total     : 00:39:54
	log duration total        : 00:52:00
	flight                    : 2018-10-06 15:23:53
	detection type          : SIGNAL
	flight duration         : 00:39:54
	max. height (in m)      : 283
	avg. speed (in km/h)    : 43
	max. speed (in km/h)    : 216
	
	model                       : FW-Swift 385
	max. height (in m)        : 291
	max. speed (in km/h)      : 193
	number flights            : 1
	flight duration total     : 00:52:45
	log duration total        : 00:54:31
	flight                    : 2019-08-15 14:47:50
	detection type          : SIGNAL
	flight duration         : 00:52:45
	max. height (in m)      : 291
	avg. speed (in km/h)    : 41
	max. speed (in km/h)    : 193
	
	
	Statisic total:
	number logs total         : 5
	log duration total        : 03:13:48
	number models             : 3
	number flights total      : 5
	flight duration total     : 02:40:44

