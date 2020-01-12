package pe.neon.여운동;

import kr.co.tqk.db.ConnectionFactoryBak;
import pe.neon.FileUtil;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ExtractData_201908 {


    /**
     * 키워드 | 키워드별 전체 MeanYear |  국가별논문건수 .... | <br>
     *
     * @20180814
     * @정승한
     */
    public void countKeywordMeanYearPerCountry() {
        String query_CNList = "SELECT DISTINCT country_code FROM  NYEO2019_SCOPUS_AKCYDATA WHERE LENGTH(country_code) > 1 ORDER BY country_code";

        String query_KeywordMeanYearCn = "SELECT ay.KEYWORD, totalCnt, MEANYEAR, COUNTRY_CODE, 국가별키워드건수 AS cnCnt " +
                "   FROM NYEO2019_SCOPUS_AKFMEANYEAR ay, NYEO2019_SCOPUS_AKCYDATA ac " +
                "   WHERE ay.KEYWORD = ac.KEYWORD" +
                "   ORDER BY ay.keyword";

        Set<String> countryList = new TreeSet<String>();


        FileUtil fu = new FileUtil();
        ExtractKCountData.ConnectionFactoryBak fac = ExtractKCountData.ConnectionFactoryBak.getInstance();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = fac.getConnection();
            pstmt = conn.prepareStatement(query_CNList);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                countryList.add(rs.getString("country_code").trim());
            }

            Map<String, MeanYearCnt> result = new TreeMap<String, MeanYearCnt>();

            pstmt.clearParameters();
            rs.clearWarnings();

            pstmt = conn.prepareStatement(query_KeywordMeanYearCn);
            rs = pstmt.executeQuery();
            String prevKeyword = null;
            Map<String, Integer> map = null;
            MeanYearCnt myc = null;
            while (rs.next()) {
                int totalCnt = rs.getInt(2);
                double meanYear = rs.getDouble(3);
                String country_code = rs.getString(4);
                int cnCnt = rs.getInt(5);
                String keyword = rs.getString(1);

                if (result.containsKey(keyword)) {
                    myc = result.get(keyword);
                    int cnt = 1;
                    if (map.containsKey(country_code)) {
                        cnt = map.get(country_code) + 1;
                    }
                    map.put(country_code, cnt);
                } else {
                    myc = new MeanYearCnt();
                    myc.totalCnt = totalCnt;
                    myc.meanYear = meanYear;
                    myc.keyword = keyword;
                    map = myc.cnCount;
                    int cnt = 1;
                    if (map.containsKey(country_code)) {
                        cnt = map.get(country_code) + 1;
                    }
                    map.put(country_code, cnt);
                }
                myc.cnCount = map;

                result.put(keyword, myc);
            }

            BufferedWriter writer = null;

            try {
                final String TAB = "\t";
                final String ENTER = "\n";
                writer = fu.getWriter("d:\\data\\yeo\\20191102\\키워드별MeanYear_소분류별_100등이상10건이상.txt");
                writer.write("KEYWORD\t키워드총건수\tMEANYEAR\t");
                for (String cn : countryList) {
                    writer.write(cn);
                    writer.write(TAB);
                }
                writer.write(ENTER);
                for (String keyword : result.keySet()) {
                    MeanYearCnt _myc = result.get(keyword);
                    writer.write(_myc.keyword);
                    writer.write(TAB);
                    writer.write(String.valueOf(_myc.totalCnt));
                    writer.write(TAB);
                    writer.write(String.valueOf(_myc.meanYear));
                    writer.write(TAB);

                    Map<String, Integer> cnCntMap = _myc.cnCount;
                    for (String cn : countryList) {
                        String cnt = "0";
                        if (cnCntMap.containsKey(cn)) {
                            cnt = String.valueOf(cnCntMap.get(cn));
                        }
                        writer.write(cnt);
                        writer.write(TAB);
                    }
                    writer.write(ENTER);
                    System.out.println("===> " + result.get(keyword));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fac.release(rs, pstmt, conn);
        }


    }

    public class MeanYearCnt {
        public String keyword;
        public int totalCnt;
        public double meanYear;
        public Map<String, Integer> cnCount = new TreeMap<String, Integer>();

        public String toString() {
            return keyword + "\t" + meanYear + "\t" + cnCount.toString();
        }

    }

    static SortedSet<String> _countryCodeSet = null;
    static Set<String> _keywordSet = null;

    static {
        _countryCodeSet = getCountryCodeList();
        System.out.println("국가코드 목록을 가져왔다. " + _countryCodeSet.size());
        _keywordSet = getKeywordList();
        System.out.println("키워드 목록을 가져왔다. " + _keywordSet.size());
    }

    /**
     * 필요한 국가코드 정보만을 가져온다. <br>
     *
     * @return
     * @since 2019.08.26
     */
    public static SortedSet<String> getCountryCodeList() {
        SortedSet<String> cclist = new TreeSet<String>();
        String sql = "SELECT DISTINCT country_code FROM NYEO2019_FILTER_KEY_CN_FC ORDER BY country_code";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ConnectionFactoryBak fac =null;
        try {
            fac = ConnectionFactoryBak.getInstance();
            conn = fac.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                cclist.add(rs.getString(1).trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fac.release(rs, pstmt, conn);
        }
        return cclist;
    }

    /**
     * 필요한 키워드 정보만을 가져온다. <br>
     *
     * @return
     * @since 2019.08.26
     */
    public static Set<String> getKeywordList() {
        Set<String> keywordlist = new LinkedHashSet<String>();
        String sql = "SELECT * FROM NYEO2019_SCOPUS_FILTERING_AKEY";
        ConnectionFactoryBak fac =null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            fac = ConnectionFactoryBak.getInstance();
            conn = fac.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                keywordlist.add(rs.getString(1).trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fac.release(rs, pstmt, conn);
        }
        return keywordlist;
    }

    public enum DATAKEYTYPE {
        CBYKEY, PBYKEY, SLOPE
    }

    public static class DataKeyByCo {
        public String pbykeybyco;
        public String cbykeybyco;
        public String slope;

        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("pbykeybyco : ");
            buf.append(pbykeybyco);
            buf.append("\tcbykeybyco : ");
            buf.append(cbykeybyco);
            buf.append("\tslope : ");
            buf.append(slope);
            return buf.toString();
        }
    }

    public void write키워드국가별피인용수() throws Exception {
        Map<String, Map<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo>> result = new HashMap<String, Map<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo>>();
        System.out.println("keyword sized : " + _keywordSet.size());
        System.out.println("_countryCodeSet sized : " + _countryCodeSet.size());

        String cnsql = "SELECT *  FROM NYEO2019_FILTER_KEY_CN_FC   order by keyword";
        ConnectionFactoryBak fac = ConnectionFactoryBak.getInstance();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = fac.getConnection();
            pstmt = conn.prepareStatement(cnsql);
            rs = pstmt.executeQuery();

            Map<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo> subResult = new HashMap<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo>();
            while (rs.next()) {
                String keyword = rs.getString("KEYWORD").trim();
                String cc = rs.getString("COUNTRY_CODE").trim();
                String pbykeybyco = rs.getString("PBYKEYBYCO").trim();
                String cbyKeybyco = rs.getString("CBYKEYBYCO").trim();

                if (result.containsKey(keyword)) {
                    subResult = result.get(keyword);
                } else {
                    subResult = new HashMap<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo>();
                }
                pe.neon.최원준.ExtractKCountData.DataKeyByCo data = null;
                if (subResult.containsKey(cc)) {
                    data = subResult.get(cc);
                } else {
                    data = new pe.neon.최원준.ExtractKCountData.DataKeyByCo();
                }
                data.cbykeybyco = cbyKeybyco;
                data.pbykeybyco = pbykeybyco;

                subResult.put(cc, data);
                result.put(keyword, subResult);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fac.release(rs, pstmt, conn);
        }

        try {
            String slopesql = "SELECT *  FROM NYEO2019_FILTER_KEY_CN_SLOPE order by keyword";
            conn = fac.getConnection();
            pstmt = conn.prepareStatement(slopesql);
            rs = pstmt.executeQuery();

            Map<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo> subResult = new HashMap<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo>();
            while (rs.next()) {
                String keyword = rs.getString("KEYWORD");
                String cc = rs.getString("COUNTRY_CODE");
                String slope = rs.getString("SLOPE");

                if (result.containsKey(keyword)) {
                    subResult = result.get(keyword);
                } else {
                    subResult = new HashMap<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo>();
                }
                pe.neon.최원준.ExtractKCountData.DataKeyByCo data = null;
                if (subResult.containsKey(cc)) {
                    data = subResult.get(cc);
                } else {
                    data = new pe.neon.최원준.ExtractKCountData.DataKeyByCo();
                }
                data.slope = slope;
                subResult.put(cc, data);
                result.put(keyword, subResult);

                //System.out.println("DB : " + keyword +"\t" + subResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fac.release(rs, pstmt, conn);
        }

        BufferedWriter cbyWriter = null;
        BufferedWriter pbyWriter = null;
        BufferedWriter slopeWriter = null;
        try {
            cbyWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("d:/data/yeo/20191102/키워드별_국가별_피인용수_cbykey"), "UTF-8"));
            pbyWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("d:/data/yeo/20191102/키워드별_국가별_논문수_pbykey"), "UTF-8"));
            slopeWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("d:/data/yeo/20191102/키워드별_전체성장률_SLOPE"), "UTF-8"));

            cbyWriter.write(writeHeader(pe.neon.최원준.ExtractKCountData.DATAKEYTYPE.CBYKEY));
            pbyWriter.write(writeHeader(pe.neon.최원준.ExtractKCountData.DATAKEYTYPE.PBYKEY));
            slopeWriter.write(writeHeader(pe.neon.최원준.ExtractKCountData.DATAKEYTYPE.SLOPE));

            Set<String> _bykeyList = result.keySet();
            StringBuilder buf = new StringBuilder();
            for (String keyword : _keywordSet) {
                buf.append(keyword);
                buf.append(TAB);
                Map<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo> keywordData = result.get(keyword);
                System.out.println(keyword + "\t" + keywordData);
                pbyWriter.write(buf.toString() + k(keywordData, pe.neon.최원준.ExtractKCountData.DATAKEYTYPE.PBYKEY));
                cbyWriter.write(buf.toString() + k(keywordData, pe.neon.최원준.ExtractKCountData.DATAKEYTYPE.CBYKEY));
                slopeWriter.write(buf.toString() + k(keywordData, pe.neon.최원준.ExtractKCountData.DATAKEYTYPE.SLOPE));
//                System.out.print("===========> " + buf.toString() + k(keywordData, DATAKEYTYPE.SLOPE));
                buf.setLength(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cbyWriter != null) {
                cbyWriter.flush();
                cbyWriter.close();
            }
            if (pbyWriter != null) {
                pbyWriter.flush();
                pbyWriter.close();
            }
            if (slopeWriter != null) {
                slopeWriter.flush();
                slopeWriter.close();
            }
        }
    }

    public static final String TAB = "\t";
    public static final String ENTER = "\r\n";

    protected String writeHeader(pe.neon.최원준.ExtractKCountData.DATAKEYTYPE type) {
        StringBuilder buf = new StringBuilder();
        buf.append("keyword");
        buf.append(TAB);
        switch (type) {
            case CBYKEY:
                buf.append("키워드별 전체 피인용수 (CbyKey)");
                break;
            case PBYKEY:
                buf.append("키워드별 전체 논문수 (PbyKey)");
                break;
            case SLOPE:
                buf.append("키워드별 전체 성장률 (SLOPE)");
                break;
        }
        buf.append(TAB);
        for (String country : _countryCodeSet) {
            buf.append(country);
            buf.append(TAB);
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append(ENTER);
        return buf.toString();
    }

    protected String k(Map<String, pe.neon.최원준.ExtractKCountData.DataKeyByCo> countryData, pe.neon.최원준.ExtractKCountData.DATAKEYTYPE type) {
        StringBuilder buf = new StringBuilder();

        double cbysum = 0d;
        double pbysum = 0d;
        double slopesum = 0d;
        if (countryData != null) {
            Set<String> cnList = countryData.keySet();
            for (String _cn : cnList) {
                pe.neon.최원준.ExtractKCountData.DataKeyByCo dbc = countryData.get(_cn);
                try {
                    cbysum += Double.valueOf(dbc.cbykeybyco);
                } catch (Exception e) {
                    cbysum += 0;
                }
                try {
                    pbysum += Double.valueOf(dbc.pbykeybyco);
                } catch (Exception e) {
                    pbysum += 0;
                }
                try {
                    slopesum += Double.valueOf(dbc.slope);
                } catch (Exception e) {
                    slopesum += 0;
                }
            }
        }
        String cby = String.format("%.6f", cbysum);
        String pby = String.format("%.6f", pbysum);
        String slope = String.format("%.6f", slopesum);
        switch (type) {
            case CBYKEY:
                buf.append(cby);
                break;
            case PBYKEY:
                buf.append(pby);
                break;
            case SLOPE:
                buf.append(slope);
                break;
        }
        buf.append(TAB);

        for (String country : _countryCodeSet) {
            if (countryData == null) {
                buf.append("0");
            } else {
                pe.neon.최원준.ExtractKCountData.DataKeyByCo dkbc = countryData.get(country);
                if (dkbc == null) {
                    buf.append("0");
                } else {
                    //System.out.println("\t" + country +"\t" + dkbc.toString());
                    switch (type) {
                        case CBYKEY:
                            buf.append(dkbc.cbykeybyco==null?"0":dkbc.cbykeybyco);
                            break;
                        case PBYKEY:
                            buf.append(dkbc.pbykeybyco==null?"0":dkbc.pbykeybyco);
                            break;
                        case SLOPE:
                            buf.append(dkbc.slope==null?"0":dkbc.slope);
                            break;
                    }
                }
            }
            buf.append(TAB);
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append(ENTER);
        return buf.toString();
    }

    public static void main(String[] args) {
        try {
            new ExtractData_201908().write키워드국가별피인용수();
            new ExtractData_201908().countKeywordMeanYearPerCountry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
