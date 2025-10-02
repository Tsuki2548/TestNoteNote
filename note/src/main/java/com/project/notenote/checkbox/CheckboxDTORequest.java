package com.project.notenote.checkbox;

public class CheckboxDTORequest {
    private String checkboxTitle;
    private Long checklistId;
    private Boolean completed;

    public CheckboxDTORequest() {}

    public CheckboxDTORequest(String title, Long checklistId, Boolean completed){
        this.checkboxTitle = title;
        this.checklistId = checklistId;
    }

    public String getCheckboxTitle() { return checkboxTitle; }
    public void setCheckboxTitle(String title) { this.checkboxTitle = title; }

    public Long getChecklistId() { return checklistId; }
    public void setChecklistId(Long id) { this.checklistId = id; }

    public Boolean getCompleted(){return completed;}
    public void setCompleted(Boolean completed){this.completed = completed;}

}
