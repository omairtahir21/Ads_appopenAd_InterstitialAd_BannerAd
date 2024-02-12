package com.sn.aichat.adspractice;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements InterstitialAdListener {
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private static final String TAG = "MainActivity";
    private AppOpenAd appOpenAd;
    private boolean isShowingAppOpenAd = false;
    private boolean isInterstitialAdDisplayed = false;
    private AdView adView;
    private GoogleAds mGoogleAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleAds = new GoogleAds(this);
        String interstitialAdId = getString(R.string.admob_interstitial_id);
        mGoogleAds.initializeInterstitialAd(interstitialAdId);
        mGoogleAds.setInterstitialAdListener(this);

        Button interstitialAdButton = findViewById(R.id.interstitialAdbutton);
        interstitialAdButton.setOnClickListener(v -> {
            if(mGoogleAds.isInterstitialAdLoaded()) {
                mGoogleAds.showInterstitialAds(false);
            }
        });

        MobileAds.initialize(this, initializationStatus -> {});
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loadAppOpenAd();
        loadBannerAd();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleAds != null) {
            if (!mGoogleAds.isInterstitialAdLoaded()) {
                mGoogleAds.callInterstitialAds(false);
            }
        }
    }
    private void loadAppOpenAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        AppOpenAd.load(
                this,
                "ca-app-pub-3940256099942544/9257395921",
                adRequest,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        showAppOpenAdIfAvailable();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "AppOpenAd failed to load: " + loadAdError.getMessage());
                    }
                }
        );
    }

    private void showAppOpenAdIfAvailable() {
        if (appOpenAd != null && !isShowingAppOpenAd) {
            isShowingAppOpenAd = true;
            appOpenAd.show(this);
            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    isShowingAppOpenAd = false;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.e(TAG, "AppOpenAd failed to show: " + adError.getMessage());
                }
            });
        } else {
            loadAppOpenAd();
        }
    }

    private void loadBannerAd() {
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void adLoaded() {}

    @Override
    public void adClosed() {}

    @Override
    public void AdFailed() {}
}
