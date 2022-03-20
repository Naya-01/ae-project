// source regex phone number : https://ihateregex.io/expr/phone/

package be.vinci.pae.ihm;

import be.vinci.pae.business.domain.dto.AddressDTO;
import be.vinci.pae.business.domain.dto.MemberDTO;
import be.vinci.pae.business.exceptions.BadRequestException;
import be.vinci.pae.business.exceptions.NotFoundException;
import be.vinci.pae.business.ucc.MemberUCC;
import be.vinci.pae.ihm.filters.Authorize;
import be.vinci.pae.ihm.manager.Token;
import be.vinci.pae.utils.JsonViews;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.glassfish.jersey.server.ContainerRequest;

@Singleton
@Path("/auth")
public class AuthResource {

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  @Inject
  private MemberUCC memberUCC;
  @Inject
  private Token tokenManager;

  /**
   * Log in a quidam by a username and a password.
   *
   * @param json a json object that contains username and password
   * @return a json object that contains the token or a http error
   */
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public ObjectNode login(JsonNode json) {

    if (!json.hasNonNull("username") || !json.hasNonNull("password")) {
      throw new BadRequestException("Pseudonyme ou mot de passe requis");
    }
    String username = json.get("username").asText();
    String password = json.get("password").asText();
    MemberDTO memberDTO = memberUCC.login(username, password);
    if (memberDTO == null) {
      throw new NotFoundException("Pseudonyme ou mot de passe incorrect");
    }
    String accessToken = tokenManager.withoutRememberMe(memberDTO);
    String refreshToken;
    if (json.get("rememberMe").asBoolean()) {
      refreshToken = tokenManager.withRememberMe(memberDTO);
    } else {
      refreshToken = accessToken;
    }

    return jsonMapper.createObjectNode()
        .put("access_token", accessToken)
        .put("refresh_token", refreshToken)
        .putPOJO("user", JsonViews.filterPublicJsonView(memberDTO, MemberDTO.class));
  }

  /**
   * Refresh the access token for authenticated member.
   *
   * @param request to get information request
   * @return a json object that contains the new access token
   */
  @GET
  @Path("/refreshToken")
  @Produces(MediaType.APPLICATION_JSON)
  @Authorize
  public ObjectNode refreshToken(@Context ContainerRequest request) {
    MemberDTO memberDTO = (MemberDTO) request.getProperty("user");
    String accessToken = tokenManager.withoutRememberMe(memberDTO);
    return jsonMapper.createObjectNode()
        .put("access_token", accessToken)
        .putPOJO("user", JsonViews.filterPublicJsonView(memberDTO, MemberDTO.class));
  }

  /**
   * Register a quidam.
   *
   * @param member : all information of the quidam.
   * @return a json object that contains the token.
   */
  @POST
  @Path("register")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public ObjectNode register(MemberDTO member) {
    // Check if there is a member, and then if there is an address
    if (member == null || member.getAddress() == null) {
      throw new BadRequestException("Manque d'informations obligatoires");
    }

    // Check is Not Null fields are not null nor blank
    if (member.getUsername() == null || member.getUsername().isBlank()
        || member.getPassword() == null || member.getPassword().isBlank()
        || member.getFirstname() == null || member.getFirstname().isBlank()
        || member.getLastname() == null || member.getLastname().isBlank()
        || member.getAddress().getBuildingNumber() == null
        || member.getAddress().getBuildingNumber().isBlank()
        || member.getAddress().getStreet() == null || member.getAddress().getStreet().isBlank()
        || member.getAddress().getPostcode() == null || member.getAddress().getPostcode().isBlank()
        || member.getAddress().getCommune() == null || member.getAddress().getCommune().isBlank()
        || member.getAddress().getCountry() == null || member.getAddress().getCountry().isBlank()
    ) {
      throw new BadRequestException("Veuillez remplir tous les champs obligatoires");
    }

    // Check length of Username, Lastname and Firstname fields (member)
    if (member.getUsername().length() > 50) {
      throw new BadRequestException("Le pseudonyme dépasse la longueur maximale autorisée "
          + "(50 caractères max compris)");
    }
    if (member.getLastname().length() > 50) {
      throw new BadRequestException("Le nom dépasse la longueur maximale autorisée (50 caractères "
          + "max compris)");
    }
    if (member.getFirstname().length() > 50) {
      throw new BadRequestException("Le prénom dépasse la longueur maximale autorisée (50 "
          + "caractères max compris)");
    }

    // Check the number phone if is valid
    if (member.getPhone() != null) {
      Pattern pattern = Pattern.compile("^[+]?[(]?[0-9]{3}[)]?[- .]?[0-9]{3}[- .]?[0-9]{4,6}$");
      Matcher matcher = pattern.matcher(member.getPhone());
      if (!matcher.find()) {
        throw new BadRequestException("Numéro de GSM invalide");
      }
    }

    // Check length of address fields
    AddressDTO addressOfMember = member.getAddress();

    if (addressOfMember.getUnitNumber() != null && addressOfMember.getUnitNumber().length() > 15) {
      throw new BadRequestException("La boite de l'adresse dépasse la longueur maximale autorisée "
          + "(15 caractères max compris)");
    }

    if (addressOfMember.getBuildingNumber().length() > 8) {
      throw new BadRequestException("Le numéro de l'adresse dépasse la longueur maximale autorisée "
          + "(8 caractères max compris)");
    }

    if (addressOfMember.getStreet().length() > 50) {
      throw new BadRequestException("Le nom de rue dépasse la longueur maximale autorisée "
          + "(50 caractères max compris)");
    }

    if (addressOfMember.getPostcode().length() > 15) {
      throw new BadRequestException("Le code postal dépasse la longueur maximale autorisée "
          + "(15 caractères max compris)");
    }

    if (addressOfMember.getCommune().length() > 50) {
      throw new BadRequestException("La commune dépasse la longueur maximale autorisée (50 "
          + "caractères max compris)");
    }

    if (addressOfMember.getCountry().length() > 50) {
      throw new BadRequestException("Le pays dépasse la longueur maximale autorisée (50 caractères "
          + "max compris)");
    }

    // Register the member
    MemberDTO memberDTO = memberUCC.register(member);
    String accessToken = tokenManager.withoutRememberMe(memberDTO);
    return jsonMapper.createObjectNode()
        .put("access_token", accessToken)
        .put("refresh_token", accessToken)
        .putPOJO("member", JsonViews.filterPublicJsonView(memberDTO, MemberDTO.class));

  }

  /**
   * Get a user by his id.
   *
   * @param id the id of the member we want to get
   * @return return the linked user to his id
   */
  @GET
  @Path("/id/{id}")
  @Authorize
  @Produces(MediaType.APPLICATION_JSON)
  public ObjectNode getUserById(@PathParam("id") int id) {
    MemberDTO memberDTO = memberUCC.getMember(id);
    return jsonMapper.createObjectNode()
        .putPOJO("user", JsonViews.filterPublicJsonView(memberDTO, MemberDTO.class));
  }


}
