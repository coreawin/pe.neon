package pe.neon.여운동;

import pe.neon.FileUtil;

import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
                writer = fu.getWriter("d:\\data\\yeo\\20190819\\키워드별MeanYear_소분류별_100등이상10건이상.txt");
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


    public static void main(String[] args) {
        new ExtractData_201908().countKeywordMeanYearPerCountry();
    }
}
