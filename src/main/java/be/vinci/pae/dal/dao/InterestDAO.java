package be.vinci.pae.dal.dao;

import be.vinci.pae.business.domain.dto.InterestDTO;
import java.util.List;

public interface InterestDAO {

  /**
   * Get an interest we want to retrieve by the id of the interested member and the id of the
   * object.
   *
   * @param interestDTO : the interest informations (id of the object and id of the member).
   * @return the interest.
   */
  InterestDTO getOne(InterestDTO interestDTO);

  InterestDTO getGiveInterest(int idObject);

  /**
   * Add one interest in the DB.
   *
   * @param item : interestDTO object.
   * @return item.
   */
  InterestDTO addOne(InterestDTO item);

  /**
   * Get a list of interest in an id object.
   *
   * @param idObject the object we want to retrieve the interests
   * @return a list of interest, by an id object
   */
  List<InterestDTO> getAll(int idObject);

  /**
   * Update the status of an interest.
   *
   * @param interestDTO the object that we want to edit the status.
   * @return interest
   */
  InterestDTO updateStatus(InterestDTO interestDTO);
}
