package com.sekalisubmit.storymu.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.data.remote.retrofit.ApiConfig
import com.sekalisubmit.storymu.databinding.ActivityCreateBinding
import com.sekalisubmit.storymu.ui.activity.CameraXActivity.Companion.CAMERAX_RESULT
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding

    private var currentImageUri: Uri? = null

    private lateinit var pref: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
        binding.buttonAdd.isEnabled = false
    }

    private fun setup() {
        binding.apply {
            if (!allPermissionsGranted()) {
                requestPermissionLauncher.launch(REQUIRED_PERMISSION)
            }

            galleryButton.setOnClickListener { startGallery() }
            cameraXButton.setOnClickListener { startCameraX() }
            buttonAdd.setOnClickListener {
                if (isInternetConnected()) {
                    handleSubmission()
                } else {
                    Toast.makeText(this@CreateActivity, "Internet Needed to Post Story", Toast.LENGTH_SHORT).show()
                }
            }

            titleStory.editText?.doOnTextChanged { text, _, _, _ -> validateTextCount(text, titleStory) }
            edAddDescription.editText?.doOnTextChanged { text, _, _, _ -> validateTextCount(text, edAddDescription) }

            // documentation:
            // https://github.com/material-components/material-components-android/blob/master/docs/components/TextField.md
        }
    }

    private fun validateTextCount(text: CharSequence?, textInputLayout: TextInputLayout) {
        val wordCount = text?.trim()?.split(" ")?.size ?: 0
        if (wordCount < 3) {
            textInputLayout.error = "${textInputLayout.hint} must be at least 3 words"
            binding.buttonAdd.isEnabled = false
        } else {
            textInputLayout.error = null
            binding.buttonAdd.isEnabled = true
        }
    }

    private fun handleSubmission() {
        binding.loadingHandler.visibility = View.VISIBLE
        val combinedText = "${binding.titleStory.editText?.text} ${binding.edAddDescription.editText?.text}"
        val file = currentImageUri?.let { uriToFile(it, this).reduceFileImage() }
        val requestFile = file?.asRequestBody("image/jpeg".toMediaType())

        if (requestFile != null) {
            lifecycleScope.launch {
                try {
                    pref = UserPreference.getInstance(this@CreateActivity.dataStore)
                    val token = pref.getToken().first()
                    val apiService = ApiConfig.getApiService(token)

                    val response = apiService.postStory(
                        combinedText.toRequestBody("text/plain".toMediaType()),
                        MultipartBody.Part.createFormData("photo", file.name, requestFile),
                        null,
                        null
                    )

                    Log.d("CreateActivity", "handleSubmission: ${response.message}")
                    binding.loadingHandler.visibility = View.GONE
                    Toast.makeText(this@CreateActivity, "Story created", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("CreateActivity", "handleSubmission: ${e.message}", e)
                    Toast.makeText(this@CreateActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }

                delay(3000)
                // lol instead of making intent to go back to main activity, I just finish this activity
                finish()
            }
        } else {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show()
        }
    }


    // File handler (literally from dicoding)
    private fun uriToFile(imageUri: Uri, context: Context): File {
        val myFile = createCustomTempFile(context)
        val inputStream = context.contentResolver.openInputStream(imageUri) as InputStream
        val outputStream = FileOutputStream(myFile)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) outputStream.write(buffer, 0, length)
        outputStream.close()
        inputStream.close()
        return myFile
    }


    // Gallery handler
    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            currentImageUri = it
            showImage()
        } ?: Log.d("Photo Picker", "No media selected")
    }


    // CameraX handler
    private fun startCameraX() {
        val intent = Intent(this, CameraXActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == CAMERAX_RESULT) {
            val uriString = result.data?.getStringExtra(CameraXActivity.EXTRA_CAMERAX_IMAGE)
            currentImageUri = uriString?.toUri()
            showImage()
        }
    }


    // Image handler
    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.imgStory.setImageURI(it)
            binding.imgStory.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    // Permission handler
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        return activeNetwork != null
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}