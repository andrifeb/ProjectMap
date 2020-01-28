package com.mbul.projectmap.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mbul.projectmap.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

@SuppressWarnings("all")
public class UserNavigActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener,
        PermissionsListener {

    private static final int MAPBOX_LOGO_OPACITY = 75;
    private static final String TAG = "UserNavigActivity";

    private MapView mapView;
    private MapboxMap map;

    private LocationComponent locationComponent;
    private PermissionsManager permissionsManager;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Point destinationPoint;
    private Button navigateBtn, selesaiBtn;
    private ProgressBar progressBar;
    private NavigationMapRoute navigationMapRoute;
    private DirectionsRoute currentRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_navig);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {

            mapboxMap.setStyle(Style.LIGHT, style -> {

                UserNavigActivity.this.map = mapboxMap;

                addDestinationIconLayer(style);

                initLocationComponent(style);

                map.setMinZoomPreference(10);

                ImageView logo = mapView.findViewById(R.id.logoView);
                logo.setAlpha(MAPBOX_LOGO_OPACITY);

            });

        });

        initButtonStatus();

        navigateBtn = findViewById(R.id.navigateBtn);
        navigateBtn.setOnClickListener(v -> {
            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .shouldSimulateRoute(true)
                    .build();

            NavigationLauncher.startNavigation(UserNavigActivity.this, options);
        });

        selesaiBtn = findViewById(R.id.selesaiBtn);
        selesaiBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            db.collection("pekerjaan")
                    .whereEqualTo("user_uid", mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("selesai")
                                        .document(timeStamp)
                                        .set(document);
                                db.collection("pekerjaan")
                                        .document(document.getId())
                                        .delete()
                                        .addOnCompleteListener(task_selesai -> {

                                            if(task_selesai.isSuccessful()) {

                                                Toast.makeText(getApplicationContext(), "Reparasi telah selesai", Toast.LENGTH_SHORT).show();
                                                finish();
                                                startActivity(new Intent(UserNavigActivity.this, UserMainActivity.class));

                                            }

                                        });
                            }
                        } else {
                            Log.d(TAG, "updateStatus : Database Error");
                        }
                    });
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }

    private void initButtonStatus() {
        db.collection("pekerjaan")
                .whereEqualTo("user_uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       for(QueryDocumentSnapshot doc : task.getResult()) {
                           if(doc.getString("status").equals("Terima")) {
                               selesaiBtn.setVisibility(View.GONE);
                           } else if (doc.getString("status").equals("Selesai")) {
                               selesaiBtn.setVisibility(View.VISIBLE);
                           }
                       }
                   }
                });
    }

    private void initLocationComponent(@NonNull Style loadedMapStyle) {
        if(PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = map.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationComponent.setLocationComponentEnabled(true);

            locationComponent.setCameraMode(CameraMode.TRACKING);

            initLocationJasa();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initLocationJasa() {
        String jasaUid = getIntent().getStringExtra("jasa_uid");
        DocumentReference jasa = db.collection("jasa").document(jasaUid);
        jasa.get().addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               DocumentSnapshot doc = task.getResult();
               if(doc.getString("nama_toko") != null) {
                   Double latitude = doc.getDouble("latitude");
                   Double longitude = doc.getDouble("longitude");

                   destinationPoint = Point.fromLngLat(longitude, latitude);
                   GeoJsonSource source = map.getStyle().getSourceAs("current-source-id");
                   if(source != null) {
                       source.setGeoJson(Feature.fromGeometry(destinationPoint));
                   }

                   Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                           locationComponent.getLastKnownLocation().getLatitude());

                   getRoute(originPoint, destinationPoint);
               }
           }
        });
    }

    @SuppressWarnings("all")
    private void getRoute(Point originPoint, Point destinationPoint) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(originPoint)
                .destination(destinationPoint)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        Log.d(TAG, "Response code:" + response.code());
                        if(response.body() == null) {
                            Log.e(TAG, "Route tidak ditemukan, pastikan akun dan token anda benar.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "Route tidak ditemukan");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        if(navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, map,
                                    R.style.NavigationMapRoute);
                        }

                        navigationMapRoute.addRoute(currentRoute);

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, "Error: " + t.getMessage());
                    }
                });
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        // Toast
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted) {
            initLocationComponent(map.getStyle());
        } else {
            Toast.makeText(getApplicationContext(), "Izin lokasi tidak diterima", Toast.LENGTH_SHORT).show();
            finish();
        }
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

    public void onClick(View v) {
        finish();
        startActivity(new Intent(this, UserStatusActivity.class));
    }

    @Override
    public void onBackPressed() { }
}
