package com.project.notenote.checkbox;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.notenote.checklist.Checklist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "checkboxs")
public class Checkbox {

    // Attributes
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long checkboxId;

    @Column(name = "checkboxTitle", nullable = false)
    private String checkboxTitle;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    // relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklistId", nullable = false)
    @JsonBackReference
    private Checklist checklist;

    public Checkbox(){super();}

    public Checkbox(String checkboxTitle){
        super();
        this.checkboxTitle = checkboxTitle;
        this.completed = false;
    }

    public Long getCheckboxId() { return checkboxId; }

    public void setCheckboxTitle(String checkboxTitle) { this.checkboxTitle = checkboxTitle; }
    public String getCheckboxTitle() { return checkboxTitle; }

    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean getCompleted() { return completed; }

    public void setChecklist(Checklist checklist) { this.checklist = checklist; }
    public Checklist getChecklist() { return this.checklist; }

}
