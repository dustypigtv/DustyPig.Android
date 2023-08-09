package tv.dustypig.dustypig.ui.signin.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.AuthManager
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.throwIfError
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.composables.PinEntry
import tv.dustypig.dustypig.ui.theme.DarkGray

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SelectProfileScreen(navHostController: NavHostController) {

    val context = LocalContext.current
    val composableScope = rememberCoroutineScope()
    val loading = remember { mutableStateOf(true) }
    val profiles = remember { mutableStateListOf<BasicProfile>() }
    val errorMessage = remember { mutableStateOf("") }
    val showLoadingError = remember { mutableStateOf(false) }
    val profileId = remember { mutableStateOf(0) }
    val pin = remember { mutableStateOf("") }
    val showPinDialog = remember { mutableStateOf(false) }
    val showLoginError = remember { mutableStateOf(false) }
    val listState = rememberLazyGridState()


    fun getPinInt(): Int? {
        return try{
            Integer.parseUnsignedInt(pin.value)
        } catch(_: Exception){
            null
        }
    }

    fun login(profileCredentials: ProfileCredentials) {
        composableScope.launch{
            try {
                loading.value = true
                val response = ThePig.api.profileLogin(profileCredentials)
                response.throwIfError()
                val data = response.body()!!.data
                AuthManager.setAuthState(context, data.token!!, data.profile_id!!, data.login_type == LoginResponse.LOGIN_TYPE_MAIN_PROFILE)
            }
            catch (ex: Exception) {
                errorMessage.value = ex.message ?: "Unknown Error"
                showLoginError.value = true
                loading.value = false
            }
        }
    }

    fun login(basicProfile: BasicProfile) {
        if(basicProfile.has_pin) {
            profileId.value = basicProfile.id
            showPinDialog.value = true
        } else {
            login(ProfileCredentials(basicProfile.id, null, null))
        }
    }

    LaunchedEffect(true){
        try{
            val response = ThePig.api.listProfiles()
            response.throwIfError()
            val data = response.body()!!.data
            profiles.addAll(data)
            loading.value = false
        } catch (ex: Exception) {
            errorMessage.value = ex.message ?: "Unknown Error"
            showLoadingError.value = true
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Select Profile") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = DarkGray)
            )
        }
    ) { contentPadding ->

        if (loading.value) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {

            LazyVerticalGrid(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 72.dp),
                verticalArrangement = Arrangement.spacedBy(36.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 24.dp),
                state = listState
            ) {
                items(profiles, key = { it.id }) {

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Avatar(basicProfile = it, onClick = { login(it) })
                        Text(text = it.name)
                    }
                }
            }
        }
    }

    if(showLoadingError.value){
        OkDialog(onDismissRequest = {
            showLoadingError.value = false
            navHostController.popBackStack()
        }, title = "Error", message = errorMessage.value)
    }

    if(showLoginError.value) {
        OkDialog(onDismissRequest = { showLoginError.value = false }, title = "Error", message = errorMessage.value)
    }

    if(showPinDialog.value) {

        val confirmEnabled = remember { derivedStateOf { pin.value.length == 4 }}

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = { showPinDialog.value = false },
            title = { Text("Enter Pin") },
            text = {
                PinEntry(valueChanged = { pin.value = it }, autoFocus = true)
            },
            confirmButton = {
                TextButton(enabled = confirmEnabled.value,
                    onClick = {
                        showPinDialog.value = false
                        login(ProfileCredentials(profileId.value, getPinInt(), null))
                    }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Preview
@Composable
fun SelectProfileScreenPreview() {
    SelectProfileScreen(rememberNavController())
}