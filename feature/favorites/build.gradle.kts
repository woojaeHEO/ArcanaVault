plugins {
    alias(libs.plugins.holobinder.android.library)
    alias(libs.plugins.holobinder.compose)
}

android { namespace = "io.github.woojaeheo.holobinder.feature.favorites" }

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
