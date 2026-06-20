package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.model.Etiqueta;

import java.util.Set;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    /**
     * Resuelve un conjunto de categorías (enum) a sus entidades Etiqueta.
     * Usado al crear un itinerario propio para asociar las etiquetas
     * elegidas por el usuario sin insertar duplicados en la tabla.
     */
    Set<Etiqueta> findByNombreIn(Set<CategoriaItinerario> nombres);
}
