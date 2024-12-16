package hakata.poker.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Comparator;
import java.util.Arrays;
import java.util.ArrayList;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hakata.poker.model.Room;
import hakata.poker.model.RoomMapper;
import hakata.poker.model.User;
import hakata.poker.model.UserMapper;
import hakata.poker.service.AsyncRoom;

@Controller
@RequestMapping("/room")
public class RoomController {

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private AsyncRoom acRoom;

  @GetMapping("/step1")
  public String room1(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("room1", true);
    model.addAttribute("login_user", loginUser);
    final ArrayList<Room> rooms1 = acRoom.syncShowRoomsList();
    // ArrayList<User> users1 = userMapper.selectAll();
    model.addAttribute("rooms", rooms1);
    // model.addAttribute("users", users1);
    return "room.html";
  }

  @GetMapping("/step2")
  @Transactional
  public String room2(@RequestParam Integer roomId, ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    User user1 = userMapper.selectAllByName(loginUser);
    Room enteredRoom = roomMapper.selectAllById(roomId);
    int userIndex = 0;
    if (enteredRoom.getUser1id() == 0) {
      userIndex = 1;
    } else if (enteredRoom.getUser2id() == 0) {
      userIndex = 2;
    }
    acRoom.syncEnterRoom(userIndex, user1, roomId);
    Room room2 = acRoom.syncShowRoomById(roomId);
    /*
     * ArrayList<User> users1 = userMapper.selectAll();
     * model.addAttribute("users", users1);
     */
    model.addAttribute("room2", true);

    model.addAttribute("room2", room2);
    return "room.html";
  }

  @GetMapping("/leaveRoom")
  @Transactional
  public String leaveRoom(@RequestParam Integer roomId, ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    // ログインユーザーが所属しているルームを取得
    acRoom.syncLeaveRoom(loginUser, roomId);

    // 再びroom1の処理を実行してルーム一覧を表示
    return room1(model, prin);
  }

  @GetMapping("/step3")
  public SseEmitter room3() {
    final SseEmitter sseEmitter = new SseEmitter(60 * 1000L);
    this.acRoom.asyncShowRoomsList(sseEmitter);
    return sseEmitter;
  }

  @GetMapping("/Asyncr2")
  public SseEmitter async_r2() {
    final SseEmitter sseEmitter = new SseEmitter(60 * 1000L);
    this.acRoom.asyncShowRoomsList(sseEmitter);
    return sseEmitter;
  }

}
