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

  @Select("SELECT id from users WHERE userName = #{name}")
  Integer selectid(String name);

  @Select("SELECT userName from Users WHERE userid = #{id}")
  String selectUserName(int id);

  @Select("SELECT * from Users WHERE username = #{username}")
  User findByUsername(String username);
}
