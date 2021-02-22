package com.gilbertohdz

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.gilbertohdz.utils.DownloadStatus
import com.gilbertohdz.utils.dpToPx
import com.gilbertohdz.utils.spToPx
import java.util.*
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0
    private var progress = 0
    private val loadingRect = Rect()

    private lateinit var valueAnimator: ValueAnimator

    private val cornerPath = Path()
    private val circleRect = RectF()
    private val cornerRadius = 4.dpToPx().toFloat()

    private var textColor = Color.WHITE
    private var primaryBackgroundColor = context.getColor(R.color.colorPrimary)
    private var secondaryBackgroundColor = context.getColor(R.color.colorPrimaryDark)
    private var circularProgressColor = context.getColor(R.color.colorAccent)


    init {

        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.LoadingButton,
                defStyleAttr,
                0
        ).apply {
            textColor = getColor(
                    R.styleable.LoadingButton_textColor,
                    textColor
            )
            primaryBackgroundColor = getColor(
                    R.styleable.LoadingButton_primaryBackgroundColor,
                    primaryBackgroundColor
            )
            secondaryBackgroundColor = getColor(
                    R.styleable.LoadingButton_secondaryBackgroundColor,
                    secondaryBackgroundColor
            )
            circularProgressColor = getColor(
                    R.styleable.LoadingButton_circularProgressColor,
                    circularProgressColor
            )

            recycle()
        }

        valueAnimator = ValueAnimator.ofInt(0, 100).setDuration(1000).apply {
            addUpdateListener {
                progress = it.animatedValue as Int
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    this@LoadingButton.buttonState = ButtonState.Completed
                }

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    progress = 0
                }

                override fun onAnimationRepeat(animation: Animator?) {
                    super.onAnimationRepeat(animation)
                }
            })
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (buttonState) {
            ButtonState.Clicked -> {
                Log.i("BTN", "clicked")
                valueAnimator.start()
            }
            ButtonState.Loading -> {
                Log.i("BTN", "loading: $progress")
                textToDraw = context.getString(R.string.downloading).toUpperCase(Locale.ENGLISH)
                //valueAnimator.start()
            }
            ButtonState.Completed -> {
                Log.i("BTN", "completed")
                textToDraw = context.getString(R.string.download).toUpperCase(Locale.ENGLISH)
                //valueAnimator.cancel()
            }
        }

        requestLayout()
        invalidate()
    }


    private val textRect = Rect()
    private var textToDraw = context.getString(R.string.download).toUpperCase(Locale.ENGLISH)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.save()

            // Clip canvas corners to form a rounded button
            it.clipPath(cornerPath)

            // Draw button background color
            it.drawColor(primaryBackgroundColor)

            // Retrieve button text bounds
            textPaint.getTextBounds(textToDraw, 0, textToDraw.length, textRect)
            val centerX = measuredWidth.toFloat() / 2 - textRect.centerX()
            val centerY = measuredHeight.toFloat() / 2 - textRect.centerY()

            if (buttonState == ButtonState.Loading) {
                // Draw button loading background color
                textPaint.color = secondaryBackgroundColor
                if (progress == 0) {
                    loadingRect.set(width * progress / 100, 0, width, height)
                } else {
                    loadingRect.set(0, 0, width * progress / 100, height)
                }
                it.drawRect(loadingRect, textPaint)

                // Draw circular progress bar
                textPaint.style = Paint.Style.FILL
                textPaint.color = circularProgressColor
                val circleStartX = (width / 2f + textRect.width() / 2f) + 30
                val circleStartY = height / 2f - 20
                circleRect.set(circleStartX, circleStartY, circleStartX + 40, circleStartY + 40)
                if (progress == 0) {
                    it.drawArc(circleRect, 0f, progress.toFloat(), true, textPaint)
                } else {
                    it.drawArc(
                        circleRect,
                        progress.toFloat(),
                        (progress.toFloat() * 360) / 100,
                        true,
                        textPaint
                    )
                }
            }

            // Draw button text
            textPaint.color = textColor
            it.drawText(textToDraw, centerX, centerY, textPaint)
            Log.i("BTN", "onDraw(), state: ${this.getState()}")

            // Restore saved canvas
            it.restore()
        }
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textAlignment = TEXT_ALIGNMENT_CENTER
        textSize = 16.spToPx().toFloat()
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cornerPath.reset()
        cornerPath.addRoundRect(
                0f,
                0f,
                w.toFloat(),
                h.toFloat(),
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
        )
        cornerPath.close()
    }

    override fun getSuggestedMinimumWidth(): Int {
        textPaint.getTextBounds(textToDraw, 0, textToDraw.length, textRect)
        return textRect.width() - textRect.left + if (buttonState == ButtonState.Loading) 70 else 0
    }

    override fun getSuggestedMinimumHeight(): Int {
        textPaint.getTextBounds(textToDraw, 0, textToDraw.length, textRect)
        return textRect.height()
    }

    fun progress(progress: Int, state: ButtonState) {
        // this.progress = progress
        this.buttonState = state
    }

    fun getState() = buttonState
}