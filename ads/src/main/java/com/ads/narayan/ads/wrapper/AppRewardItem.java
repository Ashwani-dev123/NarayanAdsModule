package com.ads.narayan.ads.wrapper;

import com.applovin.mediation.MaxReward;
import com.google.android.gms.ads.rewarded.RewardItem;

public class AppRewardItem {

    private RewardItem admobRewardItem;
    private MaxReward maxRewardItem;

    public AppRewardItem(MaxReward maxRewardItem) {
        this.maxRewardItem = maxRewardItem;
    }

    public AppRewardItem(RewardItem admobRewardItem) {
        this.admobRewardItem = admobRewardItem;
    }

    public RewardItem getAdmobRewardItem() {
        return admobRewardItem;
    }

    public void setAdmobRewardItem(RewardItem admobRewardItem) {
        this.admobRewardItem = admobRewardItem;
    }

    public MaxReward getMaxRewardItem() {
        return maxRewardItem;
    }

    public void setMaxRewardItem(MaxReward maxRewardItem) {
        this.maxRewardItem = maxRewardItem;
    }
}
