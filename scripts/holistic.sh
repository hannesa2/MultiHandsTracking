SRC=~/git/mediapipe
TARGET=library/src/main/assets

cp $SRC/mediapipe/graphs/holistic_tracking/holistic_tracking_gpu.binarypb $TARGET 2> /dev/null || echo "missing *.binarypb"
cp $SRC/mediapipe/graphs/holistic_tracking/holistic_tracking_gpu.pbtxt $TARGET 2> /dev/null || echo "missing *.pbtxt"
cp $SRC/mediapipe/modules/face_detection/face_detection_front.tflite $TARGET
cp $SRC/mediapipe/modules/face_landmark/face_landmark.tflite $TARGET
cp $SRC/mediapipe/modules/hand_landmark/hand_landmark.tflite $TARGET
cp $SRC/mediapipe/modules/hand_landmark/handedness.txt $TARGET
cp $SRC/mediapipe/modules/holistic_landmark/hand_recrop.tflite $TARGET
cp $SRC/mediapipe/modules/pose_detection/pose_detection.tflite $TARGET
cp $SRC/mediapipe/modules/pose_landmark/pose_landmark_upper_body.tflite $TARGET
cp $SRC/mediapipe/modules/pose_landmark/pose_landmark_full_body.tflite $TARGET