package tv.dustypig.dustypig.api.models

class ProfileTitleOverrideInfo (
    val profileId: Int,
    var overrideState: OverrideState,
    val avatarUrl: String = "",
    val name: String = ""
)
