package turistear.turistear_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.service.ServiceUsuario;

@RestController
@RequestMapping("/users")
public class ControllerUsuario {

    private final ServiceUsuario serviceUsuario;

    public ControllerUsuario(ServiceUsuario serviceUsuario){
        this.serviceUsuario = serviceUsuario;
    }

    @PostMapping("/usuarios")
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return serviceUsuario.crearUsuario(usuario);
    }

    @DeleteMapping("/usuarios")
    public void eliminarUsuario(@RequestParam Long idUsuario) {
        serviceUsuario.eliminarUsuario(idUsuario);
    }
}
