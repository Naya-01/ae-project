package be.vinci.pae.business.ucc;

import be.vinci.pae.business.domain.dto.MemberDTO;
import java.util.List;

public interface MemberUCC {

  /**
   * Log in a quidam by a username and a password.
   *
   * @param username : username of the member.
   * @param password : password of the member.
   * @return member having the username and password.
   */
  MemberDTO login(String username, String password);

  /**
   * Update the profil picture of the member.
   *
   * @param path of the picture
   * @param id   of the member
   * @return memberDTO updated
   */
  MemberDTO updateProfilPicture(String path, int id);

  /**
   * Find a member with his id.
   *
   * @param id : id of the member.
   * @return memberDTO having this id.
   */
  MemberDTO getMember(int id);

  /**
   * Confirm the registration of the member with his id.
   *
   * @param id of the member
   */
  void confirmRegistration(int id);

  /**
   * Decline the registration of a member with his id and the reason.
   *
   * @param id     of the member
   * @param reason for denial
   */
  void declineRegistration(int id, String reason);


  /**
   * Promote the member with his id to the admin status.
   *
   * @param id of the member
   */
  void promoteAdministrator(int id);

  /**
   * Register a quidam.
   *
   * @param memberDTO : User object with all information.
   * @return token for the user.
   */
  MemberDTO register(MemberDTO memberDTO);

  /**
   * Get all subscription requests according to their status.
   *
   * @param status the status subscription members
   * @return a list of memberDTO
   */
  List<MemberDTO> getInscriptionRequest(String status);

}
