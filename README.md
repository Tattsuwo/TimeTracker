# Suivi du temps — application Android

Application de suivi du temps passé sur les activités de la journée : Kotlin,
Jetpack Compose (Material 3), Room. Ce projet s'ouvre directement dans
Android Studio (**File > Open** > sélectionner le dossier `TimeTracker`).

## Avant le premier build

1. **Renommer le package / applicationId** si besoin : `com.example.timetracker`
   dans `app/build.gradle.kts` et dans l'arborescence
   `app/src/main/java/com/example/timetracker/` (Android Studio propose un
   refactoring automatique : clic droit sur le package > Refactor > Rename).
2. **Remplacer l'icône de lanceur** : les fichiers dans
   `app/src/main/res/mipmap-*` et `res/drawable/ic_launcher_*.xml` sont des
   placeholders générés automatiquement (un simple pictogramme d'horloge).
   Clic droit sur `res/` > New > Image Asset pour les remplacer proprement.
3. **Vérifier les versions** dans `gradle/libs.versions.toml` si vous ouvrez ce
   projet plusieurs semaines/mois après sa dernière mise à jour : certaines
   sont marquées `A VERIFIER` (lifecycle, navigation-compose) — ouvrez le lien
   indiqué en commentaire et reportez la dernière version stable. L'écosystème
   Kotlin/Compose/AGP évolue vite ; voir aussi la section ci-dessous sur les
   pièges déjà rencontrés sur ce projet.

## Pièges déjà rencontrés (et corrigés) sur cette stack

Cette liste documente des changements récents de l'écosystème Android qui ont
cassé le build à un moment ou un autre pendant le développement — utile à
relire si une prochaine mise à jour de Kotlin/AGP/Compose fait à nouveau
dérailler la compilation :

- **Wrapper Gradle obligatoire** : `gradle/wrapper/gradle-wrapper.properties`
  fixe Gradle à la version 9.6.0, exigée par AGP 9.4.0 (`app/build.gradle.kts`).
  Sans ce fichier, Android Studio peut choisir une version de Gradle
  incompatible et planter avec une erreur interne du type
  "Unable to load class ... ProjectTypeBinding". Voir la table de
  compatibilité AGP/Gradle si la version d'AGP change :
  https://developer.android.com/build/releases/agp-9-4-0-release-notes
- **Kotlin "built-in" depuis AGP 9** : AGP compile désormais Kotlin lui-même et
  **rejette** le plugin `org.jetbrains.kotlin.android` appliqué séparément
  (retiré des deux `build.gradle.kts`). La version de Kotlin utilisée par ce
  compilateur intégré est forcée à 2.4.20 par un bloc `buildscript` en tête du
  `build.gradle.kts` racine, pour rester cohérente avec les plugins
  compose/serialization. Voir
  https://developer.android.com/build/migrate-to-built-in-kotlin
- **Schéma de version de KSP changé** : depuis KSP 2.3.0, sa version n'est
  plus calée sur celle de Kotlin (l'ancien format `<kotlin>-<build>` n'existe
  plus). `gradle/libs.versions.toml` utilise `ksp = "2.3.11"` ; vérifier
  https://github.com/google/ksp/releases si le build s'y réfère à nouveau.
- **compileSdk/targetSdk 37** : Compose 1.12.1 exige de compiler contre l'API
  37 (Android 17, stable depuis juin 2026) — sinon `checkDebugAarMetadata`
  échoue au build. Voir `app/build.gradle.kts`.
- **Pas de `material-icons-extended`** : cette bibliothèque d'icônes n'existe
  pas à la version qu'on pourrait attendre par analogie avec les autres
  modules Compose (cycle de publication indépendant). Les 4 icônes de la barre
  de navigation sont donc dessinées à la main avec `Canvas` dans
  `ui/common/NavIcons.kt`, sans dépendance externe.
- **`TabRow` déprécié** : remplacé par `SecondaryTabRow` (Material3 1.4.0+),
  utilisé dans `ui/summary/SummaryScreen.kt`.

## Architecture en un coup d'œil

- `data/` : entités Room (`Category` avec sa couleur, `Session`,
  `ActiveTimer`), DAO, `AppDatabase` (version 2, avec une vraie migration
  1→2 pour l'ajout de la couleur — pas de réinitialisation destructrice).
  Voir les commentaires de `Session.kt` et `ActiveTimer.kt` pour le choix de
  conception clé : le chrono "en cours" est une table séparée de "sessions",
  qui ne contient elle que des sessions terminées.
- `repository/TimeTrackerRepository.kt` : point d'entrée unique pour les
  ViewModels (chrono unique, sessions interrompues, suppression de catégorie
  en cascade dans une transaction, export/import JSON).
- `service/TimerForegroundService.kt` : notification persistante pendant le
  chrono actif (foreground service de type `specialUse`, seul type adapté à
  un chronomètre depuis Android 14).
- `backup/` : modèles sérialisables (avec la couleur des catégories) +
  lecture/écriture via le Storage Access Framework (aucune permission de
  stockage nécessaire).
- `ui/` : un dossier par écran (`timer`, `history`, `summary`, `settings`),
  plus `theme/`, `navigation/` et `common/` (composants partagés : sélecteur
  de catégorie, sélecteur date/heure, palette et pastilles de couleur, icônes
  de navigation dessinées à la main). Pas de Hilt : `ui/ViewModelFactory.kt`
  construit les ViewModels à la main à partir du repository exposé par
  `TimeTrackerApplication`.

## Fonctionnalités couvertes

- Démarrer / arrêter un chrono, un seul actif à la fois (contrainte au
  niveau de la base de données, pas seulement côté UI).
- Notification persistante + permission `POST_NOTIFICATIONS` (Android 13+).
- Détection d'une session laissée ouverte après un redémarrage du téléphone,
  avec clôture manuelle par l'utilisateur (pas de reprise automatique).
- Nommage de la session à l'arrêt (nom, description, catégorie), catégories
  par défaut + création à la volée.
- Catégories : couleur personnalisable (palette fixe de 10 teintes), ajout
  depuis les Réglages ou à la volée pendant l'arrêt d'un chrono, modification,
  suppression en cascade de ses sessions (avec confirmation et décompte des
  sessions concernées, et interdiction de supprimer la toute dernière
  catégorie restante). La couleur est visible dans le sélecteur de catégorie,
  l'historique (puce + fond de carte légèrement teinté) et la synthèse.
- Historique groupé par jour avec total par jour, sessions modifiables ou
  supprimables. Chaque jour est repliable individuellement (clic sur l'en-tête),
  avec un bouton "Tout fermer / Tout ouvrir" pour basculer tous les jours
  d'un coup.
- Synthèse par jour / par activité / par catégorie.
- Export et import JSON via le sélecteur de fichiers système : l'import
  **remplace entièrement** les données existantes (sessions + catégories) par
  le contenu du fichier, avec confirmation avant de le lancer (pas de fusion :
  on repart proprement sur la version importée).
- Heures en 24h, durées en heures et minutes.

## Publier une version (APK + GitHub Release)

1. **Générer l'APK** : soit un APK de debug rapide
   (**Build > Build Bundle(s)/APK(s) > Build APK(s)**, suffisant pour un usage
   personnel), soit un APK signé avec un keystore personnel
   (**Build > Generate Signed App Bundle / APK**, recommandé si vous comptez
   faire des mises à jour régulières — Android exige la même clé de
   signature d'une version à l'autre pour permettre une mise à jour sans
   désinstallation). Le fichier peut être renommé librement après génération.
2. **Pousser le code** sur GitHub (GitHub Desktop ou `git push`) — le
   `.gitignore` du projet exclut déjà `build/`, `.gradle/`, `local.properties`
   et les fichiers de keystore (`*.jks`) du dépôt.
3. **Créer une Release** sur la page GitHub du dépôt (onglet **Releases** >
   **Draft a new release**), avec un tag (ex. `v1.0.0`) et l'APK glissé dans
   la zone "Attach binaries".

## Pistes d'évolution volontairement laissées de côté

- Filtrage par période dans la synthèse (aujourd'hui/7 jours/tout).
- Sélecteur de couleur libre (roue HSV) plutôt que la palette fixe actuelle —
  choix assumé pour rester simple et sans dépendance externe.
