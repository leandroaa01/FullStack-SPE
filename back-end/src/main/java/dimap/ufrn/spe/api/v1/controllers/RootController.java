package dimap.ufrn.spe.api.v1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class RootController {

   @RequestMapping
public String index() {
    return "redirect:/swagger-ui/index.html";
}

}
