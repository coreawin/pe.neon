-- 최근 5년 키워드 통계
DROP TABLE SCOPUS_2017Y_5Y_KEY_STATS CASCADE constraints PURGE;
CREATE TABLE SCOPUS_2017Y_5Y_KEY_STATS NOLOGGING AS
SELECT A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR, A.DOC_CNT
FROM (
    SELECT A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR, COUNT(DISTINCT EID) AS DOC_CNT
    FROM SCOPUS_2017Y_KEYWORD_BASE A
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
    ) AND A.PUBLICATION_YEAR BETWEEN '2012' AND '2016'
    GROUP BY A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR
) A
INNER JOIN (
    SELECT DISTINCT KEYWORD, L_ASJC_CODE
    FROM SCOPUS_2017Y_5Y_KEY_RANKING
    WHERE RANKING <= 1000
    ORDER BY KEYWORD
) B
ON A.KEYWORD = B.KEYWORD AND A.L_ASJC_CODE = B.L_ASJC_CODE;

CREATE INDEX IDX_SCOPUS_2017Y_5Y_KEY_S_KW ON SCOPUS_2017Y_5Y_KEY_STATS(KEYWORD);
CREATE INDEX IDX_SCOPUS_2017Y_5Y_KEY_S_AC ON SCOPUS_2017Y_5Y_KEY_STATS(L_ASJC_CODE);
CREATE INDEX IDX_SCOPUS_2017Y_5Y_KEY_S_PY ON SCOPUS_2017Y_5Y_KEY_STATS(PUBLICATION_YEAR);
CREATE INDEX IDX_SCOPUS_2017Y_5Y_KEY_S_RNK ON SCOPUS_2017Y_5Y_KEY_STATS(RANKING);

-- 최근 5년 연도별 키워드 통계
DROP TABLE SCOPUS_2017Y_KEY_5Y_STATS CASCADE constraints PURGE;
CREATE TABLE SCOPUS_2017Y_KEY_5Y_STATS NOLOGGING AS
SELECT A.KEYWORD, A.PUBLICATION_YEAR, A.DOC_CNT
FROM (
    SELECT A.KEYWORD, A.PUBLICATION_YEAR, COUNT(DISTINCT EID) AS DOC_CNT
    FROM SCOPUS_2017Y_KEYWORD_BASE A
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
    ) AND A.KEYWORD IN (
	      SELECT DISTINCT KEYWORD
	      FROM SCOPUS_2017Y_5Y_KEY_RANKING
	      WHERE RANKING <= 1000
    ) AND A.PUBLICATION_YEAR BETWEEN '2012' AND '2016'
    GROUP BY A.KEYWORD, A.PUBLICATION_YEAR
) A;

CREATE INDEX IDX_SCOPUS_2017Y_KEY_5Y_S_KW ON SCOPUS_2017Y_KEY_5Y_STATS(KEYWORD);
CREATE INDEX IDX_SCOPUS_2017Y_KEY_5Y_S_PY ON SCOPUS_2017Y_KEY_5Y_STATS(PUBLICATION_YEAR);

-- 국가별 키워드 통계 추출을 위한 통계 수집 데이터 (최근 5년)
DROP TABLE SCOPUS_2017Y_5Y_CT_KEY_BASE CASCADE CONSTRAINTS PURGE;
CREATE TABLE SCOPUS_2017Y_5Y_CT_KEY_BASE AS
SELECT DISTINCT A.KEYWORD, B.COUNTRY_CODE, B.EID, B.CIT_COUNT, (1 * (B.COUNTRY_CNT / B.COUNRY_TOTAL_CNT)) AS DOC_F_CNT, (B.CIT_COUNT * (B.COUNTRY_CNT / B.COUNRY_TOTAL_CNT)) AS CIT_T_CNT
FROM SCOPUS_2017Y_KEYWORD A
INNER JOIN (
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
		) AND A.PUBLICATION_YEAR BETWEEN '2012' AND '2016'
	) A
	INNER JOIN (
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
				WHERE COUNTRY_STATS.PUBLICATION_YEAR BETWEEN '2012' AND '2016'
			) STATS
			GROUP BY STATS.EID, STATS.PUBLICATION_YEAR, STATS.L_ASJC_CODE, STATS.COUNTRY_CODE, STATS.COUNRY_TOTAL_CNT
		) IN_STATS
	) B
	ON A.EID = B.EID AND A.COUNTRY_CODE = B.COUNTRY_CODE
) B
ON A.EID = B.EID;

CREATE INDEX IDX_SCOPUS_2017Y_5YCTS_CT ON SCOPUS_2017Y_5Y_CT_KEY_BASE (COUNTRY_CODE);
CREATE INDEX IDX_SCOPUS_2017Y_5YCTS_EID ON SCOPUS_2017Y_5Y_CT_KEY_BASE (EID);
CREATE INDEX IDX_SCOPUS_2017Y_5YCTS_CICNT ON SCOPUS_2017Y_5Y_CT_KEY_BASE (CIT_COUNT);
CREATE INDEX IDX_SCOPUS_2017Y_5YCTS_DCNT ON SCOPUS_2017Y_5Y_CT_KEY_BASE (DOC_F_CNT);
CREATE INDEX IDX_SCOPUS_2017Y_5YCTS_CCNT ON SCOPUS_2017Y_5Y_CT_KEY_BASE (CIT_T_CNT);

-- 국가별 키워드 통계 추출 (최근 5년)
DROP TABLE SCOPUS_2017Y_5Y_CT_KEY_STATS CASCADE CONSTRAINTS PURGE;
CREATE TABLE SCOPUS_2017Y_5Y_CT_KEY_STATS NOLOGGING AS
SELECT  A.KEYWORD, A.COUNTRY_CODE, SUM(A.DOC_F_CNT) AS D_CNT, SUM(A.CIT_T_CNT)  AS C_CNT
FROM
(
  SELECT A.KEYWORD, A.COUNTRY_CODE, A.DOC_F_CNT, A.CIT_T_CNT
  FROM SCOPUS_2017Y_5Y_CT_KEY_BASE A
  INNER JOIN (
        SELECT DISTINCT KEYWORD
	 FROM SCOPUS_2017Y_5Y_KEY_RANKING
	 WHERE RANKING <= 1000
  )B
  ON A.KEYWORD = B.KEYWORD
) A
GROUP BY A.KEYWORD, A.COUNTRY_CODE
ORDER BY A.KEYWORD, A.COUNTRY_CODE;

CREATE INDEX IDX_SCOPUS_2017Y_5YCTK_KW ON SCOPUS_2017Y_5Y_CT_KEY_STATS(KEYWORD);
CREATE INDEX IDX_SCOPUS_2017Y_5YCTK_CT ON SCOPUS_2017Y_5Y_CT_KEY_STATS(COUNTRY_CODE);

-- 전체 키워드 문서 건수, 총 피인용 수 추출 (최근 5년)
DROP TABLE SCOPUS_2017Y_CT_5Y_KEY_TOT CASCADE CONSTRAINTS PURGE;
CREATE TABLE SCOPUS_2017Y_CT_Y_KEY_TOT NOLOGGING AS
SELECT A.KEYWORD, DOC_CNT, CIT_T_CNT
FROM (
     SELECT A.KEYWORD, COUNT(EID) AS DOC_CNT, COUNT(CIT_COUNT) AS CIT_T_CNT
     FROM (
       SELECT DISTINCT KEYWORD, EID, CIT_COUNT
       FROM  SCOPUS_2017Y_5Y_CT_KEY_BASE
     ) A
     GROUP BY KEYWORD
) A;

CREATE INDEX IDX_SCOPUS_2017Y_5YKWD ON SCOPUS_2017Y_CT_5Y_KEY_TOT(KEYWORD);


-- 국가별, 키워드 활동도, 영향력 통계 추출 (최근 5년)
DROP TABLE SCOPUS_2017Y_5Y_CT_KEY_ACTIVE CASCADE CONSTRAINTS PURGE;
CREATE TABLE  SCOPUS_2017Y_5Y_CT_KEY_ACTIVE NOLOGGING AS
SELECT
   A.KEYWORD,
   A.COUNTRY_CODE,
   -- 활동도 계산
   ((A.P_BY_KEY_BY_CO/P_BY_KEY) / (A.P_BY_CO / A.P)) AS ACTIVATION,
   -- 영향력 계산
   ((A.C_BY_KEY_BY_CO/C_BY_KEY) / (A.C_BY_CO / A.C)) AS EFFECTION
FROM (
	SELECT
    	 A.KEYWORD,
    	 A.COUNTRY_CODE,
         A.P_BY_KEY_BY_CO,
         A.C_BY_KEY_BY_CO,
         (SELECT I.P FROM SCOPUS_2017Y_5Y_DOC_STATS I) AS P,
         (SELECT J.C FROM SCOPUS_2017Y_5Y_DOC_STATS J) AS C,
         (SELECT J.P_BY_CO FROM SCOPUS_2017Y_CT_COMPLETE_STAT J WHERE J.COUNTRY_CODE = A.COUNTRY_CODE) AS P_BY_CO,
         (SELECT J.C_BY_CO FROM SCOPUS_2017Y_CT_COMPLETE_STAT J WHERE J.COUNTRY_CODE = A.COUNTRY_CODE) AS C_BY_CO,
         (SELECT K.DOC_CNT FROM SCOPUS_2017Y_CT_KEYWORD_TOT K WHERE K.KEYWORD = A.KEYWORD) AS P_BY_KEY,
         (SELECT L.CIT_T_CNT FROM SCOPUS_2017Y_CT_KEYWORD_TOT L WHERE L.KEYWORD = A.KEYWORD) AS C_BY_KEY,
    FROM (
       SELECT  A.KEYWORD, A.COUNTRY_CODE,A.D_CNT AS P_BY_KEY_BY_CO, A.C_CNT AS C_BY_KEY_BY_CO
       FROM SCOPUS_2017Y_CT_KEYWORD_STATS A
    ) A
) A;