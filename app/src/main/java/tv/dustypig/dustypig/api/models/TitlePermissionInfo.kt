package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

class TitlePermissionInfo (
    @SerializedName("title_id") val titleId: Int,
    val profiles: List<ProfileTitleOverrideInfo>
)