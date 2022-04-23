package be.vinci.pae.business.ucc;

import be.vinci.pae.business.domain.dto.InterestDTO;
import java.util.List;

public interface InterestUCC {

  /**
   * Find an interest, by the id of the interested member and the id of the object.
   *
   * @param idObject : id object of the interest.
   * @param idMember : id of interested member.
   * @return interestDTO having the idObject and idMember.
   */
  InterestDTO getInterest(int idObject, int idMember);

  /**
   * Add one interest.
   *
   * @param item : the interest informations (id of the object and id of the member).
   * @return item.
   */
  InterestDTO addOne(InterestDTO item);

  /**
   * Assign the offer to a member.
   *
   * @param interestDTO : the interest informations (id of the object and id of the member).
   * @return objectDTO updated.
   */
  InterestDTO assignOffer(InterestDTO interestDTO);

  /**
   * Get a list of interest, by an id object.
   *
   * @param idObject the object we want to retrieve the interests
   * @return a list of interest, by an id object
   */
  List<InterestDTO> getInterestedCount(int idObject);

  /**
   * Get a list of notificated interest in an id object.
   *
   * @param idMember the member we want to retrieve notifications
   * @return a list of interest, by an id member
   */
  List<InterestDTO> getNotifications(int idMember);


  /**
   * Mark a notification shown.
   *
   * @param interestDTO to mark as shown.
   * @return interestDTO updated.
   */
  InterestDTO markNotificationShown(InterestDTO interestDTO);

}
