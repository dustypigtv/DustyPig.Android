package tv.dustypig.dustypig.api.models

/*
Possible Bit Flags:

    0x0000 = None
    0x0001 = G
    0x0002 = PG
    0x0004 = PG-13
    0x0008 = R
    0x0010 = NC-17
    0x0020 = Unrated
    0x0040 = TV-Y
    0x0080 = TV-Y7
    0x0100 = TV-G
    0x0200 = TV-PG
    0x0400 = TV-14
    0x0800 = TV-MA
    0x1000 = Not Rated
 */


enum class MovieRatingsFlags (val value: Int) {
    None(0x0000),
    Movies_G(0x0001),
    Movies_PG(0x0002),
    Movies_PG13(0x0004),
    Movies_R(0x0008),
    Movies_NC17(0x0010),
    Movies_Unrated(0x0020)
}

enum class TVRatingsFlags (val value: Int) {
    None(0x0000),
    TV_Y(0x0040),
    TV_Y7(0x0080),
    TV_G(0x0100),
    TV_PG(0x0200),
    TV_14(0x0400),
    TV_MA(0x0800),
    TV_NotRated(0x1000)
}

class RatingsFlags constructor() {

    constructor(fromVal: Int) : this() {
        value = fromVal
    }

    var value: Int = 0
        private set

    fun getMovie(): MovieRatingsFlags {
        for (flag in MovieRatingsFlags.values()) {
            if (flag != MovieRatingsFlags.None) {
                if (value.and(flag.value) == flag.value) {
                    return flag
                }
            }
        }
        return MovieRatingsFlags.None
    }

    fun setMovie(newFlag: MovieRatingsFlags) {
        for (flag in MovieRatingsFlags.values()) {
            if (flag != MovieRatingsFlags.None) {
                value = value.and(flag.value.inv())
            }
        }
        value = value.and(newFlag.value)
    }

    fun getTV(): TVRatingsFlags {
        for (flag in TVRatingsFlags.values()) {
            if (flag != TVRatingsFlags.None) {
                if (value.and(flag.value) == flag.value) {
                    return flag
                }
            }
        }
        return TVRatingsFlags.None
    }

    fun setTV(newFlag: TVRatingsFlags) {
        for(flag in TVRatingsFlags.values()) {
            if (flag != TVRatingsFlags.None) {
               value = value.and(flag.value.inv())
            }
        }
        value = value.and(newFlag.value)
    }
}






















