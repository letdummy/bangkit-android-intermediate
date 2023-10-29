package com.sekalisubmit.storymu.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.sekalisubmit.storymu.R

class CButton : AppCompatButton {

    private lateinit var enabledBackground: Drawable
    private lateinit var disabledBackground: Drawable

    private var txtColor: Int = 0

    private var textEnable: String? = null
    private var textDisable: String? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background = if(isEnabled) enabledBackground else disabledBackground

        setTextColor(txtColor)

        textSize = 12f

        gravity = Gravity.CENTER

        text = when {
            isEnabled -> textEnable ?: "SUBMIT"
            else -> textDisable ?: "INVALID INPUT"
        }
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomButton)
            textEnable = typedArray.getString(R.styleable.CustomButton_textEnable)
            textDisable = typedArray.getString(R.styleable.CustomButton_textDisable)
            typedArray.recycle()
        }

        txtColor = ContextCompat.getColor(context, android.R.color.background_light)
        enabledBackground = ContextCompat.getDrawable(context, R.drawable.bg_button) as Drawable
        disabledBackground = ContextCompat.getDrawable(context, R.drawable.bg_button_disable) as Drawable
    }
}