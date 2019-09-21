package pe.neon.최원준;

import kr.co.tqk.web.db.dao.export.ExportField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DownloadScopusData {
    static Logger logger = LoggerFactory.getLogger("DownloadScopusData.class");
    final String ip = "203.250.207.72";
    final int port = 5555;
    // final String downloadPath = "d:\\data\\이창환\\20190819\\";
    static String downloadPath = "d:\\data\\20190819\\";
    static String modelpath = "d:\\release\\KISTI\\SCOPUS_2014\\models\\ExportBasicFormat.xlsx";
    final DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE downloadFormat = DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE.TAB_DELIMITED;
    final String sort = "";
    final boolean isSort = false;
    final boolean isAdmin = true;
    private Set<String> selectedField = new HashSet<String>();
    final int sidx = 0;
    final int eidx = Integer.MAX_VALUE;

    private void setFieldInit() {
        selectedField.add(ExportField.TITLE.name());
        selectedField.add(ExportField.ABSTRACT.name());
        selectedField.add(ExportField.YEAR.name());
        selectedField.add(ExportField.DOI.name());
        selectedField.add(ExportField.KEYWORD.name());
        selectedField.add(ExportField.INDEX_KEYWORD.name());
//        selectedField.add(ExportField.FIRST_ASJC.name());
        selectedField.add(ExportField.ASJC.name());
        selectedField.add(ExportField.NUMBER_CITATION.name());
        selectedField.add(ExportField.CITATION.name());
        selectedField.add(ExportField.NUMBER_REFERENCE.name());
        selectedField.add(ExportField.REFERENCE.name());
        selectedField.add(ExportField.CITATION_TYPE.name());

        selectedField.add(ExportField.AUTHOR_ID.name());
        selectedField.add(ExportField.AUTHOR_AUTHORINFO.name());
        selectedField.add(ExportField.AUTHOR_NAME.name());
        selectedField.add(ExportField.AUTHOR_COUNTRYCODE.name());
        selectedField.add(ExportField.AUTHOR_EMAIL.name());
        selectedField.add(ExportField.AFFILIATION_ID.name());
        selectedField.add(ExportField.AFFILIATION_NAME.name());
        selectedField.add(ExportField.AFFILIATION_COUNTRY.name());
        selectedField.add(ExportField.DELEGATE_AFFILIATION.name());

        selectedField.add(ExportField.FIRST_AUTHOR_NAME.name());
        selectedField.add(ExportField.FIRST_AUTHOR_COUNTRYCODE.name());
        selectedField.add(ExportField.FIRST_AUTHOR_EMAIL.name());
        selectedField.add(ExportField.FIRST_AFFILIATION_NAME.name());

        selectedField.add(ExportField.GRANT_AGENCY.name());
        selectedField.add(ExportField.GRANT_AGENCY_ID.name());
        selectedField.add(ExportField.GRANT_AGENCY_ACRONYM.name());
        selectedField.add(ExportField.GRANT_ID.name());

        selectedField.add(ExportField.SOURCE_ID.name());
        selectedField.add(ExportField.SOURCE_SOURCETITLE.name());
        selectedField.add(ExportField.SOURCE_VOLUMN.name());
        selectedField.add(ExportField.SOURCE_ISSUE.name());
        selectedField.add(ExportField.SOURCE_PAGE.name());
        selectedField.add(ExportField.SOURCE_TYPE.name());
        selectedField.add(ExportField.SOURCE_PUBLICSHERNAME.name());
        selectedField.add(ExportField.SOURCE_COUNTRY.name());
        selectedField.add(ExportField.SOURCE_PISSN.name());
        selectedField.add(ExportField.SOURCE_EISSN.name());

        selectedField.add(ExportField.CORR_AUTHORNAME.name());
        selectedField.add(ExportField.CORR_COUNTRYCODE.name());
        selectedField.add(ExportField.CORR_EMAIL.name());
        selectedField.add(ExportField.CORR_AFFILIATION.name());

//        selectedField.add(ExportField.OPEN_ACCESS_STATUS.name());
//        selectedField.add(ExportField.OPEN_ACCESS_STATUS_CODE.name());
        selectedField.add(ExportField.OPEN_ACCESS_URL.name());
    }

    public DownloadScopusData(String searchRule) throws Exception {
        setFieldInit();
        DownloadSearchUtil su = new DownloadSearchUtil(ip, port, sidx, eidx, searchRule, downloadPath, modelpath,
                downloadFormat, selectedField, sort, isSort, isAdmin);
        su.execute();
    }

    public static void main(String... args) throws Exception {
        System.setProperty("EJIANA_HOME", "d:\\release\\KISTI\\SCOPUS_2014\\WEB-INF\\resources\\") ;
        String searchRule = null;
        for (int idx = 1997; idx <= 2019; idx++) {
            downloadPath = String.format("d:/data/이창환/20190819/최원준/scopus_document/SCOPUS_KOR_DOCUMENT_%s.tsv", idx);
            new File(downloadPath).getParentFile().mkdirs();
            searchRule = String.format("PY=(%s) CU=(KOR)", idx);
//            searchRule = "eid=(84994872417)";
            logger.debug("download path {}", downloadPath);
            System.out.println(searchRule);
            System.out.println(downloadPath);
            new DownloadScopusData(searchRule);
        }
    }

}
