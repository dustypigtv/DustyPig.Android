package tv.dustypig.dustypig.ui

import android.content.res.Configuration

fun Configuration.isTablet(): Boolean {

//    return if (this.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//        this.screenWidthDp > 840
//    } else {
//        this.screenWidthDp > 600
//    }

    return if (this.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        this.screenHeightDp > 600
    } else {
        this.screenWidthDp > 600
    }
}
//
//fun Configuration.isTabletLandscape(): Boolean {
//    return this.isTablet() && this.orientation == Configuration.ORIENTATION_LANDSCAPE
//}
//
//fun Configuration.isTabletPortrait(): Boolean {
//    return this.isTablet() && this.orientation != Configuration.ORIENTATION_LANDSCAPE
//}
//
