package org.jellyfin.mobile.player.ui

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.jellyfin.mobile.databinding.FragmentPlayerBinding

/** Bounds the whole player, including Media3's zoom surface, without changing controller state. */
internal class PlayerTabletopHelper(
    private val activity: Activity,
    private val binding: FragmentPlayerBinding,
    private val onEnterTabletop: () -> Unit,
    private val onPostureAvailable: () -> Unit,
) {
    private var feature: FoldingFeature? = null
    private var inPictureInPicture = false
    private var disposed = false
    private val originalPlayerParams = FrameLayout.LayoutParams(binding.playerView.layoutParams as FrameLayout.LayoutParams)
    private val originalOverlayParams = FrameLayout.LayoutParams(binding.playerOverlay.layoutParams as FrameLayout.LayoutParams)
    val hasTabletopPosture: Boolean
        get() = feature != null
    var hasPostureInfo = false
        private set
    var isTabletop = false
        private set

    private val layoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> update() }

    fun start(owner: LifecycleOwner) {
        binding.root.addOnLayoutChangeListener(layoutListener)
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).catch {
                    feature = null
                    hasPostureInfo = true
                    update()
                    onPostureAvailable()
                }.collect { info ->
                    hasPostureInfo = true
                    feature = info.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull {
                        it.state == FoldingFeature.State.HALF_OPENED &&
                            it.orientation == FoldingFeature.Orientation.HORIZONTAL
                    }
                    update()
                    onPostureAvailable()
                }
            }
        }
    }

    fun onPictureInPictureChanged(enabled: Boolean) {
        inPictureInPicture = enabled
        update()
    }

    fun dispose() {
        disposed = true
        binding.root.removeOnLayoutChangeListener(layoutListener)
        restore()
    }

    private fun update() {
        if (disposed) return
        val location = IntArray(2)
        binding.root.getLocationInWindow(location)
        val fold = feature
        val height = TabletopBounds.upperPaneHeight(
            viewTop = location[1],
            viewHeight = binding.root.height,
            hingeTop = fold?.bounds?.top ?: 0,
            hingeBottom = fold?.bounds?.bottom ?: 0,
            isTabletop = fold != null,
            isInPictureInPicture = inPictureInPicture,
        )
        if (height == null) {
            restore()
        } else {
            val entering = !isTabletop
            isTabletop = true
            bound(binding.playerView, originalPlayerParams, height)
            bound(binding.playerOverlay, originalOverlayParams, height)
            if (entering) onEnterTabletop()
        }
    }

    private fun bound(view: View, original: FrameLayout.LayoutParams, height: Int) {
        val current = view.layoutParams as FrameLayout.LayoutParams
        if (current.height == height && current.gravity == Gravity.TOP) return
        view.layoutParams = FrameLayout.LayoutParams(original).apply {
            this.height = height
            gravity = Gravity.TOP
        }
    }

    private fun restore() {
        if (!isTabletop) return
        isTabletop = false
        binding.playerView.layoutParams = FrameLayout.LayoutParams(originalPlayerParams)
        binding.playerOverlay.layoutParams = FrameLayout.LayoutParams(originalOverlayParams)
    }
}
