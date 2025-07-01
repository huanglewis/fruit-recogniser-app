package iss.nus.edu.sg.fragments.workshop.fruitrecogniserapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var tflite: Interpreter
    private lateinit var currentPhotoPath: String

    private val labels = listOf("apple", "banana", "mixed", "orange")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultText)

        findViewById<Button>(R.id.selectButton).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.captureButton).setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        try {
            tflite = Interpreter(loadModelFile("fruit_classifier_2_float32.tflite"))
        } catch (e: IOException) {
            Toast.makeText(this, "Model failed to load", Toast.LENGTH_LONG).show()
            resultText.text = getString(R.string.no_prediction)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
            classifyImage(bitmap)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            imageView.setImageBitmap(bitmap)
            classifyImage(bitmap)
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 2001)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
        }
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
        cameraLauncher.launch(uri)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_$timestamp"
        val storageDir = cacheDir
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    private fun classifyImage(bitmap: Bitmap) {
        val resized = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
        val inputBuffer = ByteBuffer.allocateDirect(1 * 100 * 100 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(100 * 100)
        resized.getPixels(pixels, 0, 100, 0, 0, 100, 100)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            inputBuffer.putFloat((r - 0.5f) / 0.5f)
            inputBuffer.putFloat((g - 0.5f) / 0.5f)
            inputBuffer.putFloat((b - 0.5f) / 0.5f)
        }

        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 4), org.tensorflow.lite.DataType.FLOAT32)
        tflite.run(inputBuffer.rewind(), outputBuffer.buffer.rewind())

        val outputArray = outputBuffer.floatArray
        val maxIdx = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1

        val result = if (maxIdx != -1) {
            val label = labels[maxIdx]
            val confidence = String.format(Locale.US, "%.2f", outputArray[maxIdx] * 100)
            "Hey! You scanned/selected $label."
        } else {
            getString(R.string.no_prediction)
        }

        resultText.text = result
    }
}
