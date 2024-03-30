package com.haneef.mindtrek.util
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MySwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    private var nestedScrollView: NestedScrollView? = null

    // Attach the NestedScrollView to the custom SwipeRefreshLayout
    fun setNestedScrollView(nestedScrollView: NestedScrollView) {
        this.nestedScrollView = nestedScrollView
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Intercept touch events only when the NestedScrollView is at the topmost position.
        return if (shouldInterceptTouchEvent(ev)) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

    private fun shouldInterceptTouchEvent(ev: MotionEvent): Boolean {
        nestedScrollView?.let {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Check if the NestedScrollView is scrolled to the topmost position
                    return !it.canScrollVertically(-1)
                }
                MotionEvent.ACTION_MOVE -> {
                    // Check if the NestedScrollView is scrolled to the topmost position
                    return !it.canScrollVertically(-1)
                }

                else -> {}
            }
        }
        return false
    }
}
