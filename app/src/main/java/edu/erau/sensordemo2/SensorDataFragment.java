package edu.erau.sensordemo2;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates the SensorDataFragment. This fragment calculates and displays the roll, pitch, latitude,
 * longitude, and bearing of the user's device. <p></p>
 * The fragment also features a toggle button that allows the user to stop collecting when the
 * button is in the "Off" state. There is also a "View Map" button that is only displayed in
 * portrait mode that takes the user's location and displays the location using the Google Maps
 * application. When the application is in landscape mode, the application shall consist of an
 * actionbar that contains a button, "Full Screen," to open the user's current location in the
 * Google Maps application.<p></p>
 * This fragment also uses the location sensors to update the map marker within the map fragment
 * displayed only in landscape mode.
 *
 * @author Cierra Sparks, Brandon Bielefeld
 * @date 3/5/2015
 */
public class SensorDataFragment extends Fragment implements SensorEventListener{
    // Buttons
    private Button viewButton;
    private Button btnFullScreen;
    private ToggleButton toggleBtn;

    // Data Fields
    private TextView rollUpdate;
    private TextView pitchUpdate;
    private TextView latitudeUpdate;
    private TextView latitudeDirectionUpdate;
    private TextView longitudeDirectionUpdate;

    private TextView longitudeUpdate;
    private TextView bearingUpdate;

    //declare strings that will concatenate for easy displaying
    String pitchStr, rollStr, latitudeStr, longitudeStr;



    // declare fields to store last position
    private static double untouchedLatitude;
    private static double untouchedLongitude;

    // Sensors
    private LocationManager locManager;
    private Location curLocation;
    private SensorManager sensorManager;
    private Sensor sensor;
    float[] rotationalMatrix = new float[9];
    float azimuth_angle;
    float pitch_angle;
    float roll_angle;
    private Timer logTimer;

    // Map variables
    private MapFragment mf;
    private GoogleMap map;
    private LatLng pos;
    boolean dualMode;

    // Format the number of decimals printed
    DecimalFormat decimalFormat = new DecimalFormat("0.####");

    /**
     * Configure all elements for the SensorDataFragment after the fragment has been created.
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Determine what to do about the sensor fragment panel
        View sensorPanel = getActivity().findViewById(R.id.sensorData);

        // Check if displaying 2 fragments or 1 fragment
        // dual mode will be true if 2 fragments are displayed
        dualMode = (sensorPanel != null) && (sensorPanel.getVisibility() == View.VISIBLE);

/*        // Source: FragmentDemonstration2
        if(savedInstanceState != null){
        }

        //
        if(dualMode){
        }*/

        // Acquire a reference to the system Location Manager
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Configure all fields with their appropriate id
        configureAllIds();

        // If in landscape mode, load the map fragment
        if(dualMode == false) {
            map = mf.getMap();
            startLocation();
        }

        toggleBtn.setChecked(true);  // start the sensors on startup

        // Register the listeners for the button and toggle button
        if(dualMode == false) {
            viewButton.setOnClickListener(sendListener);
            btnFullScreen.setOnClickListener(sendListener);
            toggleBtn.setOnCheckedChangeListener(checkListener);
        }


        // Get Sensor Manager Instance
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    /**
     * Inflates the fragment to fit the device's screen.
     *
     * Source: http://stackoverflow.com/questions/11532361/error-inflating-fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_sensor_data, container,false);
    }

    /**
     * Configures all XML layout IDs with their respective component within the fragment.
     */
    public void configureAllIds() {
        // Configure all buttons
        viewButton = (Button) getActivity().findViewById(R.id.btn_viewMap);
        //btnFullScreen = (Button) getActivity().findViewById(R.id.btn_fullScreen);
        toggleBtn = (ToggleButton) getActivity().findViewById(R.id.toggled);

        // Configure all text fields
        rollUpdate = (TextView) getActivity().findViewById(R.id.rollData);
        pitchUpdate = (TextView) getActivity().findViewById(R.id.pitchData);
        latitudeUpdate = (TextView) getActivity().findViewById(R.id.latitudeData);
        longitudeUpdate = (TextView) getActivity().findViewById(R.id.longitudeData);
        bearingUpdate = (TextView) getActivity().findViewById(R.id.bearingData);

        // latitudeDirectionUpdate = (TextView) getActivity().findViewById(R.id.latitudeDirection);
        // longitudeDirectionUpdate = (TextView) getActivity().findViewById(R.id.longitudeDirection);

        mf = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        pos = new LatLng(29.18857, -81.0487);    // stating location at Lehman Building at ERAU

        addViewButton();
    }

    /**
     * Creates a listener for the view map button to open the location in Google Maps.
     */
    View.OnClickListener sendListener = new View.OnClickListener() {
        public void onClick(View v) {
            openMap(v);
        }
    };

    /**
     * Called when the user selects the View Map Button. Opens the device's current coordinates
     * in Google Maps.
     *
     * @param view
     */
    public void openMap(View view) {
        // If the toggle button is "On" open the map with the current coordinates
        if (toggleBtn.isChecked() == true) {
            // Get the text from the appropriate text boxes
            // String strLatitude = latitudeUpdate.getText().toString(); //this variable used to be with the uri
            //  String strLongitude = longitudeUpdate.getText().toString(); //this variable used to be with the uri

            // Confirm data is received from user
            Log.i("Location Viewer Test", untouchedLatitude + ", " + untouchedLongitude);

            // Creates a geo URI and launches the appropriate intent
            //From developer.android.com/reference/android/content/Intent.html
            Uri uri = Uri.parse("geo:" + untouchedLatitude + "," + untouchedLongitude);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }

        // Otherwise do nothing if the button is toggled "Off"
        else{
            // Write to logcat of the current state
            Log.i("ToggleButtonDemo","The Toggle Button is currently de-selected");
        }
    }

    /**
     * Listener for the toggle button.
     * When toggle button is "On" collect sensor data, otherwise don't collect data.
     */
    CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                // Write to logcat of the current state
                Log.i("ToggleButtonDemo","The Toggle Button was selected.");

                onResume();
            }
            else{
                // Write to logcat of the current state
                Log.i("ToggleButtonDemo","The Toggle Button was de-selected");

                // Set the text from the xml to "0.00" for all data values
                setTextZero();
                onPause();  // pause the sensors
            }
        }
    };

    /**
     * Sets all data values (roll, pitch, latitude, longitude, and bearing) to "0.00".
     */
    public void setTextZero(){
        // Update the roll value
        rollUpdate.setText(R.string.sensor_value_placeholder);

        // Update the pitch value
        pitchUpdate.setText(R.string.sensor_value_placeholder);

        // Update the latitude value
        latitudeUpdate.setText(R.string.sensor_value_placeholder);

        // Update the longitude value
        longitudeUpdate.setText(R.string.sensor_value_placeholder);

        // Update the bearing value
        bearingUpdate.setText(R.string.sensor_value_placeholder);
    }

    /**
     * Creating a map fragment that is then zoomed to a position over ERAU campus.
     */
    public void startLocation(){
        if (map == null) {
            map = mf.getMap();

            if (map != null) {
                map.addMarker(new MarkerOptions().position(pos));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        }
        if (map != null) {
            map.addMarker(new MarkerOptions().position(pos));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
    }

    /**
     *  Programmatically adds the view map button to the Portrait layout.
     */
    public void addViewButton(){
        // if in landscape mode, don't show the view map button
        if(dualMode == false){
            //viewButton.setVisibility(View.INVISIBLE);
        }
        // show view button in portrait mode
        if(dualMode == true){
            //  viewButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Defines a listener that responds to location updates. Updates the map marker while in
     * landscape mode.
     *
     * Source: http://developer.android.com/guide/topics/location/strategies.html
     */
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i("Location", "Changed event occurred.");

            //If you have never had a location or you have a network based location, update the location to the Lehman Building at ERAU.
            if ((curLocation == null) || (curLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER))) {
                curLocation = location;

                // if in landscape mode, update the Google map marker location
                if(dualMode == false) {
                    newLocation(curLocation);
                }
            }

            //If you have a GPS location, always update the location.
            else if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                curLocation = location;

                // if in landscape mode, update the Google map marker location
                if(dualMode == false) {
                    newLocation(curLocation);
                }
            }

            //If you have not had a GPS update in over 2 minutes, you can use a network location to keep you going.
            else if ((location.getTime() - curLocation.getTime()) > (60*2*1000)) {
                curLocation = location;

                // if in landscape mode, update the Google map marker location
                if(dualMode == false) {
                    newLocation(curLocation);
                }
            }
            else {
                //Do nothing
            }
            untouchedLatitude = curLocation.getLatitude();
            untouchedLongitude = curLocation.getLongitude();
            updateLocation(curLocation.getLatitude(), curLocation.getLongitude(), curLocation.getBearing());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch(status) {
                case LocationProvider.AVAILABLE:
                    Log.i("LocationDemo", provider + "is AVAILABLE.");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i("LocationDemo", provider + "is TEMPORARILY UNAVAILABLE.");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i("LocationDemo", provider + "is OUT OF SERVICE.");
                    break;
                default:
            }
        }

        /**
         * Updates the Google map's marker location with the user's current location
         *
         * Source: http://android-er.blogspot.com/2013/02/convert-between-latlng-and-location.html
         * @param curLocation
         */
        public void newLocation(Location curLocation){
            //Convert Location to LatLng
            LatLng newLatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions().position(newLatLng).title(newLatLng.toString());

            map.addMarker(markerOptions);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("LocationDemo", provider + " Enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("LocationDemo", provider + " Disabled");
        }
    };

    /**
     * Updates the current latitude, longitude and bearing labels. The direction label is also
     * updated.
     *
     * @param latitude - the current value of the user's latitude
     * @param longitude - the current value of the user's longitude
     * @param bearing - the current value of the user's bearing
     */
    private void updateLocation(double latitude, double longitude, double bearing){



        // Get the cardinal direction of the current location
        // latitudeDirectionUpdate.setText(cardinalDirection(latitude));
        // longitudeDirectionUpdate.setText(cardinalDirection(longitude));
        // Update the text views with the current location
        if (latitude > 0) {
            latitudeStr = String.format("%.2f", latitude) + "\tN";
        } else {
            latitudeStr = String.format("%.2f", latitude * -1)  + "\tS";
        }
        if (longitude > 0) {
            longitudeStr = String.format("%.2f", longitude) + "\tE";
        } else {
            longitudeStr = String.format("%.2f", longitude * -1) + "\tW";
        }
        latitudeUpdate.setText(latitudeStr);
        longitudeUpdate.setText(longitudeStr);

        //latitudeUpdate.setText(decimalFormat.format(latitude).toString());
        //longitudeUpdate.setText(decimalFormat.format(longitude).toString());
        bearingUpdate.setText(decimalFormat.format(bearing).toString());
    }

    /**
     * Creates the timer to update the roll and pitch values displayed once every second.
     *
     * Source: http://www.steveody.com/?p=12
     */
    public void setupTimer(){
        logTimer = new Timer();
        logTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                startTimer();
            }
        }, 0, 1000);
    }

    /**
     * Timer to update the current sensor readings once every second
     * Called by the setupTimer() method.
     */
    private void startTimer()
    {
        getActivity().runOnUiThread(Timer_Tasks);
    }

    /**
     * Creates the thread to update the sensor readings in the text fields.
     * Called by the startTimer() method.
     */
    private Runnable Timer_Tasks = new Runnable() {
        @Override
        public void run() {
            rollUpdate.setText(decimalFormat.format(roll_angle).toString());
            pitchUpdate.setText(decimalFormat.format(pitch_angle *-1).toString());
        }
    };

    /**
     * Stops the timer and releases resources used by the timer.
     */
    public void stopTimer()
    {
        logTimer.cancel();
        logTimer.purge();
    }

    /**
     * Calculate the current cardinal direction of the device
     *
     * Source: http://stackoverflow.com/questions/2131195/cardinal-direction-algorithm-in-java
     * @param x - the current location of the device
     * @return - the current location of the device as a cardinal direction
     */
    public static String cardinalDirection(double x)
    {
        String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        // If the location is negative, make it positive
        if(Math.signum(x) == -1) {
            x = x * -1;
        }
        return directions[(int)Math.round(((x % 360) / 45)) % 8];
    }

    /**
     * Gets the device's current roll and pitch and update the global values.
     *
     * Source: http://stackoverflow.com/questions/14740808/android-problems-calculating-the-orientation-of-the-device
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Convert rotation-vector to a 4x4 matrix.
        sensorManager.getRotationMatrixFromVector(rotationalMatrix, event.values);
        sensorManager.getOrientation(rotationalMatrix, event.values);

        // Get and convert the values from radians to degrees
        azimuth_angle = (float) Math.toDegrees(event.values[0]);
        pitch_angle = (float) Math.toDegrees(event.values[1]);
        roll_angle = (float) Math.toDegrees(event.values[2]);
    }

    /**
     * Not implemented.
     *
     * @param sensor
     * @param accuracy
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * When the fragment is resumed, the location manager updates the location of the user's device
     * once every second. The timer for the roll and pitch sensors are resumed. If the device
     * is in landscape mode, the map fragment will also be displayed.
     */
    @Override
    public void onResume()
    {
        super.onResume();

        // Configure location providers every second
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);

        // Configure sensors
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // If in landscape mode, load the map fragment
        if(dualMode == false) {
            map = mf.getMap();
            startLocation();
        }
        addViewButton();

        // start the timer for roll and pitch sensors
        setupTimer();
    }

    /**
     * When the fragment is paused, the location manager, sensor manager, and the timer will all
     * release their resources and unregister their resources.
     */


    public void onPause()
    {
        super.onPause();

        //de-register location providers while paused.
        locManager.removeUpdates(locationListener);

        sensorManager.unregisterListener(this);
        stopTimer();
    }
    public static double getUntouchedLatitude() {
        return untouchedLatitude;
    }

    public static double getUntouchedLongitude() {
        return untouchedLongitude;
    }
}