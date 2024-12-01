package hakata.poker.model;

public class Fruit {
  int id;
  String name;
  int price;

  /**
   * 引数ありコンストラクタを作る際，Beanであれば必ずデフォルトコンストラクタを明示的に追加すること
   */
  public Fruit() {

  }

  public Fruit(int id2, String name2, int price2) {
    this.id = id2;
    this.name = name2;
    this.price = price2;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

}
