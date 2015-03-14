package edu.erau.sensordemo2;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Creates the Main Activity to launch the SensorDataFragment.
 *
 * @author Cierra Sparks, Brandon Bielefeld
 * @date 3/5/2015
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * This is important.  If you have already created the fragment, then you do not want to
         * recreate it.  You could add an else to handle the restoration of savedInstanceState
         * if applicable.  It was not needed here.
         *
         * If you do not have this transaction in the if, you will also notice that when you rotate
         * the screen that weird things will happen as it will try to recreate the fragment.
         *
         * Source: class example DynamicFragmentDemo
         */
        if(savedInstanceState == null) {
            //Creating a transaction
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            //Adding the fragment to the activity in the appropriate container
            ft.add(R.id.sensorData, new SensorDataFragment(), "sensor_data_fragment");

            //Committing the change so that it is actually processed.
            ft.commit();
        }
    }

    /**
     * referenced from the flight data recorder example. This method populates the actionbar with clickable items
     * @param menu passed the menu layout which populates the actionbar
     * @return the inflated actionbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * this method was referenced from the flight data recorder example.
     * @param item the item selected from the actionbar
     * @return the item selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.dual_view_fullscreen:
                loadExportActivity(); //Handler
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * this method was referenced from the flight data recorder example.
     * this creates a new intent that is passed the user's current gps coordinates
     */
    public void loadExportActivity()
    {
        // Creates a geo URI and launches the appropriate intent
        // From developer.android.com/reference/android/content/Intent.html
        Uri uri = Uri.parse("geo:" + SensorDataFragment.getUntouchedLatitude() + "," + SensorDataFragment.getUntouchedLongitude());
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
