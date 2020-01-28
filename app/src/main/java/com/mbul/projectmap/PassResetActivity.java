package com.mbul.projectmap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PassResetActivity extends AppCompatActivity {

    private EditText emailInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pass_reset);

        emailInput = findViewById(R.id.emailInput);
        mAuth = FirebaseAuth.getInstance();

        Button resetBtn = findViewById(R.id.resetBtn);
        resetBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString();

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

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Email reset password telah dikirim", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(PassResetActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(PassResetActivity.this, "Email belum terdaftar", Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(PassResetActivity.this, LoginActivity.class));
    }
}
