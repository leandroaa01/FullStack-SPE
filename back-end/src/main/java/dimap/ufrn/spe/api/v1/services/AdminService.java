package dimap.ufrn.spe.api.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dimap.ufrn.spe.api.v1.dtos.UpdateDTO;
import dimap.ufrn.spe.api.v1.models.User;
import dimap.ufrn.spe.api.v1.repositories.UserRepository;

@Service
public class AdminService {

    @Autowired
    UserRepository repository;

    public void validarDados(User bolsista, UpdateDTO dados) {
        if (dados.name() != null && !dados.name().isEmpty() && !dados.name().isBlank()) {
            bolsista.setName(dados.name());
        }
        if (dados.username() != null && !dados.username().isEmpty() && !dados.username().isBlank()) {
            bolsista.setUsername(dados.username());
        }
        if (dados.matricula() != null && !dados.matricula().isEmpty() && !dados.matricula().isBlank()) {
            bolsista.setMatricula(dados.matricula());
        }
        if (dados.email() != null && !dados.email().isEmpty() && !dados.email().isBlank()) {
            bolsista.setEmail(dados.email());
        }
        if (dados.role() != null) {
            bolsista.setRoles(dados.role());
        }
        if (dados.situacao() != null) {
            bolsista.setSituacao(dados.situacao());
        }
        if (dados.cargo() != null && !dados.cargo().isEmpty() && !dados.cargo().isBlank()) {
            bolsista.setCargo(dados.cargo());
        }
    }
}
