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
    java.time.OffsetDateTime start = request.getStartDate()!=null ? request.getStartDate() : java.time.OffsetDateTime.now(java.time.ZoneOffset.ofHours(7));
        Date date = new Date(start, request.getEndDate());
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

    public Date updateDateByCardId(Long cardId, DateDTORequest request){
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("NotFound Card"));
        Date date = card.getDate();
        if (date == null){
            java.time.OffsetDateTime start = request.getStartDate()!=null ? request.getStartDate() : java.time.OffsetDateTime.now(java.time.ZoneOffset.ofHours(7));
            date = new Date(start, request.getEndDate());
            date = dateRepository.save(date);
            card.setDate(date);
            cardRepository.save(card);
            return date;
        }
        if (request.getStartDate() != null){
            date.setStartDate(request.getStartDate());
        }
        // Allow clearing end date by passing null explicitly
        if (request.getEndDate() != null || (request.getEndDate() == null)){
            date.setEndDate(request.getEndDate());
        }
        return dateRepository.save(date);
    }
}
