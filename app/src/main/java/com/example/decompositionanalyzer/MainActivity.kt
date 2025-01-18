package com.example.decompositionanalyzer

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import com.example.decompositionanalyzer.databinding.ActivityMainBinding
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private var imageUri: Uri? = null
    private val materialDatabase = MaterialDatabase()

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let { uri ->
                binding.imagePreview.setImageURI(uri)
                analyzeImage(uri)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            binding.imagePreview.setImageURI(it)
            analyzeImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            cameraButton.setOnClickListener {
                checkCameraPermission()
            }

            galleryButton.setOnClickListener {
                galleryLauncher.launch("image/*")
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val uri = createImageUri()
        imageUri = uri
        cameraLauncher.launch(uri)
    }

    private fun analyzeImage(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

            // Show progress bar and hide result card
            binding.progressBar.visibility = View.VISIBLE
            binding.resultCard.visibility = View.GONE

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val material = materialDatabase.findMaterialFromLabels(labels)
                    displayResults(material)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Analysis failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayResults(material: Material?) {
        binding.progressBar.visibility = View.GONE // Hide progress bar

        if (material == null) {
            Toast.makeText(this, "Could not identify material", Toast.LENGTH_SHORT).show()
            return
        }

        // Show result card
        binding.resultCard.visibility = View.VISIBLE
        binding.apply {
            materialName.text = material.name
            decompositionTime.text = "Decomposition Time: ${material.decompositionTime}"
            environmentalImpact.text = "Environmental Impact: ${material.impact}"
            alternatives.text = "Alternatives: ${material.alternatives}"
        }
    }

    private fun createImageUri(): Uri? {
        // Create a file to store the image
        val imageFileName = "JPEG_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + "_"
        val storageDir = getExternalFilesDir(null)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        // Get the URI for the image file
        return try {
            FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                imageFile
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}