package turistear.turistear_backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import turistear.turistear_backend.dto.favoritos.AgregarFotoItinerarioRequest;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioResumenDTO;
import turistear.turistear_backend.model.FotoItinerarioUsuario;
import turistear.turistear_backend.model.ItinerarioUsuario;
import turistear.turistear_backend.repository.EtiquetaRepository;
import turistear.turistear_backend.repository.FavoritoRepository;
import turistear.turistear_backend.repository.FotoItinerarioUsuarioRepository;
import turistear.turistear_backend.repository.ItemItinerarioUsuarioRepository;
import turistear.turistear_backend.repository.ItinerarioSistemaRepository;
import turistear.turistear_backend.repository.ItinerarioUsuarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceItinerariosUsuarioTest {

    @Mock private ItinerarioUsuarioRepository itinerarioRepo;
    @Mock private ItemItinerarioUsuarioRepository itemRepo;
    @Mock private ItinerarioSistemaRepository sistemaRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private FavoritoRepository favoritoRepo;
    @Mock private EtiquetaRepository etiquetaRepo;
    @Mock private FotoItinerarioUsuarioRepository fotoRepo;

    @InjectMocks
    private ServiceItinerariosUsuario service;

    @Test
    void agregarPrimeraFotoLaEstableceComoPortada() {
        ItinerarioUsuario itinerario = ItinerarioUsuario.builder()
                .idItinerarioUsuario(121L)
                .build();
        when(itinerarioRepo.findByIdItinerarioUsuarioAndUsuario_IdUsuario(121L, 7L))
                .thenReturn(Optional.of(itinerario));
        when(fotoRepo.save(any(FotoItinerarioUsuario.class))).thenAnswer(invocation -> {
            FotoItinerarioUsuario foto = invocation.getArgument(0);
            foto.setId(23L);
            return foto;
        });

        service.agregarFoto(7L, 121L, new AgregarFotoItinerarioRequest("foto-garza"));

        assertEquals("foto-garza", itinerario.getFotoPortada());
        verify(itinerarioRepo).saveAndFlush(itinerario);
    }

    @Test
    void resumenUsaLaPrimeraFotoCuandoLaPortadaEsNula() {
        ItinerarioUsuario itinerario = ItinerarioUsuario.builder()
                .idItinerarioUsuario(121L)
                .titulo("Finde en la casa de la garza")
                .build();
        itinerario.getFotos().add(FotoItinerarioUsuario.builder()
                .id(23L)
                .itinerarioUsuario(itinerario)
                .url("foto-garza")
                .orden(0)
                .build());

        ItinerarioUsuarioResumenDTO resumen = ItinerarioUsuarioResumenDTO.from(itinerario);

        assertEquals("foto-garza", resumen.fotoPortada());
    }

    @Test
    void eliminarFotoQuitaElHuerfanoYCambiaLaPortadaAntesDeResponder() {
        ItinerarioUsuario itinerario = ItinerarioUsuario.builder()
                .idItinerarioUsuario(123L)
                .fotoPortada("foto-1")
                .build();
        FotoItinerarioUsuario foto1 = FotoItinerarioUsuario.builder()
                .id(13L)
                .itinerarioUsuario(itinerario)
                .url("foto-1")
                .orden(0)
                .build();
        FotoItinerarioUsuario foto2 = FotoItinerarioUsuario.builder()
                .id(14L)
                .itinerarioUsuario(itinerario)
                .url("foto-2")
                .orden(1)
                .build();
        itinerario.getFotos().add(foto1);
        itinerario.getFotos().add(foto2);

        when(itinerarioRepo.findByIdItinerarioUsuarioAndUsuario_IdUsuario(123L, 7L))
                .thenReturn(Optional.of(itinerario));
        when(fotoRepo.findByIdAndItinerarioUsuario_IdItinerarioUsuario(13L, 123L))
                .thenReturn(Optional.of(foto1));

        service.eliminarFoto(7L, 123L, 13L);

        assertEquals(1, itinerario.getFotos().size());
        assertEquals(14L, itinerario.getFotos().getFirst().getId());
        assertEquals("foto-2", itinerario.getFotoPortada());
        verify(itinerarioRepo).saveAndFlush(itinerario);
        verify(fotoRepo, never()).delete(foto1);
    }
}
