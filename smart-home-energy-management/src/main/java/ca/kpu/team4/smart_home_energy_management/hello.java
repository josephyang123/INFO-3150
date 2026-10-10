package ca.kpu.team4.smart_home_energy_management;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
public class hello {
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }
    
}
