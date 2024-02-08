package tv.dustypig.dustypig.api.models

data class ProfileCredentials(
    val id: Int,

    /**
     * This is actually a UShort?, but Gson throws an error.  So double check that a value is a UShort? before setting it!
     */
    val pin: Int? = null,

    val fcmToken: String? = null
)