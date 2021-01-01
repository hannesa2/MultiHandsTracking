package info.mediapipe.app

import android.os.Bundle
import javax.annotation.Nonnull
import info.hannes.github.AppUpdateHelper

class MedaipipeMainActivity : NavigationActivity() {

    override fun onCreate(@Nonnull savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediapipe_main)
        supportFragmentManager
                .beginTransaction()
                .add(R.id.contentInfo, SystemInfoFragment())
                .commit()

        AppUpdateHelper.checkForNewVersion(
                this,
                BuildConfig.GIT_USER,
                BuildConfig.GIT_REPOSITORY,
                BuildConfig.VERSION_NAME
        )
    }
}