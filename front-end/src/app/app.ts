import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookingService } from './services/booking.service';
import { AuthService } from './services/auth.service';
import { HttpClient } from '@angular/common/http';
import { Booking } from './models/coworkia.models';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
  private readonly apiUrl = environment.apiUrl;
  private readonly pageSize = 8;
  // UI State
  bookings = signal<Booking[]>([]);
  isLoggedIn = signal<boolean>(false);
  authMode = signal<'login' | 'register'>('login');
  isManagerView = signal<boolean>(false);
  showModal = signal<boolean>(false);
  showEditModal = signal<boolean>(false);
  showProfileModal = signal<boolean>(false);
  toastMessage = signal<string | null>(null);
  zones = signal<any[]>([]);
  occupationHistory = signal<any | null>(null);
  userProfile = signal<any>(null);

  // Computed Stats for Manager
  managerStats = computed(() => {
    const z = this.zones();
    if (z.length === 0) return { occupation: 0, free: 0, revenue: 0 };
    const totalCapacity = z.reduce((sum, zone) => sum + zone.capacity, 0);
    const totalOccupied = z.reduce((sum, zone) => sum + zone.occupied, 0);
    const occupation = Math.round((totalOccupied / totalCapacity) * 100);
    
    // Revenue simulation (real sum of today's confirmed bookings)
    const todayStr = new Date().toISOString().split('T')[0];
    const revenue = this.bookings()
      .filter(b => b.status === 'CONFIRMED' && b.startTime.toString().startsWith(todayStr))
      .reduce((sum, b) => sum + (b.price || 0), 0);

    return { occupation, free: totalCapacity - totalOccupied, revenue };
  });

  // Computed Stats for User
  nextBooking = computed(() => {
    const confirmed = this.bookings().filter(b => b.status === 'CONFIRMED' && new Date(b.startTime) > new Date());
    return confirmed.sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())[0];
  });

  // Sorting
  sortColumn = signal<string>('date');
  sortDirection = signal<'asc' | 'desc'>('desc');
  managerFilterUser = signal<string>('');
  managerFilterStatus = signal<'ALL' | 'CONFIRMED' | 'CANCELLED'>('ALL');
  managerFilterFromDate = signal<string>('');
  managerFilterToDate = signal<string>('');
  historyFromDate = signal<string>('');
  historyToDate = signal<string>('');
  currentPage = signal<number>(1);

  filteredBookings = computed(() => {
    let list = [...this.bookings()];

    if (!this.isManagerView()) {
      return list;
    }

    const userTerm = this.managerFilterUser().trim().toLowerCase();
    if (userTerm) {
      list = list.filter((b) => {
        const firstName = b.user?.firstName?.toLowerCase() || '';
        const lastName = b.user?.lastName?.toLowerCase() || '';
        const email = b.user?.email?.toLowerCase() || '';
        return `${firstName} ${lastName} ${email}`.includes(userTerm);
      });
    }

    if (this.managerFilterStatus() !== 'ALL') {
      list = list.filter((b) => b.status === this.managerFilterStatus());
    }

    if (this.managerFilterFromDate()) {
      const from = new Date(`${this.managerFilterFromDate()}T00:00:00`);
      list = list.filter((b) => new Date(b.startTime) >= from);
    }

    if (this.managerFilterToDate()) {
      const to = new Date(`${this.managerFilterToDate()}T23:59:59`);
      list = list.filter((b) => new Date(b.startTime) <= to);
    }

    return list;
  });

  sortedBookings = computed(() => {
    const list = [...this.filteredBookings()];
    return list.sort((a, b) => {
      let valA: any = '';
      let valB: any = '';

      switch (this.sortColumn()) {
        case 'desk':
          valA = a.desk?.code;
          valB = b.desk?.code;
          break;
        case 'zone':
          valA = a.desk?.zone?.name;
          valB = b.desk?.zone?.name;
          break;
        case 'date':
          valA = new Date(a.startTime).getTime();
          valB = new Date(b.startTime).getTime();
          break;
        case 'price':
          valA = a.price;
          valB = b.price;
          break;
        case 'status':
          valA = a.status;
          valB = b.status;
          break;
      }

      if (valA < valB) return this.sortDirection() === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection() === 'asc' ? 1 : -1;
      return 0;
    });
  });

  totalPages = computed(() => {
    return Math.max(1, Math.ceil(this.sortedBookings().length / this.pageSize));
  });

  paginatedBookings = computed(() => {
    const page = this.currentPage();
    const start = (page - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.sortedBookings().slice(start, end);
  });

  loginData = { email: 'user@coworkia.com', password: 'password' };
  registerData = { firstName: '', lastName: '', email: '', password: '', confirmPassword: '' };
  profileForm = { firstName: '', lastName: '' };
  userName = computed(() => this.userProfile() ? this.userProfile().firstName : 'Utilisateur');
  userId = computed(() => this.userProfile() ? this.userProfile().id : null);
  userPoints = computed(() => this.userProfile() ? this.userProfile().fidelityPoints : 0);

  sortBy(column: string) {
    if (this.sortColumn() === column) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortColumn.set(column);
      this.sortDirection.set('asc');
    }
    this.currentPage.set(1);
  }

  showToast(message: string) {
    this.toastMessage.set(message);
    setTimeout(() => this.toastMessage.set(null), 3000);
  }

  newBooking = {
    deskId: 1,
    date: new Date().toISOString().split('T')[0],
    time: '09:00',
    endTime: '18:00'
  };

  editingBookingId: number | null = null;
  editBooking = {
    deskId: 1,
    date: new Date().toISOString().split('T')[0],
    time: '09:00',
    endTime: '18:00'
  };

  minDate = new Date().toISOString().split('T')[0];

  availableDesks: any[] = [];

  constructor(private http: HttpClient, private bookingService: BookingService, private authService: AuthService) { }

  ngOnInit() {
    this.initializeHistoryRange();
    if (this.authService.isAuthenticated()) {
      this.isLoggedIn.set(true);
      this.loadBookings();
    }
  }

  loadAvailability() {
    this.http.get<any[]>(`${this.apiUrl}/api/zones/availability`).subscribe(data => {
      this.zones.set(data);
    });
  }

  loadDesks() {
    this.bookingService.getDesks().subscribe(data => {
      this.availableDesks = data;
      if (this.availableDesks.length > 0) {
        this.newBooking.deskId = this.availableDesks[0].id;
      }
    });
  }

  loadBookings() {
    this.loadAvailability();
    this.loadDesks();
    
    this.http.get(`${this.apiUrl}/api/users/me`, { withCredentials: true })
      .subscribe({
        next: (user: any) => {
          this.userProfile.set(user);
          const role = user.role;
          this.isManagerView.set(role === 'ROLE_MANAGER' || role === 'ROLE_ADMIN');
          if (this.isManagerView()) {
            this.loadOccupationHistory();
          }
          this.refreshBookings();
          this.isLoggedIn.set(true);
        },
        error: (err) => {
          console.error('Session expirée ou invalide :', err);
          this.authService.clearCredentials();
          this.isLoggedIn.set(false);
          this.showToast('⚠️ Session expirée, veuillez vous reconnecter.');
        }
      });
  }

  refreshBookings() {
    if (this.isManagerView()) {
      this.bookingService.getAllBookings().subscribe(data => this.bookings.set(data));
    } else {
      const uid = this.userProfile()?.id;
      if (uid) {
        this.bookingService.getUserBookings(uid).subscribe(data => this.bookings.set(data));
      }
    }
  }

  handleLogin() {
    if (this.loginData.email && this.loginData.password) {
      this.http.post<any>(`${this.apiUrl}/api/auth/login`, {
        email: this.loginData.email,
        password: this.loginData.password
      }, { withCredentials: true }).subscribe({
        next: (res) => {
          // Le serveur a posé le cookie httpOnly - on stocke juste l'état UI
          this.authService.markAuthenticated();
          this.loadBookings();
        },
        error: () => {
          this.showToast('❌ Email ou mot de passe invalide.');
        }
      });
    } else {
      this.showToast('⚠️ Veuillez remplir les identifiants.');
    }
  }

  switchAuthMode(mode: 'login' | 'register') {
    this.authMode.set(mode);
  }

  handleRegister() {
    const { firstName, lastName, email, password, confirmPassword } = this.registerData;
    if (!email || !password) {
      this.showToast('⚠️ Email et mot de passe sont obligatoires.');
      return;
    }

    if (password !== confirmPassword) {
      this.showToast('❌ Les mots de passe ne correspondent pas.');
      return;
    }

    this.http.post<any>(`${this.apiUrl}/api/auth/register`, {
      firstName,
      lastName,
      email,
      password
    }).subscribe({
      next: () => {
        this.showToast('✅ Compte créé, vous pouvez vous connecter.');
        this.loginData.email = email;
        this.loginData.password = password;
        this.registerData = { firstName: '', lastName: '', email: '', password: '', confirmPassword: '' };
        this.authMode.set('login');
      },
      error: (err) => {
        const message = err?.error?.error || 'Erreur lors de la création du compte.';
        this.showToast(`❌ ${message}`);
      }
    });
  }

  handleLogout() {
    // Supprime le cookie httpOnly côté serveur
    this.http.post(`${this.apiUrl}/api/auth/logout`, {}, { withCredentials: true }).subscribe();
    this.authService.clearCredentials();
    this.userProfile.set(null);
    this.bookings.set([]);
    this.isLoggedIn.set(false);
  }

  toggleView(isManager: boolean) {
    this.isManagerView.set(isManager);
    this.currentPage.set(1);
    if (isManager) {
      this.loadOccupationHistory();
    }
    this.refreshBookings();
  }

  openBookingModal() {
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
  }

  openEditModal(booking: Booking) {
    if (!booking.id) {
      this.showToast('❌ Réservation invalide.');
      return;
    }

    const start = new Date(booking.startTime);
    const end = new Date(booking.endTime);

    this.editingBookingId = booking.id;
    this.editBooking = {
      deskId: booking.desk?.id || this.availableDesks[0]?.id || 1,
      date: this.formatDateForInput(start),
      time: this.formatTimeForInput(start),
      endTime: this.formatTimeForInput(end)
    };
    this.showEditModal.set(true);
  }

  closeEditModal() {
    this.showEditModal.set(false);
    this.editingBookingId = null;
  }

  openProfileModal() {
    const profile = this.userProfile();
    this.profileForm = {
      firstName: profile?.firstName || '',
      lastName: profile?.lastName || ''
    };
    this.showProfileModal.set(true);
  }

  closeProfileModal() {
    this.showProfileModal.set(false);
  }

  confirmReservation() {
    const startHourNum = parseInt(this.newBooking.time.split(':')[0]);
    const endHourNum = parseInt(this.newBooking.endTime.split(':')[0]);
    const duration = Math.max(1, endHourNum - startHourNum);
    const calculatedPrice = duration * 10.0; // 10€ par heure

    const startTime = `${this.newBooking.date}T${this.newBooking.time}:00`;
    const endTime = `${this.newBooking.date}T${this.newBooking.endTime}:00`;

    if (new Date(startTime) < new Date()) {
      this.showToast("❌ Impossible de réserver dans le passé !");
      return;
    }

    if (new Date(endTime) <= new Date(startTime)) {
      this.showToast("❌ L'heure de fin doit être après l'heure de début !");
      return;
    }

    const bookingData: any = {
      user: { id: this.userId() },
      desk: { id: Number(this.newBooking.deskId) },
      startTime: startTime,
      endTime: endTime,
      status: 'CONFIRMED',
      price: calculatedPrice
    };

    this.bookingService.createBooking(bookingData).subscribe({
      next: () => {
        this.loadBookings();
        this.closeModal();
        this.showToast("✅ Réservation confirmée !");
      },
      error: (err) => this.showToast("❌ Erreur : " + err.message)
    });
  }

  confirmBookingUpdate() {
    if (!this.editingBookingId) {
      this.showToast('❌ Réservation introuvable.');
      return;
    }

    const startTime = `${this.editBooking.date}T${this.editBooking.time}:00`;
    const endTime = `${this.editBooking.date}T${this.editBooking.endTime}:00`;

    if (new Date(endTime) <= new Date(startTime)) {
      this.showToast("❌ L'heure de fin doit être après l'heure de début !");
      return;
    }

    const bookingData = {
      desk: { id: Number(this.editBooking.deskId) },
      startTime,
      endTime
    };

    this.bookingService.updateBooking(this.editingBookingId, bookingData).subscribe({
      next: () => {
        this.showToast('✅ Réservation modifiée avec succès !');
        this.closeEditModal();
        this.loadBookings();
      },
      error: (err) => {
        const message = err?.error || err?.error?.message || 'Erreur lors de la modification.';
        this.showToast(`❌ ${message}`);
      }
    });
  }

  saveProfile() {
    this.http.put<any>(`${this.apiUrl}/api/users/me`, this.profileForm, { withCredentials: true }).subscribe({
      next: (updated) => {
        this.userProfile.set(updated);
        this.showProfileModal.set(false);
        this.showToast('✅ Profil mis à jour avec succès !');
      },
      error: (err) => {
        const message = err?.error?.error || 'Erreur lors de la mise à jour du profil.';
        this.showToast(`❌ ${message}`);
      }
    });
  }

  downloadInvoice(id: number | undefined) {
    this.showToast(`ℹ️ Téléchargement de facture bientôt disponible (réservation #${id}).`);
  }

  cancelReservation(id: number | undefined) {
    if (id && confirm("Êtes-vous sûr de vouloir annuler cette réservation ?")) {
      this.bookingService.cancelBooking(id).subscribe({
        next: () => {
          this.showToast('✅ Réservation annulée avec succès !');
          this.loadBookings();
        },
        error: (err) => {
          this.showToast("❌ Erreur lors de l'annulation : " + err.message);
        }
      });
    }
  }

  onManagerFiltersChange() {
    this.currentPage.set(1);
  }

  resetManagerFilters() {
    this.managerFilterUser.set('');
    this.managerFilterStatus.set('ALL');
    this.managerFilterFromDate.set('');
    this.managerFilterToDate.set('');
    this.currentPage.set(1);
  }

  previousPage() {
    this.currentPage.update((page) => Math.max(1, page - 1));
  }

  nextPage() {
    this.currentPage.update((page) => Math.min(this.totalPages(), page + 1));
  }

  loadOccupationHistory() {
    if (!this.historyFromDate() || !this.historyToDate()) {
      this.initializeHistoryRange();
    }

    this.http.get<any>(
      `${this.apiUrl}/api/zones/history?from=${this.historyFromDate()}&to=${this.historyToDate()}`,
      { withCredentials: true }
    ).subscribe({
      next: (data) => this.occupationHistory.set(data),
      error: (err) => {
        const message = err?.error || err?.error?.message || "Impossible de charger l'historique d'occupation.";
        this.showToast(`❌ ${message}`);
      }
    });
  }

  private initializeHistoryRange() {
    const today = new Date();
    const from = new Date();
    from.setDate(today.getDate() - 7);
    this.historyFromDate.set(this.formatDateForInput(from));
    this.historyToDate.set(this.formatDateForInput(today));
  }

  private formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private formatTimeForInput(date: Date): string {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }
}
