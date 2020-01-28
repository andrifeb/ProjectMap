package com.mbul.projectmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText emailInput, passInput;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_regis);

        emailInput = findViewById(R.id.emailInput);
        passInput = findViewById(R.id.passInput);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
    }

    private void register() {
        String email = emailInput.getText().toString().trim();
        String password = passInput.getText().toString().trim();

        if(email.isEmpty()) {
            emailInput.setError("Email tidak boleh kosong");
            emailInput.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Mohon input email yang benar");
            emailInput.requestFocus();
            return;
        }

        if(password.isEmpty()) {
            passInput.setError("Password tidak boleh kosong");
            passInput.requestFocus();
            return;
        }

        if(password.length()<6) {
            passInput.setError("Minimum password harus 6 karakter");
            passInput.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                finish();
                startActivity(new Intent(RegisActivity.this, LoadingActivity.class));
            } else {
                if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(getApplicationContext(), "Email kamu telah terdaftar", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void submitRegis(View view) {
        register();
    }

    public void toLogin(View v) {
        if(v == findViewById(R.id.toLogin)){
            finish();
            startActivity(new Intent(RegisActivity.this, LoginActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(RegisActivity.this, LoginActivity.class));
    }
}
