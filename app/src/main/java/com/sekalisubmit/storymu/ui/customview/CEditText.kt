package com.sekalisubmit.storymu.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.sekalisubmit.storymu.R

class CEditText : AppCompatEditText, OnTouchListener{

    private lateinit var clearButtonImage: Drawable

    private val minLength = 8

    private var useFor: String? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    // edit my custom EditText style here
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setHintTextColor(ContextCompat.getColor(context, R.color.light_text))
        setTextColor(ContextCompat.getColor(context, R.color.dark_text))

        backgroundTintList = if (isFocused) {
            ContextCompat.getColorStateList(context, R.color.accent)
        } else {
            ContextCompat.getColorStateList(context, R.color.dark_text)
        }

        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    @SuppressLint("CustomViewStyleable") // idk why this is needed, Android Studio told me to add this
    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText)
            useFor = typedArray.getString(R.styleable.CustomEditText_useFor)
            typedArray.recycle()
        }
        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.custom_xmark) as Drawable

        setOnTouchListener(this)
    }


    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString().isNotEmpty()) showClearButton() else hideClearButton()

        error = when (useFor) {
            "email" -> {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) null else "Invalid email"
            }

            "password" -> {
                if (s.toString().length >= minLength) null else "Password must be at least $minLength characters"
            }

            "username" -> {
                if (s.toString().length >= minLength) null else "Username must be at least $minLength characters"
            }

            else -> {
                null
            }
        }

    }

    private fun showClearButton() {
        setButtonDrawables(endOfTheText = clearButtonImage)
    }

    private fun hideClearButton() {
        setButtonDrawables()
    }

    private fun setButtonDrawables(startOfTheText: Drawable? = null, topOfTheText:Drawable? = null, endOfTheText:Drawable? = null, bottomOfTheText: Drawable? = null){
        setCompoundDrawablesWithIntrinsicBounds(startOfTheText, topOfTheText, endOfTheText, bottomOfTheText)
    }

    // literally from Dicoding
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null) {
            val clearButtonStart: Float
            val clearButtonEnd: Float
            var isClearButtonClicked = false

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                clearButtonEnd = (clearButtonImage.intrinsicWidth + paddingStart).toFloat()
                when {
                    event.x < clearButtonEnd -> isClearButtonClicked = true
                }
            } else {
                clearButtonStart = (width - paddingEnd - clearButtonImage.intrinsicWidth).toFloat()
                when {
                    event.x > clearButtonStart -> isClearButtonClicked = true
                }
            }
            if (isClearButtonClicked) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.custom_xmark) as Drawable
                        showClearButton()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.custom_xmark) as Drawable
                        when {
                            text != null -> text?.clear()
                        }
                        hideClearButton()
                        return true
                    }
                    else -> return false
                }
            } else return false
        }
        return false
    }
}