package ru.`object`.devicecamera

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_object_recognition.*
import org.tensorflow.lite.examples.detection.R
import ru.`object`.devicecamera.camera.CameraPermissionsResolver
import ru.`object`.devicecamera.camera.ObjectDetectorAnalyzer
import ru.`object`.devicecamera.util.view.Scenery
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ObjectRecognitionActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView

    private lateinit var executor: ExecutorService

    private val cameraPermissionsResolver = CameraPermissionsResolver(this)

    private val config = ObjectDetectorAnalyzer.Config(
        0.5f,
        10,
        300,
        true,
        "detect.tflite",
        "labelmap.txt",
        "barcodetextTolink.txt"
    )

    var maintext: TextView? = null
    var reset:Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object_recognition)
        executor = Executors.newSingleThreadExecutor()

        maintext = findViewById(R.id.MainText)
        maintext!!.text = getString(R.string.Find_processor)

        result_overlay.setDescriptionText(findViewById(R.id.DescriptionText))
        result_overlay.setWebView(findViewById(R.id.PDFViewer))

        reset = findViewById(R.id.Reset)


        previewView = findViewById(R.id.preview_view)

        cameraPermissionsResolver.checkAndRequestPermissionsIfNeeded(
                onSuccess = {
                    getProcessCameraProvider(::bindCamera)
                },
                onFail = ::showSnackbar
        )
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }

    private fun bindCamera(cameraProvider: ProcessCameraProvider) {

        val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

        val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        imageAnalysis.setAnalyzer(
                executor,
                ObjectDetectorAnalyzer(applicationContext, config, ::onDetectionResult)
        )

        val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        cameraProvider.unbindAll()

        cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                imageAnalysis,
                preview
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    private fun getProcessCameraProvider(onDone: (ProcessCameraProvider) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
                Runnable { onDone.invoke(cameraProviderFuture.get()) },
                ContextCompat.getMainExecutor(this)
        )
    }

    private fun onDetectionResult(result: ObjectDetectorAnalyzer.Result, barcoderesult: Result?,handbound:Array<IntArray>?,isDark:Boolean,scenery: Scenery):Boolean {
        result_overlay.updateResults(result,barcoderesult,handbound,isDark,scenery)
        return true;
    }

    private fun showSnackbar(message: String) {
       // Snackbar.make(root_container, message, Snackbar.LENGTH_LONG).show()
    }



}