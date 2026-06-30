package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.AsistenciaItemGrupo;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaItemGrupoRepository extends JpaRepository<AsistenciaItemGrupo, Long> {

    /** Upsert de asistencia: buscar existente antes de crear (mismo patrón que Voto). */
    Optional<AsistenciaItemGrupo> findByItem_IdAndUsuario_IdUsuario(Long idItem, Long idUsuario);

    /** Asistencias registradas para un único item (con el usuario ya cargado). */
    @EntityGraph(attributePaths = "usuario")
    List<AsistenciaItemGrupo> findByItem_Id(Long idItem);

    /**
     * Trae TODAS las asistencias de TODOS los items de un itinerario de
     * grupo en una sola query (con el usuario ya cargado, evita N+1 al armar
     * el nombre/foto de cada fila). El service las agrupa en memoria por
     * item al armar el DTO, en vez de consultar por item individualmente.
     */
    @EntityGraph(attributePaths = "usuario")
    List<AsistenciaItemGrupo> findByItem_ItinerarioGrupo_IdItinerarioGrupo(Long idItinerarioGrupo);
}
