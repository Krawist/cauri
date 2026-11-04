package com.cauri.core.capture.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.provider.Settings
import com.cauri.core.capture.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Default implementation for [DeviceSecurity], on Android's own dialog: no library. */
@Singleton
internal class DeviceSecurityImpl @Inject constructor(@ApplicationContext private val context: Context) :
    DeviceSecurity {
    override fun available(): Boolean = context.getSystemService(KeyguardManager::class.java).isDeviceSecure

    override suspend fun authenticate(activity: Activity): Boolean {
        if (!available()) return false
        return suspendCancellableCoroutine { continuation ->
            val cancel = CancellationSignal()
            continuation.invokeOnCancellation { cancel.cancel() }
            prompt(activity).authenticate(
                cancel,
                activity.mainExecutor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) continuation.resume(true)
                    }

                    // Cancelled, too many tries or no way left: the app stays locked, the user may try again.
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        if (continuation.isActive) continuation.resume(false)
                    }
                },
            )
        }
    }

    override fun securitySettings(): Intent =
        Intent(Settings.ACTION_SECURITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    // The fingerprint or the face when the phone knows them, else the screen lock's code: whatever unlocks the phone.
    private fun prompt(activity: Activity): BiometricPrompt {
        val builder = BiometricPrompt.Builder(activity)
            .setTitle(context.getString(R.string.security_prompt_title))
            .setSubtitle(context.getString(R.string.security_prompt_subtitle))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL)
        } else {
            @Suppress("DEPRECATION") // The authenticators can be chosen from Android 11 only.
            builder.setDeviceCredentialAllowed(true)
        }
        return builder.build()
    }
}
