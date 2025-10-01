package com.project.notenoteclient.checklist.DTO;

public class ChecklistDTOResponse {
    private Long checklistId;
    private String checklistTitle;
    private Long cardId;

    public ChecklistDTOResponse(Long checklistId, String checklistTitle, Long cardId){
        this.checklistId = checklistId;
        this.checklistTitle = checklistTitle;
        this.cardId = cardId;
    }

    public Long getChecklistId() { return checklistId; }
    public String getChecklistTitle() { return checklistTitle; }
    public Long getCardId() { return cardId; }
}
