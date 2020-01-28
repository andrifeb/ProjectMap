package com.mbul.projectmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mbul.projectmap.jasa.JasaMainActivity;
import com.mbul.projectmap.user.UserMainActivity;

public class LoadingActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_loading);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        checkUserInformation();
    }

    @SuppressWarnings("all")
    private void checkUserInformation() {

        final FirebaseUser user = mAuth.getCurrentUser();

        DocumentReference docRef = db.collection("user").document(user.getUid());

        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()) {
                    if(document.getString("level").equals("user")) {
                        finish();
                        startActivity(new Intent(LoadingActivity.this, UserMainActivity.class));
                    } else {
                        Toast.makeText(LoadingActivity.this, "Level tidak diketahui", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    DocumentReference doc = db.collection("jasa").document(user.getUid());
                    doc.get().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()) {
                            DocumentSnapshot document1 = task1.getResult();
                            if(document1.exists()) {
                                if(document1.getString("level").equals("jasa")) {
                                    finish();
                                    startActivity(new Intent(LoadingActivity.this, JasaMainActivity.class));
                                } else {
                                    Toast.makeText(LoadingActivity.this, "Level tidak diketahui", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                finish();
                                startActivity(new Intent(LoadingActivity.this, RouteActivity.class));
                                Toast.makeText(getApplicationContext(), "Silahkan masuk sebagai..", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoadingActivity.this, "Kesalahan pada koneksi", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            } else {
                Toast.makeText(LoadingActivity.this, "Kesalahan pada koneksi", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
