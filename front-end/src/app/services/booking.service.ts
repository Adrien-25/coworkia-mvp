import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking } from '../models/coworkia.models';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class BookingService {
    private readonly apiUrl = `${environment.apiUrl}/api/bookings`;
    private readonly desksUrl = `${environment.apiUrl}/api/desks`;

    constructor(private http: HttpClient, private authService: AuthService) { }

    private getOptions() {
        const authHeader = this.authService.getAuthHeader();
        return {
            headers: new HttpHeaders({
                ...authHeader,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }),
            withCredentials: true // Obligatoire si le backend fait .setAllowCredentials(true)
        };
    }

    getUserBookings(userId: number): Observable<Booking[]> {
        return this.http.get<Booking[]>(`${this.apiUrl}/user/${userId}`, this.getOptions());
    }

    getAllBookings(): Observable<Booking[]> {
        return this.http.get<Booking[]>(`${this.apiUrl}/all`, this.getOptions());
    }

    createBooking(booking: any): Observable<Booking> {
        return this.http.post<Booking>(this.apiUrl, booking, this.getOptions());
    }

    updateBooking(id: number, booking: any): Observable<Booking> {
        return this.http.put<Booking>(`${this.apiUrl}/${id}`, booking, this.getOptions());
    }

    getDesks(): Observable<any[]> {
        return this.http.get<any[]>(this.desksUrl, this.getOptions());
    }

    cancelBooking(id: number): Observable<any> {
        return this.http.put(`${this.apiUrl}/${id}/cancel`, {}, this.getOptions());
    }
}
