package hakata.poker.model;

public class Cards {
  int id;
  int rid;
  int num;
  String cardtype;
  boolean isActive;
  public String getCardtype() {
    return cardtype;
  }

  public int getId() {
    return id;
  }
  public int getRid() {
      return rid;
  }

  public int getNum() {
    return num;
  }

  public boolean getActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public void setCardtype(String cardtype) {
    this.cardtype = cardtype;
  }

  public void setId(int id) {
    this.id = id;
  }
  public void setRid(int rid) {
      this.rid = rid;
  }

  public void setNum(int num) {
    this.num = num;
  }
}