package com.ads.narayan.ads;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class NarayanAdConfig {

    //switch mediation use for app
    public static final int PROVIDER_ADMOB = 0;
    public static final int PROVIDER_MAX = 1;

    /**
     * config ad mediation using for app
     */
    private int mediationProvider = PROVIDER_ADMOB;

    private boolean isVariantDev = false;
    private boolean enableAdjust = false;
    private boolean enableAppsflyer= false;
    /**
     * adjustToken enable adjust and setup adjust token
     */
    private String adjustToken = "";
    /**
     * appsflyerToken enable Appsflyer and setup dev key
     */
    private String appsflyerToken = "";
    /**
     * eventNamePurchase push event to adjust when user purchased
     */
    private String eventNamePurchase = "";
    private String idAdResume;
    private List<String> listDeviceTest = new ArrayList();

    private Application application;
    private boolean enableAdResume = false;


    public void setMediationProvider(int mediationProvider){
        this.mediationProvider = mediationProvider;
    }

    public void setVariant(Boolean isVariantDev){
        this.isVariantDev = isVariantDev;
    }

    public void enableAppsflyer(String appsflyerToken){
        enableAppsflyer = true;
        this.appsflyerToken = appsflyerToken;
    }

    public void enableAdjust(String adjustToken){
        enableAdjust = true;
        this.adjustToken = adjustToken;
    }

    public void enableAdjust(String adjustToken,String eventNamePurchase){
        enableAdjust = true;
        this.adjustToken = adjustToken;
        this.eventNamePurchase = eventNamePurchase;
    }




    public String getEventNamePurchase() {
        return eventNamePurchase;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }



    public int getMediationProvider() {
        return mediationProvider;
    }

    public Boolean isVariantDev() {
        return isVariantDev;
    }


    public String getAdjustToken() {
        return adjustToken;
    }

    public String getAppsflyerToken() {
        return appsflyerToken;
    }

    public String getIdAdResume() {
        return idAdResume;
    }

    public List<String> getListDeviceTest() {
        return listDeviceTest;
    }

    public void setListDeviceTest(List<String> listDeviceTest) {
        this.listDeviceTest = listDeviceTest;
    }



    public void setIdAdResume(String idAdResume) {
        this.idAdResume = idAdResume;
        enableAdResume =true;
    }

    public Boolean isEnableAdResume(){
        return enableAdResume;
    }


    public Boolean isEnableAdjust() {
        return enableAdjust;
    }

    public boolean isEnableAppsflyer() {
        return enableAppsflyer;
    }
}
