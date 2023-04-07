package com.ads.narayan.ads.bannerAds;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.narayan.R;
import com.ads.narayan.ads.NarayanAd;

/**
 * Created by lamlt on 28/10/2022.
 */
public class NarayanBannerAdView extends RelativeLayout {

    private String TAG = "NarayanBannerAdView";

    public NarayanBannerAdView(@NonNull Context context) {
        super(context);
        init();
    }

    public NarayanBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public NarayanBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    public NarayanBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.layout_banner_control, this);
    }

    public void loadBanner(Activity activity, String idBanner){
        NarayanAd.getInstance().loadBanner(activity, idBanner);
    }
}