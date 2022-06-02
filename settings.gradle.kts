
rootProject.name = "checkstyle-plugin"

// build scan plugin can only be applied in settings file
plugins {
    `gradle-enterprise`
    id("com.gradle.common-custom-user-data-gradle-plugin") version "1.6.5"
}
