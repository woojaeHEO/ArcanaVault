plugins {
    alias(libs.plugins.arcana.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android { namespace = "io.github.woojaeheo.arcanavault.core.data" }

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
