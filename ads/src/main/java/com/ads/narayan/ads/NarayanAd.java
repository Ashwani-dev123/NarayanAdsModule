package com.ads.narayan.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.ads.narayan.R;
import com.ads.narayan.admob.NarayanAdmob;
import com.ads.narayan.admob.AppOpenManager;
import com.ads.narayan.ads.nativeAds.NarayanAdAdapter;
import com.ads.narayan.ads.nativeAds.NarayanAdPlacer;
import com.ads.narayan.ads.wrapper.AppAdError;
import com.ads.narayan.ads.wrapper.AppAdValue;
import com.ads.narayan.ads.wrapper.AppInterstitialAd;
import com.ads.narayan.ads.wrapper.AppNativeAd;
import com.ads.narayan.ads.wrapper.AppRewardAd;
import com.ads.narayan.ads.wrapper.AppRewardItem;
import com.ads.narayan.applovin.NarayanAppLoving;
import com.ads.narayan.applovin.AppLovinCallback;
import com.ads.narayan.billing.AppPurchase;
import com.ads.narayan.event.NarayanAppsflyer;
import com.ads.narayan.funtion.AdCallback;
import com.ads.narayan.funtion.RewardCallback;
import com.ads.narayan.event.NarayanAdjust;
import com.ads.narayan.util.AppUtil;
import com.ads.narayan.event.NarayanLogEventManager;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacer;
import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class NarayanAd {
    public static final String TAG_ADJUST = "NarayanAdjust";
    public static final String TAG = "NarayanAd";
    private static volatile NarayanAd INSTANCE;
    private NarayanAdConfig Narayan_adConfig;
    private NarayanInitCallback initCallback;
    private Boolean initAdSuccess = false;

    public static synchronized NarayanAd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NarayanAd();
        }
        return INSTANCE;
    }

    /**
     * Set count click to show ads interstitial when call showInterstitialAdByTimes()
     *
     * @param countClickToShowAds - default = 3
     */
    public void setCountClickToShowAds(int countClickToShowAds) {
        NarayanAdmob.getInstance().setNumToShowAds(countClickToShowAds);
        NarayanAppLoving.getInstance().setNumShowAds(countClickToShowAds);
    }

    /**
     * Set count click to show ads interstitial when call showInterstitialAdByTimes()
     *
     * @param countClickToShowAds Default value = 3
     * @param currentClicked      Default value = 0
     */
    public void setCountClickToShowAds(int countClickToShowAds, int currentClicked) {
        NarayanAdmob.getInstance().setNumToShowAds(countClickToShowAds, currentClicked);
        NarayanAppLoving.getInstance().setNumToShowAds(countClickToShowAds, currentClicked);
    }

     /**
     * @param context
     * @param adConfig NarayanAdConfig object used for SDK initialisation
     */
    public void init(Application context, NarayanAdConfig adConfig) {
        init(context, adConfig, false);
    }

    /**
     * @param context
     * @param adConfig            NarayanAdConfig object used for SDK initialisation
     * @param enableDebugMediation set show Mediation Debugger - use only for Max Mediation
     */
    public void init(Application context, NarayanAdConfig adConfig, Boolean enableDebugMediation) {
        if (adConfig == null) {
            throw new RuntimeException("cant not set NarayanAdConfig null");
        }
        this.Narayan_adConfig = adConfig;
        AppUtil.VARIANT_DEV = adConfig.isVariantDev();
        Log.i(TAG, "Config variant dev: " + AppUtil.VARIANT_DEV);
        if (adConfig.isEnableAppsflyer()) {
            Log.i(TAG, "init appsflyer");
            NarayanAppsflyer.enableAppsflyer = true;
            NarayanAppsflyer.getInstance().init(context, adConfig.getAppsflyerToken(), this.Narayan_adConfig.isVariantDev());
        }
        if (adConfig.isEnableAdjust()) {
            Log.i(TAG, "init adjust");
            NarayanAdjust.enableAdjust = true;
            setupAdjust(adConfig.isVariantDev(), adConfig.getAdjustToken());
        }
        switch (adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().init(context, new AppLovinCallback() {
                    @Override
                    public void initAppLovinSuccess() {
                        super.initAppLovinSuccess();
                        initAdSuccess = true;
                        if (initCallback != null)
                            initCallback.initAdSuccess();
                    }
                }, enableDebugMediation);
                break;
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().init(context, adConfig.getListDeviceTest());
                if (adConfig.isEnableAdResume())
                    AppOpenManager.getInstance().init(adConfig.getApplication(), adConfig.getIdAdResume());

                initAdSuccess = true;
                if (initCallback != null)
                    initCallback.initAdSuccess();
                break;
        }
    }

    public int getMediationProvider() {
        return Narayan_adConfig.getMediationProvider();
    }

    public void setInitCallback(NarayanInitCallback initCallback) {
        this.initCallback = initCallback;
        if (initAdSuccess)
            initCallback.initAdSuccess();
    }

    private void setupAdjust(Boolean buildDebug, String adjustToken) {

        String environment = buildDebug ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        Log.i("Application", "setupAdjust: " + environment);
        AdjustConfig config = new AdjustConfig(Narayan_adConfig.getApplication(), adjustToken, environment);

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE);
        config.setPreinstallTrackingEnabled(true);
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Log.d(TAG_ADJUST, "Attribution callback called!");
                Log.d(TAG_ADJUST, "Attribution: " + attribution.toString());
            }
        });

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                Log.d(TAG_ADJUST, "Event success callback called!");
                Log.d(TAG_ADJUST, "Event success data: " + eventSuccessResponseData.toString());
            }
        });
        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                Log.d(TAG_ADJUST, "Event failure callback called!");
                Log.d(TAG_ADJUST, "Event failure data: " + eventFailureResponseData.toString());
            }
        });

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                Log.d(TAG_ADJUST, "Session success callback called!");
                Log.d(TAG_ADJUST, "Session success data: " + sessionSuccessResponseData.toString());
            }
        });

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                Log.d(TAG_ADJUST, "Session failure callback called!");
                Log.d(TAG_ADJUST, "Session failure data: " + sessionFailureResponseData.toString());
            }
        });


        config.setSendInBackground(true);
        Adjust.onCreate(config);
        Narayan_adConfig.getApplication().registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    }

    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }
    }

    public void loadBanner(final Activity mActivity, String id) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().loadBanner(mActivity, id);
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().loadBanner(mActivity, id);
        }
    }

    public void loadBanner(final Activity mActivity, String id, final AdCallback adCallback) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().loadBanner(mActivity, id, adCallback);
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().loadBanner(mActivity, id, adCallback);
        }
    }

    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().loadBannerFragment(mActivity, id, rootView);
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().loadBannerFragment(mActivity, id, rootView);
        }
    }

    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final AdCallback adCallback) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().loadBannerFragment(mActivity, id, rootView, adCallback);
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().loadBannerFragment(mActivity, id, rootView, adCallback);
        }
    }

//    public void loadBanner(final Activity mActivity, String id, final NarayanAdCallback callback) {
//        switch (adConfig.getMediationProvider()) {
//            case NarayanAdConfig.PROVIDER_ADMOB:
//                Admob.getInstance().loadBanner(mActivity, id , new AdCallback(){
//                    @Override
//                    public void onAdClicked() {
//                        super.onAdClicked();
//                        callback.onAdClicked();
//                    }
//                });
//                break;
//            case NarayanAdConfig.PROVIDER_MAX:
//                AppLovin.getInstance().loadBanner(mActivity, id, new AppLovinCallback(){
//
//                });
//        }
//    }

    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, NarayanAdCallback adListener) {
        loadSplashInterstitialAds(context, id, timeOut, timeDelay, true, adListener);
    }

    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, boolean showSplashIfReady, NarayanAdCallback adListener) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().loadSplashInterstitialAds(context, id, timeOut, timeDelay, showSplashIfReady, new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        adListener.onAdClosed();
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        adListener.onNextAction();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        adListener.onAdFailedToLoad(new AppAdError(i));

                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        adListener.onAdFailedToShow(new AppAdError(adError));

                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adListener.onAdLoaded();
                    }

                    @Override
                    public void onAdSplashReady() {
                        super.onAdSplashReady();
                        adListener.onAdSplashReady();
                    }


                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (adListener != null) {
                            adListener.onAdClicked();
                        }
                    }
                });
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().loadSplashInterstitialAds(context, id, timeOut, timeDelay, showSplashIfReady, new AppLovinCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        adListener.onAdFailedToLoad(new AppAdError(i));
                        adListener.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable MaxError adError) {
                        super.onAdFailedToShow(adError);
                        adListener.onAdFailedToShow(new AppAdError(adError));
                        adListener.onNextAction();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adListener.onAdLoaded();
                    }

                    @Override
                    public void onAdSplashReady() {
                        super.onAdSplashReady();
                        adListener.onAdSplashReady();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (adListener != null) {
                            adListener.onAdClicked();
                        }
                    }
                });
        }
    }


    public void onShowSplash(AppCompatActivity activity, NarayanAdCallback adListener) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().onShowSplash(activity, new AdCallback() {
                            @Override
                            public void onAdFailedToShow(@Nullable AdError adError) {
                                super.onAdFailedToShow(adError);
                                adListener.onAdFailedToShow(new AppAdError(adError));
                            }

                            @Override
                            public void onAdClosed() {
                                super.onAdClosed();
                                adListener.onAdClosed();
                            }

                            @Override
                            public void onNextAction() {
                                super.onNextAction();
                                adListener.onNextAction();
                            }


                        }
                );
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().onShowSplash(activity, new AppLovinCallback() {
                    @Override
                    public void onAdFailedToShow(@Nullable MaxError adError) {
                        super.onAdFailedToShow(adError);
                        adListener.onAdFailedToShow(new AppAdError(adError));
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }

                });

        }
    }

    /**
     * Called  on Resume - SplashActivity
     * It call reshow ad splash when ad splash show fail in background
     *
     * @param activity
     * @param callback
     * @param timeDelay time delay before call show ad splash (ms)
     */
    public void onCheckShowSplashWhenFail(AppCompatActivity activity, NarayanAdCallback callback,
                                          int timeDelay) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().onCheckShowSplashWhenFail(activity, new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onAdClosed();
                        callback.onNextAction();
                    }


                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        callback.onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new AppAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new AppAdError(adError));
                    }
                }, timeDelay);
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().onCheckShowSplashWhenFail(activity, new AppLovinCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        callback.onNextAction();
                    }


                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        callback.onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new AppAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable MaxError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new AppAdError(adError));
                    }
                }, timeDelay);
        }
    }

    /**
     * Result a ApInterstitialAd in onInterstitialLoad
     *
     * @param context
     * @param id         admob or max mediation
     * @param adListener
     */
    public AppInterstitialAd getInterstitialAds(Context context, String id, NarayanAdCallback adListener) {
        AppInterstitialAd apInterstitialAd = new AppInterstitialAd();
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().getInterstitialAds(context, id, new AdCallback() {
                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.d(TAG, "Admob onInterstitialLoad");
                        apInterstitialAd.setInterstitialAd(interstitialAd);
                        adListener.onInterstitialLoad(apInterstitialAd);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        adListener.onAdFailedToLoad(new AppAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        adListener.onAdFailedToShow(new AppAdError(adError));
                    }

                });
                return apInterstitialAd;

            case NarayanAdConfig.PROVIDER_MAX:
                MaxInterstitialAd maxInterstitialAd = NarayanAppLoving.getInstance().getInterstitialAds(context, id);
                maxInterstitialAd.setListener(new MaxAdListener() {

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        Log.d(TAG, "Max onInterstitialLoad: ");
                        apInterstitialAd.setMaxInterstitialAd(maxInterstitialAd);
                        adListener.onInterstitialLoad(apInterstitialAd);
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {

                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                        adListener.onAdClosed();
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                        adListener.onAdClicked();
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        adListener.onAdFailedToLoad(new AppAdError(error));
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        adListener.onAdFailedToShow(new AppAdError(error));
                    }
                });
                apInterstitialAd.setMaxInterstitialAd(maxInterstitialAd);
                return apInterstitialAd;
            default:
                return apInterstitialAd;
        }
    }

    /**
     * Result a ApInterstitialAd in onInterstitialLoad
     *
     * @param context
     * @param id      admob or max mediation
     */
    public AppInterstitialAd getInterstitialAds(Context context, String id) {
        AppInterstitialAd apInterstitialAd = new AppInterstitialAd();
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().getInterstitialAds(context, id, new AdCallback() {
                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.d(TAG, "Admob onInterstitialLoad: ");
                        apInterstitialAd.setInterstitialAd(interstitialAd);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                    }

                });
                return apInterstitialAd;

            case NarayanAdConfig.PROVIDER_MAX:
                MaxInterstitialAd maxInterstitialAd = NarayanAppLoving.getInstance().getInterstitialAds(context, id);
                maxInterstitialAd.setListener(new MaxAdListener() {

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        Log.d(TAG, "Max onInterstitialLoad: ");
                        apInterstitialAd.setMaxInterstitialAd(maxInterstitialAd);
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {

                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    }
                });
                apInterstitialAd.setMaxInterstitialAd(maxInterstitialAd);
                return apInterstitialAd;
            default:
                return apInterstitialAd;
        }
    }

    /**
     * Called force show ApInterstitialAd when ready
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     */
    public void forceShowInterstitial(Context context, AppInterstitialAd mInterstitialAd,
                                      final NarayanAdCallback callback) {
        forceShowInterstitial(context, mInterstitialAd, callback, false);
    }

    /**
     * Called force show ApInterstitialAd when ready
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     * @param shouldReloadAds auto reload ad when ad close
     */
    public void forceShowInterstitial(@NonNull Context context, AppInterstitialAd mInterstitialAd,
                                      @NonNull final NarayanAdCallback callback, boolean shouldReloadAds) {
        if (mInterstitialAd == null || mInterstitialAd.isNotReady()) {
            Log.e(TAG, "forceShowInterstitial: ApInterstitialAd is not ready");
            callback.onNextAction();
            return;
        }
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                AdCallback adCallback = new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        Log.d(TAG, "onAdClosed: ");
                        callback.onAdClosed();
                        if (shouldReloadAds) {
                            NarayanAdmob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                                @Override
                                public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                                    super.onInterstitialLoad(interstitialAd);
                                    Log.d(TAG, "Admob shouldReloadAds success");
                                    mInterstitialAd.setInterstitialAd(interstitialAd);
                                    callback.onInterstitialLoad(mInterstitialAd);
                                }

                                @Override
                                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                                    super.onAdFailedToLoad(i);
                                    mInterstitialAd.setInterstitialAd(null);
                                    callback.onAdFailedToLoad(new AppAdError(i));
                                }

                                @Override
                                public void onAdFailedToShow(@Nullable AdError adError) {
                                    super.onAdFailedToShow(adError);
                                    callback.onAdFailedToShow(new AppAdError(adError));
                                }

                            });
                        } else {
                            mInterstitialAd.setInterstitialAd(null);
                        }
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Log.d(TAG, "onNextAction: ");
                        callback.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        Log.d(TAG, "onAdFailedToShow: ");
                        callback.onAdFailedToShow(new AppAdError(adError));
                        if (shouldReloadAds) {
                            NarayanAdmob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                                @Override
                                public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                                    super.onInterstitialLoad(interstitialAd);
                                    Log.d(TAG, "Admob shouldReloadAds success");
                                    mInterstitialAd.setInterstitialAd(interstitialAd);
                                    callback.onInterstitialLoad(mInterstitialAd);
                                }

                                @Override
                                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                                    super.onAdFailedToLoad(i);
                                    callback.onAdFailedToLoad(new AppAdError(i));
                                }

                                @Override
                                public void onAdFailedToShow(@Nullable AdError adError) {
                                    super.onAdFailedToShow(adError);
                                    callback.onAdFailedToShow(new AppAdError(adError));
                                }

                            });
                        } else {
                            mInterstitialAd.setInterstitialAd(null);
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        callback.onInterstitialShow();
                    }
                };
                NarayanAdmob.getInstance().forceShowInterstitial(context, mInterstitialAd.getInterstitialAd(), adCallback);
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().forceShowInterstitial(context, mInterstitialAd.getMaxInterstitialAd(), new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        callback.onAdClosed();
                        callback.onNextAction();
                        if (shouldReloadAds)
                            mInterstitialAd.getMaxInterstitialAd().loadAd();

                    }

                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.d(TAG, "Max inter onAdLoaded:");
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new AppAdError(adError));
                        if (shouldReloadAds)
                            mInterstitialAd.getMaxInterstitialAd().loadAd();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        callback.onInterstitialShow();
                    }
                }, false);
        }
    }

    /**
     * Called force show ApInterstitialAd when reach the number of clicks show ads
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     * @param shouldReloadAds auto reload ad when ad close
     */
    public void showInterstitialAdByTimes(Context context, AppInterstitialAd mInterstitialAd,
                                          final NarayanAdCallback callback, boolean shouldReloadAds) {
        if (mInterstitialAd.isNotReady()) {
            Log.e(TAG, "forceShowInterstitial: ApInterstitialAd is not ready");
            callback.onAdFailedToShow(new AppAdError("ApInterstitialAd is not ready"));
            return;
        }
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                AdCallback adCallback = new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        Log.d(TAG, "onAdClosed: ");
                        callback.onAdClosed();
                        if (shouldReloadAds) {
                            NarayanAdmob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                                @Override
                                public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                                    super.onInterstitialLoad(interstitialAd);
                                    Log.d(TAG, "Admob shouldReloadAds success");
                                    mInterstitialAd.setInterstitialAd(interstitialAd);
                                    callback.onInterstitialLoad(mInterstitialAd);
                                }

                                @Override
                                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                                    super.onAdFailedToLoad(i);
                                    mInterstitialAd.setInterstitialAd(null);
                                    callback.onAdFailedToLoad(new AppAdError(i));
                                }

                                @Override
                                public void onAdFailedToShow(@Nullable AdError adError) {
                                    super.onAdFailedToShow(adError);
                                    callback.onAdFailedToShow(new AppAdError(adError));
                                }

                            });
                        } else {
                            mInterstitialAd.setInterstitialAd(null);
                        }
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Log.d(TAG, "onNextAction: ");
                        callback.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        Log.d(TAG, "onAdFailedToShow: ");
                        callback.onAdFailedToShow(new AppAdError(adError));
                        if (shouldReloadAds) {
                            NarayanAdmob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                                @Override
                                public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                                    super.onInterstitialLoad(interstitialAd);
                                    Log.d(TAG, "Admob shouldReloadAds success");
                                    mInterstitialAd.setInterstitialAd(interstitialAd);
                                    callback.onInterstitialLoad(mInterstitialAd);
                                }

                                @Override
                                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                                    super.onAdFailedToLoad(i);
                                    callback.onAdFailedToLoad(new AppAdError(i));
                                }

                                @Override
                                public void onAdFailedToShow(@Nullable AdError adError) {
                                    super.onAdFailedToShow(adError);
                                    callback.onAdFailedToShow(new AppAdError(adError));
                                }

                            });
                        } else {
                            mInterstitialAd.setInterstitialAd(null);
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        if (callback != null) {
                            callback.onInterstitialShow();
                        }
                    }
                };
                NarayanAdmob.getInstance().showInterstitialAdByTimes(context, mInterstitialAd.getInterstitialAd(), adCallback);
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().showInterstitialAdByTimes(context, mInterstitialAd.getMaxInterstitialAd(), new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        callback.onAdClosed();
                        callback.onNextAction();
                        if (shouldReloadAds)
                            mInterstitialAd.getMaxInterstitialAd().loadAd();

                    }

                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.d(TAG, "Max inter onAdLoaded:");
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new AppAdError(adError));
                        if (shouldReloadAds)
                            mInterstitialAd.getMaxInterstitialAd().loadAd();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        if (callback != null) {
                            callback.onInterstitialShow();
                        }
                    }
                }, false);
        }
    }

    /**
     * Load native ad and auto populate ad to view in activity
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     */
    public void loadNativeAd(final Activity activity, String id,
                             int layoutCustomNative) {
        FrameLayout adPlaceHolder = activity.findViewById(R.id.fl_adplaceholder);
        ShimmerFrameLayout containerShimmerLoading = activity.findViewById(R.id.shimmer_container_native);

        if (AppPurchase.getInstance().isPurchased()) {
            if (containerShimmerLoading != null) {
                containerShimmerLoading.stopShimmer();
                containerShimmerLoading.setVisibility(View.GONE);
            }
            return;
        }
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        populateNativeAdView(activity, new AppNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "onAdFailedToLoad : NativeAd");
                    }
                });
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().loadNativeAd(activity, id, layoutCustomNative, new AppLovinCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        populateNativeAdView(activity, new AppNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "onAdFailedToLoad : NativeAd");
                    }
                });
                break;
        }
    }

    /**
     * Load native ad and auto populate ad to adPlaceHolder and hide containerShimmerLoading
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param adPlaceHolder
     * @param containerShimmerLoading
     */
    public void loadNativeAd(final Activity activity, String id,
                             int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout
                                     containerShimmerLoading) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        populateNativeAdView(activity, new AppNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "onAdFailedToLoad : NativeAd");
                    }
                });
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().loadNativeAd(activity, id, layoutCustomNative, new AppLovinCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        populateNativeAdView(activity, new AppNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "onAdFailedToLoad : NativeAd");
                    }
                });
                break;
        }
    }

    /**
     * Result a ApNativeAd in onUnifiedNativeAdLoaded when native ad loaded
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param callback
     */
    public void loadNativeAdResultCallback(final Activity activity, String id,
                                           int layoutCustomNative, NarayanAdCallback callback) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        callback.onNativeAdLoaded(new AppNativeAd(layoutCustomNative, unifiedNativeAd));
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new AppAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new AppAdError(adError));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }
                });
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().loadNativeAd(activity, id, layoutCustomNative, new AppLovinCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        callback.onNativeAdLoaded(new AppNativeAd(layoutCustomNative, unifiedNativeAd));
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new AppAdError(i));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }
                });
                break;
        }
    }

    /**
     * Populate Unified Native Ad to View
     *
     * @param activity
     * @param apNativeAd
     * @param adPlaceHolder
     * @param containerShimmerLoading
     */
    public void populateNativeAdView(Activity activity, AppNativeAd apNativeAd, FrameLayout
            adPlaceHolder, ShimmerFrameLayout containerShimmerLoading) {
        if (apNativeAd.getAdmobNativeAd() == null && apNativeAd.getNativeView() == null) {
            containerShimmerLoading.setVisibility(View.GONE);
            Log.e(TAG, "populateNativeAdView failed : native is not loaded ");
            return;
        }
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(activity).inflate(apNativeAd.getLayoutCustomNative(), null);
                containerShimmerLoading.stopShimmer();
                containerShimmerLoading.setVisibility(View.GONE);
                adPlaceHolder.setVisibility(View.VISIBLE);
                NarayanAdmob.getInstance().populateUnifiedNativeAdView(apNativeAd.getAdmobNativeAd(), adView);
                adPlaceHolder.removeAllViews();
                adPlaceHolder.addView(adView);
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                adPlaceHolder.setVisibility(View.VISIBLE);
                containerShimmerLoading.setVisibility(View.GONE);
                adPlaceHolder.addView(apNativeAd.getNativeView());
        }
    }


    public AppRewardAd getRewardAd(Activity activity, String id) {
        AppRewardAd apRewardAd = new AppRewardAd();
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().initRewardAds(activity, id, new AdCallback() {

                    @Override
                    public void onRewardAdLoaded(RewardedAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        Log.i(TAG, "getRewardAd AdLoaded: ");
                        apRewardAd.setAdmobReward(rewardedAd);
                    }
                });
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                MaxRewardedAd maxRewardedAd = NarayanAppLoving.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }
                });
                apRewardAd.setMaxReward(maxRewardedAd);
        }
        return apRewardAd;
    }

    public AppRewardAd getRewardAdInterstitial(Activity activity, String id) {
        AppRewardAd apRewardAd = new AppRewardAd();
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().getRewardInterstitial(activity, id, new AdCallback() {

                    @Override
                    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        Log.i(TAG, "getRewardAdInterstitial AdLoaded: ");
                        apRewardAd.setAdmobReward(rewardedAd);
                    }
                });
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                MaxRewardedAd maxRewardedAd = NarayanAppLoving.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }
                });
                apRewardAd.setMaxReward(maxRewardedAd);
        }
        return apRewardAd;
    }

    public AppRewardAd getRewardAd(Activity activity, String id, NarayanAdCallback callback) {
        AppRewardAd apRewardAd = new AppRewardAd();
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().initRewardAds(activity, id, new AdCallback() {
                    @Override
                    public void onRewardAdLoaded(RewardedAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        apRewardAd.setAdmobReward(rewardedAd);
                        callback.onAdLoaded();
                    }
                });
                return apRewardAd;
            case NarayanAdConfig.PROVIDER_MAX:
                MaxRewardedAd maxRewardedAd = NarayanAppLoving.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        callback.onAdLoaded();
                    }
                });
                apRewardAd.setMaxReward(maxRewardedAd);
                return apRewardAd;
        }
        return apRewardAd;
    }

    public AppRewardAd getRewardInterstitialAd(Activity activity, String id, NarayanAdCallback callback) {
        AppRewardAd apRewardAd = new AppRewardAd();
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                NarayanAdmob.getInstance().getRewardInterstitial(activity, id, new AdCallback() {
                    @Override
                    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        apRewardAd.setAdmobReward(rewardedAd);
                        callback.onAdLoaded();
                    }
                });
                return apRewardAd;
            case NarayanAdConfig.PROVIDER_MAX:
                MaxRewardedAd maxRewardedAd = NarayanAppLoving.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        callback.onAdLoaded();
                    }
                });
                apRewardAd.setMaxReward(maxRewardedAd);
                return apRewardAd;
        }
        return apRewardAd;
    }

    public void forceShowRewardAd(Activity activity, AppRewardAd apRewardAd, NarayanAdCallback
            callback) {
        if (!apRewardAd.isReady()) {
            Log.e(TAG, "forceShowRewardAd fail: reward ad not ready");
            callback.onNextAction();
            return;
        }
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_ADMOB:
                if (apRewardAd.isRewardInterstitial()) {
                    NarayanAdmob.getInstance().showRewardInterstitial(activity, apRewardAd.getAdmobRewardInter(), new RewardCallback() {

                        @Override
                        public void onUserEarnedReward(RewardItem var1) {
                            callback.onUserEarnedReward(new AppRewardItem(var1));
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            apRewardAd.clean();
                            callback.onNextAction();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(int codeError) {
                            apRewardAd.clean();
                            callback.onAdFailedToShow(new AppAdError(new AdError(codeError, "note msg", "Reward")));
                        }

                        @Override
                        public void onAdClicked() {
                            if (callback != null) {
                                callback.onAdClicked();
                            }
                        }
                    });
                } else {
                    NarayanAdmob.getInstance().showRewardAds(activity, apRewardAd.getAdmobReward(), new RewardCallback() {

                        @Override
                        public void onUserEarnedReward(RewardItem var1) {
                            callback.onUserEarnedReward(new AppRewardItem(var1));
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            apRewardAd.clean();
                            callback.onNextAction();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(int codeError) {
                            apRewardAd.clean();
                            callback.onAdFailedToShow(new AppAdError(new AdError(codeError, "note msg", "Reward")));
                        }

                        @Override
                        public void onAdClicked() {
                            if (callback != null) {
                                callback.onAdClicked();
                            }
                        }
                    });
                }
                break;
            case NarayanAdConfig.PROVIDER_MAX:
                NarayanAppLoving.getInstance().showRewardAd(activity, apRewardAd.getMaxReward(), new AppLovinCallback() {
                    @Override
                    public void onUserRewarded(MaxReward reward) {
                        super.onUserRewarded(reward);
                        callback.onUserEarnedReward(new AppRewardItem(reward));
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        apRewardAd.clean();
                        callback.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable MaxError adError) {
                        super.onAdFailedToShow(adError);
                        apRewardAd.clean();
                        callback.onAdFailedToShow(new AppAdError(adError));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                    }
                });
        }
    }

    /**
     * Result a NarayanAdAdapter with ad native repeating interval
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param layoutAdPlaceHolder
     * @param originalAdapter
     * @param listener
     * @param repeatingInterval
     * @return
     */
    public NarayanAdAdapter getNativeRepeatAdapter(Activity activity, String id, int layoutCustomNative, int layoutAdPlaceHolder, RecyclerView.Adapter originalAdapter,
                                                   NarayanAdPlacer.Listener listener, int repeatingInterval) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_MAX:
                MaxAdPlacer.Listener maxListener = new MaxAdPlacer.Listener() {
                    @Override
                    public void onAdLoaded(int i) {
                        listener.onAdLoaded(i);
                    }

                    @Override
                    public void onAdRemoved(int i) {
                        listener.onAdRemoved(i);
                    }

                    @Override
                    public void onAdClicked(MaxAd maxAd) {
                        NarayanLogEventManager.logClickAdsEvent(activity, maxAd.getAdUnitId());
                        listener.onAdClicked();
                    }

                    @Override
                    public void onAdRevenuePaid(MaxAd maxAd) {
                        listener.onAdRevenuePaid(new AppAdValue(maxAd));
                    }
                };
                MaxRecyclerAdapter adAdapter = NarayanAppLoving.getInstance().getNativeRepeatAdapter(activity, id, layoutCustomNative,
                        originalAdapter, maxListener, repeatingInterval);

                return new NarayanAdAdapter(adAdapter);
            default:
                return new NarayanAdAdapter(NarayanAdmob.getInstance().getNativeRepeatAdapter(activity, id, layoutCustomNative, layoutAdPlaceHolder,
                        originalAdapter, listener, repeatingInterval));
        }

    }

    /**
     * Result a NarayanAdAdapter with ad native fixed in position
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param layoutAdPlaceHolder
     * @param originalAdapter
     * @param listener
     * @param position
     * @return
     */
    public NarayanAdAdapter getNativeFixedPositionAdapter(Activity activity, String id, int layoutCustomNative, int layoutAdPlaceHolder, RecyclerView.Adapter originalAdapter,
                                                          NarayanAdPlacer.Listener listener, int position) {
        switch (Narayan_adConfig.getMediationProvider()) {
            case NarayanAdConfig.PROVIDER_MAX:
                MaxAdPlacer.Listener maxListener = new MaxAdPlacer.Listener() {
                    @Override
                    public void onAdLoaded(int i) {
                        listener.onAdLoaded(i);
                    }

                    @Override
                    public void onAdRemoved(int i) {
                        listener.onAdRemoved(i);
                    }

                    @Override
                    public void onAdClicked(MaxAd maxAd) {
                        NarayanLogEventManager.logClickAdsEvent(activity, maxAd.getAdUnitId());
                        listener.onAdClicked();
                    }

                    @Override
                    public void onAdRevenuePaid(MaxAd maxAd) {
                        listener.onAdRevenuePaid(new AppAdValue(maxAd));
                    }
                };
                MaxRecyclerAdapter adAdapter = NarayanAppLoving.getInstance().getNativeFixedPositionAdapter(activity, id, layoutCustomNative,
                        originalAdapter, maxListener, position);
                adAdapter.loadAds();
                return new NarayanAdAdapter(adAdapter);
            default:
                return new NarayanAdAdapter(NarayanAdmob.getInstance().getNativeFixedPositionAdapter(activity, id, layoutCustomNative, layoutAdPlaceHolder,
                        originalAdapter, listener, position));
        }
    }
}
