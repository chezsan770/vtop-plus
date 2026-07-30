package com.vtopu.app

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class AdMobManager(
    private val activity: Activity
) {
    private val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
    private val initializationStarted = AtomicBoolean(false)

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun gatherConsent(
        onPrivacyOptionsChanged: (Boolean) -> Unit,
        onAdsReady: () -> Unit
    ) {
        val parameters = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                notifyPrivacyOptionsChanged(onPrivacyOptionsChanged)
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    notifyPrivacyOptionsChanged(onPrivacyOptionsChanged)
                    initializeAdsIfAllowed(onAdsReady)
                }
                initializeAdsIfAllowed(onAdsReady)
            },
            {
                notifyPrivacyOptionsChanged(onPrivacyOptionsChanged)
                initializeAdsIfAllowed(onAdsReady)
            }
        )
    }

    fun showPrivacyOptions(
        onPrivacyOptionsChanged: (Boolean) -> Unit,
        onAdsReady: () -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            notifyPrivacyOptionsChanged(onPrivacyOptionsChanged)
            initializeAdsIfAllowed(onAdsReady)
        }
    }

    private fun initializeAdsIfAllowed(onAdsReady: () -> Unit) {
        if (!consentInformation.canRequestAds()) return
        if (!initializationStarted.compareAndSet(false, true)) return

        val testDeviceIds = BuildConfig.ADMOB_TEST_DEVICE_IDS
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
        if (testDeviceIds.isNotEmpty()) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
            )
        }

        Thread {
            MobileAds.initialize(activity) {
                activity.runOnUiThread(onAdsReady)
            }
        }.start()
    }

    private fun notifyPrivacyOptionsChanged(onChanged: (Boolean) -> Unit) {
        activity.runOnUiThread {
            onChanged(isPrivacyOptionsRequired)
        }
    }
}
