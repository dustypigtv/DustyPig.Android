package tv.dustypig.dustypig

import tv.dustypig.dustypig.api.models.HomeScreenList

/**
 * Global States
 */
object ThePig{



    /**
     * Temporarily stores data for the ShowMore screen, reducing network calls
      */
    var showMoreData: HomeScreenList = HomeScreenList(0, "", listOf())


}