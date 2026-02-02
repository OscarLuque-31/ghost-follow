package com.oscarluque.ghostfollowcore.constants;

public enum SubscriptionStatus {
    ACTIVE,       // Pagado y disfrutando
    CANCELED,     // Canceló la renovación, pero aún le quedan días pagados
    EXPIRED,      // Se acabó el tiempo y no pagó
    PAST_DUE,     // Stripe intentó cobrar y falló (tarjeta caducada)
    NONE          // Nunca ha pagado (Free)
}
