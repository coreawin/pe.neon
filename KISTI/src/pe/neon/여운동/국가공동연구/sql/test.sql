SELECT  /*+ PARALLEL (8) */ COUNT(k.eid) as count, k.keyword, publication_year, af.COUNTRY_CODE FROM NYEO2019_SCOPUS_A_KEYWORD k, NYEO2019_SCOPUS_AKEY_CN_BASE sd, NYEO2019_SCOPUS_AKEY_FILTERING af
    WHERE
    af.keyword = k.keyword and k.EID = sd.EID AND k.KEYWORD='100% RENEWABLE ENERGY';
    GROUP BY
    k.keyword, af.country_code, publication_year
