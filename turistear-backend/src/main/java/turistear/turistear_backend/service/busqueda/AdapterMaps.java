package turistear.turistear_backend.service.busqueda;

import turistear.turistear_backend.dto.RequestActivity;

import java.util.List;

public interface AdapterMaps {

    public List<RequestActivity> searchInfo(List<RequestActivity> actividadesPreAI);

}