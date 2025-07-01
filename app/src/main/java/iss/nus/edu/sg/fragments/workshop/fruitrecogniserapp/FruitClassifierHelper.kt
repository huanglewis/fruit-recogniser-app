import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageClassifierHelper(context: Context) {

    private val model: Interpreter
    private val imageSize = 100  // your model input size
    private val labels = listOf("apple", "banana", "mixed", "orange")

    init {
        val modelAsset = context.assets.open("fruit_classifier_2_float32.tflite").readBytes()
        model = Interpreter(ByteBuffer.wrap(modelAsset))
    }

    fun classify(bitmap: Bitmap): String {
        val resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
        val input = preprocessImage(resized)
        val output = Array(1) { FloatArray(labels.size) }
        model.run(input.buffer, output)
        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        return "${labels[maxIdx]} (${(output[0][maxIdx] * 100).toInt()}%)"
    }

    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        val image = TensorImage.fromBitmap(bitmap)
        image.buffer.rewind()
        return image
    }
}
