package com.ads.narayan.ads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.narayan.ads.wrapper.AppAdError;
import com.ads.narayan.ads.wrapper.AppInterstitialAd;
import com.ads.narayan.ads.wrapper.AppNativeAd;
import com.ads.narayan.ads.wrapper.AppRewardItem;

public class NarayanAdCallback {
    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdFailedToLoad(@Nullable AppAdError adError) {
    }

    public void onAdFailedToShow(@Nullable AppAdError adError) {
    }

    public void onAdLeftApplication() {
    }

    public void onAdLoaded() {

    }

    // ad splash loaded when showSplashIfReady = false
    public void onAdSplashReady() {

    }

    public void onInterstitialLoad(@Nullable AppInterstitialAd interstitialAd) {

    }

    public void onAdClicked() {
    }

    public void onAdImpression() {
    }


    public void onNativeAdLoaded(@NonNull AppNativeAd nativeAd) {

    }

    public void onUserEarnedReward(@NonNull AppRewardItem rewardItem) {

    }

    public void onInterstitialShow() {

    }
}
