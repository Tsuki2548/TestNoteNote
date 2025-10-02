package com.project.notenote.date;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dates")
public class DateController {
    @Autowired
    private DateService dateService;

    public DateDTOResponse getDateResponse(Date date){
        DateDTOResponse response = new DateDTOResponse(
                                                date.getStartDate(),
                                                date.getEndDate()
                                        );
        return response;
    }

    public List<DateDTOResponse> getListDateResponse(List<Date> dates){
        List<DateDTOResponse> response = dates.stream()
                                        .map(c -> new DateDTOResponse(
                                            c.getStartDate(),
                                            c.getEndDate()
                                        )).toList();
        return response;
    }

    @GetMapping
    public ResponseEntity<List<DateDTOResponse>> getAllDate(){
        List<Date> dates = dateService.getAllDate();
        List<DateDTOResponse> response = getListDateResponse(dates);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{dateId}")
    public ResponseEntity<DateDTOResponse> getDateById(@PathVariable Long dateId){
        Date date = dateService.getDateById(dateId);
        DateDTOResponse response = getDateResponse(date);
        return ResponseEntity.ok(response);
    }

     @GetMapping("/byCardId/{cardId}")
    public ResponseEntity<DateDTOResponse> getDateByCardId(@PathVariable Long cardId){
        Date date = dateService.getDateByCardId(cardId);
        DateDTOResponse response = getDateResponse(date);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DateDTOResponse> createDate(@RequestBody DateDTORequest request ){
        Date date = dateService.saveDate(request);
        DateDTOResponse response = getDateResponse(date);

        return ResponseEntity.ok(response);
    }

    // Set or update date for a card: start = existing or now, end = provided
    @PutMapping("/byCardId/{cardId}")
    public ResponseEntity<DateDTOResponse> upsertDateByCardId(@PathVariable Long cardId, @RequestBody DateDTORequest request){
        Date date = dateService.updateDateByCardId(cardId, request);
        DateDTOResponse response = getDateResponse(date);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{dateId}")
    public ResponseEntity<DateDTOResponse> updateDate(@PathVariable Long dateId,@RequestBody DateDTORequest request){
        Date date = dateService.updateDate(dateId, request);
        DateDTOResponse response = getDateResponse(date);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{dateId}")
    public ResponseEntity<DateDTOResponse> deleteDate(@PathVariable Long dateId){
        Date date = dateService.deleteDateByID(dateId);
        DateDTOResponse response = getDateResponse(date);

        return ResponseEntity.ok(response);
    }

}
