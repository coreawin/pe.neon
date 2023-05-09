package pe.neon.이종택;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 사용자 입력 파라미터.
 *
 * @author neon
 */
public class InputParameter {

    String homePath = null;
    String downloadPath = null;
    int publicationYear = LocalDate.now().getYear() - 1;
    String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

    String mongodbIP = "172.10.200.225";
    String mongodbDatabase = "KISTI_2022_PATENT";
    String collectionName = "US";
    int mongodbPort = 27017;

    public InputParameter(String[] args) throws Exception {
        if (args != null) {
            for (String a : args) {
                parseParameter(a);
            }
        }
        verify();
    }

    private void verify() throws Exception {
        if(downloadPath==null){
            this.downloadPath = this.homePath + File.separator;
        }
        System.out.println(toString());
        if (homePath == null) {
            throw new Exception("프로그램 설치 경로를 입력하지 않았습니다. -ho 옵션으로 해당 경로 정보를 입력해주세요.");
        }

        if (downloadPath == null) {
            throw new Exception("다운로드할 데이터 경로를 입력하지 않았습니다. -dn 옵션으로 해당 경로 정보를 입력해주세요.");
        }
    }

    private void parseParameter(String s) {
        String param = s.substring(3);
        if (s.startsWith("-db")) {
            this.mongodbDatabase = param;
        } else if (s.startsWith("-ho")) {
            this.homePath = param;
            System.setProperty("HOME", this.homePath);
        } else if (s.startsWith("-dn")) {
            this.downloadPath = param;
        } else if (s.startsWith("-py")) {
            this.publicationYear = Integer.parseInt(param);
        } else if (s.startsWith("-mi")) {
            this.mongodbIP = param;
        } else if (s.startsWith("-cl")) {
            this.collectionName = param;
        } else if (s.startsWith("-mp")) {
            this.mongodbPort = Integer.parseInt(param);
        }
    }

    StringBuilder sb = new StringBuilder();

    public String toString() {
        sb.setLength(0);
        sb.append("Install Path  : ");
        sb.append(homePath);
        sb.append("\n");
        sb.append("currentDate  : ");
        sb.append(currentDate);
        sb.append("\n");
        sb.append("downloadPath  : ");
        sb.append(downloadPath);
        sb.append("\n");
        sb.append("publicationYear  : ");
        sb.append(this.publicationYear);
        sb.append("\n");
        sb.append("mongoDB IP : ");
        sb.append(this.mongodbIP);
        sb.append("\n");
        sb.append("mongoDB PORT : ");
        sb.append(this.mongodbPort);
        sb.append("\n");
        sb.append("mongoDB COLLECTION : ");
        sb.append(this.collectionName);
        sb.append("\n");
        return sb.toString();
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getHomePath() {
        return homePath;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public String getMongodbIp() {
        return mongodbIP;
    }

    public int getMongodbPort() {
        return mongodbPort;
    }

    public String getMongodbDatabase() {
        return mongodbDatabase;
    }

}
