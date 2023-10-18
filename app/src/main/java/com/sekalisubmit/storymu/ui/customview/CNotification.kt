package com.sekalisubmit.storymu.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColor
import com.sekalisubmit.storymu.R

class CNotification: ConstraintLayout {

    private var nfUsage: String? = null
    private var nfType: String? = null

    private lateinit var nfIcon: ImageView
    private lateinit var nfText: TextView
    private lateinit var nfColor: Color

    constructor(context: Context): super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.notification_handler, this, true)

        nfIcon = findViewById(R.id.notification_image)
        nfText = findViewById(R.id.notification_text)

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomNotification)
            nfType = typedArray.getString(R.styleable.CustomNotification_nfType)
            nfUsage = typedArray.getString(R.styleable.CustomNotification_nfUsage)
            typedArray.recycle()
        }

        setNotification(nfUsage, nfType)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setNotification(nfUsage: String?, nfType: String? = "success"){
        val notification = findViewById<ConstraintLayout>(R.id.notification_container)
        // usage: "error" or "success"
        // type for "success":  you can assign "login" or "register",
        // type for "error": you can assign "emailTaken", "invalidEmail", or "invalidPassword"
        when (nfUsage){
            "error" -> {
                nfColor = context.getColor(R.color.error).toColor()
                nfIcon.setImageResource(R.drawable.ic_error)
                notification.setBackgroundColor(nfColor.toArgb())
                when (nfType){
                    "emailTaken" -> {
                        nfText.text = context.getString(R.string.ne_emailTaken)
                        notification.contentDescription = context.getString(R.string.ne_emailTaken)
                    }

                    "invalidEmail" -> {
                        nfText.text = context.getString(R.string.ne_emailInvalid)
                        notification.contentDescription = context.getString(R.string.ne_emailInvalid)
                    }

                    "invalidPassword" -> {
                        nfText.text = context.getString(R.string.ne_passwordInvalid)
                        notification.contentDescription = context.getString(R.string.ne_passwordInvalid)
                    }
                }
            }

            "success" -> {
                nfColor = context.getColor(R.color.success).toColor()
                nfIcon.setImageResource(R.drawable.ic_success)
                notification.setBackgroundColor(nfColor.toArgb())
                when (nfType){
                    "login" -> {
                        nfText.text = context.getString(R.string.ns_login)
                        notification.contentDescription = context.getString(R.string.ns_login)
                    }

                    "register" -> {
                        nfText.text = context.getString(R.string.ns_register)
                        notification.contentDescription = context.getString(R.string.ns_register)
                    }
                }
            }
        }
    }

    fun notificationSetter(nfUsage: String?, nfType: String?){
        setNotification(nfUsage, nfType)
    }
}