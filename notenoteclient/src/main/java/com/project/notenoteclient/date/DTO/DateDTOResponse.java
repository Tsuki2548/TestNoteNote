package com.project.notenoteclient.date.DTO;

import java.time.OffsetDateTime;

public class DateDTOResponse {
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    public DateDTOResponse(){}

    public DateDTOResponse(OffsetDateTime startDate,OffsetDateTime endDate){
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setStartDate(OffsetDateTime startDate){this.startDate = startDate;}
    public OffsetDateTime getStartDate(){return startDate;}

    public void setEndDate(OffsetDateTime endDate){this.endDate = endDate;}
    public OffsetDateTime getEndDate(){return endDate;} 
}
