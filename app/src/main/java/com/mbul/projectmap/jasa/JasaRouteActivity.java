package com.mbul.projectmap.jasa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mbul.projectmap.R;
import com.mbul.projectmap.RouteActivity;

@SuppressWarnings("all")
public class JasaRouteActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText namaTokoInput, alamatInput, noTelpInput;
    String email;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_jasa_route);

        namaTokoInput = findViewById(R.id.namaTokoInput);
        alamatInput = findViewById(R.id.alamatInput);
        noTelpInput = findViewById(R.id.noTelpInput);

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initDataJasa();
    }

    private void initDataJasa() {
        DocumentReference dataJasa = db.collection("jasa")
                .document(mAuth.getCurrentUser().getUid());
        dataJasa.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if(doc.getString("nama_toko") != null) {
                    namaTokoInput.setText(doc.get("nama_toko").toString(), TextView.BufferType.EDITABLE);
                    alamatInput.setText(doc.get("alamat").toString(), TextView.BufferType.EDITABLE);
                    noTelpInput.setText(doc.get("no_telp").toString(), TextView.BufferType.EDITABLE);
                }
            }
        });

    }

    @SuppressWarnings("all")
    private void registerJasa() {
        FirebaseUser user = mAuth.getCurrentUser();

        email = user.getEmail();
        final String nama_toko = namaTokoInput.getText().toString().trim();
        String alamat = alamatInput.getText().toString().trim();
        String no_telp = noTelpInput.getText().toString().trim();
        final String level = "jasa";
        final String availableStatus = "tutup";

        if(nama_toko.isEmpty()) {
            namaTokoInput.setError("Nama toko tidak boleh kosong");
            namaTokoInput.requestFocus();
            return;
        }

        if(alamat.isEmpty()) {
            alamatInput.setError("Alamat toko tidak boleh kosong");
            alamatInput.requestFocus();
            return;
        }

        if(no_telp.isEmpty()) {
            noTelpInput.setError("Nomor Telepon tidak boleh kosong");
            noTelpInput.requestFocus();
            return;
        }

        if(!Patterns.PHONE.matcher(no_telp).matches()) {
            noTelpInput.setError("Mohon input nomor yang benar");
            noTelpInput.requestFocus();
            return;
        }

        Intent myIntent = new Intent(JasaRouteActivity.this, JasaMapActivity.class);
        myIntent.putExtra("email", email);
        myIntent.putExtra("nama_toko", nama_toko);
        myIntent.putExtra("alamat", alamat);
        myIntent.putExtra("no_telp", no_telp);
        myIntent.putExtra("level", level);
        myIntent.putExtra("available_status", availableStatus);
        finish();
        startActivity(myIntent);
    }

    public void onClick(View v) {
        registerJasa();
    }

    @Override
    public void onBackPressed() {
        DocumentReference dataJasa = db.collection("jasa")
                .document(mAuth.getCurrentUser().getUid());
        dataJasa.get().addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               DocumentSnapshot doc = task.getResult();
               if(doc.getString("nama_toko") != null) {
                   finish();
                   startActivity(new Intent(this, JasaMainActivity.class));
               } else {
                   finish();
                   startActivity(new Intent(this, RouteActivity.class));
               }
           }
        });
    }
}
