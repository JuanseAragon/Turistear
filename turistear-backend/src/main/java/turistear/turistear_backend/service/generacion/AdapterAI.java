package turistear.turistear_backend.service.generacion;

import turistear.turistear_backend.dto.RequestGeneration;
import turistear.turistear_backend.dto.ResponseGeneration;

public interface AdapterAI {
    public ResponseGeneration generate(RequestGeneration prompt);
}
