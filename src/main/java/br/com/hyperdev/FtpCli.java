package br.com.hyperdev;

import br.com.hyperdev.entities.FtpHost;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.ini4j.Ini;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "hyperftp", description = "Ftp Client upload file to hostserver", mixinStandardHelpOptions = true, version = "FTP Client v0.0.5")

public class FtpCli implements Callable<Integer> {

    @Parameters(index = "0", description = "File name to upload")
    private String filename;

    @Parameters(index = "0", description = "What serverhost will connect")
    private String host;

    @Parameters(index = "0", description = "What directory will push file")
    private String directory;

    @Option(names = "-u", description = "Upload file to ftp server")
    private String upload;

    @Option(names = "-h",  description = "This is FTP Server")
    private String server;

    @Option(names = "-p",  description = "This is FTP Server")
    private String path;

    public Integer call() throws Exception {
        FtpHost ftpHost = null;

        if (server != null) {
            Ini ini = new Ini(new File("C:\\Users\\mvanu\\AppData\\Local\\hyperftp\\FtpClient.ini"));
            FtpHost ftpServer = new FtpHost();
            ftpServer.setUsername(ini.get(server, "username"));
            ftpServer.setPassword(ini.get(server, "user_key"));
            ftpServer.setPort(Integer.parseInt(ini.get("port", "port_ftp")));
            ftpServer.setHostname(ini.get("host", "server"));
            ftpHost = ftpServer;
        }

        if (upload != null) {
            try {
                FTPClient client = new FTPClient();
                client.connect(ftpHost.getHostname(), ftpHost.getPort());
                boolean auth = client.login(ftpHost.getUsername(), ftpHost.getPassword());
                if (auth) {
                    System.out.println("Connected success");
                    client.enterLocalPassiveMode();
                    client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
                    client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                    File file = new File(upload);
                    InputStream fileSteam = new FileInputStream(file);

                    if (path != null) {
                        client.storeFile(path+upload, fileSteam);
                        client.sendSiteCommand("chmod 777 "+path+upload);
                    } else {
                        client.storeFile(upload, fileSteam);
                        client.sendSiteCommand("chmod 777 " + upload);
                    }

                    fileSteam.close();
                    client.disconnect();
                    System.out.println("File uploaded success");
                    System.out.println("");
                } else {
                    System.out.println("Auth fail");
                    return 1;
                }

                client.disconnect();
            } catch (IOException e ) {
                System.out.println("-- Connection fail --" );
                System.out.println("Upload file fail:" + e.getMessage());
                return 2;
            }
        }

        return 0;
    }

    public static void main(String... args) {
        int codeExit = new CommandLine(new FtpCli()).execute(args);
        System.exit(codeExit);
    }
}
