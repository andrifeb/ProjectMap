package com.mbul.projectmap.jasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mbul.projectmap.R;
import com.mbul.projectmap.adapter.JasaRecyclerViewAdapter;
import com.mbul.projectmap.model.SingleDataUser;

import java.util.ArrayList;

@SuppressWarnings("all")
public class JasaWorkActivity extends AppCompatActivity {

    private static final String TAG = "JasaWorkActivity";

    private ArrayList<SingleDataUser> singleDataUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_jasa_work);

        singleDataUsers = new ArrayList<>();

        initData();
    }

    private void initData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Mencari data di dalam collection pekerjaan dimana dokumen jasa_uid sama dengan UID Jasa yang sedang login
        db.collection("pekerjaan")
                .whereEqualTo("jasa_uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       for(QueryDocumentSnapshot document : task.getResult()) {

                           if(document.getString("jasa_uid") != null) {

                               if(!document.getString("status").equals("Tolak")) {
                                   //Mencari data user di dalam collection user yang telah didapat sebelumnya
                                   FirebaseFirestore.getInstance().collection("user")
                                           .document(document.getString("user_uid"))
                                           .get()
                                           .addOnCompleteListener(task_user -> {
                                               if(task_user.isSuccessful()) {

                                                   DocumentSnapshot doc_user = task_user.getResult();

                                                   //Mengirim data user ke dalam list array singleDataUsers
                                                   singleDataUsers.add(new SingleDataUser(
                                                           doc_user.getId(),
                                                           doc_user.getString("nama"),
                                                           doc_user.getString("no_telp"),
                                                           document.getString("status")
                                                   ));


                                                   //Mengirim data list array singleDataUsers ke RecyclerView
                                                   initRecyclerView();

                                               }
                                           });
                               }

                           }

                       }
                   } else {
                       Toast.makeText(getApplicationContext(), "Kesalahan pada database", Toast.LENGTH_SHORT).show();
                   }
                });
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.pekerjaanRv);
        JasaRecyclerViewAdapter jasaRecyclerViewAdapter = new JasaRecyclerViewAdapter(this, singleDataUsers);
        recyclerView.setAdapter(jasaRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(JasaWorkActivity.this, JasaMainActivity.class));
    }
}
