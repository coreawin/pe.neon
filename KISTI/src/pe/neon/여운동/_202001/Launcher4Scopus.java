package pe.neon.여운동._202001;

import pe.neon.FileRW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * SCOPUS 파일을 읽어 연구적 특성을 구한다.<br>
 * 1. 연구성장성 <br>
 * 2. 질적 활동성 격차 <br>
 * 3. 양적 활동성 격차 <br>
 *
 * @author coreawin
 * @since 2020-01-24
 */
public class Launcher4Scopus extends FileRW {

    private final String filePath;

    /**
     * @param filePath
     * scopus download file path
     */
    public Launcher4Scopus(String filePath){
        this.filePath = filePath;
        File dir = new File(filePath);
        System.out.println("read directory : " + dir.getAbsolutePath());
        if(dir.isDirectory()){
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if(name.endsWith("txt"))
                        return true;
                    return false;
                }
            });
            for(File file : files){
                beforeProgress();
                String techName = file.getName().replaceAll("\\.bulk.*$", "");
                System.out.println("Read techName : " + techName);
                readFile(file);
                afterProgress(techName);
            }
            표준화지표구하기();
        }
    }

    enum E_COUNTRYCODE{
        KOR
    }

    /**
     * 실제 논문 파일의 데이터를 읽는다.<br>
     * EID
     * TITLE
     * YEAR
     * DOI
     * KEYWORD
     * INDEX_KEYWORD
     * ASJC
     * NUMBER_CITATION
     * AFFILIATION_COUNTRY
     * FIRST_AUTHOR_COUNTRYCODnE
     * FIRST_AFFILIATION_NAME
     * SOURCE_SOURCETITLE
     * @param line
     */
    @Override
    public void readline(String line) {
        if(lineCount < 2) return;
        //필요한 항목은 연도, 인용수, 국가 정보.
        String[] datas = line.split("\t");
        String eid = datas[0];
        int year = convertInt(datas[2]);
        int cnCitaion = convertInt(datas[7]);
        String affCC = pickFirstCodeUp(datas[8]); //첫번째 국가만 사용 (저자국가가 없다면 대안으로 기관 국가를 사용한다.)
        String auCC = pickFirstCodeUp(datas[9]); // 첫번째 국가만 사용.

        sumCitation += cnCitaion; // 인용의 총 합을 구한다.;
        sumYear += year; /*연도의 총 합을 구한다. (평균구하기 위해)*/
        cnDocument += 1; /*문서건수를 카운트 한다.*/

        if(year > 0 ){
            maxYear = Math.max(year, maxYear);
            minYear = Math.min(year, minYear);

            Integer cnDocument = cnDocumentPerYear.get(year);
            if(cnDocument==null){
                cnDocument = 0;
            }
            cnDocumentPerYear.put(year, cnDocument + 1);
        }

        String countryCode = auCC;
        if("".equals(countryCode)){
            countryCode = affCC;
        }
        if(!"".equals(countryCode)){
            countrySet.add(countryCode);
            /*국가별 인용합계 정보 */
            Integer sumCitation = citationSumPerCountryCode.get(countryCode);
            if(sumCitation == null){
                sumCitation = 0;
            }
            sumCitation += cnCitaion;
            citationSumPerCountryCode.put(countryCode, sumCitation);

            /*국가별 건수 정보 저장.*/
            Integer cnDocument = cnDocumentPerCountryCode.get(countryCode);
            if(cnDocument==null){
                cnDocument = 0;
            }
            cnDocumentPerCountryCode.put(countryCode, cnDocument + 1);

            /*국가별 연도합계 정보 저장.*/
            Integer sumYear  = sumYearPerCountryCode.get(countryCode);
            if(sumYear==null){
                sumYear = 0;
            }
            sumYearPerCountryCode.put(countryCode, sumYear + 1);
        }

    }

    /**
     * 1st key : 기술군 <br>
     *     2nd key : 연도정보<br>
     *
     */
    private Map<String, Result> resultDataMap = new TreeMap<>();

    public class Result{
        String 기술군명;
        Double 성장성지표;
        Double 질적활동성격차;
        Double 양적활동성격차;
        Double 표준성장성지표;
        Double 표준질적활동성격차;
        Double 표준양적활동성격차;

        public String toString(){
            StringBuilder buf = new StringBuilder();
            buf.append("기술군명 : ");
            buf.append(기술군명);
            buf.append("\r\n\t 성장성지표");
            buf.append(성장성지표);
            buf.append(" | 표준성장성지표");
            buf.append(표준성장성지표);

            buf.append("\r\n\t 질적활동성격차");
            buf.append(질적활동성격차);
            buf.append(" | 표준질적활동성격차");
            buf.append(표준질적활동성격차);

            buf.append("\r\n\t 양적활동성격차");
            buf.append(양적활동성격차);
            buf.append(" | 표준양적활동성격차");
            buf.append(표준양적활동성격차);
            return buf.toString();
        }
    }

    private Set<String> countrySet = new HashSet<>();
    /**
     * 연도별 발표 건수.
     */
    private Map<Integer, Integer> cnDocumentPerYear = new TreeMap<>();

    /**
     * 국가별 연도별 발표 건수.
     * 1st key : 국가명
     * 2nd key : 연도별
     */
    private Map<String, Map<Integer, Integer>> cnDocumentPerYearPerCountryCode = new TreeMap<>();

    /**
     * 국가별 CPP 총합<br>
     */
    private Map<String, Integer> citationSumPerCountryCode = new TreeMap<>();
    /**
     * 국가별 문서건수
     */
    private Map<String, Integer> cnDocumentPerCountryCode = new TreeMap<>();

    /**
     * 국가별 연도 합계.
     */
    private Map<String, Integer> sumYearPerCountryCode = new TreeMap<>();


    /**
     * 국가별 지표정보.
     */
    private Map<String, Result> resultPerCountryCode = new TreeMap<>();


    //인용건수 총 합
    private int sumCitation = 0;
    //연도 합
    private int sumYear = 0;
    //평균 연도
    private float avgYear = 0;
    //문서 총 건수
    private int cnDocument = 0;

    private Double 최대성장성지표값 = 0.0 ;
    private Double 최소성장성지표값 = 1.0;

    private Double 최대질적격차지표값 = 0.0;
    private Double 최소질적격차지표값 = 1.0;

    private Double 최대양적격차지표값 = 0.0;
    private Double 최소양적격차지표값 = 1.0;

    /**
     * 가장 최신 연도.
     */
    private int maxYear = 0;
    /**
     * 가장 최소연도.
     */
    private int minYear = 2020;

    /**
     * 전체 CPP 지표.
     */
    private float cpp지표 = 0;

    /**
     * 파일을 읽을때 초기화 해야 할 값<br>
     */
    private void beforeProgress(){
        this.sumCitation = 0;
        this.cnDocument = 0;
        this.sumYear = 0;
        this.cpp지표 = 0;
        this.avgYear = 0;
        this.cnDocumentPerYear.clear();;

        this.sumYearPerCountryCode.clear();
        this.cnDocumentPerCountryCode.clear();
        this.cnDocumentPerYearPerCountryCode.clear();
        this.citationSumPerCountryCode.clear();
    }

    /**
     * 파일을 다 읽고 나서 해야 할일
     */
    private void afterProgress(String techName){
        if(cnDocument == 0 ){
            cpp지표 = 0;
            avgYear = 0;
        }else{
            cpp지표 = sumCitation / cnDocument;
            avgYear = sumYear / cnDocument;
        }

        double 성장성지표 = 성장성지표구하기();
        double 질적활동성격차 = 질적활동성격차();
        double 양적활동성격차 = 양적활동성격차();

        if(성장성지표 > 0){
            최대성장성지표값 = Math.max(성장성지표, 최대성장성지표값);
            최소성장성지표값 = Math.min(성장성지표, 최소성장성지표값);
        }
        if(질적활동성격차 > 0) {
            최대질적격차지표값 = Math.max(질적활동성격차, 최대질적격차지표값);
            최소질적격차지표값 = Math.min(질적활동성격차, 최소질적격차지표값);
        }
        if(양적활동성격차 > 0) {
            최대양적격차지표값 = Math.max(양적활동성격차, 최대양적격차지표값);
            최소양적격차지표값 = Math.min(양적활동성격차, 최소양적격차지표값);
        }

        if(cnDocument==0){
            성장성지표=0;
            질적활동성격차=0;
            양적활동성격차=0;
        }

        Result result = new Result();
        result.기술군명 = techName;
        result.성장성지표 = 성장성지표;
        result.질적활동성격차 = 질적활동성격차;
        result.양적활동성격차 = 양적활동성격차;
        resultDataMap.put(techName, result);
    }

    private void 표준화지표구하기(){
        Set<String> techNameSet = resultDataMap.keySet();
        for(String techName : techNameSet){
            Result r = resultDataMap.get(techName);
            System.out.println("표준화 지표 구하기 " + techName);
            r.표준성장성지표 = get표준화지표(r.성장성지표, 최소성장성지표값, 최대성장성지표값);
            r.표준질적활동성격차 = get표준화지표(r.질적활동성격차, 최소질적격차지표값, 최대질적격차지표값);
            r.표준양적활동성격차 = get표준화지표(r.양적활동성격차, 최소양적격차지표값, 최대양적격차지표값);
            resultDataMap.put(techName, r);
        }
    }

    //4번째 자리에서 반올림.
    double round4 = 10000d ;

    private double get표준화지표(Double 현재기술군의지표, Double 최소지표, Double 최대지표) {
        System.out.println("표준화 지표 : " + 현재기술군의지표 +"\t" + 최소지표 +"\t" + 최대지표);
        double 표준화지표 = ((현재기술군의지표 - 최소지표) / (최대지표 - 최소지표)) * 0.8d + 0.1d;
        return Math.round(표준화지표*round4) / round4;
    }

    public double 성장성지표구하기(){
        // 연도별 발표건수 구하기
        double alpha = 0.5;
        double 분모 = 0;
        double 분자 = 0;
        for(int year = minYear ; year<=maxYear ; year++){
            /*연도별 논문 건수 구하기.*/
            Integer cnDocument = cnDocumentPerYear.get(year);
            if(cnDocument==null) cnDocument = 0;
            분모 += cnDocument;
            분자 += cnDocument * Math.pow(alpha, maxYear-year +1);
        }
        if(분모==0){
            return 0;
        }
        double 양정성장성지표 = 분자 / 분모;
//        System.out.println(양정성장성지표 + " 양정성장성지표 : " + Math.round((양정성장성지표* round5)) / (double) round5 );
        return Math.round(양정성장성지표* round5) / round5;
    }

    public Map<String, Double>  국가별성장성지표구하기(){
        // 연도별 발표건수 구하기
        final double alpha = 0.5;
        Map<String, Double> 국가별성장성지표 = new TreeMap<>();
        for(String country : countrySet){
            Map<Integer, Integer> cnDocumentPerYear = cnDocumentPerYearPerCountryCode.get(country);
            double 분모 = 0;
            double 분자 = 0;
            for(int year = minYear ; year<=maxYear ; year++){
                /*연도별 논문 건수 구하기.*/
                Integer cnDocument = cnDocumentPerYear.get(year);
                if(cnDocument==null) cnDocument = 0;
                분모 += cnDocument;
                분자 += cnDocument * Math.pow(alpha, maxYear-year +1);
            }
            if(분모==0){
                국가별성장성지표.put(country, 0d);
            }else{
                double 양정성장성지표 = 분자 / 분모;
                국가별성장성지표.put(country, Math.round(양정성장성지표* round5) / round5);
            }
        }
        return 국가별성장성지표;
    }

    public double 질적활동성격차(){
        final String korCountryCode = "KOR";
        double 한국의CPP지표 = 0d;
        try {
            한국의CPP지표 = citationSumPerCountryCode.get(korCountryCode) / cnDocumentPerCountryCode.get(korCountryCode);
        }catch(Exception e){//ignore
            한국의CPP지표 =0d;
        }
        double CPP1위국가의CPP지표값 = 0;
        Set<String> countrySet = cnDocumentPerCountryCode.keySet();
        for(String country : countrySet){
            double 국가별CPP지표값 = citationSumPerCountryCode.get(country) / cnDocumentPerCountryCode.get(country);
            CPP1위국가의CPP지표값 = Math.max(CPP1위국가의CPP지표값, 국가별CPP지표값);
        }
        double 질적활동성격차지표 = 1 - (한국의CPP지표 / CPP1위국가의CPP지표값);
        return Math.round(질적활동성격차지표*round5) / round5;

    }

    //5번째 자리에서 반올림.
    double round5 = 100000d ;

    public double 양적활동성격차(){
        //5번째 자리에서 반올림.
        Integer 한국의발표건수 = 0;
        double 발표건수가가장많은건수 = 0d;
        Set<String> countrySet = cnDocumentPerCountryCode.keySet();
        for(String country : countrySet){
            Integer 국가별건수 = cnDocumentPerCountryCode.get((country));
            발표건수가가장많은건수 = Math.max(발표건수가가장많은건수, 국가별건수);
            if(E_COUNTRYCODE.KOR.name().equalsIgnoreCase(country)){
                한국의발표건수 = 국가별건수;
            }
//            System.out.println("그외 발견건수. : ==> "+ 발표건수가가장많은건수 +" : " + 국가별건수 +" : " + country);
        }
        double 양적활동성격차지표 = 1;

        if(한국의발표건수==0){
            return 양적활동성격차지표;
        }else{
            양적활동성격차지표 = 1 - (한국의발표건수 / (double)발표건수가가장많은건수);
        }
//        System.out.println("한국의발표건수 : " + 한국의발표건수 +"\t" + 발표건수가가장많은건수);
//        System.out.println("양적활동성격차지표 : " + (Math.round(양적활동성격차지표*round5) / round5));
        return Math.round(양적활동성격차지표*round5) / round5;
    }

    /**
     * ; 구분으로 되어 있는 국가코드를 분리해서 가장 앞에 있는 국가코드를 사용한다.
     * @param src
     * @return
     */
    private String pickFirstCodeUp(String src){
        if(src == null) return "";
        src = src.trim();
        if(src.indexOf(";")!=-1){
            String[] datas = src.split(";");
            for(String data : datas){
                if("".equals(data)) continue;
                else return data.trim();
            }
        }
        return src.toUpperCase().trim();
    }

    /**
     * 문자열을 숫자형으로 변환
     * @param src
     * @return
     */
    private int convertInt(String src){
        if(src==null) return 0;
        src = src.trim();
        if("".equals(src)) return 0;
        return Integer.parseInt(src);
    }

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) {
        String 분과명 = "신재생에너지";
//        분과명 = "소부장";
//        분과명 = "혁신성장";
        분과명 = "0213";
        String 작업일 = dateFormat.format(new Date());
        /*scopusPath에는 기술분과명의 폴더명이 있고, 해당 폴더에는 논문/특허 폴더에 각 기술군에 해당하는 SCOPUS raw 데이터가 있다. (download format - tab delim)*/
        String scopusPath = "d:\\data\\2020\\yeo\\"+작업일+"\\"+분과명+File.separator;
        File dir = new File(scopusPath);
        if(dir.isDirectory()){
            File[] dirs = dir.listFiles();
            for(File _dir : dirs){
                String 기술분과명 = _dir.getName();
                System.out.println("기술분과명 " + 기술분과명);
                String resultFilePath = _dir.getAbsolutePath() + File.separator + String.format("RESULT_SCOPUS_%s_%s.txt", 기술분과명, 작업일);
                Launcher4Scopus launcher = new Launcher4Scopus(_dir.getAbsolutePath() +File.separator + "scopus" + File.separator);
                launcher.writer(resultFilePath);
            }
        }
    }

    public void writer(String path) {
        final String ENTER = "\r\n";
        final String TAB = "\t";
        BufferedWriter bw = createWriter(new File(path), null);
        try {
            StringBuffer buf = new StringBuffer();
            buf.append("기술명");
            buf.append(TAB);
            buf.append("연구성장성");
            buf.append(TAB);
            buf.append("질적활동성격차");
            buf.append(TAB);
            buf.append("양적활동성격차");
            buf.append(TAB);
            buf.append("표준연구성장성");
            buf.append(TAB);
            buf.append("표준질적활동성격차");
            buf.append(TAB);
            buf.append("표준양적활동성격차");
            buf.append(ENTER);
            Set<String> techNameSet = resultDataMap.keySet();
            for (String techName : techNameSet) {
                Result r = resultDataMap.get(techName);
                buf.append(r.기술군명);
                buf.append(TAB);
                buf.append(r.성장성지표);
                buf.append(TAB);
                buf.append(r.질적활동성격차);
                buf.append(TAB);
                buf.append(r.양적활동성격차);
                buf.append(TAB);
                buf.append(r.표준성장성지표);
                buf.append(TAB);
                buf.append(r.표준질적활동성격차);
                buf.append(TAB);
                buf.append(r.표준양적활동성격차);
                buf.append(ENTER);
                try {
                    bw.write(buf.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    buf.setLength(0);
                }
            }
        }finally{
            close(bw);
        }
    }
}
