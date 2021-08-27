/**
 * 
 */
package pe.neon.이종택;

import com.diquest.coreawin.common.divisible.DivisionFileWriter;
import com.diquest.k.patent.h.CompressUtil;
import com.diquest.k.patent.jaxb.PatentJAXBParser;
import com.diquest.k.patent.jaxb.PatentTableData;
import com.diquest.k.patent.jaxb.bean.IPatentDataWriter;
import com.diquest.k.patent.jaxb.bean.PTClassification;
import com.diquest.k.patent.jaxb.bean.PTDocumentIDInfo;
import com.diquest.k.patent.nosql.MongoDBConnector;
import com.diquest.k.patent.nosql.MongoDB_Option;
import com.diquest.k.patent.util.EDQDocField;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.Binary;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//import com.diquest.coreawin.common.divisible.DivisionFileWriter;

/**
 * @author neon
 * @date 2013. 4. 19.
 * @Version 1.0
 */
public class 이종택_20200701 {
	Logger mongoLogger = null;
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		mongoLogger = Logger.getLogger("com.mongodb");
		mongoLogger.setLevel(Level.SEVERE);
		System.setProperty("KEYWORD_EXTRACT_HOME", "T:\\workspace\\public\\KISTI_PATENT-TECH-SENSING_2013_KEYWORD");
		MongoDB_Option._DQDOCDIR = "d:\\data\\dqdoc";
	}

	/**
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		mongoLogger.info("자원 종료");
		mc.close();
	}

	public static Set<String> countrySet = new LinkedHashSet<String>();
	static {
		countrySet.add("AR");
		countrySet.add("AT");
		countrySet.add("AU");
		countrySet.add("BE");
		countrySet.add("BR");
		countrySet.add("CA");
		countrySet.add("CH");
		countrySet.add("CN");
		countrySet.add("DD");
		countrySet.add("DE");
		countrySet.add("DK");
		countrySet.add("EA");
		countrySet.add("EP");
		countrySet.add("ES");
		countrySet.add("FI");
		countrySet.add("FR");
		countrySet.add("GB");
		countrySet.add("IE");
		countrySet.add("IN");
		countrySet.add("IT");
		countrySet.add("JP");
		countrySet.add("KR");
		countrySet.add("LU");
		countrySet.add("MC");
		countrySet.add("MX");
		countrySet.add("NL");
		countrySet.add("PT");
		countrySet.add("TW");
		countrySet.add("RU");
		countrySet.add("SE");
		countrySet.add("SU");
		countrySet.add("US");
		countrySet.add("WO");
	}

	MongoClient mc = null;

	StringBuffer buf = new StringBuffer();
	Set<String> datas1 = new LinkedHashSet<String>();

	private String removeDupIPC(List<IPatentDataWriter> list) {
		datas1.clear();
		for (IPatentDataWriter ipdw : list) {
			PTClassification ipc = (PTClassification) ipdw;
			datas1.add(ipc.getCodeFormatter());
		}
		buf.setLength(0);
		for (String _s : datas1) {
			buf.append(_s);
			buf.append(";");
		}
		if (buf.length() > 0) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	MongoDatabase db = null;
	PatentTableData ptd = PatentTableData.getInstance();
	PatentJAXBParser parser = null;

	Map<String, MongoCollection<Document>> collectionPool = new HashMap<String, MongoCollection<Document>>();
	Map<String, Set<String>> auPnos = new HashMap<String, Set<String>>();

	private String[] retrievePnoList(Set<String> pnoList) {
		auPnos.clear();
		StringBuilder buff1 = new StringBuilder();
		StringBuilder buff2 = new StringBuilder();
		for (String pno : pnoList) {
			String au = pno.substring(0, 2);
			Set<String> sets = auPnos.get(au);
			if (sets == null) {
				sets = new HashSet<String>();
			}
			sets.add(pno);
			auPnos.put(au, sets);
		}

		Set<String> ks = auPnos.keySet();
		for (String au : ks) {
			MongoCollection<Document> col = collectionPool.get(au.toUpperCase());
			if (col == null) {
				col = db.getCollection(au.toUpperCase());
				collectionPool.put(au.toUpperCase(), col);
			}
			Set<String> pnoSet = auPnos.get(au);
			BasicDBList orList = new BasicDBList();
			for (String _s : pnoSet) {
				orList.add(new BasicDBObject("_id", _s.trim()));
				// mongoLogger.info(_s);
			}
			BasicDBObject doc = new BasicDBObject("$or", orList);
//			mongoLogger.info("REFERENCE INFO " + au + "====> " + col.count(doc));
			FindIterable<Document> fIter = col.find(doc).noCursorTimeout(true);

			MongoCursor<Document> mCur = fIter.iterator();
			while (mCur.hasNext()) {
				// DBObject d = cur.next();
				Document d = mCur.next();
				String xml;
				try {
					// xml = CompressUtil.getInstance().unCompress((byte[])
					// d.get("xml"));

					Binary bi = (Binary) d.get("xml");
					xml = CompressUtil.getInstance().unCompress(bi.getData());
					InputStream xmlis = new ByteArrayInputStream(xml.getBytes("UTF-8"));
					ptd.processPatentDataInit(parser.unmarshal(xmlis));
					String pno = ptd.publication.pno;
					List<IPatentDataWriter> ipcList = ptd.ipc.mainList;
					ipcList.addAll(ptd.ipc.ipcList);
					String ipclist = removeDupIPC(ipcList);
					buff1.append(pno + TAB + ipclist + ENTER);
					String dockind = ptd.publication.publ_type;
					String pndate = ptd.publication.documentID.date;
					String pnyear = "";
					if (pndate != null) {
						pnyear = pndate.length() > 3 ? pndate.substring(0, 4) : "";
					}
					String pnkind = ptd.publication.documentID.kind;
					String authority = ptd.publication.documentID.country_code;
					buff2.append(pno + TAB + dockind + TAB + pnyear + TAB + pndate + TAB + pnkind + TAB + authority + ENTER);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		mongoLogger.info("reference datas build complete : "+ pnoList.size());
		return new String[] { buff1.toString(), buff2.toString() };
	}
	final String TAB = "\t";
	final String ENTER = "\r\n";

//	@Test
	public void testReadPno() throws IOException {
		String fileName = "d:\\data\\us.grant.2017.backward.1.txt";
		BufferedReader br = null;
		mc = MongoDBConnector.getInstance("203.250.207.75", 27017);
		db = mc.getDatabase("KISTI_2017_PATENT");
		try {
			parser = new PatentJAXBParser();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		PatentTableData ptd = PatentTableData.getInstance();
		Set<String> set = new TreeSet<String>();
		DivisionFileWriter w4 = new DivisionFileWriter("d:\\data\\us.grant.2017.ref.ipc.txt");
		DivisionFileWriter w5 = new DivisionFileWriter("d:\\data\\us.grant.2017.ref.publication.txt");
		try {
			String line = null;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
			while ((line = br.readLine()) != null) {
				try {
					String[] lines = line.split(TAB);
					String refNo = lines[1].trim();
					set.add(refNo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			int totalCnt = set.size();
			mongoLogger.info("TotalCnt " + totalCnt);
			int cnt = 1;
			Set<String> _tmp = new TreeSet<String>();
			for (String datas : set) {
				_tmp.add(datas);
				if (cnt % 500 == 0) {
					String[] refInfos = retrievePnoList(_tmp);
					w4.write(refInfos[0]);
					w5.write(refInfos[1]);
					w4.flush();
					w5.flush();
					mongoLogger.info("progress : " + cnt + "/"+totalCnt);
					_tmp.clear();
				}
				cnt++;
			}
			if(_tmp.size()>0){
				String[] refInfos = retrievePnoList(_tmp);
				w4.write(refInfos[0]);
				w5.write(refInfos[1]);
				w4.flush();
				w5.flush();
				mongoLogger.info("progress : " + (cnt + _tmp.size()) + "/+totalCnt");
				_tmp.clear();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (w4 != null) {
				w4.flush();
				w4.close();
			}
			if (w5 != null) {
				w5.flush();
				w5.close();
			}
		}
	}

	 @Test
	public void testWriteParseFromMongoDB() throws Exception {
		String pyear = "2019";
		String folderDate = "20200701";
		mongoLogger.info("20190701  이종택 mission");
		System.setProperty("DEBUG.MONGO", "false");
		mc = MongoDBConnector.getInstance("203.250.207.75", 27017);
		db = mc.getDatabase("KISTI_2017_PATENT");
		MongoCollection<Document> collection = db.getCollection("US");
		// DBCollection col = db.getCollection("US");
		BasicDBObject doc = new BasicDBObject("pyear", pyear);
		FindIterable<Document> fIter = collection.find(Filters.eq("pyear", pyear)).noCursorTimeout(true);
		// doc = new BasicDBObject("_id", "USRE046643E");
		// BasicDBList orList = new BasicDBList();
		// orList.add(new BasicDBObject("_id", "JP2005515298A"));
		// orList.add(new BasicDBObject("_id", "JP2004524147A"));
		// orList.add(new BasicDBObject("_id", "JP2002505942T"));
		// orList.add(new BasicDBObject("_id", "CN103567189A"));
		// doc = new BasicDBObject("$or", orList);
		// mongoLogger.info(doc);
		// BasicDBObject doc = new BasicDBObject("_id", "EP1923457A1");
		// DBCursor cur = col.find(doc);
		MongoCursor<Document> mCur = fIter.iterator();
		// int total = cur.count();
		long total = collection.count(Filters.eq("pyear", pyear));

		mongoLogger.info("total : " + total);
		parser = new PatentJAXBParser();
		PatentTableData ptd = PatentTableData.getInstance();
		DivisionFileWriter w1 = new DivisionFileWriter(String.format("d:\\data\\이종택\\%s\\us.grant.%s.backward.txt", folderDate, pyear));
		DivisionFileWriter w2 = new DivisionFileWriter(String.format("d:\\data\\이종택\\%s\\us.grant.%s.ipc.txt", folderDate, pyear));
		DivisionFileWriter w3 = new DivisionFileWriter(String.format("d:\\data\\이종택\\%s\\us.grant.%s.publication.txt", folderDate, pyear));
		DivisionFileWriter w4 = new DivisionFileWriter(String.format("d:\\data\\이종택\\%s\\us.grant.%s.ref.ipc.txt", folderDate, pyear));
		DivisionFileWriter w5 = new DivisionFileWriter(String.format("d:\\data\\이종택\\%s\\us.grant.%s.ref.publication.txt", folderDate, pyear));
		int cnt = 0;
		int grantCnt = 0;
		while (mCur.hasNext()) {
			// DBObject d = cur.next();
			Document docu = mCur.next();
			try {
				Binary bi = (Binary) docu.get("xml");
				String xml = CompressUtil.getInstance().unCompress(bi.getData());
				// mongoLogger.info(xml);
				InputStream xmlis = new ByteArrayInputStream(xml.getBytes("UTF-8"));
				ptd.processPatentDataInit(parser.unmarshal(xmlis));
				String publ_type = ptd.publication.publ_type.toString();
				if ("grant".equalsIgnoreCase(publ_type)) {
					String pno = ptd.publication.pno;
					// mongoLogger.info(pno);
					// mongoLogger.info(pno +"\t" + ptd.citations.toString());
					// w1.write(pno +"\t"+ptd.publication.documentID.date+"\n");
					List<IPatentDataWriter> citedList = ptd.citations.citedList;
					Set<String> refList = new TreeSet<String>(ptd.citations.checkBackward);

					for (IPatentDataWriter s : citedList) {
						/** backwardData */
						PTDocumentIDInfo p = (PTDocumentIDInfo) s;
						String refDocPno = p.getNo();
						String pubdate = p.date;
						String appdate = p.app_date;
						String srepPhase = p.srepPhase;
						String line = pno + TAB + refDocPno + TAB + pubdate + TAB + appdate + TAB + srepPhase + ENTER;
						w1.write(line);
					}

					List<IPatentDataWriter> ipcList = ptd.ipc.mainList;
					ipcList.addAll(ptd.ipc.ipcList);
					String ipclist = removeDupIPC(ipcList);
					w2.write(pno + TAB + ipclist + ENTER);
					String dockind = ptd.publication.publ_type;
					String pndate = ptd.publication.documentID.date;
					String pnyear = "";
					if (pndate != null) {
						pnyear = pndate.length() > 3 ? pndate.substring(0, 4) : "";
					}
					String pnkind = ptd.publication.documentID.kind;
					String authority = ptd.publication.documentID.country_code;
					w3.write(pno + TAB + dockind + TAB + pnyear + TAB + pndate + TAB + pnkind + TAB + authority + ENTER);

					 String[] refInfos = retrievePnoList(refList);
					 w4.write(refInfos[0]);
					 w5.write(refInfos[1]);

					// mongoLogger.info(ptd.citations.citedList.toString());
					// for(String s : sets){
					// mongoLogger.info("backwardSet " + s);
					// // w2.write(pno +"\t" + s +"\n");
					// }
					//
					// Set<String> sets2 = ptd.national.nationalSet;
					// for(String s : sets2){
					// w3.write(pno +"\t" + s +"\n");
					// }

					// mongoLogger.info(ptd.application.lineData());
					// mongoLogger.info(ptd.title.lineData());
					if (grantCnt % 1000 == 0) {
						mongoLogger.info("progress grantCnt(332,483) : " + grantCnt + "/332,483");
					}
					grantCnt++;
				}
				if (cnt % 1000 == 0) {
					mongoLogger.info("progress : " + cnt + "/" + total);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			cnt += 1;
		}
		w1.flush();
		w2.flush();
		w3.flush();
		w4.flush();
		w5.flush();
		//
		w1.close();
		w2.close();
		w3.close();
		w4.close();
		w5.close();
		mc.close();
		mongoLogger.info("완전하게 종료.");
	}

	BufferedWriter bw = null;

	private void writeXML(String target, String pno, String xml) throws IOException {
		try {
			File f = new File(target);
			f.mkdirs();
			bw = new BufferedWriter(new FileWriter(new File(target + File.separator + pno + ".xml")));
			bw.write(xml);
		} catch (IOException e) {
			throw e;
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
	}

}