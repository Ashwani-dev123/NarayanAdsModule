
# NarayanModuleAds
This is SDK ads by [Narayan](https://Narayan.vn/). It has built in some sdk for easy use like
- Admob
- MAX Mediation(Applovin)
- Google Billing
- Adjust
- Appsflyer
- Firebase auto log tracking event, tROAS

# Import Module
~~~
	maven { url 'https://jitpack.io' }
	implementation 'com.github.NarayanVN:NarayanModuleAds:5.2.2'
~~~	 
# Summary
* [Setup NarayanAd](#setup_Narayanad)
	* [Setup id ads](#set_up_ads)
	* [Config ads](#config_ads)
	* [Ads Formats](#ads_formats)

* [Billing App](#billing_app)
* [Ads rule](#ads_rule)

# <a id="setup_Narayanad"></a>Setup NarayanAd
## <a id="set_up_ads"></a>Setup id ads for project
* The name must be the same as the name of the marketing request
* Config variant test and release in gradle
* appDev: using id admob test while dev
* appProduct: using exactly id admob,  build release (build file .aab)
~~~    
      productFlavors {
      appDev {
              manifestPlaceholders = [ ad_app_id:"AD_APP_ID_TEST" ]
              buildConfigField "String", "ads_inter_turn_on", "\"AD_ID_INTERSTIAL_TEST\""
              buildConfigField "String", "ads_inter_turn_off", "\"AD_ID_INTERSTIAL_TEST\""
	      buildConfigField "Boolean", "build_debug", "true"
           }
       appProduct {
            // ADS CONFIG BEGIN (required)
               manifestPlaceholders = [ ad_app_id:"AD_APP_ID" ]
               buildConfigField "String", "ads_inter_splash", "\"AD_ID_INTERSTIAL\""
               buildConfigField "String", "ads_inter_turn_on", "\"AD_ID_INTERSTIAL\""
	       buildConfigField "Boolean", "build_debug", "false"
            // ADS CONFIG END (required)
           }
      }
~~~
AndroidManiafest.xml
~~~
  <meta-data
  	android:name="com.google.android.gms.ads.APPLICATION_ID"
  	android:value="@string/admob_app_id" />
~~~
## <a id="config_ads"></a>Config ads
Create class Application

Configure your mediation here. using PROVIDER_ADMOB or PROVIDER_MAX
~~~
class App : AdsMultiDexApplication(){
    @Override
    public void onCreate() {
        super.onCreate();
	...
	NarayanAdConfig.setMediationProvider(NarayanAdConfig.PROVIDER_ADMOB);
        NarayanAdConfig.setVariant(BuildConfig.build_debug);
        NarayanAdConfig.enableAdjust(ADJUST_TOKEN); // enable ajust with adjust token
	NarayanAdConfig.enableAppsflyer(APPSFLYER_TOKEN);// enable ajust with appsflyer token
        NarayanAdConfig.setIdAdResume(AppOpenManager.AD_UNIT_ID_TEST);
        listTestDevice.add(ID_TEST_DEVICE);
        NarayanAdConfig.setListDeviceTest(listTestDevice);
	NarayanAd.getInstance().init(this, NarayanAdConfig, false);
	
	// Auto disable ad resume after user click ads and back to app
	Admob.getInstance().setDisableAdResumeWhenClickAds(true);
	// If true -> onNextAction() is called right after Ad Interstitial showed
	Admob.getInstance().setOpenActivityAfterShowInterAds(false);
	}
}
~~~
AndroidManiafest.xml
~~~
<application
android:name=".App"
...
>
~~~

## <a id="ads_formats"></a>Ads formats
### Ad Splash Interstitial
SplashActivity
~~~ 
    NarayanAdCallback adCallback = new NarayanAdCallback() {
        @Override
        public void onNextAction() {
            super.onNextAction();
            Log.d(TAG, "onNextAction");
            startMain();
        }
    };
~~~
~~~
        NarayanAd.getInstance().setInitCallback(new NarayanInitCallback() {
            @Override
            public void initAdSuccess() {
                NarayanAd.getInstance().loadSplashInterstitialAds(SplashActivity.this, idAdSplash, TIME_OUT, TIME_DELAY_SHOW_AD, true, adCallback);
            }
        });
~~~
### Interstitial
Load ad interstital before show
~~~
  private fun loadInterCreate() {
	ApInterstitialAd mInterstitialAd = NarayanAd.getInstance().getInterstitialAds(this, idInter);
  }
~~~
Show and auto release ad interstitial
~~~
         if (mInterstitialAd.isReady()) {
                NarayanAd.getInstance().forceShowInterstitial(this, mInterstitialAd, new NarayanAdCallback() {
			@Override
			public void onNextAction() {
			    super.onNextAction();
			    Log.d(TAG, "onNextAction");
			   startActivity(new Intent(MainActivity.this, MaxSimpleListActivity.class));
			}
                
                }, true);
            } else {
                loadAdInterstitial();
            }
~~~
### Ad Banner
include layout banner
activity_main.xml
~~~
  <include
  android:id="@+id/include"
  layout="@layout/layout_banner_control"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:layout_alignParentBottom="true"
  app:layout_constraintBottom_toBottomOf="parent" />
~~~
load ad banner
~~~
  NarayanAd.getInstance().loadBanner(this, idBanner);
~~~

### Ad Native
Load ad native before show
~~~
        NarayanAd.getInstance().loadNativeAdResultCallback(this,ID_NATIVE_AD, narayan.R.layout.custom_native_max_small,new NarayanAdCallback(){
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
               //save or show native 
            }
        });
~~~
show ad native
~~~
	NarayanAd.getInstance().populateNativeAdView(MainApplovinActivity.this,nativeAd,flParentNative,shimmerFrameLayout);
~~~
auto load and show native contains loading

activity_main.xml
~~~
  <include layout="@layout/layout_native_control" />
~~~
MainActivity
~~~
  NarayanAd.getInstance().loadNativeAd(this, idNative, layoutNativeCustom);
~~~
Load Ad native for recyclerView
~~~~
	// ad native repeating interval
	NarayanAdAdapter     adAdapter = NarayanAd.getInstance().getNativeRepeatAdapter(this, idNative, layoutCustomNative, narayan.R.layout.layout_native_medium,
                originalAdapter, listener, 4);
	
	// ad native fixed in position
    	NarayanAdAdapter   adAdapter = NarayanAd.getInstance().getNativeFixedPositionAdapter(this, idNative, layoutCustomNative, narayan.R.layout.layout_native_medium,
                originalAdapter, listener, 4);
	
        recyclerView.setAdapter(adAdapter.getAdapter());
        adAdapter.loadAds();
~~~~
### Ad Reward
Get and show reward
~~~
  ApRewardAd rewardAd = NarayanAd.getInstance().getRewardAd(this, idAdReward);

   if (rewardAd != null && rewardAd.isReady()) {
                NarayanAd.getInstance().forceShowRewardAd(this, rewardAd, new NarayanAdCallback());
            }
});
~~~
### Ad resume
App
~~~ 
  override fun onCreate() {
  	super.onCreate()
  	AppOpenManager.getInstance().enableAppResume()
	NarayanAdConfig.setIdAdResume(AppOpenManager.AD_UNIT_ID_TEST);
	...
  }
	

~~~


# <a id="billing_app"></a>Billing app
## Init Billing
Application
~~~
    @Override
    public void onCreate() {
        super.onCreate();
        AppPurchase.getInstance().initBilling(this,listINAPId,listSubsId);
    }
~~~
## Check status billing init
~~~
 if (AppPurchase.getInstance().getInitBillingFinish()){
            loadAdsPlash();
        }else {
            AppPurchase.getInstance().setBillingListener(new BillingListener() {
                @Override
                public void onInitBillingListener(int code) {
                         loadAdsPlash();
                }
            },5000);
        }
~~~
## Check purchase status
    //check purchase with PRODUCT_ID
	 AppPurchase.getInstance().isPurchased(this,PRODUCT_ID);
	 //check purchase all
	 AppPurchase.getInstance().isPurchased(this);
##  Purchase
	 AppPurchase.getInstance().purchase(this,PRODUCT_ID);
	 AppPurchase.getInstance().subscribe(this,SUBS_ID);
## Purchase Listener
	         AppPurchase.getInstance().setPurchaseListioner(new PurchaseListioner() {
                 @Override
                 public void onProductPurchased(String productId,String transactionDetails) {

                 }

                 @Override
                 public void displayErrorMessage(String errorMsg) {

                 }
             });

## Get id purchased
	  AppPurchase.getInstance().getIdPurchased();
## Consume purchase
	  AppPurchase.getInstance().consumePurchase(PRODUCT_ID);
## Get price
	  AppPurchase.getInstance().getPrice(PRODUCT_ID)
	  AppPurchase.getInstance().getPriceSub(SUBS_ID)
### Show iap dialog
	InAppDialog dialog = new InAppDialog(this);
	dialog.setCallback(() -> {
	     AppPurchase.getInstance().purchase(this,PRODUCT_ID);
	    dialog.dismiss();
	});
	dialog.show();



# <a id="ads_rule"></a>Ads rule
## Always add device test to idTestList with all of your team's device
To ignore invalid ads traffic
https://support.google.com/adsense/answer/16737?hl=en
## Before show full-screen ad (interstitial, app open ad), alway show a short loading dialog
To ignore accident click from user. This feature is existed in library
## Never reload ad on onAdFailedToLoad
To ignore infinite loop
