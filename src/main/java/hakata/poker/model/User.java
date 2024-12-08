package hakata.poker.model;

public class User {
  int id;
  String userName;

  public User(int i, String name) {
    id = i;
    userName = name;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getId() {
    return id;
  }

  public String getUserName() {
    return userName;
  }

}
