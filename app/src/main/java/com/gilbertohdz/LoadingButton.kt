package com.gilbertohdz

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.gilbertohdz.utils.dpToPx
import com.gilbertohdz.utils.spToPx
import java.util.*
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator = ValueAnimator()

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
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

        when (buttonState) {
            ButtonState.Clicked -> {

            }
            ButtonState.Loading -> {

            }
            ButtonState.Completed -> {

            }
        }
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

            // Draw button text
            textPaint.color = textColor
            it.drawText(textToDraw, centerX, centerY, textPaint)

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
}