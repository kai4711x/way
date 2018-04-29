package org.zellview.way.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.rareventure.gps2.BuildConfig.APPLICATION_ID
import com.rareventure.gps2.R

import org.jetbrains.anko.find
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
//import org.zellview.way.BuildConfig.APPLICATION_ID
//import org.zellview.way.R
import org.zellview.way.service.ZellviewService
import org.zellview.way.service.ZellviewService.Companion.zellviewService
import org.zellview.way.view.MapsActivityView

//import org.zellview.way.BuildConfig.APPLICATION_ID

//import org.zellview.map.BuildConfig.APPLICATION_ID
//&import org.zellview.map.LocationService

//import com.google.android.gms.location.sample.basiclocationsample.BuildConfig.APPLICATION_ID
//import org.zellview.zellview_fit.R


class MapsActivity : AppCompatActivity(),
        OnMapReadyCallback,
        ServiceConnection,
        NavigationView.OnNavigationItemSelectedListener {

    private val TAG = "MapsActivity"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    companion object { lateinit var mapsActivity: MapsActivity }

    private var serviceConnected: Boolean = false


    lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastPos:LatLng? = null

    lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        MapsActivityView().setContentView(this);
        drawer = find<DrawerLayout>(R.id.drawer_layout);

        val toolbar = find<Toolbar>(R.id.toolbar);
        //this.setSupportActionBar(toolbar);

        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer.addDrawerListener(toggle);
        toggle.syncState();


        var fab: FloatingActionButton  = findViewById(R.id.fab);
        fab.setOnClickListener {
            //Snackbar.make(view, "Toggle recording", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show()
            zellviewService.toggleRecording()
        }

        val navigationView = find<NavigationView>(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //this.setContentView(R.layout.activity_maps);

        mapsActivity = this;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //val intent = Intent(this, ZellviewService::class.java);
        val startIntent = Intent(this.getApplicationContext(), ZellviewService::class.java)
        startService(startIntent)

        val bindIntent = Intent(this, ZellviewService::class.java)
        bindService(bindIntent, this, BIND_AUTO_CREATE);

    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        serviceConnected = true;
        Log.d(TAG, "onServiceConnected")
    }

    override fun onServiceDisconnected(name: ComponentName) {
        serviceConnected = false;
        Log.d(TAG, "onServiceDisconnected")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_camera -> toast("Click Camera");
            R.id.nav_gallery -> toast("Click Gallery");
            R.id.nav_slideshow -> toast("Click Slideshow");
            R.id.nav_manage -> toast("Click Manage");
            R.id.nav_share -> toast("Click Share");
            R.id.nav_send -> toast("Click Send")
        }

        drawer.closeDrawer(GravityCompat.START);
        return true
    }

    /*
    fun MarkLocation(loc: Location) {
        lastPos =  LatLng(loc.latitude, loc.longitude);
        mMap.clear();
        mMap.addMarker(MarkerOptions().position(lastPos as LatLng).title("Standort"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPos as LatLng, 14f), 1000, null);
    }
    */


    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    /**
     * Shows a [Snackbar].
     *
     * @param snackStrId The id for the string resource for the Snackbar text.
     * @param actionStrId The text of the action item.
     * @param listener The listener associated with the Snackbar action.
     */
    private fun showSnackbar(
            snackStrId: Int,
            actionStrId: Int = 0,
            listener: View.OnClickListener? = null) {

        val snackbar = Snackbar.make(findViewById(android.R.id.content), getString(snackStrId),
                LENGTH_INDEFINITE)
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
    }


    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user. This would happen if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale, android.R.string.ok, View.OnClickListener {
                // Request permission
                this.startLocationPermissionRequest()
            })

        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            Log.i(TAG, "Requesting permission");
            this.startLocationPermissionRequest()
        }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     *
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null) {
                        lastPos =  LatLng(task.result.latitude, task.result.longitude)
                        if (lastPos != null)  {
                            //mMap.addMarker(MarkerOptions().position(lastPos as LatLng).title("Standort"));
                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPos as LatLng, 14f), 8000, null);
                        }
                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.exception)
                        showSnackbar(R.string.no_location_detected)
                    }
                }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap;

        val cameraOnFinish = object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                Log.d( TAG, "animation finnished");
            }
            override fun onCancel() {
                Log.d( TAG, "user canceled animation");
            }
        }

        val pos0 = LatLng(51.0,7.0);

        mMap.addMarker(MarkerOptions().position(pos0).title("51N 7E"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos0, 8f), 5000, cameraOnFinish);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
            // If user interaction was interrupted, the permission request is cancelled and you
            // receive empty arrays.
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")

            // Permission granted.
                (grantResults[0] == PERMISSION_GRANTED) -> getLastLocation()

            // Permission denied.

            // Notify the user via a SnackBar that they have rejected a core permission for the
            // app, which makes the Activity useless. In a real app, core permissions would
            // typically be best requested during a welcome-screen flow.

            // Additionally, it is important to remember that a permission might have been
            // rejected without asking the user for permission (device policy or "Never ask
            // again" prompts). Therefore, a user interface affordance is typically implemented
            // when permissions are denied. Otherwise, your app could appear unresponsive to
            // touches or interactions which have required permissions.
                else -> {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                            View.OnClickListener {
                                // Build intent that displays the App settings screen.
                                val intent = Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", APPLICATION_ID, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(intent)
                            })
                }
            }
        }
    }

}