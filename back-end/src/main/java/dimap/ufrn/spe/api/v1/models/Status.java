package dimap.ufrn.spe.api.v1.models;

public enum Status {
    
    DEFERIDO("DEFERIDO"), //0
    INDEFERIDO("INDEFERIDO"), //1
    EMANALISE("EMANALISE"), //2
    ABERTO("ABERTO"), //3
    FECHADO("FECHADO"); //4

    private String status;
Status(String situacao) {
    this.status = situacao;
}
public String getStatus() {
    return status;
}

}
