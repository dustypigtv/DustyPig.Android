package tv.dustypig.dustypig.ui.main_app

object ScreenLoadingInfo {
    var title: String = ""
    var posterUrl: String = ""
    var backdropUrl: String = ""

    fun setInfo(title: String, posterUrl: String, backdropUrl: String) {
        this.title = title
        this.posterUrl = posterUrl
        this.backdropUrl = backdropUrl
    }

    fun clearInfo() = setInfo("", "", "")
}