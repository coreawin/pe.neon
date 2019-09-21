package pe.neon.손은수;

import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.util.*;

/**
 * @손은수 박사
 * @since 2019.09.21
 * 컴퓨터과학	2206 OR 17**
 * 물리천문학	31**
 * 생명공학	2204 OR 11** OR 13** OR 24**
 * 의학	27** OR 28** OR 29** OR 34** OR 35**
 * 약학	30**
 * 기계공학	2203 OR 2210
 * 산업공학	2207 OR 2209
 * 재료공학	2211 OR 25**
 * 전기전자공학	2208
 * 건축공학	2205 OR 2215 OR 2216
 * 수학	26**
 * 에너지공학	21**
 * 해양지구과학	2212 OR 19**
 * 항공우주공학	2202
 * 화학공학	15** OR 16**
 * 환경공학	23**
 * 인문학	12** OR 32**
 * 사회과학	14** OR 18** OR 20** OR 33** OR 36**
 */
public class AIAsjcSCOPUS {

    /**
     * 다운로드 받은 파일을 하나로 합친다 (중복 제거)
     */
    public void merge() {

        File dir = new File("d:\\data\\이창환\\20190918\\");
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            BufferedWriter bw = null;
            Map<String, String> data = new HashMap<String, String>();
            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("d:\\data\\이창환\\merge.txt")), "UTF-8"));
                int cnt = 0;
                for (File file : files) {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                        System.out.println("read file " + file.getName());
                        String line = null;
                        int idx = 0;
                        while ((line = br.readLine()) != null) {
                            if (idx == 0) {
                                idx++;
                                continue;
                            }
                            String[] values = line.split("\t");
                            String eid = values[0].trim();
                            if (data.containsKey(eid) == false) {
                                data.put(eid, line);
                            }
                            idx++;
                            cnt++;
                            if (cnt % 1000 == 0) {
                                System.out.println("\t\t progress " + cnt);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            br.close();
                        }
                    }
                }

                Set<Map.Entry<String, String>> entrySet = data.entrySet();
                for (Map.Entry<String, String> entry : entrySet) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    bw.write(value);
                    bw.write("\n");
                }
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    enum CLASSIFIACTION {
        컴퓨터과학, 물리천문학, 생명공학, 의학, 약학, 기계공학, 산업공학, 재료공학, 전기전자공학,
        건축공학, 수학, 에너지공학, 해양지구과학, 항공우주공학, 화확공학, 환경공학, 인문학, 사회과학
    }

    public Set<CLASSIFIACTION> getClassifiation(String asjc) {
        String[] asjcs = asjc.split(";");
        Set<CLASSIFIACTION> set = new HashSet<CLASSIFIACTION>();
        for (String _asjc : asjcs) {
            _asjc = _asjc.trim();
            if (_asjc.equalsIgnoreCase("2206") || _asjc.startsWith("17")) {
                set.add(CLASSIFIACTION.컴퓨터과학);
                continue;
            }
            if (_asjc.startsWith("31")) {
                set.add(CLASSIFIACTION.물리천문학);
                continue;
            }

            if (_asjc.equalsIgnoreCase("2204") || _asjc.startsWith("11")
                    || _asjc.startsWith("13") || _asjc.startsWith("24")) {
                set.add(CLASSIFIACTION.생명공학);
                continue;
            }
            if (_asjc.startsWith("27") || _asjc.startsWith("28")
                    || _asjc.startsWith("29") || _asjc.startsWith("34") || _asjc.startsWith("35")) {
                set.add(CLASSIFIACTION.의학);
                continue;
            }
            if (_asjc.startsWith("30")) {
                set.add(CLASSIFIACTION.약학);
                continue;
            }
            if (_asjc.equalsIgnoreCase("2203") || _asjc.equalsIgnoreCase("2210")) {
                set.add(CLASSIFIACTION.기계공학);
                continue;
            }
            if (_asjc.equalsIgnoreCase("2207") || _asjc.equalsIgnoreCase("2209")) {
                set.add(CLASSIFIACTION.산업공학);
                continue;
            }
            if (_asjc.equalsIgnoreCase("2211") || _asjc.startsWith("25")) {
                set.add(CLASSIFIACTION.재료공학);
                continue;
            }
            if (_asjc.equalsIgnoreCase("2208")) {
                set.add(CLASSIFIACTION.전기전자공학);
                continue;
            }
            if (_asjc.equalsIgnoreCase("2205") || _asjc.equalsIgnoreCase("2215")
                    || _asjc.equalsIgnoreCase("2216")) {
                set.add(CLASSIFIACTION.건축공학);
                continue;
            }
            if (_asjc.startsWith("26")) {
                set.add(CLASSIFIACTION.수학);
                continue;
            }
            if (_asjc.startsWith("21")) {
                set.add(CLASSIFIACTION.에너지공학);
                continue;
            }
            if (_asjc.equalsIgnoreCase("2212") || _asjc.startsWith("19")) {
                set.add(CLASSIFIACTION.해양지구과학);
                continue;
            }
            if (_asjc.equalsIgnoreCase("2202")) {
                set.add(CLASSIFIACTION.항공우주공학);
                continue;
            }
            if (_asjc.startsWith("15") || _asjc.startsWith("16")) {
                set.add(CLASSIFIACTION.화확공학);
                continue;
            }
            if (_asjc.startsWith("23")) {
                set.add(CLASSIFIACTION.환경공학);
                continue;
            }
            if (_asjc.startsWith("12") || _asjc.startsWith("32")) {
                set.add(CLASSIFIACTION.인문학);
                continue;
            }
            if (_asjc.startsWith("14") || _asjc.startsWith("18")
                    || _asjc.startsWith("20") || _asjc.startsWith("33") || _asjc.startsWith("36")) {
                set.add(CLASSIFIACTION.사회과학);
                continue;
            }
        }
        return set;
    }

    public Set<String> getCountryCode(String cn) {
        String[] cns = cn.split(";");
        Set<String> set = new HashSet<String>();
        for (String _cns : cns) {
            set.add(_cns.trim().toUpperCase());
        }
        return set;
    }

    public void calculator() {
        File file = new File("d:\\data\\이창환\\20190918\\merge.txt");
        BufferedReader br = null;
        SortedMap<CLASSIFIACTION, Map<String, Integer>> yearStat = new TreeMap<CLASSIFIACTION, Map<String, Integer>>();
        SortedMap<CLASSIFIACTION, Map<String, Integer>> cnStat = new TreeMap<CLASSIFIACTION, Map<String, Integer>>();
        SortedSet<String> cnSet = new TreeSet<String>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            System.out.println("read file " + file.getName());
            String line = null;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\t");
                String eid = values[0].trim();
                String year = values[1].trim();
                String firstasjc = values[2].trim();
                String asjc = values[3].trim();
                String cn = values[5].trim();
                Set<CLASSIFIACTION> classSet = getClassifiation(asjc);
                Set<String> cnList = getCountryCode(cn);
                cnSet.addAll(cnList);
                for (CLASSIFIACTION _class : classSet) {
                    Map<String, Integer> yearStatData = yearStat.get(_class);
                    if (yearStatData == null) {
                        yearStatData = new HashMap<String, Integer>();
                    }
                    Map<String, Integer> cnStatData = yearStat.get(_class);
                    if (cnStatData == null) {
                        cnStatData = new HashMap<String, Integer>();
                    }
                    year = year.trim();
                    if (!"".equalsIgnoreCase(year)) {
                        int yearValue = 1;
                        if (yearStatData.containsKey(year)) {
                            yearValue = yearStatData.get(year) + 1;
                        }
                        yearStatData.put(year, yearValue);
                        yearStat.put(_class, yearStatData);
                    }
                    if (!"".equalsIgnoreCase(cn)) {
                        for (String _cn : cnList) {
                            int cnValue = 1;
                            if (cnStatData.containsKey(_cn)) {
                                cnValue = cnStatData.get(_cn) + 1;
                            }
                            cnStatData.put(_cn, cnValue);
                            cnStat.put(_class, cnStatData);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        BufferedWriter bw1 = null;
        BufferedWriter bw2 = null;
        Map<String, String> data = new HashMap<String, String>();
        try {
            bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("d:\\data\\이창환\\20190918\\year.txt")), "UTF-8"));
            bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("d:\\data\\이창환\\20190918\\cn.txt")), "UTF-8"));
//            Map<CLASSIFIACTION, Map<String, Integer>> yearStat = new HashMap<CLASSIFIACTION, Map<String, Integer>>();
            Set<CLASSIFIACTION> yearKeys = yearStat.keySet();
            StringBuffer buf = new StringBuffer();
            final String TAB = "\t";
            final String ENTER = "\n";


            buf.append("분류");
            buf.append(TAB);
            for (int sy = 1999; sy <= 2018; sy++) {
                buf.append(String.valueOf(sy));
                buf.append(TAB);
            }
            buf.deleteCharAt(buf.length()-1);
            buf.append(ENTER);
            bw1.write(buf.toString());
            buf.setLength(0);


            buf.append("분류");
            buf.append(TAB);
            for (String _cns : cnSet) {
                buf.append(_cns);
                buf.append(TAB);
            }
            buf.deleteCharAt(buf.length()-1);
            buf.append(ENTER);
            bw2.write(buf.toString());
            buf.setLength(0);

            for (CLASSIFIACTION _class : yearKeys) {
                Map<String, Integer> values = yearStat.get(_class);
                buf.append(_class);
                buf.append(TAB);
                for (int sy = 1999; sy <= 2018; sy++) {
                    buf.append(values.get(String.valueOf(sy)) == null ? "0" : values.get(String.valueOf(sy)));
                    buf.append(TAB);
                }
                buf.deleteCharAt(buf.length()-1);
                buf.append(ENTER);
                bw1.write(buf.toString());
                buf.setLength(0);

                Map<String, Integer> cnValues = cnStat.get(_class);
                buf.append(_class);
                buf.append(TAB);
                for (String _cns : cnSet) {
                    buf.append(cnValues.get(_cns) == null ? "0" : cnValues.get(_cns));
                    buf.append(TAB);
                }
                buf.deleteCharAt(buf.length()-1);
                buf.append(ENTER);
                bw2.write(buf.toString());
                buf.setLength(0);
            }
//            Map<CLASSIFIACTION, Map<String, Integer>> cnStat = new HashMap<CLASSIFIACTION, Map<String, Integer>>();
            bw1.flush();
            bw2.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw1 != null) {
                try {
                    bw1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bw2 != null) {
                try {
                    bw2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
//        new AIAsjcSCOPUS().merge();
        new AIAsjcSCOPUS().calculator();
    }
}
