package dimap.ufrn.spe.api.v1.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meus_horarios")
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bolsista_id", nullable = false)
    private User bolsista;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "horario_slots", joinColumns = @JoinColumn(name = "horario_id"))
    @Column(name = "slot", nullable = false)
    private List<String> horariosSelecionados;

    @Column(nullable = false)
    private Integer totalHoras;
}
