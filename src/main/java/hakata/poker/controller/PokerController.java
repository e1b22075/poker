package hakata.poker.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Comparator;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;

import hakata.poker.model.card;

@Controller
public class PokerController {

  Random rmd = new Random();

  @GetMapping("poker")
  public String login(ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログインユーザ情報
    model.addAttribute("login_user", loginUser);
    return "poker.html";
  }

  @GetMapping("poker/card")
  public String showCard(ModelMap model, Principal prin) {
    int num = 0;// カードの値用
    String types = "";// マーク用
    int i = 0;// ループ用
    // ログインユーザ情報の受け渡し
    String loginUser = prin.getName();
    model.addAttribute("login_user", loginUser);
    // ここまで

    // 手札を決めるための処理。今回は適当

    ArrayList<Boolean> validCards = new ArrayList<Boolean>();// これサンプル用。DBできてないから作ってる。カードの重複を防ぐ
    for (int j = 0; j < 52; j++) {
      validCards.add(false);
    }

    ArrayList<card> cardList = new ArrayList<>();
    // これはカード格納用の変数(モデル含め仮組)

    ArrayList<card> cpu_CardList = new ArrayList<>();
    // これはCPUのカード格納用の変数(モデル含め仮組)

    while (i < 5) {
      // ここからの処理は、カードの値を正常にする処理とマークを判別してる。
      /*
       * これからの数値の基準はこんな感じ
       * 0~12 ♥
       * 13~25 ♦
       * 26~38 ♠
       * 39~51 ♣
       */
      num = rmd.nextInt(52);
      if (!validCards.get(num)) {
        validCards.set(num, true);
        if (num <= 12) {
          num += 1;
          types = "♥";
        } else if (num <= 25) {
          num -= 12;
          types = "♦";
        } else if (num <= 38) {
          num -= 25;
          types = "♠";
        } else if (num <= 51) {
          num -= 38;
          types = "♣";
        }

        cardList.add(new card(num, types));
        i++;
      }
    }
    cardList.sort(Comparator.comparing(card::getNumber));
    i = 0;
    while (i < 5) {
      // ここからの処理は、CPUのカードの値を正常にする処理とマークを判別してる。

      num = rmd.nextInt(52);
      if (!validCards.get(num)) {
        validCards.set(num, true);
        if (num <= 12) {
          num += 1;
          types = "♥";
        } else if (num <= 25) {
          num -= 12;
          types = "♦";
        } else if (num <= 38) {
          num -= 25;
          types = "♠";
        } else if (num <= 51) {
          num -= 38;
          types = "♣";
        }
        cpu_CardList.add(new card(num, types));
        i++;
      }
    }
    cpu_CardList.sort(Comparator.comparing(card::getNumber));

    model.addAttribute("cardList", cardList);
    model.addAttribute("cpu_CardList", cpu_CardList);
    return "poker.html";
  }

}
