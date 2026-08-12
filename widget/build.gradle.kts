plugins {
    alias(libs.plugins.arcana.android.library)
    alias(libs.plugins.arcana.compose)
}

android { namespace = "io.github.woojaeheo.arcanavault.widget" }

dependencies {
    implementation(projects.core.database)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
}
