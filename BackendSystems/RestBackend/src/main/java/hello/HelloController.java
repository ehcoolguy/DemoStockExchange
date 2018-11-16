package hello;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by hhjau on 2017/05/11.
 */

@RestController
public class HelloController {
    @RequestMapping("/hello")
    public String helloWorld() {
        return "Greeting from Spring-Boot HelloWorld!!";
    }
}