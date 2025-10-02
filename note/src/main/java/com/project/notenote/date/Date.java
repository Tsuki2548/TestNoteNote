package com.project.notenote.date;

import java.time.OffsetDateTime;

import com.project.notenote.card.Card;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "dates")
public class Date {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dateId;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date", nullable = true)
    private OffsetDateTime endDate;

    @OneToOne(mappedBy = "date", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Card card;

    public Date(){}

    public Date(OffsetDateTime startDate,OffsetDateTime endDate){
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getDateId(){return dateId;}

    public void setStartDate(OffsetDateTime startDate){this.startDate = startDate;}
    public OffsetDateTime getStartDate(){return startDate;}

    public void setEndDate(OffsetDateTime endDate){this.endDate = endDate;}
    public OffsetDateTime getEndDate(){return endDate;}

    public void setCard(Card card){this.card = card;}
    public Card getCard(){return card;}
}
