package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

class ProfileTitleOverrideInfo (
    @SerializedName("profile_id") val profileId: Int,
    var state: OverrideState,

    val name: String = "",
    @SerializedName("avatar_url") val avatarUrl: String = ""
)
