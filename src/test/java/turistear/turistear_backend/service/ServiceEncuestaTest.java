package turistear.turistear_backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import turistear.turistear_backend.dto.grupo.*;
import turistear.turistear_backend.enumerable.EstadoEncuesta;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.enumerable.RolGrupo;
import turistear.turistear_backend.exception.BadRequestException;
import turistear.turistear_backend.exception.ForbiddenException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.*;
import turistear.turistear_backend.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceEncuestaTest {

    @Mock private EncuestaRepository encuestaRepo;
    @Mock private OpcionEncuestaRepository opcionRepo;
    @Mock private VotoRepository votoRepo;
    @Mock private GrupoRepository grupoRepo;
    @Mock private MiembroGrupoRepository miembroRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private ItinerarioSistemaRepository sistemaRepo;
    @Mock private ItinerarioUsuarioRepository itinerarioUsuarioRepo;
    @Mock private FavoritoRepository favoritoRepo;
    @Mock private ServiceItinerariosUsuario serviceItinerariosUsuario;

    @InjectMocks
    private ServiceEncuesta service;

    @Test
    void crearEncuesta_sinNombreAsignaTituloPorDefecto() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        MiembroGrupo miembro = miembro(grupo, creador, RolGrupo.CREADOR);
        ItinerarioSistema sistema = ItinerarioSistema.builder()
                .idItinerario(5L)
                .titulo("Escapada a Mendoza")
                .fotoPortada("foto.jpg")
                .provincia(Provincia.MENDOZA)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusDays(2))
                .duracionDias(3)
                .build();

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro));
        when(sistemaRepo.findById(5L)).thenReturn(Optional.of(sistema));
        when(encuestaRepo.save(any(Encuesta.class))).thenAnswer(invocation -> {
            Encuesta e = invocation.getArgument(0);
            e.setIdEncuesta(100L);
            return e;
        });
        when(opcionRepo.save(any(OpcionEncuesta.class))).thenAnswer(invocation -> {
            OpcionEncuesta o = invocation.getArgument(0);
            o.setId(20L);
            return o;
        });
        doNothing().when(opcionRepo).flush();
        when(votoRepo.countByOpcion_Id(any())).thenReturn(0L);
        when(votoRepo.findByEncuesta_IdEncuestaAndUsuario_IdUsuario(100L, 1L)).thenReturn(Optional.empty());

        CrearEncuestaRequest req = new CrearEncuestaRequest(
                null,
                List.of(new CrearEncuestaRequest.OpcionSolicitud(5L, null)));

        EncuestaDTO dto = service.crearEncuesta(1L, 10L, req);

        assertEquals("Encuesta de viaje", dto.nombre());
        assertEquals(1, dto.opciones().size());
    }

    @Test
    void crearEncuestaConOpcionDelSistemaLaGuardaAbierta() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        MiembroGrupo miembro = miembro(grupo, creador, RolGrupo.CREADOR);
        ItinerarioSistema sistema = ItinerarioSistema.builder()
                .idItinerario(5L)
                .titulo("Escapada a Mendoza")
                .fotoPortada("foto.jpg")
                .provincia(Provincia.MENDOZA)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusDays(2))
                .duracionDias(3)
                .build();

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro));
        when(sistemaRepo.findById(5L)).thenReturn(Optional.of(sistema));
        when(encuestaRepo.save(any(Encuesta.class))).thenAnswer(invocation -> {
            Encuesta e = invocation.getArgument(0);
            e.setIdEncuesta(100L);
            return e;
        });
        when(opcionRepo.save(any(OpcionEncuesta.class))).thenAnswer(invocation -> {
            OpcionEncuesta o = invocation.getArgument(0);
            o.setId(20L);
            return o;
        });
        doNothing().when(opcionRepo).flush();
        when(votoRepo.countByOpcion_Id(any())).thenReturn(0L);
        when(votoRepo.findByEncuesta_IdEncuestaAndUsuario_IdUsuario(100L, 1L)).thenReturn(Optional.empty());

        CrearEncuestaRequest req = new CrearEncuestaRequest(
                null,
                List.of(new CrearEncuestaRequest.OpcionSolicitud(5L, null)));

        EncuestaDTO dto = service.crearEncuesta(1L, 10L, req);

        assertEquals(EstadoEncuesta.ABIERTA, dto.estado());
        assertEquals(1, dto.opciones().size());
        assertEquals("Escapada a Mendoza", dto.opciones().get(0).tituloSnapshot());

        ArgumentCaptor<Encuesta> captor = ArgumentCaptor.forClass(Encuesta.class);
        verify(encuestaRepo).save(captor.capture());
        assertEquals(EstadoEncuesta.ABIERTA, captor.getValue().getEstado());
    }

    @Test
    void crearEncuestaConVariasOpcionesPersisteTodas() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        MiembroGrupo miembro = miembro(grupo, creador, RolGrupo.CREADOR);
        ItinerarioSistema sistema1 = ItinerarioSistema.builder()
                .idItinerario(5L)
                .titulo("Escapada a Mendoza")
                .fotoPortada("foto.jpg")
                .provincia(Provincia.MENDOZA)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusDays(2))
                .duracionDias(3)
                .build();
        ItinerarioSistema sistema2 = ItinerarioSistema.builder()
                .idItinerario(6L)
                .titulo("Escapada a Córdoba")
                .fotoPortada("foto2.jpg")
                .provincia(Provincia.CORDOBA)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusDays(2))
                .duracionDias(3)
                .build();

        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro));
        when(sistemaRepo.findById(5L)).thenReturn(Optional.of(sistema1));
        when(sistemaRepo.findById(6L)).thenReturn(Optional.of(sistema2));
        when(encuestaRepo.save(any(Encuesta.class))).thenAnswer(invocation -> {
            Encuesta e = invocation.getArgument(0);
            e.setIdEncuesta(100L);
            return e;
        });
        AtomicLong idGenerator = new AtomicLong(20L);
        when(opcionRepo.save(any(OpcionEncuesta.class))).thenAnswer(invocation -> {
            OpcionEncuesta o = invocation.getArgument(0);
            o.setId(idGenerator.getAndIncrement());
            return o;
        });
        doNothing().when(opcionRepo).flush();
        when(votoRepo.countByOpcion_Id(any())).thenReturn(0L);
        when(votoRepo.findByEncuesta_IdEncuestaAndUsuario_IdUsuario(100L, 1L)).thenReturn(Optional.empty());

        CrearEncuestaRequest req = new CrearEncuestaRequest(
                "Encuesta test",
                List.of(
                        new CrearEncuestaRequest.OpcionSolicitud(5L, null),
                        new CrearEncuestaRequest.OpcionSolicitud(6L, null)));

        EncuestaDTO dto = service.crearEncuesta(1L, 10L, req);

        assertEquals(2, dto.opciones().size());
        verify(opcionRepo, times(2)).save(any(OpcionEncuesta.class));
        verify(opcionRepo).flush();
    }

    @Test
    void finalizarEncuestaConMayoriaDeclaraGanador() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        Encuesta encuesta = encuestaAbierta(100L, grupo, creador);
        OpcionEncuesta opcion1 = opcion(20L, encuesta, "Opción A");
        OpcionEncuesta opcion2 = opcion(21L, encuesta, "Opción B");

        when(encuestaRepo.findByIdEncuesta(100L)).thenReturn(Optional.of(encuesta));
        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro(grupo, creador, RolGrupo.CREADOR)));
        when(miembroRepo.countByGrupo_IdGrupo(10L)).thenReturn(3L);
        when(opcionRepo.findByEncuesta_IdEncuesta(100L)).thenReturn(List.of(opcion1, opcion2));
        when(votoRepo.countByEncuesta_IdEncuesta(100L)).thenReturn(3L);
        when(votoRepo.countByOpcion_Id(20L)).thenReturn(2L);
        when(votoRepo.countByOpcion_Id(21L)).thenReturn(1L);

        ResultadoEncuestaDTO resultado = service.finalizarEncuesta(1L, 100L);

        assertFalse(resultado.empate());
        assertEquals(20L, resultado.ganador().id());
        assertEquals(EstadoEncuesta.FINALIZADA, encuesta.getEstado());
        assertEquals(opcion1, encuesta.getOpcionGanadora());
    }

    @Test
    void finalizarEncuestaConEmpateMarcaEstadoEmpate() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        Encuesta encuesta = encuestaAbierta(100L, grupo, creador);
        OpcionEncuesta opcion1 = opcion(20L, encuesta, "Opción A");
        OpcionEncuesta opcion2 = opcion(21L, encuesta, "Opción B");

        when(encuestaRepo.findByIdEncuesta(100L)).thenReturn(Optional.of(encuesta));
        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro(grupo, creador, RolGrupo.CREADOR)));
        when(miembroRepo.countByGrupo_IdGrupo(10L)).thenReturn(2L);
        when(opcionRepo.findByEncuesta_IdEncuesta(100L)).thenReturn(List.of(opcion1, opcion2));
        when(votoRepo.countByEncuesta_IdEncuesta(100L)).thenReturn(2L);
        when(votoRepo.countByOpcion_Id(20L)).thenReturn(1L);
        when(votoRepo.countByOpcion_Id(21L)).thenReturn(1L);

        ResultadoEncuestaDTO resultado = service.finalizarEncuesta(1L, 100L);

        assertTrue(resultado.empate());
        assertNull(resultado.ganador());
        assertEquals(2, resultado.opcionesEmpatadas().size());
        assertEquals(EstadoEncuesta.EMPATE, encuesta.getEstado());
    }

    @Test
    void finalizarEncuestaConVotosFaltantesLanzaBadRequest() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        Encuesta encuesta = encuestaAbierta(100L, grupo, creador);
        OpcionEncuesta opcion1 = opcion(20L, encuesta, "Opción A");
        OpcionEncuesta opcion2 = opcion(21L, encuesta, "Opción B");

        when(encuestaRepo.findByIdEncuesta(100L)).thenReturn(Optional.of(encuesta));
        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro(grupo, creador, RolGrupo.CREADOR)));
        when(miembroRepo.countByGrupo_IdGrupo(10L)).thenReturn(3L);
        when(opcionRepo.findByEncuesta_IdEncuesta(100L)).thenReturn(List.of(opcion1, opcion2));
        when(votoRepo.countByEncuesta_IdEncuesta(100L)).thenReturn(1L);

        assertThrows(BadRequestException.class, () -> service.finalizarEncuesta(1L, 100L));
        verify(encuestaRepo, never()).save(any());
    }

    @Test
    void votarRegistraElVotoDelMiembro() {
        Usuario creador = usuario(1L, "Ana");
        Usuario votante = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, creador);
        Encuesta encuesta = encuestaAbierta(100L, grupo, creador);
        OpcionEncuesta opcion = opcion(20L, encuesta, "Opción A");
        encuesta.getOpciones().add(opcion);

        Voto votoGuardado = Voto.builder()
                .id(500L)
                .encuesta(encuesta)
                .opcion(opcion)
                .usuario(votante)
                .fechaVoto(LocalDateTime.now())
                .build();

        when(encuestaRepo.findByIdEncuesta(100L)).thenReturn(Optional.of(encuesta));
        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, votante, RolGrupo.MIEMBRO)));
        when(opcionRepo.findByIdAndEncuesta_IdEncuesta(20L, 100L)).thenReturn(Optional.of(opcion));
        when(votoRepo.findByEncuesta_IdEncuestaAndUsuario_IdUsuario(100L, 2L))
                .thenReturn(Optional.empty(), Optional.of(votoGuardado));
        when(votoRepo.save(any(Voto.class))).thenAnswer(invocation -> {
            Voto v = invocation.getArgument(0);
            v.setId(500L);
            return v;
        });
        when(votoRepo.countByOpcion_Id(any())).thenReturn(1L);

        EncuestaDTO dto = service.votar(2L, 100L, 20L);

        assertTrue(dto.yaVoto());
        assertEquals(20L, dto.idOpcionVotada());
        verify(votoRepo).save(any(Voto.class));
    }

    @Test
    void votarCambiaLaOpcionSiYaExisteVoto() {
        Usuario creador = usuario(1L, "Ana");
        Usuario votante = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, creador);
        Encuesta encuesta = encuestaAbierta(100L, grupo, creador);
        OpcionEncuesta opcion20 = opcion(20L, encuesta, "Opción A");
        OpcionEncuesta opcion21 = opcion(21L, encuesta, "Opción B");
        encuesta.getOpciones().add(opcion20);
        encuesta.getOpciones().add(opcion21);

        Voto votoExistente = Voto.builder()
                .id(500L)
                .encuesta(encuesta)
                .opcion(opcion20)
                .usuario(votante)
                .fechaVoto(LocalDateTime.now())
                .build();

        when(encuestaRepo.findByIdEncuesta(100L)).thenReturn(Optional.of(encuesta));
        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro(grupo, votante, RolGrupo.MIEMBRO)));
        when(opcionRepo.findByIdAndEncuesta_IdEncuesta(21L, 100L)).thenReturn(Optional.of(opcion21));
        when(votoRepo.findByEncuesta_IdEncuestaAndUsuario_IdUsuario(100L, 2L))
                .thenReturn(Optional.of(votoExistente));
        when(votoRepo.countByOpcion_Id(any())).thenReturn(1L);

        EncuestaDTO dto = service.votar(2L, 100L, 21L);

        assertTrue(dto.yaVoto());
        assertEquals(21L, dto.idOpcionVotada());

        ArgumentCaptor<Voto> captor = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepo).save(captor.capture());
        assertEquals(21L, captor.getValue().getOpcion().getId());
    }

    @Test
    void desempatarEligeOpcionManualmente() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        Encuesta encuesta = encuestaEmpatada(100L, grupo, creador);
        OpcionEncuesta opcion = opcion(20L, encuesta, "Opción A");

        when(encuestaRepo.findByIdEncuesta(100L)).thenReturn(Optional.of(encuesta));
        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro(grupo, creador, RolGrupo.CREADOR)));
        when(opcionRepo.findByIdAndEncuesta_IdEncuesta(20L, 100L)).thenReturn(Optional.of(opcion));
        when(votoRepo.countByOpcion_Id(20L)).thenReturn(1L);

        ResultadoEncuestaDTO resultado = service.desempatar(1L, 100L, 20L);

        assertFalse(resultado.empate());
        assertEquals(20L, resultado.ganador().id());
        assertEquals(EstadoEncuesta.FINALIZADA, encuesta.getEstado());
        assertEquals(opcion, encuesta.getOpcionGanadora());
    }

    @Test
    void eliminarEncuesta_creadorElimina() {
        Usuario creador = usuario(1L, "Ana");
        Grupo grupo = grupo(10L, creador);
        MiembroGrupo miembro = miembro(grupo, creador, RolGrupo.CREADOR);
        Encuesta encuesta = encuestaAbierta(100L, grupo, creador);

        when(encuestaRepo.findById(100L)).thenReturn(Optional.of(encuesta));
        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 1L))
                .thenReturn(Optional.of(miembro));

        service.eliminarEncuesta(1L, 100L);

        verify(votoRepo).deleteByEncuesta_IdEncuesta(100L);
        verify(encuestaRepo).delete(encuesta);
    }

    @Test
    void eliminarEncuesta_noCreadorLanzaForbidden() {
        Usuario creador = usuario(1L, "Ana");
        Usuario noCreador = usuario(2L, "Luis");
        Grupo grupo = grupo(10L, creador);
        MiembroGrupo miembro = miembro(grupo, noCreador, RolGrupo.MIEMBRO);
        Encuesta encuesta = encuestaAbierta(100L, grupo, creador);

        when(encuestaRepo.findById(100L)).thenReturn(Optional.of(encuesta));
        when(miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(10L, 2L))
                .thenReturn(Optional.of(miembro));

        assertThrows(ForbiddenException.class, () -> service.eliminarEncuesta(2L, 100L));
        verify(encuestaRepo, never()).delete(any());
    }

    @Test
    void eliminarEncuesta_noExistenteLanzaNotFound() {
        when(encuestaRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminarEncuesta(1L, 999L));
        verify(encuestaRepo, never()).delete(any());
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

    private MiembroGrupo miembro(Grupo grupo, Usuario usuario, RolGrupo rol) {
        return MiembroGrupo.builder()
                .grupo(grupo)
                .usuario(usuario)
                .rol(rol)
                .fechaUnion(LocalDateTime.now())
                .build();
    }

    private Encuesta encuestaAbierta(Long id, Grupo grupo, Usuario creador) {
        Encuesta e = Encuesta.builder()
                .idEncuesta(id)
                .grupo(grupo)
                .creador(creador)
                .estado(EstadoEncuesta.ABIERTA)
                .fechaCreacion(LocalDateTime.now())
                .build();
        e.setOpciones(new java.util.ArrayList<>());
        return e;
    }

    private Encuesta encuestaEmpatada(Long id, Grupo grupo, Usuario creador) {
        Encuesta e = encuestaAbierta(id, grupo, creador);
        e.setEstado(EstadoEncuesta.EMPATE);
        return e;
    }

    private OpcionEncuesta opcion(Long id, Encuesta encuesta, String titulo) {
        return OpcionEncuesta.builder()
                .id(id)
                .encuesta(encuesta)
                .tituloSnapshot(titulo)
                .propietario(encuesta.getCreador())
                .build();
    }
}
