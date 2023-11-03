package com.sekalisubmit.storymu.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputLayout
import com.sekalisubmit.storymu.R
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.data.remote.PostStory
import com.sekalisubmit.storymu.databinding.ActivityCreateBinding
import com.sekalisubmit.storymu.ui.activity.CameraXActivity.Companion.CAMERAX_RESULT
import com.sekalisubmit.storymu.ui.viewmodel.CreateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding

    private var currentImageUri: Uri? = null

    private val pref: UserPreference by lazy {
        UserPreference.getInstance(dataStore)
    }
    private val viewModel: CreateViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var myLoc: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
        binding.buttonAdd.isEnabled = false
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun setup() {
        binding.apply {
            if (!allPermissionsGranted()) {
                requestPermissionLauncher.launch(REQUIRED_PERMISSION)
            }

            galleryButton.setOnClickListener { startGallery() }
            cameraXButton.setOnClickListener { startCameraX() }

            titleStory.editText?.doOnTextChanged { text, _, _, _ -> validateTextCount(text, titleStory) }
            edAddDescription.editText?.doOnTextChanged { text, _, _, _ -> validateTextCount(text, edAddDescription) }

            switchLoc.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    getMyLastLocation()
                } else {
                    myLoc = null
                }
            }

            buttonAdd.setOnClickListener {
                if (isInternetConnected()) {
                    handleSubmission()
                } else {
                    Toast.makeText(this@CreateActivity,
                        getString(R.string.err_internet), Toast.LENGTH_SHORT).show()
                }
            }
            // documentation:
            // https://github.com/material-components/material-components-android/blob/master/docs/components/TextField.md
        }
    }

    private fun validateTextCount(text: CharSequence?, textInputLayout: TextInputLayout) {
        val wordCount = text?.trim()?.split(" ")?.size ?: 0
        Log.d("CreateActivity", "hint: ${textInputLayout.hint}")
        if (wordCount < 3) {
            textInputLayout.error = getString(R.string.err_create, textInputLayout.hint)
            binding.buttonAdd.isEnabled = false
        } else {
            textInputLayout.error = null
            binding.buttonAdd.isEnabled = true
        }

        if (textInputLayout.hint == getString(R.string.story_title)) {
            if ((text?.length ?: 0) > 25) {
                textInputLayout.error = getString(R.string.err_title_max, textInputLayout.hint)
                binding.buttonAdd.isEnabled = false
            }
        } else {
            if ((text?.length ?: 0) > 100) {
                textInputLayout.error = getString(R.string.err_desc_max, textInputLayout.hint)
                binding.buttonAdd.isEnabled = false
            }
        }
    }

    private fun handleSubmission() {
        binding.loadingHandler.visibility = View.VISIBLE
        val combinedText = "${binding.titleStory.editText?.text} ${binding.edAddDescription.editText?.text}"
        val file = currentImageUri?.let { uriToFile(it, this).reduceFileImage() }
        val requestFile = file?.asRequestBody("image/jpeg".toMediaType())

        val postStory = requestFile?.let {
            PostStory(
                combinedText,
                MultipartBody.Part.createFormData("photo", file.name, requestFile),
                myLoc?.latitude ?: 0.0,
                myLoc?.longitude ?: 0.0
            )
        }

        if (requestFile != null) {
            viewModel.submitStory(pref, postStory)
        } else {
            Toast.makeText(this, getString(R.string.err_media), Toast.LENGTH_SHORT).show()
            binding.loadingHandler.visibility = View.GONE
        }

        viewModel.submissionStatus.observe(this) { status ->
            lifecycleScope.launch {
                if (status) {
                    Toast.makeText(this@CreateActivity, getString(R.string.story_true), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CreateActivity, getString(R.string.story_false), Toast.LENGTH_SHORT).show()
                }
                delay(2000)
                binding.loadingHandler.visibility = View.GONE
                finish()
            }
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
                Toast.makeText(this, getString(R.string.permission_true), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.permission_false), Toast.LENGTH_LONG).show()
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

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ){
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    myLoc = location
                } else {
                    Toast.makeText(
                        this@CreateActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestLoc.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private val requestLoc =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                else -> {

                }
            }
        }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}