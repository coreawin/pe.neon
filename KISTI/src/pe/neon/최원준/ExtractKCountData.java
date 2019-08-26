package pe.neon.최원준;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.*;

public class ExtractKCountData {

    /**
     * DB 커넥션 가져온다.
     */
    static class ConnectionFactoryBak {

        private static ConnectionFactoryBak instance = null;

        Connection conn = null;

        private static final String URL = "jdbc:oracle:thin:@203.250.196.44:1551:KISTI5";
        private static final String USER = "scopus";
        private static final String PASS = "scopus+11";

        public static synchronized ConnectionFactoryBak getInstance() {
            if (instance == null) {
                instance = new ConnectionFactoryBak();
            }
            return instance;
        }

        private ConnectionFactoryBak() {
        }

        public Connection getConnection() throws Exception {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
                return conn = DriverManager.getConnection(URL, USER, PASS);
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
                throw ex;
            }
        }

        public void release(Connection conn) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

        public void release(PreparedStatement pstmt, Connection conn) {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

        public void release(ResultSet rs, PreparedStatement pstmt, Connection conn) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

        public void release(ResultSet rs, PreparedStatement pstmt) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    // Map<String, >
    /* 분야별 국가 데이터를 처리하기 위한 국가 정보 */
    class CountryInfo {
        String ctCode;
        Map<String, Double> yearCoCtCnt = new TreeMap<String, Double>();
        long totalDocCnt;

        Map<String, Double> yearCoCitCnt = new TreeMap<String, Double>();
        long totalCitCnt;

        StringBuffer sb = new StringBuffer();
        double deltaKAvg = 0.0d;
        Map<String, Double> yearOfKctMultiKorCPP = new TreeMap<String, Double>();

        /**
         * K국가 코드
         **/
        public String getCtCode() {
            return ctCode;
        }

        public void setCtCode(String ctCode) {
            this.ctCode = ctCode;
        }

        public static final int START_YEAR = 2007;
        public static final int END_YEAR = 2016;

        public CountryInfo() {
            for (int s = START_YEAR; s <= END_YEAR; s++) {
                yearCoCtCnt.put(String.valueOf(s), 0.0);
            }

            for (int s = START_YEAR; s <= END_YEAR; s++) {
                yearCoCitCnt.put(String.valueOf(s), 0.0);
            }
        }

        /**
         * 분야별 K국가 i연도 논문 총 건수 설정
         **/
        public void putDocCnt(int year, double count) {
            if (year >= START_YEAR && year <= END_YEAR) {
                yearCoCtCnt.put(String.valueOf(year), count);
            }
        }

        /**
         * 분야별 K국가 i연도 논문 총 피인용수 설정
         **/
        public void putCitCnt(int year, double citCount) {
            if (year >= START_YEAR && year <= END_YEAR) {
                yearCoCitCnt.put(String.valueOf(year), citCount);
            }
        }

        /**
         * 분야별 K국가 i연도 논문 총 건수 설정
         **/
        public void putKDocCnt(int year, double count) {
            if (year >= START_YEAR && year <= END_YEAR) {
                yearCoCtCnt.put(String.valueOf(year), count);
            }
        }

        /**
         * 분야별 K국가 i연도 논문 총 피인용수 설정
         **/
        public void putKCitCnt(int year, double citCount) {
            if (year >= START_YEAR && year <= END_YEAR) {
                yearCoCitCnt.put(String.valueOf(year), citCount);
            }
        }

        /**
         * 분야별 K국가 분야별 논문 총 건수
         **/
        public double getTotalDocCnt() {
            double s = 0l;
            for (String k : yearCoCtCnt.keySet()) {
                s += yearCoCtCnt.get(k);
            }
            return s;
        }

        /**
         * 분야별 K국가 분야별 논문의 총 피인용 건수
         **/
        public double getTotalCitCnt() {
            double s = 0l;
            for (String k : yearCoCitCnt.keySet()) {
                s += yearCoCitCnt.get(k);
            }
            return s;
        }

        /**
         * 연도별 대한 K국가 논문 건수 * 연도별 한국논문 CPP (논문 총피인용수 / 논문 건수)
         * <p>
         * 분야별 Delta K의 값을 구하기 위해 처리
         *
         * @param year      발행연도
         * @param docCnt    해당 분야 공동연구 논문 건수
         * @param korDocCnt 해당 분야 한국논문 건수
         * @param korCitCnt 해당 분야 한국논문 총 피인용 수
         */
        public void setYearOfKctMultiKorCPP(int year, double docCnt, double korDocCnt, double korCitCnt) {
            if (year >= START_YEAR && year <= END_YEAR) {
                if (korDocCnt == 0) {
                    return;
                }
                yearOfKctMultiKorCPP.put(String.valueOf(year), (docCnt * (korCitCnt / korDocCnt)));
            }
        }

        /**
         * 연도별 대한 K국가 논문 건수 * 연도별 한국논문 CPP (논문 총피인용수 / 논문 건수)
         * <p>
         * 분야별 Delta K의 값을 구하기 위해 처리
         * <p>
         * 연도별 대한 K국가 논문 건수 * 연도별 한국논문 CPP (논문 총피인용수 / 논문 건수) 총합
         */
        public double getTotalYearOfKctMultiKorCPP() {
            double s = 0.0d;
            for (String k : yearOfKctMultiKorCPP.keySet()) {
                s += yearOfKctMultiKorCPP.get(k);
            }
            return s;
        }

        /* DELTA K 분야 값 */
        /* 분야별 K국가에 대한 Delta K 분야 계산 */
        public double deltaK() {
            if (getTotalDocCnt() == 0) {
                return 0.0d;
            }
            return (getTotalCitCnt() - getTotalYearOfKctMultiKorCPP()) / getTotalDocCnt();
        }

        @Override
        public String toString() {
            sb.append(this.ctCode);
            sb.append("\t");
            for (String k : yearCoCtCnt.keySet()) {
                sb.append(yearCoCtCnt.get(k));
                sb.append("\t");
            }
            sb.append(this.getTotalDocCnt());
            sb.append("\t");
            for (String k : yearCoCitCnt.keySet()) {
                sb.append(yearCoCitCnt.get(k));
                sb.append("\t");
            }
            sb.append(this.getTotalCitCnt());
            sb.append("\n");
            return sb.toString();
        }

        public String getInfo() {
            sb.append(this.ctCode);
            sb.append("\t");
            for (String k : yearCoCtCnt.keySet()) {
                sb.append(yearCoCtCnt.get(k));
                sb.append("\t");
            }
            sb.append(this.getTotalDocCnt());
            sb.append("\t");
            for (String k : yearCoCitCnt.keySet()) {
                sb.append(yearCoCitCnt.get(k));
                sb.append("\t");
            }
            sb.append(this.getTotalCitCnt());
            sb.append("\t");
            sb.append(getTotalYearOfKctMultiKorCPP());
            sb.append("\t");
            sb.append(deltaK());
            sb.append("\n");
            return sb.toString();
        }

        public String toNotTotalString() {
            sb.append(this.ctCode);
            sb.append("\t");
            for (String k : yearCoCtCnt.keySet()) {
                sb.append(yearCoCtCnt.get(k));
                sb.append("\t");
            }
            // sb.append(this.getTotalDocCnt());
            // sb.append("\t");
            for (String k : yearCoCitCnt.keySet()) {
                sb.append(yearCoCitCnt.get(k));
                sb.append("\t");
            }
            // sb.append(this.getTotalCitCnt());
            sb.append("\n");
            return sb.toString();
        }

    }

    public void writeDeltaK(String path) throws Exception {
        Map<String, Map<String, CountryInfo>> asCountryStats = new LinkedHashMap<String, Map<String, CountryInfo>>();

        /*
         * SCOPUS_2017_CO_RES_AFF_CT_KOR => 한국과 공동연구한 국가에 대한 연도별 분야 공동연구 논문에 대한 Fractional Count 방식의 논문 건수, Fractional Count 방식 피인용수 정보
         * 사용되는 필드
         * ASJC_CODE => ASJC 대분야
         * PUBLICATION_YEAR => 발행연도
         * COUNTRY_CODE => 국가 건수
         * DOC_CNT => 한국이 작성한 논문 비중 -> 1 * (논문에 포함된 해당 국가 건수 / 기관 국가의 총건수)
         * CIT_CNT => 한국이 작성한 피인용 비중 -> 논문의 피용수 * (논문에 포함된 해당 국가 건수 / 기관 국가의 총건수)
         *
         **/
        String sql = "SELECT ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE, "
                + "SUM(DOC_CNT) AS CO_CT_RES_DOC_TOT_CNT, "
                + "SUM(CIT_CNT) AS CO_CT_RES_CIT_TOT_CNT "
                + "FROM SCOPUS_2017_CO_RES_AFF_CT_KOR "
                + "GROUP BY  ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE "
                + "ORDER BY  ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE";

        ConnectionFactoryBak fac = ConnectionFactoryBak.getInstance();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set<String> countryCodeSet = new TreeSet<String>();
        Set<String> asjcCodeSet = new TreeSet<String>();
        Map<String, Map<String, Double>> deltaK = new TreeMap<String, Map<String, Double>>();
        try {
            conn = fac.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String ctCode = rs.getString(3);
                String asjcCode = rs.getString(1);
                int year = Integer.parseInt(rs.getString(2));
                double coctcnt = rs.getDouble(4);
                double cocitcnt = rs.getDouble(5);
                asjcCodeSet.add(asjcCode);
                countryCodeSet.add(ctCode);


                /*
                 * SCOPUS_2017_AFF_CT_STAT_KOR => 연도별 분야에 대한 한국 논문의 Fractional Count 방식의 논문 건수, Fractional Count 방식 피인용수 정보
                 * 사용되는 필드
                 * ASJC_CODE => ASJC 대분야
                 * PUBLICATION_YEAR => 발행연도
                 * COUNTRY_CODE => 국가 건수
                 * DOC_CNT => 한국이 작성한 논문 비중 -> 1 * (논문에 포함된 한국 건수 / 논문에 포함된 기관 국가의 총건수)
                 * CIT_CNT => 한국이 작성한 피인용 비중 -> 논문의 피용수 * (논문에 포함된 한국 건수 / 논문에 포함된 기관 국가의 총건수)
                 **/
                sql = "SELECT ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE, "
                        + "SUM(DOC_CNT) AS KOR_DOC_TOT_CNT, "
                        + "SUM(CIT_CNT) AS KOR_RES_CIT_TOT_CNT "
                        + "FROM SCOPUS_2017_AFF_CT_STAT_KOR "
                        + "WHERE ASJC_CODE = ? "
                        + "AND PUBLICATION_YEAR = ? "
                        + "AND COUNTRY_CODE = 'KOR'"
                        + "GROUP BY ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE "
                        + "ORDER BY ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE";

                PreparedStatement sPstmt = null;
                ResultSet subSet = null;

                Map<String, CountryInfo> ctInfo = null;

                if (asCountryStats.containsKey(asjcCode)) {
                    ctInfo = asCountryStats.get(asjcCode);
                } else {
                    ctInfo = new TreeMap<String, CountryInfo>();
                    asCountryStats.put(asjcCode, ctInfo);
                }

                CountryInfo countryInfo = null;
                if (!ctInfo.containsKey(ctCode)) {
                    countryInfo = new CountryInfo();
                    ctInfo.put(ctCode, countryInfo);
                } else {
                    countryInfo = ctInfo.get(ctCode);
                }

                countryInfo.putDocCnt(year, coctcnt);
                countryInfo.putCitCnt(year, cocitcnt);
                countryInfo.setCtCode(ctCode);
                if ("TUR".equalsIgnoreCase(ctCode) && "3400".equalsIgnoreCase(asjcCode)) {
                    System.out.println("CIT : " + countryInfo.yearCoCitCnt + "\n");
                    System.out.println("CCT : " + countryInfo.yearCoCtCnt + "\n");
                    System.out.println("CPPM : " + countryInfo.yearOfKctMultiKorCPP + "\n");
                    System.out.println("CCT : " + countryInfo.yearCoCtCnt + "\n");
                }
                try {
                    sPstmt = conn.prepareStatement(sql);
                    sPstmt.setString(1, asjcCode);
                    sPstmt.setString(2, String.valueOf(year));
                    subSet = sPstmt.executeQuery();

                    while (subSet.next()) {
                        countryInfo.setYearOfKctMultiKorCPP(year, coctcnt, subSet.getInt("KOR_DOC_TOT_CNT"), subSet.getInt("KOR_RES_CIT_TOT_CNT"));
                    }
                } catch (Exception e) {

                } finally {
                    fac.release(subSet, sPstmt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fac.release(rs, pstmt, conn);
        }

        for (String ctCode : countryCodeSet) {
            Map<String, Double> ctInfo = null;
            if (deltaK.containsKey(ctCode)) {
                ctInfo = deltaK.get(ctCode);
            } else {
                ctInfo = new TreeMap<String, Double>();
                deltaK.put(ctCode, ctInfo);
            }

            for (String asjc : asjcCodeSet) {
                Double countryInfo = 0.0;
                if (!ctInfo.containsKey(asjc)) {
                    countryInfo = 0.0;
                    ctInfo.put(asjc, countryInfo);
                } else {
                    countryInfo = ctInfo.get(asjc);
                }

                Map<String, CountryInfo> countryInfo1 = null;
                if (asCountryStats.containsKey(asjc)) {
                    countryInfo1 = asCountryStats.get(asjc);
                } else {
                    countryInfo1 = new TreeMap<String, CountryInfo>();
                    asCountryStats.put(asjc, countryInfo1);
                }

                CountryInfo bean = null;
                if (!countryInfo1.containsKey(ctCode)) {
                    bean = new CountryInfo();
                    countryInfo1.put(ctCode, bean);
                } else {
                    bean = countryInfo1.get(ctCode);
                }
                if ("TUR".equalsIgnoreCase(ctCode) && "3400".equalsIgnoreCase(asjc)) {
                    System.out.println("CIT : " + bean.yearCoCitCnt + "\n");
                    System.out.println("CCT : " + bean.yearCoCtCnt + "\n");
                    System.out.println("CPPM : " + bean.yearOfKctMultiKorCPP + "\n");
                    System.out.println("CCT : " + bean.yearCoCtCnt + "\n");
                }
                countryInfo = bean.deltaK();
                ctInfo.put(asjc, countryInfo);
            }
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            writer.write("COUNTRY_CODE" + "\t");
            for (String asjc : asjcCodeSet) {
                writer.write(asjc + "\t");
            }
            writer.write("AVG" + "\n");
            for (String ctCode : countryCodeSet) {
                Map<String, Double> ctInfo = deltaK.get(ctCode);
                int count = 0;
                double deltaKSum = 0.0;
                writer.write(ctCode + "\t");
                for (String asjc : asjcCodeSet) {
                    double deltaKInfo = ctInfo.get(asjc);
                    writer.write(deltaKInfo + "\t");
                    if (deltaKInfo > 0) {
                        count++;
                    }
                    deltaKSum += deltaKInfo;
                }
                writer.write((count == 0 ? "0.0" : (deltaKSum / count)) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }

    }

    public void writeCoCtInfo() throws Exception {
        Map<String, Map<String, CountryInfo>> asCountryStats = new LinkedHashMap<String, Map<String, CountryInfo>>();

        String sql = "SELECT ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE, "
                + "SUM(DOC_CNT) AS CO_CT_RES_DOC_TOT_CNT, "
                + "SUM(CIT_CNT) AS CO_CT_RES_CIT_TOT_CNT "
                + "FROM SCOPUS_2017_CO_RES_AFF_CT_KOR "
                + "GROUP BY  ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE "
                + "ORDER BY  ASJC_CODE, PUBLICATION_YEAR, COUNTRY_CODE";

        ConnectionFactoryBak fac = ConnectionFactoryBak.getInstance();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set<String> countryCodeSet = new TreeSet<String>();
        Set<String> asjcCodeSet = new TreeSet<String>();
        try {
            conn = fac.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String ctCode = rs.getString(3);
                String asjcCode = rs.getString(1);
                int year = Integer.parseInt(rs.getString(2));
                double coctcnt = rs.getDouble(4);
                double citCnt = rs.getDouble(5);
                asjcCodeSet.add(asjcCode);
                countryCodeSet.add(ctCode);

                Map<String, CountryInfo> ctInfo = null;
                if (asCountryStats.containsKey(asjcCode)) {
                    ctInfo = asCountryStats.get(asjcCode);
                } else {
                    ctInfo = new TreeMap<String, CountryInfo>();
                    asCountryStats.put(asjcCode, ctInfo);
                }

                CountryInfo countryInfo = null;
                if (!ctInfo.containsKey(ctCode)) {
                    countryInfo = new CountryInfo();
                    ctInfo.put(ctCode, countryInfo);
                } else {
                    countryInfo = ctInfo.get(ctCode);
                }
                countryInfo.setCtCode(ctCode);
                countryInfo.putDocCnt(year, coctcnt);
                countryInfo.putCitCnt(year, citCnt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fac.release(rs, pstmt, conn);
        }

        for (String asjc : asjcCodeSet) {
            if (!asCountryStats.containsKey(asjc)) {
                continue;
            }
            Map<String, CountryInfo> ctInfo = asCountryStats.get(asjc);
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream("d:/SCOPUS_DELTA_K/WORLD/" + asjc + "_CO_RES_CT_INFO.txt"), "UTF-8"));
                for (String ctCode : countryCodeSet) {
                    CountryInfo info = null;
                    if (ctInfo.containsKey(ctCode)) {
                        info = ctInfo.get(ctCode);
                    } else {
                        info = new CountryInfo();
                        info.setCtCode(ctCode);
                    }
                    writer.write(info.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }

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
        ConnectionFactoryBak fac = ConnectionFactoryBak.getInstance();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
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
        ConnectionFactoryBak fac = ConnectionFactoryBak.getInstance();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
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
        Map<String, Map<String, DataKeyByCo>> result = new HashMap<String, Map<String, DataKeyByCo>>();
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

            Map<String, DataKeyByCo> subResult = new HashMap<String, DataKeyByCo>();
            while (rs.next()) {
                String keyword = rs.getString("KEYWORD").trim();
                String cc = rs.getString("COUNTRY_CODE").trim();
                String pbykeybyco = rs.getString("PBYKEYBYCO").trim();
                String cbyKeybyco = rs.getString("CBYKEYBYCO").trim();

                if (result.containsKey(keyword)) {
                    subResult = result.get(keyword);
                } else {
                    subResult = new HashMap<String, DataKeyByCo>();
                }
                DataKeyByCo data = null;
                if (subResult.containsKey(cc)) {
                    data = subResult.get(cc);
                } else {
                    data = new DataKeyByCo();
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

            Map<String, DataKeyByCo> subResult = new HashMap<String, DataKeyByCo>();
            while (rs.next()) {
                String keyword = rs.getString("KEYWORD");
                String cc = rs.getString("COUNTRY_CODE");
                String slope = rs.getString("SLOPE");

                if (result.containsKey(keyword)) {
                    subResult = result.get(keyword);
                } else {
                    subResult = new HashMap<String, DataKeyByCo>();
                }
                DataKeyByCo data = null;
                if (subResult.containsKey(cc)) {
                    data = subResult.get(cc);
                } else {
                    data = new DataKeyByCo();
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
                    new OutputStreamWriter(new FileOutputStream("d:/data/yeo/20190819/키워드별_국가별_피인용수_cbykey"), "UTF-8"));
            pbyWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("d:/data/yeo/20190819/키워드별_국가별_논문수_pbykey"), "UTF-8"));
            slopeWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("d:/data/yeo/20190819/키워드별_전체성장률_SLOPE"), "UTF-8"));

            cbyWriter.write(writeHeader(DATAKEYTYPE.CBYKEY));
            pbyWriter.write(writeHeader(DATAKEYTYPE.PBYKEY));
            slopeWriter.write(writeHeader(DATAKEYTYPE.SLOPE));

            Set<String> _bykeyList = result.keySet();
            StringBuilder buf = new StringBuilder();
            for (String keyword : _keywordSet) {
                buf.append(keyword);
                buf.append(TAB);
                Map<String, DataKeyByCo> keywordData = result.get(keyword);
                System.out.println(keyword + "\t" + keywordData);
                pbyWriter.write(buf.toString() + k(keywordData, DATAKEYTYPE.PBYKEY));
                cbyWriter.write(buf.toString() + k(keywordData, DATAKEYTYPE.CBYKEY));
                slopeWriter.write(buf.toString() + k(keywordData, DATAKEYTYPE.SLOPE));
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

    protected String writeHeader(DATAKEYTYPE type) {
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

    protected String k(Map<String, DataKeyByCo> countryData, DATAKEYTYPE type) {
        StringBuilder buf = new StringBuilder();

        double cbysum = 0d;
        double pbysum = 0d;
        double slopesum = 0d;
        if (countryData != null) {
            Set<String> cnList = countryData.keySet();
            for (String _cn : cnList) {
                DataKeyByCo dbc = countryData.get(_cn);
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
                DataKeyByCo dkbc = countryData.get(country);
                if (dkbc == null) {
                    buf.append("0");
                } else {
                    //System.out.println("\t" + country +"\t" + dkbc.toString());
                    switch (type) {
                        case CBYKEY:
                            buf.append(dkbc.cbykeybyco);
                            break;
                        case PBYKEY:
                            buf.append(dkbc.pbykeybyco);
                            break;
                        case SLOPE:
                            buf.append(dkbc.slope);
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

    public static void main(String[] args) throws Exception {
        System.out.println("ABC");
        new ExtractKCountData().write키워드국가별피인용수();
    }
}
