package hakata.poker.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
  @Select("SELECT * from users")
  ArrayList<User> selectAll();

  @Select("SELECT * from users where id = #{id}")
  User selectAllById(int id);

  @Select("SELECT * from users where userName =#{userName}")
  User selectAllByName(String userName);

  @Select("SELECT id from users WHERE userName = #{name}")
  Integer selectid(String name);

  @Select("SELECT userName from Users WHERE userid = #{id}")
  String selectUserName(int id);

<
  @Select("SELECT * from Users WHERE username = #{username}")
  User findByUsername(String username);

  @Update("UPDATE users SET userName=#{newuserName} WHERE id=#{userid}")
  void updateUserName(int userid, String newuserName);

  @Insert("INSERT INTO users (userName) VALUES (#{userName});")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertUser(User user);
}
