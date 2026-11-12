
package com.myvoiceai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.text.InputType
import android.provider.OpenableColumns
import android.view.Gravity
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var textInput: EditText
    private lateinit var recordButton: Button
    private lateinit var playButton: Button
    private lateinit var savedVoiceSpinner: Spinner

    private var recorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingFile: File? = null
    private var selectedAudioPath: String? = null
    private var isRecording = false
    private var savedVoiceFiles: List<File> = emptyList()
    private var spinnerReady = false

    companion object {
        private const val RECORD_PERMISSION = 101
        private const val PICK_AUDIO = 102
        private const val PREFS = "my_voice_ai_prefs"
        private const val SELECTED_VOICE = "selected_voice"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createScreen()
        refreshSavedVoices()
    }

    private fun voiceFolder(): File {
        val folder = File(filesDir, "saved_voices")
        if (!folder.exists()) folder.mkdirs()
        return folder
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
                if (isRecording) stopRecording() else checkRecordingPermission()
            }
        }

        val uploadButton = Button(this).apply {
            text = "UPLOAD VOICE"
            setOnClickListener { chooseAudio() }
        }

        val savedTitle = TextView(this).apply {
            text = "\nSAVED VOICES"
            textSize = 18f
            setTextColor(Color.WHITE)
        }

        savedVoiceSpinner = Spinner(this)

        val languageTitle = TextView(this).apply {
            text = "\nSELECT LANGUAGE"
            textSize = 18f
            setTextColor(Color.WHITE)
        }

        val language = Spinner(this)
        val languages = arrayOf("বাংলা (Bengali)", "Hindi", "English")
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
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(15, 15, 15, 15)
            setBackgroundColor(Color.rgb(35, 43, 65))
        }

        val generateButton = Button(this).apply {
            text = "✨ GENERATE MY VOICE"
            setOnClickListener {
                if (textInput.text.toString().trim().isEmpty()) {
                    textInput.error = "Please enter your text"
                } else if (selectedAudioPath == null) {
                    status.text = "Please record or upload a voice sample first."
                } else {
                    status.text = "Voice sample ready. AI cloning engine is not connected yet."
                }
            }
        }

        playButton = Button(this).apply {
            text = "▶ PLAY VOICE SAMPLE"
            isEnabled = false
            setOnClickListener { playVoice() }
        }

        val downloadButton = Button(this).apply {
            text = "⬇ DOWNLOAD MP3 (COMING SOON)"
            isEnabled = false
        }

        status = TextView(this).apply {
            text = "Welcome! Record or upload your voice to begin."
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
        content.addView(savedTitle)
        content.addView(savedVoiceSpinner)
        content.addView(languageTitle)
        content.addView(language)
        content.addView(textTitle)
        content.addView(textInput)
        content.addView(generateButton)
        content.addView(playButton)
        content.addView(downloadButton)
        content.addView(status)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        savedVoiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                if (!spinnerReady || savedVoiceFiles.isEmpty()) return

                val file = savedVoiceFiles.getOrNull(position) ?: return
                selectedAudioPath = file.absolutePath

                getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit().putString(SELECTED_VOICE, file.name).apply()

                stopPlayer()
                playButton.isEnabled = file.exists()
                playButton.text = "▶ PLAY VOICE SAMPLE"
                status.text = "Saved voice selected: ${file.name}"
            }
        }
    }

    private fun refreshSavedVoices(preferredName: String? = null) {
        val folder = voiceFolder()

        savedVoiceFiles = folder.listFiles()
            ?.filter { it.isFile && it.length() > 0L }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        val names = if (savedVoiceFiles.isEmpty()) {
            listOf("No saved voice yet")
        } else {
            savedVoiceFiles.map { it.name }
        }

        spinnerReady = false

        savedVoiceSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            names
        )

        if (savedVoiceFiles.isEmpty()) {
            selectedAudioPath = null
            playButton.isEnabled = false
            spinnerReady = true
            return
        }

        val wanted = preferredName
            ?: getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(SELECTED_VOICE, null)

        val index = savedVoiceFiles.indexOfFirst { it.name == wanted }.let {
            if (it >= 0) it else 0
        }

        savedVoiceSpinner.setSelection(index, false)
        selectedAudioPath = savedVoiceFiles[index].absolutePath

        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit().putString(SELECTED_VOICE, savedVoiceFiles[index].name).apply()

        playButton.isEnabled = true
        spinnerReady = true
    }

    private fun checkRecordingPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_PERMISSION)
        } else {
            startRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                status.text = "Microphone permission is required."
            }
        }
    }

    private fun startRecording() {
        try {
            stopPlayer()

            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            recordingFile = File(voiceFolder(), "MyVoice_$time.m4a")

            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }

            recorder = newRecorder
            newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            newRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            newRecorder.setAudioEncodingBitRate(128000)
            newRecorder.setAudioSamplingRate(44100)
            newRecorder.setOutputFile(recordingFile!!.absolutePath)

            newRecorder.prepare()
            newRecorder.start()

            isRecording = true
            recordButton.text = "■ STOP RECORDING"
            status.text = "Recording your voice... Speak clearly."

        } catch (e: Exception) {
            releaseRecorder()
            recordingFile?.delete()
            isRecording = false
            recordButton.text = "RECORD YOUR VOICE"
            status.text = "Recording failed: ${e.message}"
        }
    }

    private fun stopRecording() {
        try {
            recorder?.stop()

            val file = recordingFile

            if (file != null && file.exists() && file.length() > 0L) {
                selectedAudioPath = file.absolutePath

                getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit().putString(SELECTED_VOICE, file.name).apply()

                refreshSavedVoices(file.name)
                status.text = "Voice recorded and saved successfully!"
            } else {
                status.text = "Recording file was not created. Please record again."
            }

        } catch (e: Exception) {
            recordingFile?.delete()
            selectedAudioPath = null
            status.text = "Recording was too short. Please record again."

        } finally {
            releaseRecorder()
            isRecording = false
            recordButton.text = "RECORD YOUR VOICE"
        }
    }

    private fun releaseRecorder() {
        try { recorder?.reset() } catch (_: Exception) {}
        try { recorder?.release() } catch (_: Exception) {}
        recorder = null
    }

    private fun chooseAudio() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }

            startActivityForResult(intent, PICK_AUDIO)

        } catch (e: Exception) {
            status.text = "Unable to open audio files."
        }
    }

    private fun getOriginalFileName(uri: Uri): String {
        var name = "UploadedVoice"
        var cursor: Cursor? = null

        try {
            cursor = contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) name = cursor.getString(index) ?: name
            }

        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }

        return name
    }

    private fun safeFileName(name: String): String {
        val cleaned = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
            .trim('_', '.')

        return cleaned.ifBlank { "UploadedVoice" }
    }

    private fun saveUploadedAudio(uri: Uri) {
        try {
            val originalName = safeFileName(getOriginalFileName(uri))

            val extension = originalName.substringAfterLast('.', "audio").let {
                if (it.length in 2..6) it else "audio"
            }

            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

            val destination = File(
                voiceFolder(),
                "${originalName.substringBeforeLast('.', originalName)}_$time.$extension"
            )

            val input = contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open selected audio")

            input.use { source ->
                FileOutputStream(destination).use { output ->
                    source.copyTo(output)
                    output.flush()
                }
            }

            if (!destination.exists() || destination.length() == 0L) {
                destination.delete()
                throw IllegalStateException("Selected audio is empty")
            }

            stopPlayer()
            selectedAudioPath = destination.absolutePath

            getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit().putString(SELECTED_VOICE, destination.name).apply()

            refreshSavedVoices(destination.name)

            playButton.isEnabled = true
            playButton.text = "▶ PLAY VOICE SAMPLE"

            status.text = "Uploaded voice saved successfully: ${destination.name}"

        } catch (e: Exception) {
            status.text = "Could not save uploaded voice: ${e.message}"
        }
    }

    @Deprecated("Using activity result callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO && resultCode == RESULT_OK) {
            val uri = data?.data

            if (uri != null) {
                saveUploadedAudio(uri)
            }
        }
    }

    private fun playVoice() {
        val currentPlayer = mediaPlayer

        if (currentPlayer != null) {
            stopPlayer()
            playButton.text = "▶ PLAY VOICE SAMPLE"
            status.text = "Voice playback stopped."
            return
        }

        val path = selectedAudioPath

        if (path == null || !File(path).exists()) {
            status.text = "Please select or upload a voice sample first."
            playButton.isEnabled = false
            return
        }

        try {
            val player = MediaPlayer()
            mediaPlayer = player

            player.setOnCompletionListener {
                it.release()

                if (mediaPlayer === it) {
                    mediaPlayer = null
                }

                playButton.text = "▶ PLAY VOICE SAMPLE"
                status.text = "Voice playback completed."
            }

            player.setDataSource(path)
            player.prepare()
            player.start()

            playButton.text = "■ STOP VOICE"
            status.text = "Playing your voice sample..."

        } catch (e: Exception) {
            stopPlayer()
            playButton.text = "▶ PLAY VOICE SAMPLE"
            status.text = "Unable to play audio: ${e.message}"
        }
    }

    private fun stopPlayer() {
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        try { mediaPlayer?.release() } catch (_: Exception) {}

        mediaPlayer = null

        if (::playButton.isInitialized) {
            playButton.text = "▶ PLAY VOICE SAMPLE"
        }
    }

    override fun onDestroy() {
        if (isRecording) {
            try { recorder?.stop() } catch (_: Exception) {}
            releaseRecorder()
        }

        stopPlayer()
        super.onDestroy()
    }
}
