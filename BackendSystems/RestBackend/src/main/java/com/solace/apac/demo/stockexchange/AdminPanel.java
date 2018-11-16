package com.solace.apac.demo.stockexchange;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpSession;

@Controller
public class AdminPanel {
    @RequestMapping("/adminPanel")
    public String controlPanel(HttpSession session, Model model) {
        model.addAttribute("Today", (new java.util.Date()));

        return "adminPanel";
    }
}
