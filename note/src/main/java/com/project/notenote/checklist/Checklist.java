package com.project.notenote.checklist;

import java.util.ArrayList;
import java.util.List;

import com.project.notenote.card.Card;
import com.project.notenote.checkbox.Checkbox;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "checklists")
public class Checklist {
    // Attributes
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long checklistId;

    @Column(name = "checklistTitle", nullable = false)
    private String checklistTitle;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Checkbox> Checkboxs = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cardId", nullable = true)
    private Card card;

    public Checklist(){super();}

    public Checklist(String checklistTitle){
        super();
        this.checklistTitle = checklistTitle;
    }

    public Long getChecklistId(){return checklistId;}

    public void setChecklistTitle(String checklistTitle) { this.checklistTitle = checklistTitle; }
    public String getChecklistTitle() { return checklistTitle; }

    public void setCard(Card card) { this.card = card; }
    public Card getCard() { return card; }

    public void setCheckboxs(List<Checkbox> checkboxs) { Checkboxs = checkboxs; }
    public List<Checkbox> getCheckboxs() { return Checkboxs; }

    public void addCheckbox(Checkbox checkbox) {
        Checkboxs.add(checkbox);
        checkbox.setChecklist(this);
    }
    public void removeCheckbox(Checkbox checkbox) {
        Checkboxs.remove(checkbox);
        checkbox.setChecklist(null);
    }

}
