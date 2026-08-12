plugins { alias(libs.plugins.holobinder.android.library) }

android { namespace = "io.github.woojaeheo.holobinder.core.common" }

dependencies {
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
