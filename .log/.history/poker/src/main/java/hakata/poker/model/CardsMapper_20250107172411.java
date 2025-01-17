package hakata.poker.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface CardsMapper {
  @Select("SELECT * from cards")
  ArrayList<Cards> selectAll();

  @Select("SELECT * from cards where rid = #{rid}")
  ArrayList<Cards> selectAllByrid(int rid);

  @Select("SELECT * from cards where id = #{id}")
  Cards selectAllById(int id);

  // 1こランダムに選択
  @Select("SELECT * FROM cards ORDER BY RAND() LIMIT 1")
  Cards selectRandomCard();

  @Select("SELECT * FROM cards where rid = #{rid} ORDER BY RAND() LIMIT 1")
  Cards selectRandomCardByrid(int rid);

  // ５こランダムに選択
  @Select("SELECT * FROM cards ORDER BY RAND() LIMIT 5")
  ArrayList<Cards> select5RandomCard();

  @Select("SELECT * FROM cards where rid = #{rid} ORDER BY RAND() LIMIT 5")
  ArrayList<Cards> select5RandomCardByrid(int rid);

  @Update("UPDATE CARDS SET ISACTIVE=true WHERE id =#{id}")
  void updateisActiveTrueById(int id);

  @Update("UPDATE CARDS SET ISACTIVE=false WHERE id =#{id}")
  void updateisActiveFalseById(int id);

  // trueのカードをfalseに
  // 手札引き直すときのリセット処理
  @Update("UPDATE CARDS SET ISACTIVE=false WHERE isActive = true")
  void updateAllfalsetotrueByfalse();

  @Update("UPDATE CARDS SET ISACTIVE=true WHERE id =#{id} and rid = #{rid}")
  void updateisActiveTrueByIdAndrid(int id,int rid);

  @Update("UPDATE CARDS SET ISACTIVE=false WHERE id =#{id} and rid = #{rid}")
  void updateisActiveFalseByIdAndrid(int id,int rid);

  // trueのカードをfalseに
  // 手札引き直すときのリセット処理
  @Update("UPDATE CARDS SET ISACTIVE=false WHERE isActive = true and rid = #{rid}")
  void updateAllfalsetotrueByfalseAndrid(int rid);
  
  //CardsService.insertCards(int rid)を使うと52枚insertできます
  @Insert("INSERT INTO cards (rid,num,cardtype,isActive) VALUES (#{card.rid},#{card.num},#{card.cardtype},#{card.isActive});")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertCard(Cards card);
}