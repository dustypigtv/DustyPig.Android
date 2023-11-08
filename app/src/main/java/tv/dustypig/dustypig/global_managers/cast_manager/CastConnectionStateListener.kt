package tv.dustypig.dustypig.global_managers.cast_manager

/*
    Having a listener outside of a state flow allows slow operations (like starting playback
    on a cast device) to complete without worrying about cancelled action blocks of
    rapidly firing state flows
 */
interface CastConnectionStateListener {
    fun onConnectionStateChanged(castConnectionState: CastConnectionState)
}