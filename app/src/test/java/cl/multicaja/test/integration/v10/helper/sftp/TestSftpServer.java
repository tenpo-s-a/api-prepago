package cl.multicaja.test.integration.v10.helper.sftp;

import cl.multicaja.prepaid.helpers.mastercard.MastercardFileHelper;
import com.jcraft.jsch.*;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestSftpServer {

  private static TestSftpServer INSTANCE;
  private final SshServer sshd;
  public static String BASE_DIR = "src/test/resources/";
  private String HOST_NAME = "localhost";
  private final Integer PORT = 7001;

  private String user = "test";
  private String pass = "test";

  private TestSftpServer() {
    sshd = SshServer.setUpDefaultServer();
    sshd.setPort(PORT);
    sshd.setHost(HOST_NAME);
    sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
      @Override
      public boolean authenticate(String username, String password, ServerSession session) {
        return user.equals(username) && pass.equals(password);
      }
    });

    sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {
      @Override
      public boolean authenticate(String s, PublicKey publicKey, ServerSession serverSession) {
        return false;
      }
    });
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
    sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/sh", "-i", "-l" }));
    sshd.setCommandFactory(new ScpCommandFactory());
    sshd.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(new SftpSubsystem.Factory()));
  }

  private static void createInstance() {
    if (INSTANCE == null) {
      synchronized(MastercardFileHelper.class) {
        if (INSTANCE == null) {
          INSTANCE = new TestSftpServer();
        }
      }
    }
  }

  public static TestSftpServer getInstance() {
    if (INSTANCE == null) {
      createInstance();
    }
    return INSTANCE;
  }

  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  public void createDirectories() throws Exception {
    Map<String, Object> context = openChanel();
    ChannelSftp channelSftp = (ChannelSftp) context.get("channel");
    createIfNotExists(channelSftp, "mastercard", "mastercard/T058", "mastercard/T058/done", "mastercard/T058/error","multicajared","multicajared/done","multicajared/error");
    channelSftp.exit();
    ((Session) context.get("session")).disconnect();
  }

  public void start() throws Exception {
    sshd.start();

  }

  public void end() throws Exception {
    if(sshd != null){
      sshd.close(true);
    }
  }

  private void createIfNotExists(ChannelSftp channel, final String ... folders) throws SftpException {
    for (String folder: folders) {
      try{
        channel.ls(BASE_DIR + folder);
      } catch (SftpException ex) {
        channel.mkdir(BASE_DIR + folder);
      }
    }
  }

  public Map<String, Object> openChanel() throws Exception {
    final Session session = new JSch().getSession(user, HOST_NAME, PORT);
    session.setPassword(pass);
    session.setUserInfo(new SftpUserInfo());
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);
    session.connect();
    Channel channel = session.openChannel("sftp");
    channel.connect();
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("session", session);
    context.put("channel", channel);
    return context;
  }

}
