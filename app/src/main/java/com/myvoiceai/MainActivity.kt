
package com.myvoiceai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.widget.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var textInput: EditText
    private lateinit var recordButton: Button
    private lateinit var playButton: Button

    private var recorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingFile: File? = null
    private var selectedAudioPath: String? = null
    private var selectedAudioUri: Uri? = null

    private var isRecording = false

    companion object {
        private const val RECORD_PERMISSION = 101
        private const val PICK_AUDIO = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createScreen()
    }

    private fun createScreen() {

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

        recordButton = Button(this).apply {
            text = "RECORD YOUR VOICE"
            setOnClickListener {
                if (isRecording) {
                    stopRecording()
                } else {
                    checkRecordingPermission()
                }
            }
        }

        val uploadButton = Button(this).apply {
            text = "UPLOAD VOICE"
            setOnClickListener {
                chooseAudio()
            }
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
            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(15, 15, 15, 15)
            setBackgroundColor(Color.rgb(35, 43, 65))
        }

        val generateButton = Button(this).apply {
            text = "✨ GENERATE MY VOICE"

            setOnClickListener {
                if (textInput.text.toString().trim().isEmpty()) {
                    textInput.error = "Please enter your text"
                } else if (
                    selectedAudioPath == null &&
                    selectedAudioUri == null
                ) {
                    status.text =
                        "Please record or select a voice sample first."
                } else {
                    status.text =
                        "Voice sample ready. AI cloning engine is not connected yet."
                }
            }
        }

        playButton = Button(this).apply {
            text = "▶ PLAY VOICE SAMPLE"
            isEnabled = false

            setOnClickListener {
                playVoice()
            }
        }

        val downloadButton = Button(this).apply {
            text = "⬇ DOWNLOAD MP3 (COMING SOON)"
            isEnabled = false
        }

        status = TextView(this).apply {
            text = "Welcome! Record your voice to begin."
            textSize = 15f
            setTextColor(Color.rgb(100, 220, 160))
            gravity = Gravity.CENTER
            setPadding(8, 15, 8, 15)
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

    private fun checkRecordingPermission() {

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_PERMISSION
            )
        } else {
            startRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == RECORD_PERMISSION) {
            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startRecording()
            } else {
                status.text =
                    "Microphone permission is required."
            }
        }
    }

    private fun startRecording() {

        try {

            stopPlayer()

            val folder = getExternalFilesDir(
                Environment.DIRECTORY_MUSIC
            ) ?: cacheDir

            if (!folder.exists()) {
                folder.mkdirs()
            }

            val time = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(Date())

            recordingFile = File(
                folder,
                "MyVoice_$time.m4a"
            )

            val newRecorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(this)
                } else {
                    MediaRecorder()
                }

            recorder = newRecorder

            newRecorder.setAudioSource(
                MediaRecorder.AudioSource.MIC
            )

            newRecorder.setOutputFormat(
                MediaRecorder.OutputFormat.MPEG_4
            )

            newRecorder.setAudioEncoder(
                MediaRecorder.AudioEncoder.AAC
            )

            newRecorder.setAudioEncodingBitRate(128000)
            newRecorder.setAudioSamplingRate(44100)

            newRecorder.setOutputFile(
                recordingFile!!.absolutePath
            )

            newRecorder.prepare()
            newRecorder.start()

            isRecording = true

            recordButton.text = "■ STOP RECORDING"

            status.text =
                "Recording your voice... Speak clearly."

        } catch (e: Exception) {

            releaseRecorder()

            recordingFile?.delete()

            isRecording = false

            recordButton.text = "RECORD YOUR VOICE"

            status.text =
                "Recording failed: ${e.message}"

        }
    }

    private fun stopRecording() {

        try {

            recorder?.stop()

            selectedAudioPath =
                recordingFile?.absolutePath

            selectedAudioUri = null

            playButton.isEnabled =
                selectedAudioPath != null

            status.text =
                "Voice recorded successfully! You can play your sample."

        } catch (e: Exception) {

            recordingFile?.delete()

            selectedAudioPath = null

            status.text =
                "Recording was too short. Please record again."

        } finally {

            releaseRecorder()

            isRecording = false

            recordButton.text = "RECORD YOUR VOICE"

        }
    }

    private fun releaseRecorder() {

        try {
            recorder?.reset()
        } catch (_: Exception) {
        }

        try {
            recorder?.release()
        } catch (_: Exception) {
        }

        recorder = null
    }

    private fun chooseAudio() {

        try {

            val intent = Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }

            startActivityForResult(
                intent,
                PICK_AUDIO
            )

        } catch (e: Exception) {

            status.text =
                "Unable to open audio files."

        }
    }

    @Deprecated("Using activity result callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == PICK_AUDIO &&
            resultCode == RESULT_OK
        ) {

            val uri = data?.data

            if (uri != null) {

                selectedAudioUri = uri
                selectedAudioPath = null

                playButton.isEnabled = true

                status.text =
                    "Voice file selected successfully! Ready for preview."

            }
        }
    }

    private fun playVoice() {

        try {

            stopPlayer()

            val player = MediaPlayer()

            mediaPlayer = player

            player.setOnCompletionListener {
                it.release()

                if (mediaPlayer === it) {
                    mediaPlayer = null
                }

                status.text = "Voice playback completed."
            }

            if (selectedAudioUri != null) {

                player.setDataSource(
                    this,
                    selectedAudioUri!!
                )

            } else if (selectedAudioPath != null) {

                player.setDataSource(
                    selectedAudioPath!!
                )

            } else {

                status.text = "No voice sample selected."
                player.release()
                mediaPlayer = null
                return
            }

            player.prepare()
            player.start()

            status.text = "Playing your voice sample..."

        } catch (e: Exception) {

            stopPlayer()

            status.text =
                "Unable to play audio: ${e.message}"

        }
    }

    private fun stopPlayer() {

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }

        mediaPlayer = null
    }

    override fun onDestroy() {

        if (isRecording) {
            try {
                recorder?.stop()
            } catch (_: Exception) {
            }

            releaseRecorder()
        }

        stopPlayer()

        super.onDestroy()
    }
}
