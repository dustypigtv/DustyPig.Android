package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class UpdateProfile(
    val id: Int,
    val name: String,
    val pin: UShort?,
    val locked: Boolean,
    @SerializedName("avatar_image") val avatarImage: ByteArray?,
    @SerializedName("allowed_ratings") val allowedRatings: Int,
    @SerializedName("title_request_permissions") val titleRequestPermissions: TitleRequestPermissions
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateProfile

        if (id != other.id) return false
        if (name != other.name) return false
        if (pin != other.pin) return false
        if (locked != other.locked) return false
        if (avatarImage != null) {
            if (other.avatarImage == null) return false
            if (!avatarImage.contentEquals(other.avatarImage)) return false
        } else if (other.avatarImage != null) return false
        if (allowedRatings != other.allowedRatings) return false
        if (titleRequestPermissions != other.titleRequestPermissions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + (pin?.hashCode() ?: 0)
        result = 31 * result + locked.hashCode()
        result = 31 * result + (avatarImage?.contentHashCode() ?: 0)
        result = 31 * result + allowedRatings
        result = 31 * result + titleRequestPermissions.hashCode()
        return result
    }
}