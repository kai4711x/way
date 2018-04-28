package org.zellview.way.modules

import android.location.Location
import android.util.Log

private val TAG = "Tracks";

var trackPoints: MutableList<Location> = mutableListOf<Location>()

fun addLocation(location: Location) {
    Log.v(TAG, location.toString());
    trackPoints.add(location);
    Log.v(TAG, "Cnt " + trackPoints.size.toString())
}
