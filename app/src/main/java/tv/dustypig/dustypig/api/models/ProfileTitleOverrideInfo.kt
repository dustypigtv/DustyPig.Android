package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

class ProfileTitleOverrideInfo (
    @SerializedName("profile_id") val profileId: Int,
    @SerializedName("override_state") var overrideState: OverrideState,
    @SerializedName("avatar_url") val avatarUrl: String = "",
    val name: String = ""
)
