package com.project.notenote.date;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.notenote.card.Card;
import com.project.notenote.card.CardNotFoundException;
import com.project.notenote.card.CardRepository;
@Service
public class DateService {
    @Autowired
    DateRepository dateRepository;

    @Autowired
    CardRepository cardRepository;


    public Date saveDate(DateDTORequest request){
        Date date = new Date(
                            request.getStartDate(),
                            request.getEndDate()
                        );

        return dateRepository.save(date);
    }

    public Date getDateById(Long dateId){
        if (dateId == null){
            return null;
        }
        return dateRepository.findById(dateId).orElseThrow(() -> new DateNotFoundException(dateId));
    }

    public List<Date> getAllDate(){
        List<Date> dates = (List<Date>) dateRepository.findAll();
        return dates;
    }

    public Date deleteDateByID(Long dateId){
        Date date = dateRepository.findById(dateId).orElseThrow(() -> new DateNotFoundException(dateId));
        dateRepository.delete(date);
        return date;
    }

    public Date updateDate(Long dateId,DateDTORequest request){
        Date newDate = dateRepository.findById(dateId).orElseThrow(() -> new DateNotFoundException(dateId));

        if (request.getStartDate() != null){
            newDate.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null){
            newDate.setEndDate(request.getEndDate());
        }

        return dateRepository.save(newDate);
    }

    public Date getDateByCardId(Long cardId){
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("NotFound Card"));
        return card.getDate();
    }
}
