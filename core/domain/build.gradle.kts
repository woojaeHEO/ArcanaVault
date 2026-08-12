plugins {
    alias(libs.plugins.arcana.android.library)
}

android { namespace = "io.github.woojaeheo.arcanavault.core.domain" }

dependencies {
    api(projects.core.model)
    implementation(libs.javax.inject)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
