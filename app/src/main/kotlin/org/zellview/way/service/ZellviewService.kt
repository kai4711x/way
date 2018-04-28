package org.zellview.way.service

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.app.Service
import android.content.Intent;
import android.graphics.Color
//import android.content.pm.PackageManager
//import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import org.jetbrains.anko.toast

import org.zellview.way.activity.MapsActivity.Companion.mapsActivity
import org.zellview.way.modules.addLocation
import org.zellview.way.util.createLogfile
import org.zellview.way.modules.trackPoints

//import org.zellview.way.R

private val MinInterval = 500L
private val StdInterval = 1000L
private val MinDisplacement = 1F //Unit: m

private val Stopped = 0
private val Paused = 1
private val Recording = 2


class ZellviewService: Service() {

    val TAG = "ZellviewService";
    companion object { lateinit var zellviewService: ZellviewService}

    var mRecStatus = Stopped

    private lateinit var mSettingsClient: SettingsClient;
    private lateinit var mLocSettingsRequest: LocationSettingsRequest;
    private lateinit var mLocClient: FusedLocationProviderClient;
    private lateinit var mLocRequest: LocationRequest;
    private lateinit var mLocCallback: LocationCallback;

    private var mCurLocation: Location? = null
    private var mLastLocation: Location? = null
    private var mPolyline: Polyline? = null

    private var mCurPos: LatLng? = null
    private var mLastPos: LatLng? = null
    private var mPosMarker: Marker? = null

    override fun onBind(intent: Intent): IBinder? {
        return Binder()
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        val task: Task<LocationSettingsResponse>
        task = mSettingsClient.checkLocationSettings(mLocSettingsRequest);

        val succesListener = OnSuccessListener<LocationSettingsResponse> {
            Log.i(TAG, "All location settings are satisfied.");
            if (ActivityCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION)==PERMISSION_GRANTED) {
                mLocClient.requestLocationUpdates(mLocRequest, mLocCallback, Looper.myLooper() )
            }
        }
        task.addOnSuccessListener(mapsActivity, succesListener)

        val failureListener = OnFailureListener { e ->
            val statusCode = (e as ApiException).statusCode
            when (statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                    Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings ")

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    val errorMessage = "Location settings are inadequate, and cannot be " +
                                       "fixed here. Fix in Settings."
                    Log.e(TAG, errorMessage)
                    //Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    //mRequestingLocationUpdates = false
                }
            }
            /*
                try {
                  // Show the dialog by calling startResolutionForResult(), and check the
                  // result in onActivityResult().
                  ResolvableApiException rae = (ResolvableApiException) e;
                  rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sie) {
                  Log.i(TAG, "PendingIntent unable to execute request.");
                }                        */
        }
        task.addOnFailureListener(mapsActivity, failureListener)
        //updateUI();
    }


    fun handleNewLocation(location: Location) {
        //var points: ArrayList<LatLng>
        //val myLatLng = LatLng(location.latitude, location.longitude)
        //val line = PolylineOptions

        Log.d(TAG, "lat " + location.latitude.toString() + " long " + location.longitude.toString())
        mCurPos = LatLng(location.latitude, location.longitude)

        if (mRecStatus == Recording) {
            addLocation(location)

            if (mLastPos != null) {
                mPolyline = mapsActivity.mMap.addPolyline(PolylineOptions().apply {
                    color(Color.RED)
                    width(5f)
                    add(mCurPos, mLastPos)
                })
            }
        }

        /*
        mPosMarker = instance.mMap.addMarker(MarkerOptions()
                .title("Standort")
                .äposition(mCurLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location-arrow) ) )
        */
        //mPosMarker = MarkerOptions().position(mCurPos).title("Standort")

        mPosMarker?.remove()
        mPosMarker = mapsActivity.mMap.addMarker(MarkerOptions().position(mCurPos as LatLng).title("Standort"));
        val zoom= mapsActivity.mMap.cameraPosition.zoom
        mapsActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurPos, zoom), 5000, null);

        //instance.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurLatLng, 8f))

        //mLastLocation = location
        //mLastPos = LatLng(location.latitude, location.longitude)
        mLastPos = mCurPos

    }

    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocRequest);
        mLocSettingsRequest = builder.build()
    }


    private fun createLocationRequest() {
        mLocRequest = LocationRequest.create().apply {
            interval = StdInterval;
            fastestInterval = MinInterval;
            smallestDisplacement = MinDisplacement;
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }


    private fun createLocationCallback() {
        mLocCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult ?: return
                mCurLocation = locationResult.lastLocation;
                for (location in locationResult.locations){
                    handleNewLocation(location)
                }
                //MapsActivity.instance.MarkLocation(mCurrentLocation)
                //mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                //ngDetect.addLocationAndTimeEvent(mCurrentLocation, System.currentTimeMillis())
                //updateLocationUI();
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ShowLog("onStartCommand");

        zellviewService = this

        mLocClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)

        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
        startLocationUpdates()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        ShowLog("onDestroy");
        super.onDestroy()
    }

    override fun onCreate() {
        ShowLog("onCreate");
        mLastPos = LatLng(51.0, 7.0)
        super.onCreate()
    }

    fun ShowLog(message: String){
        Log.d(TAG, message)
    }

    fun toggleRecording() {
        if ( ( mRecStatus == Stopped ) || (mRecStatus == Paused) ) {
            mRecStatus = Recording
            mapsActivity.mMap.clear()
            toast("Recording started");
        } else {
            mRecStatus = Stopped
            toast("Recording stopped");
            createLogfile(trackPoints)
        }
    }
}



