package hakata.poker.model;

public class Match {
  int id;
  int user1id;
  int user2id;
  int user1coin;
  int user2coin;
  int bet;
  boolean isActive;

  public void setId(int id) {
    this.id = id;
  }

  public void setUser1id(int user1id) {
    this.user1id = user1id;
  }

  public void setUser2id(int user2id) {
    this.user2id = user2id;
  }

  public void setUser1coin(int user1coin) {
    this.user1coin = user1coin;
  }

  public void setUser2coin(int user2coin) {
    this.user2coin = user2coin;
  }

  public void setBet(int bet) {
    this.bet = bet;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public int getUser1id() {
    return user1id;
  }

  public int getUser2id() {
    return user2id;
  }

  public int getUser1coin() {
    return user1coin;
  }

  public int getUser2coin() {
    return user2coin;
  }

  public int getBet() {
    return bet;
  }

  public boolean isActive() {
    return isActive;
  }

}
