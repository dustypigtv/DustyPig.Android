package tv.dustypig.dustypig

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
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
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import tv.dustypig.dustypig.global_managers.auth_manager.AuthStates
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.MyDownloadManager
import tv.dustypig.dustypig.global_managers.progress_manager.ProgressReportManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.auth_flow.AuthNav
import tv.dustypig.dustypig.ui.isTablet
import tv.dustypig.dustypig.ui.main_app.AppNav
import tv.dustypig.dustypig.ui.showSystemUi
import tv.dustypig.dustypig.ui.theme.DustyPigTheme
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var downloadManager: MyDownloadManager

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

        //enableEdgeToEdge()

        setContent {

            var currentTheme by remember {
                mutableStateOf(Themes.Maggies)
            }

            LaunchedEffect(true) {
                settingsManager.themeFlow.collectLatest {
                    currentTheme = it
                }
            }

            //val playerScreenVisible by PlayerStateManager.playerScreenVisible.collectAsState()
            val playerScreenVisible by rememberUpdatedState(PlayerStateManager.playerScreenVisible)
            if (!playerScreenVisible)
                this.showSystemUi()

            requestedOrientation = if (playerScreenVisible) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else if (isTablet()) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }


            DustyPigTheme(currentTheme) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppStateSwitcher()
                }
            }
        }

        checkIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }


    private fun checkIntent(intent: Intent?) {
        if (intent == null)
            return

        AlertsManager.handleNotificationTapped(intent)
    }




    @Composable
    fun AppStateSwitcher() {

        val authState by authManager.loginState.collectAsState(AuthStates.Nada)

        Log.d(TAG, "AuthState: $authState")

        when (authState) {
            AuthStates.LoggedIn -> {
                AskNotificationPermission()
                AppNav()
                Log.d(TAG, "Trigger Alerts")
                AlertsManager.triggerUpdate()
            }

            AuthStates.LoggedOut -> {
                AuthNav()
            }

            else -> {
                Scaffold { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo_transparent),
                            modifier = Modifier.size(200.dp),
                            contentDescription = null
                        )
                        CircularProgressIndicator()
                    }
                }
                authManager.changeProfilesPhase2Enabled = true
            }
        }
    }


    @Composable
    private fun AskNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
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
            if (isGranted)
                AlertsManager.triggerUpdateFCMToken()
        }
    }
}















