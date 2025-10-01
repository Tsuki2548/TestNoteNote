package com.project.notenoteclient.label.DTO;

public class LabelDTOResponse {
    private Long labelId;
    private String labelName;


    public LabelDTOResponse() {}

    public LabelDTOResponse(Long labelId, String labelName) {
        this.labelId = labelId;
        this.labelName = labelName;
    }

    public void setLabelId(Long labelId) { this.labelId = labelId; }
    public Long getLabelId() { return labelId; }

    public void setLabelName(String labelName) { this.labelName = labelName; }
    public String getLabelName() { return labelName; }
}
