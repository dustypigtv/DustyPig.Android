package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

class TitlePermissionInfo (
    @SerializedName("title_id") val titleId: Int,
    @SerializedName("sub_profiles") val subProfiles: List<ProfileTitleOverrideInfo>,
    @SerializedName("friend_profiles") val friendProfiles: List<ProfileTitleOverrideInfo>
)