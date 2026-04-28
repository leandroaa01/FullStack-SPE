package dimap.ufrn.spe.api.v1.models;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "justificativa_ponto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Justificativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bolsista_id", nullable = false)
    private User bolsista;
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime data;

    @Column(nullable = false)
    private double qtdDeHoras;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private String justificativa;

    private Status justificaSituacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private User tecnico;

    @Column(nullable = false)
    private String justicativaTecnico;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dataJustificativaTecnico;

    public Justificativa(User bolsista, LocalDateTime data, double qtdDeHoras, String motivo, String justificativa,
            User tecnico, String justicativaTecnico) {
        this.bolsista = bolsista;
        this.data = data;
        this.qtdDeHoras = qtdDeHoras;
        this.motivo = motivo;
        this.justificativa = justificativa;
        this.tecnico = tecnico;
        this.justicativaTecnico = (justicativaTecnico == null) ? "" : justicativaTecnico;
        this.justificaSituacao = Status.EMANALISE;
    }

}
