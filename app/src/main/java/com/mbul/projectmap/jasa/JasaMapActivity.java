package com.mbul.projectmap.jasa;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mbul.projectmap.R;

import java.util.HashMap;
import java.util.Map;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

@SuppressWarnings("all")
public class JasaMapActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

    private static final int MAPBOX_LOGO_OPACITY = 75;
    private static final String TAG = "JasaMapActivity";

    private MapView mapView;
    private MapboxMap map;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Point currentPoint;
    private Button daftarBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_jasa_map);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {

            mapboxMap.setStyle(Style.LIGHT, style -> {

                JasaMapActivity.this.map = mapboxMap;

                map.setMinZoomPreference(10);

                ImageView logo = mapView.findViewById(R.id.logoView);
                logo.setAlpha(MAPBOX_LOGO_OPACITY);

                addDestinationIconLayer(style);

                initLocationJasa();

                map.addOnMapClickListener(JasaMapActivity.this);

            });
        });

        daftarBtn = findViewById(R.id.daftarBtn);
        daftarBtn.setOnClickListener(v -> {

            FirebaseUser user = mAuth.getCurrentUser();
            String uId = user.getUid();
            double latitude = 0.0;
            double longitude = 0.0;

            if(currentPoint != null) {
                latitude = currentPoint.latitude();
                longitude = currentPoint.longitude();
            }

            Map<String, Object> lokasiJasa = new HashMap<>();
            lokasiJasa.put("latitude", latitude);
            lokasiJasa.put("longitude", longitude);
            lokasiJasa.put("email", getIntent().getStringExtra("email"));
            lokasiJasa.put("nama_toko", getIntent().getStringExtra("nama_toko"));
            lokasiJasa.put("alamat", getIntent().getStringExtra("alamat"));
            lokasiJasa.put("no_telp", getIntent().getStringExtra("no_telp"));
            lokasiJasa.put("level", getIntent().getStringExtra("level"));
            lokasiJasa.put("available_status", getIntent().getStringExtra("available_status"));

            progressBar.setVisibility(View.VISIBLE);

            if(currentPoint == null) {

                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Lokasi tidak boleh kosong", Toast.LENGTH_SHORT).show();

            } else {

                db.collection("jasa")
                        .document(uId)
                        .set(lokasiJasa)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Pendaftaran berhasil", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(JasaMapActivity.this, JasaMainActivity.class));
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.kesalahan), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    private void initLocationJasa() {
        DocumentReference jasa = db.collection("jasa").document(mAuth.getCurrentUser().getUid());
        jasa.get().addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               DocumentSnapshot doc = task.getResult();
               if(doc.getString("nama_toko") != null) {
                   Double latitude = doc.getDouble("latitude");
                   Double longitude = doc.getDouble("longitude");

                   currentPoint = Point.fromLngLat(longitude, latitude);
                   GeoJsonSource source = map.getStyle().getSourceAs("current-source-id");
                   if(source != null) {
                       source.setGeoJson(Feature.fromGeometry(currentPoint));
                   }

               }
           }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        currentPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

        GeoJsonSource source = map.getStyle().getSourceAs("current-source-id");

        if(source != null) {
            source.setGeoJson(Feature.fromGeometry(currentPoint));
        }

        return true;
    }

    private void addDestinationIconLayer(Style style) {
        style.addImage("current-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));

        GeoJsonSource geoJsonSource = new GeoJsonSource("current-source-id");
        style.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer = new SymbolLayer("current-symbol-layer-id", "current-source-id");
        destinationSymbolLayer.withProperties(iconImage("current-icon-id"), iconAllowOverlap(true),
                iconIgnorePlacement(true));

        style.addLayer(destinationSymbolLayer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


}
