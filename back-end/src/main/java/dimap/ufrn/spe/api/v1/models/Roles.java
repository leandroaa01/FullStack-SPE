package dimap.ufrn.spe.api.v1.models;

public enum Roles {
    ADMIN("ADMIN"),
    BOLSISTA("BOLSISTA");

    private String role;

    Roles(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}