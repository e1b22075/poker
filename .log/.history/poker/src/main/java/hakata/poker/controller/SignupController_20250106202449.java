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
import hakata.poker.model.User;


@Controller
@RequestMapping("/signup")
public class SignupController {

  @Autowired
  private UserMapper uMapper;

  @GetMapping
  public String signup_page() {
    return "signup";
  }

  @PostMapping
  public String signupUser(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
    
    return "signup";
  }
}
