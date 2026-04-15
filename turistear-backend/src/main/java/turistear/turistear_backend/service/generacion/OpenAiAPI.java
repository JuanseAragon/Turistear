package turistear.turistear_backend.service.generacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import turistear.turistear_backend.dto.RequestActivity;
import turistear.turistear_backend.dto.RequestGeneration;
import turistear.turistear_backend.dto.ResponseGeneration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiAPI implements AdapterAI {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    public OpenAiAPI() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ResponseGeneration generate(RequestGeneration prompt) {
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "API Key de OpenAI no configurada.");
        }

        String apiUrl = "https://api.openai.com/v1/chat/completions";

        String systemPrompt = "Eres un planificador de viajes experto. Genera un itinerario estructurado en JSON válido que pueda ser parseado directamente. Devuelve ÚNICAMENTE el JSON y nada más. El formato estricto debe ser:\n" +
                "{\n" +
                "  \"nombre\": \"Nombre del viaje\",\n" +
                "  \"fecha_inicio\": \"2024-05-20T00:00:00.000+00:00\",\n" +
                "  \"fecha_final\": \"2024-05-25T00:00:00.000+00:00\",\n" +
                "  \"ubicaciones\": [\n" +
                "    {\n" +
                "      \"direccion\": \"Dirección exacta o nombre específico del lugar para buscar en OpenStreetMap API\",\n" +
                "      \"fecha_hora_inicial\": \"2024-05-20T10:00:00.000+00:00\",\n" +
                "      \"fecha_hora_final\": \"2024-05-20T12:00:00.000+00:00\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String userPrompt = String.format("Genera un itinerario para el destino '%s' desde la fecha '%s' hasta '%s'. Toma en cuenta los siguientes intereses: %s",
                prompt.destiny_location(), prompt.start(), prompt.end(), prompt.tags() != null ? String.join(", ", prompt.tags()) : "Ninguno específico");

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI devolvio una respuesta invalida");
            }

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode choicesNode = rootNode.path("choices");
            if (!choicesNode.isArray() || choicesNode.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI no devolvio opciones de respuesta");
            }

            String content = choicesNode.get(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI devolvio contenido vacio");
            }

            String sanitizedContent = extractJson(content);
            JsonNode contentNode;
            try {
                contentNode = objectMapper.readTree(sanitizedContent);
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo parsear el JSON devuelto por OpenAI", e);
            }

            String nombre = contentNode.path("nombre").asText("Itinerario generado");
            Date fechaInicio = parseDateOrNull(contentNode.path("fecha_inicio").asText(null), "fecha_inicio");
            Date fechaFinal = parseDateOrNull(contentNode.path("fecha_final").asText(null), "fecha_final");

            if (fechaInicio != null && fechaFinal != null && fechaInicio.after(fechaFinal)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI devolvio un rango de fechas invalido");
            }

            List<RequestActivity> actividades = new ArrayList<>();
            JsonNode ubicacionesNode = contentNode.path("ubicaciones");

            if (ubicacionesNode.isArray()) {
                for (JsonNode ubicacionNode : ubicacionesNode) {
                    String direccion = ubicacionNode.path("direccion").asText("").trim();
                    if (direccion.isEmpty()) {
                        continue;
                    }

                    Date fechaHoraInicial = parseDateOrNull(ubicacionNode.path("fecha_hora_inicial").asText(null), "fecha_hora_inicial");
                    Date fechaHoraFinal = parseDateOrNull(ubicacionNode.path("fecha_hora_final").asText(null), "fecha_hora_final");

                    if (fechaHoraInicial != null && fechaHoraFinal != null && fechaHoraFinal.before(fechaHoraInicial)) {
                        Date aux = fechaHoraInicial;
                        fechaHoraInicial = fechaHoraFinal;
                        fechaHoraFinal = aux;
                    }

                    actividades.add(new RequestActivity(null, null, direccion, fechaHoraFinal, fechaHoraInicial));
                }
            }

            return new ResponseGeneration(nombre, fechaInicio, fechaFinal, actividades);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al generar el itinerario con OpenAI", e);
        }
    }

    private String extractJson(String content) {
        String sanitized = content.trim();

        if (sanitized.startsWith("```")) {
            int firstBreak = sanitized.indexOf('\n');
            int lastFence = sanitized.lastIndexOf("```");
            if (firstBreak > -1 && lastFence > firstBreak) {
                sanitized = sanitized.substring(firstBreak + 1, lastFence).trim();
            }
        }

        int firstBrace = sanitized.indexOf('{');
        int lastBrace = sanitized.lastIndexOf('}');
        if (firstBrace == -1 || lastBrace == -1 || lastBrace <= firstBrace) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI no devolvio un JSON utilizable");
        }

        return sanitized.substring(firstBrace, lastBrace + 1);
    }

    private Date parseDateOrNull(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            return Date.from(Instant.parse(rawValue));
        } catch (DateTimeParseException ignored) {
            // Try next format
        }

        try {
            return Date.from(OffsetDateTime.parse(rawValue).toInstant());
        } catch (DateTimeParseException ignored) {
            // Try next format
        }

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(rawValue);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Formato de fecha invalido para '" + fieldName + "': " + rawValue);
        }
    }
}
