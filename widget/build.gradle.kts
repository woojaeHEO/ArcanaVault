plugins {
    alias(libs.plugins.arcana.android.library)
    alias(libs.plugins.arcana.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android { namespace = "io.github.woojaeheo.arcanavault.widget" }

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.designsystem)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt.android)
    ksp(libs.androidx.hilt.compiler)
    ksp(libs.hilt.compiler)
}
