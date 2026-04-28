package dimap.ufrn.spe.api.v1.models;

import java.time.*;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pontos")
public class Ponto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime horaDeEntrada;
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime horaDeSaida;
    private Status pontoStatus;
    private double qtdDeHorasFeitas;

    @ManyToOne
    @JoinColumn(name = "bolsista_id")
    private User bolsista;

    public void calcularHorasFeitas() {
        if (isHorarioValido()) {
            this.qtdDeHorasFeitas = calcularDiferencaHoras(horaDeEntrada, horaDeSaida);
        }
    }

    private boolean isHorarioValido() {
        return horaDeEntrada != null && horaDeSaida != null && !horaDeSaida.isBefore(horaDeEntrada);
    }

    public LocalDateTime  setHoraDeSaida(LocalDateTime horaDeSaida) {
        this.horaDeSaida = horaDeSaida;
        calcularHorasFeitas();
        LocalDateTime dataCorreta;
         var horaFeita = this.qtdDeHorasFeitas;
        if( horaFeita > 9){
          if(this.getHoraDeEntrada().getHour()< 12){
             dataCorreta = this.horaDeEntrada.withHour(12).withMinute(0);
             this.horaDeSaida = dataCorreta;
          } else if(this.getHoraDeEntrada().getHour() < 17){    
            dataCorreta = this.horaDeEntrada.withHour(17).withMinute(0);
            this.horaDeSaida = dataCorreta;
        }
    }
    calcularHorasFeitas();
    return this.horaDeSaida;
}

    private double calcularDiferencaHoras(LocalDateTime entrada, LocalDateTime saida) {
        final int MINUTES_IN_AN_HOUR = 60;
        long minutos = Duration.between(entrada, saida).toMinutes();
        return Math.round((double) minutos / MINUTES_IN_AN_HOUR * 100.0) / 100.0;
    }
}
