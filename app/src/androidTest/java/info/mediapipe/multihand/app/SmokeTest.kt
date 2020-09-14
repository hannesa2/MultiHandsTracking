package info.mediapipe.multihand.app

import android.Manifest
import android.view.View
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.bmwgroup.idnext.test.assertions.WaitingAssertion
import com.moka.utils.Screenshot
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
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)

    @Test
    fun smokeTestSimplyStart() {
        Screenshot.takeScreenshot("start")
        WaitingAssertion.assertVisibility(R.id.preview_display_layout, View.VISIBLE, 9000)
        // onView(withId(R.id.preview_display_layout)).check(matches(isDisplayed()))
        Screenshot.takeScreenshot("end")
    }
}
