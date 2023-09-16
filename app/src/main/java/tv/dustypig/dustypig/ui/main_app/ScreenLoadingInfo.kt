package tv.dustypig.dustypig.ui.main_app

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenLoadingInfo @Inject constructor() {
    var title: String = ""
    var posterUrl: String = ""
    var backdropUrl: String = ""
}