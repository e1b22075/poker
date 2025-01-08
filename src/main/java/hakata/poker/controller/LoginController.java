package hakata.poker.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.stream.Collectors;
import hakata.poker.dto.SignupRequestDto;
import hakata.poker.service.UserRegistrationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @Autowired
  private UserRegistrationService userRegistrationService;

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

  @GetMapping("/user")
  public String signup(HttpServletRequest request) {
    return "signup.html";
  }

  // ユーザー登録のエンドポイント
  @PostMapping("/signup")
  public String signup(@ModelAttribute SignupRequestDto signupRequestDto,
      BindingResult bindingResult, ModelMap model) {
    String errorMessage = signupRequestDto.validate();
    // バリデーションエラーのチェック
    if (!errorMessage.isEmpty()) {
      model.addAttribute("error", errorMessage);
      return "signup";
    }
    try {
      // ユーザー登録処理
      userRegistrationService.registerUser(signupRequestDto);
      return "login";
    } catch (Exception ex) {
      // 登録エラーのハンドリング
      model.addAttribute("error", "エラーが発生しました。もう一度お願いします。");
      return "error";
    }
  }
}
