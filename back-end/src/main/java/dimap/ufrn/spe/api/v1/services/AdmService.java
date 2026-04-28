package dimap.ufrn.spe.api.v1.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dimap.ufrn.spe.api.v1.dtos.BolsistasDto;
import dimap.ufrn.spe.api.v1.dtos.HorarioDiaDTO;
import dimap.ufrn.spe.api.v1.dtos.HorarioSemanaDTO;
import dimap.ufrn.spe.api.v1.dtos.ImprimirAdminDTO;
import dimap.ufrn.spe.api.v1.dtos.ImprimirDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaAdminDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaAdminReposponseDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaResponse1DTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaResponseDTO;
import dimap.ufrn.spe.api.v1.dtos.MyData;
import dimap.ufrn.spe.api.v1.dtos.PerfilDTO;
import dimap.ufrn.spe.api.v1.dtos.PontoDTO;
import dimap.ufrn.spe.api.v1.dtos.RegisterDTO;
import dimap.ufrn.spe.api.v1.dtos.UpdateDTO;
import dimap.ufrn.spe.api.v1.models.Justificativa;
import dimap.ufrn.spe.api.v1.models.Ponto;
import dimap.ufrn.spe.api.v1.models.Status;
import dimap.ufrn.spe.api.v1.models.User;
import dimap.ufrn.spe.api.v1.repositories.HorarioRepository;
import dimap.ufrn.spe.api.v1.repositories.JustificativaRepository;
import dimap.ufrn.spe.api.v1.repositories.PontoRepository;
import dimap.ufrn.spe.api.v1.repositories.UserRepository;

@Service
public class AdmService {

    private static final DateTimeFormatter DATA_HORA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final JustificativaRepository justificativaRepository;
    private final PontoRepository pontoRepository;
    private final HorarioRepository horarioRepository;

    public record PdfGerado(String nomeArquivo, byte[] conteudo) {}

    public AdmService(
            AdminService adminService,
            UserRepository userRepository,
            JustificativaRepository justificativaRepository,
            PontoRepository pontoRepository,
            HorarioRepository horarioRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
        this.justificativaRepository = justificativaRepository;
        this.pontoRepository = pontoRepository;
        this.horarioRepository = horarioRepository;
    }

    public void registrarUsuario(RegisterDTO data) {
        String senhaCriptografada = new BCryptPasswordEncoder().encode(data.password());
        User novoUsuario = new User(
                data.name(),
                data.username(),
                data.matricula(),
                senhaCriptografada,
                data.email(),
                data.roles(),
                data.cargo());
        novoUsuario.setDataCriacao(LocalDateTime.now());
        userRepository.save(novoUsuario);
    }

    public void atualizarSenhaBolsista(String matricula, String senhaNova, String senhaConfirmacao) {
        User bolsista = userRepository.findByMatricula(matricula);
        if (bolsista == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro: bolsista não encontrado.");
        }
        if (senhaNova == null || senhaNova.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro: senha vazia.");
        }
        if (senhaNova.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha muito fraca, mínimo 8 caracteres.");
        }
        if (!senhaNova.equals(senhaConfirmacao)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As senhas não coincidem.");
        }

        var encoder = new BCryptPasswordEncoder();
        bolsista.setPassword(encoder.encode(senhaNova));
        userRepository.save(bolsista);
    }

    public void atualizarDadosBolsista(@NonNull Long id, UpdateDTO dados) {
        User bolsista = userRepository.findById(id).orElse(null);
        if (bolsista == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro: bolsista não encontrado.");
        }
        adminService.validarDados(bolsista, dados);
        userRepository.save(bolsista);
    }

    public PerfilDTO obterPerfilBolsista(@NonNull Long idBolsista) {
        User alvo = userRepository.findById(idBolsista)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bolsista não encontrado"));

        MyData dadosBolsista = new MyData(
                alvo.getName(),
                alvo.getCargo(),
                alvo.getEmail(),
                alvo.getMatricula());

        var dias = horarioRepository.findAllByBolsistaOrderByIdAsc(alvo)
                .stream()
                .map(horario -> new HorarioDiaDTO(
                        horario.getDia(),
                        horario.getHorariosSelecionados(),
                        horario.getTotalHoras()))
                .toList();

        var horariosBolsista = new HorarioSemanaDTO(dias);

        List<PontoDTO> pontosBolsista = alvo.getPontos()
                .stream()
                .map(ponto -> new PontoDTO(
                        ponto.getHoraDeEntrada(),
                        ponto.getHoraDeSaida(),
                        String.valueOf(ponto.getQtdDeHorasFeitas())))
                .toList();

        List<JustificativaResponseDTO> justificativasBolsista = justificativaRepository
                .findAllByBolsistaOrderByDataDesc(alvo)
                .stream()
                .map(j -> new JustificativaResponseDTO(
                        j.getData(),
                        j.getQtdDeHoras(),
                        j.getMotivo(),
                        j.getJustificativa(),
                        j.getJustificaSituacao(),
                        j.getId()))
                .toList();

        return new PerfilDTO(dadosBolsista, horariosBolsista, pontosBolsista, justificativasBolsista);
    }

    public List<BolsistasDto> listarBolsistas() {
        var users = userRepository.findAll();
        return users.stream()
                .filter(user -> user.getRoles().getRole().equals("BOLSISTA"))
                .map(user -> new BolsistasDto(
                        user.getId(),
                        user.getName(),
                        user.getUsername(),
                        user.getMatricula(),
                        user.getEmail(),
                        user.getSituacao(),
                        user.getDataCriacao(),
                        user.getCargo()))
                .toList();
    }

    public List<JustificativaResponse1DTO> listarJustificativas() {
        return justificativaRepository.findAll()
                .stream()
                .map(j -> new JustificativaResponse1DTO(
                        j.getData(),
                        j.getQtdDeHoras(),
                        j.getMotivo(),
                        j.getJustificativa(),
                        j.getJustificaSituacao(),
                        j.getId(),
                        j.getBolsista().getName(),
                        j.getBolsista().getId()))
                .toList();
    }

    public JustificativaAdminDTO obterDetalhesJustificativa(@NonNull Long idJustificativa) {
        Justificativa justificativa = justificativaRepository.findById(idJustificativa)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Justificativa não encontrada"));

        return new JustificativaAdminDTO(
                justificativa.getData(),
                justificativa.getQtdDeHoras(),
                justificativa.getMotivo(),
                justificativa.getJustificativa(),
                justificativa.getJustificaSituacao());
    }

        public void atualizarJustificativa(
            @NonNull Long idJustificativa,
            JustificativaAdminReposponseDTO conclusaoJustificativa,
            User tecnico) {
        Justificativa justificativa = justificativaRepository.findById(idJustificativa)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Justificativa não encontrada"));

        var statusAtual = justificativa.getJustificaSituacao();
        var dataJustificativaTecnico = justificativa.getDataJustificativaTecnico();

        if (statusAtual == Status.DEFERIDO) {
            LocalDateTime dataLimite = dataJustificativaTecnico.plusDays(15);
            if (LocalDateTime.now().isAfter(dataLimite)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Não é possível alterar a justificativa após 15 dias do seu cadastramento");
            }
        }

        justificativa.setTecnico(tecnico);
        justificativa.setJustificaSituacao(conclusaoJustificativa.status());
        justificativa.setJusticativaTecnico(conclusaoJustificativa.observacoes());
        justificativa.setDataJustificativaTecnico(LocalDateTime.now());
        justificativaRepository.save(justificativa);
    }

    public User obterBolsistaPorUsernameOuErro(String username) {
        var bolsista = userRepository.findByUsername(username);
        if (bolsista == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bolsista não encontrado");
        }
        return bolsista;
    }

    public PdfGerado gerarPdfDePontos(User admin, ImprimirAdminDTO dados) {
        var bolsista = obterBolsistaPorUsernameOuErro(dados.username());
        return gerarPdfDePontos(admin, bolsista, dados);
    }

    public PdfGerado gerarPdfDePontos(User admin, User bolsista, ImprimirAdminDTO dados) {

        var inicioLocal = dados.dataInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        var fimLocal = dados.dataFim().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        ImprimirDTO imprimirDTO = new ImprimirDTO(inicioLocal, fimLocal);

        var pontos = pontoRepository.findAllByBolsista(bolsista).stream()
                .filter(ponto -> ponto.getHoraDeEntrada() != null)
                .filter(ponto -> !ponto.getHoraDeEntrada().isBefore(imprimirDTO.dataInicio())
                        && !ponto.getHoraDeEntrada().isAfter(imprimirDTO.dataFim()))
                .sorted((primeiro, segundo) -> primeiro.getHoraDeEntrada()
                        .compareTo(segundo.getHoraDeEntrada()))
                .toList();

        try {
            byte[] pdf = gerarPdfDePontos(admin, bolsista, imprimirDTO, pontos);
            return new PdfGerado("pontos-" + bolsista.getUsername() + ".pdf", pdf);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar PDF dos pontos", exception);
        }
    }

    private byte[] gerarPdfDePontos(User admin, User bolsista, ImprimirDTO imprimirDTO, List<Ponto> pontos) throws IOException {
        var justificativas = justificativaRepository.findAllByBolsistaOrderByDataDesc(bolsista).stream()
                .filter(justificativa -> justificativa.getData() != null)
                .filter(justificativa -> !justificativa.getData().isBefore(imprimirDTO.dataInicio())
                        && !justificativa.getData().isAfter(imprimirDTO.dataFim()))
                .toList();

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float marginLeft = 50f;
            float marginTop = 50f;
            float currentY = page.getMediaBox().getHeight() - marginTop;
            float lineSpacing = 16f;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                String titulo = "Relatorio de pontos";
                float tituloFontSize = 14f;
                float pageWidth = page.getMediaBox().getWidth();
                float tituloWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(titulo) / 1000f) * tituloFontSize;
                float tituloX = (pageWidth - tituloWidth) / 2f;

                contentStream.beginText();
                contentStream.setLeading(lineSpacing);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, tituloFontSize);
                contentStream.newLineAtOffset(tituloX, currentY);
                contentStream.showText(titulo);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setLeading(lineSpacing);
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(marginLeft, currentY - lineSpacing);
                contentStream.showText("Bolsista: " + bolsista.getName());
                contentStream.newLine();
                contentStream.showText("Email: " + bolsista.getEmail());
                contentStream.newLine();
                contentStream.showText("Cargo: " + bolsista.getCargo());
                contentStream.newLine();
                contentStream.showText("Periodo: "
                        + imprimirDTO.dataInicio().format(DATA_HORA_FORMATTER)
                        + " ate "
                        + imprimirDTO.dataFim().format(DATA_HORA_FORMATTER));
                contentStream.newLine();
                contentStream.newLine();

                contentStream.showText("Justificativas no periodo:");
                contentStream.newLine();

                if (justificativas.isEmpty()) {
                    contentStream.showText("Nenhuma justificativa encontrada no periodo informado.");
                    contentStream.newLine();
                } else {
                    for (Justificativa justificativa : justificativas) {
                        String linhaJustificativa = String.format(
                                "Data: %s | Horas: %s | Motivo: %s | Status: %s",
                                formatarData(justificativa.getData()),
                                justificativa.getQtdDeHoras(),
                                justificativa.getMotivo(),
                                justificativa.getJustificaSituacao());
                        contentStream.showText(linhaJustificativa);
                        contentStream.newLine();
                    }
                }

                contentStream.newLine();
                contentStream.showText("Pontos no periodo:");
                contentStream.newLine();

                if (pontos.isEmpty()) {
                    contentStream.showText("Nenhum ponto encontrado no periodo informado.");
                } else {
                    for (Ponto ponto : pontos) {
                        String linha = String.format(
                                "Entrada: %s | Saida: %s | Horas: %s ",
                                formatarData(ponto.getHoraDeEntrada()),
                                formatarData(ponto.getHoraDeSaida()),
                                ponto.getQtdDeHorasFeitas());

                        contentStream.showText(linha);
                        contentStream.newLine();
                    }
                }

                var totalHoras = pontos.stream()
                        .filter(p -> p.getPontoStatus().equals(Status.FECHADO))
                        .mapToDouble(Ponto::getQtdDeHorasFeitas)
                        .sum();
                var totalHorasJustificadas = justificativas.stream()
                        .filter(j -> j.getJustificaSituacao().equals(Status.DEFERIDO))
                        .mapToDouble(Justificativa::getQtdDeHoras)
                        .sum();

                totalHoras += totalHorasJustificadas;
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.showText("Total de horas no periodo: " + String.format("%.2f", totalHoras) + " Hrs");
                contentStream.newLine();
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                contentStream.setLeading(11f);
                contentStream.newLineAtOffset(marginLeft, 40f);
                contentStream.showText(
                        "Relatório gerado em: "
                                + LocalDateTime.now().format(DATA_HORA_FORMATTER)
                                + " por "
                                + admin.getName());
                contentStream.newLine();
                contentStream.showText(
                        "© Dimap/UFRN - Departamento de Informática e Matemática Aplicada da Universidade Federal do Rio Grande do Norte");
                contentStream.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String formatarData(LocalDateTime data) {
        if (data == null) {
            return "-";
        }
        return data.format(DATA_HORA_FORMATTER);
    }
}
