package dimap.ufrn.spe.api.v1.dtos;

import dimap.ufrn.spe.api.v1.models.Roles;

public record RegisterDTO(String name, String username,String matricula, String password, String email, Roles roles, String cargo) {
    
}
