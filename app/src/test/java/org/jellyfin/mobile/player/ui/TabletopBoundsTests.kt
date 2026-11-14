package org.jellyfin.mobile.player.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TabletopBoundsTests {
    @Test
    fun convertsWindowCoordinatesAndAvoidsHingeBand() {
        assertEquals(900, height(top = 100, hingeTop = 1000, hingeBottom = 1020))
        assertEquals(1000, height()) // zero-width crease
    }

    @Test
    fun restoresNormalLayoutForUnsupportedPostureAndPictureInPicture() {
        assertNull(TabletopBounds.upperPaneHeight(0, 2000, null, null, false))
        assertNull(height(pip = true))
    }

    @Test
    fun rejectsUnmeasuredAndNonIntersectingOrInvalidGeometry() {
        assertNull(height(viewHeight = 0))
        assertNull(height(hingeTop = 0, hingeBottom = 0))
        assertNull(height(hingeTop = -100, hingeBottom = -100))
        assertNull(height(hingeTop = 2000, hingeBottom = 2000))
        assertNull(height(hingeTop = 1990, hingeBottom = 2000))
        assertNull(height(hingeTop = 1000, hingeBottom = 900))
        assertNull(height(top = Int.MIN_VALUE, hingeTop = Int.MAX_VALUE, hingeBottom = Int.MAX_VALUE))
    }

    private fun height(
        top: Int = 0,
        viewHeight: Int = 2000,
        hingeTop: Int = 1000,
        hingeBottom: Int = hingeTop,
        pip: Boolean = false,
    ) = TabletopBounds.upperPaneHeight(top, viewHeight, hingeTop, hingeBottom, pip)
}
