package com.mbul.projectmap;

import androidx.appcompat.app.AlertDialog;
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

public class LoginActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText emailInput, passInput;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passInput = findViewById(R.id.passInput);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, LoadingActivity.class));
        }
    }

    private void userLogin() {
        final String email = emailInput.getText().toString().trim();
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

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if(task.isSuccessful()) {
                finish();
                Intent myIntent = new Intent(LoginActivity.this, LoadingActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
            } else {
                Toast.makeText(getApplicationContext(), "Login Gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void submitLogin(View view) {
        userLogin();
    }

    public void toLupaPass(View v) {
        finish();
        startActivity(new Intent(LoginActivity.this, PassResetActivity.class));
    }

    public void toRegis(View v) {
        finish();
        startActivity(new Intent(LoginActivity.this, RegisActivity.class));
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Apakah kamu yakin ingin keluar?")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialog, id) -> LoginActivity.this.finish())
                .setNegativeButton("Tidak", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
