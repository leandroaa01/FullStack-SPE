package dimap.ufrn.spe.api.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dimap.ufrn.spe.api.v1.dtos.AuthenticationDTO;
import dimap.ufrn.spe.api.v1.dtos.LoginResponseDTO;
import dimap.ufrn.spe.api.v1.models.Situacao;
import dimap.ufrn.spe.api.v1.models.User;
import dimap.ufrn.spe.api.v1.repositories.UserRepository;
import dimap.ufrn.spe.api.v1.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
    
    @RestController
    @RequestMapping("auth")
    @Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
    public class AuthenticationController {
        @Autowired
        private AuthenticationManager authenticationManager;
        @Autowired
        private UserRepository repository;
        @Autowired
        private TokenService tokenService;
    
        @Operation(summary = "Realizar login", description = "Endpoint para autenticar um usuário e gerar um token de acesso.")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso", 
                     content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos", 
                     content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas ou não autorizadas", 
                     content = @Content(schema = @Schema(implementation = String.class)))
        })
        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDTO data) {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());
            User user = repository.findByUsername(data.username());
            if (user == null) {
                return ResponseEntity.status(401).body("Credenciais inválidas.");
            }
            var situacao = user.getSituacao();
            if (Situacao.DESATIVADO.equals(situacao)) {
                return ResponseEntity.status(401).body("Usuário inativo. Contate o administrador.");
            }
            var auth = this.authenticationManager.authenticate(usernamePassword);
    
            var token = tokenService.generateToken((User) auth.getPrincipal());

            return ResponseEntity.ok(new LoginResponseDTO(token, ((User) auth.getPrincipal()).getRoles()));
        }
    
    }
