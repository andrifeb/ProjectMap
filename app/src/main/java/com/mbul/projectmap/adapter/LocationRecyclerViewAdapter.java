package com.mbul.projectmap.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mbul.projectmap.R;
import com.mbul.projectmap.model.IndividualLocation;

import java.util.List;

/**
 * RecyclerView adapter to display a list of location cards on top of the map
 */
@SuppressWarnings("all")
public class LocationRecyclerViewAdapter extends
        RecyclerView.Adapter<LocationRecyclerViewAdapter.ViewHolder> {

    private List<IndividualLocation> listOfLocations;
    private Context context;
    private static ClickListener clickListener;
    private Drawable emojiForCircle = null;
    private int upperCardSectionColor = 0;

    private int locationNameColor = 0;
    private int locationAddressColor = 0;
    private int locationPhoneNumColor = 0;
    private int locationPhoneHeaderColor = 0;

    public LocationRecyclerViewAdapter(List<IndividualLocation> styles,
                                       Context context, ClickListener cardClickListener) {
        this.context = context;
        this.listOfLocations = styles;
        this.clickListener = cardClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int singleRvCardToUse = R.layout.single_location_map_view_rv_card;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(singleRvCardToUse, parent, false);
        return new ViewHolder(itemView);
    }

    public interface ClickListener {
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        return listOfLocations.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder card, int position) {

        IndividualLocation locationCard = listOfLocations.get(position);

        card.namaTokoTextView.setText(locationCard.getNama_toko());
        card.alamatTextView.setText(locationCard.getAlamat());
        card.noTelpTextView.setText(locationCard.getNo_telp());
        card.noTelpHeaderTextView.setText("Kontak : ");

        emojiForCircle = ResourcesCompat.getDrawable(context.getResources(), R.drawable.jasa_icon, null);
        setColors(R.color.colorPrimary_blue, R.color.colorWhite, R.color.md_grey_700, R.color.md_grey_200,
                R.color.md_grey_200);
        setAlphas(card, .41f, .48f, 100f);

        card.emojiImageView.setImageDrawable(emojiForCircle);
        card.constraintUpperColorSection.setBackgroundColor(upperCardSectionColor);
        card.namaTokoTextView.setTextColor(locationNameColor);
        card.noTelpTextView.setTextColor(locationPhoneNumColor);
        card.alamatTextView.setTextColor(locationAddressColor);
        card.noTelpHeaderTextView.setTextColor(locationPhoneHeaderColor);
    }

    private void setColors(int colorForUpperCard, int colorForName, int colorForAddress, int colorForPhoneNum,
                           int colorForPhoneHeader) {
        upperCardSectionColor = ResourcesCompat.getColor(context.getResources(), colorForUpperCard, null);
        locationNameColor = ResourcesCompat.getColor(context.getResources(), colorForName, null);
        locationAddressColor = ResourcesCompat.getColor(context.getResources(), colorForAddress, null);
        locationPhoneNumColor = ResourcesCompat.getColor(context.getResources(), colorForPhoneNum, null);
        locationPhoneHeaderColor = ResourcesCompat.getColor(context.getResources(), colorForPhoneHeader, null);
    }

    private void setAlphas(ViewHolder card, float addressAlpha,float phoneHeaderAlpha, float phoneNumAlpha) {
        card.alamatTextView.setAlpha(addressAlpha);
        card.noTelpHeaderTextView.setAlpha(phoneHeaderAlpha);
        card.noTelpTextView.setAlpha(phoneNumAlpha);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView namaTokoTextView;
        TextView alamatTextView;
        TextView noTelpTextView;
        TextView noTelpHeaderTextView;
        Button pilihJasaButton;
        ConstraintLayout constraintUpperColorSection;
        CardView cardView;
        ImageView emojiImageView;

        ViewHolder(View itemView) {
            super(itemView);
            namaTokoTextView = itemView.findViewById(R.id.location_name_tv);
            alamatTextView = itemView.findViewById(R.id.location_alamat_tv);
            noTelpTextView = itemView.findViewById(R.id.location_no_telp_tv);
            noTelpHeaderTextView = itemView.findViewById(R.id.no_telp_header_tv);
            emojiImageView = itemView.findViewById(R.id.emoji);
            constraintUpperColorSection = itemView.findViewById(R.id.constraint_upper_color);
            cardView = itemView.findViewById(R.id.map_view_location_card);
            pilihJasaButton = itemView.findViewById(R.id.pilihJasaBtn);
            pilihJasaButton.setOnClickListener(view -> clickListener.onItemClick(getLayoutPosition()));
        }
    }
}
