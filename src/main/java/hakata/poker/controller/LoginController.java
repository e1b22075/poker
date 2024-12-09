package hakata.poker.controller;

import org.springframework.stereotype.Controller;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping("/login")
  public String login_page() {
    return "login";
  }

  @GetMapping("/error")
  public String error_page() {
    return "error";
  }

}
