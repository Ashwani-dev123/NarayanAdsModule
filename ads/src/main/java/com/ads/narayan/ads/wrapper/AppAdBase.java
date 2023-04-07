package com.ads.narayan.ads.wrapper;


public abstract class AppAdBase {
    protected StatusAd status = StatusAd.AD_INIT;

    public AppAdBase(StatusAd status) {
        this.status = status;
    }

    public AppAdBase() {
    }

    public StatusAd getStatus() {
        return status;
    }

    public void setStatus(StatusAd status) {
        this.status = status;
    }


    abstract boolean isReady();

    public boolean isNotReady(){
        return !isReady();
    }

    public boolean isLoading(){
        return status == StatusAd.AD_LOADING;
    }
    public boolean isLoadFail(){
        return status == StatusAd.AD_LOAD_FAIL;
    }
}
