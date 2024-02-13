package com.sn.aichat.adspractice;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;

public class MainActivity extends AppCompatActivity implements InterstitialAdListener {
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private static final String TAG = "MainActivity";
    private AppOpenAd appOpenAd;
    private boolean isShowingAppOpenAd = false;
    private boolean isInterstitialAdDisplayed = false;
    private AdView adView;
    private GoogleAds mGoogleAds;

    private ImageView gifImageView;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleAds = new GoogleAds(this);
        String interstitialAdId = getString(R.string.admob_interstitial_id);
        mGoogleAds.initializeInterstitialAd(interstitialAdId);
        mGoogleAds.setInterstitialAdListener(this);
        TemplateView mTemplateView = findViewById(R.id.my_template);

        MobileAds.initialize(this);
        AdLoader adLoader = new AdLoader.Builder(this, getResources().getString(R.string.admob_native))
                .forNativeAd(nativeAd -> {
                    NativeTemplateStyle styles = new
                            NativeTemplateStyle.Builder().build();


                    mTemplateView.setVisibility(View.VISIBLE);
                    mTemplateView.setStyles(styles);
                    mTemplateView.setNativeAd(nativeAd);

                })
                .build();


        adLoader.loadAd(new AdRequest.Builder().build());

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
