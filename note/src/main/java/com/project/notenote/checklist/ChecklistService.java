package com.project.notenote.checklist;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.notenote.card.Card;
import com.project.notenote.card.CardService;

@Service
public class ChecklistService {
    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private CardService cardService;

    public Checklist saveChecklist(ChecklistDTORequest request){
        Checklist checklist = new Checklist(
                                    request.getChecklistTitle()
                                );
        if (request.getCardId() != null){
            Card card = cardService.getCardById(request.getCardId());
            checklist.setCard(card);
        }else{
            checklist.setCard(null);
        }
        

        return checklistRepository.save(checklist);
    }

    public Checklist getChecklistById(Long checklistId){
        if (checklistId == null){
            return null;
        }
        return checklistRepository.findById(checklistId).orElseThrow(() -> new ChecklistNotFoundException("NotFound Checklist"));
    }

    public List<Checklist> getAllChecklist() {
        return (List<Checklist>) checklistRepository.findAll();
    }

    public Checklist deleteChecklistById(Long checklistId) {
        Checklist checklist = checklistRepository.findById(checklistId).orElseThrow(() -> new ChecklistNotFoundException("NotFound Checklist"));
        
        checklistRepository.delete(checklist);
        return checklist;
    }

    public Checklist updateChecklist(Long checklistId, ChecklistDTORequest request) {
        Checklist checklist = checklistRepository.findById(checklistId).orElseThrow(() -> new ChecklistNotFoundException("NotFound Checklist"));
        Card card = cardService.getCardById(request.getCardId());

        checklist.setChecklistTitle(request.getChecklistTitle());
        checklist.setCard(card);

        return checklistRepository.save(checklist);
    }

    public List<Checklist> getChecklistByCardId(Long cardId){
        Card card = cardService.getCardById(cardId);
        return card.getChecklists();
    }
}
