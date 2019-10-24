package pe.neon.이창환.highschool;

import kr.co.tqk.web.db.dao.export.ExportField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.kisti.DownloadSearchUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DownloadScopusData_highSchool {
    static Logger logger = LoggerFactory.getLogger("DownloadScopusData.class");
    final String ip = "203.250.207.72";
    final int port = 5555;
    // final String downloadPath = "d:\\data\\이창환\\20190819\\";
    static String downloadPath = "d:\\data\\이창환\\20191004\\";
    static String modelpath = "t:/release/KISTI/SCOPUS_2014_WEB/models/ExportBasicFormat.xlsx";
    final DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE downloadFormat = DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE.TAB_DELIMITED;
    final String sort = "";
    final boolean isSort = false;
    final boolean isAdmin = true;
    private Set<String> selectedField = new HashSet<String>();
    final int sidx = 0;
    final int eidx = Integer.MAX_VALUE;

    private void setFieldInit() {
        selectedField.add(ExportField.TITLE.name());
//        selectedField.add(ExportField.ABSTRACT.name());
        selectedField.add(ExportField.YEAR.name());
//        selectedField.add(ExportField.DOI.name());
//        selectedField.add(ExportField.KEYWORD.name());
//        selectedField.add(ExportField.INDEX_KEYWORD.name());
//        selectedField.add(ExportField.FIRST_ASJC.name());
//        selectedField.add(ExportField.ASJC.name());
//        selectedField.add(ExportField.NUMBER_CITATION.name());
//        selectedField.add(ExportField.CITATION.name());
//        selectedField.add(ExportField.NUMBER_REFERENCE.name());
//        selectedField.add(ExportField.REFERENCE.name());
//        selectedField.add(ExportField.CITATION_TYPE.name());

        selectedField.add(ExportField.AUTHOR_AUTHORINFO.name());
        selectedField.add(ExportField.AUTHOR_NAME.name());
//        selectedField.add(ExportField.AUTHOR_COUNTRYCODE.name());
        selectedField.add(ExportField.AUTHOR_EMAIL.name());
//        selectedField.add(ExportField.AFFILIATION_ID.name());
        selectedField.add(ExportField.AFFILIATION_NAME.name());
//        selectedField.add(ExportField.AFFILIATION_COUNTRY.name());
        selectedField.add(ExportField.DELEGATE_AFFILIATION.name());

//        selectedField.add(ExportField.FIRST_AUTHOR_NAME.name());
//        selectedField.add(ExportField.FIRST_AUTHOR_COUNTRYCODE.name());
//        selectedField.add(ExportField.FIRST_AUTHOR_EMAIL.name());
//        selectedField.add(ExportField.FIRST_AFFILIATION_NAME.name());

        selectedField.add(ExportField.SOURCE_ID.name());
        selectedField.add(ExportField.SOURCE_SOURCETITLE.name());
        selectedField.add(ExportField.SOURCE_VOLUMN.name());
        selectedField.add(ExportField.SOURCE_ISSUE.name());
        selectedField.add(ExportField.SOURCE_PAGE.name());
        selectedField.add(ExportField.SOURCE_TYPE.name());
        selectedField.add(ExportField.SOURCE_PUBLICSHERNAME.name());
//        selectedField.add(ExportField.SOURCE_COUNTRY.name());
        selectedField.add(ExportField.SOURCE_PISSN.name());
        selectedField.add(ExportField.SOURCE_EISSN.name());

//        selectedField.add(ExportField.CORR_AUTHORNAME.name());
//        selectedField.add(ExportField.CORR_COUNTRYCODE.name());
//        selectedField.add(ExportField.CORR_EMAIL.name());
//        selectedField.add(ExportField.CORR_AFFILIATION.name());
    }

    public DownloadScopusData_highSchool(String searchRule) throws Exception {
        setFieldInit();
        DownloadSearchUtil su = new DownloadSearchUtil(ip, port, sidx, eidx, searchRule, downloadPath, modelpath,
                downloadFormat, selectedField, sort, isSort, isAdmin);
        su.execute();
    }

    public static void main(String... args) throws Exception {
//        String searchRule = "((TS=(\"machine learning\" OR \"machine intelligence\" OR \"supervised learning\" OR \"supervised training\" OR \"unsupervised learning\" OR \"unsupervised training\" OR \"semi-supervised learning\" OR \"semi-supervised training\" OR \"semisupervised learning\" OR \"semisupervised training\" OR \"reinforced learning\" OR \"reinforcement learning\" OR \"multi-task learning\" OR \"support vector machine\" OR \"support vector machines\" OR \"deep learning\" OR \"logical learning\" OR \"relational learning\" OR \"probabilistic graphical model\" OR \"probabilistic graphical models\" OR \"rule learning\" OR \"instance-based learning\" OR \"latent representation\" OR \"latent representations\" OR \"bio-inspired approach\" OR \"bio-inspired approaches\" OR \"transfer-learning\" OR \"learning algorithm\" OR \"learning model\" OR \"learning models\" OR \"multilayer perceptron\" OR \"genetic algorithm\" OR \"logic programming\" OR \"description logistics\" OR \"fuzzy logic\" OR \"fuzzy system\" OR \"fuzzy systems\" OR \"generative adversarial network\" OR \"deep generative\" OR \"neuroinformatics\" OR \"artificial neural\" OR \"neural software framework\" OR \"memristor\" OR \"neural engineering\" OR \"neurorobotics\" OR \"cognitive computing\" OR \"brain computing\" OR \"brain computer\" OR \"brain-machine\" OR \"human-machine\" OR \"brain model\" OR \"brain models\" OR \"neuromorphic\" OR \"neuro feedback\" OR \"brain feedback\" OR \"brain wave\" OR \"backpropagation\" AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018)) OR (TK=(\"classification and regression tree\" OR \"classification and regression trees\" OR \"neural network\" OR \"neural networks\") AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018))) OR (TS=(\"computer vision\" OR \"augmented reality\" OR \"biometrics\" OR \"visual biometrics\" OR \"image segmentation\" OR \"video segmentation\" OR \"character recognition\" OR \"object tracking\" OR \"scene segmentation\" OR \"virtual reality\" OR \"image recognition\" OR \"image caption\" OR \"visual intelligence\" OR \"object recognition\" OR \"motion recognition\" OR \"motion intelligence\" OR \"intelligence augmentation\" OR \"gesture recognition\" OR \"video analysis\" OR \"video story extraction\" OR \"video story representation\" OR \"video story summary\" OR \"biocybernetics\" OR \"face recognition\" OR \"visual question\" OR \"visual anwsering\" OR \"visual relationship\") AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018)) OR ((TK=(\"logistic regression\") AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018)) OR (TS=(\"probabilistic reasoning\" OR \"bayesian network\" OR \"bayesian networks\" OR \"expert system\" OR \"expert systems\" OR \"intelligence information system\" OR \"intelligence information systems\" OR \"random forest\" OR \"machine reasoning\" OR \"knowledge representation\" OR \"activity reasoning\" OR \"case based reasoning\" OR \"gradient tree boosting\" OR \"gradient boosting\" OR \"XGBOOST\" OR \"ADABOOST\" OR \"RANKBOOST\" OR \"stochastic gradient descent\" OR \"hidden markov model\" OR \"hidden markov models\" OR \"decision tree\" OR \"decision trees\" OR \"decision support system\" OR \"decision support systems\" OR \"predictive analytics\" OR \"recommender system\" OR \"recommender systems\" OR \"product recommendation\" OR \"product recommendations\" OR \"planning and scheduling\" OR \"inference engine\" AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018))) OR (TS=(\"phonology\" OR \"speech process\" OR \"speech processing\" OR \"voice process\" OR \"voice processing\" OR \"speech recognition\" OR \"voice recognition\" OR \"speech synthesis\" OR \"voice synthesis\" OR \"speech to speech\" OR \"voice to voice\" OR \"speech generation\" OR \"speech generator\" OR \"speech generators\" OR \"voice generation\" OR \"voice generator\" OR \"voice generators\" OR \"speaker recognition\" OR \"speaker authentication\" OR \"speaker identification\" OR \"dictation system\" OR \"dictation systems\" OR \"dialogue process\" OR \"dialogue processing\" OR \"dialog process\" OR \"dialog processing\" OR \"dialogue understanding\" OR \"dialog understanding\" OR \"dialogue generation\" OR \"dialogue generator\" OR \"dialog generation\" OR \"dialog generator\") AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018)) OR ((TS=((\"cooperation and coordination\" OR swarm) AND intelligen*)) AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018)) OR (TS=(\"distributed AI\" OR \"distributed artificial intelligence\" OR \"multi-agent system\" OR \"multi-agent systems\" OR \"intelligent agent\" OR \"intelligent agents\" OR \"mobile agent\" OR \"mobile agents\" OR \"intelligent assistance\" OR \"intelligent assistant\" OR \"intelligent assistants\" OR \"compound intelligence\" OR \"complex intelligence\" OR \"sense recognition\" OR \"adaptive system\" OR \"adaptive systems\" OR \"ambient intelligence\" OR \"autonomous agent\" OR \"autonomous agents\") AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018))) OR ((TS=((dialogue OR dialog OR morphology OR \"question and answer\" OR \"question and answering\" OR \"query and answer\" OR \"query and answering\") and intelligen*) AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018)) OR (TS=(\"natural language process\" OR \"natural language processing\" OR \"natural language generation\" OR \"information extraction\" OR \"machine translation\" OR \"semantics\" OR \"semantic net\" OR \"semantic web\" OR \"semantic role labeling\" OR \"semantic parsing\" OR \"latent semantic analysis\" OR \"latent dirichlet allocation\" OR \"sentiment analysis\" OR \"word sense disambiguation\" OR \"automatic translation\" OR \"automatic summarization\" OR \"document summarization\" OR \"text summarization\" OR \"chatbot\" OR \"question answering system\" OR \"question answering systems\" OR \"query answering system\" OR \"query answering systems\" OR \"ontology engineering\" OR \"ontologies engineering\") AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018))) OR (TS=(\"artificial intelligence\" OR \"computational intelligence\") AND DOCTYPE=(AR OR CP OR IP) AND PY=(1999-2018))\n";
        String[] searchRules = {
//                "AF-ID=(60032144 OR 60025960) OR OG=(korea and technology and science and institute) OO=(korea and technology and science and institute) or OG=(KIST) or OO=(KIST) PY=(2007-2019)",
//                "OG=(green and  technology and center ) OO=(green and  technology and center ) or OG=(GTC) or OO=(GTC) PY=(2007-2019)",
//                "AF-ID=(60068696) or OG=(korea and basic and science institute) or OO=(korea and basic and science institute) or OG=(KBSI) or OO=(KBSI)) and  PY=(2007-2019)",
//                "AF-ID=(60104266 OR 106224706) OG=(National and Fusion and institute and research) OO=(National and Fusion and institute and research) or OG=(NFRI) or OO=(NFRI) PY=(2007-2019)",
//                "AF-ID=(60081106) or OG=(Korea and Astronomy and Space and Science and institute) OO=(Korea and Astronomy and Space and Science and institute) or OG=(KASI) or OO=(KASI) PY=(2007-2019)",
//                "AF-ID=(60068693) or OG=(Korea and Institute and research and bioscience and biotechnology) OO=(Korea and Institute and research and bioscience and biotechnology) or OG=(KRIBB) or OO=(KRIBB) PY=(2007-2019)",
//                "AF-ID=(60092867) or OG=(Korea and Institute and science and Technology and Information) OO=(Korea and Institute and science and Technology and Information) or OG=(KISTI) or OO=(KISTI) PY=(2007-2019)",
//                "AF-ID=(60081221) or OG=(Korea and Institute and Oriental and Medicine) OO=(Korea and Institute and Oriental and Medicine) or OG=(KIOM) or OO=(KIOM) PY=(2007-2019)",
//                "AF-ID=(60073750) or OG=(Korea and Institute and Industrial and technology) OO=(Korea and Institute and Industrial and technology) or OG=(KITECH) or OO=(KITECH) PY=(2007-2019)",
//                "AF-ID=(60001558) or OG=(Electronics and Telecommunications and research and Institute) OO=(Electronics and Telecommunications and research and Institute) or OG=(ETRI) or OO=(ETRI) PY=(2007-2019)",
//                "AF-ID=(60094223) or OG=(Security and research and Institute and National) OO=(Security and research and Institute and National) or OG=(NSR) or OO=(NSR) PY=(2007-2019)",
//                "AF-ID=(115322331 OR 114505467 OR 116345247 OR 118010886 OR 114608509 OR 116508794 OR 116552948) or OG=(korea and research and Institute and Civil and Engineering and Building and Technology) OO=(korea and research and Institute and Civil and Engineering and Building and Technology) or OG=(KICT) or OO=(KICT) PY=(2007-2019)  ",
//                "AF-ID=(60068706 OR 107093508) or OG=(korea and research and Institute and Railroad) OO=(korea and research and Institute and Railroad) or OG=(KRRI) or OO=(KRRI) PY=(2007-2019)",
//                "AF-ID=(60028270 OR 60023053) or OG=(korea and research and Institute and Standards and Science) OO=(korea and research and Institute and Standards and Science) or OG=(KRISS) or OO=(KRISS) PY=(2007-2019)",
//                "AF-ID=(60068707) or OG=(korea and food and research and Institute) OO=(korea and food and research and Institute) or OG=(KFRI) or OO=(KFRI) PY=(2007-2019)",
//                "AF-ID=(113621682 OR 110815652 OR 111525747 OR 116345463) or OG=(World and Kimchi Institute) OO=(World and Kimchi Institute) or OG=(WIKIM) or OO=(WIKIM) PY=(2007-2019) ",
//                "AF-ID=(60031430) or OG=(Korea and Geoscience and Mineral and resources and Institute) OO=(Korea and Geoscience and Mineral and resources and Institute) or OG=(KIGAM) or OO=(KIGAM) PY=(2007-2019)",
//                "AF-ID=(60026196 OR 60073750) or OG=(Korea and Machinery and Materials and Institute) OO=(Korea and Machinery and Materials and Institute) or OG=(KIMM) or OO=(KIMM) PY=(2007-2019) ",
//                "AF-ID=(60068719 OR 100373417) or OG=(Korea and Aerospaceand Institute and research) OO=(Korea and Aerospace and Institute and research) or OG=(KARI) or OO=(KARI) PY=(2007-2019)",
//                "AF-ID=(60087822) or OG=(Korea and Energy and Institute and research) OO=(Korea and Energy and Institute and research) or OG=(KIER) or OO=(KIER) PY=(2007-2019) ",
//                "AF-ID=(60068700) or OG=(Korea and Electrotechnology and Research and Institute) OO=(Korea and Electrotechnology and Research and Institute) or OG=(KERI) or OO=(KERI) PY=(2007-2019)",
                "AF-ID=(60008646) or OG=(Korea and Chemical and Technology and Research and Institute) OO=(Korea and Chemical and Technology and Research and Institute) or OG=(KRICT) or OO=(KRICT) PY=(2007-2019)",
//                "AF-ID=(60103150 OR 106756523) or OG=(kitox) or OO=(kitox) OG=(Korea and Institute and Toxicology) or OO=(Korea and Institute and Toxicology) PY=(2007-2019)",
//                "AF-ID=(60009754) or OG=(Korea and Atomic and Energy and Research and Institute) OO=(Korea and Atomic and Energy and Research and Institute) or OG=(KAERI) or OO=(KAERI) PY=(2007-2019)",
        };

        System.setProperty("EJIANA_HOME", "d:\\release\\KISTI\\SCOPUS_2014\\WEB-INF\\resources\\") ;
        /* 2019.09.18 이창환 박사 ASJC별 연도별 건수 추출 대상 데이터 다운로드... */
        if (args != null) {
            try {
                downloadPath = args[0];
                logger.info("download path : {} ", downloadPath);
            } catch (Exception e) {
            }
            try {
                modelpath = args[1];
            } catch (Exception e) {
            }
        }
        new File(downloadPath).mkdirs();
        for(String searchRule : searchRules){
            logger.debug("searchRule {}", searchRule);
            new DownloadScopusData_highSchool(searchRule);
        }
    }

}
