package hakata.poker.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Comparator;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hakata.poker.model.Room;
import hakata.poker.model.index;
import hakata.poker.service.AsyncCount;
import hakata.poker.model.Cards;
import hakata.poker.model.CardsMapper;
import hakata.poker.model.Hand;
import hakata.poker.model.UserMapper;
import hakata.poker.model.HandMapper;

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

  @GetMapping("room")
  public String room_login(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    this.room.addUser("CPU");
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
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    return "poker.html";
  }

  @GetMapping("poker/card")
  public String showCard(ModelMap model, Principal prin) {
    int userid;
    int coin = 5;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで
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
    model.addAttribute("myCards", myCards);
    model.addAttribute("coin", hand.getCoin());
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
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum()) {
        if (myCards.get(0).getNum() == myCards.get(1).getNum()) {
          hand.setRoleid(4);
          role = "あなたの役はフルハウスです。";

          model.addAttribute("role", role);
        }
      }
    }
    // ツウ・ペアの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum()) || (myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())) {
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

    model.addAttribute("myCards", myCards);
    model.addAttribute("coin", hand.getCoin());
    model.addAttribute("index", new index());

    return "poker.html";
  }

  @GetMapping("poker/drop")
  public String showDrop(ModelMap model, Principal prin) {
    int id;
    int coin;
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで

    String message = "ドロップしました";
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
    model.addAttribute("myCards", myCards);
    model.addAttribute("coin", hand.getCoin());
    model.addAttribute("index", new index());
    model.addAttribute("message", message);

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
  public String formRays(@RequestParam("rays") Integer rays, ModelMap model, Principal prin) {
    int id;
    Hand hand;
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);

    id = userMapper.selectid(loginUser);
    hand = handMapper.selectByUserId(id);

    myCards.add(cardsMapper.selectAllById(hand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand5id()));
    model.addAttribute("rays", rays);
    model.addAttribute("myCards", myCards);
    model.addAttribute("coin", hand.getCoin());
    model.addAttribute("index", new index());

    return "poker";
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
}
