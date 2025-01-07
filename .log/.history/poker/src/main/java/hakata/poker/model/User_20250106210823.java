package hakata.poker.model;

public class User {
  int id;
  String userName;
  String password_hash;

  public User(int id, String userName, String password_hash) {
    this.id = id;
    this.userName = userName;
    this.password_hash = password_hash;
  }

  public User(String name, String pass) {
    this.userName = name;
    this.password_hash = pass;
  }

  public String getPassword_hash() {
    return password_hash;
  }

  public void setPassword_hash(String password_hash) {
    this.password_hash = password_hash;
  }

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
