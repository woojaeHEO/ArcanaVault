plugins {
    alias(libs.plugins.arcana.android.library)
    alias(libs.plugins.arcana.compose)
}

android { namespace = "io.github.woojaeheo.arcanavault.feature.settings" }

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
}
