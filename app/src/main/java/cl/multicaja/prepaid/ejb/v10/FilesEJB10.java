package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.helpers.users.model.AppFile;
import cl.multicaja.prepaid.helpers.users.model.AppFileStatus;
import cl.multicaja.prepaid.helpers.users.model.UserFile;
import cl.multicaja.prepaid.helpers.users.model.UserFileStatus;

import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 **/
public interface FilesEJB10 {

  /**
   * Retorna info del ejb, usado para monitoreo, ping, etc
   *
   * @return
   * @throws Exception
   */
  Map<String, Object> info() throws Exception;

  /**
   * Busca los archivos de aplicacion de forma dinamica y genera una lista
   *
   * @param headers
   * @param id
   * @param name
   * @param version
   * @param status
   * @return
   * @throws Exception
   */
  List<AppFile> getAppFiles(Map<String, Object> headers, Long id, String name, String version, AppFileStatus status) throws Exception;

  /**
   * Busca los archivos de usuario de forma dinamica y genera una lista
   *
   * @param headers
   * @param id
   * @param userId
   * @param name
   * @param version
   * @param status
   * @return
   * @throws Exception
   */
  List<UserFile> getUsersFile(Map<String, Object> headers, Long id, Long userId, String name, String version, UserFileStatus status) throws Exception;

  /**
   *  Busca un archivo de usuario por id
   * @param headers
   * @param userIdMc
   * @param id
   * @return
   * @throws Exception
   */
  UserFile getUserFileById(Map<String, Object> headers, Long userIdMc, Long id) throws Exception;

  /**
   * Agrega un nuevo archivo al usuario
   * @param headers
   * @param userId
   * @param appFileId
   * @param name
   * @param version
   * @param description
   * @param mimeType
   * @param location
   * @return
   * @throws Exception
   */
  UserFile createUserFile(Map<String, Object> headers, Long userId, Long appFileId, String name, String version, String description, String mimeType, String location) throws Exception;

}
