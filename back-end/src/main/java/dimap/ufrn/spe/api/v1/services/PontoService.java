package dimap.ufrn.spe.api.v1.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dimap.ufrn.spe.api.v1.dtos.BolsistaPontoDTO;
import dimap.ufrn.spe.api.v1.repositories.PontoRepository;

@Service
public class PontoService {

    @Autowired
    private PontoRepository pontoRepository;

    public List<BolsistaPontoDTO> listarTodosOsPontos() {
        var pontos = pontoRepository.findAll();

        return pontos.stream()
                     .map(p -> new BolsistaPontoDTO(
                         p.getBolsista().getName(),
                         p.getHoraDeEntrada(),
                         p.getHoraDeSaida(),
                         p.getPontoStatus(),
                         p.getQtdDeHorasFeitas()))
                     .toList();
    }


}
