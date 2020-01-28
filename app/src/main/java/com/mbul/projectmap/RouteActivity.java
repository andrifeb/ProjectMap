package com.mbul.projectmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mbul.projectmap.jasa.JasaRouteActivity;
import com.mbul.projectmap.user.UserRouteActivity;

public class RouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_route);
    }

    public void onClick(View v) {
        if (v == findViewById(R.id.toUser)) {
            Intent myIntent = new Intent(RouteActivity.this, UserRouteActivity.class);
            finish();
            startActivity(myIntent);
        } else if (v == findViewById(R.id.toJasa)) {
            Intent myIntent = new Intent(RouteActivity.this, JasaRouteActivity.class);
            finish();
            startActivity(myIntent);
        }
    }

    @Override
    public void onBackPressed() { }
}
