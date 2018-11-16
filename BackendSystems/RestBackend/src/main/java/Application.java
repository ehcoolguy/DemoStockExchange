
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by hhjau on 2017/05/11.
 */
@SpringBootApplication
@Controller
public class Application {
/*
    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("Today", (new java.util.Date()));

        return "index";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    */
}
