package info.mediapipe.multihand.app

import android.Manifest
import android.graphics.Bitmap
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.captureToBitmap
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import info.mediapipe.app.R
import info.mediapipe.app.multihand.MultiHandActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MultiHandActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)

    @Test
    fun smokeTestSimplyStart() {
        onView(withId(R.id.preview_display_layout)).check(matches(isDisplayed()))
        onView(ViewMatchers.isRoot())
            .perform(captureToBitmap { bitmap: Bitmap -> bitmap.writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}") })

    }
}
