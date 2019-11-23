package pe.neon.여운동.특허데이터추출;

import java.io.*;
import java.util.*;

/**
 * 키워드	2014	2015	2016	2017	2018	평균연도	총건수	한국건수	일등건수	일등건수/한국건수	일등국가 <BR>
 * 특허에서 Keyword 정보를 추출 (키워드 베타) 위의 형태로 키워드 통계 정보를 추출한다.<br>
 * pno	pnyear	pndate	authority	appyear	appdate	inventor	inventor-count	assignee	assignee-count	keyword	ipc	app_gp <br>
 * EP3509961A1	2019	2019-07-17	EP	2017	2017-09-06	ILSGB`NICHOLLS, Darren;GB`WON, Simon	2	US`Lavazza Professional North America, LLC	1	NOZZLE;NOZZLE BODY;INLET END;OUTLET END;BEVERAGE PREPARATION PACKAGE;DETACHABLE MANNER;PREPARATION PACKAGE;PRESENT INVENTION	B65D 75/58;B65D 47/10	US`LAVAZZA PROFESSIONAL NORTH AMERICA <br>
 *
 * @author coreawin
 * @since 2019.11.23
 */
public class ExtractPatentKeywordStat {
    String[] ipc26Datas = new String[]{
            "A61N", "B81B", "B81C", "B82B", "B82Y", "C30B", "F15C", "G01B", "G01C", "G01D", "G01F", "G01H", "G01J", "G01K", "G01L", "G01M", "G01N", "G01Q", "G01R", "G01S", "G01V", "G01W", "G02B", "G02C", "G02F", "G03B", "G03C", "G03H", "G04B", "G04C", "G04D", "G04F", "G04G", "G04R", "G05B", "G05F", "G06C", "G06D", "G06E", "G06F", "G06G", "G06J", "G06N", "G06T", "G08B", "G08C", "G09C", "G11C", "G12B", "G21K", "H01C", "H01F", "H01G", "H01J", "H01L", "H01Q", "H01S", "H03B", "H03C", "H03D", "H03F", "H03G", "H03H", "H03J", "H03K", "H03L", "H03M", "H04B", "H04H", "H04J", "H04K", "H04L", "H04M", "H04N", "H04Q", "H04R", "H04S", "H04W", "H05G", "H05H", "H05K"
    };

    String[] ipc27Datas = new String[]{
            "A21B", "A45D", "A47G", "A47J", "A47L", "B01B", "B60M", "B61L", "D06F", "E06C", "F21H", "F21K", "F21L", "F21M", "F21P", "F21Q", "F21S", "F21V", "F21W", "F21Y", "F24B", "F24C", "F24D", "F25C", "F25D", "G08G", "G10K", "H01B", "H01H", "H01K", "H01M", "H01P", "H01R", "H01T", "H02B", "H02G", "H02H", "H02J", "H02K", "H02M"
    };

    Set<String> ipc26Set = new HashSet<String>();
    Set<String> ipc27Set = new HashSet<String>();

    public ExtractPatentKeywordStat() {
        ipcSetup();
    }

    protected void ipcSetup() {
        for (String ipc : ipc26Datas) {
            ipc26Set.add(ipc.trim());
        }
        for (String ipc : ipc27Datas) {
            ipc27Set.add(ipc.trim());
        }
        ipc26Set.addAll(ipc27Set);
    }

    public void readFile() {
        String fileName = "D:\\git\\pe.neon\\KISTI\\src\\pe\\neon\\여운동\\특허데이터추출\\data\\patent_20191123.txt";
        Map<String, KeywordData> result = new HashMap<String, KeywordData>();
        int realDocument = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            String line;
            int cnt = 0;
            while ((line = br.readLine()) != null) {
                cnt++;
                if (cnt == 1) continue;
                String[] datas = line.split("\t");
//                pno	pnyear	pndate	authority	appyear	appdate	inventor	inventor-count	assignee	assignee-count	keyword	ipc	app_gp <br>
                String ipcData = datas[11];
                String[] ipc = ipcData.split(";");
                boolean matchIpc = false;
                for (String ipcFull : ipc) {
                    /*IPC는 대상 문서를 골라내는데 사용한다.*/
                    String clazz4 = ipcFull.substring(0, 4);
//                    System.out.println("ipcTarget " + clazz4 +"\t" + ipc26Set.contains(clazz4));
                    if (ipc26Set.contains(clazz4)) {
                        matchIpc = true;
                        break;
                    }
                }
                if (matchIpc == false) {
                    continue;
                }
                realDocument++;

                String pno = datas[0];
                String appyear = datas[4];
                Integer apYear = Integer.parseInt(appyear);
                String inventers = datas[6];
                String assignees = datas[8];
                String keywords = datas[10];
                String[] inventorList = inventers.split(";");
                String[] assigneeList = assignees.split(";");
                String[] keywordList = keywords.split(";");

                Set<String> cnSet = new HashSet<String>();
                for (String datum : inventorList) {
                    datum = datum.trim();
                    String[] cn = datum.split("`");
                    cnSet.add(cn[0].toUpperCase().trim());
                }
                for (String datum : assigneeList) {
                    datum = datum.trim();
                    String[] cn = datum.split("`");
                    cnSet.add(cn[0].toUpperCase().trim());
                }

                for (String keyword : keywordList) {
                    keyword = keyword.trim();
                    //키워드 길이가 1이하인건 제외한다.
                    if(keyword.length() < 2) continue;
                    KeywordData kd = result.get(keyword);
                    if(kd == null){
                        kd = new KeywordData();
                        kd.keyword = keyword;
                    }
                    kd.putAyear(apYear);
                    kd.pnos.add(pno);
                    for(String cn : cnSet){
                        Integer cnCnt = kd.countries.get(cn);
                        if(cnCnt==null){
                            cnCnt = 1;
                        }else{
                            cnCnt +=1 ;
                        }
                        kd.countries.put(cn, cnCnt);
                    }
                    result.put(keyword, kd);
                }
                System.out.println("target document " + pno + "\t" + cnSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String out = "d:\\data\\yeo\\20191123\\out.txt";
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(out)), "UTF-8"))){
            Set<String> resultSet = result.keySet();
            bw.write("키워드\t2014\t2015\t2016\t2017\t2018\t평균연도\t총건수\t한국건수\t일등건수\t일등건수/한국건수\t일등국가\n");
            for(String keyword : resultSet){
                KeywordData kd = result.get(keyword);
                System.out.println(kd.writeData());
                bw.write(kd.writeData() +"\n");
            }
            bw.flush();;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("총 대상 특허 건수 " + realDocument);

    }

    public static void main(String[] args) {
        new ExtractPatentKeywordStat().readFile();
    }

    public class KeywordData {
        String keyword;

        public List<Integer> ayearList = new ArrayList<Integer>();
        /*연도별 건수가 필요.*/
        public LinkedHashMap<Integer, Integer> ayears = new LinkedHashMap<Integer, Integer>();

        public KeywordData(){
            for(int idx=2014; idx<=2018; idx++){
                ayears.put(idx, 0);
            }
        }

        public void putAyear(Integer ayear){
            ayearList.add(ayear);
            Integer cnt = ayears.get(ayear);
            if(cnt==null){
                cnt =0;
            }else{
                cnt+=1;
            }
            ayears.put(ayear, cnt);
        }
        /**
         * 국가 건수 정보를 기록.
         */
        public Map<String, Integer> countries = new HashMap<String, Integer>();
        public List<String> pnos = new ArrayList<String>();

        Set<String> top1Country = new HashSet<String>();
        Integer top1Cn;
        Integer krCount;
        Float meanYear =null;

        public void makeCal(){
            int sumAYear = 0;
            for(Integer ayear : ayearList){
                sumAYear += ayear;
            }
            meanYear = (float)sumAYear / (float)ayearList.size();

            krCount = countries.get("KR");
            if(krCount == null){
                krCount = 0;
            }

            Set<String> countryList = countries.keySet();
            int max = 0;
            for(String country : countryList){
                Integer cnt = countries.get(country);
                if(max <= cnt){
                    top1Cn = cnt;
                    max = cnt;
                }
            }
            for(String country : countryList){
                Integer cnt = countries.get(country);
                if(top1Cn == cnt){
                    top1Country.add(country);
                }
            }

        }

        static final String TAB = "\t";
        public String writeData(){
            if(meanYear==null){
                makeCal();
            }
//            키워드	2014	2015	2016	2017	2018	평균연도	총건수	한국건수	일등건수	일등건수/한국건수	일등국가 <BR>
            StringBuilder buffer = new StringBuilder();
            buffer.append(keyword);
            buffer.append(TAB);
            Set<Integer> aYearSet = ayears.keySet();
            for(Integer ayear : aYearSet){
                buffer.append(ayears.get(ayear));
                buffer.append(TAB);
            }
            buffer.append(meanYear);
            buffer.append(TAB);
            buffer.append(pnos.size());
            buffer.append(TAB);
            buffer.append(krCount);
            buffer.append(TAB);
            buffer.append(top1Cn);
            buffer.append(TAB);
            if(krCount==0){
                buffer.append(0f);
            }else{
                buffer.append((float)top1Cn/(float)krCount);
            }
            buffer.append(TAB);
            for(String cn : top1Country){
                buffer.append(cn);
                buffer.append(",");
            }
            buffer.deleteCharAt(buffer.length()-1);
            return buffer.toString();
        }

        private String writeData(boolean abc){
            makeCal();
            return null;
        }

        public String toString(){
            if(meanYear==null){
                makeCal();
            }
            StringBuilder buffer = new StringBuilder();
            buffer.append("keyword : ");
            buffer.append(keyword);
            buffer.append("\n");
            buffer.append("countries : ");
            buffer.append(countries);
            buffer.append("\n");
            buffer.append("meanYear : ");
            buffer.append(meanYear);
            buffer.append("\t Year : ");
            buffer.append(ayearList);
            buffer.append("\n");
            buffer.append("특허건수.  : ");
            buffer.append(pnos.size());
            buffer.append("\n");
            buffer.append("한국 특허건수.  : ");
            buffer.append(krCount);
            buffer.append("\n");
            buffer.append("1등국가  : ");
            buffer.append(top1Country);
            buffer.append("\n");
            buffer.append("1등국가 건수. : ");
            buffer.append(top1Cn);
            buffer.append("\n");
            buffer.append("연도별 건수. : ");
            buffer.append(ayears);
            buffer.append("\n");
            buffer.append(writeData());
            buffer.append("\n");
            return buffer.toString();
        }
    }
}
