package pe.neon.여운동._202001;

import pe.neon.FileRW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

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
        if(dir.isDirectory()){
            File[] files = dir.listFiles();
            for(File file : files){
                readFile(file);
            }
        }
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
     * FIRST_AUTHOR_COUNTRYCODE
     * FIRST_AFFILIATION_NAME
     * SOURCE_SOURCETITLE
     * @param line
     */
    @Override
    public void readline(String line) {
        //필요한 항목은 연도, 인용수, 국가 정보.
        String[] datas = line.split("\t");
        String eid = datas[0];
        int year = convertInt(datas[2]);
        int cnCitaion = convertInt(datas[7]);
        String affCC = pickFirstCodeUp(datas[8]); //첫번째 국가만 사용 (저자국가가 없다면 대안으로 기관 국가를 사용한다.)
        String auCC = pickFirstCodeUp(datas[9]); // 첫번째 국가만 사용.
    }

    public void 양적성장성지표(){
        // 연도별 발표건수 구하기



    }
//    private Map<Integer, >

    public void 질적활동성격차(){

    }

    public void 양적활동성격차(){

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
        return src;
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

    public static void main(String[] args) {

    }

    @Override
    public void writerline() {

    }
}
