package com.ads.narayan.ads.wrapper;

import com.applovin.mediation.MaxAd;
import com.google.android.gms.ads.AdValue;

public class AppAdValue {
    private AdValue admobAdValue;
    private MaxAd  maxAdValue;

    public AppAdValue(MaxAd maxAdValue) {
        this.maxAdValue = maxAdValue;
    }

    public AppAdValue(AdValue admobAdValue) {
        this.admobAdValue = admobAdValue;
    }

    public AdValue getAdmobAdValue() {
        return admobAdValue;
    }

    public MaxAd getMaxAdValue() {
        return maxAdValue;
    }
}
