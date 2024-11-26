import java.util.Arrays;
import java.util.Random;

public class poker {
  public static void main(String[] args) {
    // ランダム関数でidを引くための配列
    int[] id = new int[52];

    // カード情報の配列(記号, 数字, idなど)
    int[][] card = new int[52][3];

    for (int i = 0; i < 52; i++) {
      id[i] = i + 1;
    }

    // カードの数値を入れてる
    // 記号も入れてる ♠ = 1, ♥ = 2, ♦ = 3, ♣ = 4
    int j = 0;
    int k = 0;
    for (int i = 0; i < 52; i++) {
      if (i % 13 == 0) {
        j = 0;
        k++;
      }
      card[i][0] = i + 1; // カードのid
      card[i][1] = j + 1; // カードの数字
      card[i][2] = k; // カードの記号
      j++;
    }

    // プレイヤー1とプレイヤー2の手札を取得
    int[][] player1Hand = drawCards(id, card);
    int[][] player2Hand = drawCards(id, card);

    for (int i = 0; i < 5; i++) {
      player1Hand[i][2] = 1;
      player2Hand[i][2] = 2;
    }

    // 手札の表示
    System.out.println("プレイヤー1の手札:");
    displayHand(player1Hand);

    System.out.println("プレイヤー2の手札:");
    displayHand(player2Hand);

    int player1flag1 = 0; // ロイヤルストレートフラッシュ
    int player1flag2 = 0; // ストレートフラッシュ
    int player1flag3 = 0; // フォア・カード
    int player1flag4 = 0; // フルハウス
    int player1flag5 = 0; // フラッシュ
    int player1flag6 = 0; // ストレート
    int player1flag7 = 0; // スリーカード
    int player1flag8 = 0; // ツウ・ペア
    int player1flag9 = 0; // ワン・ペア

    int one1 = 0; // ワン・ペア時の数字を格納

    int player2flag1 = 0;
    int player2flag2 = 0;
    int player2flag3 = 0;
    int player2flag4 = 0;
    int player2flag5 = 0;
    int player2flag6 = 0;
    int player2flag7 = 0;
    int player2flag8 = 0;
    int player2flag9 = 0;

    int one2 = 0; // ワン・ペア時の数字を格納

    int resultflag1 = 10;
    int resultflag2 = 10;

    // ストレートの判定
    if (player1Hand[4][1] == player1Hand[3][1] + 1 && player1Hand[3][1] == player1Hand[2][1] + 1
        && player1Hand[2][1] == player1Hand[1][1] + 1 && player1Hand[1][1] == player1Hand[0][1] + 1) {
      player1flag6 = 1;
    }
    // フラッシュの判定
    if (player1Hand[0][2] == player1Hand[1][2] && player1Hand[1][2] == player1Hand[2][2]
        && player1Hand[2][2] == player1Hand[3][2] && player1Hand[3][2] == player1Hand[4][2]) {
      player1flag5 = 1;
    }

    // ロイヤルストレートフラッシュの判定
    if (player1flag5 == 1 && player1Hand[0][1] == 1 && player1Hand[1][1] == 10 && player1Hand[2][1] == 11
        && player1Hand[3][1] == 12 && player1Hand[4][1] == 13) {
      player1flag1 = 1;
    }
    // ストレートフラッシュの判定
    else if (player1flag6 == 1 && player1flag5 == 1) {
      player1flag2 = 1;
    }

    // フォーカードの判定
    if ((player1Hand[0][1] == player1Hand[1][1] && player1Hand[1][1] == player1Hand[2][1]
        && player1Hand[2][1] == player1Hand[3][1])
        || (player1Hand[1][1] == player1Hand[2][1] && player1Hand[2][1] == player1Hand[3][1]
            && player1Hand[3][1] == player1Hand[4][1])) {
      player1flag3 = 1;
    }
    // スリーカードの判定
    else if ((player1Hand[0][1] == player1Hand[1][1] && player1Hand[1][1] == player1Hand[2][1])
        || (player1Hand[1][1] == player1Hand[2][1] && player1Hand[2][1] == player1Hand[3][1])
        || (player1Hand[2][1] == player1Hand[3][1] && player1Hand[3][1] == player1Hand[4][1])) {
      player1flag7 = 1;
      // フルハウス用の処理
      if (player1Hand[0][1] == player1Hand[1][1] && player1Hand[1][1] == player1Hand[2][1]) {
        if (player1Hand[3][1] == player1Hand[4][1]) {
          player1flag4 = 1;
        }
      } else if (player1Hand[2][1] == player1Hand[3][1] && player1Hand[3][1] == player1Hand[4][1]) {
        if (player1Hand[0][1] == player1Hand[1][1]) {
          player1flag4 = 1;
        }
      }
    }
    // ツウ・ペアの判定
    else if ((player1Hand[0][1] == player1Hand[1][1] && player1Hand[2][1] == player1Hand[3][1])
        || (player1Hand[1][1] == player1Hand[2][1] && player1Hand[3][1] == player1Hand[4][1])) {
      player1flag8 = 1;
    }
    // ワン・ペアの判定
    else if ((player1Hand[0][1] == player1Hand[1][1]) || (player1Hand[1][1] == player1Hand[2][1])
        || (player1Hand[2][1] == player1Hand[3][1]) || (player1Hand[3][1] == player1Hand[4][1])) {
      player1flag9 = 1;
      if (player1Hand[0][1] == player1Hand[1][1]) {
        one1 = player1Hand[1][1];
      } else if (player1Hand[1][1] == player1Hand[2][1]) {
        one1 = player1Hand[2][1];
      } else if (player1Hand[2][1] == player1Hand[3][1]) {
        one1 = player1Hand[3][1];
      } else if (player1Hand[3][1] == player1Hand[4][1]) {
        one1 = player1Hand[4][1];
      }
    }

    if (player1flag1 == 1) {
      System.out.println("プレイヤー1はロイヤルストレートフラッシュです");
      resultflag1 = 1;
    } else if (player1flag2 == 1) {
      System.out.println("プレイヤー1はストレートフラッシュです。");
      resultflag1 = 2;
    } else if (player1flag3 == 1) {
      System.out.println("プレイヤー1はフォア・カードです。");
      resultflag1 = 3;
    } else if (player1flag4 == 1) {
      System.out.println("プレイヤー1はフルハウスです。");
      resultflag1 = 4;
    } else if (player1flag5 == 1) {
      System.out.println("プレイヤー1はフラッシュです。");
      resultflag1 = 5;
    } else if (player1flag6 == 1) {
      System.out.println("プレイヤー1はストレートです。");
      resultflag1 = 6;
    } else if (player1flag7 == 1) {
      System.out.println("プレイヤー1はスリーカードです。");
      resultflag1 = 7;
    } else if (player1flag8 == 1) {
      System.out.println("プレイヤー1はツウ・ペアです。");
      resultflag1 = 8;
    } else if (player1flag9 == 1) {
      System.out.println("プレイヤー1はワン・ペアです。");
      resultflag1 = 9;
    }

    // ストレートの判定
    if (player2Hand[4][1] == player2Hand[3][1] + 1 && player2Hand[3][1] == player2Hand[2][1] + 1
        && player2Hand[2][1] == player2Hand[1][1] + 1 && player2Hand[1][1] == player2Hand[0][1] + 1) {
      player2flag6 = 1;
    }
    // フラッシュの判定
    if (player2Hand[0][2] == player2Hand[1][2] && player2Hand[1][2] == player2Hand[2][2]
        && player2Hand[2][2] == player2Hand[3][2] && player2Hand[3][2] == player2Hand[4][2]) {
      player2flag5 = 1;
    }

    // ロイヤルストレートフラッシュの判定
    if (player2flag5 == 1 && player2Hand[0][1] == 1 && player2Hand[1][1] == 10 && player2Hand[2][1] == 11
        && player2Hand[3][1] == 12 && player2Hand[4][1] == 13) {
      player2flag1 = 1;
    }
    // ストレートフラッシュの判定
    else if (player2flag6 == 1 && player2flag5 == 1) {
      player2flag2 = 1;
    }

    // フォーカードの判定
    if ((player2Hand[0][1] == player2Hand[1][1] && player2Hand[1][1] == player2Hand[2][1]
        && player2Hand[2][1] == player2Hand[3][1])
        || (player2Hand[1][1] == player2Hand[2][1] && player2Hand[2][1] == player2Hand[3][1]
            && player2Hand[3][1] == player2Hand[4][1])) {
      player2flag3 = 1;
    }
    // スリーカードの判定
    else if ((player2Hand[0][1] == player2Hand[1][1] && player2Hand[1][1] == player2Hand[2][1])
        || (player2Hand[1][1] == player2Hand[2][1] && player2Hand[2][1] == player2Hand[3][1])
        || (player2Hand[2][1] == player2Hand[3][1] && player2Hand[3][1] == player2Hand[4][1])) {
      player2flag7 = 1;
      // フルハウスの判定
      if (player2Hand[0][1] == player2Hand[1][1] && player2Hand[1][1] == player2Hand[2][1]) {
        if (player2Hand[3][1] == player2Hand[4][1]) {
          player2flag4 = 1;
        }
      } else if (player2Hand[2][1] == player2Hand[3][1] && player2Hand[3][1] == player2Hand[4][1]) {
        if (player2Hand[0][1] == player2Hand[1][1]) {
          player2flag4 = 1;
        }
      }
    }
    // ツウ・ペアの判定
    else if ((player2Hand[0][1] == player2Hand[1][1] && player2Hand[2][1] == player2Hand[3][1])
        || (player2Hand[1][1] == player2Hand[2][1] && player2Hand[3][1] == player2Hand[4][1])) {
      player2flag8 = 1;
    }
    // ワン・ペアの判定
    else if ((player2Hand[0][1] == player2Hand[1][1]) || (player2Hand[1][1] == player2Hand[2][1])
        || (player2Hand[2][1] == player2Hand[3][1]) || (player2Hand[3][1] == player2Hand[4][1])) {
      player2flag9 = 1;
      if (player2Hand[0][1] == player2Hand[1][1]) {
        one2 = player2Hand[1][1];
      } else if (player2Hand[1][1] == player2Hand[2][1]) {
        one2 = player2Hand[2][1];
      } else if (player2Hand[2][1] == player2Hand[3][1]) {
        one2 = player1Hand[3][1];
      } else if (player2Hand[3][1] == player2Hand[4][1]) {
        one2 = player2Hand[4][1];
      }
    }

    if (player2flag1 == 1) {
      System.out.println("プレイヤー2はロイヤルストレートフラッシュです");
      resultflag2 = 1;
    } else if (player2flag2 == 1) {
      System.out.println("プレイヤー2はストレートフラッシュです。");
      resultflag2 = 2;
    } else if (player2flag3 == 1) {
      System.out.println("プレイヤー2はフォア・カードです。");
      resultflag2 = 3;
    } else if (player2flag4 == 1) {
      System.out.println("プレイヤー2はフルハウスです。");
      resultflag2 = 4;
    } else if (player2flag5 == 1) {
      System.out.println("プレイヤー2はフラッシュです。");
      resultflag2 = 5;
    } else if (player2flag6 == 1) {
      System.out.println("プレイヤー2はストレートです。");
      resultflag2 = 6;
    } else if (player2flag7 == 1) {
      System.out.println("プレイヤー2はスリーカードです。");
      resultflag2 = 7;
    } else if (player2flag8 == 1) {
      System.out.println("プレイヤー2はツウ・ペアです。");
      resultflag2 = 8;
    } else if (player2flag9 == 1) {
      System.out.println("プレイヤー2はワン・ペアです。");
      resultflag2 = 9;
    }

    // resultflagの大小で勝利者を判定
    if (resultflag1 < resultflag2) {
      System.out.println("プレイヤー1の勝利です。");
    } else if (resultflag1 > resultflag2) {
      System.out.println("プレイヤー2の勝利です。");
    }
    // ロイヤルストレートフラッシュ同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 1) {
      if (player1Hand[4][2] < player2Hand[4][2]) {
        System.out.println("プレイヤー1の勝利です。");
      } else if (player1Hand[4][2] > player2Hand[4][2]) {
        System.out.println("プレイヤー2の勝利です。");
      }
    }
    // ストレートフラッシュ同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 2) {
      if (player1Hand[4][1] > player2Hand[4][1]) {
        System.out.println("プレイヤー1の勝利です。");
      } else if (player1Hand[4][1] < player2Hand[4][1]) {
        System.out.println("プレイヤー2の勝利です。");
      } else if (player1Hand[4][1] == player2Hand[4][1]) {
        if (player1Hand[4][2] < player2Hand[4][2]) {
          System.out.println("プレイヤー1の勝利です");
        } else if (player1Hand[4][2] > player2Hand[4][2]) {
          System.out.println("プレイヤー2の勝利です");
        }
      }
    }
    // フラッシュ同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 5) {
      if (player1Hand[4][2] < player2Hand[4][2]) {
        System.out.println("プレイヤー1の勝利です");
      } else if (player1Hand[4][2] > player2Hand[4][2]) {
        System.out.println("プレイヤー2の勝利です");
      }
    }
    // ストレート同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 6) {
      if (player1Hand[4][1] > player2Hand[4][1]) {
        System.out.println("プレイヤー1の勝利です。");
      } else if (player1Hand[4][1] < player2Hand[4][1]) {
        System.out.println("プレイヤー2の勝利です。");
      } else if (player1Hand[4][1] == player2Hand[4][1]) {
        if (player1Hand[4][2] < player2Hand[4][2]) {
          System.out.println("プレイヤー1の勝利です");
        } else if (player1Hand[4][2] > player2Hand[4][2]) {
          System.out.println("プレイヤー2の勝利です");
        }
      }
    }
    // フルハウスの同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 4) {
      if (player1Hand[2][1] > player2Hand[2][1]) {
        System.out.println("プレイヤー1の勝利です。");
      } else if (player1Hand[2][1] < player2Hand[2][1]) {
        System.out.println("プレイヤー2の勝利です。");
      }
    }
    // フォア・カード同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 3) {
      if (player1Hand[3][1] > player2Hand[3][1]) {
        System.out.println("プレイヤー1の勝利です");
      } else if (player1Hand[3][1] < player2Hand[3][1]) {
        System.out.println("プレイヤー2の勝利です");
      }
    }
    // スリーカード同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 7) {
      if (player1Hand[2][1] > player2Hand[2][1]) {
        System.out.println("プレイヤー1の勝利です");
      } else if (player1Hand[2][1] < player2Hand[2][1]) {
        System.out.println("プレイヤー2の勝利です");
      }
    }
    // ツウ・ペア同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 8) {
      if (player1Hand[3][1] > player2Hand[3][1]) {
        System.out.println("プレイヤー1の勝利です");
      } else if (player1Hand[3][1] < player2Hand[3][1]) {
        System.out.println("プレイヤー2の勝利です");
      }
    }
    // ワン・ペア同士の比較
    else if (resultflag1 == resultflag2 && resultflag2 == 9) {
      if (one1 > one2) {
        System.out.println("プレイヤー1の勝利です");
      } else if (one1 < one2) {
        System.out.println("プレイヤー2の勝利です");
      }
    }
  }

  // ランダムにカードを5枚引き、昇順に並び替えるメソッド
  public static int[][] drawCards(int[] id, int[][] card) {
    boolean[] selected = new boolean[52]; // 選ばれたカードの記録
    int[][] handCards = new int[5][3]; // 手札の情報を格納する配列

    int count = 0;
    while (count < 5) {
      int randomId = getRandomNumber(id); // ランダムにカードIDを取得
      if (!selected[randomId - 1]) { // 重複していなければ選択
        selected[randomId - 1] = true;
        handCards[count] = card[randomId - 1]; // 手札にカード情報を格納
        count++;
      }
    }

    // 手札を数字 (handCards[i][1]) の昇順にソート
    Arrays.sort(handCards, (card1, card2) -> Integer.compare(card1[1], card2[1]));
    return handCards;
  }

  // 手札を表示するメソッド
  public static void displayHand(int[][] handCards) {
    for (int[] card : handCards) {
      System.out.println(String.format(
          "id: %2d 数字: %2d 記号: %d",
          card[0], card[1], card[2]));
    }
  }

  public static int getRandomNumber(int[] array) {
    Random random = new Random();
    int index = random.nextInt(array.length); // 配列の長さに基づいてランダムなインデックスを生成
    return array[index];
  }

  public static String getRandomSymbol(String[] array) {
    Random random = new Random();
    int index = random.nextInt(array.length); // 配列の長さに基づいてランダムなインデックスを生成
    return array[index];
  }

}
