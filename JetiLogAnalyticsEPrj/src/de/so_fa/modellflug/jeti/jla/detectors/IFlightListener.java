package de.so_fa.modellflug.jeti.jla.detectors;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;

public interface IFlightListener {
  void flightStart();

  void flightEnd(Flight aF);
}
