package dimap.ufrn.spe.api.v1.models;

public enum Situacao {
    ATIVO("ATIVO"),
    DESATIVADO("DESATIVADO");
 
    private String situacao;
    
    Situacao(String status) {
       this.situacao = status;
    }
     public String getSituacao() {
         return situacao;
     }
}
