package com.hotukrainianboyz.nearly.advertisement

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.hotukrainianboyz.nearly.R

class AdsManager(context: Context) {

    private val mInterstitialAd: InterstitialAd = InterstitialAd(context)

    init {
        mInterstitialAd.adUnitId = context.getString(R.string.test_ad_id)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object: AdListener() {
            override fun onAdFailedToLoad(errorCode: Int) {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
    }

    fun showInterstitialAd(): Boolean{
        if(mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
            return true
        }
        return false
    }
}