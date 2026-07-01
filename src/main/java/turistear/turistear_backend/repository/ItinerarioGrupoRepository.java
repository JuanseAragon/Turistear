package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.ItinerarioGrupo;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItinerarioGrupoRepository extends JpaRepository<ItinerarioGrupo, Long> {

    /**
     * Itinerarios de grupo ordenados por fecha de creación descendente. El
     * service toma el primero (el más reciente) — un grupo tiene un solo
     * itinerario "vigente" a la vez. Devolver {@code List} en vez de
     * {@code Optional} deja la puerta abierta a un futuro historial sin
     * tener que tocar este repositorio.
     * <p>
     * {@code items.propuestoPor} y {@code creador} son relaciones ManyToOne
     * (no colecciones): combinarlas en el mismo {@code @EntityGraph} junto a
     * la colección {@code items} es seguro y evita el N+1 al armar el DTO,
     * sin el producto cartesiano que generaría combinar dos colecciones a la
     * vez (mismo patrón ya usado en EncuestaRepository).
     */
    @EntityGraph(attributePaths = {"items", "items.propuestoPor", "creador"})
    List<ItinerarioGrupo> findByGrupo_IdGrupoOrderByFechaCreacionDesc(Long idGrupo);

    /** Detalle puntual, validando que el itinerario pertenezca al grupo del path. */
    @EntityGraph(attributePaths = {"items", "items.propuestoPor", "creador"})
    Optional<ItinerarioGrupo> findByIdItinerarioGrupoAndGrupo_IdGrupo(Long idItinerarioGrupo, Long idGrupo);

    /** Idempotencia: evita crear dos itinerarios de grupo para la misma encuesta. */
    boolean existsByEncuestaOrigen_IdEncuesta(Long idEncuesta);

    /**
     * Borra los itinerarios de grupo (y en cascada sus items/asistencias) de un
     * grupo. Se llama al eliminar el grupo, antes de borrar sus encuestas, para
     * no dejar itinerarios apuntando a encuestas que están por eliminarse.
     */
    void deleteByGrupo_IdGrupo(Long idGrupo);
}
