package pe.neon.hkchoi;

import com.diquest.coreawin.common.divisible.DivisionFileWriter;
import com.diquest.scopus.schema.bean.ani515.DocTp;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.kisti.mirian.nosql.conn.MongoDBConnector;
import re.kisti.mirian.util.CompressUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringReader;
import java.text.NumberFormat;

/**
 * 최현규 2023.01.19 요청
 * <p>
 * 이름 | Scopus 저자 ID | Occid id 형태로 데이터 추출.
 */
public class DownloadScopusAuthorOccidData {

    Author_GroupTable table = new Author_GroupTable();

    JAXBContext jc= null;

    protected JAXBElement<?> unmarshall(String xml, boolean sec) throws Exception {
        JAXBElement<?> obj = null;
        InputStream stream = null;
        try {
            obj = (JAXBElement<?>) unmarshaller.unmarshal(new StringReader(xml));
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                }
            }
        }
        return obj;
    }

    private void initMarshaller(){
        try {
            jc = JAXBContext.newInstance(SCOPUS_PACKAGE);
            this.unmarshaller = jc.createUnmarshaller();
        }catch(Exception e){
            e.printStackTrace();;
        }
    }

    public String extractOccid(String xml) {
        try {
            JAXBElement<?> obj = null;
            DocTp root = null;
            String eid = "";
            try {

                obj = unmarshall(xml, true);
                root = (DocTp) obj.getValue();
                eid = root.getMeta().getEid();
                table.initValues(root);
                if (table.existOrcId()) {
                    logger.info("eid : {} / {}", eid, table.toString4최현규());
                    return table.toString4최현규();
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("===== SCOPUSLoader.class loadFile");
                logger.error("obj " + obj);
                logger.error("root " + root);
                logger.error("root.getMeta() " + root.getMeta());
                logger.error("root.getMeta().getEid() " + root.getMeta().getEid());
                logger.error("eid : [" + eid + "] " + e.getMessage(), e);
            } finally {
                obj = null;
                root = null;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    protected static String SCOPUS_PACKAGE = "com.diquest.scopus.schema.bean.ani515";
    protected static String SCOPUS_PACKAGE2 = "com.diquest.scopus.schema.bean.ani515.cite";

    protected Unmarshaller unmarshaller;

    public void connectMongoDB(String path) {
        initMarshaller();
        MongoClient client = null;
        long total = 0;
        long counter = 0;
        long findOrcidCounter = 0;
        long gte2010 = 0;
        try {
//            Document findDoc = new Document("pubyear", new Document("$gte", 2010));
            w1 = new DivisionFileWriter(path);
            client = MongoDBConnector.getInstance("203.250.207.76", 27017);
            MongoDatabase mongodb = client.getDatabase("KISTI_2019_SCOPUS");
            MongoCollection<Document> collection = mongodb.getCollection("SCOPUS");
            total = collection.count();
            logger.info("탐색 전체 갯수 : {}", total);
//            System.out.println("탐색 전체 갯수 : " + total);
            FindIterable<Document> cur = collection.find();
            cur.noCursorTimeout(true);
            for (Document doc : cur) {
                if (counter % 10000 == 0) {
                    logger.info(String.format("SCOPUS 데이터 진행 확인  orcid 발견 건수 : %s / 진행건수 : %s / 총건수 : %s / 2015년 이후 건수 : %s", NumberFormat.getInstance().format(findOrcidCounter), NumberFormat.getInstance().format(counter), NumberFormat.getInstance().format(total), NumberFormat.getInstance().format(gte2010)));
//                    System.out.println(String.format("SCOPUS 데이터 진행 확인  orcid 발견 건수 : %s / 진행건수 : %s / 총건수 : %s / 2015년 이후 건수 : %s", NumberFormat.getInstance().format(findOrcidCounter), NumberFormat.getInstance().format(counter), NumberFormat.getInstance().format(total), NumberFormat.getInstance().format(gte2010)));

                }

                Binary b = (Binary) (doc.get("xml"));
                int pubyear = 0;
                try {
                    pubyear = Integer.valueOf(String.valueOf(doc.get("pubyear")));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                counter += 1;
                if (pubyear < 2015) {
                    continue;
                }
                gte2010 += 1;
                String xml = CompressUtil.getInstance().unCompress(b.getData());
                String resultData = extractOccid(xml);
                if (resultData != null) {
                    w1.write(resultData);
                    findOrcidCounter += 1;
                }
                if (gte2010 % 10000 == 0) {
                    logger.info(String.format("SCOPUS 데이터 진행 확인  orcid 발견 건수 : %s / 진행건수 : %s / 총건수 : %s / 2015년 이후 건수 : %s", NumberFormat.getInstance().format(findOrcidCounter), NumberFormat.getInstance().format(counter), NumberFormat.getInstance().format(total), NumberFormat.getInstance().format(gte2010)));
//                    System.out.println(String.format("SCOPUS 데이터 진행 확인  orcid 발견 건수 : %s / 진행건수 : %s / 총건수 : %s / 2015년 이후 건수 : %s", NumberFormat.getInstance().format(findOrcidCounter), NumberFormat.getInstance().format(counter), NumberFormat.getInstance().format(total), NumberFormat.getInstance().format(gte2010)));

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(String.format("SCOPUS 데이터 추출 완료  o  rcid 발견 건수 : %s / 진행건수 : %s / 총건수 : %s / 2015년 이후 건수 : %s ", NumberFormat.getInstance().format(findOrcidCounter), NumberFormat.getInstance().format(counter), NumberFormat.getInstance().format(total), NumberFormat.getInstance().format(gte2010)));
//        System.out.println(String.format("SCOPUS 데이터 추출 완료  o  rcid 발견 건수 : %s / 진행건수 : %s / 총건수 : %s / 2015년 이후 건수 : %s ", NumberFormat.getInstance().format(findOrcidCounter), NumberFormat.getInstance().format(counter), NumberFormat.getInstance().format(total), NumberFormat.getInstance().format(gte2010)));

    }

    DivisionFileWriter w1 = null;
    Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String... args) throws Exception {

        System.out.println("executor " + args.length);

        if (args.length > 0) {
            System.out.println("===> " + args[0].trim());
            new DownloadScopusAuthorOccidData().connectMongoDB(args[0].trim());
        } else {
            String path = "d:\\data\\27.최현규\\authorData\\scopus_occid_data_202301.txt";
            new DownloadScopusAuthorOccidData().connectMongoDB(path);
        }
    }

}
