package dimap.ufrn.spe.api.v1.controllers;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dimap.ufrn.spe.api.v1.dtos.DadosDTO;
import dimap.ufrn.spe.api.v1.dtos.HorarioSemanaDTO;
import dimap.ufrn.spe.api.v1.dtos.ImprimirDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaGetResponseDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaResponseDTO;
import dimap.ufrn.spe.api.v1.dtos.MyData;
import dimap.ufrn.spe.api.v1.dtos.PontoDTO;
import dimap.ufrn.spe.api.v1.models.User;
import dimap.ufrn.spe.api.v1.services.BolsistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.lang.NonNull;

@RestController
@RequestMapping("/spe/api/bolsista")
@Tag(name = "Bolsista", description = "Endpoints para gestão de pontos e dados do bolsista")
public class BolsistaController {

        private final BolsistaService bolsistaService;

        public BolsistaController(BolsistaService bolsistaService) {
                this.bolsistaService = bolsistaService;
        }

        @Operation(summary = "Obter dados do bolsista", description = "Endpoint para obter os dados do bolsista autenticado.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponse(responseCode = "200", description = "Dados do bolsista retornados com sucesso.", content = @Content(schema = @Schema(implementation = MyData.class)))
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.", content = @Content(schema = @Schema(implementation = String.class)))
        @PreAuthorize("hasRole('BOLSISTA')")
        @GetMapping("/me")
        public ResponseEntity<MyData> meusDados(@AuthenticationPrincipal User bolsista) {
                return ResponseEntity.ok(new MyData(
                                bolsista.getName(),
                                bolsista.getCargo(),
                                bolsista.getEmail(),
                                bolsista.getMatricula()));
        }

        @Operation(summary = "Justificar ponto", description = "Endpoint para justificar um ponto do bolsista.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponse(responseCode = "200", description = "Justificativa registrada com sucesso.", content = @Content(schema = @Schema(implementation = String.class)))
        @ApiResponse(responseCode = "400", description = "Dados inválidos para justificativa.", content = @Content(schema = @Schema(implementation = String.class)))
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.", content = @Content(schema = @Schema(implementation = String.class)))
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("hasRole('BOLSISTA')")
        @PostMapping("/justificar-ponto")
        public ResponseEntity<Map<String, String>> justificarPonto(@AuthenticationPrincipal User bolsista,
                        @RequestBody @Valid JustificativaDTO infos) {
                try {
                        return ResponseEntity.ok(bolsistaService.justificarPonto(bolsista, infos));
                } catch (ResponseStatusException exception) {
                        if (exception.getStatusCode().is4xxClientError()) {
                                return ResponseEntity.status(exception.getStatusCode())
                                                .body(Map.of("message", exception.getReason()));
                        }
                        throw exception;
                }
        }

        @Operation(summary = "Listar justificativas", description = "Endpoint para listar todas as justificativas do bolsista.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponse(responseCode = "200", description = "Justificativas listadas com sucesso.", content = @Content(schema = @Schema(implementation = JustificativaResponseDTO.class)))
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.", content = @Content(schema = @Schema(implementation = String.class)))
        @SecurityRequirement(name = "bearerAuth")
        @GetMapping("/minhas-justificativas")
        public ResponseEntity<List<JustificativaResponseDTO>> minhasJustificativas(@AuthenticationPrincipal User bolsista) {
                return ResponseEntity.ok(bolsistaService.listarJustificativas(bolsista));
        }

        @Operation(summary = "Detalhes da justificativa", description = "Endpoint para obter os detalhes de uma justificativa específica do bolsista.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")),
                        @Parameter(name = "id", description = "ID da justificativa a ser detalhada", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", example = "1"))
        })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Detalhes da justificativa retornados com sucesso.", content = @Content(schema = @Schema(implementation = JustificativaGetResponseDTO.class))),
                        @ApiResponse(responseCode = "403", description = "Acesso negado, sempermissão de bolsista.", content = @Content(schema = @Schema(implementation = String.class))),
                        @ApiResponse(responseCode = "404", description = "Justificativa não encontrada para o ID fornecido.", content = @Content(schema = @Schema(implementation = String.class)))
        })
        @SecurityRequirement(name = "bearerAuth")
        @GetMapping("/minhas-justificativas/{id}")
        public ResponseEntity<JustificativaGetResponseDTO> detalhesJustificativa(@AuthenticationPrincipal User bolsista,
                        @PathVariable @NonNull Long id) {
                return ResponseEntity.ok(bolsistaService.detalhesJustificativa(bolsista, id));
        }

        @Operation(summary = "Registrar ponto", description = "Endpoint para registrar a entrada ou saída do bolsista.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponse(responseCode = "200", description = "Ponto registrado com sucesso.", content = @Content(schema = @Schema(implementation = PontoDTO.class)))
        @ApiResponse(responseCode = "400", description = "Não é possível registrar ponto fora do horário de funcionamento.", content = @Content(schema = @Schema(implementation = DadosDTO.class)))
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.", content = @Content(schema = @Schema(implementation = String.class)))
        @PreAuthorize("hasRole('BOLSISTA')")
        @PostMapping("/registre-ponto")
        public ResponseEntity<String> registrarPonto(@AuthenticationPrincipal User bolsista) {
                return ResponseEntity.ok(bolsistaService.registrarPonto(bolsista));
        }

        
        @Operation(summary = "Visualizar pontos", description = "Endpoint para visualizar os pontos registrados pelo bolsista.", parameters = {@Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Pontos retornados com sucesso.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PontoDTO.class)))),
                        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.", content = @Content(schema = @Schema(implementation = String.class)))
        })
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("hasRole('BOLSISTA')")
        @GetMapping("/meus-pontos")
        public ResponseEntity<List<PontoDTO>> visualizarMeusPontos(@AuthenticationPrincipal User bolsista) {
                return ResponseEntity.ok(bolsistaService.listarPontos(bolsista));
        }

        @Operation(summary = "Obter total de horas", description = "Endpoint para obter o total de horas trabalhadas pelo bolsista.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Total de horas retornado com sucesso."),
                        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.")
        })
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("hasRole('BOLSISTA')")
        @GetMapping("/total-horas")
        public ResponseEntity<Double> getTotalHoras(@AuthenticationPrincipal User bolsista) {
                return ResponseEntity.ok(bolsistaService.calcularTotalHoras(bolsista));
        }

        @Operation(summary = "Meus dados", description = "Endpoint para o bolsista visualizar seus dados.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso."),
                        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.")
        })
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("hasRole('BOLSISTA')")
        @GetMapping("/meus-dados")
        public ResponseEntity<DadosDTO> atualizarMeusDados(@AuthenticationPrincipal User bolsista) {
                return ResponseEntity.ok(bolsistaService.obterMeusDados(bolsista));
        }

        @Operation(summary = "Enviar horários da semana", description = "Endpoint para salvar os horários selecionados pelo bolsista na semana.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponse(responseCode = "200", description = "Horários salvos com sucesso.", content = @Content(schema = @Schema(implementation = String.class)))
        @ApiResponse(responseCode = "400", description = "Dados inválidos para os horários.", content = @Content(schema = @Schema(implementation = String.class)))
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.", content = @Content(schema = @Schema(implementation = String.class)))
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("hasRole('BOLSISTA')")
        @PostMapping("/meus-horarios")
        public ResponseEntity<Map<String, String>> salvarHorarios(
                        @AuthenticationPrincipal User bolsista,
                        @RequestBody @Valid HorarioSemanaDTO horarioSemana) {
                try {
                        return ResponseEntity.ok(bolsistaService.salvarHorarios(bolsista, horarioSemana));
                } catch (ResponseStatusException exception) {
                        if (exception.getStatusCode().is4xxClientError()) {
                                return ResponseEntity.status(exception.getStatusCode())
                                                .body(Map.of("message", exception.getReason()));
                        }
                        throw exception;
                }
        }

        @Operation(summary = "Listar meus horários", description = "Endpoint para retornar os horários já salvos do bolsista autenticado.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponse(responseCode = "200", description = "Horários retornados com sucesso.", content = @Content(schema = @Schema(implementation = HorarioSemanaDTO.class)))
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.", content = @Content(schema = @Schema(implementation = String.class)))
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("hasRole('BOLSISTA')")
        @GetMapping("/meus-horarios")
        public ResponseEntity<HorarioSemanaDTO> meusHorarios(@AuthenticationPrincipal User bolsista) {
                return ResponseEntity.ok(bolsistaService.obterMeusHorarios(bolsista));
        }

        @Operation(summary = "Imprimir pontos", description = "Endpoint para gerar um PDF com os pontos do bolsista em um intervalo de datas.", parameters = {
                        @Parameter(name = "Authorization", description = "Token JWT no formato: **Bearer <token>**", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
        })
        @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso.")
        @ApiResponse(responseCode = "400", description = "Datas inválidas ou intervalo inválido.")
        @ApiResponse(responseCode = "403", description = "Acesso negado, sem permissão de bolsista.")
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("hasRole('BOLSISTA')")
        @PostMapping(value = "/imprimir-ponto", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<byte[]> imprimirPonto(@AuthenticationPrincipal User bolsista,
                        @RequestBody @Valid ImprimirDTO imprimirDTO) {

                if (imprimirDTO.dataInicio() == null || imprimirDTO.dataFim() == null) {
                        return ResponseEntity.badRequest().build();
                }

                if (imprimirDTO.dataFim().isBefore(imprimirDTO.dataInicio())) {
                        return ResponseEntity.badRequest().build();
                }

                try {
                        byte[] pdf = bolsistaService.imprimirPonto(bolsista, imprimirDTO);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        headers.setContentDisposition(ContentDisposition.attachment()
                                        .filename("pontos-" + bolsista.getUsername() + ".pdf")
                                        .build());

                        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
                } catch (IOException exception) {
                        throw new RuntimeException("Erro ao gerar PDF dos pontos", exception);
                }
        }

}
