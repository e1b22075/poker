package hakata.poker.model;

public class match {
  int id;
  int user1id;
  int user2id;
  int user1coin;
  int user2coin;
  int rid;
  int round;

  public void setUser1hand(int user1hand) {
    this.user1hand = user1hand;
  }

  public void setUser2hand(int user2hand) {
    this.user2hand = user2hand;
  }

  int user1hand;
  int user2hand;

  public int getUser1hand() {
    return user1hand;
  }

  public int getUser2hand() {
    return user2hand;
  }

  public match() {
  }

  public void setUser1state(String user1state) {
    this.user1state = user1state;
  }

  public void setUser2state(String user2state) {
    this.user2state = user2state;
  }

  int bet;

  String user1state;
  String user2state;

  public int getId() {
    return id;
  }

  public String getUser1state() {
    return user1state;
  }

  public String getUser2state() {
    return user2state;
  }

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

  boolean isActive;

  public void setRound(int round) {
    this.round = round;
  }

  public int getRound() {
    return round;
  }

  public void setRid(int rid) {
    this.rid = rid;
  }

  public int getRid() {
    return rid;
  }

}
