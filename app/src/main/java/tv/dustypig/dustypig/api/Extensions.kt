package tv.dustypig.dustypig.api

import kotlin.math.roundToInt



fun Double.toTimeString(): String {
    val hours = (this / 3600).toInt()
    val minutes = ((this % 3600) / 60).roundToInt()

    var ret = ""
    if(hours > 0) {
        ret = hours.toString() + "h"
        if(minutes > 0)
            ret += " $minutes" + "m"
    } else if(minutes > 0) {
        ret = minutes.toString() + "m"
    }

    return ret
}

