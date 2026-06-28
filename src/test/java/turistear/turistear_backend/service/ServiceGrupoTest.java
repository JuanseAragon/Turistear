package turistear.turistear_backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import turistear.turistear_backend.dto.grupo.CodigoInvitacionDTO;
import turistear.turistear_backend.dto.grupo.CrearGrupoRequest;
import turistear.turistear_backend.dto.grupo.GrupoDTO;
import turistear.turistear_backend.enumerable.RolGrupo;
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
                .fechaExpiracion(LocalDateTime.now().plusMinutes(15))
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

        service.salirDeGrupo(1L, 10L);

        assertEquals(RolGrupo.CREADOR, antiguo.getRol());
        verify(miembroRepo).save(antiguo);
        verify(miembroRepo).delete(creador);
    }

    @Test
    void generarCodigoInvitacionCreaCodigoDe8CaracteresCon15MinutosDeValidez() {
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
        assertTrue(minutos >= 14 && minutos <= 16);

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
