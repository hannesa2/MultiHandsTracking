package info.mediapipe.app.multihand

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Size
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.components.CameraHelper.CameraFacing
import com.google.mediapipe.components.CameraXPreviewHelper
import com.google.mediapipe.components.ExternalTextureConverter
import com.google.mediapipe.components.FrameProcessor
import com.google.mediapipe.components.PermissionHelper
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.glutil.EglManager
import info.mediapipe.app.R
import info.mediapipe.app.landmarksDebugString
import timber.log.Timber

class MultiHandActivity : AppCompatActivity() {

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private var previewFrameTexture: SurfaceTexture? = null

    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private var previewDisplayView: SurfaceView? = null

    private var eglManager: EglManager? = null

    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed frames onto a {@link Surface}.
    private lateinit var processor: FrameProcessor

    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private var converter: ExternalTextureConverter? = null

    // Handles camera access via the {@link CameraX} Jetpack support library.
    private var cameraHelper: CameraXPreviewHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multihand)
        previewDisplayView = SurfaceView(this)
        setupPreviewDisplayView()
        // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g., binary graphs.
        AndroidAssetUtil.initializeNativeAssetManager(this)
        eglManager = EglManager(null)
        processor = FrameProcessor(
                this,
                eglManager!!.nativeContext,
                BINARY_GRAPH_NAME,
                INPUT_VIDEO_STREAM_NAME,
                OUTPUT_VIDEO_STREAM_NAME)
        processor.videoSurfaceOutput.setFlipY(FLIP_FRAMES_VERTICALLY)
        processor.addPacketCallback(OUTPUT_LANDMARKS_STREAM_NAME) { packet: Packet ->
            val timeDelay = System.currentTimeMillis() - packet.timestamp
            val multiHandLandmarks = PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser())
            if (multiHandLandmarks.isNotEmpty())
                Timber.d("[delay: ${timeDelay}] ${multiHandLandmarks.landmarksDebugString()})")
        }
        PermissionHelper.checkAndRequestCameraPermissions(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        converter = ExternalTextureConverter(eglManager!!.context)
        converter?.setFlipY(FLIP_FRAMES_VERTICALLY)
        converter?.setConsumer(processor)
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        converter?.close()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupPreviewDisplayView() {
        previewDisplayView!!.visibility = View.GONE
        val viewGroup = findViewById<ViewGroup>(R.id.preview_display_layout)
        viewGroup.addView(previewDisplayView)
        previewDisplayView!!.holder
                .addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        processor.videoSurfaceOutput.setSurface(holder.surface)
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                        // (Re-)Compute the ideal size of the camera-preview display (the area that the
                        // camera-preview frames get rendered onto, potentially with scaling and rotation)
                        // based on the size of the SurfaceView that contains the display.
                        val viewSize = Size(width, height)
                        val displaySize = cameraHelper!!.computeDisplaySizeFromViewSize(viewSize)
                        // Connect the converter to the camera-preview frames as its input (via
                        // previewFrameTexture), and configure the output width and height as the computed
                        // display size.
                        converter?.setSurfaceTextureAndAttachToGLContext(previewFrameTexture, displaySize.width, displaySize.height)
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        processor.videoSurfaceOutput.setSurface(null)
                    }
                })
    }

    private fun startCamera() {
        cameraHelper = CameraXPreviewHelper()
        cameraHelper!!.setOnCameraStartedListener { surfaceTexture: SurfaceTexture? ->
            previewFrameTexture = surfaceTexture
            // Make the display view visible to start showing the preview. This triggers the
            // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
            previewDisplayView!!.visibility = View.VISIBLE
        }
        cameraHelper!!.startCamera(this, CAMERA_FACING,  /*surfaceTexture=*/null)
    }

    companion object {
        private const val BINARY_GRAPH_NAME = "multi_hand_tracking_mobile_gpu.binarypb"
        private const val INPUT_VIDEO_STREAM_NAME = "input_video"
        private const val OUTPUT_VIDEO_STREAM_NAME = "output_video"
        private const val OUTPUT_LANDMARKS_STREAM_NAME = "multi_hand_landmarks"
        private val CAMERA_FACING = CameraFacing.FRONT

        // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
        // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
        // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
        // corner, whereas MediaPipe in general assumes the image origin is at top-left.
        private const val FLIP_FRAMES_VERTICALLY = true

        init {
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
        }
    }
}