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

    myCards.sort(Comparator.comparingInt((Cards card) -> {
      int num = card.getNum();
      return num == 1 ? Integer.MAX_VALUE : num; // 1を最大値として扱う
    }));

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

    CPUCards.sort(Comparator.comparingInt((Cards card) -> {
      int num = card.getNum();
      return num == 1 ? Integer.MAX_VALUE : num; // 1を最大値として扱う
    }));

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

    int myflashflag = 0; // プレイヤーのフラッシュのフラグ
    int mystraightflag = 0; // プレイヤーのストレートのフラグ

    int cpuflashflag = 0; // CPUのフラッシュのフラグ
    int cpustraightflag = 0; // CPUのストレートのフラグ

    int myonepairkickernum = 0;
    int cpuonepairkickernum = 0;
    int myonepairkickerid = 0;
    int cpuonepairkickerid = 0;
    int mytwopairid = 0;
    int cputwopairid = 0;

    String myrole;
    String cpurole;

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

    myCards.sort(Comparator.comparingInt((Cards card) -> {
      int num = card.getNum();
      return num == 1 ? Integer.MAX_VALUE : num; // 1を最大値として扱う
    }));

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

    CPUCards.sort(Comparator.comparingInt((Cards card) -> {
      int num = card.getNum();
      return num == 1 ? Integer.MAX_VALUE : num; // 1を最大値として扱う
    }));

    cpuhand.setHand1id(CPUCards.get(0).getId());
    cpuhand.setHand2id(CPUCards.get(1).getId());
    cpuhand.setHand3id(CPUCards.get(2).getId());
    cpuhand.setHand4id(CPUCards.get(3).getId());
    cpuhand.setHand5id(CPUCards.get(4).getId());
    handMapper.insertHandandIsActive(cpuhand);
    model.addAttribute("CPUCards", CPUCards);

    model.addAttribute("cpuindex", new CPUIndex());

    model.addAttribute("coin", cpuhand.getTurn());

    userhand.setRoleid(10); //プレイヤーのRoleidをハイカードのid10に設定
    cpuhand.setRoleid(10); // CPUのRoleidをハイカードのid10に設定

    // ストレートの判定
    if ((myCards.get(4).getNum() == myCards.get(3).getNum() + 1 && myCards.get(3).getNum() == myCards.get(2).getNum() + 1 && myCards.get(2).getNum() == myCards.get(1).getNum() + 1 && myCards.get(1).getNum() == myCards.get(0).getNum() + 1) || (myCards.get(4).getNum() == 1 && myCards.get(3).getNum() == 13 && myCards.get(2).getNum() == 12 && myCards.get(1).getNum() == 11 && myCards.get(0).getNum() == 10)) {
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
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(2).getNum() == myCards.get(3).getNum()) || (myCards.get(1).getNum() == myCards.get(2).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())
        || (myCards.get(0).getNum() == myCards.get(1).getNum() && myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(8);

      // ペアになっていないカードの手札idを保存(カードの数値ではなく、手札の左から何番目にあるかの数字)
      if (myCards.get(0).getNum() == myCards.get(1).getNum()
          && myCards.get(2).getNum() == myCards.get(3).getNum()) {
        userhand.setRolenum(myCards.get(4).getNum());
        mytwopairid = 4;
      } else if (myCards.get(1).getNum() == myCards.get(2).getNum()
          && myCards.get(3).getNum() == myCards.get(4).getNum()) {
        userhand.setRolenum(myCards.get(0).getNum());
        mytwopairid = 0;
      } else if (myCards.get(0).getNum() == myCards.get(1).getNum()
          && myCards.get(3).getNum() == myCards.get(4).getNum()) {
        userhand.setRolenum(myCards.get(2).getNum());
        mytwopairid = 2;
      }
    }
    // ワン・ペアの判定
    else if ((myCards.get(0).getNum() == myCards.get(1).getNum()) || (myCards.get(1).getNum() == myCards.get(2).getNum()) || (myCards.get(2).getNum() == myCards.get(3).getNum())
        || (myCards.get(3).getNum() == myCards.get(4).getNum())) {
      userhand.setRoleid(9);

      // ワンペア時の数値を格納
      if (myCards.get(0).getNum() == myCards.get(1).getNum()) {
        userhand.setRolenum(myCards.get(1).getNum());
        myonepairkickernum = myCards.get(4).getNum();
        myonepairkickerid = 4;
      } else if (myCards.get(1).getNum() == myCards.get(2).getNum()) {
        userhand.setRolenum(myCards.get(2).getNum());
        myonepairkickernum = myCards.get(4).getNum();
        myonepairkickerid = 4;
      } else if (myCards.get(2).getNum() == myCards.get(3).getNum()) {
        userhand.setRolenum(myCards.get(3).getNum());
        myonepairkickernum = myCards.get(4).getNum();
        myonepairkickerid = 4;
      } else if (myCards.get(3).getNum() == myCards.get(4).getNum()) {
        userhand.setRolenum(myCards.get(4).getNum());
        myonepairkickernum = myCards.get(2).getNum();
        myonepairkickerid = 2;
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

    // ここからCPUの手札
    // ストレートの判定
    if ((CPUCards.get(4).getNum() == CPUCards.get(3).getNum() + 1
        && CPUCards.get(3).getNum() == CPUCards.get(2).getNum() + 1
        && CPUCards.get(2).getNum() == CPUCards.get(1).getNum() + 1
        && CPUCards.get(1).getNum() == CPUCards.get(0).getNum() + 1)
        || ((CPUCards.get(4).getNum() == 1 && CPUCards.get(3).getNum() == 13 && CPUCards.get(2).getNum() == 12 && CPUCards.get(1).getNum() == 11 && CPUCards.get(0).getNum() == 10))) {
      cpuhand.setRoleid(6);
    }
    // フラッシュの判定
    if (CPUCards.get(0).getCardtype() == CPUCards.get(1).getCardtype()
        && CPUCards.get(1).getCardtype() == CPUCards.get(2).getCardtype()
        && CPUCards.get(2).getCardtype() == CPUCards.get(3).getCardtype()
        && CPUCards.get(3).getCardtype() == CPUCards.get(4).getCardtype()) {
      cpuhand.setRoleid(5);
    }
    // ロイヤルストレートフラッシュの判定
    if (cpuflashflag == 1 && CPUCards.get(4).getNum() == 1 && CPUCards.get(0).getNum() == 10
        && CPUCards.get(1).getNum() == 11 && CPUCards.get(2).getNum() == 12 && CPUCards.get(3).getNum() == 13) {
      cpuhand.setRoleid(1);
    }
    // ストレートフラッシュの判定
    else if (cpustraightflag == 1 && cpuflashflag == 1) {
      cpuhand.setRoleid(2);
    }

    // フォア・カードの判定
    if ((CPUCards.get(0).getNum() == CPUCards.get(1).getNum() && CPUCards.get(1).getNum() == CPUCards.get(2).getNum()
        && CPUCards.get(2).getNum() == CPUCards.get(3).getNum())
        || (CPUCards.get(1).getNum() == CPUCards.get(2).getNum() && CPUCards.get(2).getNum() == CPUCards.get(3).getNum()
            && CPUCards.get(3).getNum() == CPUCards.get(4).getNum())) {
      cpuhand.setRoleid(3);
    }
    // スリーカードの判定
    else if ((CPUCards.get(0).getNum() == CPUCards.get(1).getNum()
        && CPUCards.get(1).getNum() == CPUCards.get(2).getNum())
        || (CPUCards.get(1).getNum() == CPUCards.get(2).getNum()
            && CPUCards.get(2).getNum() == CPUCards.get(3).getNum())
        || (CPUCards.get(2).getNum() == CPUCards.get(3).getNum()
            && CPUCards.get(3).getNum() == CPUCards.get(4).getNum())) {
      cpuhand.setRoleid(7);

      // フルハウスの判定
      if (CPUCards.get(0).getNum() == CPUCards.get(1).getNum()
          && CPUCards.get(1).getNum() == CPUCards.get(2).getNum()) {
        if (CPUCards.get(3).getNum() == CPUCards.get(4).getNum()) {
          cpuhand.setRoleid(4);
        }
      } else if (CPUCards.get(2).getNum() == CPUCards.get(3).getNum()
          && CPUCards.get(3).getNum() == CPUCards.get(4).getNum()) {
        if (CPUCards.get(0).getNum() == CPUCards.get(1).getNum()) {
          cpuhand.setRoleid(4);
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

      //ペアになっていないカードの手札idを保存(カードの数値ではなく、手札の左から何番目にあるかの数字)
      if(CPUCards.get(0).getNum() == CPUCards.get(1).getNum()
          && CPUCards.get(2).getNum() == CPUCards.get(3).getNum()) {
        cpuhand.setRolenum(CPUCards.get(4).getNum());
        cputwopairid = 4;
      } else if (CPUCards.get(1).getNum() == CPUCards.get(2).getNum()
          && CPUCards.get(3).getNum() == CPUCards.get(4).getNum()) {
        cpuhand.setRolenum(CPUCards.get(0).getNum());
        cputwopairid = 0;
      } else if (CPUCards.get(0).getNum() == CPUCards.get(1).getNum()
          && CPUCards.get(3).getNum() == CPUCards.get(4).getNum()) {
        cpuhand.setRolenum(CPUCards.get(2).getNum());
        cputwopairid = 2;
      }
    }
    // ワン・ペアの判定
    else if ((CPUCards.get(0).getNum() == CPUCards.get(1).getNum())
        || (CPUCards.get(1).getNum() == CPUCards.get(2).getNum())
        || (CPUCards.get(2).getNum() == CPUCards.get(3).getNum())
        || (CPUCards.get(3).getNum() == CPUCards.get(4).getNum())) {
      cpuhand.setRoleid(9);

      // ワンペア時の数値を格納
      if (CPUCards.get(0).getNum() == CPUCards.get(1).getNum()) {
        cpuhand.setRolenum(CPUCards.get(1).getNum());
        cpuonepairkickernum = myCards.get(4).getNum();
        cpuonepairkickerid = 4;
      } else if (CPUCards.get(1).getNum() == CPUCards.get(2).getNum()) {
        cpuhand.setRolenum(CPUCards.get(2).getNum());
        cpuonepairkickernum = myCards.get(4).getNum();
        cpuonepairkickerid = 4;
      } else if (CPUCards.get(2).getNum() == CPUCards.get(3).getNum()) {
        cpuhand.setRolenum(CPUCards.get(3).getNum());
        cpuonepairkickernum = myCards.get(4).getNum();
        cpuonepairkickerid = 4;
      } else if (CPUCards.get(3).getNum() == CPUCards.get(4).getNum()) {
        cpuhand.setRolenum(CPUCards.get(4).getNum());
        cpuonepairkickernum = myCards.get(2).getNum();
        cpuonepairkickerid = 2;
      }
    }

    if (cpuhand.getRoleid() == 1) {
      cpurole = "CPUの役はロイヤルストレートフラッシュです。";
      model.addAttribute("cpurole", cpurole);
    } else if (cpuhand.getRoleid() == 2) {
      cpurole = "CPUの役はストレートフラッシュです。";
      model.addAttribute("cpurole", cpurole);
    } else if (cpuhand.getRoleid() == 3) {
      cpurole = "CPUの役はフォア・カードです。";
      model.addAttribute("cpurole", cpurole);
    } else if (cpuhand.getRoleid() == 4) {
      cpurole = "CPUの役はフルハウスです。";
      model.addAttribute("cpurole", cpurole);
    } else if (cpuhand.getRoleid() == 5) {
      cpurole = "CPUの役はフラッシュです。";
      model.addAttribute("cpurole", cpurole);
    } else if (cpuhand.getRoleid() == 6) {
      cpurole = "CPUの役はストレートです。";
      model.addAttribute("cpurole", cpurole);
    } else if (cpuhand.getRoleid() == 7) {
      cpurole = "CPUの役はスリーカードです。";
      model.addAttribute("cpurole", cpurole);
    } else if (cpuhand.getRoleid() == 8) {
      cpurole = "CPUの役はツウ・ペアです。";
      model.addAttribute("cpurole", cpurole);
    } else if (cpuhand.getRoleid() == 9) {
      cpurole = "CPUの役はワン・ペアです。";
      model.addAttribute("cpurole", cpurole);
    }


    int a1 = myCards.get(4).getNum();
    int a2 = CPUCards.get(4).getNum();
    // 5枚目のカードが1の時、数値比較の都合上14にする
    if (myCards.get(4).getNum() == 1) {
      a1 = 14;
    }
    if (CPUCards.get(4).getNum() == 1) {
      a2 = 14;
    }

    //Roleidの大小関係で勝利者を判定
    if (userhand.getRoleid() < cpuhand.getRoleid()) {
      result = "あなたの勝利です!";
      model.addAttribute("result", result);
    } else if (userhand.getRoleid() > cpuhand.getRoleid()) {
      result = "CPUの勝利です...";
      model.addAttribute("result", result);
    }
    // ロイヤルストレートフラッシュ同士の比較
    else if (userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 1) {
      if (determinType(myCards, 4) < determinType(CPUCards, 4)) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (determinType(myCards, 4) > determinType(CPUCards, 4)) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      }
    }
    // フォア・カード同士の比較
    else if (userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 3) {
      if (myCards.get(3).getNum() > CPUCards.get(3).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(3).getNum() < CPUCards.get(3).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      }
    }
    // フルハウス・スリーカード同士の比較
    else if ((userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 4) || (userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 7)) {
      if (myCards.get(2).getNum() > CPUCards.get(2).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(2).getNum() < CPUCards.get(2).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      }
    }
    // ストレートフラッシュ・フラッシュ・ストレート同士の比較
    else if ((userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 2)
        || (userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 5)
        || (userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 6)) {
      if (a1 > a2) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (a1 < a2) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      } else if (a1 == a2) {
        if (determinType(myCards, 4) < determinType(CPUCards, 4)) {
          result = "あなたの勝利です!";
          model.addAttribute("result", result);
        } else if (determinType(myCards, 4) > determinType(CPUCards, 4)) {
          result = "CPUの勝利です...";
          model.addAttribute("result", result);
        }
      }
    }
    // ツウ・ペア同士の比較
    else if (userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 8) {
      if (myCards.get(3).getNum() > CPUCards.get(3).getNum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (myCards.get(3).getNum() < CPUCards.get(3).getNum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      } else if (myCards.get(3).getNum() == CPUCards.get(3).getNum()) {
          if (userhand.getRolenum() > cpuhand.getRolenum()) {
            result = "あなたの勝利です!";
            model.addAttribute("result", result);
          } else if (userhand.getRolenum() < cpuhand.getRolenum()) {
            result = "CPUの勝利です...";
            model.addAttribute("result", result);
          } else if (userhand.getRolenum() == cpuhand.getRolenum()) {
              if (determinType(myCards, mytwopairid) < determinType(CPUCards, cputwopairid)) {
                result = "あなたの勝利です!";
                model.addAttribute("result", result);
              } else if (determinType(myCards, mytwopairid) > determinType(CPUCards, cputwopairid)) {
                result = "CPUの勝利です...";
                model.addAttribute("result", result);
              }
          }
      }
    }
    // ワン・ペア同士の比較
    else if (userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 9) {
      if (userhand.getRolenum() > cpuhand.getRolenum()) {
        result = "あなたの勝利です!";
        model.addAttribute("result", result);
      } else if (userhand.getRolenum() < cpuhand.getRolenum()) {
        result = "CPUの勝利です...";
        model.addAttribute("result", result);
      } else if (userhand.getRolenum() == cpuhand.getRolenum()) {
          if (myonepairkickernum > cpuonepairkickernum) {
            result = "あなたの勝利です!";
            model.addAttribute("result", result);
          } else if (myonepairkickernum < cpuonepairkickernum) {
            result = "CPUの勝利です...";
            model.addAttribute("result", result);
          } else if (myonepairkickernum ==  cpuonepairkickernum) {
              if (determinType(myCards, myonepairkickerid) < determinType(CPUCards, cpuonepairkickerid)) {
                result = "あなたの勝利です!";
                model.addAttribute("result", result);
              } else if (determinType(myCards, myonepairkickerid) > determinType(CPUCards, cpuonepairkickerid)) {
                result = "CPUの勝利です...";
                model.addAttribute("result", result);
              }
          }
      }
    }
    // ハイカード同士の比較
    else if (userhand.getRoleid() == cpuhand.getRoleid() && cpuhand.getRoleid() == 10) {
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
