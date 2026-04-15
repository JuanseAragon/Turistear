package turistear.turistear_backend.service.busqueda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import turistear.turistear_backend.dto.RequestActivity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class NominatimAPI implements AdapterMaps {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NominatimAPI() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<RequestActivity> searchInfo(List<RequestActivity> actividadesPreAI) {
        List<RequestActivity> actividadesFinales = new ArrayList<>();

        for (RequestActivity actPreAI : actividadesPreAI) {
            String direccion = actPreAI.ubicacion();
            try {
                String queryEncoded = URLEncoder.encode(direccion, StandardCharsets.UTF_8.toString());
                String nominatimUrl = "https://nominatim.openstreetmap.org/search?q="
                        + queryEncoded + "&format=json&addressdetails=1&limit=1";

                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "TuristeAR-Backend/1.0 (Proyecto Universitario de Desarrollo de Apps sin fines de lucro)");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(nominatimUrl, HttpMethod.GET, entity, String.class);
                JsonNode resultsNode = objectMapper.readTree(response.getBody());

                if (resultsNode.isArray() && resultsNode.size() > 0) {
                    JsonNode primerResultado = resultsNode.get(0);

                    String nombreLugar = primerResultado.path("name").asText();
                    String ubicacion = primerResultado.path("display_name").asText();

                    if (nombreLugar == null || nombreLugar.isEmpty()) {
                        nombreLugar = ubicacion.split(",")[0];
                    }

                    String categoria = primerResultado.path("class").asText();
                    String tipo = primerResultado.path("type").asText();
                    String descripcion = "Lugar turístico (Categoría: " + categoria + " - " + tipo + "). Datos verificados en OpenStreetMap.";

                    RequestActivity actividad = new RequestActivity(
                            nombreLugar,
                            descripcion,
                            ubicacion,
                            actPreAI.fecha_hora_final(),
                            actPreAI.fecha_hora_inicial()
                    );
                    actividadesFinales.add(actividad);
                } else {
                    actividadesFinales.add(new RequestActivity("No encontrado en OpenStreetMap", "Sin detalles", direccion, actPreAI.fecha_hora_final(), actPreAI.fecha_hora_inicial()));
                }

                Thread.sleep(1000);

            } catch (Exception e) {
                actividadesFinales.add(new RequestActivity("Error de búsqueda", "Error: " + e.getMessage(), direccion, actPreAI.fecha_hora_final(), actPreAI.fecha_hora_inicial()));
            }
        }

        return actividadesFinales;
    }
}