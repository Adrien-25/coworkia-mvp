export interface Site {
    id: number;
    name: string;
    address: string;
    city: string;
}

export interface Zone {
    id: number;
    name: string;
    type: string;
    capacity: number;
    site: Site;
}

export interface Desk {
    id: number;
    code: string;
    zone: Zone;
}

export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    fidelityPoints: number;
}

export interface Booking {
    id?: number;
    user?: User;
    desk: Desk;
    startTime: string;
    endTime: string;
    status?: string;
    price: number;
    createdAt?: string;
}
