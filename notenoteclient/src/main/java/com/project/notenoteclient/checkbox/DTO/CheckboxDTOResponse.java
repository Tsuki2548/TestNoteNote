package com.project.notenoteclient.checkbox.DTO;

public class CheckboxDTOResponse {
    private Long checkboxId;
    private String checkboxTitle;
    private Long checklistId;
    private boolean completed;

    public CheckboxDTOResponse(Long checkboxId, String checkboxTitle, Long checklistId, boolean completed){
        this.checkboxId = checkboxId;
        this.checkboxTitle = checkboxTitle;
        this.checklistId = checklistId;
        this.completed = completed;
    }

    public Long getCheckboxId() { return checkboxId; }
    public String getCheckboxTitle() { return checkboxTitle; }
    public Long getChecklistId() { return checklistId; }
    public boolean getCompleted() { return completed; }
}
