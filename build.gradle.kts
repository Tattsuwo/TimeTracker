// Fichier racine : déclare les plugins (appliqués individuellement par
// app/build.gradle.kts).
//
// CORRIGÉ (24/09) : AGP 9+ compile Kotlin lui-même ("built-in Kotlin") et
// n'a donc plus besoin, et n'accepte même plus, du plugin
// org.jetbrains.kotlin.android appliqué séparément (il est retiré de
// app/build.gradle.kts). Par défaut, AGP utilise sa propre version interne
// de Kotlin (plus ancienne que 2.4.20) : le bloc buildscript ci-dessous force
// le compilateur intégré à utiliser la même version que les plugins
// compose/serialization appliqués plus bas, pour éviter tout conflit de version.
// Le numéro ci-dessous doit être tenu à jour manuellement avec "kotlin" dans
// gradle/libs.versions.toml (on ne peut pas référencer le catalogue de
// versions dans un bloc buildscript, qui s'exécute trop tôt).
// Source : https://developer.android.com/build/migrate-to-built-in-kotlin
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.room) apply false
}
