package com.project.notenoteclient.checklist.DTO;

public class ChecklistDTORequest {
    private String checklistTitle;
    private Long cardId;

    public ChecklistDTORequest(){}

    public ChecklistDTORequest(String title, Long id){
        checklistTitle = title;
        cardId = id;
    }

    public String getChecklistTitle(){
        return checklistTitle;
    }

    public void setChecklistTitle(String title){
        checklistTitle = title;
    }

    public Long getCardId(){
        return cardId;
    }

    public void setCardId(Long id){
        cardId = id;
    }
}
