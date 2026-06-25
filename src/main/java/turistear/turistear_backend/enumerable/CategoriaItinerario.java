package turistear.turistear_backend.enumerable;

/**
 * Categorías con las que se etiqueta un itinerario del sistema.
 * Los valores coinciden con el CHECK constraint de la tabla `etiquetas`
 * en Supabase.
 */
public enum CategoriaItinerario {
    NATURALEZA,
    GASTRONOMIA,
    AVENTURA,
    CULTURA,
    NOCHE,
    COMPRA
}
