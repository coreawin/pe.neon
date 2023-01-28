/**
 * 
 */
package pe.neon.이종택;

import com.diquest.coreawin.common.divisible.DivisionFileWriter;
import com.diquest.k.patent.h.CompressUtil;
import com.diquest.k.patent.jaxb.PatentJAXBParser2020;
import com.diquest.k.patent.jaxb.PatentTableData2020;
import com.diquest.k.patent.jaxb.bean2020.IPatentDataWriter;
import com.diquest.k.patent.jaxb.bean2020.PTClassification;
import com.diquest.k.patent.jaxb.bean2020.PTDocumentIDInfo;
import com.diquest.k.patent.nosql.MongoDBConnector;
import com.diquest.k.patent.nosql.MongoDB_Option;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

//import com.diquest.coreawin.common.divisible.DivisionFileWriter;

/**
 * @author neon
 * @date 2013. 4. 19.
 * @Version 1.0
 */
public class 이종택_20221027 {
	Logger mongoLogger = LoggerFactory.getLogger(getClass());
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
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
	PatentTableData2020 ptd = PatentTableData2020.getInstance();
	PatentJAXBParser2020 parser = null;

	Map<String, MongoCollection<Document>> collectionPool = new HashMap<String, MongoCollection<Document>>();
	Map<String, Set<String>> auPnos = new HashMap<String, Set<String>>();

	private String[] retrievePnoList(Set<String> pnoList) {
		auPnos.clear();
		StringBuilder buff1 = new StringBuilder();
		StringBuilder buff2 = new StringBuilder();
//		mongoLogger.info("ref pno list : {}" , pnoList);
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
					xml = checkCompress(String.valueOf(d.get(MongoDB_Option.FIELD_XML)), (byte[])d.get("xml_compress"));
//					Binary bi = (Binary) d.get("xml");
//					xml = CompressUtil.getInstance().unCompress(bi.getData());
					InputStream xmlis = new ByteArrayInputStream(xml.getBytes("UTF-8"));
					ptd.processPatentDataInit(parser.unmarshal_type(xmlis));
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
//		mongoLogger.info("reference datas build complete : "+ pnoList.size());
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
			parser = new PatentJAXBParser2020();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		PatentTableData2020 ptd = PatentTableData2020.getInstance();
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

	/**
	 * 일반적으로 XML을 String으로 저장하는데 (속도이슈), BSON 저장 한계 16M라 이보다 큰 파일은 불가피하게 기존처럼 압축해서 보관<br>
	 * 이를 자동으로 처리하기 위한 위한 로직<br>
	 *
	 * @param xml
	 * @return
	 * @since 2022.10
	 */
	private String checkCompress(String xml, byte[] xml_compress) {
		String xml_data = null;
		if (xml == null) {
			// 16M보다 크면...
		} else {
			if (xml.length() < 1000) {
				// 길이가 현저히 적으면... xml_compress를 활용...
			} else {
				return xml;
			}
		}
		if (xml_compress == null) return xml;
		try {
			xml_data = CompressUtil.getInstance().unCompress(xml_compress);
		} catch (IOException e) {
			e.printStackTrace();;
			throw new RuntimeException(e);
		}
		return xml_data;
	}
	 @Test
	public void testWriteParseFromMongoDB() throws Exception {
		String pyear = "2021";
		String folderDate = "20221102";
		String downloadPath = String.format("d:\\data\\이종택\\%s\\", folderDate);
		mongoLogger.info("download path : {}", downloadPath);
		File f = new File(downloadPath);
		f.delete();
		f.mkdirs();
		mongoLogger.info("{}  이종택 mission", folderDate);
		System.setProperty("DEBUG.MONGO", "false");

		mc = MongoDBConnector.getInstance("172.10.200.225", 27017);
//		 mc = MongoDBConnector.getInstance("203.250.207.75", 27017);
		db = mc.getDatabase("KISTI_2022_PATENT");
		MongoCollection<Document> collection = db.getCollection("US");
//		 DBCollection col = db.getCollection("US");
		BasicDBObject doc = new BasicDBObject("pyear", pyear);
		FindIterable<Document> fIter = collection.find(Filters.eq("pyear", pyear)).noCursorTimeout(true);


//		 doc = new BasicDBObject("_id", "US10524410");
//		 FindIterable<Document> fIter = collection.find(doc).noCursorTimeout(true);

		// doc = new BasicDBObject("_id", "USRE046643E");
		// BasicDBList orList = new BasicDBList();
		// orList.add(new BasicDBObject("_id", "JP2005515298A"));
		// orList.add(new BasicDBObject("_id", "JP2004524147A"));
		// orList.add(new BasicDBObject("_id", "JP2002505942T"));
		// orList.add(new BasicDBObject("_id", "CN103567189A"));
		// doc = new BasicDBObject("$or", orList);
		// mongoLogger.info(doc);
//		 doc = new BasicDBObject("_id", "US10524410");
//		 DBCursor cur = collection.find(doc);
		MongoCursor<Document> mCur = fIter.iterator();
		// int total = cur.count();
		long total = collection.count(Filters.eq("pyear", pyear));

//		mongoLogger.info("total : " + total);
		parser = new PatentJAXBParser2020();
		PatentTableData2020 ptd = PatentTableData2020.getInstance();
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
				String n_pndate = String.valueOf(docu.get("pndate"));
				String n_pyear = String.valueOf(docu.get("pyear"));
				String n_authority = String.valueOf(docu.get("authority"));

				String xml = checkCompress(String.valueOf(docu.get(MongoDB_Option.FIELD_XML)), (byte[])docu.get("xml_compress"));
//				 mongoLogger.info(xml);
				InputStream xmlis = new ByteArrayInputStream(xml.getBytes("UTF-8"));
				ptd.processPatentDataInit(parser.unmarshal_type(xmlis));
				String publ_type = ptd.publication.publ_type.toString();
				if ("grant".equalsIgnoreCase(publ_type)) {
					String pno = ptd.publication.pno;
					String pnoFilePath = downloadPath + "xml/" +pno +".xml";
//					mongoLogger.info("pno : {} : {}", pno , pnoFilePath);
//					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(pnoFilePath))));
//					bw.write(xml);
//					bw.close();
//					mongoLogger.info("pno : {}", pno);
//					 mongoLogger.info("xml : {}", xml);
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
//						mongoLogger.info("w1 line : {}", line);
						w1.write(line);
					}

					List<IPatentDataWriter> ipcList = ptd.ipc.mainList;
					ipcList.addAll(ptd.ipc.ipcList);
					String ipclist = removeDupIPC(ipcList);
					w2.write(pno + TAB + ipclist + ENTER);
					String dockind = ptd.publication.publ_type;
//					String pndate = ptd.publication.documentID.date;
//					String pnyear = "";
//					if (pndate != null) {
//						pnyear = pndate.length() > 3 ? pndate.substring(0, 4) : "";
//					}
					String pnkind = ptd.publication.documentID.kind;


					w3.write(pno + TAB + dockind + TAB + n_pyear + TAB + n_pndate + TAB + pnkind + TAB + n_authority + ENTER);
//					mongoLogger.info("w3 line : {}", pno + TAB + dockind + TAB + n_pyear + TAB + n_pndate + TAB + pnkind + TAB + n_authority);
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
						mongoLogger.info("progress grant cnt(390,578) : " + grantCnt + "/390,578");
					}
					if (grantCnt % 10000 == 0) {
						mongoLogger.info("flusing...");
						w1.flush();
						w2.flush();
						w3.flush();
						w4.flush();
						w5.flush();
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