package io.github.saveriobutright.jobradar.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class DashboardController {

    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }
}
