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
import hakata.poker.model.PlayerIndex;
import hakata.poker.model.CPUIndex;
import hakata.poker.service.AsyncCount;
import hakata.poker.model.Cards;
import hakata.poker.model.CardsMapper;
import hakata.poker.model.Hand;
import hakata.poker.model.User;
import hakata.poker.model.UserMapper;
import hakata.poker.model.HandMapper;
import hakata.poker.service.AsyncRoom;
import hakata.poker.service.AsyncUser;
import hakata.poker.model.index;

@Controller
@RequestMapping("/cpu")
public class CPUController {

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private CardsMapper cardsMapper;

  @Autowired
  private HandMapper handMapper;

  @Autowired
  private UserMapper userMapper;

  @GetMapping("poker")
  public String login(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    return "cpu_poker.html";
  }

  @GetMapping("poker/card")
  public String showCard(ModelMap model, Principal prin) {
    int userid;
    int cpuid;
    int coin = 5;
    String cpuname = "CPU";
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで

    CPUIndex CPUindex = new CPUIndex();
    CPUindex.getId();

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
    hand.setTurn(coin);
    userid = userMapper.selectid(loginUser);
    hand.setUserid(userid);
    myCards.sort(Comparator.comparing(Cards::getNum));

    handMapper.insertHandandIsActive(hand);
    model.addAttribute("myCards", myCards);
    model.addAttribute("coin", coin);
    model.addAttribute("myindex", new PlayerIndex());

    Hand CPUhand = new Hand();
    CPUhand.setActive(true);
    ArrayList<Cards> CPUCards = cardsMapper.select5RandomCard();
    for (Cards card : CPUCards) {
      cardsMapper.updateisActiveTrueById(card.getId());
    }

    CPUhand.setHand1id(CPUCards.get(0).getId());
    CPUhand.setHand2id(CPUCards.get(1).getId());
    CPUhand.setHand3id(CPUCards.get(2).getId());
    CPUhand.setHand4id(CPUCards.get(3).getId());
    CPUhand.setHand5id(CPUCards.get(4).getId());
    CPUhand.setTurn(coin);
    cpuid = userMapper.selectid(cpuname);
    CPUhand.setUserid(cpuid);
    CPUCards.sort(Comparator.comparing(Cards::getNum));

    handMapper.insertHandandIsActive(CPUhand);
    model.addAttribute("CPUCards", CPUCards);
    model.addAttribute("coin", coin);
    model.addAttribute("cpuindex", CPUindex);

    return "cpu_poker.html";
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

  @PostMapping("/result")
  public String formResult(@RequestParam("type") String type, @ModelAttribute PlayerIndex PlayerIndex,
      @ModelAttribute CPUIndex CPUIndex, ModelMap model, Principal prin) {
    int userid;
    int cpuid;
    Hand userhand;
    Hand cpuhand;
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    ArrayList<Cards> CPUCards = new ArrayList<Cards>();
    Cards userdrawCards;
    Cards cpudrawCards;

    int myflag1 = 0; // ロイヤルストレートフラッシュ
    int myflag2 = 0; // ストレートフラッシュ
    int myflag3 = 0; // フォア・カード
    int myflag4 = 0; // フルハウス
    int myflag5 = 0; // フラッシュ
    int myflag6 = 0; // ストレート
    int myflag7 = 0; // スリーカード
    int myflag8 = 0; // ツウ・ペア
    int myflag9 = 0; // ワン・ペア

    int cpuflag1 = 0;
    int cpuflag2 = 0;
    int cpuflag3 = 0;
    int cpuflag4 = 0;
    int cpuflag5 = 0;
    int cpuflag6 = 0;
    int cpuflag7 = 0;
    int cpuflag8 = 0;
    int cpuflag9 = 0;

    int myonepairnum = 0;
    int cpuonepairnum = 0;
    int myonepairkicker[] = new int[3];
    int cpuonepairkicker[] = new int[3];
    int mytwopairkicker = 0;
    int cputwopairkicker = 0;

    String myrole;
    String cpurole;

    int myresultflag = 10;
    int cpuresultflag = 10;

    String result;

    if ("player".equals(type)) {
      for (Integer indes : PlayerIndex.getId()) {
        System.out.println(indes + "選択されました: ");
      }
    }

    if ("cpu".equals(type)) {
      for (Integer inde : CPUIndex.getId()) {
        System.out.println(inde + "選択されました: ");
      }
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
    if ("player".equals(type)) {
      for (Integer indes : PlayerIndex.getId()) {
        userdrawCards = cardsMapper.selectRandomCard();
        while (userdrawCards.getActive()) {
          userdrawCards = cardsMapper.selectRandomCard();
        }
        myCards.set(indes - 1, userdrawCards);
        cardsMapper.updateisActiveTrueById(myCards.get(indes - 1).getId());
      }
    }

    myCards.sort(Comparator.comparing(Cards::getNum));
    userhand.setHand1id(myCards.get(0).getId());
    userhand.setHand2id(myCards.get(1).getId());
    userhand.setHand3id(myCards.get(2).getId());
    userhand.setHand4id(myCards.get(3).getId());
    userhand.setHand5id(myCards.get(4).getId());
    handMapper.insertHandandIsActive(userhand);
    model.addAttribute("myCards", myCards);

    model.addAttribute("myindex", new PlayerIndex());

    model.addAttribute("coin", userhand.getTurn());

    String cpuname = "CPU";
    cpuid = userMapper.selectid(cpuname);
    cpuhand = handMapper.selectByUserId(cpuid);
    handMapper.updateIsActivefalsetotrueByfalseAndUserId(cpuid);

    CPUCards.add(cardsMapper.selectAllById(cpuhand.getHand1id()));
    CPUCards.add(cardsMapper.selectAllById(cpuhand.getHand2id()));
    CPUCards.add(cardsMapper.selectAllById(cpuhand.getHand3id()));
    CPUCards.add(cardsMapper.selectAllById(cpuhand.getHand4id()));
    CPUCards.add(cardsMapper.selectAllById(cpuhand.getHand5id()));

    if ("cpu".equals(type)) {
      for (Integer indes : CPUIndex.getId()) {
        cpudrawCards = cardsMapper.selectRandomCard();
        while (cpudrawCards.getActive()) {
          cpudrawCards = cardsMapper.selectRandomCard();
        }
        CPUCards.set(indes - 1, cpudrawCards);
        cardsMapper.updateisActiveTrueById(CPUCards.get(indes - 1).getId());
      }
    }
    CPUCards.sort(Comparator.comparing(Cards::getNum));
    cpuhand.setHand1id(CPUCards.get(0).getId());
    cpuhand.setHand2id(CPUCards.get(1).getId());
    cpuhand.setHand3id(CPUCards.get(2).getId());
    cpuhand.setHand4id(CPUCards.get(3).getId());
    cpuhand.setHand5id(CPUCards.get(4).getId());
    handMapper.insertHandandIsActive(cpuhand);
    model.addAttribute("CPUCards", CPUCards);

    model.addAttribute("cpuindex", new CPUIndex());

    model.addAttribute("coin", cpuhand.getTurn());

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
        for (int i = 2; i <= 0; i--) {
          myonepairkicker[i] = myCards.get(i + 2).getNum();
        }
      } else if (myCards.get(1).getNum() == myCards.get(2).getNum()) {
        myonepairnum = myCards.get(2).getNum();
        for (int i = 2; i < 0; i--) {
          myonepairkicker[i] = myCards.get(i + 2).getNum();
        }
        myonepairkicker[0] = myCards.get(0).getNum();
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum()) {
        myonepairnum = myCards.get(3).getNum();
        myonepairkicker[2] = myCards.get(4).getNum();
        for (int i = 1; i <= 0; i--) {
          myonepairkicker[i] = myCards.get(i).getNum();
        }
      } else if (myCards.get(3).getNum() == myCards.get(4).getNum()) {
        myonepairnum = myCards.get(4).getNum();
        for (int i = 2; i <= 0; i--) {
          myonepairkicker[i] = myCards.get(i).getNum();
        }
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

    // ここからCPUの手札
    // ストレートの判定
    if (CPUCards.get(4).getNum() == CPUCards.get(3).getNum() + 1
        && CPUCards.get(3).getNum() == CPUCards.get(2).getNum() + 1
        && CPUCards.get(2).getNum() == CPUCards.get(1).getNum() + 1
        && CPUCards.get(1).getNum() == CPUCards.get(0).getNum() + 1) {
      cpuhand.setRoleid(6);
      cpuflag6 = 1;
    }
    // フラッシュの判定
    if (CPUCards.get(0).getCardtype() == CPUCards.get(1).getCardtype()
        && CPUCards.get(1).getCardtype() == CPUCards.get(2).getCardtype()
        && CPUCards.get(2).getCardtype() == CPUCards.get(3).getCardtype()
        && CPUCards.get(3).getCardtype() == CPUCards.get(4).getCardtype()) {
      cpuhand.setRoleid(5);
      cpuflag5 = 1;
    }
    // ロイヤルストレートフラッシュの判定
    if (cpuflag5 == 1 && CPUCards.get(0).getNum() == 1 && CPUCards.get(1).getNum() == 10
        && CPUCards.get(2).getNum() == 11 && CPUCards.get(3).getNum() == 12 && CPUCards.get(4).getNum() == 13) {
      cpuhand.setRoleid(1);
      cpuflag1 = 1;
    }
    // ストレートフラッシュの判定
    else if (cpuflag6 == 1 && cpuflag5 == 1) {
      cpuhand.setRoleid(2);
      cpuflag2 = 1;
    }

    // フォーカードの判定
    if ((CPUCards.get(0).getNum() == CPUCards.get(1).getNum() && CPUCards.get(1).getNum() == CPUCards.get(2).getNum()
        && CPUCards.get(2).getNum() == CPUCards.get(3).getNum())
        || (CPUCards.get(1).getNum() == CPUCards.get(2).getNum() && CPUCards.get(2).getNum() == CPUCards.get(3).getNum()
            && CPUCards.get(3).getNum() == CPUCards.get(4).getNum())) {
      cpuhand.setRoleid(3);
      cpuflag3 = 1;
    }
    // スリーカードの判定
    else if ((CPUCards.get(0).getNum() == CPUCards.get(1).getNum()
        && CPUCards.get(1).getNum() == CPUCards.get(2).getNum())
        || (CPUCards.get(1).getNum() == CPUCards.get(2).getNum()
            && CPUCards.get(2).getNum() == CPUCards.get(3).getNum())
        || (CPUCards.get(2).getNum() == CPUCards.get(3).getNum()
            && CPUCards.get(3).getNum() == CPUCards.get(4).getNum())) {
      cpuhand.setRoleid(7);
      cpuflag7 = 1;

      // フルハウスの判定
      if (CPUCards.get(0).getNum() == CPUCards.get(1).getNum()
          && CPUCards.get(1).getNum() == CPUCards.get(2).getNum()) {
        if (CPUCards.get(3).getNum() == CPUCards.get(4).getNum()) {
          cpuhand.setRoleid(4);
          cpuflag4 = 1;
        }
      } else if (CPUCards.get(2).getNum() == CPUCards.get(3).getNum()
          && CPUCards.get(3).getNum() == CPUCards.get(4).getNum()) {
        if (CPUCards.get(0).getNum() == CPUCards.get(1).getNum()) {
          cpuhand.setRoleid(4);
          cpuflag4 = 1;
        }
      }
    }
    // ツウ・ペアの判定
    else if ((CPUCards.get(0).getNum() == CPUCards.get(1).getNum()
        && CPUCards.get(2).getNum() == CPUCards.get(3).getNum())
        || (CPUCards.get(1).getNum() == CPUCards.get(2).getNum()
            && CPUCards.get(3).getNum() == CPUCards.get(4).getNum())
        || (CPUCards.get(0).getNum() == CPUCards.get(1).getNum()
            && CPUCards.get(3).getNum() == CPUCards.get(4).getNum())) {
      cpuhand.setRoleid(8);
      cpuflag8 = 1;
    }
    // ワン・ペアの判定
    else if ((CPUCards.get(0).getNum() == CPUCards.get(1).getNum())
        || (CPUCards.get(1).getNum() == CPUCards.get(2).getNum())
        || (CPUCards.get(2).getNum() == CPUCards.get(3).getNum())
        || (CPUCards.get(3).getNum() == CPUCards.get(4).getNum())) {
      cpuhand.setRoleid(9);
      cpuflag9 = 1;

      // ワンペア時の数値を格納
      if (CPUCards.get(0).getNum() == CPUCards.get(1).getNum()) {
        cpuonepairnum = CPUCards.get(1).getNum();
        for (int i = 2; i <= 0; i--) {
          cpuonepairkicker[i] = CPUCards.get(i + 2).getNum();
        }
      } else if (CPUCards.get(1).getNum() == CPUCards.get(2).getNum()) {
        cpuonepairnum = CPUCards.get(2).getNum();
        for (int i = 2; i < 0; i--) {
          cpuonepairkicker[i] = CPUCards.get(i + 2).getNum();
        }
        cpuonepairkicker[0] = CPUCards.get(0).getNum();
      } else if (CPUCards.get(2).getNum() == CPUCards.get(3).getNum()) {
        cpuonepairnum = CPUCards.get(3).getNum();
        myonepairkicker[2] = myCards.get(4).getNum();
        for (int i = 1; i <= 0; i--) {
          myonepairkicker[i] = myCards.get(i).getNum();
        }
      } else if (CPUCards.get(3).getNum() == CPUCards.get(4).getNum()) {
        cpuonepairnum = CPUCards.get(4).getNum();
        for (int i = 2; i <= 0; i--) {
          myonepairkicker[i] = myCards.get(i).getNum();
        }
      }
    }

    model.addAttribute("myresultflag", myresultflag);

    model.addAttribute("myonepairkicker0", myonepairkicker[0]);
    model.addAttribute("myonepairkicker1", myonepairkicker[1]);
    model.addAttribute("myonepairkicker2", myonepairkicker[2]);

    if (cpuflag1 == 1) {
      cpurole = "CPUの役はロイヤルストレートフラッシュです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 1;
    } else if (cpuflag2 == 1) {
      cpurole = "CPUの役はストレートフラッシュです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 2;
    } else if (cpuflag3 == 1) {
      cpurole = "CPUの役はフォア・カードです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 3;
    } else if (cpuflag4 == 1) {
      cpurole = "CPUの役はフルハウスです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 4;
    } else if (cpuflag5 == 1) {
      cpurole = "CPUの役はフラッシュです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 5;
    } else if (cpuflag6 == 1) {
      cpurole = "CPUの役はストレートです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 6;
    } else if (cpuflag7 == 1) {
      cpurole = "CPUの役はスリーカードです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 7;
    } else if (cpuflag8 == 1) {
      cpurole = "CPUの役はツウ・ペアです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 8;
    } else if (cpuflag9 == 1) {
      cpurole = "CPUの役はワン・ペアです。";
      model.addAttribute("cpurole", cpurole);
      cpuresultflag = 9;
    }

    // resultflagの大小関係で勝利者を判定
    if (myresultflag < cpuresultflag) {
      result = "あなたの勝利です!";
      model.addAttribute("result", result);
    } else if (myresultflag > cpuresultflag) {
      result = "CPUの勝利です...";
      model.addAttribute("result", result);
    }
    // ロイヤルストレートフラッシュ同士の比較
    else if (myresultflag == cpuresultflag && cpuresultflag == 1) {
      if (determinType(myCards, 4) < determinType(CPUCards, 4)) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (determinType(myCards, 4) > determinType(CPUCards, 4)) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      }
    }
    // フォア・カード同士の比較
    else if (myresultflag == cpuresultflag && cpuresultflag == 3) {
      if (myCards.get(3).getNum() > CPUCards.get(3).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(3).getNum() < CPUCards.get(3).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      }
    }
    // フルハウス同士の比較
    else if (myresultflag == cpuresultflag && cpuresultflag == 4) {
      if (myCards.get(2).getNum() > CPUCards.get(2).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(2).getNum() < CPUCards.get(2).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      }
    }
    // ストレートフラッシュ・フラッシュ・ストレート同士の比較
    else if ((myresultflag == cpuresultflag && cpuresultflag == 2)
        || (myresultflag == cpuresultflag && cpuresultflag == 5)
        || (myresultflag == cpuresultflag && cpuresultflag == 6)) {
      if (myCards.get(4).getNum() > CPUCards.get(4).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(4).getNum() < CPUCards.get(4).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      } else if (myCards.get(4).getNum() == CPUCards.get(4).getNum()) {
        if (determinType(myCards, 4) < determinType(CPUCards, 4)) {
          result = "あなたの勝利です!";
          model.addAttribute("result", result);
        } else if (determinType(myCards, 4) > determinType(CPUCards, 4)) {
          result = "CPUの勝利です...";
          model.addAttribute("result", result);
        }
      }
    }
    // スリーカード同士の比較
    else if (myresultflag == cpuresultflag && cpuresultflag == 7) {
      if (myCards.get(2).getNum() > CPUCards.get(2).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(2).getNum() < CPUCards.get(2).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      }
    }
    // ツウ・ペア同士の比較
    else if (myresultflag == cpuresultflag && cpuresultflag == 8) {
      if (myCards.get(3).getNum() > CPUCards.get(3).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(3).getNum() < CPUCards.get(3).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      } else if (myCards.get(3).getNum() == CPUCards.get(3).getNum()) {

      }
    }
    // ワン・ペア同士の比較
    else if (myresultflag == cpuresultflag && cpuresultflag == 9) {
      if (myonepairnum > cpuonepairnum) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myonepairnum < cpuonepairnum) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      } else if (myonepairnum == cpuonepairnum) {
        int i = 2;
        while (i <= 0) {
          if (myonepairkicker[i] > cpuonepairkicker[i]) {
            result = "あなたの勝利です!";
            model.addAttribute("result", result);
            break;
          } else if (myonepairkicker[i] < cpuonepairkicker[i]) {
            result = "CPUの勝利です...";
            model.addAttribute("result", result);
            break;
          }
          i--;
        }
      }
    }
    // ハイカード同士の比較
    else if (myresultflag == cpuresultflag && cpuresultflag == 10) {
      if (myCards.get(4).getNum() > CPUCards.get(4).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(4).getNum() < CPUCards.get(4).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      }
    }

    return "cpu_select";
  }

  @GetMapping("poker/call")
  public String showCall(ModelMap model, Principal prin) {
    int userid;
    int cpuid;
    String cpuname = "CPU";
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    userid = userMapper.selectid(loginUser);
    Hand hand = handMapper.selectByUserId(userid);

    PlayerIndex Playerindex = new PlayerIndex();
    Playerindex.getId();
    CPUIndex CPUindex = new CPUIndex();
    CPUindex.getId();

    myCards.add(cardsMapper.selectAllById(hand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(hand.getHand5id()));

    model.addAttribute("myCards", myCards);

    model.addAttribute("myindex", Playerindex);

    model.addAttribute("coin", hand.getTurn());

    ArrayList<Cards> CPUCards = new ArrayList<Cards>();
    cpuid = userMapper.selectid(cpuname);
    Hand CPUhand = handMapper.selectByUserId(cpuid);

    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand1id()));
    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand2id()));
    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand3id()));
    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand4id()));
    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand5id()));

    model.addAttribute("CPUCards", CPUCards);

    model.addAttribute("cpuindex", CPUindex);

    model.addAttribute("coin", hand.getTurn());

    return "cpu_poker.html";
  }

  @GetMapping("poker/drop")
  public String showDrop(ModelMap model, Principal prin) {
    int userid;
    int coin;
    int cpuid;

    PlayerIndex Playerindex = new PlayerIndex();
    Playerindex.getId();
    CPUIndex CPUindex = new CPUIndex();
    CPUindex.getId();

    String cpuname = "CPU";
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで
    String message = "ドロップしました";
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    userid = userMapper.selectid(loginUser);
    Hand userhand = handMapper.selectByUserId(userid);
    handMapper.updateIsActivefalsetotrueByfalseAndUserId(userid);
    coin = userhand.getTurn() - 1;
    userhand.setTurn(coin);

    myCards.add(cardsMapper.selectAllById(userhand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand5id()));

    handMapper.insertHandandIsActive(userhand);
    model.addAttribute("myCards", myCards);

    model.addAttribute("myindex", Playerindex);

    model.addAttribute("coin", userhand.getTurn());
    model.addAttribute("index", new index());

    model.addAttribute("message", message);

    ArrayList<Cards> CPUCards = new ArrayList<Cards>();
    cpuid = userMapper.selectid(cpuname);
    Hand CPUhand = handMapper.selectByUserId(cpuid);

    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand1id()));
    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand2id()));
    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand3id()));
    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand4id()));
    CPUCards.add(cardsMapper.selectAllById(CPUhand.getHand5id()));

    model.addAttribute("CPUCards", CPUCards);

    model.addAttribute("cpuindex", CPUindex);

    model.addAttribute("cpucoin", CPUhand.getTurn());
    model.addAttribute("index", new index());

    return "cpu_poker.html";
  }

  @GetMapping("poker/rays")
  public String rays(ModelMap model, Principal prin) {
    int id;
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    id = userMapper.selectid(loginUser);
    Hand hand = handMapper.selectByUserId(id);
    model.addAttribute("coin", hand.getTurn());
    return "cpu_rays.html";
  }

  @PostMapping("/rays")
  public String formRays(@RequestParam("rays") Integer rays, ModelMap model, Principal prin) {
    int userid;
    int cpuid;
    Hand userhand;
    Hand cpuhand;
    ArrayList<Cards> myCards = new ArrayList<Cards>();
    ArrayList<Cards> cpuCards = new ArrayList<Cards>();
    String cpuname = "CPU";
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);

    PlayerIndex Playerindex = new PlayerIndex();
    Playerindex.getId();
    CPUIndex CPUindex = new CPUIndex();
    CPUindex.getId();

    userid = userMapper.selectid(loginUser);
    userhand = handMapper.selectByUserId(userid);

    myCards.add(cardsMapper.selectAllById(userhand.getHand1id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand2id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand3id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand4id()));
    myCards.add(cardsMapper.selectAllById(userhand.getHand5id()));
    model.addAttribute("rays", rays);
    model.addAttribute("myCards", myCards);

    model.addAttribute("myindex", Playerindex);

    model.addAttribute("coin", userhand.getTurn());
    model.addAttribute("index", new index());

    cpuid = userMapper.selectid(cpuname);
    cpuhand = handMapper.selectByUserId(cpuid);

    cpuCards.add(cardsMapper.selectAllById(cpuhand.getHand1id()));
    cpuCards.add(cardsMapper.selectAllById(cpuhand.getHand2id()));
    cpuCards.add(cardsMapper.selectAllById(cpuhand.getHand3id()));
    cpuCards.add(cardsMapper.selectAllById(cpuhand.getHand4id()));
    cpuCards.add(cardsMapper.selectAllById(cpuhand.getHand5id()));

    model.addAttribute("CPUCards", cpuCards);

    model.addAttribute("cpuindex", CPUindex);

    model.addAttribute("coin", cpuhand.getTurn());
    model.addAttribute("index", new index());

    return "cpu_poker";
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
