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
import org.springframework.web.bind.annotation.PathVariable;

import hakata.poker.model.Hand;
import hakata.poker.model.HandMapper;
import hakata.poker.model.Room;
import hakata.poker.model.RoomMapper;
import hakata.poker.model.User;
import hakata.poker.model.UserMapper;
import hakata.poker.service.AsyncRoom;
import hakata.poker.model.match;
import hakata.poker.model.matchMapper;
import hakata.poker.service.AsyncReady;

@Controller
@RequestMapping("/room")
public class RoomController {

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private matchMapper matchMapper;

  @Autowired
  private HandMapper handMapper;

  @Autowired
  private AsyncReady ready;

  @Autowired
  private AsyncRoom acRoom;

  @GetMapping("/step1")
  public String room1(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("room1", true);
    model.addAttribute("login_user", loginUser);
    int userid;
    match match;
    Room room;
    userid = userMapper.selectid(loginUser);
    if (matchMapper.selectAllByuser1Id(userid) != null) {
      match = matchMapper.selectAllById(userid);
      matchMapper.deleteById(match.getId());
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
    return "ready_room.html";
  }

  @GetMapping("/leaveRoom")
  @Transactional
  public String leaveRoom(@RequestParam Integer roomId, ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    int userid;
    match match;
    userid = userMapper.selectid(loginUser);
    if (matchMapper.selectAllById(userid) != null) {
      match = matchMapper.selectAllById(userid);
      matchMapper.deleteById(match.getId());
    }

    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);
    // ログインユーザーが所属しているルームを取得
    acRoom.syncLeaveRoom(loginUser, roomId);

    // 再びroom1の処理を実行してルーム一覧を表示
    return room1(model, prin);
  }

  @GetMapping("/changeStatus")
  @Transactional
  public String changeStatus(@RequestParam Integer roomId, ModelMap model, Principal prin) {
    int userid;
    match match = new match();
    String loginUser = prin.getName(); // ログインユーザ情報
    acRoom.syncChangeStatusByuName_and_rId(loginUser, roomId);
    model.addAttribute("login_user", loginUser);
    // 再びroom1の処理を実行してルーム一覧を表示
    User user1 = userMapper.selectAllByName(loginUser);
    Room enteredRoom = roomMapper.selectAllById(roomId);
    int userIndex = 0;
    if (enteredRoom.getUser1id() == user1.getId()) {
      userIndex = 1;
    } else if (enteredRoom.getUser2id() == user1.getId()) {
      userIndex = 2;
    }
    Room room2 = acRoom.syncShowRoomById(roomId);
    /*
     * ArrayList<User> users1 = userMapper.selectAll();
     * model.addAttribute("users", users1);
     */
    model.addAttribute("room2", true);

    model.addAttribute("room2", room2);
    if (userIndex == 1 && !enteredRoom.getUser1Status()) {
      return room2(roomId, model, prin);
    } else if (userIndex == 2 && !enteredRoom.getUser2Status()) {
      return room2(roomId, model, prin);
    }
    if (enteredRoom.getUser1Status() && enteredRoom.getUser2Status()) {
      if (matchMapper.selectAllById(room2.getUser1id()) != null || matchMapper
          .selectAllById(room2.getUser2id()) != null) {
        match = matchMapper.selectAllById(room2.getUser1id());
        model.addAttribute("round", match.getRound() / 2 + 1);
        model.addAttribute("coin", match.getUser1coin());
        model.addAttribute("bet", match.getBet());
        return "poker";
      }
      userid = userMapper.selectid(room2.getUser1Name());
      match.setUser1id(userid);
      userid = userMapper.selectid(room2.getUser2Name());
      match.setUser2id(userid);
      match.setUser1coin(5);
      match.setUser2coin(5);
      match.setBet(1);
      match.setRound(1);
      match.setRid(roomId);
      this.ready.syncNewMatch(match);
      model.addAttribute("round", match.getRound() / 2 + 1);
      model.addAttribute("coin", match.getUser1coin());
      model.addAttribute("bet", match.getBet());
      return "poker.html";
    }
    model.addAttribute("rid", room2.getId());
    return "ready2.html";
  }

  @GetMapping("/serch/{rid}")
  @Transactional
  public String serch(@PathVariable Integer rid, ModelMap model, Principal prin) {
    int userid;
    match match = new match();
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    userid = userMapper.selectid(loginUser);
    // 再びroom1の処理を実行してルーム一覧を表示
    User user1 = userMapper.selectAllByName(loginUser);
    Room enteredRoom = roomMapper.selectAllById(rid);
    int userIndex = 0;
    if (enteredRoom.getUser1id() == user1.getId()) {
      userIndex = 1;
    } else if (enteredRoom.getUser2id() == user1.getId()) {
      userIndex = 2;
    }
    Room room2 = acRoom.syncShowRoomById(rid);
    /*
     * ArrayList<User> users1 = userMapper.selectAll();
     * model.addAttribute("users", users1);
     */
    model.addAttribute("room2", true);
    model.addAttribute("room2", room2);
    model.addAttribute("rid", room2.getId());
    if (matchMapper.selectAllById(userid) == null) {
      return "ready2";
    }
    match = matchMapper.selectAllById(userid);
    model.addAttribute("round", match.getRound() / 2 + 1);
    model.addAttribute("coin", match.getUser1coin());
    model.addAttribute("bet", match.getBet());
    return "poker";
  }

  @GetMapping("/step3")
  public SseEmitter room3() {
    final SseEmitter sseEmitter = new SseEmitter(60 * 1000L);
    this.acRoom.asyncShowRoomsList(sseEmitter);
    return sseEmitter;
  }

  @GetMapping("/update")
  public SseEmitter asyncr2(@RequestParam Integer roomId) {
    final SseEmitter sseEmitter = new SseEmitter(60 * 1000L);
    this.acRoom.asyncShowRoomInfo(roomId, sseEmitter);
    return sseEmitter;
  }

  @GetMapping("/start/{rid}")
  public SseEmitter sample(@PathVariable int rid) {
    final SseEmitter emitter = new SseEmitter();
    ready.registerEmitter(rid, emitter);
    ready.AsyncReadySend(rid);
    return emitter;
  }
}
