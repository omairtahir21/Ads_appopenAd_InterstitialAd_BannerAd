package com.sn.aichat.adspractice;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.jetbrains.annotations.NotNull;

public class GoogleAds {
    String id;
    private AdView adview;
    private Context context;
    private int googleAdRefreshTime = 15000;
    public InterstitialAd mInterstitialAd=null;
    private boolean isNetworkConnected = false, showInterstitialAd = false, isBreakAd = false;
    private InterstitialAdListener listener = null;
    private static final String LOG_TAG = "Ads";
    private final Handler adsHandler = new Handler();

    public GoogleAds(Context context) {
        this.context = context;
    }

    public GoogleAds(Context context, AdView adview) {
        super();
        this.context = context;
        this.adview = adview;
        setAdsListener();
        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);
        isNetworkConnected = NetworkUtils.isNetworkConnected(context);
    }

    public void initializeInterstitialAd(String adid) {
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        id=adid;
        AdRequest adRequest = new AdRequest.Builder().build();
        if (mInterstitialAd==null) {
            InterstitialAd.load(
                    context,
                    adid,
                    adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAdloc) {
                            mInterstitialAd = interstitialAdloc;
                            if (listener != null)
                                listener.adLoaded();
                            if (showInterstitialAd)
                                showInterstitialAds(false);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            mInterstitialAd = null;
                            if (listener != null)
                                listener.AdFailed();
                        }
                    });
        }
    }

    public void callInterstitialAds(final boolean showAd) {
        showInterstitialAd = showAd;
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        initializeInterstitialAd(id);
    }

    public void showInterstitialAds(boolean isSplashActivity) {
        if (mInterstitialAd!= null)
            mInterstitialAd.show((Activity) context);
        else {
            if (listener != null) {
                if (isSplashActivity)
                    listener.AdFailed();
                else
                    listener.adClosed();
            }
        }
    }

    public void startAdsCall() {
        if (adview != null) {
            isBreakAd = false;
            if (isNetworkConnected || NetworkUtils.isNetworkConnected(context)) {
                adview.resume();
                AdRequest adRequest = new AdRequest.Builder().build();
                adview.loadAd(adRequest);
            } else {
                isNetworkConnected = false;
                adsHandler.removeCallbacks(sendUpdatesToUI);
                if(!isBreakAd)
                    adsHandler.postDelayed(sendUpdatesToUI, googleAdRefreshTime);
            }
        }
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            if(isBreakAd)
                return;
            adsHandler.removeCallbacks(sendUpdatesToUI);
            updateUIAds();
        }
    };

    private final void updateUIAds() {
        if (NetworkUtils.isNetworkConnected(context)) {
            isNetworkConnected = true;
            adsHandler.removeCallbacks(sendUpdatesToUI);
            AdRequest adRequest = new AdRequest.Builder().build();
            adview.loadAd(adRequest);
        } else {
            isNetworkConnected = false;
            adsHandler.removeCallbacks(sendUpdatesToUI);
            if(!isBreakAd)
                adsHandler.postDelayed(sendUpdatesToUI, googleAdRefreshTime);
        }
    }

    public void stopAdsCall() {
        if (adview != null) {
            isBreakAd = true;
            adview.pause();
            adsHandler.removeCallbacks(sendUpdatesToUI);
        }
    }

    public boolean isInterstitialAdLoaded(){
        if (mInterstitialAd!=null){
            return true;
        }else{
            return  false;
        }
    }

    private void setAdsListener() {
        adview.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {}

            @Override
            public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                isNetworkConnected = false;
                adsHandler.removeCallbacks(sendUpdatesToUI);
                if(!isBreakAd)
                    adsHandler.postDelayed(sendUpdatesToUI, googleAdRefreshTime);
            }

            @Override
            public void onAdOpened() {}

            @Override
            public void onAdLoaded() {}
        });
    }

    private String getErrorReason(int errorCode) {
        String errorReason = "";
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Internal error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "Invalid request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "No fill";
                break;
        }
        return errorReason;
    }

    public void setInterstitialAdListener(InterstitialAdListener listener) {
        this.listener = listener;
    }
}

class NetworkUtils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
}
