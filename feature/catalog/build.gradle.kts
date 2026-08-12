plugins {
    alias(libs.plugins.arcana.android.library)
    alias(libs.plugins.arcana.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android { namespace = "io.github.woojaeheo.arcanavault.feature.catalog" }

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
