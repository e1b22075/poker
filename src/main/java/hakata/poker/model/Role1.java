package hakata.poker.model;

import java.util.ArrayList;

public class Role1 {
  public Role Role1check(ArrayList<card> card_List) {
    boolean flag2 = false;
    boolean flag3 = false;
    int i;
    int count = 1;
    int max = 0;
    int num = 0;
    String role;
    ArrayList<Boolean> checker = new ArrayList<Boolean>();

    for (i = 0; i < card_List.size(); i++) {
      checker.add(true);
    }

    for (i = 0; i < card_List.size(); i++) {
      if (checker.get(i)) {
        for (int j = i; j < card_List.size(); j++) {
          if (card_List.get(i).number == card_List.get(j).number) {
            count++;
            num = card_List.get(i).number;
            checker.set(j, false);
          }
        }
      }
      switch (count) {
        case 2:
          if (flag2) {
            role = "ツーペア";
            flag2 = false;
          } else {
            flag2 = true;
          }
          break;
        case 3:
          flag3 = true;
          break;
        case 4:
          role = "フォーカード";
          break;
        default:
          break;
      }

      if (card_List.get(i).number > max) {
        max = card_List.get(i).number;
      }
    }
    if (flag2 && flag3) {
      role = "フルハウス";
    } else {
      if (flag2) {
        role = "ワンペア";
      } else if (flag3) {
        role = "スリーカード";
      } else {
        role = "ハイカード";
        num = max;
      }
    }

    Role role1 = new Role(num, role);

    return role1;
  }
}

class Role {

  String role;
  int number;

  public Role(int num, String role) {
    this.number = num;
    this.role = role;
  }

  public void setrole(String role) {
    this.role = role;
  }

  public void setNumber(int num) {
    this.number = num;
  }

  public String getrole() {
    return role;
  }

  public int getNumber() {
    return number;
  }
}
