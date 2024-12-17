package hakata.poker.model;

public class PlayerIndex {
  private Integer[] id1; // プロパティ名を "id" に変更

  public void setId(Integer[] id) {
    this.id1 = id;
  }

  public Integer[] getId() {
    return id1;
  }
}
