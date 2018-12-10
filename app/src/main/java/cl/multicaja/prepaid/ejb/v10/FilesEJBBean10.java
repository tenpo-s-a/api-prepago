package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 **/
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class FilesEJBBean10 extends PrepaidBaseEJBBean10 implements FilesEJB10 {

  private static Log log = LogFactory.getLog(FilesEJBBean10.class);

  private final String APP_NAME = "api-prepaid";

  private UserClient userClient;

  @Override
  public UserClient getUserClient() {
    if(userClient == null) {
      userClient = UserClient.getInstance();
    }
    return userClient;
  }

  @Override
  public Map<String, Object> info() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    return map;
  }

  /**
    APP FILES
   */
  @Override
  public List<AppFile> getAppFiles(Map<String, Object> headers, Long id, String name, String version, AppFileStatus status) throws Exception {
    // si viene algun parametro en null se establece NullParam
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      name != null ? name : new NullParam(Types.VARCHAR),
      version != null ? version : new NullParam(Types.VARCHAR),
      status != null ? status.toString() : new NullParam(Types.VARCHAR),
    };

    // se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para
    // transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {

      AppFile af = new AppFile();
      af.setId(getNumberUtils().toLong(row.get("_id"), null));
      af.setName(String.valueOf(row.get("_name")));
      af.setVersion(String.valueOf(row.get("_version")));
      af.setDescription(String.valueOf(row.get("_description")));
      af.setMimeType(String.valueOf(row.get("_mime_type")));
      af.setLocation(String.valueOf(row.get("_location")));
      af.setStatus(AppFileStatus.valueOfEnum(String.valueOf(row.get("_status"))));

      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp) row.get("_created_at"));
      timestamps.setUpdatedAt((Timestamp) row.get("_updated_at"));

      af.setTimestamps(timestamps);

      return af;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_get_app_file_v10", rm, params);

    List res = (List) resp.get("result");
    return res != null ? res : Collections.EMPTY_LIST;
  }

  /**
    USER FILES
   */
  @Override
  public List<UserFile> getUsersFile(Map<String, Object> headers, Long id, Long userId, String name, String version, UserFileStatus status) throws Exception {
    return this.getUserClient().getUserFiles(headers, userId, APP_NAME, name, version, status);
  }

  @Override
  public UserFile getUserFileById(Map<String, Object> headers, Long userIdMc, Long id) throws Exception {
    return this.getUserClient().getUserFileById(headers, userIdMc, id);
  }

  @Override
  public UserFile createUserFile(Map<String, Object> headers, Long userId, Long appFileId, String name, String version, String description, String mimeType, String location) throws Exception {

    UserFile file = new UserFile();
    file.setUserId(userId);
    file.setApp(APP_NAME);
    file.setName(name);
    file.setVersion(version);
    file.setDescription(description);
    file.setMimeType(mimeType);
    file.setLocation(location);

    return this.getUserClient().createUserFile(headers, userId, file);
  }
}
