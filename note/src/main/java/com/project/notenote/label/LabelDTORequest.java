package com.project.notenote.label;

public class LabelDTORequest {
    private String labelName;

    public LabelDTORequest() { }

    public LabelDTORequest(String labelName, Long cardId) {
        this.labelName = labelName;
    }

    public void setLabelName(String labelName) { this.labelName = labelName; }
    public String getLabelName() { return labelName; }

}
