package com.project.notenote.label;

import java.util.HashSet;
import java.util.Set;

import com.project.notenote.card.Card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
    
    @Column(name = "color", nullable = false, length = 20)
    private String color;

    //Relationships (many-to-many with cards)
    @ManyToMany(mappedBy = "labels", fetch = FetchType.LAZY)
    private Set<Card> cards = new HashSet<>();

    public Label(){}
    
    public Label(String labelName){
        super();
        this.labelName = labelName;
    }

    public Label(String labelName, String color){
        super();
        this.labelName = labelName;
        this.color = color;
    }

    public Long getLabelId(){return labelId;}

    public void setLabelName(String labelName){this.labelName = labelName;}
    public String getLabelName(){return labelName;}

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public void setCards(Set<Card> cards){this.cards = cards;}
    public Set<Card> getCards(){return cards;}

    // convenience helpers for bidirectional sync (optional to use from service)
    public void addCard(Card card) { this.cards.add(card); }
    public void removeCard(Card card) { this.cards.remove(card); }
}
