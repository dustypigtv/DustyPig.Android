package tv.dustypig.dustypig.ui.main_app.screens.player

data class VideoTiming(
    val mediaId: String,
    val introStartTime: Double?,
    val introEndTime: Double?,
    var introClicked: Boolean = false,
    val creditsStartTime: Double?,
    var creditsClicked: Boolean = false,
    val isMovie: Boolean
) {
    fun positionWithinIntro(position: Double): Boolean {
        if (introClicked)
            return false

        if (introStartTime == null)
            return false

        if (introEndTime == null)
            return false

        if (introEndTime <= introStartTime)
            return false

        return position >= introStartTime && position < introEndTime
    }

    fun positionWithinCredits(position: Double, length: Double): Boolean {
        if (creditsClicked)
            return false

        if (isMovie && creditsStartTime == null)
            return false

        val calcTime = creditsStartTime ?: (length - 30.0)

        if (calcTime < 1.0)
            return false

        return position >= calcTime
    }
}
