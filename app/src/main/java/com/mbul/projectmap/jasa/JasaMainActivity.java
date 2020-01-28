package com.mbul.projectmap.jasa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mbul.projectmap.LoginActivity;
import com.mbul.projectmap.R;

@SuppressWarnings("all")
public class JasaMainActivity extends AppCompatActivity {

    private static final String TAG = "JasaMainActivity";

    private TextView permintaanStatus;
    private Button bukaBtn, tutupBtn;
    private RelativeLayout permintaanLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_jasa_main);

        permintaanStatus = findViewById(R.id.permintaanStatus);
        bukaBtn = findViewById(R.id.bukaBtn);
        tutupBtn = findViewById(R.id.tutupBtn);
        permintaanLayout = findViewById(R.id.permintaanLayout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initAvailableStatus();

        realtimeRequestStatus();
    }

    private void initAvailableStatus() {
        DocumentReference user = db.collection("jasa")
                .document(mAuth.getCurrentUser().getUid());
        user.get().addOnCompleteListener(task -> {
            DocumentSnapshot doc = task.getResult();
            String availableStatus = doc.get("available_status").toString();
            if(availableStatus.equals("buka")) {
                bukaBtn.setTextColor(getResources().getColor(R.color.colorYellow));
                tutupBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            } else if (availableStatus.equals("tutup")) {
                tutupBtn.setTextColor(getResources().getColor(R.color.colorYellow));
                bukaBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            } else {
                Toast.makeText(getApplicationContext(), "status masih kosong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void realtimeRequestStatus() {
        CollectionReference collRef = db.collection("pekerjaan");
        collRef.whereEqualTo("jasa_uid", mAuth.getCurrentUser().getUid())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    int count = 0;
                    if (e!=null){
                        Log.d(TAG,"Listen stopped: "+e.getMessage());
                    } else {
                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if(doc.getString("status").equals("Permintaan")) {
                                count++;

                                permintaanLayout.setVisibility(View.VISIBLE);
                                permintaanStatus.setText(String.valueOf(count));
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
        db.collection("jasa")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        namaItem.setTitle("\t" + doc.getString("nama_toko"));
                    }
                });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ubahData:
                finish();
                startActivity(new Intent(this, JasaRouteActivity.class));
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

    public void onClickStatus(View v) {

        DocumentReference status = db.collection("jasa")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if(v == bukaBtn) {
            bukaBtn.setTextColor(getResources().getColor(R.color.colorYellow));
            tutupBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            status.update("available_status", "buka").addOnSuccessListener(aVoid ->
                    Toast.makeText(getApplicationContext(), "Jasa anda telah dibuka", Toast.LENGTH_SHORT).show());
        }
        if(v == tutupBtn) {
            tutupBtn.setTextColor(getResources().getColor(R.color.colorYellow));
            bukaBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            status.update("available_status", "tutup").addOnSuccessListener(aVoid ->
                    Toast.makeText(getApplicationContext(), "Jasa anda telah ditutup", Toast.LENGTH_SHORT).show());
        }
    }

    public void onClick(View v) {
        finish();
        startActivity(new Intent(JasaMainActivity.this, JasaWorkActivity.class));
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Apakah kamu yakin ingin keluar?")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialog, id) -> JasaMainActivity.this.finish())
                .setNegativeButton("Tidak", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
