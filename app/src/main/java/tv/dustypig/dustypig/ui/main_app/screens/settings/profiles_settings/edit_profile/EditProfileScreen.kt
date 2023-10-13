package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.BasicLibrary
import tv.dustypig.dustypig.api.models.MovieRatings
import tv.dustypig.dustypig.api.models.TVRatings
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.ui.composables.AvatarEditor
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.EnumSelectorDropdown
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog

@Composable
fun EditProfileScreen(vm: EditProfileViewModel) {
    val uiState by vm.uiState.collectAsState()
    EditProfileScreenInternal(
        popBackStack = vm::popBackStack,
        setError = vm::setError,
        hideError = vm::hideError,
        infoLoaded = vm::infoLoaded,
        saveProfile = vm::saveProfile,
        deleteProfile = vm::deleteProfile,
        uiState = uiState
    )
}

@Composable
private fun EditProfileScreenInternal(
    popBackStack: () -> Unit,
    setError: (Exception, Boolean) -> Unit,
    hideError: () -> Unit,
    infoLoaded: () -> Unit,
    saveProfile: (
        String,                     //name
        String,                     //pin
        Boolean,                    //deletePin
        MovieRatings,
        TVRatings,
        TitleRequestPermissions,
        LockedState,
        List<Int>,                  //selectedLibraries
        String                      //avatarFile
    ) -> Unit,
    deleteProfile: () -> Unit,
    uiState: EditProfileUIState
) {

    var newName by remember { mutableStateOf(uiState.name) }
    var nameError by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var deletePin by remember { mutableStateOf(false) }
    var newAvatar by remember { mutableStateOf(uiState.avatarUrl) }
    val newSelectedLibraryIds = remember { mutableStateListOf<Int>() }
    val listState = rememberLazyListState()
    var maxMovieRating by remember { mutableStateOf(uiState.maxMovieRating) }
    var maxTVRating by remember { mutableStateOf(uiState.maxTVRating) }
    var titleRequestPermission by remember { mutableStateOf(uiState.titleRequestPermissions) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var profileLocked by remember { mutableStateOf(uiState.lockedState) }


    val topBarText = if(uiState.addMode) "Add Profile" else "Edit Profile"

    if(uiState.loadingComplete) {
        newName = uiState.name
        newAvatar = uiState.avatarUrl
        maxMovieRating = uiState.maxMovieRating
        maxTVRating = uiState.maxTVRating
        titleRequestPermission = uiState.titleRequestPermissions
        profileLocked = uiState.lockedState
        newSelectedLibraryIds.clear()
        for(id in uiState.selectedLibraryIds) {
            newSelectedLibraryIds.add(id)
        }
        infoLoaded()
    }



    Scaffold (
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = topBarText)
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                state = listState
            ) {

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                /**
                 * Name - Required
                 */
                item {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            if (newName.isNotBlank())
                                nameError = false
                        },
                        label = {
                            Text(text = "Name")
                        },
                        singleLine = true,
                        enabled = !uiState.busy,
                        modifier = Modifier
                            .width(300.dp)
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        isError = nameError
                    )
                }

                /**
                 * Pin
                 */
                item {
                    if (uiState.hasPin) {
                        Row(
                            modifier = Modifier.width(300.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pin,
                                onValueChange = {
                                    if (it.length <= 4)
                                        pin = it.trimStart('0')
                                },
                                label = {
                                    Text(text = "Pin #:")
                                },
                                singleLine = true,
                                enabled = !uiState.busy && !deletePin,
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.NumberPassword
                                ),
                                visualTransformation = PasswordVisualTransformation()
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "Delete Pin",
                                maxLines = 1,
                                modifier = Modifier.padding(12.dp)
                            )
                            Switch(
                                enabled = !uiState.busy,
                                checked = deletePin,
                                onCheckedChange = {
                                    deletePin = it
                                    if (deletePin)
                                        pin = ""
                                }
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 4)
                                    pin = it.trimStart('0')
                            },
                            label = {
                                Text(text = "Pin #:")
                            },
                            singleLine = true,
                            enabled = !uiState.busy,
                            modifier = Modifier.width(300.dp),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }
                }


                /**
                 * Max Movie Rating
                 */
                item {
                    EnumSelectorDropdown(
                        enabled = !uiState.busy && !uiState.selfMode,
                        label = "Max Movie Rating",
                        values = MovieRatings.values(),
                        exclude = arrayOf(MovieRatings.None),
                        currentValue = maxMovieRating,
                        onChanged = { maxMovieRating = it }
                    )
                }

                /**
                 * Max TV Rating
                 */
                item {
                    EnumSelectorDropdown(
                        enabled = !uiState.busy && !uiState.selfMode,
                        label = "Max TV Rating",
                        values = TVRatings.values(),
                        exclude = arrayOf(TVRatings.None),
                        currentValue = maxTVRating,
                        onChanged = { maxTVRating = it }
                    )
                }

                /**
                 * Title Request Permissions
                 */
                item {
                    EnumSelectorDropdown(
                        enabled = !uiState.busy && !uiState.selfMode,
                        label = "Request Title Permission",
                        values = TitleRequestPermissions.values(),
                        exclude = arrayOf(),
                        currentValue = titleRequestPermission,
                        onChanged = { titleRequestPermission = it }
                    )
                }

                if(!uiState.addMode && !uiState.selfMode) {
                    item{
                        EnumSelectorDropdown(
                            enabled = !uiState.busy,
                            label = "Profile Status",
                            values = LockedState.values(),
                            exclude = arrayOf(),
                            currentValue = profileLocked,
                            onChanged = { profileLocked = it }
                        )
                    }
                }


                /**
                 * Avatar
                 */
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    AvatarEditor(
                        enabled = !uiState.busy,
                        currentAvatar = newAvatar,
                        onChanged = { newAvatar = it },
                        onException = {
                            if (it != null)
                                setError(it, false)
                        }
                    )
                }


                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                /**
                 * Libraries
                 */
                if (uiState.libraries.isNotEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 0.dp)
                                .clip(shape = RoundedCornerShape(4.dp))
                                .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp)),

                            ) {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = "Library",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = "Allowed",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    items(uiState.libraries) { library ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 0.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(12.dp, 0.dp),
                                text = library.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            if(uiState.selfMode) {
                                TintedIcon(
                                    imageVector = Icons.Filled.Check,
                                    modifier = Modifier.padding(12.dp, 0.dp)
                                )
                            } else {
                                Switch(
                                    modifier = Modifier.padding(12.dp, 0.dp),
                                    checked = newSelectedLibraryIds.contains(library.id),
                                    enabled = !uiState.busy,
                                    onCheckedChange = {
                                        if (it) {
                                            if (!newSelectedLibraryIds.contains(library.id))
                                                newSelectedLibraryIds.add(library.id)
                                        } else {
                                            if (newSelectedLibraryIds.contains(library.id))
                                                newSelectedLibraryIds.remove(library.id)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }


                /**
                 * Save button
                 */
                item {
                    Button(
                        modifier = Modifier.padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 24.dp),
                        enabled = !uiState.busy,
                        onClick = {
                            if (newName.isBlank()) {
                                nameError = true
                                scope.launch {
                                    listState.scrollToItem(0, scrollOffset = 0)
                                    try {
                                        focusRequester.requestFocus()
                                    } catch (_: Exception) {
                                    }
                                }
                            } else {
                                saveProfile(
                                    newName,
                                    pin,
                                    deletePin,
                                    maxMovieRating,
                                    maxTVRating,
                                    titleRequestPermission,
                                    profileLocked,
                                    newSelectedLibraryIds.toList(),
                                    newAvatar
                                )
                            }
                        }
                    ) {
                        Text(text = "Save")
                    }
                }


                /**
                 * Delete Button
                 */
                if (!uiState.addMode && !uiState.selfMode) {
                    item {
                        Button(
                            modifier = Modifier.padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 24.dp),
                            enabled = !uiState.busy,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            onClick = { showDeleteDialog = true }
                        ) {
                            Text(text = "Delete Profile")
                        }
                    }
                }
            }

            if(uiState.busy) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }


            if(showDeleteDialog) {
                YesNoDialog(
                    onNo = { showDeleteDialog = false },
                    onYes = {
                        showDeleteDialog = false
                        deleteProfile()
                    },
                    title = "Confirm",
                    message = "Are you sure you want to delete this profile?"
                )
            }

            if(uiState.showErrorDialog) {
                ErrorDialog(onDismissRequest = hideError, message = uiState.errorMessage)
            }
        }
    }
}



@Preview
@Composable
private fun EditProfileScreenPreview() {

    val libs = arrayListOf<BasicLibrary>()
    for(i in 1..10)
        libs.add(
            BasicLibrary(
                id = i,
                name = "Library $i",
                isTV = false
            )
        )

    val uiState = EditProfileUIState(
        busy = false,
        libraries = libs,
        addMode = false,
        hasPin = true
    )

    PreviewBase {
        EditProfileScreenInternal(
            popBackStack = { },
            setError = { _, _ -> },
            hideError = { },
            infoLoaded = { },
            saveProfile = { _, _, _, _, _, _, _, _, _ -> },
            deleteProfile = { },
            uiState = uiState
        )
    }
}