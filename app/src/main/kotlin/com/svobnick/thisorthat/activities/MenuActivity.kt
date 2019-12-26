package com.svobnick.thisorthat.activities

//class MenuActivity : Activity() {
//
//    private lateinit var mInterstitialAd: InterstitialAd
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_statistics_menu)
//
//        MobileAds.initialize(this, getString(R.string.application_ad_id))
//
//        mInterstitialAd = InterstitialAd(this)
//        mInterstitialAd.adUnitId = getString(R.string.new_question_interstitial_ad_id)
//        mInterstitialAd.loadAd(AdRequest.Builder().addTestDevice("A6160C7F178A849F4631ADF45ABA4274").build())
//        mInterstitialAd.adListener = object : AdListener() {
//            override fun onAdClosed() {
//                mInterstitialAd.loadAd(AdRequest.Builder().addTestDevice("A6160C7F178A849F4631ADF45ABA4274").build())
//            }
//
//            override fun onAdLoaded() {
//                Log.i("Ads", "Loaded!")
//            }
//        }
//
//    }
//
//    fun onNewQuestionButtonClick(selected: View) {
//        if (mInterstitialAd.isLoaded) {
//            mInterstitialAd.show()
//            val intent = Intent(this, NewQuestionActivity::class.java)
//            startActivity(intent)
//        } else {
//            Log.i("ADS", "The interstitial wasn't loaded yet")
//        }
//    }
//}