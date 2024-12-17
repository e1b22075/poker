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

import hakata.poker.model.Room;
import hakata.poker.model.RoomMapper;
import hakata.poker.model.PlayerIndex;
import hakata.poker.service.AsyncCount;
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
    hand.setTurn(turn);
    userid = userMapper.selectid(loginUser);
    hand.setUserid(userid);
    myCards.sort(Comparator.comparing(Cards::getNum));

    handMapper.insertHandandIsActive(hand);

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

    return "poker.html";
  }

  @PostMapping("/result")
  public String formResult(@ModelAttribute PlayerIndex index, ModelMap model, Principal prin) {
    int userid;
    Hand userhand;
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    Cards userdrawCards;
    match match;

    int myflag1 = 0; // ロイヤルストレートフラッシュ
    int myflag2 = 0; // ストレートフラッシュ
    int myflag3 = 0; // フォア・カード
    int myflag4 = 0; // フルハウス
    int myflag5 = 0; // フラッシュ
    int myflag6 = 0; // ストレート
    int myflag7 = 0; // スリーカード
    int myflag8 = 0; // ツウ・ペア
    int myflag9 = 0; // ワン・ペア

    int myonepairnum = 0;

    String myrole;

    int myresultflag = 10;

    String result;

    for (Integer indes : index.getId()) {
      System.out.println(indes + "選択されました: ");
    }
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

    for (Integer indes : index.getId()) {
      userdrawCards = cardsMapper.selectRandomCard();
      while (userdrawCards.getActive()) {
        userdrawCards = cardsMapper.selectRandomCard();
      }
      myCards.set(indes - 1, userdrawCards);
      cardsMapper.updateisActiveTrueById(myCards.get(indes - 1).getId());
    }
    myCards.sort(Comparator.comparing(Cards::getNum));
    userhand.setHand1id(myCards.get(0).getId());
    userhand.setHand2id(myCards.get(1).getId());
    userhand.setHand3id(myCards.get(2).getId());
    userhand.setHand4id(myCards.get(3).getId());
    userhand.setHand5id(myCards.get(4).getId());
    handMapper.insertHandandIsActive(userhand);

    model.addAttribute("myCards", myCards);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      model.addAttribute("coin", match.getUser1coin());
    } else if (match.getUser2id() == userid) {
      model.addAttribute("coin", match.getUser2coin());
    }

    model.addAttribute("rays", match.getBet());

    model.addAttribute("myCards", myCards);
    model.addAttribute("turn", userhand.getTurn());

    model.addAttribute("myCards", myCards);

    model.addAttribute("index", new index());

    // ストレートの判定
    if (myCards.get(4).getNum() == myCards.get(3).getNum() + 1 && myCards.get(3).getNum() == myCards.get(2).getNum() + 1
        && myCards.get(2).getNum() == myCards.get(1).getNum() + 1
        && myCards.get(1).getNum() == myCards.get(0).getNum() + 1) {
      userhand.setRoleid(6);
      myflag6 = 1;
    }
    // フラッシュの判定
    if (myCards.get(0).getCardtype() == myCards.get(1).getCardtype()
        && myCards.get(1).getCardtype() == myCards.get(2).getCardtype()
        && myCards.get(2).getCardtype() == myCards.get(3).getCardtype()
        && myCards.get(3).getCardtype() == myCards.get(4).getCardtype()) {
      userhand.setRoleid(5);
      myflag5 = 1;
    }
    // ロイヤルストレートフラッシュの判定
    if (myflag5 == 1 && myCards.get(0).getNum() == 1 && myCards.get(1).getNum() == 10
        && myCards.get(2).getNum() == 11 && myCards.get(3).getNum() == 12 && myCards.get(4).getNum() == 13) {
      userhand.setRoleid(1);
      myflag1 = 1;
    }
    // ストレートフラッシュの判定
    else if (myflag6 == 1 && myflag5 == 1) {
      userhand.setRoleid(2);
      myflag2 = 1;
    }

    // フォーカードの判定
    if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum()
        && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum()
            && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(3);
      myflag3 = 1;
    }
    // スリーカードの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(2).getNum() == myCards.get(3).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(7);
      myflag7 = 1;

      // フルハウスの判定
      if (myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(1).getNum() == myCards.get(2).getNum()) {
        if (myCards.get(3).getNum() == myCards.get(4).getNum()) {
          userhand.setRoleid(4);
          myflag4 = 1;
        }
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum()
          && myCards.get(3).getNum() == myCards.get(4).getNum()) {
        if (myCards.get(0).getNum() == myCards.get(1).getNum()) {
          userhand.setRoleid(4);
          myflag4 = 1;
        }
      }
    }
    // ツウ・ペアの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())
        || (myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(8);
      myflag8 = 1;
    }
    // ワン・ペアの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum())
        || (myCards.get(1).getNum() == myCards.get(2).getNum()) || (myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(9);
      myflag9 = 1;

      // ワンペア時の数値を格納
      if (myCards.get(0).getNum() == myCards.get(1).getNum()) {
        myonepairnum = myCards.get(1).getNum();
      } else if (myCards.get(1).getNum() == myCards.get(2).getNum()) {
        myonepairnum = myCards.get(2).getNum();
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum()) {
        myonepairnum = myCards.get(3).getNum();
      } else if (myCards.get(3).getNum() == myCards.get(4).getNum()) {
        myonepairnum = myCards.get(4).getNum();
      }
    }

    if (myflag1 == 1) {
      myrole = "あなたの役はロイヤルストレートフラッシュです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 1;
    } else if (myflag2 == 1) {
      myrole = "あなたの役はストレートフラッシュです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 2;
    } else if (myflag3 == 1) {
      myrole = "あなたの役はフォア・カードです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 3;
    } else if (myflag4 == 1) {
      myrole = "あなたの役はフルハウスです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 4;
    } else if (myflag5 == 1) {
      myrole = "あなたの役はフラッシュです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 5;
    } else if (myflag6 == 1) {
      myrole = "あなたの役はストレートです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 6;
    } else if (myflag7 == 1) {
      myrole = "あなたの役はスリーカードです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 7;
    } else if (myflag8 == 1) {
      myrole = "あなたの役はツウ・ペアです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 8;
    } else if (myflag9 == 1) {
      myrole = "あなたの役はワン・ペアです。";
      model.addAttribute("myrole", myrole);
      myresultflag = 9;
    }

    return "select";
  }

  @GetMapping("poker/call")
  public String showCall(ModelMap model, Principal prin) {
    int userid;
    int cpuid;
    match match;
    String cpuname = "CPU";
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

    hand.setTurn(hand.getTurn() + 1);
    handMapper.insertHandandIsActive(hand);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      model.addAttribute("coin", match.getUser1coin());
    } else if (match.getUser2id() == userid) {
      model.addAttribute("coin", match.getUser2coin());
    }

    model.addAttribute("rays", match.getBet());

    model.addAttribute("turn", hand.getTurn());

    model.addAttribute("myCards", myCards);
    if (hand.getTurn() >= 3) {
      if (match.getUser1id() == userid) {
        this.result.syncUser1(match.getId(), hand.getId());
        System.out.println("ユーザー1の書き込み");
      } else if (match.getUser2id() == userid) {
        this.result.syncUser2(match.getId(), hand.getId());
        System.out.println("ユーザー2の書き込み");
      }
      match = matchMapper.selectAllById(userid);
      if (match.getUser1hand() != 0 && match.getUser2hand() != 0) {

      }
      return "wait";
    } else {
      model.addAttribute("index", new index());

      return "poker.html";
    }
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

    handMapper.insertHandandIsActive(userhand);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      match.setUser1coin(match.getUser1coin() - 1);
      model.addAttribute("coin", match.getUser1coin());
      matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin());
      drop.syncDrop1(match.getId());
    } else if (match.getUser2id() == userid) {
      match.setUser2coin(match.getUser2coin() - 1);
      model.addAttribute("coin", match.getUser2coin());
      matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin());
      drop.syncDrop2(match.getId());
    }
    match.setBet(1);
    matchMapper.updateBetById(match.getId(), 1);
    model.addAttribute("rays", match.getBet());

    model.addAttribute("turn", userhand.getTurn());

    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);

    cardsMapper.updateAllfalsetotrueByfalse();

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

    handMapper.insertHandandIsActive(userhand);

    match = matchMapper.selectAllById(userid);

    if (match.getUser1id() == userid) {
      match.setUser1coin(match.getUser1coin() + 1);
      matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin());
      drop.syncDrop1(match.getId());
    } else if (match.getUser2id() == userid) {
      match.setUser2coin(match.getUser2coin() + 1);
      matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin());
      drop.syncDrop2(match.getId());
    }
    match.setBet(1);
    matchMapper.updateBetById(match.getId(), 1);
    model.addAttribute("rays", match.getBet());

    model.addAttribute("turn", userhand.getTurn());

    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);

    cardsMapper.updateAllfalsetotrueByfalse();

    model.addAttribute("message", message);

    model.addAttribute("index", new index());

    return "drop.html";
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

    userhand.setTurn(userhand.getTurn() + 1);
    handMapper.insertHandandIsActive(userhand);

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
    if (userhand.getTurn() >= 3) {
      if (match.getUser1id() == userid) {
        System.out.println("ユーザー１の書き込み");
        this.result.syncUser1(match.getId(), userhand.getId());
      } else if (match.getUser2id() == userid) {
        System.out.println("ユーザー2の書き込み");
        this.result.syncUser2(match.getId(), userhand.getId());
      }
      return "wait";
    } else {
      model.addAttribute("index", new index());

      return "poker";
    }
  }

  @GetMapping("poker/result")
  public String result(ModelMap model, Principal prin) {
    int userid;
    int userid2;
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    match match;
    String message;
    userid = userMapper.selectid(loginUser);
    Hand userhand = handMapper.selectByUserId(userid);
    Hand hand;
    match = matchMapper.selectAllById(userid);
    if (match.getUser1hand() != 0 && match.getUser2hand() != 0) {
      if (match.getUser1id() == userid) {
        userid2 = match.getUser2id();
        hand = handMapper.selectByUserId(userid2);
        message = "あなたの負けです";
        this.result.syncresult(userid);
        model.addAttribute("message", message);
      } else if (match.getUser2id() == userid) {
        userid2 = match.getUser1id();
        hand = handMapper.selectByUserId(userid2);
        message = "あなたの勝ちです";
        this.result.syncresult(userid);
        model.addAttribute("message", message);
      }

      return "result";
    } else {
      return "wait";
    }
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

  @GetMapping("/start")
  public SseEmitter sample() {
    final SseEmitter emitter = new SseEmitter();
    this.ready.AsyncReadySend(emitter);
    return emitter;
  }

  @GetMapping("/drop3")
  public SseEmitter dropSse() {
    final SseEmitter emitter = new SseEmitter();
    this.drop.AsyncDropSend(emitter);
    return emitter;
  }

  @GetMapping("/re")
  public SseEmitter resultSse() {
    final SseEmitter emitter = new SseEmitter();
    this.result.AsyncReusltSend(emitter);
    return emitter;
  }
}
