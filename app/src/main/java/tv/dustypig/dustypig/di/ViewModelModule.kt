package tv.dustypig.dustypig.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideRouteNavigator(screenLoadingInfo: ScreenLoadingInfo): RouteNavigator = MyRouteNavigator(screenLoadingInfo)
}