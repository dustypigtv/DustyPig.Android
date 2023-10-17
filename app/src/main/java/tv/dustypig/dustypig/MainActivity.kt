package tv.dustypig.dustypig

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.NotificationsManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.fcm_manager.FCMManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.auth_flow.AuthNav
import tv.dustypig.dustypig.ui.composables.LockScreenOrientation
import tv.dustypig.dustypig.ui.isTablet
import tv.dustypig.dustypig.ui.main_app.AppNav
import tv.dustypig.dustypig.ui.main_app.AppNavViewModel
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerViewModel
import tv.dustypig.dustypig.ui.theme.DustyPigTheme
import javax.inject.Inject

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
    lateinit var notificationsManager: NotificationsManager

    @Inject
    lateinit var settingsManager: SettingsManager

    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FCMManager.init()
        analytics = Firebase.analytics
        authManager.init()
        downloadManager.start()

        setContent {

            var currentTheme by remember {
                mutableStateOf(Themes.Maggies)
            }

            LaunchedEffect(true) {
                settingsManager.themeFlow.collectLatest {
                    currentTheme = it
                }
            }

            DustyPigTheme(currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppStateSwitcher()
                }
            }
        }

        checkIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        FCMManager.activityResumed()
    }

    override fun onPause() {
        super.onPause()
        FCMManager.activityPaused()
    }


    private fun checkIntent(intent: Intent?) {
        try {
            if(intent == null)
                return

            val id = intent.getIntExtra(FCMManager.INTENT_DATA_ID, -1)
            if(id < 0)
                return

            notificationsManager.markAsRead(id)

            val deepLink = intent.getStringExtra(FCMManager.INTENT_DATA_DEEP_LINK)
            if(!deepLink.isNullOrEmpty()) {
                AppNavViewModel.queueDeepLink(deepLink)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }





    @Composable
    fun AppStateSwitcher() {

        
        if(PlayerViewModel.playerScreenVisible) {
            LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        } else if(!LocalConfiguration.current.isTablet()) {
            LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
            
        if (authManager.loginState == AuthManager.LOGIN_STATE_LOGGED_IN) {
            AskNotificationPermission()
            AppNav()
        } else if (authManager.loginState == AuthManager.LOGIN_STATE_LOGGED_OUT) {
            AuthNav()
        } else {
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
            if (authManager.loginState == AuthManager.LOGIN_STATE_SWITCHING_PROFILES) {
                LaunchedEffect(true) {
                    authManager.switchProfileEnd()
                }
            }
        }
    }


    @Composable
    private fun AskNotificationPermission() {

        if(authManager.loginState != AuthManager.LOGIN_STATE_LOGGED_IN)
            return

        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
                Log.d(TAG, "Notification permission granted: true")


//            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
//                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
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
        }
    }
}















