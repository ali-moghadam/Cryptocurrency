/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alirnp.watch.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.annotation.StyleRes
import androidx.annotation.UiThread
import com.alirnp.watch.R

/**
 * BoxInsetLayout is a screen shape-aware ViewGroup that can box its children in the center
 * square of a round screen by using the `layout_boxedEdges` attribute. The values for this attribute
 * specify the child's edges to be boxed in: `left|top|right|bottom` or `all`. The
 * `layout_boxedEdges` attribute is ignored on a device with a rectangular screen.
 */
@UiThread
class BoxInsetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @StyleRes defStyle: Int = 0
) : ViewGroup(context, attrs, defStyle) {
    private val mScreenHeight: Int
    private val mScreenWidth: Int
    private var mIsRound = false
    private var mForegroundPadding: Rect? = null
    private var mInsets: Rect? = null
    private var mForegroundDrawable: Drawable? = null
    override fun setForeground(drawable: Drawable) {
        super.setForeground(drawable)
        mForegroundDrawable = drawable
        if (mForegroundPadding == null) {
            mForegroundPadding = Rect()
        }
        if (mForegroundDrawable != null) {
            drawable.getPadding(mForegroundPadding!!)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    /* getSystemWindowInsetXXXX */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mIsRound = resources.configuration.isScreenRound
        val insets = rootWindowInsets
        mInsets!![insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight] =
            insets.systemWindowInsetBottom
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount
        // find max size
        var maxWidth = 0
        var maxHeight = 0
        var childState = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                var marginLeft = 0
                var marginRight = 0
                var marginTop = 0
                var marginBottom = 0
                if (mIsRound) {
                    // round screen, check boxed, don't use margins on boxed
                    if (lp.boxedEdges and LayoutParams.BOX_LEFT == 0) {
                        marginLeft = lp.leftMargin
                    }
                    if (lp.boxedEdges and LayoutParams.BOX_RIGHT == 0) {
                        marginRight = lp.rightMargin
                    }
                    if (lp.boxedEdges and LayoutParams.BOX_TOP == 0) {
                        marginTop = lp.topMargin
                    }
                    if (lp.boxedEdges and LayoutParams.BOX_BOTTOM == 0) {
                        marginBottom = lp.bottomMargin
                    }
                } else {
                    // rectangular, ignore boxed, use margins
                    marginLeft = lp.leftMargin
                    marginTop = lp.topMargin
                    marginRight = lp.rightMargin
                    marginBottom = lp.bottomMargin
                }
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                maxWidth = Math.max(maxWidth, child.measuredWidth + marginLeft + marginRight)
                maxHeight = Math.max(
                    maxHeight,
                    child.measuredHeight + marginTop + marginBottom
                )
                childState = combineMeasuredStates(childState, child.measuredState)
            }
        }
        // Account for padding too
        maxWidth += (paddingLeft + mForegroundPadding!!.left + paddingRight
                + mForegroundPadding!!.right)
        maxHeight += (paddingTop + mForegroundPadding!!.top + paddingBottom
                + mForegroundPadding!!.bottom)

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, suggestedMinimumHeight)
        maxWidth = Math.max(maxWidth, suggestedMinimumWidth)

        // Check against our foreground's minimum height and width
        if (mForegroundDrawable != null) {
            maxHeight = Math.max(maxHeight, mForegroundDrawable!!.minimumHeight)
            maxWidth = Math.max(maxWidth, mForegroundDrawable!!.minimumWidth)
        }
        val measuredWidth = resolveSizeAndState(maxWidth, widthMeasureSpec, childState)
        val measuredHeight = resolveSizeAndState(
            maxHeight, heightMeasureSpec,
            childState shl MEASURED_HEIGHT_STATE_SHIFT
        )
        setMeasuredDimension(measuredWidth, measuredHeight)

        // determine boxed inset
        val boxInset = calculateInset(measuredWidth, measuredHeight)
        // adjust the the children measures, if necessary
        for (i in 0 until count) {
            measureChild(widthMeasureSpec, heightMeasureSpec, boxInset, i)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        val parentLeft = paddingLeft + mForegroundPadding!!.left
        val parentRight = right - left - paddingRight - mForegroundPadding!!.right
        val parentTop = paddingTop + mForegroundPadding!!.top
        val parentBottom = bottom - top - paddingBottom - mForegroundPadding!!.bottom
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                val width = child.measuredWidth
                val height = child.measuredHeight
                var childLeft: Int
                var childTop: Int
                var gravity = lp.gravity
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY
                }
                val layoutDirection = layoutDirection
                val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
                val horizontalGravity = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                val desiredInset = calculateInset(measuredWidth, measuredHeight)

                // If the child's width is match_parent then we can ignore gravity.
                val leftChildMargin = calculateChildLeftMargin(lp, horizontalGravity, desiredInset)
                val rightChildMargin = calculateChildRightMargin(
                    lp, horizontalGravity,
                    desiredInset
                )
                childLeft = if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    parentLeft + leftChildMargin
                } else {
                    when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                        Gravity.CENTER_HORIZONTAL -> parentLeft + (parentRight - parentLeft - width) / 2 + leftChildMargin - rightChildMargin
                        Gravity.RIGHT -> parentRight - width - rightChildMargin
                        Gravity.LEFT -> parentLeft + leftChildMargin
                        else -> parentLeft + leftChildMargin
                    }
                }

                // If the child's height is match_parent then we can ignore gravity.
                val topChildMargin = calculateChildTopMargin(lp, verticalGravity, desiredInset)
                val bottomChildMargin = calculateChildBottomMargin(
                    lp, verticalGravity,
                    desiredInset
                )
                childTop = if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    parentTop + topChildMargin
                } else {
                    when (verticalGravity) {
                        Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - height) / 2 + topChildMargin - bottomChildMargin
                        Gravity.BOTTOM -> parentBottom - height - bottomChildMargin
                        Gravity.TOP -> parentTop + topChildMargin
                        else -> parentTop + topChildMargin
                    }
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height)
            }
        }
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    private fun measureChild(
        widthMeasureSpec: Int, heightMeasureSpec: Int, desiredMinInset: Int,
        i: Int
    ) {
        val child = getChildAt(i)
        val childLayoutParams = child.layoutParams as LayoutParams
        var gravity = childLayoutParams.gravity
        if (gravity == -1) {
            gravity = DEFAULT_CHILD_GRAVITY
        }
        val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
        val horizontalGravity = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
        val childWidthMeasureSpec: Int
        val childHeightMeasureSpec: Int
        val leftParentPadding = paddingLeft + mForegroundPadding!!.left
        val rightParentPadding = paddingRight + mForegroundPadding!!.right
        val topParentPadding = paddingTop + mForegroundPadding!!.top
        val bottomParentPadding = paddingBottom + mForegroundPadding!!.bottom

        // adjust width
        val totalWidthMargin = leftParentPadding + rightParentPadding + calculateChildLeftMargin(
            childLayoutParams, horizontalGravity, desiredMinInset
        ) + calculateChildRightMargin(
            childLayoutParams, horizontalGravity, desiredMinInset
        )

        // adjust height
        val totalHeightMargin = topParentPadding + bottomParentPadding + calculateChildTopMargin(
            childLayoutParams, verticalGravity, desiredMinInset
        ) + calculateChildBottomMargin(
            childLayoutParams, verticalGravity, desiredMinInset
        )
        childWidthMeasureSpec = getChildMeasureSpec(
            widthMeasureSpec, totalWidthMargin,
            childLayoutParams.width
        )
        childHeightMeasureSpec = getChildMeasureSpec(
            heightMeasureSpec, totalHeightMargin,
            childLayoutParams.height
        )
        val maxAllowedWidth = measuredWidth - totalWidthMargin
        val maxAllowedHeight = measuredHeight - totalHeightMargin
        if (child.measuredWidth > maxAllowedWidth
            || child.measuredHeight > maxAllowedHeight
        ) {
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }
    }

    private fun calculateChildLeftMargin(
        lp: LayoutParams,
        horizontalGravity: Int,
        desiredMinInset: Int
    ): Int {
        if (mIsRound && lp.boxedEdges and LayoutParams.BOX_LEFT != 0) {
            if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT || horizontalGravity == Gravity.LEFT) {
                return lp.leftMargin + desiredMinInset
            }
        }
        return lp.leftMargin
    }

    private fun calculateChildRightMargin(
        lp: LayoutParams,
        horizontalGravity: Int,
        desiredMinInset: Int
    ): Int {
        if (mIsRound && lp.boxedEdges and LayoutParams.BOX_RIGHT != 0) {
            if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT || horizontalGravity == Gravity.RIGHT) {
                return lp.rightMargin + desiredMinInset
            }
        }
        return lp.rightMargin
    }

    private fun calculateChildTopMargin(
        lp: LayoutParams,
        verticalGravity: Int,
        desiredMinInset: Int
    ): Int {
        if (mIsRound && lp.boxedEdges and LayoutParams.BOX_TOP != 0) {
            if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT || verticalGravity == Gravity.TOP) {
                return lp.topMargin + desiredMinInset
            }
        }
        return lp.topMargin
    }

    private fun calculateChildBottomMargin(
        lp: LayoutParams,
        verticalGravity: Int,
        desiredMinInset: Int
    ): Int {
        if (mIsRound && lp.boxedEdges and LayoutParams.BOX_BOTTOM != 0) {
            if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT || verticalGravity == Gravity.BOTTOM) {
                return lp.bottomMargin + desiredMinInset
            }
        }
        return lp.bottomMargin
    }

    private fun calculateInset(measuredWidth: Int, measuredHeight: Int): Int {
        val rightEdge = Math.min(measuredWidth, mScreenWidth)
        val bottomEdge = Math.min(measuredHeight, mScreenHeight)
        return (FACTOR * Math.max(rightEdge, bottomEdge)).toInt()
    }

    /**
     * Per-child layout information for layouts that support margins, gravity and boxedEdges.
     * See [BoxInsetLayout Layout Attributes][R.styleable.BoxInsetLayout_Layout] for a list
     * of all child view attributes that this class supports.
     *
     */
    class LayoutParams : FrameLayout.LayoutParams {
        /** @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @IntDef(BOX_NONE, BOX_LEFT, BOX_TOP, BOX_RIGHT, BOX_BOTTOM, BOX_ALL)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class BoxedEdges

        /** Specifies the screen-specific insets for each of the child edges.  */
        @BoxedEdges
        var boxedEdges = BOX_NONE

        /**
         * Creates a new set of layout parameters. The values are extracted from the supplied
         * attributes set and context.
         *
         * @param context the application environment
         * @param attrs the set of attributes from which to extract the layout parameters' values
         */
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val a = context.obtainStyledAttributes(
                attrs, R.styleable.BoxInsetLayout_Layout,
                0, 0
            )
            var boxedEdgesResourceKey = R.styleable.BoxInsetLayout_Layout_layout_boxedEdges
            if (!a.hasValueOrEmpty(R.styleable.BoxInsetLayout_Layout_layout_boxedEdges)) {
                boxedEdgesResourceKey = R.styleable.BoxInsetLayout_Layout_boxedEdges
            }
            boxedEdges = a.getInt(boxedEdgesResourceKey, BOX_NONE)
            a.recycle()
        }

        /**
         * Creates a new set of layout parameters with the specified width and height.
         *
         * @param width the width, either [.MATCH_PARENT],
         * [.WRAP_CONTENT] or a fixed size in pixels
         * @param height the height, either [.MATCH_PARENT],
         * [.WRAP_CONTENT] or a fixed size in pixelsy
         */
        constructor(width: Int, height: Int) : super(width, height) {}

        /**
         * Creates a new set of layout parameters with the specified width, height
         * and gravity.
         *
         * @param width the width, either [.MATCH_PARENT],
         * [.WRAP_CONTENT] or a fixed size in pixels
         * @param height the height, either [.MATCH_PARENT],
         * [.WRAP_CONTENT] or a fixed size in pixels
         * @param gravity the gravity
         *
         * @see android.view.Gravity
         */
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity) {}
        constructor(width: Int, height: Int, gravity: Int, @BoxedEdges boxed: Int) : super(
            width,
            height,
            gravity
        ) {
            boxedEdges = boxed
        }

        /**
         * Copy constructor. Clones the width and height of the source.
         *
         * @param source The layout params to copy from.
         */
        constructor(source: ViewGroup.LayoutParams) : super(source) {}

        /**
         * Copy constructor. Clones the width, height and margin values.
         *
         * @param source The layout params to copy from.
         */
        constructor(source: MarginLayoutParams) : super(source) {}

        /**
         * Copy constructor. Clones the width, height, margin values, and
         * gravity of the source.
         *
         * @param source The layout params to copy from.
         */
        constructor(source: FrameLayout.LayoutParams) : super(source) {}

        /**
         * Copy constructor. Clones the width, height, margin values, boxedEdges and
         * gravity of the source.
         *
         * @param source The layout params to copy from.
         */
        constructor(source: LayoutParams) : super(source) {
            boxedEdges = source.boxedEdges
            gravity = source.gravity
        }

        companion object {
            /** Default boxing setting. There are no insets forced on the child views.  */
            const val BOX_NONE = 0x0

            /** The view will force an inset on the left edge of the children.  */
            const val BOX_LEFT = 0x01

            /** The view will force an inset on the top edge of the children.  */
            const val BOX_TOP = 0x02

            /** The view will force an inset on the right edge of the children.  */
            const val BOX_RIGHT = 0x04

            /** The view will force an inset on the bottom edge of the children.  */
            const val BOX_BOTTOM = 0x08

            /** The view will force an inset on all of the edges of the children.  */
            const val BOX_ALL = 0x0F
        }
    }

    companion object {
        private const val FACTOR = 0.146447f //(1 - sqrt(2)/2)/2
        private const val DEFAULT_CHILD_GRAVITY = Gravity.TOP or Gravity.START
    }
    /**
     * Perform inflation from XML and apply a class-specific base style from a theme attribute.
     * This constructor allows subclasses to use their own base style when they are inflating.
     *
     * @param context  The [Context] the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a reference to a style
     * resource that supplies default values for the view. Can be 0 to not look for
     * defaults.
     */
    /**
     * Constructor that is called when inflating a view from XML. This is called when a view is
     * being constructed from an XML file, supplying attributes that were specified in the XML
     * file. This version uses a default style of 0, so the only attribute values applied are those
     * in the Context's Theme and the given AttributeSet.
     *
     *
     *
     *
     * The method onFinishInflate() will be called after all children have been added.
     *
     * @param context The [Context] the view is running in, through which it can access
     * the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The [Context] the view is running in, through which it can access
     * the current theme, resources, etc.
     */
    init {
        // make sure we have a foreground padding object
        if (mForegroundPadding == null) {
            mForegroundPadding = Rect()
        }
        if (mInsets == null) {
            mInsets = Rect()
        }
        mScreenHeight = Resources.getSystem().displayMetrics.heightPixels
        mScreenWidth = Resources.getSystem().displayMetrics.widthPixels
    }
}