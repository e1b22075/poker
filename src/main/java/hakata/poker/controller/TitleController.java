package hakata.poker.controller;

import java.security.Principal;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

public class TitleController {
  @GetMapping("/title")
  public String help_page(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    return "title.html";
  }
}
