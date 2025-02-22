package com.example.minhopark.View;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.Switch;

import com.example.MinhoPark.R;
import com.example.MinhoPark.databinding.ActivityMaps2Binding;
import com.example.minhopark.View.directionHelpers.FetchURL;
import com.example.minhopark.View.directionHelpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private ActivityMaps2Binding binding;
    private Polyline currentPolyline;

    private LatLng utilizador;
    private LatLng parque;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String[] coordUtilizadores = getIntent().getStringExtra("loc").split(",");
        String[] coordParque = getIntent().getStringExtra("coordenadas").split(",");

        String veiculo = getIntent().getStringExtra("veiculo");

        boolean portagem = false;
        if(getIntent().getStringExtra("portagem").equals("true")) portagem = true;


        String mode = null;

        if(veiculo.equals("pe")) mode = "walking";
        if(veiculo.equals("bicicleta")) mode = "cycling";
        if(veiculo.equals("carro")) mode = "driving";

        utilizador = new LatLng(Double.parseDouble(coordUtilizadores[0]),Double.parseDouble(coordUtilizadores[1]));
        parque = new LatLng(Double.parseDouble(coordParque[0]),Double.parseDouble(coordParque[1]));


        binding = ActivityMaps2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if(!portagem && veiculo.equals("carro")){
            String url = getUrlSemPortagens(utilizador, parque);
            new FetchURL(MapsActivity.this).execute(url, mode);
        }else{
            String url = getUrl(utilizador, parque, mode);
            new FetchURL(MapsActivity.this).execute(url, mode);
        }

    }


    private String getUrlSemPortagens(LatLng origin, LatLng dest){
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "avoid=tolls&mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);

        return url;
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        adicionaMarker(mMap, utilizador, "Minha Localização");
        adicionaMarker(mMap, parque, "Parque");

        mMap.moveCamera(CameraUpdateFactory.newLatLng(parque));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(parque, 12.0f));
    }


    public LatLng adicionaMarker(GoogleMap mMap, double latitude, double longitude, String name) {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title(name));

        return latLng;
    }

    public void adicionaMarker(GoogleMap mMap, LatLng latLng, String name) {
        mMap.addMarker(new MarkerOptions().position(latLng).title(name));
    }


    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        else {
            currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        }
    }
}