package be.vinci.pae.business.domain;

import be.vinci.pae.business.domain.dto.ObjectDTO;
import be.vinci.pae.business.domain.dto.OfferDTO;
import be.vinci.pae.utils.Views;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferImpl implements OfferDTO {

  @JsonView(Views.Public.class)
  private int idOffer;
  @JsonView(Views.Public.class)
  private LocalDate date;
  @JsonView(Views.Public.class)
  private String timeSlot;
  @JsonView(Views.Public.class)
  private ObjectDTO object;

  @Override
  public int getIdOffer() {
    return idOffer;
  }

  @Override
  public void setIdOffer(int idOffer) {
    this.idOffer = idOffer;
  }

  @Override
  public LocalDate getDate() {
    return date;
  }

  @Override
  public void setDate(LocalDate date) {
    this.date = date;
  }

  @Override
  public String getTimeSlot() {
    return timeSlot;
  }

  @Override
  public void setTimeSlot(String timeSlot) {
    this.timeSlot = timeSlot;
  }

  @Override
  public ObjectDTO getObject() {
    return object;
  }

  @Override
  public void setObject(ObjectDTO object) {
    this.object = object;
  }

  @Override
  public String toString() {
    return "OfferImpl{" +
        "idOffer=" + idOffer +
        ", date=" + date +
        ", timeSlot='" + timeSlot + '\'' +
        ", object=" + object +
        '}';
  }
}