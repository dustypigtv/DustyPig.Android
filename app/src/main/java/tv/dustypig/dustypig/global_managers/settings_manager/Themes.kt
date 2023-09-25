package tv.dustypig.dustypig.global_managers.settings_manager

enum class Themes {
    Maggies,
    DustyPig,
    LB,
    Red,
    HuluGreen,
    DisneyBlue,
    BurntOrange,
    AndroidDark;

    companion object {
        fun fromOrdinal(ordinal: Int) = values()[ordinal]
    }
}