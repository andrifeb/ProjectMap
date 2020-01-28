package com.mbul.projectmap.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mapbox.mapboxsdk.maps.Style;
import com.mbul.projectmap.R;

public class CustomThemeManager {
    private Context context;
    private Bitmap unselectedMarkerIcon;
    private Bitmap selectedMarkerIcon;
    private String mapStyle;

    public CustomThemeManager(Context context) {
        this.context = context;
        initializeTheme();
    }

    private void initializeTheme() {
        mapStyle = Style.LIGHT;
        unselectedMarkerIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.jasa_icon_unselected);
        selectedMarkerIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.jasa_icon_selected);
    }

    public String getMapStyle() {
        return mapStyle;
    }

    public Bitmap getUnSelectedMarkerIcon() {
        return unselectedMarkerIcon;
    }

    public Bitmap getSelectedMarkerIcon() {
        return selectedMarkerIcon;
    }
}
