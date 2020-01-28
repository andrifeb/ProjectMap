package com.mbul.projectmap.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mbul.projectmap.R;
import com.mbul.projectmap.model.SingleDataJasa;

import java.util.List;

@SuppressWarnings("all")
public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "UserRecyclerViewAdapter";
    private static final String HAPUS = "Hapus";
    private static final String DITERIMA = "Diterima";
    private static final String DITOLAK = "Ditolak";
    private static final String TUNGGU = "Tunggu";

    private List<SingleDataJasa> listDataJasa;
    private Context mContext;
    private static ClickListener clickListener;

    public UserRecyclerViewAdapter(Context mContext, List<SingleDataJasa> listDataJasa,
                                   ClickListener naviClickListener) {
        this.listDataJasa = listDataJasa;
        this.mContext = mContext;
        this.clickListener = naviClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_user_reparasi, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public interface ClickListener {
        void onItemClick(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SingleDataJasa jasaHolder = listDataJasa.get(position);

        holder.jasaTv.setText(jasaHolder.getJasa_nama_toko());
        holder.noTelpTv.setText(jasaHolder.getJasa_no_telp());

        initRealtimeStatus(holder);

    }

    @Override
    public int getItemCount() {
        return listDataJasa.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView jasaTv, noTelpTv, statusHeader, statusTv;
        Button navigasiBtn;
        LinearLayout statusLinearLayout, parentLinearLayout;
        RelativeLayout parentRelativeLayout, dataLayout;
        CardView cardView, dataCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            jasaTv = itemView.findViewById(R.id.jasaTv);
            noTelpTv = itemView.findViewById(R.id.noTelpTv);
            dataLayout = itemView.findViewById(R.id.data_layout);
            dataCardView = itemView.findViewById(R.id.data_card_layout);
            statusHeader = itemView.findViewById(R.id.status_header);
            statusTv = itemView.findViewById(R.id.statusTv);
            statusLinearLayout = itemView.findViewById(R.id.status_layout);
            navigasiBtn = itemView.findViewById(R.id.navigasiBtn);
            parentRelativeLayout = itemView.findViewById(R.id.reparasi_relative_layout);
            parentLinearLayout = itemView.findViewById(R.id.linear_parent_layout);
            cardView = itemView.findViewById(R.id.reparasi_card_layout);

            navigasiBtn.setOnClickListener(view -> clickListener.onItemClick(getLayoutPosition()));
        }
    }

    private void initRealtimeStatus(@NonNull ViewHolder holder) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        CollectionReference collRef = FirebaseFirestore.getInstance().collection("pekerjaan");
        collRef.whereEqualTo("user_uid", mAuth.getCurrentUser().getUid())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e!=null){
                        Log.d(TAG,"Listen stopped: "+e.getMessage());
                    } else {
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            switch (document.getString("status")) {
                                case "Permintaan":
                                    holder.statusTv.setText(TUNGGU);
                                    holder.navigasiBtn.setTextColor(Color.parseColor("#3E84AF"));
                                    holder.navigasiBtn.setEnabled(false);
                                    break;

                                case "Terima":
                                    holder.statusTv.setText(DITERIMA);
                                    holder.navigasiBtn.setTextColor(Color.parseColor("#ffffff"));
                                    holder.navigasiBtn.setEnabled(true);
                                    break;

                                case "Tolak":
                                    holder.statusTv.setText(DITOLAK);
                                    holder.statusLinearLayout.setBackgroundResource(R.drawable.corner_round_red);
                                    holder.navigasiBtn.setTextColor(Color.parseColor("#ffffff"));
                                    holder.navigasiBtn.setEnabled(true);
                                    holder.navigasiBtn.setText(HAPUS);
                                    break;

                                case "Proses":
                                    holder.statusTv.setText(document.getString("status"));
                                    holder.navigasiBtn.setTextColor(Color.parseColor("#3E84AF"));
                                    holder.navigasiBtn.setEnabled(false);
                                    break;

                                case "Selesai":
                                    holder.statusTv.setText(document.getString("status"));
                                    holder.navigasiBtn.setTextColor(Color.parseColor("#ffffff"));
                                    holder.navigasiBtn.setEnabled(true);
                                    break;
                            }
                        }
                    }
                });
    }

}
