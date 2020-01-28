package com.mbul.projectmap.user;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mbul.projectmap.LoginActivity;
import com.mbul.projectmap.R;

@SuppressWarnings("all")
public class UserMainActivity extends AppCompatActivity {

    private static final String TAG = "UserMainActivity";
    private static final String DITERIMA = "Diterima";
    private static final String DITOLAK = "Ditolak";
    private static final String TUNGGU = "Tunggu";

    TextView statusTv;
    CardView toMaps, toStatus;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_main);

        statusTv = findViewById(R.id.statusTv);
        toMaps = findViewById(R.id.toMaps);
        toStatus = findViewById(R.id.toStatus);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initMenu();

        realtimeStatus();
    }

    private void initMenu() {
        db.collection("pekerjaan")
                .whereEqualTo("user_uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       for(QueryDocumentSnapshot document : task.getResult()) {
                           if(document.getString("user_uid") != null) {
                               toMaps.setVisibility(View.GONE);
                               toStatus.setVisibility(View.VISIBLE);
                           } else {
                               toMaps.setVisibility(View.VISIBLE);
                               toStatus.setVisibility(View.GONE);
                           }
                       }
                   } else {
                        Toast.makeText(getApplicationContext(), "Kesalahan pada database", Toast.LENGTH_SHORT).show();
                   }
                });
    }

    private void realtimeStatus() {
        CollectionReference collRef = db.collection("pekerjaan");
        collRef.whereEqualTo("user_uid", mAuth.getCurrentUser().getUid())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e!=null){
                        Log.d(TAG,"Listen stopped: "+e.getMessage());
                    } else {
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if(document.getString("status").equals("Tolak")) {
                                statusTv.setText(DITOLAK);
                                statusTv.setBackground(getDrawable(R.drawable.corner_status_red));
                            } else if(document.getString("status").equals("Terima")) {
                                statusTv.setText(DITERIMA);
                                statusTv.setBackground(getDrawable(R.drawable.corner_status_blue));
                            } else if(document.getString("status").equals("Permintaan")) {
                                statusTv.setText(TUNGGU);
                                statusTv.setBackground(getDrawable(R.drawable.corner_status_blue));
                            } else {
                                statusTv.setText(document.getString("status"));
                                statusTv.setBackground(getDrawable(R.drawable.corner_status_blue));
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);

        MenuItem namaItem = menu.findItem(R.id.namaItem);
        db.collection("user")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        namaItem.setTitle("\t" + doc.getString("nama"));
                    }
                });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ubahData:
                finish();
                startActivity(new Intent(this, UserRouteActivity.class));
                return true;
            case R.id.signOut:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                Toast.makeText(getApplicationContext(), "Logout berhasil", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    public void onClick(View v) {
        if (v == toMaps) {
            finish();
            startActivity(new Intent(this, UserMapActivity.class));
        }
        if (v == toStatus) {
            finish();
            startActivity(new Intent(this, UserStatusActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Apakah kamu yakin ingin keluar?")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialog, id) -> UserMainActivity.this.finish())
                .setNegativeButton("Tidak", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
