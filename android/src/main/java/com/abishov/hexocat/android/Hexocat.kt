package com.abishov.hexocat.android

import android.app.Application
import android.os.StrictMode
import com.abishov.hexocat.android.common.dispatcher.DefaultDispatcherProvider
import com.abishov.hexocat.android.common.utils.CrashReportingTree
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import hu.supercluster.paperwork.Paperwork
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.threeten.bp.Clock
import timber.log.Timber
import javax.inject.Inject

open class Hexocat : Application(), HasAndroidInjector {

    protected lateinit var appComponent: AppComponent
    protected lateinit var refWatcher: RefWatcher

    @Inject
    internal lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    internal lateinit var paperwork: Paperwork

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // this process is going to be used to
            // analyze heap dumps by LeakCanary
            return
        }

        AndroidThreeTen.init(this)

        setupAppComponent()
        setUpLeakCanary()
        setUpTimber()

        // Do not allow to do any work on the
        // main thread. Detect activity leaks.
        setupStrictMode()
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    protected fun setupAppComponent() {
        appComponent = prepareAppComponent()
        appComponent.inject(this)
    }

    private fun setUpLeakCanary() {
        refWatcher = if (BuildConfig.DEBUG) {
            LeakCanary.install(this)
        } else {
            RefWatcher.DISABLED
        }
    }

    private fun setUpTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree(paperwork))
        }
    }

    protected open fun prepareAppComponent(): AppComponent {
        return DaggerAppComponent.builder()
            .baseUrl("https://api.github.com/graphql".toHttpUrl())
            .dispatcherProvider(DefaultDispatcherProvider())
            .clock(Clock.systemDefaultZone())
            .application(this)
            .build()
    }

    fun appComponent(): AppComponent {
        return appComponent
    }

    fun refWatcher(): RefWatcher {
        return refWatcher
    }
}
