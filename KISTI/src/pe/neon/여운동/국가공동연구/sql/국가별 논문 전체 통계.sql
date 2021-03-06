-- 국가별 Fractional 논문 건수
DROP TABLE SCOPUS_2017Y_COUNTRY_STATS CASCADE constraints PURGE;
CREATE TABLE SCOPUS_2017Y_COUNTRY_STATS NOLOGGING AS
SELECT A.COUNTRY_CODE, A.EID, A.CIT_COUNT, (1 * (A.COUNTRY_CNT / A.COUNRY_TOTAL_CNT)) AS DOC_F_CNT, (A.CIT_COUNT * (A.COUNTRY_CNT / A.COUNRY_TOTAL_CNT)) AS CIT_T_CNT 
FROM (
    SELECT DISTINCT A.COUNTRY_CODE, A.EID, A.CIT_COUNT, B.COUNRY_TOTAL_CNT, B.COUNTRY_CNT
    FROM (
        SELECT DISTINCT A.COUNTRY_CODE, A.EID, A.CIT_COUNT
        FROM SCOPUS_2017_UNIQ_AFF_CT_EID A
		-- 20개 과학기술분야 문서 검색
        WHERE A.L_ASJC_CODE IN ('1000', '1100',
        '1300', '1500',
        '1600', '1700',
        '1800', '1900',
        '2100', '2200',
        '2300', '2400',
        '2500', '2600',
        '2700', '2800',
        '3000', '3100',
        '3400', '3500'
        )
    ) A
    INNER JOIN (
		-- FRACTIONAL 통계(논문건수, 총피인용수)를 위한 문헌별 국가 통계
        SELECT DISTINCT IN_STATS.EID, IN_STATS.COUNTRY_CODE, IN_STATS.COUNRY_TOTAL_CNT, IN_STATS.COUNTRY_CNT
        FROM (
            SELECT STATS.EID, STATS.PUBLICATION_YEAR, STATS.L_ASJC_CODE AS ASJC_CODE, STATS.COUNTRY_CODE, STATS.COUNRY_TOTAL_CNT, COUNT (STATS.COUNTRY_CODE) AS COUNTRY_CNT
            FROM (
                SELECT COUNTRY_STATS.EID, S_STATS.PUBLICATION_YEAR, COUNTRY_STATS.L_ASJC_CODE, COUNTRY_STATS.COUNTRY_CODE, S_STATS.CT_CNT AS COUNRY_TOTAL_CNT                       
                FROM  SCOPUS_2017_AFF_CT_EID COUNTRY_STATS
                INNER JOIN (
                    SELECT SK.EID, SK.PUBLICATION_YEAR, SK.L_ASJC_CODE AS ASJC_CODE, COUNT(SK.COUNTRY_CODE) AS CT_CNT
                    FROM SCOPUS_2017_AFF_CT_EID SK 
                    GROUP BY SK.EID, SK.PUBLICATION_YEAR, SK.L_ASJC_CODE
                ) S_STATS
                ON  S_STATS.EID = COUNTRY_STATS.EID 
                AND S_STATS.PUBLICATION_YEAR = COUNTRY_STATS.PUBLICATION_YEAR 
                AND S_STATS.ASJC_CODE = COUNTRY_STATS.L_ASJC_CODE
            ) STATS
            GROUP BY STATS.EID, STATS.PUBLICATION_YEAR, STATS.L_ASJC_CODE, STATS.COUNTRY_CODE, STATS.COUNRY_TOTAL_CNT
        ) IN_STATS
    ) B
    ON A.EID = B.EID AND A.COUNTRY_CODE = B.COUNTRY_CODE
) A;

CREATE INDEX idx_scopus_2017Y_CTS_CT ON SCOPUS_2017Y_COUNTRY_STATS(COUNTRY_CODE);
CREATE INDEX idx_scopus_2017Y_CTS_EID ON SCOPUS_2017Y_COUNTRY_STATS(EID);
CREATE INDEX idx_scopus_2017Y_CTS_CIT_CNT ON SCOPUS_2017Y_COUNTRY_STATS(CIT_COUNT);
CREATE INDEX idx_scopus_2017Y_CTS_DCNT ON SCOPUS_2017Y_COUNTRY_STATS(DOC_F_CNT);
CREATE INDEX idx_scopus_2017Y_CTS_SCICNT ON SCOPUS_2017Y_COUNTRY_STATS(CIT_T_CNT);

-- 국가별 Fractional 논문 건수 (최근 5년)
DROP TABLE SCOPUS_2017Y_COUNTRY_5Y_STATS CASCADE constraints PURGE;
CREATE TABLE SCOPUS_2017Y_COUNTRY_5Y_STATS NOLOGGING AS
SELECT A.COUNTRY_CODE, A.EID, A.CIT_COUNT, (1 * (A.COUNTRY_CNT / A.COUNRY_TOTAL_CNT)) AS DOC_F_CNT, (A.CIT_COUNT * (A.COUNTRY_CNT / A.COUNRY_TOTAL_CNT)) AS CIT_T_CNT 
FROM (
    SELECT DISTINCT A.COUNTRY_CODE, A.EID, A.CIT_COUNT, B.COUNRY_TOTAL_CNT, B.COUNTRY_CNT
    FROM (
        SELECT DISTINCT A.COUNTRY_CODE, A.EID, A.CIT_COUNT
        FROM SCOPUS_2017_UNIQ_AFF_CT_EID A
		-- 20개 과학기술분야 문서 검색
        WHERE A.L_ASJC_CODE IN ('1000', '1100',
        '1300', '1500',
        '1600', '1700',
        '1800', '1900',
        '2100', '2200',
        '2300', '2400',
        '2500', '2600',
        '2700', '2800',
        '3000', '3100',
        '3400', '3500'
       ) 
       -- 최근 5년치 범위 조건
       AND A.PUBLICATION_YEAR BETWEEN '2014' AND '2016'
     ) A
    INNER JOIN (
		-- FRACTIONAL 통계(논문건수, 총피인용수)를 위한 문헌별 국가 통계
        SELECT DISTINCT IN_STATS.EID, IN_STATS.COUNTRY_CODE, IN_STATS.COUNRY_TOTAL_CNT, IN_STATS.COUNTRY_CNT
        FROM (
            SELECT STATS.EID, STATS.PUBLICATION_YEAR, STATS.L_ASJC_CODE AS ASJC_CODE, STATS.COUNTRY_CODE, STATS.COUNRY_TOTAL_CNT, COUNT (STATS.COUNTRY_CODE) AS COUNTRY_CNT
            FROM (
                SELECT COUNTRY_STATS.EID, S_STATS.PUBLICATION_YEAR, COUNTRY_STATS.L_ASJC_CODE, COUNTRY_STATS.COUNTRY_CODE, S_STATS.CT_CNT AS COUNRY_TOTAL_CNT                       
                FROM  SCOPUS_2017_AFF_CT_EID COUNTRY_STATS
                INNER JOIN (
                    SELECT SK.EID, SK.PUBLICATION_YEAR, SK.L_ASJC_CODE AS ASJC_CODE, COUNT(SK.COUNTRY_CODE) AS CT_CNT
                    FROM SCOPUS_2017_AFF_CT_EID SK 
                    GROUP BY SK.EID, SK.PUBLICATION_YEAR, SK.L_ASJC_CODE
                ) S_STATS
                ON  S_STATS.EID = COUNTRY_STATS.EID 
                AND S_STATS.PUBLICATION_YEAR = COUNTRY_STATS.PUBLICATION_YEAR 
                AND S_STATS.ASJC_CODE = COUNTRY_STATS.L_ASJC_CODE
            ) STATS
            GROUP BY STATS.EID, STATS.PUBLICATION_YEAR, STATS.L_ASJC_CODE, STATS.COUNTRY_CODE, STATS.COUNRY_TOTAL_CNT
        ) IN_STATS
    ) B
    ON A.EID = B.EID AND A.COUNTRY_CODE = B.COUNTRY_CODE
) A;

CREATE INDEX idx_scopus_2017Y_5Y_CTS_CT ON SCOPUS_2017Y_COUNTRY_5Y_STATS(COUNTRY_CODE);
CREATE INDEX idx_scopus_2017Y_5Y_CTS_EID ON SCOPUS_2017Y_COUNTRY_5Y_STATS(EID);
CREATE INDEX idx_scopus_2017Y_5Y_CTS_CIT_CNT ON SCOPUS_2017Y_COUNTRY_5Y_STATS(CIT_COUNT);
CREATE INDEX idx_scopus_2017Y_5Y_CTS_DCNT ON SCOPUS_2017Y_COUNTRY_5Y_STATS(DOC_F_CNT);
CREATE INDEX idx_scopus_2017Y_5Y_CTS_SCICNT ON SCOPUS_2017Y_COUNTRY_5Y_STATS(CIT_T_CNT);


-- 국가별 논문 통계 쿼리 V1.1 (2017.09.26)
DROP TABLE SCOPUS_2017Y_CT_COMPLETE_STAT CASCADE constraints PURGE;
CREATE TABLE SCOPUS_2017Y_CT_COMPLETE_STAT AS
SELECT A.COUNTRY_CODE, A.P, A.C, A.DOC_CNT, A.CIT_TOT_CNT
FROM (
  SELECT COUNTRY_CODE, COUNT(EID) AS P, SUM(CIT_COUNT) AS C, SUM(DOC_F_CNT) P_BY_CO, SUM(CIT_T_CNT) AS C_BY_CO
  FROM SCOPUS_2017Y_COUNTRY_STATS
  GROUP BY COUNTRY_CODE
) A
ORDER BY A.DOC_CNT DESC, A.COUNTRY_CODE ASC;

-- 국가별 논문 통계 쿼리 V1.1 (2017.09.26) - 최근 5년
DROP TABLE SCOPUS_2017Y_CT_5Y_COMP_STAT CASCADE constraints PURGE;
CREATE TABLE SCOPUS_2017Y_CT_5Y_COMP_STAT AS
SELECT A.COUNTRY_CODE, A.P, A.C, A.P_BY_CO, A.C_BY_CO
FROM (
  SELECT COUNTRY_CODE, COUNT(EID) AS P, SUM(CIT_COUNT) AS C, SUM(DOC_F_CNT) P_BY_CO, SUM(CIT_T_CNT) AS C_BY_CO
  FROM SCOPUS_2017Y_COUNTRY_5Y_STATS
  GROUP BY COUNTRY_CODE
) A
ORDER BY A.P_BY_CO DESC, A.COUNTRY_CODE ASC;