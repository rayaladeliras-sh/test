package com.stubhub.domain.inventory.v2.DTO;

import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import javax.xml.bind.annotation.*;

/**
 * Created by kabburi on 3/26/19.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ticketMedium")
@XmlType(name = "", propOrder = {
        "ticketMedium",
        "inHandDate",
        "adjustInHandDate"
})
public class TicketMediumInfo {

    @XmlElement(name = "ticketMedium", required = true)
    @JsonDeserialize(using = TicketMediumDeserializer.class)
    private TicketMedium ticketMedium;

    @XmlElement(name = "inHandDate", required = true)
    private String inHandDate;

    @XmlElement(name = "adjustInHandDate", required = false)
    private Boolean adjustInHandDate = true;

    public TicketMedium getTicketMedium() {
        return ticketMedium;
    }

    public void setTicketMedium(TicketMedium ticketMedium) {
        this.ticketMedium = ticketMedium;
    }

    public String getInHandDate() {
        return inHandDate;
    }

    public void setInHandDate(String inHandDate) {
        this.inHandDate = inHandDate;
    }

    public Boolean getAdjustInHandDate() {
        return adjustInHandDate;
    }

    public void setAdjustInHandDate(Boolean adjustInHandDate) {
        this.adjustInHandDate = adjustInHandDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TicketMediumInfo that = (TicketMediumInfo) o;

        return new EqualsBuilder()
                .append(ticketMedium, that.ticketMedium)
                .append(inHandDate, that.inHandDate)
                .append(adjustInHandDate, that.adjustInHandDate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(ticketMedium)
                .append(inHandDate)
                .append(adjustInHandDate)
                .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TicketMediumInfo{");
        sb.append("ticketMedium=").append(ticketMedium);
        sb.append(", inHandDate='").append(inHandDate).append('\'');
        sb.append(", adjustInHandDate='").append(adjustInHandDate).append('\'');
        sb.append('}');
        return sb.toString();
    }


}
