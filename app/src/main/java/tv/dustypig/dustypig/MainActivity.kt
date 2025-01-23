package tv.dustypig.dustypig

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.AlertsManager
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.progress_manager.ProgressReportManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.auth_flow.AuthNav
import tv.dustypig.dustypig.ui.isTablet
import tv.dustypig.dustypig.ui.main_app.AppNav
import tv.dustypig.dustypig.ui.theme.DustyPigTheme
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity: ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var castManager: CastManager

    @Inject
    lateinit var alertsManager: AlertsManager

    @Inject
    lateinit var progressReportManager: ProgressReportManager

    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authManager.init()
        FCMManager.init()
        analytics = Firebase.analytics

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setPictureInPictureParams(
                PictureInPictureParams
                    .Builder()
                    .setAutoEnterEnabled(true)
                    .build()
            )
        }

        enableEdgeToEdge()

        setContent {

            var currentTheme by remember {
                mutableStateOf(Themes.Maggies)
            }

            LaunchedEffect(true) {
                settingsManager.themeFlow.collectLatest {
                    currentTheme = it
                }
            }

//            Box(Modifier.safeDrawingPadding()) {
                DustyPigTheme(currentTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppStateSwitcher()
                    }
                }
//            }
        }

        checkIntent(intent)
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        checkIntent(intent)
//    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        castManager.setPassiveScanning()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onPause() {
        super.onPause()
        castManager.stopScanning()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        castManager.destroy()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if(!PlayerStateManager.playerScreenVisible.value)
            return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                @Suppress("DEPRECATION")
                enterPictureInPictureMode()
            }
        }
    }

    private fun checkIntent(intent: Intent?) {
        if (intent == null)
            return

        AlertsManager.handleNotificationTapped(intent)
    }



    @Composable
    fun AppStateSwitcher() {

        val loggedIn by authManager.loginState.collectAsState()

        when (loggedIn) {
            true -> {

                val playerScreenVisible by PlayerStateManager.playerScreenVisible.collectAsState()
//                if (playerScreenVisible)
//                    hideSystemUi()
//                else
//                    showSystemUi()


                requestedOrientation = if (playerScreenVisible) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else if (isTablet()) {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                AskNotificationPermission()
                AppNav()

            }
            false -> {
//                showSystemUi()
                requestedOrientation = if (isTablet()) {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                AuthNav()
            }

            else -> {
                //Unknown state
//                showSystemUi()
                Scaffold { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }


    @Composable
    private fun AskNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Notification permission granted: true")
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }



    @OptIn(DelicateCoroutinesApi::class)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        Log.d(TAG, "Notification permission granted: $isGranted")
        GlobalScope.launch {
            settingsManager.setAllowNotifications(isGranted)
            if(isGranted)
                AlertsManager.triggerUpdateFCMToken()
        }
    }
}















