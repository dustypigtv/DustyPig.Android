package tv.dustypig.dustypig.api.models

class TitlePermissions(
    val mediaId: Int,
    val subProfiles: List<ProfileTitleOverrideInfo>,
    val friendProfiles: List<ProfileTitleOverrideInfo>
)