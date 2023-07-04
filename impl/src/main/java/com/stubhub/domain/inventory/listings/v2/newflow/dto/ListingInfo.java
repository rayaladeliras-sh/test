package com.stubhub.domain.inventory.listings.v2.newflow.dto;

import java.util.ArrayList;
import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;

public class ListingInfo {
  
  private Integer quantity;
  
  private Integer quantityRemain;
  
  private String section;
  
  private String row;
  
  private String seats;
  
  private List<TicketSeat> ticketSeats;
  
  private List<ListingSeatTrait> ticketTraits;
  
  private Short splitOption;
  
  private Integer splitQuantity;

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Integer getQuantityRemain() {
    return quantityRemain;
  }

  public void setQuantityRemain(Integer quantityRemain) {
    this.quantityRemain = quantityRemain;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getRow() {
    return row;
  }

  public void setRow(String row) {
    this.row = row;
  }

  public String getSeats() {
    return seats;
  }

  public void setSeats(String seats) {
    this.seats = seats;
  }

  public List<TicketSeat> getTicketSeats() {
    return new ArrayList<TicketSeat>(ticketSeats);
  }

  public void setTicketSeats(List<TicketSeat> ticketSeats) {
    this.ticketSeats = new ArrayList<TicketSeat>(ticketSeats);
  }

  public List<ListingSeatTrait> getTicketTraits() {
    return new ArrayList<ListingSeatTrait>(ticketTraits);
  }

  public void setTicketTraits(List<ListingSeatTrait> ticketTraits) {
    this.ticketTraits = new ArrayList<ListingSeatTrait>(ticketTraits);
  }

  public Short getSplitOption() {
    return splitOption;
  }

  public void setSplitOption(Short splitOption) {
    this.splitOption = splitOption;
  }

  public Integer getSplitQuantity() {
    return splitQuantity;
  }

  public void setSplitQuantity(Integer splitQuantity) {
    this.splitQuantity = splitQuantity;
  }
  
}
