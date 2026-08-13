package io.github.woojaeheo.holobinder

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import io.github.woojaeheo.holobinder.widget.RecommendationWorker
import okhttp3.Dispatcher
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class HoloBinderApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /** 카드 이미지 메모리 캐시와 디스크 캐시 및 네트워크 요청을 한곳에서 관리한다 */
    override fun newImageLoader(context: Context): ImageLoader {
        val networkDispatcher = Dispatcher().apply {
            maxRequests = 32
            maxRequestsPerHost = 16
        }
        val imageClient = OkHttpClient.Builder()
            .dispatcher(networkDispatcher)
            .connectionPool(ConnectionPool(16, 5, TimeUnit.MINUTES))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, .20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("card_image_cache").toOkioPath())
                    .maxSizeBytes(256L * 1_024L * 1_024L)
                    .build()
            }
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { imageClient },
                    ),
                )
            }
            .crossfade(140)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        RecommendationWorker.enqueueInitial(this)
    }
}
