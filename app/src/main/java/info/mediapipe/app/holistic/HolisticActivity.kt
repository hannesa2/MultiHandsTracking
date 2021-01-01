package info.mediapipe.app.holistic

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
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

class HolisticActivity : AppCompatActivity() {

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

    // ApplicationInfo for retrieving metadata defined in the manifest.
    private var applicationInfoX: ApplicationInfo? = null

    // Handles camera access via the {@link CameraX} Jetpack support library.
    private var cameraHelper: CameraXPreviewHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewLayoutResId)
        try {
            applicationInfoX = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e("Cannot find application info: $e")
        }
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
            OUTPUT_VIDEO_STREAM_NAME
        )
        processor.videoSurfaceOutput?.setFlipY(FLIP_FRAMES_VERTICALLY)
        PermissionHelper.checkAndRequestCameraPermissions(this)

//        val packetCreator = processor.packetCreator
//        val inputSidePackets: MutableMap<String, Packet> = HashMap()
//        Timber.d("inputSidePackets0=${inputSidePackets.size}")
//        inputSidePackets[INPUT_NUM_HANDS_SIDE_PACKET_NAME] = packetCreator.createInt32(NUM_HANDS)
//        Timber.d("inputSidePackets1=${inputSidePackets.size}")
//        processor.setInputSidePackets(inputSidePackets)

        // To show verbose logging, run:
        // adb shell setprop log.tag.MainActivity VERBOSE
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            processor.addPacketCallback(OUTPUT_LANDMARKS_STREAM_NAME) { packet: Packet ->
                Timber.v("Received Holistic landmarks packet.")
                val timeDelay = System.currentTimeMillis() - packet.timestamp
                val multiHolisticLandmarks = PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser())
                if (multiHolisticLandmarks.isNotEmpty())
                    Timber.d("[delay: ${timeDelay}] ${multiHolisticLandmarks.landmarksDebugString()})")
            }
        }
    }

    // Used to obtain the content view for this application. If you are extending this class, and
    // have a custom layout, override this method and return the custom layout.
    @Suppress("ProtectedInFinal")
    protected val contentViewLayoutResId: Int
        get() = R.layout.activity_multihand

    override fun onResume() {
        super.onResume()
        converter = ExternalTextureConverter(eglManager!!.context, 2)
        converter?.setFlipY(FLIP_FRAMES_VERTICALLY)
        converter?.setConsumer(processor)
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        converter?.close()

        // Hide preview display until we re-open the camera again.
        previewDisplayView!!.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupPreviewDisplayView() {
        previewDisplayView!!.visibility = View.GONE
        val viewGroup = findViewById<ViewGroup>(R.id.preview_display_layout)
        viewGroup.addView(previewDisplayView)
        previewDisplayView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Timber.v("previewDisplayView created")
                processor.videoSurfaceOutput.setSurface(holder.surface)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                Timber.v("previewDisplayView changes")
                onPreviewDisplaySurfaceChanged(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Timber.v("previewDisplayView destroyed")
                processor.videoSurfaceOutput.setSurface(null)
            }
        })
    }

    private fun onCameraStarted(surfaceTexture: SurfaceTexture?) {
        previewFrameTexture = surfaceTexture
        // Make the display view visible to start showing the preview. This triggers the
        // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
        previewDisplayView!!.visibility = View.VISIBLE
    }

    private fun cameraTargetResolution(): Size? {
        return null // No preference and let the camera (helper) decide.
    }

    private fun startCamera() {
        cameraHelper = CameraXPreviewHelper()
        cameraHelper!!.setOnCameraStartedListener { surfaceTexture: SurfaceTexture? ->
            onCameraStarted(surfaceTexture)
        }
        val cameraFacing = CameraFacing.FRONT
        cameraHelper!!.startCamera(
            this, cameraFacing,  /*unusedSurfaceTexture=*/null, cameraTargetResolution()
        )
    }

    private fun computeViewSize(width: Int, height: Int): Size {
        return Size(width, height)
    }

    private fun onPreviewDisplaySurfaceChanged(width: Int, height: Int) {
        // (Re-)Compute the ideal size of the camera-preview display (the area that the
        // camera-preview frames get rendered onto, potentially with scaling and rotation)
        // based on the size of the SurfaceView that contains the display.
        val viewSize = computeViewSize(width, height)
        val displaySize = cameraHelper!!.computeDisplaySizeFromViewSize(viewSize)
        val isCameraRotated = cameraHelper!!.isCameraRotated

        // Connect the converter to the camera-preview frames as its input (via
        // previewFrameTexture), and configure the output width and height as the computed
        // display size.
        converter!!.setSurfaceTextureAndAttachToGLContext(
            previewFrameTexture,
            if (isCameraRotated) displaySize.height else displaySize.width,
            if (isCameraRotated) displaySize.width else displaySize.height
        )
    }

    companion object {
        private const val TAG = "HolisterActivity"
        private const val BINARY_GRAPH_NAME = "holistic_tracking_gpu.binarypb"
        private const val INPUT_VIDEO_STREAM_NAME = "input_video"
        private const val OUTPUT_VIDEO_STREAM_NAME = "output_video"
        private const val OUTPUT_LANDMARKS_STREAM_NAME = "holistic_landmarks"

        // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
        // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
        // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
        // corner, whereas MediaPipe in general assumes the image origin is at top-left.
        private const val FLIP_FRAMES_VERTICALLY = true

        init {
            // Load all native libraries needed by the app.
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
        }
    }

}