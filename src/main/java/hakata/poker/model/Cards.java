package poker.model;

public class Cards {
  int id;
  int num;
  String cardtype;
  boolean isActive;

  public String getCardtype() {
    return cardtype;
  }

  public int getId() {
    return id;
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

  public void setNum(int num) {
    this.num = num;
  }
}
