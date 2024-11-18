package hakata.poker.model;

public class card {

  int number;
  String type;

  public card(int num, String type) {
    this.number = num;
    this.type = type;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getNumber() {
    return number;
  }

  public String getType() {
    return type;
  }

}
