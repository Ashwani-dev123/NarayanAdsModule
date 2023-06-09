package com.ads.narayan.ads.wrapper;

import android.view.View;

import com.google.android.gms.ads.nativead.NativeAd;

public class AppNativeAd extends AppAdBase {
    private int layoutCustomNative;
    private View nativeView;
    private NativeAd admobNativeAd;

    public AppNativeAd(StatusAd status) {
        super(status);
    }

    public AppNativeAd(int layoutCustomNative, View nativeView) {
        this.layoutCustomNative = layoutCustomNative;
        this.nativeView = nativeView;
        status = StatusAd.AD_LOADED;
    }

    public AppNativeAd(int layoutCustomNative, NativeAd admobNativeAd) {
        this.layoutCustomNative = layoutCustomNative;
        this.admobNativeAd = admobNativeAd;
        status = StatusAd.AD_LOADED;
    }

    public NativeAd getAdmobNativeAd() {
        return admobNativeAd;
    }

    public void setAdmobNativeAd(NativeAd admobNativeAd) {
        this.admobNativeAd = admobNativeAd;
        if (admobNativeAd != null)
            status = StatusAd.AD_LOADED;
    }

    public AppNativeAd() {
    }


    @Override
    boolean isReady() {
        return nativeView != null || admobNativeAd != null;
    }


    public int getLayoutCustomNative() {
        return layoutCustomNative;
    }

    public void setLayoutCustomNative(int layoutCustomNative) {
        this.layoutCustomNative = layoutCustomNative;
    }

    public View getNativeView() {
        return nativeView;
    }

    public void setNativeView(View nativeView) {
        this.nativeView = nativeView;
    }

    public String toString(){
        return "Status:"+ status + " == nativeView:"+nativeView + " == admobNativeAd:"+admobNativeAd;
    }

}
