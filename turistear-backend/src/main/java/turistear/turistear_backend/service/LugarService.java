package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.LugarRequest;
import turistear.turistear_backend.dto.LugarResponse;
import turistear.turistear_backend.model.Lugar;
import turistear.turistear_backend.repository.LugarRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LugarService {

    private final LugarRepository lugarRepository;

    public LugarResponse create(LugarRequest request) {
        Lugar lugar = Lugar.builder()
                .provincia(request.getProvincia())
                .localidad(request.getLocalidad())
                .direccion(request.getDireccion())
                .build();
        return toResponse(lugarRepository.save(lugar));
    }

    public LugarResponse getById(Long id) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lugar no encontrado"));
        return toResponse(lugar);
    }

    public List<LugarResponse> getAll() {
        return lugarRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LugarResponse update(Long id, LugarRequest request) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lugar no encontrado"));
        lugar.setProvincia(request.getProvincia());
        lugar.setLocalidad(request.getLocalidad());
        lugar.setDireccion(request.getDireccion());
        return toResponse(lugarRepository.save(lugar));
    }

    public void delete(Long id) {
        if (!lugarRepository.existsById(id)) {
            throw new IllegalArgumentException("Lugar no encontrado");
        }
        lugarRepository.deleteById(id);
    }

    private LugarResponse toResponse(Lugar lugar) {
        return LugarResponse.builder()
                .idLugar(lugar.getIdLugar())
                .provincia(lugar.getProvincia())
                .localidad(lugar.getLocalidad())
                .direccion(lugar.getDireccion())
                .build();
    }
}