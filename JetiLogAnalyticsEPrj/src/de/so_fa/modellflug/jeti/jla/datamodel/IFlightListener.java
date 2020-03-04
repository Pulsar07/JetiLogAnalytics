package de.so_fa.modellflug.jeti.jla.datamodel;

public interface IFlightListener {
  void flightStart();

  void flightEnd(Flight aF);
}
