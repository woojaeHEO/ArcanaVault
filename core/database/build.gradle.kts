plugins {
    alias(libs.plugins.holobinder.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

android { namespace = "io.github.woojaeheo.holobinder.core.database" }

room { schemaDirectory("$projectDir/schemas") }

dependencies {
    implementation(projects.core.model)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt.android)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
}
