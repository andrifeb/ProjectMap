package com.mbul.projectmap.user;

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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mbul.projectmap.R;
import com.mbul.projectmap.RouteActivity;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class UserRouteActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText namaInput, noTelpInput;
    String email;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_route);

        namaInput = findViewById(R.id.namaInput);
        noTelpInput = findViewById(R.id.noTelpInput);

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initDataUser();
    }

    private void initDataUser() {
        DocumentReference dataUser = db.collection("user")
                .document(mAuth.getCurrentUser().getUid());
        dataUser.get().addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               DocumentSnapshot doc = task.getResult();
               if(doc.getString("nama") != null) {
                   namaInput.setText(doc.get("nama").toString(), TextView.BufferType.EDITABLE);
                   noTelpInput.setText(doc.get("no_telp").toString(), TextView.BufferType.EDITABLE);
               }
           }
        });
    }

    private void registerUser() {
        FirebaseUser user = mAuth.getCurrentUser();

        email = user.getEmail();
        final String nama = namaInput.getText().toString().trim();
        String no_telp = noTelpInput.getText().toString().trim();
        final String level = "user";

        if(nama.isEmpty()) {
            namaInput.setError("Nama tidak boleh kosong");
            namaInput.requestFocus();
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

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> userdata = new HashMap<>();
        userdata.put("email", email);
        userdata.put("nama", nama);
        userdata.put("no_telp", no_telp);
        userdata.put("level", level);

        db.collection("user")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(userdata)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        finish();
                        startActivity(new Intent(UserRouteActivity.this, UserMainActivity.class));
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), getString(R.string.kesalahan), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onClick(View v) {
        registerUser();
    }

    @Override
    public void onBackPressed() {
        DocumentReference dataUser = db.collection("user")
                .document(mAuth.getCurrentUser().getUid());
        dataUser.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.getString("nama") != null) {
                    finish();
                    startActivity(new Intent(this, UserMainActivity.class));
                } else {
                    finish();
                    startActivity(new Intent(this, RouteActivity.class));
                }
            }
        });
    }
}
