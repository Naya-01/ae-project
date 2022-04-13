package be.vinci.pae.business.ucc;

import be.vinci.pae.business.domain.dto.InterestDTO;
import be.vinci.pae.business.domain.dto.OfferDTO;
import be.vinci.pae.business.domain.dto.TypeDTO;
import be.vinci.pae.dal.dao.InterestDAO;
import be.vinci.pae.dal.dao.ObjectDAO;
import be.vinci.pae.dal.dao.OfferDAO;
import be.vinci.pae.dal.dao.TypeDAO;
import be.vinci.pae.dal.services.DALService;
import be.vinci.pae.exceptions.FatalException;
import be.vinci.pae.exceptions.NotFoundException;
import jakarta.inject.Inject;
import java.util.List;

public class OfferUCCImpl implements OfferUCC {

  @Inject
  private OfferDAO offerDAO;
  @Inject
  private ObjectDAO objectDAO;
  @Inject
  private TypeDAO typeDAO;
  @Inject
  private DALService dalService;
  @Inject
  private InterestDAO interestDAO;

  /**
   * Get the last six offers posted.
   *
   * @return a list of six offerDTO
   */
  @Override
  public List<OfferDTO> getLastOffers() {
    List<OfferDTO> offers;
    try {
      dalService.startTransaction();
      offers = offerDAO.getAllLast();
      if (offers.isEmpty()) {
        throw new NotFoundException("Aucune offres");
      }
      dalService.commitTransaction();

    } catch (Exception e) {
      dalService.rollBackTransaction();
      throw e;
    }
    return offers;
  }

  /**
   * Get the offer with a specific id.
   *
   * @param idOffer the id of the offer
   * @return an offer that match with the idOffer or an error if offer not found
   */
  @Override
  public OfferDTO getOfferById(int idOffer) {
    OfferDTO offerDTO;
    try {
      dalService.startTransaction();
      offerDTO = offerDAO.getOne(idOffer);
      if (offerDTO == null) {
        throw new NotFoundException("Aucune offres");
      }
      dalService.commitTransaction();

    } catch (Exception e) {
      dalService.rollBackTransaction();
      throw e;
    }
    return offerDTO;

  }

  /**
   * Add an offer in the db with out without an object.
   *
   * @param offerDTO an offer we want to add in the db
   * @return the offerDTO added
   */
  @Override
  public OfferDTO addOffer(OfferDTO offerDTO) {
    OfferDTO offer;
    try {
      dalService.startTransaction();
      setCorrectType(offerDTO);

      if (offerDTO.getObject().getIdObject() == null
          && objectDAO.addOne(offerDTO.getObject()) == null) {
        throw new FatalException("Problème lors de la création d'un objet");
      }
      offer = offerDAO.addOne(offerDTO);
      if (offer == null) {
        throw new FatalException("Problème lors de la création d'une offre");
      }
      dalService.commitTransaction();
    } catch (Exception e) {
      dalService.rollBackTransaction();
      throw e;
    }
    return offer;
  }

  /**
   * Update the time slot of an offer or an errorcode.
   *
   * @param offerDTO an offerDTO that contains the new time slot and the id of the offer
   * @return an offerDTO with the id and the new time slot
   */
  @Override
  public OfferDTO updateOffer(OfferDTO offerDTO) {
    OfferDTO offer;
    try {
      dalService.startTransaction();
      offer = offerDAO.updateOne(offerDTO);
      if (offer == null) {
        throw new FatalException("Problème lors de la mise à jour de l'offre");
      }

      dalService.commitTransaction();
    } catch (Exception e) {
      dalService.rollBackTransaction();
      throw e;
    }
    return offer;
  }

  /**
   * Verify the type and set it.
   *
   * @param offerDTO the offer that has an object that has a type.
   */
  private void setCorrectType(OfferDTO offerDTO) {
    TypeDTO typeDTO;
    if (offerDTO.getObject().getType().getTypeName() != null && !offerDTO.getObject().getType()
        .getTypeName().isBlank()) {
      typeDTO = typeDAO.getOne(offerDTO.getObject().getType().getTypeName());
      if (typeDTO == null) {
        typeDTO = typeDAO.addOne(offerDTO.getObject().getType().getTypeName());
        if (typeDTO == null) {
          throw new FatalException("Problème lors de la création du type");
        }
      }
    } else {
      typeDTO = typeDAO.getOne(offerDTO.getObject().getType().getIdType());
    }
    offerDTO.getObject().setType(typeDTO);
  }

  /**
   * Get all offers.
   *
   * @param search   the search pattern (empty -> all) according to their type, description
   * @param idMember the member id if you want only your offers (0 -> all)
   * @param type     the type of object that we want
   * @return list of offers
   */
  @Override
  public List<OfferDTO> getOffers(String search, int idMember, String type, String objectStatus) {
    List<OfferDTO> offerDTO = null;
    try {
      dalService.startTransaction();
      offerDTO = offerDAO.getAll(search, idMember, type, objectStatus);
      if (offerDTO.isEmpty()) {
        throw new NotFoundException("Aucune offre");
      }
      dalService.commitTransaction();
    } catch (Exception e) {
      dalService.rollBackTransaction();
      throw e;
    }
    return offerDTO;
  }

  /**
   * Get all offers received by a member.
   *
   * @param idReceiver the id of the receiver
   * @return a list of offerDTO
   */
  @Override
  public List<OfferDTO> getGivenOffers(int idReceiver) {
    List<OfferDTO> offerDTO;
    try {
      dalService.startTransaction();
      offerDTO = offerDAO.getAllGivenOffers(idReceiver);
      if (offerDTO.isEmpty()) {
        throw new NotFoundException("Aucune offre");
      }
      dalService.commitTransaction();
    } catch (Exception e) {
      dalService.rollBackTransaction();
      throw e;
    }
    return offerDTO;
  }


  /**
   * Cancel an Object.
   *
   * @param offerDTO object with his id & new status to 'cancelled'
   * @return an object
   */
  @Override
  public OfferDTO cancelObject(OfferDTO offerDTO) {
    try {
      dalService.startTransaction();
      offerDTO.setStatus("cancelled");
      offerDTO.getObject().setStatus("cancelled");

      offerDTO = offerDAO.updateOne(offerDTO);
      System.out.println("ehsufgjhgfdqFjk");
      System.out.println(offerDTO);
      offerDTO.setObject(objectDAO.updateOne(offerDTO.getObject()));
      InterestDTO interestDTO = interestDAO
          .getAssignedInterest(offerDTO.getObject().getIdObject());

      if (interestDTO != null) {
        interestDTO.setStatus("published");
        interestDAO.updateStatus(interestDTO);
      }

      dalService.commitTransaction();
    } catch (Exception e) {
      dalService.rollBackTransaction();
      throw e;
    }

    return offerDTO;
  }
}
