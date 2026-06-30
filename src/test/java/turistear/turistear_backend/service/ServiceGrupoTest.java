package turistear.turistear_backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import turistear.turistear_backend.dto.grupo.ActualizarGrupoRequest;
import turistear.turistear_backend.dto.grupo.CodigoInvitacionDTO;
import turistear.turistear_backend.dto.grupo.CrearGrupoRequest;
import turistear.turistear_backend.dto.grupo.GrupoDTO;
import turistear.turistear_backend.enumerable.RolGrupo;
import turistear.turistear_backend.exception.ForbiddenException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.*;
import turistear.turistear_backend.repository.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceGrupoTest {

    @Mock private GrupoRepository grupoRepo;
    @Mock private MiembroGrupoRepository miembroRepo;
    @Mock private CodigoInvitacionRepository codigoRepo;
    @Mock private EncuestaRepository encuestaRepo;
    @Mock private OpcionEncuestaRepository opcionRepo;
    @Mock private VotoRepository votoRepo;
    @Mock private UsuarioRepository usuarioRepo;

    @InjectMocks
    private ServiceGrupo service;

    @Test
    void crearGrupoCreaAlCreadorComoPrimerMiembro() {
        Usuario usuario = usuario(1L, "Ana");
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
        when(grupoRepo.save(any(Grupo.class))).thenAnswer(invocation -> {
            Grupo g = invocation.getArgument(0);
            g.setIdGrupo(10L);
            return g;
        });
        when(miembroRepo.save(any(MiembroGrupo.class))).thenAnswer(invocation -> {
            MiembroGrupo m = invocation.getArgument(0);
            m.setId(100L);
            return m;
        });

        GrupoDTO dto = service.crearGrupo(1L, new CrearGrupoRequest("Amigos", "Viaje", "https://portada.jpg"));

        assertEquals(10L, dto.idGrupo());
        assertEquals("Amigos", dto.nombre());
        assertEquals("https://portada.jpg", dto.fotoPortada());
        assertEquals(1, dto.cantidadMiembros());
        assertEquals(1, dto.miembros().size());
        assertEquals(RolGrupo.CREADOR, dto.miembros().get(0).rol());
        verify(grupoRepo).save(any(Grupo.class));
        verify(miembroRepo).save(any(MiembroGrupo.class));
    }

    @Test
    void unirseAGrupoAgregaMiembroYActualizaCantidad() {
        Usuario creador = usuario(1L, "Ana");
        Usuario nuevo = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, creador);
        CodigoInvitacion codigo = CodigoInvitacion.builder()
                .id(50L)
                .grupo(grupo)
                .codigo("ABC12345")
                .fechaGeneracion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();

        when(codigoRepo.findByCodigo("ABC12345")).thenReturn(Optional.of(codigo));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(nuevo));
        when(miembroRepo.existsByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L)).thenReturn(false);
        when(miembroRepo.save(any(MiembroGrupo.class))).thenAnswer(invocation -> {
            MiembroGrupo m = invocation.getArgument(0);
            m.setId(101L);
            return m;
        });
        when(miembroRepo.countByGrupo_IdGrupo(10L)).thenReturn(2L);
        when(miembroRepo.findByGrupo_IdGrupo(10L)).thenReturn(List.of());
        when(encuestaRepo.existsByGrupo_IdGrupoAndEstado(10L, turistear.turistear_backend.enumerable.EstadoEncuesta.ABIERTA))
                .thenReturn(false);

        GrupoDTO dto = service.unirseAGrupo(2L, "ABC12345");

        assertEquals(10L, dto.idGrupo());
        assertEquals(2, dto.cantidadMiembros());
        verify(miembroRepo).save(any(MiembroGrupo.class));
    }

    @Test
    void salirDeGrupoPromueveAlMiembroMasAntiguoCuandoSaleElCreador() {
        Grupo grupo = grupo(10L, usuario(1L, "Ana"));
        when(grupoRepo.findWithLockById(10L)).thenReturn(Optional.of(grupo));
        MiembroGrupo creador = MiembroGrupo.builder()
                .id(1L).grupo(grupo).usuario(usuario(1L, "Ana"))
                .rol(RolGrupo.CREADOR).fechaUnion(LocalDateTime.now().plusMinutes(1))
                .build();
        MiembroGrupo antiguo = MiembroGrupo.builder()
                .id(2L).grupo(grupo).usuario(usuario(2L, "Luis"))
                .rol(RolGrupo.MIEMBRO).fechaUnion(LocalDateTime.now())
                .build();

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(creador));
        when(miembroRepo.findByGrupo_IdGrupoOrderByFechaUnionAsc(10L))
                .thenReturn(List.of(antiguo, creador));
        doNothing().when(codigoRepo).deleteByGrupo_IdGrupo(10L);
        doNothing().when(miembroRepo).flush();
        when(miembroRepo.countByGrupo_IdGrupo(10L)).thenReturn(1L);

        service.salirDeGrupo(1L, 10L);

        assertEquals(RolGrupo.CREADOR, antiguo.getRol());
        verify(codigoRepo).deleteByGrupo_IdGrupo(10L);
        verify(miembroRepo).save(antiguo);
        verify(miembroRepo).delete(creador);
        verify(miembroRepo).flush();
        verify(miembroRepo).countByGrupo_IdGrupo(10L);
    }

    @Test
    void salirDeGrupo_grupoInexistente_lanzaNotFound() {
        when(grupoRepo.findWithLockById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.salirDeGrupo(1L, 99L));
        verify(grupoRepo).findWithLockById(99L);
        verify(miembroRepo, never()).findByGrupo_IdGrupoAndUsuario_IdUsuario(anyLong(), anyLong());
    }

    @Test
    void salirDeGrupo_ultimoMiembroEliminaGrupoYRelacionados() {
        Grupo grupo = grupo(10L, usuario(1L, "Ana"));
        when(grupoRepo.findWithLockById(10L)).thenReturn(Optional.of(grupo));
        MiembroGrupo creador = MiembroGrupo.builder()
                .id(1L).grupo(grupo).usuario(usuario(1L, "Ana"))
                .rol(RolGrupo.CREADOR).fechaUnion(LocalDateTime.now())
                .build();

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(creador));
        when(miembroRepo.findByGrupo_IdGrupoOrderByFechaUnionAsc(10L))
                .thenReturn(List.of(creador));
        doNothing().when(miembroRepo).flush();
        when(miembroRepo.countByGrupo_IdGrupo(10L)).thenReturn(0L);
        doNothing().when(votoRepo).deleteByEncuesta_Grupo_IdGrupo(10L);
        when(grupoRepo.findById(10L)).thenReturn(Optional.of(grupo));

        service.salirDeGrupo(1L, 10L);

        verify(miembroRepo).delete(creador);
        verify(miembroRepo).flush();
        verify(miembroRepo).countByGrupo_IdGrupo(10L);
        verify(votoRepo).deleteByEncuesta_Grupo_IdGrupo(10L);
        verify(grupoRepo).findById(10L);
        verify(grupoRepo).delete(grupo);
    }

    @Test
    void salirDeGrupo_ultimoMiembroConEncuestaFinalizadaNoLanzaTransientException() {
        Usuario ana = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, ana);
        when(grupoRepo.findWithLockById(10L)).thenReturn(Optional.of(grupo));
        MiembroGrupo creador = MiembroGrupo.builder()
                .id(1L).grupo(grupo).usuario(ana)
                .rol(RolGrupo.CREADOR).fechaUnion(LocalDateTime.now())
                .build();
        grupo.getMiembros().add(creador);

        Encuesta encuesta = Encuesta.builder()
                .idEncuesta(100L)
                .grupo(grupo)
                .creador(ana)
                .estado(turistear.turistear_backend.enumerable.EstadoEncuesta.FINALIZADA)
                .fechaCreacion(LocalDateTime.now())
                .build();
        OpcionEncuesta opcion = OpcionEncuesta.builder()
                .id(50L)
                .encuesta(encuesta)
                .tituloSnapshot("Opción ganadora")
                .propietario(ana)
                .build();
        encuesta.getOpciones().add(opcion);
        encuesta.setOpcionGanadora(opcion);
        grupo.getEncuestas().add(encuesta);

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(creador));
        when(miembroRepo.findByGrupo_IdGrupoOrderByFechaUnionAsc(10L))
                .thenReturn(List.of(creador));
        doNothing().when(miembroRepo).flush();
        when(miembroRepo.countByGrupo_IdGrupo(10L)).thenReturn(0L);
        doNothing().when(votoRepo).deleteByEncuesta_Grupo_IdGrupo(10L);
        when(grupoRepo.findById(10L)).thenReturn(Optional.of(grupo));

        assertDoesNotThrow(() -> service.salirDeGrupo(1L, 10L));

        verify(votoRepo).deleteByEncuesta_Grupo_IdGrupo(10L);
        verify(grupoRepo).delete(grupo);
        assertNull(encuesta.getOpcionGanadora());
    }

    @Test
    void actualizarGrupo_creadorActualizaDatos() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        MiembroGrupo miembro = MiembroGrupo.builder()
                .id(1L).grupo(grupo).usuario(creador)
                .rol(RolGrupo.CREADOR).fechaUnion(LocalDateTime.now())
                .build();

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro));
        when(grupoRepo.findById(10L)).thenReturn(Optional.of(grupo));
        when(grupoRepo.save(grupo)).thenReturn(grupo);
        when(miembroRepo.findByGrupo_IdGrupo(10L)).thenReturn(List.of(miembro));
        when(encuestaRepo.existsByGrupo_IdGrupoAndEstado(10L, turistear.turistear_backend.enumerable.EstadoEncuesta.ABIERTA))
                .thenReturn(false);

        GrupoDTO dto = service.actualizarGrupo(1L, 10L,
                new ActualizarGrupoRequest("Nuevo nombre", "Nueva descripción", "https://nueva.jpg"));

        assertEquals("Nuevo nombre", dto.nombre());
        assertEquals("Nueva descripción", dto.descripcion());
        assertEquals("https://nueva.jpg", dto.fotoPortada());
        verify(grupoRepo).save(grupo);
    }

    @Test
    void actualizarGrupo_noCreadorLanzaForbidden() {
        Usuario creador = usuario(1L, "Ana");
        Usuario noCreador = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, creador);
        MiembroGrupo miembro = MiembroGrupo.builder()
                .id(2L).grupo(grupo).usuario(noCreador)
                .rol(RolGrupo.MIEMBRO).fechaUnion(LocalDateTime.now())
                .build();

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro));

        assertThrows(ForbiddenException.class, () -> service.actualizarGrupo(2L, 10L,
                new ActualizarGrupoRequest("Nuevo nombre", null, null)));
        verify(grupoRepo, never()).save(any());
    }

    @Test
    void generarCodigoInvitacionCreaCodigoDe8CaracteresCon10MinutosDeValidez() {
        Grupo grupo = grupo(10L, usuario(1L, "Ana"));
        MiembroGrupo creador = MiembroGrupo.builder()
                .id(1L).grupo(grupo).usuario(usuario(1L, "Ana"))
                .rol(RolGrupo.CREADOR).fechaUnion(LocalDateTime.now())
                .build();

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(creador));
        when(grupoRepo.findById(10L)).thenReturn(Optional.of(grupo));
        when(codigoRepo.existsByCodigo(anyString())).thenReturn(false);
        when(codigoRepo.save(any(CodigoInvitacion.class))).thenAnswer(invocation -> {
            CodigoInvitacion c = invocation.getArgument(0);
            c.setId(200L);
            return c;
        });

        CodigoInvitacionDTO dto = service.generarCodigoInvitacion(1L, 10L);

        assertEquals(8, dto.codigo().length());
        long minutos = Duration.between(LocalDateTime.now(), dto.fechaExpiracion()).toMinutes();
        assertTrue(minutos >= 9 && minutos <= 11);

        ArgumentCaptor<CodigoInvitacion> captor = ArgumentCaptor.forClass(CodigoInvitacion.class);
        verify(codigoRepo).save(captor.capture());
        assertEquals(grupo, captor.getValue().getGrupo());
    }

    private Usuario usuario(Long id, String nombre) {
        return Usuario.builder().idUsuario(id).nombre(nombre).email(nombre + "@mail.com").build();
    }

    private Grupo grupo(Long id, Usuario creador) {
        return Grupo.builder()
                .idGrupo(id)
                .creador(creador)
                .nombre("Grupo " + id)
                .fechaCreacion(LocalDateTime.now())
                .activo(true)
                .build();
    }
}
