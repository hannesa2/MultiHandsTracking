[![](https://jitpack.io/v/hannesa2/mediapipe_multi_hands_tracking.svg)](https://jitpack.io/#hannesa2/mediapipe_multi_hands_tracking)

The steps to build and use MediaPipe AAR is documented in MediaPipe's [android_archive_library.md](https://github.com/google/mediapipe/blob/master//docs/getting_started/android_archive_library.md).

The source code is copied from MediaPipe's [multi-hand tracking gpu demo](https://github.com/hannesa2/mediapipe/tree/LastMultihandTrackiing/mediapipe/examples/android/src/java/com/google/mediapipe/apps/multihandtrackinggpu).
It's a branch, because on master it was deleted

# Usage in Gradle

in top `build.gradle`

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
and in module `build.gradle`

	dependencies {
	        implementation "com.github.hannesa2:mediapipe_multi_hands_tracking:$LATEST"
	}

Please see details here https://jitpack.io/#hannesa2/mediapipe_multi_hands_tracking
