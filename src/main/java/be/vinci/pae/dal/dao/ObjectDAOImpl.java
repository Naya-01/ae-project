package be.vinci.pae.dal.dao;

import be.vinci.pae.business.domain.dto.ObjectDTO;
import be.vinci.pae.exceptions.FatalException;
import be.vinci.pae.business.factories.ObjectFactory;
import be.vinci.pae.dal.services.DALBackendService;
import jakarta.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ObjectDAOImpl implements ObjectDAO {

  @Inject
  private DALBackendService dalBackendService;
  @Inject
  private ObjectFactory objectFactory;

  /**
   * Get an object we want to retrieve by his id.
   *
   * @param id : the id of the object that we want to retrieve
   * @return the object
   */
  @Override
  public ObjectDTO getOne(int id) {
    PreparedStatement preparedStatement = dalBackendService.getPreparedStatement(
        "SELECT id_object, description, status, image, id_offeror "
            + "FROM donnamis.objects WHERE id_object = ?");

    ObjectDTO objectDTO = objectFactory.getObjectDTO();
    try {
      preparedStatement.setInt(1, id);
      preparedStatement.executeQuery();
      ResultSet resultSet = preparedStatement.getResultSet();
      if (!resultSet.next()) {
        return null;
      }
      setObject(objectDTO, resultSet);

      resultSet.close();
      preparedStatement.close();
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return objectDTO;
  }

  /**
   * Get all objects that we want to retrieve by his status.
   *
   * @param status : the status of the objects that we want to retrieve
   * @return the object
   */
  @Override
  public List<ObjectDTO> getAllByStatus(String status) {
    PreparedStatement preparedStatement = dalBackendService.getPreparedStatement(
        "SELECT id_object, id_type, description, status, image, id_offeror "
            + "FROM donnamis.objects WHERE status = ?");

    List<ObjectDTO> objectDTOList = new ArrayList<>();
    try {
      preparedStatement.setString(1, status);
      setListObject(preparedStatement, objectDTOList);
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return objectDTOList;
  }

  /**
   * Get all objects of a member that we want to retrieve by his id.
   *
   * @param idMember : take all object of this member.
   * @return list object of this member.
   */
  @Override
  public List<ObjectDTO> getAllObjectOfMember(int idMember) {
    PreparedStatement preparedStatement = dalBackendService.getPreparedStatement(
        "SELECT id_object, id_type, description, status, image, id_offeror "
            + "FROM donnamis.objects WHERE id_offeror = ?");

    List<ObjectDTO> objectDTOList = new ArrayList<>();
    try {
      preparedStatement.setInt(1, idMember);
      setListObject(preparedStatement, objectDTOList);
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return objectDTOList;
  }

  /**
   * Add object in the database with all information.
   *
   * @param objectDTO : object that we want to add in the database.
   * @return object with his id.
   */
  @Override
  public ObjectDTO addOne(ObjectDTO objectDTO) {
    String query = "insert into donnamis.objects (id_type, description, status, image, id_offeror) "
        + "values (?,?,?,?,?) RETURNING id_object, description, status, image, id_offeror";

    try {
      PreparedStatement preparedStatement = dalBackendService.getPreparedStatement(query);
      preparedStatement.setInt(1, objectDTO.getType().getIdType());
      preparedStatement.setString(2, objectDTO.getDescription());
      preparedStatement.setString(3, objectDTO.getStatus());
      preparedStatement.setBytes(4, objectDTO.getImage());
      preparedStatement.setInt(5, objectDTO.getIdOfferor());

      preparedStatement.executeQuery();

      ResultSet resultSet = preparedStatement.getResultSet();
      if (!resultSet.next()) {
        return null;
      }

      setObject(objectDTO, resultSet);
      resultSet.close();
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return objectDTO;
  }

  /**
   * Update an object.
   *
   * @param objectDTO : object that we want to update.
   * @return object updated
   */
  @Override
  public ObjectDTO updateOne(ObjectDTO objectDTO) {
    PreparedStatement preparedStatement = dalBackendService.getPreparedStatement(
        "UPDATE donnamis.objects SET id_type = ?, description = ?, image = ?"
            + "WHERE id_object = ? RETURNING id_object, description, status, image, id_offeror");

    try {
      preparedStatement.setInt(1, objectDTO.getType().getIdType());
      preparedStatement.setString(2, objectDTO.getDescription());
      preparedStatement.setBytes(3, objectDTO.getImage());
      preparedStatement.setInt(4, objectDTO.getIdObject());

      preparedStatement.executeQuery();

      ResultSet resultSet = preparedStatement.getResultSet();
      if (!resultSet.next()) {
        return null;
      }

      setObject(objectDTO, resultSet);
      resultSet.close();
    } catch (SQLException e) {
      throw new FatalException(e);
    }

    return objectDTO;
  }

  private void setListObject(PreparedStatement preparedStatement, List<ObjectDTO> objectDTOList) {
    try {
      preparedStatement.executeQuery();

      ResultSet resultSet = preparedStatement.getResultSet();
      while (resultSet.next()) {
        ObjectDTO objectDTO = objectFactory.getObjectDTO();
        setObject(objectDTO, resultSet);
        objectDTOList.add(objectDTO);
      }

      resultSet.close();
      preparedStatement.close();
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }


  private void setObject(ObjectDTO objectDTO, ResultSet resultSet) {
    try {
      objectDTO.setIdObject(resultSet.getInt(1));
      objectDTO.setDescription(resultSet.getString(2));
      objectDTO.setStatus(resultSet.getString(3));
      objectDTO.setImage(resultSet.getBytes(4));
      objectDTO.setIdOfferor(resultSet.getInt(5));
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }

}
