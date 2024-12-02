package hakata.poker.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Comparator;
import java.util.Arrays;

import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hakata.poker.model.Room;
import hakata.poker.model.index;
import hakata.poker.model.Cards;
import hakata.poker.model.CardsMapper;
import hakata.poker.model.Hand;
import hakata.poker.model.UserMapper;
import hakata.poker.model.HandMapper;
import hakata.poker.model.Match;
import hakata.poker.model.matchMapper;
import hakata.poker.service.AsyncMatch;
import hakata.poker.service.AsyncState;

@Controller
public class PokerController {

  @Autowired
  private Room room;
  Random rmd = new Random();

  @Autowired
  private CardsMapper cardsMapper;

  @Autowired
  private HandMapper handMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private matchMapper matchMapper;

  @Autowired
  AsyncMatch match12;

  @Autowired
  AsyncState states;

  @GetMapping("room")
  public String room_login(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    this.room.addUser(loginUser);
    model.addAttribute("login_user", loginUser);
    model.addAttribute("room", room);
    return "room.html";
  }

  @GetMapping("help")
  public String help_page(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    return "help.html";
  }

  @GetMapping("poker")
  public String login(ModelMap model, Principal prin) {
    int userid;
    int coin = 5;
    Match match = new Match();
    ArrayList<String> user;
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    userid = userMapper.selectid(loginUser);
    // マッチ内容を登録する処理(ルームに二人いる想定で作成中)
    if (matchMapper.selectAllById(userid).isEmpty() && room.getCount() == 2) {
      user = room.getUsers();
      userid = userMapper.selectid(user.get(0));
      match.setUser1id(userid);
      userid = userMapper.selectid(user.get(1));
      match.setUser2id(userid);
      match.setUser1coin(coin);
      match.setUser2coin(coin);
      match.setBet(1);
      this.match12.syncNewMatch(match);
      return "poker.html";
    }
    // ここまで
    else if (!matchMapper.selectAllById(userid).isEmpty()) {
      return "poker.html";
    } else {
      model.addAttribute("room", room);
      return "room.html";
    }
  }

  @GetMapping("poker/card")
  public String showCard(ModelMap model, Principal prin) {
    int userid;
    int coin = 5;

    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで

    // 手札をデータベースに登録する処理
    Hand hand = new Hand();
    hand.setActive(true);
    ArrayList<Cards> myCards = cardsMapper.select5RandomCard();
    for (Cards card : myCards) {
      cardsMapper.updateisActiveTrueById(card.getId());
    }

    hand.setHand1id(myCards.get(0).getId());
    hand.setHand2id(myCards.get(1).getId());
    hand.setHand3id(myCards.get(2).getId());
    hand.setHand4id(myCards.get(3).getId());
    hand.setHand5id(myCards.get(4).getId());
    hand.setCoin(coin);
    userid = userMapper.selectid(loginUser);
    hand.setUserid(userid);
    myCards.sort(Comparator.comparing(Cards::getNum));
    handMapper.insertHandandIsActive(hand);
    // ここまで
    model.addAttribute("myCards", myCards);
    model.addAttribute("coin", coin);
    model.addAttribute("index", new index());
    return "poker.html";

  }

  @PostMapping("/result")
  public String formResult(@ModelAttribute index index, ModelMap model, Principal prin) {
    int id;
    Hand hand;
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    Cards drawCards;
    Match match;

    int Straightflag = 0;
    int Flashflag = 0; // フラッシュの旗

    int onepairnum = 0;

    for (Integer indes : index.getId()) {
      System.out.println(indes + "選択されました: ");
    }
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);

    id = userMapper.selectid(loginUser);
    hand = handMapper.selectByUserId(id);
    handMapper.updateIsActivefalsetotrueByfalseAndUserId(id);

    myCards.add(cardsMapper.selectAllById(hand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand5id()));

    for (Integer indes : index.getId()) {
      drawCards = cardsMapper.selectRandomCard();
      while (drawCards.getActive()) {
        drawCards = cardsMapper.selectRandomCard();

      }
      myCards.set(indes - 1, drawCards);
      cardsMapper.updateisActiveTrueById(myCards.get(indes - 1).getId());
    }
    myCards.sort(Comparator.comparing(Cards::getNum));
    hand.setHand1id(myCards.get(0).getId());
    hand.setHand2id(myCards.get(1).getId());
    hand.setHand3id(myCards.get(2).getId());
    hand.setHand4id(myCards.get(3).getId());
    hand.setHand5id(myCards.get(4).getId());
    handMapper.insertHandandIsActive(hand);
    // ここまでカードをデータベースに登録する処理
    // ここから試合状況のほうにデータベースの登録を行う処理
    if (matchMapper.selectAllByuser1Id(id) != null) {
      match = matchMapper.selectAllByuser1Id(id);
      model.addAttribute("coin", match.getUser1coin());
      this.states.syncchange1(match.getId(), "change");
      model.addAttribute("rays", match.getBet());

    } else if (matchMapper.selectAllByuser2Id(id) != null) {
      match = matchMapper.selectAllByuser2Id(id);
      model.addAttribute("coin", match.getUser2coin());
      this.states.syncchange2(match.getId(), "change");
      model.addAttribute("rays", match.getBet());
    }
    model.addAttribute("myCards", myCards);
    model.addAttribute("index", new index());
    // ストレートの判定
    if (myCards.get(0).getNum() == myCards.get(1).getNum() + 1 && myCards.get(1).getNum() == myCards.get(2).getNum() + 1
        && myCards.get(2).getNum() == myCards.get(3).getNum() + 1
        && myCards.get(3).getNum() == myCards.get(4).getNum() + 1) {
      hand.setRoleid(6);
      String role = "あなたの役はストレートです。";
      Straightflag = 1;

      model.addAttribute("role", role);
    }
    // フラッシュの判定
    if (myCards.get(0).getCardtype() == myCards.get(1).getCardtype()
        && myCards.get(1).getCardtype() == myCards.get(2).getCardtype()
        && myCards.get(2).getCardtype() == myCards.get(3).getCardtype()
        && myCards.get(3).getCardtype() == myCards.get(4).getCardtype()) {
      hand.setRoleid(5);
      String role = "あなたの役はフラッシュです。";
      Flashflag = 1;

      model.addAttribute("role", role);
    }
    // ロイヤルストレートフラッシュの判定
    if (Flashflag == 1 && myCards.get(0).getNum() == 1 && myCards.get(1).getNum() == 10
        && myCards.get(2).getNum() == 11 && myCards.get(3).getNum() == 12 && myCards.get(4).getNum() == 13) {
      hand.setRoleid(1);
      String role = "あなたの役はロイヤルストレートフラッシュです。";

      model.addAttribute("role", role);
    }
    // ストレートフラッシュの判定
    else if (Straightflag == 1 && Flashflag == 1) {
      hand.setRoleid(2);
      String role = "あなたの役はストレートフラッシュです。";

      model.addAttribute("role", role);
    }

    // フォーカードの判定
    if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum()
        && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum()
            && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      hand.setRoleid(3);
      String role = "あなたの役はフォア・カードです。";

      model.addAttribute("role", role);
    }
    // スリーカードの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(2).getNum() == myCards.get(3).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      hand.setRoleid(7);
      String role = "あなたの役はスリーカードです。";

      model.addAttribute("role", role);
      // フルハウスの判定
      if (myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum()) {
        if (myCards.get(3).getNum() == myCards.get(4).getNum()) {
          hand.setRoleid(4);
          role = "あなたの役はフルハウスです。";

          model.addAttribute("role", role);
        }
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum()
          && myCards.get(3).getNum() == myCards.get(4).getNum()) {
        if (myCards.get(0).getNum() == myCards.get(1).getNum()) {
          hand.setRoleid(4);
          role = "あなたの役はフルハウスです。";

          model.addAttribute("role", role);
        }
      }
    }
    // ツウ・ペアの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())
        || (myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      hand.setRoleid(8);
      String role = "あなたの役はツウ・ペアです。";

      model.addAttribute("role", role);
    }
    // ワン・ペアの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum()) || (myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(3).getNum() == myCards.get(4).getNum())) {
      hand.setRoleid(9);
      String role = "あなたの役はワン・ペアです。";

      model.addAttribute("role", role);
      // ワンペア時の数値を格納
      if (myCards.get(0).getNum() == myCards.get(1).getNum()) {
        onepairnum = myCards.get(1).getNum();
      } else if (myCards.get(1).getNum() == myCards.get(2).getNum()) {
        onepairnum = myCards.get(2).getNum();
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum()) {
        onepairnum = myCards.get(3).getNum();
      } else if (myCards.get(3).getNum() == myCards.get(4).getNum()) {
        onepairnum = myCards.get(4).getNum();
      }
    }
    return "select";
  }

  @GetMapping("poker/call")
  public String showCall(ModelMap model, Principal prin) {
    int id;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    Match match;
    model.addAttribute("login_user", loginUser);
    // ここまで
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    id = userMapper.selectid(loginUser);
    Hand hand = handMapper.selectByUserId(id);

    myCards.add(cardsMapper.selectAllById(hand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand5id()));

    if (matchMapper.selectAllByuser1Id(id) != null) {
      match = matchMapper.selectAllByuser1Id(id);
      model.addAttribute("coin", match.getUser1coin());
      this.states.synccall1(match.getId(), "call");
      model.addAttribute("rays", match.getBet());
    } else if (matchMapper.selectAllByuser2Id(id) != null) {
      match = matchMapper.selectAllByuser2Id(id);
      model.addAttribute("coin", match.getUser2coin());
      this.states.synccall2(match.getId(), "call");
      model.addAttribute("rays", match.getBet());
    }

    model.addAttribute("myCards", myCards);
    model.addAttribute("index", new index());

    return "poker.html";
  }

  @GetMapping("poker/drop")
  public String showDrop(ModelMap model, Principal prin) {
    int id;
    int coin;
    Match match;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで

    ArrayList<Cards> myCards = new ArrayList<Cards>();
    id = userMapper.selectid(loginUser);
    Hand hand = handMapper.selectByUserId(id);
    handMapper.updateIsActivefalsetotrueByfalseAndUserId(id);
    coin = hand.getCoin() - 1;
    hand.setCoin(coin);
    myCards.add(cardsMapper.selectAllById(hand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand5id()));
    handMapper.insertHandandIsActive(hand);

    if (matchMapper.selectAllByuser1Id(id) != null) {
      match = matchMapper.selectAllByuser1Id(id);
      coin = match.getUser1coin() - 1;
      match.setUser1coin(coin);
      model.addAttribute("coin", match.getUser1coin());
      this.states.syncdrop1(match.getId(), "drop");
      matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin());

    } else if (matchMapper.selectAllByuser2Id(id) != null) {
      match = matchMapper.selectAllByuser2Id(id);
      coin = match.getUser2coin() - 1;
      match.setUser2coin(coin);
      model.addAttribute("coin", match.getUser2coin());
      this.states.syncdrop2(match.getId(), "drop");
      matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin());
    }
    model.addAttribute("myCards", myCards);
    model.addAttribute("index", new index());

    return "poker.html";
  }

  @GetMapping("poker/rays")
  public String rays(ModelMap model, Principal prin) {
    int id;
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    id = userMapper.selectid(loginUser);
    Hand hand = handMapper.selectByUserId(id);
    model.addAttribute("coin", hand.getCoin());
    return "rays.html";
  }

  @PostMapping("/rays")
  public String formRays(@RequestParam("rays") int rays, ModelMap model, Principal prin) {
    int id;
    Hand hand;
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    Match match;
    String loginUser = prin.getName();
    int bet;
    model.addAttribute("login_user", loginUser);

    id = userMapper.selectid(loginUser);
    hand = handMapper.selectByUserId(id);

    myCards.add(cardsMapper.selectAllById(hand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand5id()));

    if (matchMapper.selectAllByuser1Id(id) != null) {
      match = matchMapper.selectAllByuser1Id(id);
      model.addAttribute("coin", match.getUser1coin());
      this.states.syncrays2(match.getId(), "rays");
      bet = rays + match.getBet();
      match.setBet(bet);
      matchMapper.updateBetById(match.getId(), bet);
      model.addAttribute("rays", match.getBet());
    } else if (matchMapper.selectAllByuser2Id(id) != null) {
      match = matchMapper.selectAllByuser2Id(id);
      model.addAttribute("coin", match.getUser2coin());
      this.states.syncrays2(match.getId(), "rays");
      bet = rays + match.getBet();
      match.setBet(bet);
      matchMapper.updateBetById(match.getId(), bet);
      model.addAttribute("rays", match.getBet());
    }

    model.addAttribute("myCards", myCards);
    model.addAttribute("index", new index());

    return "poker";
  }

  @GetMapping("/sample")
  public SseEmitter sample() {
    final SseEmitter emitter = new SseEmitter();
    this.match12.AsyncMatchsend(emitter);
    return emitter;
  }

  @GetMapping("/state")
  public SseEmitter state() {
    final SseEmitter emitter = new SseEmitter();
    this.states.AsyncMatchsend(emitter);
    return emitter;
  }
}
