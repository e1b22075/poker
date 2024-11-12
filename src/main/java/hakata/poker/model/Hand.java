package hakata.poker.model;

public class Hand {
  int id;
  int userid;
  int hand1id;
  int hand2id;
  int hand3id;
  int hand4id;
  int hand5id;
  boolean isActive;

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public void setHand1id(int hand1id) {
    this.hand1id = hand1id;
  }

  public void setHand2id(int hand2id) {
    this.hand2id = hand2id;
  }

  public void setHand3id(int hand3id) {
    this.hand3id = hand3id;
  }

  public void setHand4id(int hand4id) {
    this.hand4id = hand4id;
  }

  public void setHand5id(int hand5id) {
    this.hand5id = hand5id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setUserid(int userid) {
    this.userid = userid;
  }

  public int getHand1id() {
    return hand1id;
  }

  public int getHand2id() {
    return hand2id;
  }

  public int getHand3id() {
    return hand3id;
  }

  public int getHand4id() {
    return hand4id;
  }

  public int getHand5id() {
    return hand5id;
  }

  public int getId() {
    return id;
  }

  public int getUserid() {
    return userid;
  }
}
