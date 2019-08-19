package pe.neon;

import pe.neon.여운동.ExtractKCountData;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class FileUtil {

    public BufferedWriter getWriter(String outPath){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outPath), "UTF-8"));
            return writer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

/*
    public void write(){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("d:\\data\\yeo\\20190819\\키워드별MeanYear_소분류별_100등이상10건이상.txt"), "UTF-8"));
            for (String ctCode : countryCodeSet) {
                ExtractKCountData.CountryInfo info = null;
                if (ctInfo.containsKey(ctCode)) {
                    info = ctInfo.get(ctCode);
                } else {
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
    }*/
}
