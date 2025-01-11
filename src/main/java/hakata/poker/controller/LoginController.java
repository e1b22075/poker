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
import hakata.poker.model.HandMapper;
import hakata.poker.model.Room;
import hakata.poker.model.RoomMapper;
import hakata.poker.model.UserMapper;
import hakata.poker.model.match;
import hakata.poker.model.matchMapper;
import hakata.poker.service.Asynclogout;
import hakata.poker.service.UserRegistrationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @Autowired
  private UserRegistrationService userRegistrationService;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private matchMapper matchMapper;

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private HandMapper handMapper;

  @Autowired
  private Asynclogout logout;

  @GetMapping("/login")
  public String login_page() {
    return "login";
  }

  @GetMapping("/error")
  public String error_page() {
    return "error";
  }

  @GetMapping("/logout")
  public String logout_page(HttpServletRequest request, ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報

    match match;
    int userid;
    Room room;
    userid = userMapper.selectid(loginUser);
    if (matchMapper.selectAllByuser1Id(userid) != null) {
      match = matchMapper.selectAllById(userid);
      matchMapper.deleteById(match.getId());
      logout.synclogput(userid, match.getRid());
    }
    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);
    if (roomMapper.selectAllByuserId(userid) != null) {
      room = roomMapper.selectAllByuserId(userid);
      if (room.getUser1id() != 0) {
        if (room.getUser1id() == userid) {
          roomMapper.updateUser1ResetByRoomId(room.getId());
        }
      }
      if (room.getUser2id() != 0) {
        if (room.getUser2id() == userid) {
          roomMapper.updateUser2ResetByRoomId(room.getId());
        }
      }
    }

    request.getSession().invalidate();
    SecurityContextHolder.clearContext();
    return "login";
  }

  @GetMapping("/user")
  public String signup(HttpServletRequest request) {
    return "signup.html";
  }

  @GetMapping("/lost")
  public String lost(HttpServletRequest request) {
    return "lost.html";
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

  @GetMapping("/lost2/{rid}")
  public SseEmitter dropSse(@PathVariable int rid) {
    final SseEmitter emitter = new SseEmitter();
    logout.registerEmitter(rid, emitter);
    // 初回アクセスで監視タスクを起動
    logout.startRoomMonitor(rid);
    return emitter;
  }
}
