/*
 * MIT License
 * Copyright (c) 2021.  TechnoWolf FOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package `in`.technowolf.nyx

import `in`.technowolf.nyx.ui.di.databaseModule
import `in`.technowolf.nyx.ui.di.viewModelModule
import android.app.Application
import android.graphics.Bitmap
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.util.CoilUtils
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Nyx : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Nyx)
            modules(listOf(databaseModule, viewModelModule))
        }

        UnsplashPhotoPicker.init(
            this,
            BuildConfig.GRADLE_ACCESS_KEY,
            BuildConfig.GRADLE_PRIVATE_KEY,
            10
        )

        /**
         * Building singleton instance of Coil image loader
         * use Content#imageLoader to access image loader
         */
        val imageLoader = ImageLoader.Builder(applicationContext)
            .crossfade(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .build()
            }
            .build()
        Coil.setImageLoader(imageLoader)
    }
}