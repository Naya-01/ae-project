// source regex phone number : https://ihateregex.io/expr/phone/
package be.vinci.pae.ihm;

import be.vinci.pae.business.domain.dto.AddressDTO;
import be.vinci.pae.business.domain.dto.MemberDTO;
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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
      throw new WebApplicationException("Pseudonyme ou mot de passe requis",
          Response.Status.BAD_REQUEST);
    }
    String username = json.get("username").asText();
    String password = json.get("password").asText();
    MemberDTO memberDTO = memberUCC.login(username, password);
    if (memberDTO == null) {
      throw new WebApplicationException("Pseudonyme ou mot de passe incorrect",
          Response.Status.NOT_FOUND);
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
   * Get a user by his token.
   *
   * @param request to get information request
   * @return return the linked user to his token
   */
  @POST
  @Path("/getuserbytoken")
  @Authorize
  @Produces(MediaType.APPLICATION_JSON)
  public ObjectNode getUserByToken(@Context ContainerRequest request) {
    MemberDTO memberDTO = (MemberDTO) request.getProperty("user");
    return jsonMapper.createObjectNode()
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
      throw new WebApplicationException("Manque d'informations obligatoires",
          Response.Status.BAD_REQUEST);
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
      throw new WebApplicationException("Veuillez remplir tous les champs obligatoires",
          Response.Status.BAD_REQUEST);
    }

    // Check length of Username, Lastname and Firstname fields (member)
    if (member.getUsername().length() > 50) {
      throw new WebApplicationException(
          "Le pseudonyme dépasse la longueur maximale autorisée (50 caractères max compris)",
          Response.Status.BAD_REQUEST);
    }
    if (member.getLastname().length() > 50) {
      throw new WebApplicationException(
          "Le nom dépasse la longueur maximale autorisée (50 caractères max compris)",
          Response.Status.BAD_REQUEST);
    }
    if (member.getFirstname().length() > 50) {
      throw new WebApplicationException(
          "Le prénom dépasse la longueur maximale autorisée (50 caractères max compris)",
          Response.Status.BAD_REQUEST);
    }

    // Check the number phone if is valid
    Pattern pattern = Pattern.compile("^[+]?[(]?[0-9]{3}[)]?[- .]?[0-9]{3}[- .]?[0-9]{4,6}$");
    Matcher matcher = pattern.matcher(member.getPhone());
    if (!matcher.find()) {
      throw new WebApplicationException("Numéro de GSM invalide",
          Response.Status.BAD_REQUEST);
    }

    // Check length of address fields
    AddressDTO addressOfMember = member.getAddress();

    if (addressOfMember.getUnitNumber() != null && addressOfMember.getUnitNumber().length() > 15) {
      throw new WebApplicationException(
          "La boite de l'adresse dépasse la longueur maximale autorisée "
              + "(15 caractères max compris)", Response.Status.BAD_REQUEST);
    }

    if (addressOfMember.getBuildingNumber().length() > 8) {
      throw new WebApplicationException(
          "Le numéro de l'adresse dépasse la longueur maximale autorisée "
              + "(8 caractères max compris)", Response.Status.BAD_REQUEST);
    }

    if (addressOfMember.getStreet().length() > 50) {
      throw new WebApplicationException(
          "Le nom de rue dépasse la longueur maximale autorisée (50 caractères max compris)",
          Response.Status.BAD_REQUEST);
    }

    if (addressOfMember.getPostcode().length() > 15) {
      throw new WebApplicationException(
          "Le code postal dépasse la longueur maximale autorisée (15 caractères max compris)",
          Response.Status.BAD_REQUEST);
    }

    if (addressOfMember.getCommune().length() > 50) {
      throw new WebApplicationException(
          "La commune dépasse la longueur maximale autorisée (50 caractères max compris)",
          Response.Status.BAD_REQUEST);
    }

    if (addressOfMember.getCountry().length() > 50) {
      throw new WebApplicationException(
          "Le pays dépasse la longueur maximale autorisée (50 caractères max compris)",
          Response.Status.BAD_REQUEST);
    }

    // Register the member
    MemberDTO memberDTO = memberUCC.register(member);
    if (memberDTO == null) {
      throw new WebApplicationException("Ce membre existe déjà", Response.Status.CONFLICT);
    }
    String accessToken = tokenManager.withoutRememberMe(memberDTO);
    return jsonMapper.createObjectNode()
        .put("access_token", accessToken)
        .put("refresh_token", accessToken)
        .putPOJO("member", JsonViews.filterPublicJsonView(memberDTO, MemberDTO.class));

  }
}
