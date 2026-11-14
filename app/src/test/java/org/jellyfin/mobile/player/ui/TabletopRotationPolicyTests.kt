package org.jellyfin.mobile.player.ui

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TabletopRotationPolicyTests {
    @Test
    fun distinguishesClamshellFromBookFoldAcrossWindowRotation() {
        assertTrue(TabletopRotationPolicy.matchesTabletopAxis(windowLandscape = false, hingeHorizontal = true))
        assertTrue(TabletopRotationPolicy.matchesTabletopAxis(windowLandscape = true, hingeHorizontal = false))
        assertFalse(TabletopRotationPolicy.matchesTabletopAxis(windowLandscape = false, hingeHorizontal = false))
        assertFalse(TabletopRotationPolicy.matchesTabletopAxis(windowLandscape = true, hingeHorizontal = true))
    }

    @Test
    fun portraitHalfFoldReleasesAppLandscapeRequest() {
        assertTrue(shouldRotate())
    }

    @Test
    fun physicalLandscapeOrUnknownReadingsPreserveCurrentOrientation() {
        // The listener maps both landscape and unknown sensor readings to portrait=false.
        assertFalse(shouldRotate(portrait = false))
    }

    @Test
    fun flatPhoneRetainsFullscreenLandscape() {
        assertFalse(shouldRotate(halfOpen = false))
    }

    @Test
    fun playbackLockAndPictureInPicturePreventRotation() {
        assertFalse(shouldRotate(locked = true))
        assertFalse(shouldRotate(pip = true))
    }

    @Test
    fun existingPortraitAndSystemOrientationAreNotOverridden() {
        assertFalse(shouldRotate(appLandscape = false))
    }

    private fun shouldRotate(
        halfOpen: Boolean = true,
        portrait: Boolean = true,
        appLandscape: Boolean = true,
        locked: Boolean = false,
        pip: Boolean = false,
    ) = TabletopRotationPolicy.shouldRequestPortrait(halfOpen, portrait, appLandscape, locked, pip)
}
