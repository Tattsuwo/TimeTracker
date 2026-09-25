# Suivi du temps — base du projet Android

Base de code Kotlin / Jetpack Compose / Room pour l'application de suivi du
temps décrite dans le cahier des charges. Ce projet s'ouvre directement dans
Android Studio (File > Open > sélectionner le dossier `TimeTracker`).

## Avant le premier build

1. **Wrapper Gradle** : `gradle/wrapper/gradle-wrapper.properties` fixe la
   version de Gradle à 9.6.0 (version minimale et par défaut exigée par AGP
   9.4.0, voir `app/build.gradle.kts`). Si Android Studio réclame malgré tout
   une autre version au moment du sync, vérifiez la table de compatibilité
   AGP/Gradle correspondant à la version d'AGP réellement utilisée :
   https://developer.android.com/build/releases/agp-9-4-0-release-notes
2. **Kotlin "built-in"** : AGP 9+ compile Kotlin lui-même et n'accepte plus le
   plugin `org.jetbrains.kotlin.android` appliqué séparément (il a été retiré
   des deux `build.gradle.kts`). La version de Kotlin utilisée par ce
   compilateur intégré est forcée à 2.4.20 par un bloc `buildscript` en tête
   du `build.gradle.kts` racine, pour rester cohérente avec les autres
   plugins Kotlin du projet. Voir
   https://developer.android.com/build/migrate-to-built-in-kotlin
3. **Vérifier les versions** dans `gradle/libs.versions.toml`. Certaines sont
   marquées `A VERIFIER` (lifecycle, navigation-compose) : ouvrez le lien
   indiqué en commentaire et reportez la dernière version stable. KSP a déjà
   été corrigé une fois (2.3.11) suite à un changement de son schéma de
   version, indépendant de celui de Kotlin depuis KSP 2.3.0 — vérifiez-le
   aussi si plusieurs semaines se sont écoulées depuis la génération.
4. **Renommer le package / applicationId** : `com.example.timetracker` dans
   `app/build.gradle.kts` et dans l'arborescence
   `app/src/main/java/com/example/timetracker/` (Android Studio propose un
   refactoring automatique : clic droit sur le package > Refactor > Rename).
5. **Remplacer l'icône de lanceur** : les fichiers dans
   `app/src/main/res/mipmap-*` et `res/drawable/ic_launcher_*.xml` sont des
   placeholders générés automatiquement (un simple pictogramme d'horloge).
   Clic droit sur `res/` > New > Image Asset pour les remplacer proprement.
6. Ce projet n'a jamais été compilé dans cet environnement (pas d'accès
   réseau ni de SDK Android disponibles ici pour lancer Gradle) : la première
   synchronisation Gradle dans Android Studio peut faire remonter d'autres
   ajustements mineurs de version que je n'aurais pas anticipés.

## Architecture en un coup d'œil

- `data/` : entités Room (`Category`, `Session`, `ActiveTimer`), DAO,
  `AppDatabase`. Voir les commentaires de `Session.kt` et `ActiveTimer.kt`
  pour le choix de conception clé : le chrono "en cours" est une table
  séparée de "sessions", qui ne contient elle que des sessions terminées.
- `repository/TimeTrackerRepository.kt` : point d'entrée unique pour les
  ViewModels (chrono unique, calcul des sessions interrompues, export/import
  JSON).
- `service/TimerForegroundService.kt` : notification persistante pendant le
  chrono actif (foreground service de type `specialUse`, seul type adapté à
  un chronomètre depuis Android 14).
- `backup/` : modèles sérialisables + lecture/écriture via le Storage Access
  Framework (aucune permission de stockage nécessaire).
- `ui/` : un dossier par écran (`timer`, `history`, `summary`, `settings`),
  plus `theme/`, `navigation/` et `common/` (composants partagés). Pas de
  Hilt : `ui/ViewModelFactory.kt` construit les ViewModels à la main à partir
  du repository exposé par `TimeTrackerApplication`.

## Fonctionnalités couvertes par cette base

- Démarrer / arrêter un chrono, un seul actif à la fois (contrainte au
  niveau de la base de données, pas seulement côté UI).
- Notification persistante + permission `POST_NOTIFICATIONS` (Android 13+).
- Détection d'une session laissée ouverte après un redémarrage du téléphone,
  avec clôture manuelle par l'utilisateur (pas de reprise automatique).
- Nommage de la session à l'arrêt (nom, description, catégorie), catégories
  par défaut + création à la volée.
- Historique modifiable/supprimable, groupé par jour avec total par jour.
- Catégories : couleur personnalisable (palette fixe), ajout depuis les
  Réglages ou à la volée pendant l'arrêt d'un chrono, suppression en cascade
  (avec confirmation et décompte des sessions concernées).
- Synthèse par jour / par activité / par catégorie.
- Export et restauration JSON via le sélecteur de fichiers système (la
  restauration remplace entièrement les données existantes, avec confirmation).
- Heures en 24h, durées en heures et minutes.

## Pistes d'évolution volontairement laissées de côté

- Détection de doublons à l'import JSON (inutile désormais : l'import
  remplace entièrement les données existantes plutôt que de les fusionner).
- Filtrage par période dans la synthèse (aujourd'hui/7 jours/tout).
- Palette de couleurs fixe (10 teintes) plutôt qu'un sélecteur libre type roue
  HSV, pour rester simple et sans dépendance externe.
