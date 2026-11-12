
package com.myvoiceai

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var textInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 35, 24, 24)
            setBackgroundColor(Color.rgb(12, 17, 35))
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "MY VOICE AI"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Your Voice, Your AI"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        val voiceTitle = TextView(this).apply {
            text = "\n🎙 YOUR VOICE"
            textSize = 22f
            setTextColor(Color.WHITE)
        }

        val recordButton = Button(this).apply {
            text = "RECORD YOUR VOICE"
        }

        val uploadButton = Button(this).apply {
            text = "UPLOAD VOICE"
        }

        val languageTitle = TextView(this).apply {
            text = "\nSELECT LANGUAGE"
            textSize = 18f
            setTextColor(Color.WHITE)
        }

        val language = Spinner(this)
        val languages = arrayOf(
            "বাংলা (Bengali)",
            "Hindi",
            "English"
        )

        language.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languages
        )

        val textTitle = TextView(this).apply {
            text = "\nENTER YOUR TEXT"
            textSize = 18f
            setTextColor(Color.WHITE)
        }

        textInput = EditText(this).apply {
            hint = "এখানে আপনার লেখা লিখুন..."
            textSize = 18f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            minLines = 5
            gravity = Gravity.TOP
            setPadding(15, 15, 15, 15)
            setBackgroundColor(Color.rgb(35, 43, 65))
        }

        val generateButton = Button(this).apply {
            text = "✨ GENERATE MY VOICE"
        }

        val playButton = Button(this).apply {
            text = "▶ PLAY VOICE"
            isEnabled = false
        }

        val downloadButton = Button(this).apply {
            text = "⬇ DOWNLOAD MP3"
            isEnabled = false
        }

        status = TextView(this).apply {
            text = "Welcome! Record your voice to begin."
            textSize = 15f
            setTextColor(Color.rgb(100, 220, 160))
            gravity = Gravity.CENTER
        }

        recordButton.setOnClickListener {
            status.text =
                "Voice recording feature will be connected next."
        }

        uploadButton.setOnClickListener {
            status.text =
                "Voice upload feature will be connected next."
        }

        generateButton.setOnClickListener {
            if (textInput.text.toString().trim().isEmpty()) {
                textInput.error = "Please enter your text"
            } else {
                status.text =
                    "AI Voice Engine is not connected yet."
            }
        }

        content.addView(title)
        content.addView(subtitle)
        content.addView(voiceTitle)
        content.addView(recordButton)
        content.addView(uploadButton)
        content.addView(languageTitle)
        content.addView(language)
        content.addView(textTitle)
        content.addView(textInput)

        content.addView(generateButton)
        content.addView(playButton)
        content.addView(downloadButton)
        content.addView(status)

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1, 0, 1f
            )
        )

        setContentView(root)
    }
}
