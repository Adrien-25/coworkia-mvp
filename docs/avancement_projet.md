# Avancement du projet Coworkia MVP

Ce document centralise le suivi fonctionnel, les decisions de perimetre MVP et l'historique des evolutions majeures.

## 1) Perimetre MVP (site pilote Paris Republique)

Fonctionnalites ciblees :
- Authentification JWT (cookie httpOnly) et gestion des roles.
- Consultation des zones et reservation en temps reel.
- Suivi d'occupation (temps reel + historique).
- Accessibilite de base integree dans l'interface.

Hors scope explicite :
- Tests automatises obligatoires.
- Docker / containerisation.
- Multi-sites complets, facturation avancee, reporting avance.

## 2) Etat d'avancement

- Core backend Spring en couches (`controller -> service -> repository`) operationnel.
- Front Angular operationnel avec parcours principal login -> reservation -> suivi.
- Scripts SQL fournis : `schema.sql` + `data.sql` (jeu de donnees de demonstration).
- README aligne sur la configuration runtime et les variables d'environnement.

## 3) Cadre de versioning (projet)

- Branche principale : `main`.
- Commits atomiques et lisibles (un objectif clair par commit).
- Message de commit en francais, oriente impact/objectif.
- Commits de suivi alignes avec les evolutions fonctionnelles et techniques ci-dessous.

## 4) Historique des evolutions (versionning fonctionnel)

### 2026-04-15 - `62a90b5`
- Publication de la base initiale du projet Coworkia MVP (structure backend/frontend et premiers elements fonctionnels).

### 2026-04-15 - `4441ab5`
- Ajout de `back-end/.env.example` pour versionner proprement le gabarit des variables d'environnement sans exposer de secrets.

### 2026-04-22 - `700e369`
- Stabilisation de l'authentification (JWT et configuration associee).
- Ajustement de la vue manager pour garantir la coherence du flux de reservations.
- Alignement backend/frontend sur les variables d'environnement du projet.

### 2026-04-22 - `496a3cf`
- Finalisation du MVP pour le rendu : accessibilite des toasts, personnalisation titre/favicon, coherence README et suivi projet.
