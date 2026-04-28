package dimap.ufrn.spe.api.v1;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import dimap.ufrn.spe.api.v1.models.Roles;
import dimap.ufrn.spe.api.v1.models.User;
import dimap.ufrn.spe.api.v1.repositories.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SpeApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(SpeApplication.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        // Verificar se já existe um usuário admin
        if (userRepository.findByUsername("redes") == null) {
            // Criptografar a senha
            var encryptedPassword = "$2a$10$tEV/FwT1W1ioN2qcLQYXvu.xCws/sip3hwgJ7nE0xMqC/z.yFwASC";

            // Criar o usuário administrador com o papel ADMIN
            User adminUser = new User();
            adminUser.setName("Redes");
            adminUser.setUsername("redes");
            adminUser.setMatricula("00000000");
            adminUser.setPassword(encryptedPassword);
            adminUser.setEmail("redes@dimap.ufrn.br");
            adminUser.setCargo("Tecnico de TI");
            adminUser.setRoles(Roles.ADMIN); 

            // Salvar no repositório
            userRepository.save(adminUser);

            //System.out.println("Usuário administrador criado automaticamente.");
        }
    }
}
