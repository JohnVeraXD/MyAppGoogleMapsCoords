package com.example.myappgooglemapscoords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    GoogleMap mapa;
    List<LatLng> listapuntos;
    PolylineOptions lineas;

    long distancia = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listapuntos = new ArrayList<>();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        //Ya seta conectado el mapa
        mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mapa.getUiSettings().setZoomControlsEnabled(true);
        //Mover mapa a una ubicacion
        LatLng madrid = new LatLng(40.689838118503765, -74.04504323453487);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(madrid)
                .zoom(19)
                .bearing(45) //noreste arriba
                .tilt(70) //punto de vista de la cámara 70 grados
                .build();
        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);
        mapa.animateCamera(camUpd3);
        mapa.setOnMapClickListener(this);

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        LatLng punto = new LatLng(latLng.latitude, latLng.longitude);
        MarkerOptions marcador = new MarkerOptions();
        marcador.position(latLng);
        marcador.title("punto");

        mapa.addMarker(marcador);

        listapuntos.add(latLng);
        if (listapuntos.size()==6){
            lineas = new PolylineOptions();
            for(int i = 0; i < 6; i++) {
                lineas.add(new LatLng(listapuntos.get(i).latitude, listapuntos.get(i).longitude));

                String corde1;
                String corde2 = "";

                //Mandar las coordenada para que calcula la distancia
                //Coordenada de origin
                corde1 = Double.toString(listapuntos.get(i).latitude)+","+Double.toString(listapuntos.get(i).longitude);
                if(i < 5){
                    //coordenada de destino
                corde2 = Double.toString(listapuntos.get(i+1).latitude)+","+Double.toString(listapuntos.get(i+1).longitude);
                }

                if (i == 5) {
                    lineas.add(new LatLng(listapuntos.get(0).latitude, listapuntos.get(0).longitude));
                    corde2 = Double.toString(listapuntos.get(0).latitude)+","+Double.toString(listapuntos.get(0).longitude);
                }

                //LLamar a la Api para mardale las coordenadas
                CalcularDistanciaAPi(corde1,corde2);

                lineas.width(8);
                lineas.color(Color.RED);
                mapa.addPolyline(lineas);
            }
            Log.d("Distancia suma", String.valueOf(distancia));
            Toast.makeText(MainActivity.this,"Distancia en metros: "+distancia,
                    Toast.LENGTH_SHORT).show();
            listapuntos.clear();
            distancia = 0;
        }
    }


    public void CalcularDistanciaAPi(String coord1,String coord2){
        //LLamar a la api con Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        Log.d("LSita1",coord1);
        Log.d("Lista2",coord2);

        // Especificar la URL base
        String urlbase = "https://maps.googleapis.com/maps/api/distancematrix/json?";

        // Construir la URL con parámetros utilizando Uri.Builder para manda a la APi
        Uri.Builder builder = Uri.parse(urlbase).buildUpon();
        builder.appendQueryParameter("key", "AIzaSyDMmRXHBYOjJyXZruXemR11tl7uiJ2T_Q8");
        builder.appendQueryParameter("origins", coord1);
        builder.appendQueryParameter("destinations",coord2);
        builder.appendQueryParameter("units", "meters");
        String url = builder.build().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Parcear la respuesta de la API
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            JSONArray rowsArray = jsonObject.getJSONArray("rows");
                            JSONObject elementsObject = rowsArray.getJSONObject(0).getJSONArray("elements").getJSONObject(0);
                            JSONObject distanceObject = elementsObject.getJSONObject("distance");
                            //Sumar la distancia
                            int op = distanceObject.getInt("value");
                            distancia = distancia + op;
                            Log.d("distancia", String.valueOf(distancia));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,"Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        //textView.setText("That didn't work!");
                    }
                });
        queue.add(stringRequest);

    }
}