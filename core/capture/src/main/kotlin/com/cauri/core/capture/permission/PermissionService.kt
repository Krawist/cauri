package com.cauri.core.capture.permission

import android.content.Intent

/** Status of the permissions Cauri relies on, and the system screens where the user changes them. */
interface PermissionService {
    /** Reads the status from Android: call it again when the user comes back from the system settings. */
    fun status(): PermissionStatus

    /** System screen where the user gives Cauri access to the notifications. */
    fun notificationAccessSettings(): Intent

    /** System screen of Cauri's own notifications. */
    fun notificationSettings(): Intent

    /** System screen of Cauri's details, which lists its permissions, location included. */
    fun appSettings(): Intent
}
