package org.jellyfin.mobile.player.ui

/** Window-relative hinge geometry converted into a usable upper player pane. */
internal object TabletopBounds {
    fun upperPaneHeight(
        viewTop: Int,
        viewHeight: Int,
        hingeTop: Int?,
        hingeBottom: Int?,
        isInPictureInPicture: Boolean,
    ): Int? {
        if (isInPictureInPicture || viewHeight <= 0) return null
        if (hingeTop == null || hingeBottom == null || hingeBottom < hingeTop) return null
        val offset = hingeTop.toLong() - viewTop
        val bottom = hingeBottom.toLong() - viewTop
        // Require usable space on both sides; never retain an old split for invalid geometry.
        return if (offset > 0 && bottom < viewHeight) offset.toInt() else null
    }
}
