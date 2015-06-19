package com.giisoo.core.index;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class Location.
 */
public class Location implements Serializable {

  /** The lat. */
  double lat;
  
  /** The lng. */
  double lng;

  /**
   * Gets the lat.
   *
   * @return the lat
   */
  public double getLat() {
    return lat;
  }

  /**
   * Sets the lat.
   *
   * @param lat the new lat
   */
  public void setLat(double lat) {
    this.lat = lat;
  }

  /**
   * Gets the lng.
   *
   * @return the lng
   */
  public double getLng() {
    return lng;
  }

  /**
   * Sets the lng.
   *
   * @param lng the new lng
   */
  public void setLng(double lng) {
    this.lng = lng;
  }

}
