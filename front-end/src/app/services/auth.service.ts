import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly SESSION_FLAG = 'coworkia_session';

    // Le token JWT est stocké dans un cookie httpOnly côté navigateur
    // (inaccessible depuis JavaScript → protection XSS/OWASP)
    // Angular n'a pas besoin de le gérer manuellement.

    markAuthenticated() {
        // On utilise localStorage pour partager l'état de connexion entre les onglets
        localStorage.setItem(this.SESSION_FLAG, 'true');
    }

    clearCredentials() {
        localStorage.removeItem(this.SESSION_FLAG);
    }

    isAuthenticated(): boolean {
        return localStorage.getItem(this.SESSION_FLAG) === 'true';
    }

    // Plus besoin d'envoyer un header Authorization manuellement :
    // le cookie jwt_token est envoyé automatiquement par le navigateur
    // sur toutes les requêtes grâce à withCredentials: true
    getAuthHeader(): { [header: string]: string } {
        return {}; // Cookie envoyé automatiquement par le navigateur
    }
}
