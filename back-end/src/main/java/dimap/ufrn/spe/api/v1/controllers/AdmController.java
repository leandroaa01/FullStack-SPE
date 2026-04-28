
package dimap.ufrn.spe.api.v1.controllers;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dimap.ufrn.spe.api.v1.models.User;
import dimap.ufrn.spe.api.v1.dtos.BolsistaPontoDTO;
import dimap.ufrn.spe.api.v1.dtos.BolsistasDto;
import dimap.ufrn.spe.api.v1.dtos.ImprimirAdminDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaAdminDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaAdminReposponseDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaResponse1DTO;
import dimap.ufrn.spe.api.v1.dtos.MyData;
import dimap.ufrn.spe.api.v1.dtos.MyDataAdmin;
import dimap.ufrn.spe.api.v1.dtos.PasswordDTO;
import dimap.ufrn.spe.api.v1.dtos.PerfilDTO;
import dimap.ufrn.spe.api.v1.dtos.RegisterDTO;
import dimap.ufrn.spe.api.v1.dtos.UpdateDTO;
import dimap.ufrn.spe.api.v1.services.AdmService;
import dimap.ufrn.spe.api.v1.services.PontoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("spe/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Tecnico", description = "Endpoints técnicos para gestão de usuários e pontos")
public class AdmController {

    private final PontoService pontoService;
    private final AdmService admService;

    public AdmController(PontoService pontoService, AdmService admService) {
        this.pontoService = pontoService;
        this.admService = admService;
    }



     @Operation(summary = "Obter dados do admin", description = "Endpoint para obter os dados do admin autenticado.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponse(responseCode = "200", description = "Dados do admin retornados com sucesso.", content = @Content(schema = @Schema(implementation = MyData.class)))
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de admin.", content = @Content(schema = @Schema(implementation = String.class)))
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/me")
        public ResponseEntity<MyDataAdmin> meusDados(@AuthenticationPrincipal User admin) {
            return ResponseEntity.ok(new MyDataAdmin(admin.getName(), admin.getCargo(), admin.getEmail()));
        }

    @Operation(summary = "Registrar um novo usuário", description = "Endpoint para registrar um novo usuário no sistema.", parameters = {
            @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou já em uso", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão administrativa", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO data, BindingResult result) {
        if (result.hasErrors()) {
            var mensagem = result.getAllErrors().isEmpty()
                    ? "Dados inválidos."
                    : result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body("Dados inválidos: " + mensagem);
        }

        admService.registrarUsuario(data);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso");
    }

    @Operation(summary = "Listar todos os pontos", description = "Endpoint para listar todos os pontos registrados no sistema.", parameters = {
            @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pontos retornada com sucesso", content = @Content(schema = @Schema(implementation = BolsistaPontoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão administrativa", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/pontos/bolsistas/listagem")
    public ResponseEntity<List<BolsistaPontoDTO>> listarPontos(@AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(pontoService.listarTodosOsPontos());
    }

    @Operation(summary = "Atualizar senha do bolsista", description = "Endpoint para atualizar a senha do bolsista em caso de esquecimento ou troca de senha.", parameters = {
            @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
    }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Objeto contendo a nova senha.", required = true, content = @Content(schema = @Schema(implementation = PasswordDTO.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro de validação, senha vazia ou fraca."),
            @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/mudar-senha/bolsista/")
    public ResponseEntity<String> mudarSenha(@AuthenticationPrincipal User admin, @RequestBody @Valid PasswordDTO dado) {
        try {
            admService.atualizarSenhaBolsista(dado.matricula(), dado.senhaNova(), dado.senhaConfirmacao());
            return ResponseEntity.ok("Senha atualizada com sucesso");
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
            }
            throw exception;
        }
    }

    @Operation(summary = "Atualizar dados do bolsista", description = "Endpoint para atualizar os dados do bolsista.", parameters = {
            @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
            @Parameter(name = "id", description = "ID do bolsista a ter os dados atualizados", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64", example = "42"))
    }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Objeto contendo os novos dados do bolsista.", required = true, content = @Content(schema = @Schema(implementation = UpdateDTO.class))))
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/mudar-dados/bolsista/{id}")
    public ResponseEntity<String> mudarDados(@PathVariable("id") @NonNull Long id, @RequestBody @Valid UpdateDTO dados) {

        try {
            admService.atualizarDadosBolsista(id, dados);
            return ResponseEntity.ok("Dados do usuário atualizados com sucesso.");
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
            }
            throw exception;
        }
    }

 @Operation(
    summary = "Dados do perfil do Bolsista",
    description = "Endpoint que retorna os dados do bolsista de forma individual",
    parameters = {
        @Parameter(
            name = "id",
            description = "ID do bolsista para consulta",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = "integer", format = "int64")
        )
    }
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
    @ApiResponse(responseCode = "400", description = "Erro de ID inválido"),
    @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de admin."),
    @ApiResponse(responseCode = "404", description = "Bolsista não encontrado")
})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/bolsistas/dados/perfil/{id}")
public ResponseEntity<PerfilDTO> perfilBolsista(
    @PathVariable("id") @NonNull Long id,
        @AuthenticationPrincipal User admin) {

    return ResponseEntity.ok(admService.obterPerfilBolsista(id));
}

@GetMapping("/listagem/bolsistas")
@Operation(summary = "Listar bolsistas", description = "Endpoint para listar todos os bolsistas registrados no sistema.", parameters = {
        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
})
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de bolsistas retornada com sucesso", content = @Content(schema = @Schema(implementation = BolsistasDto.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão administrativa", content = @Content(schema = @Schema(implementation = String.class)))    
})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<BolsistasDto>> listarBolsistas(@AuthenticationPrincipal User admin) {
    return ResponseEntity.ok(admService.listarBolsistas());
}

@GetMapping("/listagem/justificativas")
@Operation(summary = "Listar justificativas", description = "Endpoint para listar todas as justificativas registradas no sistema.", parameters = {
        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
})
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de justificativas retornada com sucesso", content = @Content(schema = @Schema(implementation = JustificativaResponse1DTO.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão administrativa", content = @Content(schema = @Schema(implementation = String.class)))
})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<JustificativaResponse1DTO>> listarJustificativas(@AuthenticationPrincipal User admin) {
    return ResponseEntity.ok(admService.listarJustificativas());
}


@Operation(summary = "Obter detalhes da justificativa", description = "Endpoint para obter os detalhes de uma justificativa específica.", parameters = {
        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
        @Parameter(name = "id", description = "ID da justificativa para consulta", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))    
})
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalhes da justificativa retornados com sucesso", content = @Content(schema = @Schema(implementation = JustificativaDTO.class))),
        @ApiResponse(responseCode = "400", description = "Erro de ID inválido"),        
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão administrativa"),
        @ApiResponse(responseCode = "404", description = "Justificativa não encontrada")
})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')") 
@GetMapping("/justificativa/{idBolsista}/detalhes/{idJustificativa}")  
public ResponseEntity<JustificativaAdminDTO> detalhesJustificativa(
    @PathVariable("idBolsista") @NonNull Long idBolsista,
    @PathVariable("idJustificativa") @NonNull Long idJustificativa,
        @AuthenticationPrincipal User admin) {
    return ResponseEntity.ok(admService.obterDetalhesJustificativa(idJustificativa));
    }

@PutMapping("/justificativa/{idBolsista}/detalhes/{idJustificativa}/atualizar")
@Operation(summary = "Atualizar justificativa do bolsista", description = "Endpoint para atualizar os detalhes de uma justificativa específica.", parameters = {
        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
        @Parameter(name = "idBolsista", description = "ID do bolsista relacionado à justificativa", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64")),
        @Parameter(name = "idJustificativa", description = "ID da justificativa para atualização", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
}, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Objeto contendo os novos detalhes da justificativa.", required = true, content = @Content(schema = @Schema(implementation = JustificativaAdminReposponseDTO.class))))
@SecurityRequirement(name = "bearerAuth")               
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> atualizarJustificativa(
    @PathVariable("idBolsista") @NonNull Long idBolsista,
    @PathVariable("idJustificativa") @NonNull Long idJustificativa,
        @RequestBody @Valid JustificativaAdminReposponseDTO conclucaoJustificativa,
        @AuthenticationPrincipal User admin) {
    admService.atualizarJustificativa(idJustificativa, conclucaoJustificativa, admin);
    return ResponseEntity.ok("Justificativa atualizada com sucesso");
}

@GetMapping("/imprimir-ponto/bolsista/")
@Operation(summary = "Gerar PDF do ponto do bolsista", description = "Endpoint para gerar um PDF contendo os registros de ponto de um bolsista específico.", parameters = {
        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
    @Parameter(name = "username", description = "Username do bolsista para consulta dos pontos", required = true, in = ParameterIn.QUERY, schema = @Schema(type = "string")),
    @Parameter(name = "dataInicio", description = "Data/hora inicial (ISO 8601, ex: 2026-04-10T14:07:39.377Z)", required = true, in = ParameterIn.QUERY, schema = @Schema(type = "string")),
    @Parameter(name = "dataFim", description = "Data/hora final (ISO 8601, ex: 2026-04-12T14:07:39.377Z)", required = true, in = ParameterIn.QUERY, schema = @Schema(type = "string"))
})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<byte[]> imprimirPonto(@AuthenticationPrincipal User admin, @ModelAttribute ImprimirAdminDTO dados) {
    var bolsista = admService.obterBolsistaPorUsernameOuErro(dados.username());
    if (dados.dataInicio() == null || dados.dataFim() == null) {
        return ResponseEntity.badRequest().build();
    }

    if (dados.dataFim().isBefore(dados.dataInicio())) {
        return ResponseEntity.badRequest().build();
    }
    var pdfGerado = admService.gerarPdfDePontos(admin, bolsista, dados);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(ContentDisposition.attachment().filename(pdfGerado.nomeArquivo()).build());

    return new ResponseEntity<>(pdfGerado.conteudo(), headers, HttpStatus.OK);
    }


}

    
