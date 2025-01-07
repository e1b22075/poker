package hakata.poker.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import hakata.poker.model.Cards;
import hakata.poker.model.CardsMapper;


@Service
public class CardsService {

    @Autowired
    private CardsMapper cMapper;

    @Transactional
    public void insertCards(int rid) {
        String[] types = {"heart", "spade", "dia", "clover"};
        for (String type : types) {
            for (int i = 1; i <= 13; i++) {
                Cards card = new Cards();
                card.setRid(rid);
                card.setNum(i);
                card.setCardtype(type);
                card.setActive(false);
                cMapper.insertCard(card);
            }
        }
    }
}
