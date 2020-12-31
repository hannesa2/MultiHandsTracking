package info.mediapipe.app

import com.google.mediapipe.formats.proto.LandmarkProto

fun List<LandmarkProto.NormalizedLandmarkList>.landmarksDebugString(): String {
    var multiHandLandmarksStr = "hands ∑${this.size}".trimIndent()
    for ((handIndex, landmarks) in this.withIndex()) {
        multiHandLandmarksStr += "\r#Hand landmarks for hand[$handIndex]: ∑${landmarks.landmarkCount}"
        for ((landmarkIndex, landmark) in landmarks.landmarkList.withIndex()) {
            multiHandLandmarksStr += "\rLandmark [$landmarkIndex]: (${landmark.x}, ${landmark.y}, ${landmark.z})"
        }
    }
    return multiHandLandmarksStr
}