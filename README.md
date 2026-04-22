# Coworkia MVP - Outil national de gestion

Projet réalisé dans le cadre de l'exercice "Etude de cas Coworkia - MVP".

## Présentation
Coworkia centralise la gestion d'un réseau de 18 espaces de coworking. Ce dépôt implémente le MVP pour le site pilote **Paris République**.

### Fonctionnalités principales
- Authentification sécurisée (JWT + cookie httpOnly) et gestion des rôles.
- Consultation des zones et des postes.
- Réservation en temps réel (création, modification, annulation).
- Dashboard manager (occupation, filtres, pagination, indicateurs clés).
- Facturation automatique liée aux réservations.

## Stack technique
- Backend : Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA.
- Frontend : Angular (standalone), TypeScript, CSS.
- Base de données : PostgreSQL.

## Architecture et structure
- `back-end` : API Spring Boot.
  - `controller -> service -> repository`
  - scripts SQL : `back-end/scripts/schema.sql` et `back-end/scripts/data.sql`
- `front-end` : application Angular.
- `docs` : suivi d'avancement et documentation projet.

## Prérequis
- Java 17+
- Maven 3.9+ (ou wrapper Maven)
- Node.js 20+ et npm
- PostgreSQL 14+

Vérification rapide :
```bash
java -version
mvn -v
node -v
npm -v
```

## Installation et lancement

### 0) Récupération du projet
```bash
git clone https://github.com/Adrien-25/coworkia-mvp.git
cd coworkia-mvp
```

### 1) Base de données
Exécuter les scripts dans cet ordre :
1. `back-end/scripts/schema.sql`
2. `back-end/scripts/data.sql`

### 2) Backend
```bash
cd back-end
./mvnw spring-boot:run
```
API disponible sur `http://localhost:8080`.

### 3) Frontend
```bash
cd front-end
npm install
ng serve
```
Application disponible sur `http://localhost:4200`.

## Variables d'environnement (backend)

| Variable | Obligatoire | Exemple |
| :--- | :---: | :--- |
| `DB_URL_COWORKIA` | Oui (cible prod) | `jdbc:postgresql://localhost:5432/coworkia` |
| `DB_USERNAME` | Oui (cible prod) | `postgres` |
| `DB_PASSWORD` | Oui (cible prod) | `votre_mot_de_passe` |
| `JWT_SECRET` | Oui (cible prod) | `une_cle_longue_et_forte_au_moins_32_chars` |
| `JWT_EXPIRATION_MS` | Non | `43200000` |

Note JWT : en production, fournir un `JWT_SECRET` d’au moins 32 caractères ; en local, si le secret est trop court, il est dérivé en SHA-256 afin d’obtenir une clé compatible HS256.

Etat actuel :
- Le projet fonctionne encore en local sans variables grâce à des fallbacks.
- Objectif de fin de sprint : supprimer les fallbacks sensibles.
- Le fichier `back-end/.env.example` est un gabarit documentaire des variables attendues ; l'application lit les variables d'environnement du système (session shell / OS), pas un fichier `.env` automatiquement.

Exemple PowerShell (session courante) :
```powershell
$env:DB_URL_COWORKIA="jdbc:postgresql://localhost:5432/coworkia"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="votre_mot_de_passe"
$env:JWT_SECRET="une_cle_longue_et_forte_au_moins_32_chars"
$env:JWT_EXPIRATION_MS="43200000"
```

## Comptes de démonstration
- `user@coworkia.com / password` (ROLE_USER)
- `manager@coworkia.com / password` (ROLE_MANAGER)
- `admin@coworkia.com / password` (ROLE_ADMIN)

## Accessibilité (WCAG/RGAA)
L'interface applique les principes WCAG/RGAA sur le périmètre MVP.

### Mesures implémentées
- Structure HTML sémantique (`main`, `header`, `nav`, `section`).
- Navigation clavier fonctionnelle avec focus visible.
- Lien d'évitement ("Passer au contenu principal").
- Champs de formulaires avec `label`.
- Messages d'état accessibles (`aria-live`, `role="status"`, `role="alert"`).
- Actions critiques avec `aria-label`.
- Contraste visuel cible >= 4.5:1.

### Check-list de vérification
- [x] Parcours clavier complet sur login, réservation, modification, annulation.
- [x] Labels de formulaires présents.
- [x] Focus visible sur les éléments interactifs.
- [x] Feedbacks d'erreur/succès accessibles.
- [x] Zones principales identifiables sémantiquement.
- [x] Skip-link disponible.

### Limites connues
- Validation lecteur d'écran (NVDA/VoiceOver) à compléter.
- Audit automatisé (Lighthouse/axe) à intégrer.
- Vérification formelle zoom 200% à terminer.

## Sécurité (A10)
- Mots de passe hashés avec BCrypt.
- Authentification JWT stateless.
- Cookie httpOnly utilisé pour le token.
- Contrôle d'accès par rôle (USER, MANAGER, ADMIN).
- Gestion des erreurs HTTP (401/403).

## Troubleshooting
- Port `8080` occupé :
  - `Get-NetTCPConnection -LocalPort 8080 -State Listen`
  - `taskkill /PID <pid> /T /F`
- Port `4200` occupé :
  - arrêter le `ng serve` en cours (`Ctrl+C`) puis relancer.
- Erreur DB :
  - vérifier URL/identifiants PostgreSQL et exécution de `schema.sql` + `data.sql`.
- Erreur d'authentification :
  - se déconnecter/reconnecter pour renouveler le cookie.

## Suivi du projet
Le suivi des fonctionnalités et de la roadmap est maintenu dans `docs/avancement_projet.md`.
