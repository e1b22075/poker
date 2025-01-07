package hakata.poker.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;


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

  @GetMapping("/logout")
  public String logout_page(HttpServletRequest request) {
    request.getSession().invalidate();
    SecurityContextHolder.clearContext();
    return "login";
  }

}
