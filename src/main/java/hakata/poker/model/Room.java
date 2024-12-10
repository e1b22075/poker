package hakata.poker.model;

public class Room {
  int id;
  String roomName;
  int user1id;
  String user1Name;
  Boolean user1Status;
  int user2id;
  String user2Name;
  Boolean user2Status;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public Boolean getUser1Status() {
    return user1Status;
  }

  public int getUser1id() {
    return user1id;
  }

  public Boolean getUser2Status() {
    return user2Status;
  }

  public int getUser2id() {
    return user2id;
  }

  public String getUser1Name() {
    return user1Name;
  }

  public String getUser2Name() {
    return user2Name;
  }
  

  public void setUser1Status(Boolean user1Status) {
    this.user1Status = user1Status;
  }

  public void setUser1id(int user1id) {
    this.user1id = user1id;
  }

  public void setUser2Status(Boolean user2Status) {
    this.user2Status = user2Status;
  }

  public void setUser2id(int user2id) {
    this.user2id = user2id;
  }

  public void setUser1Name(String user1Name) {
    this.user1Name = user1Name;
  }

  public void setUser2Name(String user2Name) {
    this.user2Name = user2Name;
  }


}
