package de.so_fa.modellflug.jeti.jla.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SensorDevices {

  Set<String> mySensorDevices = new HashSet<String>();

  public void add(Collection<String> aSensorDevices) {
	// [Tx, Rx REX10A, RxB RSat900, VarioGPS, Rx REX10A REX10A, RxB]
	mySensorDevices.addAll(aSensorDevices);

	Set<String> newset = new HashSet<String>();

	for (String ndev : aSensorDevices) {
	  if (!mySensorDevices.contains(ndev)) {
		for (String edev : mySensorDevices) {
		  if (ndev.startsWith(edev)) {

		  }
		}
	  }
	}

  }

  public String toString() {
	List<String> notFinalizedDevNames = new ArrayList<String>();
	for (String dev : mySensorDevices) {
	  if (dev.endsWith(" ")) { // not finalized name
		notFinalizedDevNames.add(dev);
	  }
	}
	List<String> toBeRemovedDevNames = new ArrayList<String>();
	for (String dev : mySensorDevices) {
	  for (String notFinDev : notFinalizedDevNames) {
		if (!dev.equals(notFinDev) && dev.startsWith(notFinDev)) {
		  // notFinDev can be removed
		  toBeRemovedDevNames.add(notFinDev);
		}
	  }
	}
	for (String toBeRemoved : toBeRemovedDevNames) {
	  mySensorDevices.remove(toBeRemoved);
	}

	List<String> result = new ArrayList<String>(mySensorDevices);
	result.sort(Comparator.naturalOrder());
	return result.toString();
  }
}
