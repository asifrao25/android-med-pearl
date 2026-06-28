plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
}

subprojects {
    configurations.configureEach {
        resolutionStrategy {
            force(
                "androidx.activity:activity:${libs.versions.activityCompose.get()}",
                "androidx.activity:activity-ktx:${libs.versions.activityCompose.get()}",
                "androidx.activity:activity-compose:${libs.versions.activityCompose.get()}",
            )
        }
    }
}
