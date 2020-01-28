package com.mbul.projectmap.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mbul.projectmap.R;
import com.mbul.projectmap.adapter.LocationRecyclerViewAdapter;
import com.mbul.projectmap.model.IndividualLocation;
import com.mbul.projectmap.tools.CustomThemeManager;
import com.mbul.projectmap.tools.Haversine;
import com.mbul.projectmap.tools.LinearLayoutManagerWithSmoothScroller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class UserMapActivity extends AppCompatActivity implements
        LocationRecyclerViewAdapter.ClickListener, MapboxMap.OnMapClickListener, PermissionsListener {

    private static final int MAPBOX_LOGO_OPACITY = 75;
    private static final Double CIRCLE = 2.0;
    private static final String PROPERTY_SELECTED = "selected";
    private static final String TAG = "UserMapActivity";

    private MapView mapView;
    private MapboxMap map;
    private CustomThemeManager customTheme;
    private RecyclerView locationRecyclerView;

    private LocationComponent locationComponent;
    private PermissionsManager permissionsManager;
    private FeatureCollection featureCollection;
    private ArrayList<IndividualLocation> listOfIndividualLocations;
    private List<Feature> featureList;
    private List<Feature> selectedFeature;

    private Button navigateBtn;
    private ProgressBar progressBar;

    @Override
    @SuppressWarnings("all")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_map);

        listOfIndividualLocations = new ArrayList<>();

        locationRecyclerView = findViewById(R.id.map_layout_rv);

        progressBar = findViewById(R.id.progressBar);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {

            customTheme = new CustomThemeManager(UserMapActivity.this);

            mapboxMap.setStyle(new Style.Builder().fromUrl(customTheme.getMapStyle()), style -> {
                
                UserMapActivity.this.map = mapboxMap;
                
                initLocationComponent(style);
                
                map.setMinZoomPreference(10);

                ImageView logo = mapView.findViewById(R.id.logoView);
                logo.setAlpha(MAPBOX_LOGO_OPACITY);

            });
        });

        navigateBtn = findViewById(R.id.navigateBtn);
        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Mengirim notifikasi ke tempat reparasi yang dipilih
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String jasaUid = selectedFeature.get(0).getStringProperty("uid");
                String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String status = "Permintaan";

                Map<String, Object> pekerjaanData = new HashMap<>();
                pekerjaanData.put("jasa_uid", jasaUid);
                pekerjaanData.put("user_uid", userUid);
                pekerjaanData.put("status", status);

                db.collection("pekerjaan")
                        .document(timeStamp)
                        .set(pekerjaanData)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Permintaan berhasil, silahkan tunggu", Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(UserMapActivity.this, UserMainActivity.class));
                            } else {
                                Toast.makeText(getApplicationContext(), "Permintaan gagal, mohon periksa jaringan", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    @Override
    @SuppressWarnings("all")
    // Memanggil fungsi klik pada map
    public boolean onMapClick(@NonNull LatLng point) {

        handleClickIcon(map.getProjection().toScreenLocation(point));

        if(handleClickIcon(map.getProjection().toScreenLocation(point))) {
            locationRecyclerView.setVisibility(View.VISIBLE);
        } else {
            locationRecyclerView.setVisibility(View.GONE);
        }

        return true;
    }

    @SuppressWarnings("all")
    // Memanggil fungsi klik pada marker lokasi jasa
    private boolean handleClickIcon(PointF screenPoint) {

        navigateBtn.setVisibility(View.GONE);

        selectedFeature = map.queryRenderedFeatures(screenPoint, "store-location-layer-id");
        if(!selectedFeature.isEmpty()) {
            String namaToko = selectedFeature.get(0).getStringProperty("nama_toko");
            List<Feature> featureList = featureCollection.features();

            for(int i = 0; i < featureList.size(); i++) {

                if(featureList.get(i).getStringProperty("nama_toko").equals(namaToko)) {
                    Point selectedFeaturePoint = (Point) featureList.get(i).geometry();

                    if(featureSelectStatus(i)) {
                        setFeatureSelectState(featureList.get(i), false);
                    } else {
                        setSelected(i);
                    }
                    if(selectedFeaturePoint.latitude() != locationComponent.getLastKnownLocation().getLatitude()) {
                        for(int x = 0; x < featureCollection.features().size(); x++) {

                            if(listOfIndividualLocations.get(x).getLocation().getLatitude() == selectedFeaturePoint.latitude()) {

                                locationRecyclerView.smoothScrollToPosition(x);
                            }
                        }
                    }
                } else {
                    setFeatureSelectState(featureList.get(i), false);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("all")
    // Memanggil fungsi klik pada RecyclerView lokasi jasa
    public void onItemClick(int position) {

        navigateBtn.setVisibility(View.VISIBLE);

        // mendapatkan lokasi individu yang telah dipilih dari posisi kartu di recyclerview
        IndividualLocation selectedLocation = listOfIndividualLocations.get(position);

        // evaluasi tiap feature yang dipilih untuk menyesuaikan style pada icon lokasi
        List<Feature> featureList = featureCollection.features();
        Point selectedLocationPoint = (Point) featureCollection.features().get(position).geometry();
        for(int i = 0; i < featureList.size(); i++) {
            if(featureList.get(i).getStringProperty("nama_toko").equals(selectedLocation.getNama_toko())) {
                if(featureSelectStatus(i)) {
                    setFeatureSelectState(featureList.get(i), false);
                } else {
                    setSelected(i);
                }
            } else {
                setFeatureSelectState(featureList.get(i), false);
            }
        }

        if(selectedLocation != null) {
            repositionMapCamera(selectedLocationPoint);
        }

        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
        locationComponent.getLastKnownLocation().getLatitude());

    }

    @SuppressWarnings("all")
    // Menampilkan lokasi user
    private void initLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = map.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            locationComponent.setLocationComponentEnabled(true);

            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Memanggil database setelah mendapatkan lokasi user untuk menggunakan Haversine
            getLocationData();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings("all")
    // Menampilkan semua marker lokasi jasa yang telah di saring sesuai jarak terdekat
    private void initStoreLocationSymbolLayer() {
        Style style = map.getStyle();
        if(style != null) {
            // menambah icon ke map
            style.addImage("store-location-icon-id", customTheme.getUnSelectedMarkerIcon());

            // membuat dan menambah GeoJSONSource ke map
            GeoJsonSource storeLocationGeoJsonSource = new GeoJsonSource("store-location-source-id");
            style.addSource(storeLocationGeoJsonSource);

            // membuat dan menambah icon SymbolLayer lokasi jasa ke map
            SymbolLayer storeLocationSymbolLayer = new SymbolLayer("store-location-layer-id",
                    "store-location-source-id");
            storeLocationSymbolLayer.withProperties(
                    iconImage("store-location-icon-id"),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
            );
            style.addLayer(storeLocationSymbolLayer);

        } else {
            Log.d("StoreFinderActivity", "initStoreLocationIconSymbolLayer: Style isn't ready yet");

            throw new IllegalStateException("Style isn't ready yet");
        }
    }

    @SuppressWarnings("all")
    // Menampilkan marker lokasi jasa yang telah dipilih
    private void initSelectedStoreSymbolLayer() {
        Style style = map.getStyle();
        if(style != null) {

            style.addImage("selected-store-location-icon-id", customTheme.getSelectedMarkerIcon());

            SymbolLayer selectedStoreLocationSymbolLayer = new SymbolLayer("selected-store-location-icon-id",
                    "store-location-source-id");
            selectedStoreLocationSymbolLayer.withProperties(
              iconImage("selected-store-location-icon-id"),
              iconAllowOverlap(true)
            );
            selectedStoreLocationSymbolLayer.withFilter(eq((get(PROPERTY_SELECTED)), literal(true)));
            style.addLayer(selectedStoreLocationSymbolLayer);
        } else {
            Log.d(TAG, "initSelectedStoreSymbolLayer: Style belum siap");
            throw new IllegalStateException("Style belum siap");
        }
    }

    @SuppressWarnings("all")
    // Mengecek apakah FeatureCollection kosong atau tidak, dan isi dari properti selected true atau false
    private boolean featureSelectStatus(int index) {
        if(featureCollection != null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }

    @SuppressWarnings("all")
    // Mengubah properti selected menjadi true
    private void setSelected(int index) {
        Feature feature = featureCollection.features().get(index);
        setFeatureSelectState(feature, true);
        refreshSource();
    }

    @SuppressWarnings("all")
    // Mengubah properti selected
    private void setFeatureSelectState(Feature feature, boolean selectedState) {
        feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
        refreshSource();
    }

    @SuppressWarnings("all")
    // Menampilkan kembali marker lokasi dari data yang telah berubah
    private void refreshSource() {
        GeoJsonSource source = map.getStyle().getSourceAs("store-location-source-id");
        if(source != null && featureCollection != null) {
            source.setGeoJson(featureCollection);
        }
    }

    @SuppressWarnings("all")
    // Menampilkan marker lokasi jasa dan menyimpan data lokasi jasa tunggal
    private void addStoreLocationSymbolLayer() {

        // mengambil dan mengupdate source untuk memunculkan icon lokasi jasa
        GeoJsonSource source = map.getStyle().getSourceAs("store-location-source-id");

        if(source != null) {
            source.setGeoJson(FeatureCollection.fromFeatures(featureList));
        }

        if(featureList != null) {

            for(int x = 0; x < featureList.size(); x++) {

                Feature singleLocation = featureList.get(x);

                // mengambil String dari properti single lokasi untuk menempatkan pada marker map
                String singleLocationUid = singleLocation.getStringProperty("uid");
                String singleLocationNamaToko = singleLocation.getStringProperty("nama_toko");
                String singleLocationAlamat = singleLocation.getStringProperty("alamat");
                String singleLocationNoTelp = singleLocation.getStringProperty("no_telp");

                // menambah property boolean untuk digunakan sebagai penanda lokasi yang dipilih

                // mengambil koordinat LatLng dari single lokasi
                Point singleLocationPosition = (Point) singleLocation.geometry();

                // membuat objek LatLng baru dengan objek Position diatas
                LatLng singleLocationLatLng = new LatLng(singleLocationPosition.latitude(),
                        singleLocationPosition.longitude());

                // menambah lokasi ke ArrayList lokasi untuk nanti digunakan pada recycleview
                listOfIndividualLocations.add(new IndividualLocation(
                        singleLocationUid,
                        singleLocationNamaToko,
                        singleLocationAlamat,
                        singleLocationNoTelp,
                        singleLocationLatLng
                ));

            }

        }

    }

    @Override
    // Memanggil fungsi permintaan izin lokasi
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        // Toast
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onPermissionResult(boolean granted) {
        if(granted) {
            initLocationComponent(map.getStyle());
        } else {
            Toast.makeText(getApplicationContext(), "Izin lokasi tidak diterima", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Memindahkan posisi kamera map pada target lokasi jasa menjadi tengah
    private void repositionMapCamera(Point newTarget) {
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(new LatLng(newTarget.latitude(), newTarget.longitude()))
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 1200);
    }

    @SuppressWarnings("all")
    // Memanggil fungsi database dan inisiasi tampilan marker lokasi jasa pada map
    private void getLocationData() {

        featureList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("jasa")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                if(document.getString("available_status").equals("buka")) {
                                    getDataJasa(document);
                                }
//                              Log.d(TAG, "Database fetch" + document.getData().toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        featureCollection = FeatureCollection.fromFeatures(featureList);

                        // konfigurasi SymbolLayer yang akan menampilkan icon untuk setiap tempat jasa reparasi

                        initStoreLocationSymbolLayer();

                        initSelectedStoreSymbolLayer();

                        addStoreLocationSymbolLayer();

                        setUpRecyclerViewLocationCards();

                        map.addOnMapClickListener(UserMapActivity.this);

                    } else {
                        Log.d(TAG, "Error get database");
                    }
                });

    }

    @SuppressWarnings("all")
    // Memindahkan data dari database ke dalam variabel
    private void getDataJasa(QueryDocumentSnapshot doc) throws JSONException {

        String namaToko = doc.getData().get("nama_toko").toString();
        String email = doc.getData().get("email").toString();
        String noTelp = doc.getData().get("no_telp").toString();
        String alamat = doc.getData().get("alamat").toString();
        String uid = doc.getId();
        String level = doc.getData().get("level").toString();

        Double latToko = doc.getDouble("latitude");
        Double lngToko = doc.getDouble("longitude");

//        Log.d(TAG, "getDataUser Result : " + namaToko +" | "+ email +" | "+ noTelp +" | "+
//                alamat +" | "+ kecamatan +" | "+ uid +" | "+ latToko +" | "+ lngToko);

        getFeatureListFromJson(namaToko, email, noTelp, alamat, uid, latToko, lngToko);

    }

    // Membuat data menjadi format JSON untuk di ubah ke dalam FeatureCollection sementara
    private void getFeatureListFromJson(String namaToko, String email, String noTelp, String alamat,
                                        String uid, Double latToko, Double lngToko) throws JSONException {

        JSONObject featureCollecting = new JSONObject();
        featureCollecting.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();
        JSONObject feature = new JSONObject();

        feature.put("type", "Feature");

        JSONObject properties = new JSONObject();
        properties.put("nama_toko", namaToko);
        properties.put("email", email);
        properties.put("no_telp", noTelp);
        properties.put("alamat", alamat);
        properties.put("uid", uid);
        feature.put("properties", properties);

        JSONObject geometry = new JSONObject();
        JSONArray JSONArrayCoord = new JSONArray();
        JSONArrayCoord.put(lngToko);
        JSONArrayCoord.put(latToko);
        geometry.put("type", "Point");
        geometry.put("coordinates", JSONArrayCoord);
        feature.put("geometry", geometry);

        features.put(feature);
        featureCollecting.put("features", features);

//        Log.d(TAG, "getFeatureListFromJson Result : " + featureCollecting);

        FeatureCollection temporareFeatures = FeatureCollection.fromJson(featureCollecting.toString());

        getFeatureListFromJsonFiltered(temporareFeatures, latToko, lngToko);

    }

    // Implementasi ALGORITMA HAVERSINE dan memindahkan FeatureCollection sementara ke dalam FeatureList
    @SuppressWarnings("all")
    private void getFeatureListFromJsonFiltered(FeatureCollection tempFeatureCollection, Double endLat, Double endLng) {
        Double originLat = locationComponent.getLastKnownLocation().getLatitude();
        Double originLng = locationComponent.getLastKnownLocation().getLongitude();

        Double distance = Haversine.distance(originLat, originLng, endLat, endLng);

        // Menyaring data feature sesuai dengan jarak yang ditentukan
        if(distance <= CIRCLE) {
            Log.d(TAG, "Distance Result : " + distance);
            featureList.add(tempFeatureCollection.features().get(0));
        }
    }

    // Mengatur informasi lokasi dalam bentuk RecyclerView
    private void setUpRecyclerViewLocationCards() {
        // Inisiasi kartu lokasi dalam recyclerview dan class untuk otomatisasi geser kartu
        locationRecyclerView.setHasFixedSize(true);
        locationRecyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(this));
        LocationRecyclerViewAdapter styleRvAdapter = new LocationRecyclerViewAdapter(listOfIndividualLocations,
                getApplicationContext(), this);
        locationRecyclerView.setAdapter(styleRvAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(locationRecyclerView);
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
        startActivity(new Intent(this, UserMainActivity.class));
    }

    @Override
    public void onBackPressed() { }
}

