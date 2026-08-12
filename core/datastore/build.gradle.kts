plugins {
    alias(libs.plugins.arcana.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android { namespace = "io.github.woojaeheo.arcanavault.core.datastore" }

dependencies {
    implementation(projects.core.model)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
