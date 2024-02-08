package tv.dustypig.dustypig.api.models

data class SetTitlePermission(
    val mediaId: Int,
    val profileId: Int,
    val overrideState: OverrideState
)
