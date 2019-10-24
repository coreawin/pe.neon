/*
@coreawin 2019-10-22
20190801_[2]공동연구 후보기술군 도출.sql 파일에서 구한 다음 테이블에서.
NYEO2019_SCOPUS_FILTERING_DOC

키워드 | 대분류 | 중분류

데이터를 구한다.

대분류, 중분류는 세미콜론으로 분리되어 구축된다.
*/

drop table NYEO2019_COLLECT_ASJCCODE cascade constraints purge;
create TABLE NYEO2019_COLLECT_ASJCCODE NOLOGGING AS
SELECT keyword, asjc_code, (SUBSTR(ASJC_CODE,1,2) ||'00') as L_ASJC_CODE FROM
	(SELECT /*+ PARALLEL (4) */ DISTINCT keyword, ASJC_CODE FROM NYEO2019_SCOPUS_FILTERING_DOC)
 order by keyword;

/*
@coreawin 2019-10.24
키워드별 대분류, 중분류 항목
*/
SELECT
	keyword,
	WM_CONCAT(DISTINCT asjc_code),
	WM_CONCAT(DISTINCT l_asjc_code)
FROM
	NYEO2019_COLLECT_ASJCCODE
GROUP BY
	keyword
ORDER BY
	keyword
;

/*
@coreawin 2019-10.24
키워드별 국가, 기관, 논문수
*/

drop table NYEO2019_COLLECT_FILTERDOC_AFFINFO cascade constraints purge;
create TABLE NYEO2019_COLLECT_FDOC_AFFINFO NOLOGGING AS
	SELECT  /*+ PARALLEL (4) */ doc.keyword, aff.afid, count(DISTINCT doc.eid) AS "논문건수"
	FROM NYEO2019_SCOPUS_FILTERING_DOC doc, NYEO2019_SCOPUS_AFFIL_FULL aff
	WHERE doc.eid = aff.eid
	GROUP BY  keyword, afid
	ORDER BY keyword
;

SELECT  /*+ PARALLEL (4) */ keyword, doc.afid, aff.AFFILIATION, aff.COUNTRY_CODE, 논문건수 FROM NYEO2019_COLLECT_FDOC_AFFINFO doc, SCOPUS_KISTI_AFFILIATION aff
WHERE doc.afid (+)= aff.afid
;