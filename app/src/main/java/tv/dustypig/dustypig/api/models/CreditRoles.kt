package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class CreditRoles {
    @SerializedName("1")
    Cast,
    @SerializedName("2")
    Director,
    @SerializedName("3")
    Producer,
    @SerializedName("4")
    Writer,
    @SerializedName("5")
    ExecutiveProducer,
}