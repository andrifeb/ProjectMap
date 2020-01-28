package com.mbul.projectmap.user;

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
import com.mbul.projectmap.adapter.UserRecyclerViewAdapter;
import com.mbul.projectmap.model.SingleDataJasa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("all")
public class UserStatusActivity extends AppCompatActivity implements UserRecyclerViewAdapter.ClickListener {

    private static final String TAG = "UserStatusActivity";

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    private ArrayList<SingleDataJasa> singleDataJasas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_status);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        singleDataJasas = new ArrayList<>();

        initData();
    }

    private void initData() {

        db.collection("pekerjaan")
                .whereEqualTo("user_uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       for(QueryDocumentSnapshot document : task.getResult()) {
                           if(document.getString("user_uid") != null) {
                               db.collection("jasa")
                                       .document(document.getString("jasa_uid"))
                                       .get()
                                       .addOnCompleteListener(task_jasa -> {
                                          if(task_jasa.isSuccessful()) {
                                              DocumentSnapshot doc_jasa = task_jasa.getResult();
                                              singleDataJasas.add(new SingleDataJasa(
                                                 doc_jasa.getId(),
                                                 doc_jasa.getString("nama_toko"),
                                                 doc_jasa.getString("no_telp")
                                              ));
                                              initRecyclerView();
                                          }
                                       });
                           }
                       }
                   } else {
                       Toast.makeText(getApplicationContext(), "Kesalahan pada database", Toast.LENGTH_SHORT).show();
                   }
                });
    }

    @Override
    public void onItemClick(int position) {
        SingleDataJasa singleDataJasa = singleDataJasas.get(position);

        db.collection("pekerjaan")
                .whereEqualTo("user_uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       for(QueryDocumentSnapshot doc : task.getResult()) {

                           if(doc.getString("status").equals("Tolak")) {
                               String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                               db.collection("selesai")
                                       .document(timeStamp)
                                       .set(doc);
                               db.collection("pekerjaan")
                                       .document(doc.getId())
                                       .delete()
                                       .addOnCompleteListener(task_selesai -> {
                                           if(task_selesai.isSuccessful()) {
                                               Toast.makeText(getApplicationContext(), "Reparasi telah dihapus", Toast.LENGTH_SHORT).show();
                                               finish();
                                               startActivity(new Intent(UserStatusActivity.this, UserMainActivity.class));
                                           }
                                       });
                           } else {
                               Intent myIntent = new Intent(UserStatusActivity.this, UserNavigActivity.class);
                               myIntent.putExtra("jasa_uid", singleDataJasa.getJasa_uid());
                               finish();
                               startActivity(myIntent);
                           }

                       }
                   }
                });
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.statusRv);
        UserRecyclerViewAdapter userRecyclerViewAdapter = new UserRecyclerViewAdapter(getApplicationContext(),
                singleDataJasas, this);
        recyclerView.setAdapter(userRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(UserStatusActivity.this, UserMainActivity.class));
    }
}
