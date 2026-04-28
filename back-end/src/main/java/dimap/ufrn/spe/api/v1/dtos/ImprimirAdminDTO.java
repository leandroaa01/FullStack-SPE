package dimap.ufrn.spe.api.v1.dtos;

import java.time.OffsetDateTime;

public record ImprimirAdminDTO(String username, OffsetDateTime dataInicio, OffsetDateTime dataFim) {
    
}
