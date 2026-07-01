package turistear.turistear_backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import turistear.turistear_backend.dto.grupo.ActualizarFechaItinerarioGrupoRequest;
import turistear.turistear_backend.dto.grupo.ItinerarioGrupoDTO;
import turistear.turistear_backend.dto.grupo.ItemItinerarioGrupoDTO;
import turistear.turistear_backend.dto.grupo.ItemItinerarioGrupoRequest;
import turistear.turistear_backend.enumerable.EstadoItemItinerarioGrupo;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.enumerable.RolGrupo;
import turistear.turistear_backend.exception.BadRequestException;
import turistear.turistear_backend.exception.ForbiddenException;
import turistear.turistear_backend.model.*;
import turistear.turistear_backend.repository.AsistenciaItemGrupoRepository;
import turistear.turistear_backend.repository.ItemItinerarioGrupoRepository;
import turistear.turistear_backend.repository.ItinerarioGrupoRepository;
import turistear.turistear_backend.repository.MiembroGrupoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceItinerarioGrupoTest {

    @Mock private ItinerarioGrupoRepository itinerarioGrupoRepo;
    @Mock private ItemItinerarioGrupoRepository itemRepo;
    @Mock private AsistenciaItemGrupoRepository asistenciaRepo;
    @Mock private MiembroGrupoRepository miembroRepo;

    @InjectMocks
    private ServiceItinerarioGrupo service;

    /* ---------------- proponer ---------------- */

    @Test
    void proponerComoLider_quedaConfirmado() {
        Usuario lider = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro(grupo, lider, RolGrupo.CREADOR)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));

        ItemItinerarioGrupoDTO dto = service.proponerItem(1L, 10L, req("Trekking", 1));

        assertEquals(EstadoItemItinerarioGrupo.CONFIRMADO, dto.estado());
        assertEquals(1L, dto.propuestoPorId());
    }

    @Test
    void proponerComoMiembro_quedaPropuesto() {
        Usuario lider = usuario(1L, "Ana");
        Usuario miembro = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, miembro, RolGrupo.MIEMBRO)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));

        ItemItinerarioGrupoDTO dto = service.proponerItem(2L, 10L, req("Kayak", 1));

        assertEquals(EstadoItemItinerarioGrupo.PROPUESTO, dto.estado());
        assertEquals(2L, dto.propuestoPorId());
    }

    /* ---------------- actualizar fecha inicio ---------------- */

    @Test
    void actualizarFechaInicio_creadorCambiaFechaYRecalculaFin() {
        Usuario lider = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);
        LocalDate nuevaFecha = LocalDate.now().plusDays(5);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro(grupo, lider, RolGrupo.CREADOR)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));
        when(itinerarioGrupoRepo.save(itinerario)).thenReturn(itinerario);

        ItinerarioGrupoDTO dto = service.actualizarFechaInicio(1L, 10L,
                new ActualizarFechaItinerarioGrupoRequest(nuevaFecha));

        assertEquals(nuevaFecha, dto.fechaInicio());
        assertEquals(nuevaFecha.plusDays(2), dto.fechaFin());
        assertEquals(3, dto.duracionDias());
        verify(itinerarioGrupoRepo).save(itinerario);
    }

    @Test
    void actualizarFechaInicio_noCreadorLanzaForbidden() {
        Usuario lider = usuario(1L, "Ana");
        Usuario miembro = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, lider);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, miembro, RolGrupo.MIEMBRO)));

        assertThrows(ForbiddenException.class,
                () -> service.actualizarFechaInicio(2L, 10L,
                        new ActualizarFechaItinerarioGrupoRequest(LocalDate.now())));
        verify(itinerarioGrupoRepo, never()).save(any());
    }

    /* ---------------- confirmar ---------------- */

    @Test
    void confirmarPorNoLider_lanzaForbidden() {
        Usuario lider = usuario(1L, "Ana");
        Usuario miembro = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, lider);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, miembro, RolGrupo.MIEMBRO)));

        assertThrows(ForbiddenException.class, () -> service.confirmarItem(2L, 10L, 50L));
    }

    /* ---------------- editar ---------------- */

    @Test
    void editarConfirmadaPorNoLider_lanzaForbidden() {
        Usuario lider = usuario(1L, "Ana");
        Usuario miembro = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);
        ItemItinerarioGrupo item = item(50L, itinerario, lider, EstadoItemItinerarioGrupo.CONFIRMADO);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, miembro, RolGrupo.MIEMBRO)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));
        when(itemRepo.findByIdAndItinerarioGrupo_IdItinerarioGrupo(50L, 99L))
                .thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> service.actualizarItem(2L, 10L, 50L, req("Editada", 1)));
    }

    @Test
    void editarPropuestaPropiaMientrasEstaPropuesta_ok() {
        Usuario lider = usuario(1L, "Ana");
        Usuario miembro = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);
        ItemItinerarioGrupo item = item(50L, itinerario, miembro, EstadoItemItinerarioGrupo.PROPUESTO);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, miembro, RolGrupo.MIEMBRO)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));
        when(itemRepo.findByIdAndItinerarioGrupo_IdItinerarioGrupo(50L, 99L))
                .thenReturn(Optional.of(item));

        ItemItinerarioGrupoDTO dto = service.actualizarItem(2L, 10L, 50L, req("Corregida", 2));

        assertEquals("Corregida", dto.nombreActividad());
        assertEquals(2, dto.dia());
        verify(itemRepo).save(item);
    }

    /* ---------------- eliminar ---------------- */

    @Test
    void eliminarPropuestaAjenaPorNoLider_lanzaForbidden() {
        Usuario lider = usuario(1L, "Ana");
        Usuario miembro = usuario(2L, "Luis");
        Usuario otro = usuario(3L, "Marta");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);
        ItemItinerarioGrupo itemAjeno = item(50L, itinerario, otro, EstadoItemItinerarioGrupo.PROPUESTO);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, miembro, RolGrupo.MIEMBRO)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));
        when(itemRepo.findByIdAndItinerarioGrupo_IdItinerarioGrupo(50L, 99L))
                .thenReturn(Optional.of(itemAjeno));

        assertThrows(ForbiddenException.class, () -> service.eliminarItem(2L, 10L, 50L));
        verify(itemRepo, never()).delete(any());
    }

    @Test
    void eliminarPorLider_borraCualquierItem() {
        Usuario lider = usuario(1L, "Ana");
        Usuario otro = usuario(3L, "Marta");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);
        ItemItinerarioGrupo item = item(50L, itinerario, otro, EstadoItemItinerarioGrupo.PROPUESTO);
        itinerario.getItems().add(item);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro(grupo, lider, RolGrupo.CREADOR)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));
        when(itemRepo.findByIdAndItinerarioGrupo_IdItinerarioGrupo(50L, 99L))
                .thenReturn(Optional.of(item));

        service.eliminarItem(1L, 10L, 50L);

        // El item se quita de la colección (orphanRemoval lo borra) y se persiste.
        assertTrue(itinerario.getItems().isEmpty());
        verify(itinerarioGrupoRepo).save(itinerario);
    }

    /* ---------------- asistencia ---------------- */

    @Test
    void togglearAsistencia_creaSiNoExiste() {
        Usuario lider = usuario(1L, "Ana");
        Usuario miembro = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);
        ItemItinerarioGrupo item = item(50L, itinerario, lider, EstadoItemItinerarioGrupo.CONFIRMADO);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, miembro, RolGrupo.MIEMBRO)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));
        when(itemRepo.findByIdAndItinerarioGrupo_IdItinerarioGrupo(50L, 99L))
                .thenReturn(Optional.of(item));
        when(asistenciaRepo.findByItem_IdAndUsuario_IdUsuario(50L, 2L)).thenReturn(Optional.empty());

        service.togglearAsistencia(2L, 10L, 50L, true);

        ArgumentCaptor<AsistenciaItemGrupo> captor = ArgumentCaptor.forClass(AsistenciaItemGrupo.class);
        verify(asistenciaRepo).save(captor.capture());
        assertTrue(captor.getValue().isAsiste());
        assertEquals(2L, captor.getValue().getUsuario().getIdUsuario());
    }

    @Test
    void togglearAsistenciaSobreItemNoConfirmado_lanzaBadRequest() {
        Usuario lider = usuario(1L, "Ana");
        Usuario miembro = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, lider);
        ItinerarioGrupo itinerario = itinerario(grupo, lider);
        ItemItinerarioGrupo item = item(50L, itinerario, miembro, EstadoItemItinerarioGrupo.PROPUESTO);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, miembro, RolGrupo.MIEMBRO)));
        when(itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(itinerario));
        when(itemRepo.findByIdAndItinerarioGrupo_IdItinerarioGrupo(50L, 99L))
                .thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> service.togglearAsistencia(2L, 10L, 50L, true));
        verify(asistenciaRepo, never()).save(any());
    }

    /* ---------------- helpers ---------------- */

    private Usuario usuario(Long id, String nombre) {
        return Usuario.builder().idUsuario(id).nombre(nombre).email(nombre + "@mail.com").build();
    }

    private Grupo grupo(Long id, Usuario creador) {
        return Grupo.builder().idGrupo(id).creador(creador).nombre("Grupo " + id)
                .fechaCreacion(LocalDateTime.now()).activo(true).build();
    }

    private MiembroGrupo miembro(Grupo grupo, Usuario usuario, RolGrupo rol) {
        return MiembroGrupo.builder().grupo(grupo).usuario(usuario).rol(rol)
                .fechaUnion(LocalDateTime.now()).build();
    }

    private ItinerarioGrupo itinerario(Grupo grupo, Usuario creador) {
        return ItinerarioGrupo.builder()
                .idItinerarioGrupo(99L)
                .grupo(grupo)
                .creador(creador)
                .titulo("Compartido")
                .provincia(Provincia.MENDOZA)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusDays(2))
                .duracionDias(3)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private ItemItinerarioGrupo item(Long id, ItinerarioGrupo itinerario, Usuario propuestoPor,
                                     EstadoItemItinerarioGrupo estado) {
        return ItemItinerarioGrupo.builder()
                .id(id)
                .itinerarioGrupo(itinerario)
                .nombreActividad("Actividad")
                .dia(1)
                .estado(estado)
                .propuestoPor(propuestoPor)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private ItemItinerarioGrupoRequest req(String nombre, int dia) {
        return new ItemItinerarioGrupoRequest(nombre, null, null, null, dia, null);
    }
}
