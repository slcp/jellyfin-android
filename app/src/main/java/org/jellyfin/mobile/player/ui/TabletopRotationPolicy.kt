package org.jellyfin.mobile.player.ui

/** Only undo an app landscape request when portrait posture is positively known. */
internal object TabletopRotationPolicy {
    // Rotating a portrait clamshell window to landscape turns its hinge vertical.
    fun matchesTabletopAxis(windowLandscape: Boolean, hingeHorizontal: Boolean): Boolean =
        windowLandscape != hingeHorizontal

    fun shouldRequestPortrait(
        halfOpen: Boolean,
        physicallyPortrait: Boolean,
        appRequestedLandscape: Boolean,
        playbackLocked: Boolean,
        inPictureInPicture: Boolean,
    ): Boolean {
        if (!halfOpen || !physicallyPortrait) return false
        if (playbackLocked || inPictureInPicture) return false
        return appRequestedLandscape
    }
}
