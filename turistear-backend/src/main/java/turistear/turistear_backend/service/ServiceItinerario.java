package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.sistema.ItinerarioSistemaDTO;
import turistear.turistear_backend.dto.sistema.ItinerarioSistemaResumenDTO;
import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.ItinerarioSistema;
import turistear.turistear_backend.repository.ItinerarioSistemaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Lógica de itinerarios precargados del sistema. Maneja Explorar,
 * Buscar por preferencias, Ranking, detalle y borrado (admin).
 * No toca copias del usuario — eso es responsabilidad de
 * {@link ServiceFavoritos}.
 */
@Service
@RequiredArgsConstructor
public class ServiceItinerario {

    private final ItinerarioSistemaRepository repository;

    /**
     * Explorar: devuelve la lista de itinerarios del sistema.
     *
     * @param categorias          filtro opcional. Si viene null o vacío, no filtra.
     * @param ordenarPorFavoritos si true, ordena por cantidad de veces guardado
     *                            como favorito. Se puede combinar con categorias.
     */
    @Transactional(readOnly = true)
    public List<ItinerarioSistemaResumenDTO> explorar(
            Set<CategoriaItinerario> categorias,
            boolean ordenarPorFavoritos) {

        boolean tieneCategorias = categorias != null && !categorias.isEmpty();

        List<ItinerarioSistema> resultado;
        if (ordenarPorFavoritos && tieneCategorias) {
            resultado = repository.findRankingByVecesGuardadoConCategorias(categorias);
        } else if (ordenarPorFavoritos) {
            resultado = repository.findRankingByVecesGuardado();
        } else if (tieneCategorias) {
            resultado = repository.findByCategoriaIn(categorias);
        } else {
            resultado = repository.findAll();
        }

        return resultado.stream()
                .map(ItinerarioSistemaResumenDTO::from)
                .toList();
    }

    /**
     * Buscar por preferencias: combina provincia (opcional), set de tags
     * (opcional) y rango de fechas (opcional, con solapamiento) en una
     * sola query. Si los tags vienen vacíos delega a la variante
     * "sin tags" del repo para no pasarle un IN vacío a Hibernate.
     */
    @Transactional(readOnly = true)
    public List<ItinerarioSistemaResumenDTO> buscarPorPreferencias(
            Provincia provincia,
            Set<CategoriaItinerario> tags,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        List<ItinerarioSistema> resultado = (tags == null || tags.isEmpty())
                ? repository.buscarPorPreferenciasSinTags(provincia, fechaInicio, fechaFin)
                : repository.buscarPorPreferenciasConTags(provincia, tags, fechaInicio, fechaFin);

        return resultado.stream()
                .map(ItinerarioSistemaResumenDTO::from)
                .toList();
    }

    /**
     * Devuelve el detalle completo de un itinerario del sistema —
     * incluyendo todos sus items para la pantalla de detalle.
     */
    @Transactional(readOnly = true)
    public ItinerarioSistemaDTO obtenerPorId(Long id) {
        ItinerarioSistema itinerario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Itinerario no encontrado: " + id));
        return ItinerarioSistemaDTO.from(itinerario);
    }

    /**
     * Borrado de un itinerario del sistema. CASCADE en Supabase se
     * encarga de borrar sus items, sus etiquetas asociadas y todas las
     * copias de usuario que lo referenciaban — junto a sus items.
     *
     * No incluye chequeo de rol admin todavía (la autorización por rol
     * queda pendiente para más adelante).
     */
    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Itinerario no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
