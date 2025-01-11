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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.PathVariable;

import hakata.poker.model.Room;
import hakata.poker.model.RoomMapper;
import hakata.poker.model.PlayerIndex;
import hakata.poker.service.AsyncCount;
import hakata.poker.model.CPUIndex;
import hakata.poker.model.Cards;
import hakata.poker.model.CardsMapper;
import hakata.poker.model.Hand;
import hakata.poker.model.User;
import hakata.poker.model.UserMapper;
import hakata.poker.model.HandMapper;
import hakata.poker.service.AsyncRoom;
import hakata.poker.service.AsyncUser;
import hakata.poker.model.match;
import hakata.poker.model.matchMapper;
import hakata.poker.model.Entry;
import hakata.poker.service.AsyncReady;
import hakata.poker.service.AsyncDrop;
import hakata.poker.service.Asyncresult;
import hakata.poker.model.index;

@Controller
public class PokerController {

  @Autowired
  private CardsMapper cardsMapper;

  @Autowired
  private HandMapper handMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private matchMapper matchMapper;

  @Autowired
  private AsyncUser acUser;

  @Autowired
  private Entry entry;

  @Autowired
  private AsyncReady ready;

  @Autowired
  private AsyncDrop drop;

  @Autowired
  private Asyncresult result;

  @GetMapping("ready")
  public String ready(ModelMap model, Principal prin) {
    int userid;
    ArrayList<String> users;
    match match = new match();
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    entry.addUser(loginUser);
    System.out.println(entry.getUsers());
    if (entry.getCount() == 2) {
      users = entry.getUsers();
      userid = userMapper.selectid(users.get(0));
      match.setUser1id(userid);
      userid = userMapper.selectid(users.get(1));
      match.setUser2id(userid);
      match.setUser1coin(5);
      match.setUser2coin(5);
      match.setBet(1);
      this.ready.syncNewMatch(match);

    }
    return "ready.html";
  }

  @GetMapping("poker")
  public String login(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    int userid;
    match match;

    userid = userMapper.selectid(loginUser);
    match = matchMapper.selectAllById(userid);
    if (userid == match.getUser1id()) {
      model.addAttribute("round", match.getRound() / 2 + 1);
      model.addAttribute("coin", match.getUser1coin());
      model.addAttribute("bet", match.getBet());
    } else if (userid == match.getUser2id()) {
      model.addAttribute("round", match.getRound() / 2 + 1);
      model.addAttribute("coin", match.getUser2coin());
      model.addAttribute("bet", match.getBet());
    }
    model.addAttribute("rid", match.getRid());

    model.addAttribute("login_user", loginUser);
    return "poker.html";
  }

  @GetMapping("poker/card")
  public String showCard(ModelMap model, Principal prin) {
    int userid;
    int turn = 1;
    match match;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで
    userid = userMapper.selectid(loginUser);
    match = matchMapper.selectAllById(userid);
    Hand hand = new Hand();
    hand.setActive(true);
    match = matchMapper.selectAllById(userid);
    ArrayList<Cards> myCards = cardsMapper.select5RandomCardByrid(match.getRid());
    for (Cards card : myCards) {
      cardsMapper.updateisActiveTrueByIdAndrid(card.getId(), match.getRid());
    }
    myCards.sort(Comparator.comparingInt((Cards card) -> {
      int num = card.getNum();
      return num == 1 ? Integer.MAX_VALUE : num; // 1を最大値として扱う
    }));
    hand.setHand1id(myCards.get(0).getId());
    hand.setHand2id(myCards.get(1).getId());
    hand.setHand3id(myCards.get(2).getId());
    hand.setHand4id(myCards.get(3).getId());
    hand.setHand5id(myCards.get(4).getId());
    hand.setTurn(turn);
    userid = userMapper.selectid(loginUser);
    hand.setUserid(userid);

    myCards.sort(Comparator.comparingInt((Cards card) -> {
      int num = card.getNum();
      return num == 1 ? Integer.MAX_VALUE : num; // 1を最大値として扱う
    }));

    handMapper.insertHandandIsActive2(hand);

    model.addAttribute("myCards", myCards);

    model.addAttribute("index", new PlayerIndex());

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      model.addAttribute("coin", match.getUser1coin());
    } else if (match.getUser2id() == userid) {
      model.addAttribute("coin", match.getUser2coin());
    }

    model.addAttribute("rays", match.getBet());

    model.addAttribute("myCards", myCards);
    model.addAttribute("turn", hand.getTurn());
    model.addAttribute("index", new index());
    model.addAttribute("round", match.getRound() / 2 + 1);
    model.addAttribute("rid", match.getRid());
    return "select.html";
  }

  @PostMapping("/result")
  public String formResult(@ModelAttribute PlayerIndex index, ModelMap model, Principal prin) {
    int userid;
    Hand userhand;
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    Cards userdrawCards;
    match match;

    int myflashflag = 0; // プレイヤーのフラッシュのフラグ
    int mystraightflag = 0; // プレイヤーのストレートのフラグ

    int myonepairkickernum = 0;
    int myonepairkickerid = 0;
    int mytwopairid = 0;

    String myrole;

    String result;

    for (Integer indes : index.getId()) {
      System.out.println(indes + "選択されました: ");
    }
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);

    userid = userMapper.selectid(loginUser);
    userhand = handMapper.selectByUserId(userid);
    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);
    match = matchMapper.selectAllById(userid);
    myCards.add(cardsMapper.selectAllById(userhand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand5id()));

    for (Integer indes : index.getId()) {
      userdrawCards = cardsMapper.selectRandomCardByrid(match.getRid());
      while (userdrawCards.getActive()) {
        userdrawCards = cardsMapper.selectRandomCardByrid(match.getRid());
      }
      myCards.set(indes - 1, userdrawCards);
      cardsMapper.updateisActiveTrueByIdAndrid(myCards.get(indes - 1).getId(), match.getRid());
    }

    myCards.sort(Comparator.comparingInt((Cards card) -> {
      int num = card.getNum();
      return num == 1 ? Integer.MAX_VALUE : num; // 1を最大値として扱う
    }));

    userhand.setHand1id(myCards.get(0).getId());
    userhand.setHand2id(myCards.get(1).getId());
    userhand.setHand3id(myCards.get(2).getId());
    userhand.setHand4id(myCards.get(3).getId());
    userhand.setHand5id(myCards.get(4).getId());
    userhand.setTurn(userhand.getTurn() + 1);

    model.addAttribute("myCards", myCards);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      model.addAttribute("coin", match.getUser1coin());
    } else if (match.getUser2id() == userid) {
      model.addAttribute("coin", match.getUser2coin());
    }

    model.addAttribute("rays", match.getBet());

    model.addAttribute("myCards", myCards);

    model.addAttribute("myCards", myCards);

    model.addAttribute("index", new index());

    // ユーザのroleidを初期値10に設定
    userhand.setRoleid(10);

    // ストレートの判定
    if ((myCards.get(4).getNum() == myCards.get(3).getNum() + 1
        && myCards.get(3).getNum() == myCards.get(2).getNum() + 1
        && myCards.get(2).getNum() == myCards.get(1).getNum() + 1
        && myCards.get(1).getNum() == myCards.get(0).getNum() + 1)
        || (myCards.get(4).getNum() == 1 && myCards.get(3).getNum() == 13 && myCards.get(2).getNum() == 12
            && myCards.get(1).getNum() == 11 && myCards.get(0).getNum() == 10)) {
      userhand.setRoleid(6);
      mystraightflag = 1;
    }
    // フラッシュの判定
    if (myCards.get(0).getCardtype() == myCards.get(1).getCardtype()
        && myCards.get(1).getCardtype() == myCards.get(2).getCardtype()
        && myCards.get(2).getCardtype() == myCards.get(3).getCardtype()
        && myCards.get(3).getCardtype() == myCards.get(4).getCardtype()) {
      userhand.setRoleid(5);
      myflashflag = 1;
    }
    // ロイヤルストレートフラッシュの判定
    if (myflashflag == 1 && myCards.get(4).getNum() == 1 && myCards.get(0).getNum() == 10
        && myCards.get(1).getNum() == 11 && myCards.get(2).getNum() == 12 && myCards.get(3).getNum() == 13) {
      userhand.setRoleid(1);
    }
    // ストレートフラッシュの判定
    else if (mystraightflag == 1 && myflashflag == 1) {
      userhand.setRoleid(2);
    }

    // フォーカードの判定
    if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum()
        && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum()
            && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(3);
    }
    // スリーカードの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(2).getNum() == myCards.get(3).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(7);

      // フルハウスの判定
      if (myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum()) {
        if (myCards.get(3).getNum() == myCards.get(4).getNum()) {
          userhand.setRoleid(4);
        }
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum()
          && myCards.get(3).getNum() == myCards.get(4).getNum()) {
        if (myCards.get(0).getNum() == myCards.get(1).getNum()) {
          userhand.setRoleid(4);
        }
      }
    }
    // ツウ・ペアの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())
        || (myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(8);

      // ペアになっていないカードの手札idを保存(カードの数値ではなく、手札の左から何番目にあるかの数字)
      if (myCards.get(0).getNum() == myCards.get(1).getNum()
          && myCards.get(2).getNum() == myCards.get(3).getNum()) {
        userhand.setRolenum(myCards.get(4).getNum());
        userhand.setTwopairid(4);
      } else if (myCards.get(1).getNum() == myCards.get(2).getNum()
          && myCards.get(3).getNum() == myCards.get(4).getNum()) {
        userhand.setRolenum(myCards.get(0).getNum());
        userhand.setTwopairid(0);
      } else if (myCards.get(0).getNum() == myCards.get(1).getNum()
          && myCards.get(3).getNum() == myCards.get(4).getNum()) {
        userhand.setRolenum(myCards.get(2).getNum());
        userhand.setTwopairid(2);
      }
    }
    // ワン・ペアの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum()) || (myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(9);

      // ワンペア時の数値を格納
      if (myCards.get(0).getNum() == myCards.get(1).getNum()) {
        userhand.setRolenum(myCards.get(1).getNum());
        userhand.setOnepairkickernum(myCards.get(4).getNum());
        userhand.setOnepairkickerid(4);
      } else if (myCards.get(1).getNum() == myCards.get(2).getNum()) {
        userhand.setRolenum(myCards.get(2).getNum());
        userhand.setOnepairkickernum(myCards.get(4).getNum());
        userhand.setOnepairkickerid(4);
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum()) {
        userhand.setRolenum(myCards.get(3).getNum());
        userhand.setOnepairkickernum(myCards.get(4).getNum());
        userhand.setOnepairkickerid(4);
      } else if (myCards.get(3).getNum() == myCards.get(4).getNum()) {
        userhand.setRolenum(myCards.get(4).getNum());
        userhand.setOnepairkickernum(myCards.get(2).getNum());
        userhand.setOnepairkickerid(2);
      }
    }

    if (userhand.getRoleid() == 1) {
      myrole = "あなたの役はロイヤルストレートフラッシュです。";
      model.addAttribute("myrole", myrole);
    } else if (userhand.getRoleid() == 2) {
      myrole = "あなたの役はストレートフラッシュです。";
      model.addAttribute("myrole", myrole);
    } else if (userhand.getRoleid() == 3) {
      myrole = "あなたの役はフォア・カードです。";
      model.addAttribute("myrole", myrole);
    } else if (userhand.getRoleid() == 4) {
      myrole = "あなたの役はフルハウスです。";
      model.addAttribute("myrole", myrole);
    } else if (userhand.getRoleid() == 5) {
      myrole = "あなたの役はフラッシュです。";
      model.addAttribute("myrole", myrole);
    } else if (userhand.getRoleid() == 6) {
      myrole = "あなたの役はストレートです。";
      model.addAttribute("myrole", myrole);
    } else if (userhand.getRoleid() == 7) {
      myrole = "あなたの役はスリーカードです。";
      model.addAttribute("myrole", myrole);
    } else if (userhand.getRoleid() == 8) {
      myrole = "あなたの役はツウ・ペアです。";
      model.addAttribute("myrole", myrole);
    } else if (userhand.getRoleid() == 9) {
      myrole = "あなたの役はワン・ペアです。";
      model.addAttribute("myrole", myrole);
    }

    handMapper.insertHandandIsActive2(userhand);

    if (userhand.getTurn() >= 3) {
      if (match.getUser1id() == userid) {
        this.result.syncUser1(match.getId(), userhand.getId());
        System.out.println("ユーザー1の書き込み");
      } else if (match.getUser2id() == userid) {
        this.result.syncUser2(match.getId(), userhand.getId());
        System.out.println("ユーザー2の書き込み");
      }
      model.addAttribute("round", match.getRound() / 2 + 1);
      model.addAttribute("rid", match.getRid());
      return "wait";
    } else {
      model.addAttribute("round", match.getRound() / 2 + 1);
      model.addAttribute("turn", userhand.getTurn());
      model.addAttribute("rid", match.getRid());
      return "select";
    }
  }

  @GetMapping("poker/call")
  public String showCall(ModelMap model, Principal prin) {
    int userid;
    match match;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    userid = userMapper.selectid(loginUser);
    Hand hand = handMapper.selectByUserId(userid);

    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);

    myCards.add(cardsMapper.selectAllById(hand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand5id()));

    model.addAttribute("myCards", myCards);

    handMapper.insertHandandIsActive2(hand);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      model.addAttribute("coin", match.getUser1coin());
    } else if (match.getUser2id() == userid) {
      model.addAttribute("coin", match.getUser2coin());
    }

    model.addAttribute("rays", match.getBet());

    model.addAttribute("turn", hand.getTurn());

    model.addAttribute("myCards", myCards);

    model.addAttribute("index", new index());
    model.addAttribute("round", match.getRound() / 2 + 1);
    model.addAttribute("rid", match.getRid());
    return "poker.html";
  }

  @GetMapping("poker/drop")
  public String showDrop(ModelMap model, Principal prin) {
    int userid;
    int coin;
    int cpuid;
    match match;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで
    String message = "ドロップしました";

    userid = userMapper.selectid(loginUser);
    Hand userhand = handMapper.selectByUserId(userid);
    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);
    userhand.setTurn(1);

    handMapper.insertHandandIsActive2(userhand);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      match.setUser1coin(match.getUser1coin() - 1);
      model.addAttribute("coin", match.getUser1coin());
      matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin());
      drop.syncDrop1(match.getId(), match.getRid());
    } else if (match.getUser2id() == userid) {
      match.setUser2coin(match.getUser2coin() - 1);
      model.addAttribute("coin", match.getUser2coin());
      matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin());
      drop.syncDrop2(match.getId(), match.getRid());
    }
    match.setBet(1);
    matchMapper.updateBetById(match.getId(), 1);
    model.addAttribute("rays", match.getBet());

    model.addAttribute("turn", userhand.getTurn());

    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);

    cardsMapper.updateAllfalsetotrueByfalseAndrid(match.getRid());

    model.addAttribute("message", message);

    model.addAttribute("index", new index());

    return "drop.html";
  }

  @GetMapping("poker/drop2")
  public String showDrop2(ModelMap model, Principal prin) {
    int userid;
    int coin;
    int cpuid;
    match match;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで
    String message = "相手がドロップしました";

    userid = userMapper.selectid(loginUser);
    Hand userhand = handMapper.selectByUserId(userid);
    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);
    userhand.setTurn(1);

    handMapper.insertHandandIsActive2(userhand);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      match.setUser1coin(match.getUser1coin() + 1);
      matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin());

    } else if (match.getUser2id() == userid) {
      match.setUser2coin(match.getUser2coin() + 1);
      matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin());

    }
    match.setBet(1);
    matchMapper.updateBetById(match.getId(), 1);
    model.addAttribute("rays", match.getBet());

    model.addAttribute("turn", userhand.getTurn());

    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);

    cardsMapper.updateAllfalsetotrueByfalseAndrid(match.getRid());

    model.addAttribute("message", message);

    model.addAttribute("index", new index());

    return "drop.html";
  }

  @GetMapping("drop_reset")
  public String reset2(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    match match;
    int userid;
    userid = userMapper.selectid(loginUser);
    match = matchMapper.selectAllById(userid);
    model.addAttribute("rid", match.getRid());
    if (match.getUser1coin() <= 0 || match.getUser2coin() <= 0 || match.getRound() >= 5) {
      if (match.getUser1id() == userid && match.getUser1coin() <= 0) {
        return "lose";
      } else if (match.getUser2id() == userid && match.getUser2coin() <= 0) {
        return "lose";
      } else if (match.getUser1id() == userid && match.getUser1coin() < match.getUser2coin()) {
        return "lose";
      } else if (match.getUser2id() == userid && match.getUser2coin() < match.getUser1coin()) {
        return "lose";
      } else {
        return "win";
      }
    }

    matchMapper.updateUser1HandById(match.getId(), 0);
    matchMapper.updateUser2HandById(match.getId(), 0);
    match.setRound(match.getRound() + 1);
    matchMapper.updateRoundById(match.getId(), match.getRound());
    model.addAttribute("round", match.getRound() / 2 + 1);
    model.addAttribute("rid", match.getRid());
    return "poker.html";
  }

  @GetMapping("poker/reset")
  public String reset(ModelMap model, Principal prin) {
    int userid;
    int coin;
    match match;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで

    userid = userMapper.selectid(loginUser);

    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);

    cardsMapper.updateAllfalsetotrueByfalse();

    model.addAttribute("index", new index());
    match = matchMapper.selectAllById(userid);
    model.addAttribute("rid", match.getRid());
    if (match.getUser1coin() <= 0 || match.getUser2coin() <= 0 || match.getRound() >= 5) {
      if (match.getUser1id() == userid && match.getUser1coin() <= 0) {
        return "lose";
      } else if (match.getUser2id() == userid && match.getUser2coin() <= 0) {
        return "lose";
      } else if (match.getUser1id() == userid && match.getUser1coin() < match.getUser2coin()) {
        return "lose";
      } else if (match.getUser2id() == userid && match.getUser2coin() < match.getUser1coin()) {
        return "lose";
      } else {
        return "win";
      }
    }

    matchMapper.updateUser1HandById(match.getId(), 0);
    matchMapper.updateUser2HandById(match.getId(), 0);
    match.setRound(match.getRound() + 1);
    matchMapper.updateRoundById(match.getId(), match.getRound());
    model.addAttribute("round", match.getRound() / 2 + 1);
    model.addAttribute("rid", match.getRid());
    return "poker.html";
  }

  @GetMapping("poker/rays")
  public String rays(ModelMap model, Principal prin) {
    int userid;
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    match match;
    userid = userMapper.selectid(loginUser);
    Hand hand = handMapper.selectByUserId(userid);
    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      model.addAttribute("coin", match.getUser1coin());
    } else if (match.getUser2id() == userid) {
      model.addAttribute("coin", match.getUser2coin());
    }
    model.addAttribute("round", match.getRound() / 2 + 1);
    return "rays.html";
  }

  @PostMapping("/rays")
  public String formRays(@RequestParam("rays") Integer rays, ModelMap model, Principal prin) {
    int userid;
    int cpuid;
    Hand userhand;
    Hand cpuhand;
    match match;
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);

    userid = userMapper.selectid(loginUser);
    userhand = handMapper.selectByUserId(userid);

    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);
    myCards.add(cardsMapper.selectAllById(userhand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand5id()));

    model.addAttribute("myCards", myCards);

    handMapper.insertHandandIsActive2(userhand);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      model.addAttribute("coin", match.getUser1coin());
    } else if (match.getUser2id() == userid) {
      model.addAttribute("coin", match.getUser2coin());
    }

    match.setBet(match.getBet() + rays);
    matchMapper.updateBetById(match.getId(), match.getBet());
    model.addAttribute("rays", match.getBet());

    model.addAttribute("turn", userhand.getTurn());

    model.addAttribute("index", new index());
    model.addAttribute("round", match.getRound() / 2 + 1);
    model.addAttribute("rid", match.getRid());
    return "poker";
  }

  // カードタイプを区別する関数
  public int determinType(ArrayList<Cards> cards, int a) {
    int cardtype = 0;
    if (cards.get(a).getCardtype().equals("spade")) {
      cardtype = 1;
    } else if (cards.get(a).getCardtype().equals("heart")) {
      cardtype = 2;
    } else if (cards.get(a).getCardtype().equals("dia")) {
      cardtype = 3;
    } else if (cards.get(a).getCardtype().equals("clover")) {
      cardtype = 4;
    }
    return cardtype;
  }

  @GetMapping("poker/result")
  public String result(ModelMap model, Principal prin) {
    int userid;
    int userid2;
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    match match;
    String message = "";

    ArrayList<Cards> myCards = new ArrayList<Cards>();
    ArrayList<Cards> RivalCards = new ArrayList<Cards>();

    userid = userMapper.selectid(loginUser);
    Hand userhand = handMapper.selectByUserId(userid);

    myCards.add(cardsMapper.selectAllById(userhand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand5id()));

    Hand hand;
    match = matchMapper.selectAllById(userid);
    model.addAttribute("round", match.getRound() / 2 + 1);

    if (match.getUser1hand() != 0 && match.getUser2hand() != 0) {
      if (match.getUser1id() == userid) {
        userid2 = match.getUser2id();
        hand = handMapper.selectByUserId(userid2);

        RivalCards.add(cardsMapper.selectAllById(hand.getHand1id()));
        RivalCards.add(cardsMapper.selectAllById(hand.getHand2id()));
        RivalCards.add(cardsMapper.selectAllById(hand.getHand3id()));
        RivalCards.add(cardsMapper.selectAllById(hand.getHand4id()));
        RivalCards.add(cardsMapper.selectAllById(hand.getHand5id()));

        int a1 = myCards.get(4).getNum();
        int a2 = RivalCards.get(4).getNum();
        // 5枚目のカードが1の時、数値比較の都合上14にする
        if (myCards.get(4).getNum() == 1) {
          a1 = 14;
        }
        if (RivalCards.get(4).getNum() == 1) {
          a2 = 14;
        }

        // Roleidの大小関係で勝利者を判定
        if (userhand.getRoleid() < hand.getRoleid()) {
          message = "あなたの勝利です!";
          matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
          matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
        } else if (userhand.getRoleid() > hand.getRoleid()) {
          message = "あなたの負けです...";
          matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
          matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
        }
        // ロイヤルストレートフラッシュ同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 1) {
          if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          }
        }
        // フォア・カード同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 3) {
          if (myCards.get(3).getNum() > RivalCards.get(3).getNum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (myCards.get(3).getNum() < RivalCards.get(3).getNum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          }
        }
        // フルハウス・スリーカード同士の比較
        else if ((userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 4)
            || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 7)) {
          if (myCards.get(2).getNum() > RivalCards.get(2).getNum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (myCards.get(2).getNum() < RivalCards.get(2).getNum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          }
        }
        // ストレートフラッシュ・フラッシュ・ストレート同士の比較
        else if ((userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 2)
            || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 5)
            || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 6)) {
          if (a1 > a2) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (a1 < a2) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (a1 == a2) {
            if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
              message = "あなたの勝利です!";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
            } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
              message = "あなたの負けです...";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
            }
          }
        }
        // ツウ・ペア同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 8) {
          if (myCards.get(3).getNum() > RivalCards.get(3).getNum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (myCards.get(3).getNum() < RivalCards.get(3).getNum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (myCards.get(3).getNum() == RivalCards.get(3).getNum()) {
            if (userhand.getRolenum() > hand.getRolenum()) {
              message = "あなたの勝利です!";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
            } else if (userhand.getRolenum() < hand.getRolenum()) {
              message = "あなたの負けです...";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
            } else if (userhand.getRolenum() == hand.getRolenum()) {
              if (determinType(myCards, userhand.getTwopairid()) < determinType(RivalCards, hand.getTwopairid())) {
                message = "あなたの勝利です!";
                matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
                matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
              } else if (determinType(myCards, userhand.getTwopairid()) > determinType(RivalCards,
                  hand.getTwopairid())) {
                message = "あなたの負けです...";
                matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
                matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
              }
            }
          }
        }
        // ワン・ペア同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 9) {
          if (userhand.getRolenum() > hand.getRolenum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (userhand.getRolenum() < hand.getRolenum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (userhand.getRolenum() == hand.getRolenum()) {
            if (userhand.getOnepairkickernum() > hand.getOnepairkickernum()) {
              message = "あなたの勝利です!";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
            } else if (userhand.getOnepairkickernum() < hand.getOnepairkickernum()) {
              message = "あなたの負けです...";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
            } else if (userhand.getOnepairkickernum() == hand.getOnepairkickernum()) {
              if (determinType(myCards,
                  userhand.getOnepairkickernum()) < determinType(RivalCards, hand.getOnepairkickerid())) {
                message = "あなたの勝利です!";
                matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
                matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
              } else if (determinType(myCards, userhand.getOnepairkickerid()) > determinType(RivalCards,
                  hand.getOnepairkickerid())) {
                message = "あなたの負けです...";
                matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
                matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
              }
            }
          }
        }
        // ハイカード同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 10) {
          if (myCards.get(4).getNum() > RivalCards.get(4).getNum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (myCards.get(4).getNum() < RivalCards.get(4).getNum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (myCards.get(4).getNum() == RivalCards.get(4).getNum()) {
            if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
              message = "あなたの勝利です!";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
            } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
              message = "あなたの負けです...";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
            }
          }
        }

        drop.syncDrop1(match.getId(), match.getRid());
        match = matchMapper.selectAllById(userid);
        model.addAttribute("message", message);
        model.addAttribute("coin", match.getUser1coin());

      } else if (match.getUser2id() == userid) {
        userid2 = match.getUser1id();
        hand = handMapper.selectByUserId(userid2);

        RivalCards.add(cardsMapper.selectAllById(hand.getHand1id()));
        RivalCards.add(cardsMapper.selectAllById(hand.getHand2id()));
        RivalCards.add(cardsMapper.selectAllById(hand.getHand3id()));
        RivalCards.add(cardsMapper.selectAllById(hand.getHand4id()));
        RivalCards.add(cardsMapper.selectAllById(hand.getHand5id()));

        int a1 = myCards.get(4).getNum();
        int a2 = RivalCards.get(4).getNum();
        // 5枚目のカードが1の時、数値比較の都合上14にする
        if (myCards.get(4).getNum() == 1) {
          a1 = 14;
        }
        if (RivalCards.get(4).getNum() == 1) {
          a2 = 14;
        }

        // Roleidの大小関係で勝利者を判定
        if (userhand.getRoleid() < hand.getRoleid()) {
          message = "あなたの勝利です!";
          matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
          matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
        } else if (userhand.getRoleid() > hand.getRoleid()) {
          message = "あなたの負けです...";
          matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
          matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
        }
        // ロイヤルストレートフラッシュ同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 1) {
          if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          }
        }
        // フォア・カード同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 3) {
          if (myCards.get(3).getNum() > RivalCards.get(3).getNum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (myCards.get(3).getNum() < RivalCards.get(3).getNum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          }
        }
        // フルハウス・スリーカード同士の比較
        else if ((userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 4)
            || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 7)) {
          if (myCards.get(2).getNum() > RivalCards.get(2).getNum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (myCards.get(2).getNum() < RivalCards.get(2).getNum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          }
        }
        // ストレートフラッシュ・フラッシュ・ストレート同士の比較
        else if ((userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 2)
            || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 5)
            || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 6)) {
          if (a1 > a2) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (a1 < a2) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (a1 == a2) {
            if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
              message = "あなたの勝利です!";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
            } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
              message = "あなたの負けです...";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
            }
          }
        }
        // ツウ・ペア同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 8) {
          if (myCards.get(3).getNum() > RivalCards.get(3).getNum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (myCards.get(3).getNum() < RivalCards.get(3).getNum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (myCards.get(3).getNum() == RivalCards.get(3).getNum()) {
            if (userhand.getRolenum() > hand.getRolenum()) {
              message = "あなたの勝利です!";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
            } else if (userhand.getRolenum() < hand.getRolenum()) {
              message = "あなたの負けです...";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
            } else if (userhand.getRolenum() == hand.getRolenum()) {
              if (determinType(myCards, userhand.getTwopairid()) < determinType(RivalCards, hand.getTwopairid())) {
                message = "あなたの勝利です!";
                matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
                matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
              } else if (determinType(myCards, userhand.getTwopairid()) > determinType(RivalCards,
                  hand.getTwopairid())) {
                message = "あなたの負けです...";
                matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
                matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
              }
            }
          }
        }
        // ワン・ペア同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 9) {
          if (userhand.getRolenum() > hand.getRolenum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (userhand.getRolenum() < hand.getRolenum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (userhand.getRolenum() == hand.getRolenum()) {
            if (userhand.getOnepairkickernum() > hand.getOnepairkickernum()) {
              message = "あなたの勝利です!";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
            } else if (userhand.getOnepairkickernum() < hand.getOnepairkickernum()) {
              message = "あなたの負けです...";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
            } else if (userhand.getOnepairkickernum() == hand.getOnepairkickernum()) {
              if (determinType(myCards,
                  userhand.getOnepairkickernum()) < determinType(RivalCards, hand.getOnepairkickerid())) {
                message = "あなたの勝利です!";
                matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
                matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
              } else if (determinType(myCards, userhand.getOnepairkickerid()) > determinType(RivalCards,
                  hand.getOnepairkickerid())) {
                message = "あなたの負けです...";
                matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
                matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
              }
            }
          }
        }
        // ハイカード同士の比較
        else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 10) {
          if (myCards.get(4).getNum() > RivalCards.get(4).getNum()) {
            message = "あなたの勝利です!";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
          } else if (myCards.get(4).getNum() < RivalCards.get(4).getNum()) {
            message = "あなたの負けです...";
            matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
            matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
          } else if (myCards.get(4).getNum() == RivalCards.get(4).getNum()) {
            if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
              message = "あなたの勝利です!";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
            } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
              message = "あなたの負けです...";
              matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() + match.getBet());
              matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() - match.getBet());
            }
          }
        }

        drop.syncDrop2(match.getId(), match.getRid());
        match = matchMapper.selectAllById(userid);
        model.addAttribute("message", message);
        model.addAttribute("coin", match.getUser2coin());
      }
      model.addAttribute("rid", match.getRid());
      return "result";
    } else {
      model.addAttribute("myCards", myCards);
      model.addAttribute("rid", match.getRid());
      return "wait";
    }
  }

  @GetMapping("poker/result2")
  public String result2(ModelMap model, Principal prin) {
    int userid;
    int userid2;
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    match match;
    String message = "";
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    ArrayList<Cards> RivalCards = new ArrayList<Cards>();

    userid = userMapper.selectid(loginUser);
    Hand userhand = handMapper.selectByUserId(userid);
    myCards.add(cardsMapper.selectAllById(userhand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand5id()));
    Hand hand;
    match = matchMapper.selectAllById(userid);
    model.addAttribute("round", match.getRound() / 2 + 1);
    if (match.getUser1id() == userid) {
      userid2 = match.getUser2id();
      hand = handMapper.selectByUserId(userid2);
      RivalCards.add(cardsMapper.selectAllById(hand.getHand1id()));
      RivalCards.add(cardsMapper.selectAllById(hand.getHand2id()));
      RivalCards.add(cardsMapper.selectAllById(hand.getHand3id()));
      RivalCards.add(cardsMapper.selectAllById(hand.getHand4id()));
      RivalCards.add(cardsMapper.selectAllById(hand.getHand5id()));

      int a1 = myCards.get(4).getNum();
      int a2 = RivalCards.get(4).getNum();
      // 5枚目のカードが1の時、数値比較の都合上14にする
      if (myCards.get(4).getNum() == 1) {
        a1 = 14;
      }
      if (RivalCards.get(4).getNum() == 1) {
        a2 = 14;
      }

      // Roleidの大小関係で勝利者を判定
      if (userhand.getRoleid() < hand.getRoleid()) {
        message = "あなたの勝利です!";
      } else if (userhand.getRoleid() > hand.getRoleid()) {
        message = "あなたの負けです...";
      }
      // ロイヤルストレートフラッシュ同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 1) {
        if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
          message = "あなたの勝利です!";
        } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
          message = "あなたの負けです...";
        }
      }
      // フォア・カード同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 3) {
        if (myCards.get(3).getNum() > RivalCards.get(3).getNum()) {
          message = "あなたの勝利です!";
        } else if (myCards.get(3).getNum() < RivalCards.get(3).getNum()) {
          message = "あなたの負けです...";
        }
      }
      // フルハウス・スリーカード同士の比較
      else if ((userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 4)
          || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 7)) {
        if (myCards.get(2).getNum() > RivalCards.get(2).getNum()) {
          message = "あなたの勝利です!";
        } else if (myCards.get(2).getNum() < RivalCards.get(2).getNum()) {
          message = "あなたの負けです...";
        }
      }
      // ストレートフラッシュ・フラッシュ・ストレート同士の比較
      else if ((userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 2)
          || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 5)
          || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 6)) {
        if (a1 > a2) {
          message = "あなたの勝利です!";
        } else if (a1 < a2) {
          message = "あなたの負けです...";
        } else if (a1 == a2) {
          if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
            message = "あなたの勝利です!";
          } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
            message = "あなたの負けです...";
          }
        }
      }
      // ツウ・ペア同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 8) {
        if (myCards.get(3).getNum() > RivalCards.get(3).getNum()) {
          message = "あなたの勝利です!";
        } else if (myCards.get(3).getNum() < RivalCards.get(3).getNum()) {
          message = "あなたの負けです...";
        } else if (myCards.get(3).getNum() == RivalCards.get(3).getNum()) {
          if (userhand.getRolenum() > hand.getRolenum()) {
            message = "あなたの勝利です!";
          } else if (userhand.getRolenum() < hand.getRolenum()) {
            message = "あなたの負けです...";
          } else if (userhand.getRolenum() == hand.getRolenum()) {
            if (determinType(myCards, userhand.getTwopairid()) < determinType(RivalCards, hand.getTwopairid())) {
              message = "あなたの勝利です!";
            } else if (determinType(myCards, userhand.getTwopairid()) > determinType(RivalCards,
                hand.getTwopairid())) {
              message = "あなたの負けです...";
            }
          }
        }
      }
      // ワン・ペア同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 9) {
        if (userhand.getRolenum() > hand.getRolenum()) {
          message = "あなたの勝利です!";
        } else if (userhand.getRolenum() < hand.getRolenum()) {
          message = "あなたの負けです...";
        } else if (userhand.getRolenum() == hand.getRolenum()) {
          if (userhand.getOnepairkickernum() > hand.getOnepairkickernum()) {
            message = "あなたの勝利です!";
          } else if (userhand.getOnepairkickernum() < hand.getOnepairkickernum()) {
            message = "あなたの負けです...";
          } else if (userhand.getOnepairkickernum() == hand.getOnepairkickernum()) {
            if (determinType(myCards,
                userhand.getOnepairkickernum()) < determinType(RivalCards, hand.getOnepairkickerid())) {
              message = "あなたの勝利です!";
            } else if (determinType(myCards, userhand.getOnepairkickerid()) > determinType(RivalCards,
                hand.getOnepairkickerid())) {
              message = "あなたの負けです...";
            }
          }
        }
      }
      // ハイカード同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 10) {
        if (myCards.get(4).getNum() > RivalCards.get(4).getNum()) {
          message = "あなたの勝利です!";
        } else if (myCards.get(4).getNum() < RivalCards.get(4).getNum()) {
          message = "あなたの負けです...";
        } else if (myCards.get(4).getNum() == RivalCards.get(4).getNum()) {
          if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
            message = "あなたの勝利です!";
          } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
            message = "あなたの負けです...";
          }
        }
      }
      model.addAttribute("message", message);
      model.addAttribute("coin", match.getUser1coin());
    } else if (match.getUser2id() == userid) {
      userid2 = match.getUser1id();
      hand = handMapper.selectByUserId(userid2);
      RivalCards.add(cardsMapper.selectAllById(hand.getHand1id()));
      RivalCards.add(cardsMapper.selectAllById(hand.getHand2id()));
      RivalCards.add(cardsMapper.selectAllById(hand.getHand3id()));
      RivalCards.add(cardsMapper.selectAllById(hand.getHand4id()));
      RivalCards.add(cardsMapper.selectAllById(hand.getHand5id()));

      int a1 = myCards.get(4).getNum();
      int a2 = RivalCards.get(4).getNum();
      // 5枚目のカードが1の時、数値比較の都合上14にする
      if (myCards.get(4).getNum() == 1) {
        a1 = 14;
      }
      if (RivalCards.get(4).getNum() == 1) {
        a2 = 14;
      }

      // Roleidの大小関係で勝利者を判定
      if (userhand.getRoleid() < hand.getRoleid()) {
        message = "あなたの勝利です!";
      } else if (userhand.getRoleid() > hand.getRoleid()) {
        message = "あなたの負けです...";
      }
      // ロイヤルストレートフラッシュ同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 1) {
        if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
          message = "あなたの勝利です!";
        } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
          message = "あなたの負けです...";
        }
      }
      // フォア・カード同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 3) {
        if (myCards.get(3).getNum() > RivalCards.get(3).getNum()) {
          message = "あなたの勝利です!";
        } else if (myCards.get(3).getNum() < RivalCards.get(3).getNum()) {
          message = "あなたの負けです...";
        }
      }
      // フルハウス・スリーカード同士の比較
      else if ((userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 4)
          || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 7)) {
        if (myCards.get(2).getNum() > RivalCards.get(2).getNum()) {
          message = "あなたの勝利です!";
        } else if (myCards.get(2).getNum() < RivalCards.get(2).getNum()) {
          message = "あなたの負けです...";
        }
      }
      // ストレートフラッシュ・フラッシュ・ストレート同士の比較
      else if ((userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 2)
          || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 5)
          || (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 6)) {
        if (a1 > a2) {
          message = "あなたの勝利です!";
        } else if (a1 < a2) {
          message = "あなたの負けです...";
        } else if (a1 == a2) {
          if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
            message = "あなたの勝利です!";
          } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
            message = "あなたの負けです...";
          }
        }
      }
      // ツウ・ペア同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 8) {
        if (myCards.get(3).getNum() > RivalCards.get(3).getNum()) {
          message = "あなたの勝利です!";
        } else if (myCards.get(3).getNum() < RivalCards.get(3).getNum()) {
          message = "あなたの負けです...";
        } else if (myCards.get(3).getNum() == RivalCards.get(3).getNum()) {
          if (userhand.getRolenum() > hand.getRolenum()) {
            message = "あなたの勝利です!";
          } else if (userhand.getRolenum() < hand.getRolenum()) {
            message = "あなたの負けです...";
          } else if (userhand.getRolenum() == hand.getRolenum()) {
            if (determinType(myCards, userhand.getTwopairid()) < determinType(RivalCards, hand.getTwopairid())) {
              message = "あなたの勝利です!";
            } else if (determinType(myCards, userhand.getTwopairid()) > determinType(RivalCards,
                hand.getTwopairid())) {
              message = "あなたの負けです...";
            }
          }
        }
      }
      // ワン・ペア同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 9) {
        if (userhand.getRolenum() > hand.getRolenum()) {
          message = "あなたの勝利です!";
        } else if (userhand.getRolenum() < hand.getRolenum()) {
          message = "あなたの負けです...";
        } else if (userhand.getRolenum() == hand.getRolenum()) {
          if (userhand.getOnepairkickernum() > hand.getOnepairkickernum()) {
            message = "あなたの勝利です!";
          } else if (userhand.getOnepairkickernum() < hand.getOnepairkickernum()) {
            message = "あなたの負けです...";
          } else if (userhand.getOnepairkickernum() == hand.getOnepairkickernum()) {
            if (determinType(myCards,
                userhand.getOnepairkickernum()) < determinType(RivalCards, hand.getOnepairkickerid())) {
              message = "あなたの勝利です!";
            } else if (determinType(myCards, userhand.getOnepairkickerid()) > determinType(RivalCards,
                hand.getOnepairkickerid())) {
              message = "あなたの負けです...";
            }
          }
        }
      }
      // ハイカード同士の比較
      else if (userhand.getRoleid() == hand.getRoleid() && hand.getRoleid() == 10) {
        if (myCards.get(4).getNum() > RivalCards.get(4).getNum()) {
          message = "あなたの勝利です!";
        } else if (myCards.get(4).getNum() < RivalCards.get(4).getNum()) {
          message = "あなたの負けです...";
        } else if (myCards.get(4).getNum() == RivalCards.get(4).getNum()) {
          if (determinType(myCards, 4) < determinType(RivalCards, 4)) {
            message = "あなたの勝利です!";
          } else if (determinType(myCards, 4) > determinType(RivalCards, 4)) {
            message = "あなたの負けです...";
          }
        }
      }
      model.addAttribute("message", message);
      model.addAttribute("coin", match.getUser2coin());
    }
    model.addAttribute("rid", match.getRid());
    return "result";

  }

  private final Logger logger = LoggerFactory.getLogger(PokerController.class);

  @Autowired
  private AsyncCount sse;

  @GetMapping("step1")
  public SseEmitter pushCount() {
    // infoレベルでログを出力する
    logger.info("pushCount");

    // finalは初期化したあとに再代入が行われない変数につける（意図しない再代入を防ぐ）
    final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);//
    // 引数にLongの最大値をTimeoutとして指定する

    try {
      this.sse.count(emitter);
    } catch (IOException e) {
      // 例外の名前とメッセージだけ表示する
      logger.warn("Exception:" + e.getClass().getName() + ":" + e.getMessage());
      emitter.complete();
    }
    return emitter;
  }

  @GetMapping("/start/{rid}")
  public SseEmitter sample(@PathVariable int rid) {
    final SseEmitter emitter = new SseEmitter();
    this.ready.AsyncReadySend(emitter, rid);
    return emitter;
  }

  @GetMapping("/drop3/{rid}")
  public SseEmitter dropSse(@PathVariable int rid) {
    final SseEmitter emitter = new SseEmitter();
    drop.registerEmitter(rid, emitter);
    // 初回アクセスで監視タスクを起動
    drop.startRoomMonitor(rid);
    return emitter;
  }

  @GetMapping("/result/{rid}")
  public SseEmitter resultSse(@PathVariable int rid) {
    final SseEmitter emitter = new SseEmitter();
    drop.registerEmitter(rid, emitter);
    // 初回アクセスで監視タスクを起動
    drop.startRoomMonitor(rid);
    return emitter;
  }
}
