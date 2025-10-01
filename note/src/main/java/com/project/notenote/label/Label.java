package com.project.notenote.label;

import java.util.ArrayList;
import java.util.List;

import com.project.notenote.card.Card;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "labels")
public class Label {
    // Attributes
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long labelId;

    @Column(name = "labelName",nullable = false)
    private String labelName;

    //Relationships
    @OneToMany(mappedBy = "label", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Card> cards = new ArrayList<>();

    public Label(){}
    
    public Label(String labelName){
        super();
        this.labelName = labelName;
    }

    public Long getLabelId(){return labelId;}

    public void setLabelName(String labelName){this.labelName = labelName;}
    public String getLabelName(){return labelName;}

    public void setCard(List<Card> cards){this.cards = cards;}
    public List<Card> getCard(){return cards;}

    public void addCard(Card card) {
        cards.add(card);
        card.setLabel(this);
    }
    public void removeCard(Card card) {
        cards.remove(card);
        card.setLabel(null);
    }
}
