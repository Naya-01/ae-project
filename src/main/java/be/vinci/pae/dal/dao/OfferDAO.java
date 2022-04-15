package be.vinci.pae.dal.dao;

import be.vinci.pae.business.domain.dto.OfferDTO;
import java.util.List;

public interface OfferDAO {

  /**
   * Get all offers.
   *
   * @param searchPattern the search pattern (empty -> all) according to their type, description
   * @param idMember      the member id if you want only your offers (0 -> all)
   * @param type          the type of object that we want
   * @return list of offers
   */
  List<OfferDTO> getAll(String searchPattern, int idMember, String type, String objectStatus);

  /**
   * Get the last six offers posted.
   *
   * @return a list of six offerDTO
   */
  List<OfferDTO> getAllLast();

  /**
   * Get the offer with a specific id.
   *
   * @param idOffer       the id of the offer
   * @return an offer that match with the idOffer or null
   */
  OfferDTO getOne(int idOffer);


  /**
   * Get last offer of an object.
   *
   * @param idObject the id of the object
   * @return an offer
   */
  OfferDTO getLastObjectOffer(int idObject);

  /**
   * Add an offer in the db.
   *
   * @param offerDTO an offer we want to add in the db
   * @return the offerDTO added
   */
  OfferDTO addOne(OfferDTO offerDTO);

  /**
   * Update the time slot of an offer.
   *
   * @param offerDTO an offerDTO that contains the new time slot and the id of the offer
   * @return an offerDTO with the id and the new time slot or null
   */
  OfferDTO updateOne(OfferDTO offerDTO);

  /**
   * Get all offers received by a member.
   *
   * @param idReceiver the id of the receiver
   * @return a list of offerDTO
   */
  List<OfferDTO> getAllGivenOffers(int idReceiver);
}
