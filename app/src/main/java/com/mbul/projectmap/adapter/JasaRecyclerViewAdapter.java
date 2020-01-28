package com.mbul.projectmap.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mbul.projectmap.R;
import com.mbul.projectmap.model.SingleDataUser;

import java.util.List;

@SuppressWarnings("all")
public class JasaRecyclerViewAdapter extends RecyclerView.Adapter<JasaRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "JasaRecyclerViewAdapter";
    private static final String SELESAI = "Selesai";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<SingleDataUser> listDataUser;
    private Context mContext;

    public JasaRecyclerViewAdapter(Context mContext, List<SingleDataUser> userList) {
        this.listDataUser = userList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_jasa_pekerjaan, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SingleDataUser userHolder = listDataUser.get(position);

        holder.namaTv.setText(userHolder.getUser_nama());
        holder.noTelpTv.setText(userHolder.getUser_no_telp());

        switch (userHolder.getStatus()) {
            case "Permintaan":
                holder.terimaBtn.setVisibility(View.VISIBLE);
                holder.tolakBtn.setVisibility(View.VISIBLE);
                holder.statusLinearLayout.setVisibility(View.GONE);
                holder.prosesBtn.setVisibility(View.GONE);
                break;

            case "Terima":
                holder.terimaBtn.setVisibility(View.GONE);
                holder.tolakBtn.setVisibility(View.GONE);
                holder.statusLinearLayout.setVisibility(View.VISIBLE);
                holder.prosesBtn.setVisibility(View.VISIBLE);
                holder.statusTv.setText(userHolder.getStatus());
                break;

            case "Proses":
                holder.terimaBtn.setVisibility(View.GONE);
                holder.tolakBtn.setVisibility(View.GONE);
                holder.prosesBtn.setVisibility(View.VISIBLE);
                holder.prosesBtn.setText(SELESAI);
                holder.statusLinearLayout.setVisibility(View.VISIBLE);
                holder.statusTv.setText(userHolder.getStatus());
                break;

            case "Selesai":
                holder.terimaBtn.setVisibility(View.GONE);
                holder.tolakBtn.setVisibility(View.GONE);
                holder.statusLinearLayout.setVisibility(View.VISIBLE);
                holder.statusTv.setText(userHolder.getStatus());
                break;
        }

        holder.prosesBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Apakah kamu yakin?")
                    .setCancelable(false)
                    .setPositiveButton("Ya", (dialog, id) -> {
                        String status;
                        switch (holder.prosesBtn.getText().toString()) {
                            case "Proses":
                                status = "Proses";
                                updateStatus(position, status);
                                holder.statusTv.setText(status);
                                holder.prosesBtn.setText(SELESAI);
                                break;
                            case "Selesai":
                                status = "Selesai";
                                updateStatus(position, status);
                                holder.statusTv.setText(status);
                                holder.prosesBtn.setVisibility(View.GONE);
                                break;
                        }
                    })
                    .setNegativeButton("Tidak", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        });

    }

    @Override
    public int getItemCount() {
        return listDataUser.size();
    }

    @SuppressLint("LogNotTimber")
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView namaTv, noTelpTv, statusHeader, statusTv;
        Button terimaBtn, tolakBtn, prosesBtn;
        LinearLayout statusLinearLayout, parentLinearLayout;
        RelativeLayout parentRelativeLayout, dataLayout;
        CardView cardView, dataCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            namaTv = itemView.findViewById(R.id.namaTv);
            noTelpTv = itemView.findViewById(R.id.noTelpTv);
            dataLayout = itemView.findViewById(R.id.data_layout);
            dataCardView = itemView.findViewById(R.id.data_card_layout);
            terimaBtn = itemView.findViewById(R.id.terimaBtn);
            tolakBtn = itemView.findViewById(R.id.tolakBtn);
            statusHeader = itemView.findViewById(R.id.status_header);
            statusTv = itemView.findViewById(R.id.statusTv);
            statusLinearLayout = itemView.findViewById(R.id.status_layout);
            prosesBtn = itemView.findViewById(R.id.prosesBtn);
            parentRelativeLayout = itemView.findViewById(R.id.pekerjaan_relative_layout);
            parentLinearLayout = itemView.findViewById(R.id.linear_parent_layout);
            cardView = itemView.findViewById(R.id.pekerjaan_card_layout);

            terimaBtn.setOnClickListener(view -> {
                String status = "Terima";
                updateStatus(getAdapterPosition(), status);
                statusTv.setText(status);

                terimaBtn.setVisibility(View.GONE);
                tolakBtn.setVisibility(View.GONE);

                statusLinearLayout.setVisibility(View.VISIBLE);
                prosesBtn.setVisibility(View.VISIBLE);
            });

            tolakBtn.setOnClickListener(view -> {
                String status = "Tolak";
                updateStatus(getAdapterPosition(), status);
                removeAt(getAdapterPosition());
            });
        }
    }

    private void updateStatus(int position, String newStatus) {
        SingleDataUser userData = listDataUser.get(position);

        db.collection("pekerjaan")
                .whereEqualTo("user_uid", userData.getUser_uid())
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       for(QueryDocumentSnapshot document : task.getResult()) {
                           db.collection("pekerjaan")
                                   .document(document.getId())
                                   .update("status", newStatus);
                       }
                   } else {
                       Log.d(TAG, "updateStatus : Database Error");
                   }
                });
    }

    private void removeAt(int position) {
        listDataUser.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listDataUser.size());
    }
}
