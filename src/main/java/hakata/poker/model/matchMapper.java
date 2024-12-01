package hakata.poker.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface matchMapper {

  @Select("SELECT * from match where (user1id = #{id} or user2id = #{id}) AND isActive = true")
  ArrayList<Match> selectAllById(int id);

  @Insert("INSERT INTO match (user1id,user2id,user1coin,user2coin,bet,isActive) VALUES (#{user1id},#{user2id},#{user1coin},#{user2coin},#{bet},true);")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertMatchandIsActive(Match match);
}
