package org.jellyfin.mobile.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener

/**
 * Listener that watches the current device orientation.
 * It makes sure that the orientation sensor can still be used (if enabled)
 * after toggling the orientation manually.
 */
class SmartOrientationListener(
    private val activity: Activity,
    private val onPhysicalOrientationChanged: (Int) -> Unit = {},
) : OrientationEventListener(activity) {
    override fun enable() {
        onPhysicalOrientationChanged(ORIENTATION_UNKNOWN)
        super.enable()
    }

    override fun disable() {
        onPhysicalOrientationChanged(ORIENTATION_UNKNOWN)
        super.disable()
    }

    override fun onOrientationChanged(orientation: Int) {
        // Physical posture remains useful when system auto-rotate is disabled.
        onPhysicalOrientationChanged(orientation)
        if (!activity.isAutoRotateOn()) return

        val isAtTarget = when (activity.requestedOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> orientation in Constants.ORIENTATION_PORTRAIT_RANGE
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> orientation in Constants.ORIENTATION_LANDSCAPE_RANGE
            else -> false
        }
        if (isAtTarget) {
            // Reset to unspecified orientation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}
