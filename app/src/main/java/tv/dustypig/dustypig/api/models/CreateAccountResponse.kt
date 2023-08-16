package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class CreateAccountResponse (
    @SerializedName("email_verification_required") val emailVerificationRequired: Boolean?
)