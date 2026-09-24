# Suivi du temps — base du projet Android

Base de code Kotlin / Jetpack Compose / Room pour l'application de suivi du
temps décrite dans le cahier des charges. Ce projet s'ouvre directement dans
Android Studio (File > Open > sélectionner le dossier `TimeTracker`).

## Avant le premier build

1. **Vérifier les versions** dans `gradle/libs.versions.toml`. Certaines sont
   marquées `A VERIFIER` (KSP, lifecycle, navigation-compose) : ouvrez le lien
   indiqué en commentaire et reportez la dernière version stable. Les autres
   ont été vérifiées sur la documentation officielle au moment de la
   génération (septembre 2026) mais l'écosystème Kotlin/Compose évolue vite —
   recontrôlez-les si vous ouvrez ce projet plusieurs semaines après.
2. **Renommer le package / applicationId** : `com.example.timetracker` dans
   `app/build.gradle.kts` et dans l'arborescence
   `app/src/main/java/com/example/timetracker/` (Android Studio propose un
   refactoring automatique : clic droit sur le package > Refactor > Rename).
3. **Remplacer l'icône de lanceur** : les fichiers dans
   `app/src/main/res/mipmap-*` et `res/drawable/ic_launcher_*.xml` sont des
   placeholders générés automatiquement (un simple pictogramme d'horloge).
   Clic droit sur `res/` > New > Image Asset pour les remplacer proprement.
4. Ce projet n'a jamais été compilé dans cet environnement (pas d'accès
   réseau ni de SDK Android disponibles ici pour lancer Gradle) : la première
   synchronisation Gradle dans Android Studio peut faire remonter un
   ajustement mineur de version, notamment sur KSP qui doit correspondre
   exactement à la version de Kotlin.

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
- Synthèse par jour / par activité / par catégorie.
- Export et restauration JSON via le sélecteur de fichiers système.
- Heures en 24h, durées en heures et minutes.

## Pistes d'évolution volontairement laissées de côté

- Suppression de catégorie (bloquée tant que des sessions la référencent :
  contrainte `RESTRICT` sur la clé étrangère).
- Détection de doublons à l'import JSON (l'import est additif).
- Filtrage par période dans la synthèse (aujourd'hui/7 jours/tout).
