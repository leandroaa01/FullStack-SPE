package dimap.ufrn.spe.api.v1.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dimap.ufrn.spe.api.v1.dtos.DadosDTO;
import dimap.ufrn.spe.api.v1.dtos.HorarioDiaDTO;
import dimap.ufrn.spe.api.v1.dtos.HorarioSemanaDTO;
import dimap.ufrn.spe.api.v1.dtos.ImprimirDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaGetResponseDTO;
import dimap.ufrn.spe.api.v1.dtos.JustificativaResponseDTO;
import dimap.ufrn.spe.api.v1.dtos.PontoDTO;
import dimap.ufrn.spe.api.v1.models.Horario;
import dimap.ufrn.spe.api.v1.models.Justificativa;
import dimap.ufrn.spe.api.v1.models.Ponto;
import dimap.ufrn.spe.api.v1.models.Status;
import dimap.ufrn.spe.api.v1.models.User;
import dimap.ufrn.spe.api.v1.repositories.HorarioRepository;
import dimap.ufrn.spe.api.v1.repositories.JustificativaRepository;
import dimap.ufrn.spe.api.v1.repositories.PontoRepository;
import dimap.ufrn.spe.api.v1.repositories.UserRepository;

@Service
public class BolsistaService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Fortaleza");
    private static final DateTimeFormatter DATA_HORA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PontoRepository pontoRepository;
    private final UserRepository userRepository;
    private final JustificativaRepository justificativaRepository;
    private final HorarioRepository horarioRepository;

    public BolsistaService(
            PontoRepository pontoRepository,
            UserRepository userRepository,
            JustificativaRepository justificativaRepository,
            HorarioRepository horarioRepository) {
        this.pontoRepository = pontoRepository;
        this.userRepository = userRepository;
        this.justificativaRepository = justificativaRepository;
        this.horarioRepository = horarioRepository;
    }

    public Map<String, String> justificarPonto(User bolsista, JustificativaDTO infos) {
        if (infos.data() == null || infos.qtdDeHoras() <= 0 || infos.motivo() == null || infos.justificativa() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados inválidos para justificativa.");
        }

        LocalDate dataJustificativa = infos.data().toLocalDate();

        if (dataJustificativa.isAfter(LocalDate.now(BUSINESS_ZONE))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido justificar dias futuros.");
        }

        var justificativaExistente = justificativaRepository.findByBolsistaAndData(bolsista, dataJustificativa);
        if (justificativaExistente.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe uma justificativa para este dia.");
        }

        if (dataJustificativa.getDayOfWeek().getValue() == 6 || dataJustificativa.getDayOfWeek().getValue() == 7) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é permitido justificar pontos para finais de semana.");
        }

        Justificativa justificativa = new Justificativa(
                bolsista,
                infos.data(),
                infos.qtdDeHoras(),
                infos.motivo(),
                infos.justificativa(),
                null,
                null);

        justificativaRepository.save(justificativa);
        return Map.of("message", "Justificativa registrada com sucesso!");
    }

    public List<JustificativaResponseDTO> listarJustificativas(User bolsista) {
        return justificativaRepository.findAllByBolsistaOrderByDataDesc(bolsista)
                .stream()
                .map(j -> new JustificativaResponseDTO(
                        j.getData(),
                        j.getQtdDeHoras(),
                        j.getMotivo(),
                        j.getJustificativa(),
                        j.getJustificaSituacao(),
                        j.getId()))
                .toList();
    }

    public JustificativaGetResponseDTO detalhesJustificativa(User bolsista, @NonNull Long id) {
        Optional<Justificativa> justificativa = justificativaRepository.findByIdAndBolsista(id, bolsista);
        String nomeTecnico = null;

        if (justificativa.isPresent()) {
            var tecnico = justificativa.get().getTecnico();
            if (tecnico != null) {
                nomeTecnico = tecnico.getName();
            }
        }

        return new JustificativaGetResponseDTO(
                justificativa.get().getData(),
                justificativa.get().getQtdDeHoras(),
                justificativa.get().getMotivo(),
                justificativa.get().getJustificativa(),
                justificativa.get().getJustificaSituacao(),
                justificativa.get().getId(),
                justificativa.get().getDataJustificativaTecnico(),
                justificativa.get().getJusticativaTecnico(),
                nomeTecnico);
    }

    public String registrarPonto(User bolsista) {
        var agora = LocalDateTime.now(BUSINESS_ZONE);
        Ponto pontoAberto = pontoRepository.findByBolsistaAndPontoStatus(bolsista, Status.ABERTO);

        LocalTime agoraTime = agora.toLocalTime();
        boolean dentroDaManha = !agoraTime.isBefore(LocalTime.of(7, 0)) && !agoraTime.isAfter(LocalTime.of(12, 0));
        boolean dentroDaTarde = !agoraTime.isBefore(LocalTime.of(13, 0)) && !agoraTime.isAfter(LocalTime.of(17, 0));

        if (!dentroDaManha && !dentroDaTarde) {
            return "Fora do horário permitido para registro de ponto. Manhã: 07:00-12:00 / Tarde: 13:00-17:00";
        }

        if (agora.getDayOfWeek().getValue() == 6 || agora.getDayOfWeek().getValue() == 7) {
            return "Não é permitido registrar pontos durante o final de semana.";
        }

        if (pontoAberto == null) {
            Ponto pontoEntrada = new Ponto();
            pontoEntrada.setBolsista(bolsista);
            pontoEntrada.setHoraDeEntrada(agora);
            pontoEntrada.setPontoStatus(Status.ABERTO);
            pontoRepository.save(pontoEntrada);
            return "Ponto de entrada registrado com sucesso!";
        }

        pontoAberto.setHoraDeSaida(agora);
        pontoAberto.calcularHorasFeitas();
        pontoAberto.setPontoStatus(Status.FECHADO);
        pontoRepository.save(pontoAberto);
        return "Ponto de saída registrado com sucesso!";
    }

    public List<PontoDTO> listarPontos(User bolsista) {
        var pontos = pontoRepository.findAllByBolsista(bolsista);
        return pontos.stream()
                .map(p -> new PontoDTO(
                        p.getHoraDeEntrada(),
                        p.getHoraDeSaida(),
                        p.getQtdDeHorasFeitas() + ""))
                .toList();
    }

    public Double calcularTotalHoras(User bolsista) {
        var pontos = pontoRepository.findAllByBolsista(bolsista);
        double totalHoras = pontos.stream()
                .filter(p -> p.getPontoStatus().equals(Status.FECHADO))
                .mapToDouble(Ponto::getQtdDeHorasFeitas)
                .sum();

        var umMesAtras = LocalDateTime.now(BUSINESS_ZONE).minusMonths(1);
        var justificativas = justificativaRepository.findAllByBolsistaOrderByDataDesc(bolsista).stream()
                .filter(j -> j.getData() != null && !j.getData().isBefore(umMesAtras))
                .filter(j -> j.getJustificaSituacao().equals(Status.DEFERIDO))
                .toList();

        var totalHorasJustificadas = justificativas.stream()
                .mapToDouble(Justificativa::getQtdDeHoras)
                .sum();

        return Math.round((totalHoras + totalHorasJustificadas) * 100.0) / 100.0;
    }

    public DadosDTO obterMeusDados(User bolsista) {
        var dados = userRepository.findById(bolsista.getId());
        return new DadosDTO(dados.get().getName(), dados.get().getUsername(), dados.get().getEmail());
    }

    public Map<String, String> salvarHorarios(User bolsista, HorarioSemanaDTO horarioSemana) {
        if (horarioSemana.dias() == null || horarioSemana.dias().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum horário foi enviado.");
        }

        horarioRepository.findAllByBolsista(bolsista).forEach(horarioRepository::delete);

        for (var dia : horarioSemana.dias()) {
            if (dia.dia() == null || dia.dia().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O dia da semana é obrigatório.");
            }

            var horarioExistente = horarioRepository.findAllByBolsistaAndDia(bolsista, dia.dia());
            horarioExistente.forEach(horarioRepository::delete);

            Horario horario = new Horario();
            horario.setBolsista(bolsista);
            horario.setDia(dia.dia());
            horario.setHorariosSelecionados(dia.horariosSelecionados());
            horario.setTotalHoras(dia.totalHoras());
            horarioRepository.save(horario);
        }

        return Map.of("message", "Horários salvos com sucesso!");
    }

    public HorarioSemanaDTO obterMeusHorarios(User bolsista) {
        var dias = horarioRepository.findAllByBolsistaOrderByIdAsc(bolsista)
                .stream()
                .map(horario -> new HorarioDiaDTO(
                        horario.getDia(),
                        horario.getHorariosSelecionados(),
                        horario.getTotalHoras()))
                .toList();

        return new HorarioSemanaDTO(dias);
    }

    public byte[] imprimirPonto(User bolsista, ImprimirDTO imprimirDTO) throws IOException {
        var pontos = pontoRepository.findAllByBolsista(bolsista).stream()
                .filter(ponto -> ponto.getHoraDeEntrada() != null)
                .filter(ponto -> !ponto.getHoraDeEntrada().isBefore(imprimirDTO.dataInicio())
                        && !ponto.getHoraDeEntrada().isAfter(imprimirDTO.dataFim()))
                .sorted((primeiro, segundo) -> primeiro.getHoraDeEntrada().compareTo(segundo.getHoraDeEntrada()))
                .toList();

        return gerarPdfDePontos(bolsista, imprimirDTO, pontos);
    }

    private byte[] gerarPdfDePontos(User bolsista, ImprimirDTO imprimirDTO, List<Ponto> pontos) throws IOException {
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
                        "Relatório gerado em: " + LocalDateTime.now().format(DATA_HORA_FORMATTER));
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
