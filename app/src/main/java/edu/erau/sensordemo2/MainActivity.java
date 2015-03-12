package edu.erau.sensordemo2;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Cierra on 3/5/2015.
 */
public class MainActivity extends Activity {

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
         */
        if(savedInstanceState == null) {
            //Creating a transaction
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            //Adding the fragment to the activity in the appropriate container
            ft.add(R.id.sensorData, new SensorDataFragment(), "sensor_data_fragment");

            //Committing the change so that it is actually processed.
            ft.commit();
        }

  /*      MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        GoogleMap map = mf.getMap();

        LatLng pos = new LatLng(29.18857,-81.0487);
        map.addMarker(new MarkerOptions().position(pos));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15)); */
    }
}
