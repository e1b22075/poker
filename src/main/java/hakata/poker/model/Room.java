package hakata.poker.model;

public class Room {
  int id;
  String roomName;
  int user1id;
  int user2id;

  public int getId() {
    return id;
  }
  public String getRoomName() {
    return roomName;
  }

  public int getUser1id() {
    return user1id;
  }

  public int getUser2id() {
    return user2id;
  }

  public void setId(int id) {
    this.id = id;
  }
  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public void setUser1id(int user1id) {
    this.user1id = user1id;
  }

  public void setUser2id(int user2id) {
    this.user2id = user2id;
  }
}
