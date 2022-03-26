package be.vinci.pae.business.ucc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import be.vinci.pae.TestBinder;
import be.vinci.pae.dal.dao.OfferDAO;
import be.vinci.pae.dal.services.DALService;
import be.vinci.pae.exceptions.NotFoundException;
import java.util.ArrayList;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OfferUCCImplTest {

  private DALService mockDalService;
  private OfferDAO offerDAO;
  private OfferUCC offerUCC;

  @BeforeEach
  void setUp() {
    ServiceLocator locator = ServiceLocatorUtilities.bind(new TestBinder());
    this.mockDalService = locator.getService(DALService.class);
    this.offerDAO = locator.getService(OfferDAO.class);
    this.offerUCC = locator.getService(OfferUCC.class);
  }

  @DisplayName("Test get last offers avec aucune offre recue du DAO")
  @Test
  public void testGetAllLastOffersWithDAOReturningEmptyListOfOffers() {
    Mockito.when(offerDAO.getAllLast()).thenReturn(new ArrayList<>());
    assertAll(
        () -> assertThrows(NotFoundException.class, () -> offerUCC.getLastOffers()),
        () -> Mockito.verify(mockDalService, Mockito.atLeastOnce()).startTransaction(),
        () -> Mockito.verify(mockDalService, Mockito.atLeastOnce()).rollBackTransaction()
    );
  }
}