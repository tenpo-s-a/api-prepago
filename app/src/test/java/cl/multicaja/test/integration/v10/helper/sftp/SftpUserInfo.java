package cl.multicaja.test.integration.v10.helper.sftp;

import com.jcraft.jsch.UserInfo;

public class SftpUserInfo implements UserInfo  {
  @Override
  public String getPassphrase() {
    return null;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public boolean promptPassword(String s) {
    return false;
  }

  @Override
  public boolean promptPassphrase(String s) {
    return false;
  }

  @Override
  public boolean promptYesNo(String s) {
    return false;
  }

  @Override
  public void showMessage(String s) {

  }
}
