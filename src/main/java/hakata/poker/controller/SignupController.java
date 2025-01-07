package hakata.poker.controller;

import java.util.ArrayList;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.beans.factory.annotation.Autowired;
import hakata.poker.model.UserMapper;
import hakata.poker.service.UserService;
import hakata.poker.service.UserRegistrationService;
import org.springframework.ui.ModelMap;

import hakata.poker.model.User;


@Controller
@RequestMapping("/signup")
public class SignupController {

  @Autowired
  private UserRegistrationService uRService;

  @GetMapping
  public String signup_page() {
    return "signup";
  }

  @PostMapping
  public String signupUser(@RequestParam String username, @RequestParam String password, @RequestParam String pass_check,ModelMap model) {
    try {
      // UserServiceを呼び出して登録処理を実行
      uRService.registerUser(username,password,pass_check);
      return "redirect:/login"; // ログインページへリダイレクト
    } catch (Exception e) {
      // エラーがあればsignupページにエラーメッセージを表示
      model.addAttribute("error", "ユーザー登録に失敗しました: " + e.getMessage());
      return "signup"; // signup.htmlを再表示
    }
  }
}
