/**
데이터 전체에 대한 통계
각 국가별 논문 및 피인용 수 구하기.
최근 5년간(2014-2018) 논문(article, proceeding, review)에서 발생하는 키워드들을 20개 과학기술분류의 하위분류(4자리코드)에 대해서 찾는다.*/
-- @coreawin 201911 작업 : 최근 5년간 논문 데이터 구축 ar, cp, re 2014-2018

DROP TABLE NYEO2019_SCOPUS_DOCUMENT CASCADE CONSTRAINTS PURGE;
CREATE TABLE NYEO2019_SCOPUS_DOCUMENT nologging AS
    SELECT /*+ PARALLEL (4) */ EID, PUBLICATION_YEAR, TITLE, CITATION_TYPE, SOURCE_ID, DOI, CIT_COUNT, REF_COUNT
    FROM SCOPUS_2014_DOCUMENT
    WHERE PUBLICATION_YEAR BETWEEN '2014' AND '2018'
    AND CITATION_TYPE IN ('ar', 'cp', 're')
;
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_DOCUMENT IS '@coreawin 201911 작업 : 최근 5년간 논문 데이터 구축 ar, cp, re 2014-2018';
CREATE INDEX IDX_SUB_DOC_EID ON NYEO2019_SCOPUS_DOCUMENT (EID) nologging parallel 2;
CREATE INDEX IDX_SUB_DOC_PY ON NYEO2019_SCOPUS_DOCUMENT (PUBLICATION_YEAR) nologging parallel 2;
CREATE INDEX IDX_SUB_DOC_CT ON NYEO2019_SCOPUS_DOCUMENT (CITATION_TYPE) nologging parallel 2;
CREATE INDEX IDX_SUB_DOC_CC ON NYEO2019_SCOPUS_DOCUMENT (CIT_COUNT) nologging parallel 2;
CREATE INDEX IDX_SUB_RC ON NYEO2019_SCOPUS_DOCUMENT (REF_COUNT) nologging parallel 2;
CREATE INDEX IDX_SUB_DOI ON NYEO2019_SCOPUS_DOCUMENT (DOI) nologging parallel 2;

/*
@coreawin 201911. 최근 5년간 논문에서 발생한 키워드의 존재 유무를 구축한다.
AK 저자키워드
*/
drop table NYE2019_SCOPUS_KEYWORD_INFO cascade constraints purge;
CREATE TABLE NYE2019_SCOPUS_KEYWORD_INFO NOLOGGING AS
    SELECT   /*+ PARALLEL (4) */ * from (
        SELECT eid, (select distinct eid FROM SCOPUS_2014_AUTHOR_KEYWORD ak where ak.eid = sd.eid) as AK_EID
        FROM NYEO2019_SCOPUS_DOCUMENT sd
    ) where  AK_EID is not null;

COMMENT ON TABLE SCOPUS.NYE2019_SCOPUS_KEYWORD_INFO IS '@coreawin 201908. 최근 5년간 논문에서 발생한 키워드의 존재 유무를 구축한다. AK 저자키워드 ';
--CREATE INDEX IDX_NYEO_SKI_EID ON NYE2019_SCOPUS_KEYWORD_INFO (eid) nologging parallel 2;
CREATE INDEX IDX_NYEO_SKI_CT ON NYE2019_SCOPUS_KEYWORD_INFO (AK_EID) nologging parallel 2;

/*
@coreawin 201911. 최근 5년간 논문에서 발생한 저자 키워드를 구축한다.
키워드가 없는것은 제외한다.
*/
drop table NYEO2019_SCOPUS_A_KEYWORD cascade constraints purge;
create TABLE  NYEO2019_SCOPUS_A_KEYWORD NOLOGGING AS
select  /*+ PARALLEL (4) */ distinct A.EID,  A.KEYWORD
from (
select A.*
from (
  select distinct A.EID, upper(trim(regexp_replace(A.KEYWORD,'(\(.*\))'))) as KEYWORD
  from SCOPUS_2014_AUTHOR_KEYWORD A
  where A.EID in (
    select EID
    from NYE2019_SCOPUS_KEYWORD_INFO B
    where B.AK_EID is not null
  )
 ) A
 where A.KEYWORD is not null
) A;
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_A_KEYWORD IS '@coreawin 201911. 저자 키워드 정보를 수집한다. 키워드가 없는것은 제외한다.';
create index idx_nyeo_sak_eid on NYEO2019_SCOPUS_A_KEYWORD(eid) nologging parallel 2;
create index idx_nyeo_sak_kw on NYEO2019_SCOPUS_A_KEYWORD(KEYWORD) nologging parallel 2;


-- @coreawin 201911 작업 : 국가코드 및 기관정보를 수집한다.
DROP TABLE SCOPUS_2016_AFFIL_FULL CASCADE CONSTRAINTS PURGE; --과거 테이블 삭제
DROP TABLE NYEO2019_SCOPUS_AFFIL_FULL CASCADE CONSTRAINTS PURGE;
create table NYEO2019_SCOPUS_AFFIL_FULL nologging as
SELECT  /*+ PARALLEL (4) */  DISTINCT A.EID, NVL(B.AFID, A.AFID) AS AFID, NVL(B.AFFILIATION, A.AFFILIATION) AS AFFILIATION, NVL(B.COUNTRY_CODE, UPPER(NVL(A.COUNTRY_CODE, 'NONE'))) AS COUNTRY_CODE
FROM SCOPUS_2014_AFFILIATION A
LEFT OUTER JOIN SCOPUS_KISTI_AFFILIATION B
ON A.AFID = B.AFID;

COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AFFIL_FULL IS '@coreawin 201911 작업 : 국가코드 및 기관정보를 수집한다. 국가정보가 없는것은 NONE으로 처리한다.';
create index IDX_SCOPUS_2016_AFFIL_EID on NYEO2019_SCOPUS_AFFIL_FULL(EID) nologging;
--create index IDX_SCOPUS_2016_AFFIL_AFID on NYEO2019_SCOPUS_AFFIL_FULL(AFID) nologging;
--create index IDX_SCOPUS_2016_AFFIL_CT on NYEO2019_SCOPUS_AFFIL_FULL(COUNTRY_CODE) nologging;

============================================


/*
  @coreawin 201911 작업 : 논문 ID, 논문의 연도, 대분류, 분류,  국가 정보, 피인용 수 수집
    - SCOPUS 기관의 국가정보 수집시 논문 1건의 기관의 국가정보의 중복을 베제하고 수집.
*/
DROP TABLE NYEO2019_SCOPUS_UNIQ_CT_EID CASCADE CONSTRAINTS PURGE;
CREATE TABLE NYEO2019_SCOPUS_UNIQ_CT_EID NOLOGGING AS
SELECT /*+ PARALLEL (4) */  DOC_INFO.EID, DOC_INFO.PUBLICATION_YEAR, LARGE_ASJC.L_ASJC_CODE, LARGE_ASJC.ASJC_CODE, DECODE(NVL(AFF_INFO.COUNTRY_CODE, 'NONE'), '.', 'NONE', NVL(AFF_INFO.COUNTRY_CODE, 'NONE')) AS COUNTRY_CODE, DOC_INFO.CIT_COUNT
FROM NYEO2019_SCOPUS_DOCUMENT DOC_INFO
LEFT OUTER JOIN(
	SELECT DISTINCT EID, COUNTRY_CODE
	FROM NYEO2019_SCOPUS_AFFIL_FULL
) AFF_INFO
ON DOC_INFO.EID = AFF_INFO.EID
LEFT OUTER JOIN (
  SELECT DISTINCT EID, DECODE(ASJC_CODE, NULL, 'NONE',(SUBSTR(ASJC_CODE,1,2) ||'00')) AS L_ASJC_CODE, ASJC_CODE
  FROM SCOPUS_2014_CLASS_ASJC
) LARGE_ASJC
ON DOC_INFO.EID = LARGE_ASJC.EID;

COMMENT ON TABLE NYEO2019_SCOPUS_UNIQ_CT_EID IS '@coreawin 201911 작업 : 논문 ID, 논문의 연도, 대분류, 분류, 국가 정보, 피인용 수 수집, SCOPUS 기관의 국가정보 수집시 논문 1건의 기관의 국가정보의 중복을 베제하고 수집.';
COMMENT ON COLUMN NYEO2019_SCOPUS_UNIQ_CT_EID.EID IS '문헌 EID';
COMMENT ON COLUMN NYEO2019_SCOPUS_UNIQ_CT_EID.PUBLICATION_YEAR IS '발행연도';
COMMENT ON COLUMN NYEO2019_SCOPUS_UNIQ_CT_EID.L_ASJC_CODE IS 'ASJC 대분류 코드';
COMMENT ON COLUMN NYEO2019_SCOPUS_UNIQ_CT_EID.ASJC_CODE IS 'ASJC 분류 코드';
COMMENT ON COLUMN NYEO2019_SCOPUS_UNIQ_CT_EID.COUNTRY_CODE IS '국가코드';
COMMENT ON COLUMN NYEO2019_SCOPUS_UNIQ_CT_EID.CIT_COUNT IS '피인용 수';

--CREATE INDEX IDX_SCOPUS_UAFFCT_CT ON NYEO2019_SCOPUS_UNIQ_CT_EID(COUNTRY_CODE) NOLOGGING parallel 2;
CREATE INDEX IDX_SCOPUS_UAFFCT_EID ON NYEO2019_SCOPUS_UNIQ_CT_EID(EID) NOLOGGING parallel 2;
--CREATE INDEX IDX_SCOPUS_UAFFCT_YEAR ON NYEO2019_SCOPUS_UNIQ_CT_EID(PUBLICATION_YEAR) NOLOGGING parallel 2;
--CREATE INDEX IDX_SCOPUS_UAFFCT_CITCNT ON NYEO2019_SCOPUS_UNIQ_CT_EID(CIT_COUNT) NOLOGGING parallel 2;
--CREATE INDEX IDX_SCOPUS_UAFFCT_LASJC ON NYEO2019_SCOPUS_UNIQ_CT_EID(L_ASJC_CODE) NOLOGGING parallel 2;

/*
@coreawin 201911. 연도별-분야 키워드 통계를 추출 하기 위한 저자 키워드 데이터 수집 쿼리 - 국가코드 포함
 */
drop table NYEO2019_SCOPUS_AKEY_CN_BASE cascade constraints purge;
create TABLE NYEO2019_SCOPUS_AKEY_CN_BASE NOLOGGING AS
select  /*+ PARALLEL (4) */ A.EID, A.L_ASJC_CODE, A.ASJC_CODE, A.PUBLICATION_YEAR, B.KEYWORD, A.COUNTRY_CODE
from (
select distinct A.EID, A.L_ASJC_CODE, ASJC_CODE, A.PUBLICATION_YEAR, B.COUNTRY_CODE
    from NYEO2019_SCOPUS_UNIQ_CT_EID A, NYEO2019_SCOPUS_AFFIL_FULL B
    where A.EID = B.EID
) A
inner join NYEO2019_SCOPUS_A_KEYWORD B
on A.EID = B.EID
where B.KEYWORD is not null;

COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKEY_CN_BASE IS '@coreawin 201911. 저자 키워드 정보를 수집한다. 키워드가 없는것은 제외한다. 국가코드 포함.';
create INDEX IDX_NYEO_SKCB_EID ON NYEO2019_SCOPUS_AKEY_CN_BASE (EID) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_LASJC ON NYEO2019_SCOPUS_AKEY_CN_BASE (L_ASJC_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_CN ON NYEO2019_SCOPUS_AKEY_CN_BASE (COUNTRY_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_ASJC ON NYEO2019_SCOPUS_AKEY_CN_BASE (ASJC_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_KW ON NYEO2019_SCOPUS_AKEY_CN_BASE (KEYWORD) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_PY ON NYEO2019_SCOPUS_AKEY_CN_BASE (PUBLICATION_YEAR) nologging parallel 2;

/*
   @coreawin 201911 국가-분야별 키워드 랭킹(RANK()함수 이용), 백분율 (RATIO_TO_REPORT() 함수 이용)
	분야별 키워드 랭킹 및 백분율
*/
drop table NYEO2019_SCOPUS_KEYWORD_RANK cascade constraints purge;
drop table NYEO2019_SCOPUS_AKEY_CN_RANK cascade constraints purge;
create TABLE NYEO2019_SCOPUS_AKEY_CN_RANK NOLOGGING AS
select   /*+ PARALLEL (8) */ RATIO_TO_REPORT(DOC_CNT) over (partition by A.COUNTRY_CODE, A.ASJC_CODE ) as RATIO,  rank() over (partition by A.COUNTRY_CODE, A.ASJC_CODE order by DOC_CNT desc) as RANKING, A.COUNTRY_CODE, A.KEYWORD, A.ASJC_CODE, A.DOC_CNT
from (
    select A.COUNTRY_CODE, A.KEYWORD, A.ASJC_CODE, count(distinct A.EID) as DOC_CNT
    from NYEO2019_SCOPUS_AKEY_CN_BASE A
    group by A.COUNTRY_CODE, A.ASJC_CODE, A.KEYWORD
) A
order by COUNTRY_CODE, ASJC_CODE, KEYWORD
;

COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKEY_CN_RANK IS '@coreawin 201911 국가-분야별 키워드 랭킹(RANK()함수 이용), 백분율 (RATIO_TO_REPORT() 함수 이용) 분야별 키워드 랭킹 및 백분율';
create INDEX IDX_NYEO_KRR_RANKING ON NYEO2019_SCOPUS_AKEY_CN_RANK(RANKING) nologging parallel 2;
create INDEX IDX_NYEO_KRR_RATIO ON NYEO2019_SCOPUS_AKEY_CN_RANK(RATIO) nologging parallel 2;
create INDEX IDX_NYEO_KRR_CN ON NYEO2019_SCOPUS_AKEY_CN_RANK(COUNTRY_CODE) nologging parallel 2;
create INDEX IDX_NYEO_KRR_KW ON NYEO2019_SCOPUS_AKEY_CN_RANK(KEYWORD) nologging parallel 2;
create INDEX IDX_NYEO_KRR_ASJC ON NYEO2019_SCOPUS_AKEY_CN_RANK(ASJC_CODE) nologging parallel 2;


/*
  @coreawin 201908 작업 : 논문 ID, 논문의 연도, 국가 정보, 피인용 수 수집
    - SCOPUS  국가정보 수집시 논문 1건의 기관의 국가정보의 중복을 허용하고 수집 (factional counting 계산을 위한)
*/
--DROP TABLE SCOPUS_2017_UNIQ_AFF_CT_EID CASCADE CONSTRAINTS PURGE;  --과거 테이블 삭제
drop table NYEO2019_COUNTRY_CIT cascade constraints purge;
create TABLE NYEO2019_COUNTRY_CIT NOLOGGING AS
select /*+ PARALLEL (4) */  DOC_INFO.EID, DOC_INFO.PUBLICATION_YEAR, decode(nvl(AFF_INFO.COUNTRY_CODE, 'NONE'), '.', 'NONE', nvl(AFF_INFO.COUNTRY_CODE, 'NONE')) as COUNTRY_CODE, DOC_INFO.CIT_COUNT
from NYEO2019_SCOPUS_DOCUMENT DOC_INFO
left outer join(
	select EID, COUNTRY_CODE
	from NYEO2019_SCOPUS_AFFIL_FULL
) AFF_INFO
on DOC_INFO.EID = AFF_INFO.EID
--WHERE  DOC_INFO.EID IN ('0000320779')
;

COMMENT ON TABLE NYEO2019_COUNTRY_CIT IS '@coreawin 201911 작업 : 논문 ID, 논문의 연도, 국가 정보, 피인용 수 수집 :  SCOPUS  국가정보 수집시 논문 1건의 기관의 국가정보의 중복을 허용하고 수집 (factional counting 계산을 위한)';
COMMENT ON COLUMN NYEO2019_COUNTRY_CIT.EID IS '문헌 EID';
COMMENT ON COLUMN NYEO2019_COUNTRY_CIT.PUBLICATION_YEAR IS '발행연도';
COMMENT ON COLUMN NYEO2019_COUNTRY_CIT.COUNTRY_CODE IS '국가코드';
COMMENT ON COLUMN NYEO2019_COUNTRY_CIT.CIT_COUNT IS '피인용 수';

create INDEX IDX_CC_CT ON NYEO2019_COUNTRY_CIT(COUNTRY_CODE) NOLOGGING parallel 2;
create INDEX IDX_CC_EID ON NYEO2019_COUNTRY_CIT(EID) NOLOGGING parallel 2;
create INDEX IDX_CC_YEAR ON NYEO2019_COUNTRY_CIT(PUBLICATION_YEAR) NOLOGGING parallel 2;
create INDEX IDX_CC_CITCNT ON NYEO2019_COUNTRY_CIT(CIT_COUNT) NOLOGGING parallel 2;


  /*
'@coreawin 201911.. <br>
상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.
*/

drop table NYEO2019_SCOPUS_FILTER_DOC_ALL cascade constraints purge;
  create TABLE NYEO2019_SCOPUS_FILTER_DOC_ALL NOLOGGING AS
	  SELECT eid, PUBLICATION_YEAR, cb.country_code, cb.keyword, cb.asjc_code FROM  NYEO2019_SCOPUS_AKEY_CN_BASE cb
	  where cb.keyword IN (SELECT DISTINCT keyword FROM NYEO2019_SCOPUS_AKEY_FILTERING WHERE RANKING <= 100  AND DOC_CNT >=10);
	  --AND cb.KEYWORD ='3D PRINTING' AND cb.COUNTRY_CODE = 'RUS'
  ;
 COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_FILTER_DOC_ALL IS '@coreawin 201911. 각소분류에서 상위 100위 및 문서 건수 10건 이상으로 매칭된 키워드를 갖는 논문들';




--다-2. 국가별 총 논문수 Fractional counting (FILTERING)
drop table NYEO2019_RE_PBYCO_COUNTRY_F cascade constraints purge;
create TABLE NYEO2019_RE_PBYCO_COUNTRY_F NOLOGGING AS
select country_code, ROUND(sum(fractional_count_country), 4) as PbyCo,  ROUND(sum(fractional_count_citcnt),4) as CbyCo
from (
	select country_code, country_per_eid/sumcountry_per_eid as fractional_count_country, CIT_COUNT * (country_per_eid/sumcountry_per_eid) as fractional_count_citcnt, CIT_COUNT
	from (
			select eid, country_code, country_per_eid, sum(country_per_eid) over (partition by eid) as sumcountry_per_eid, (select CIT_count from NYEO2019_SCOPUS_DOCUMENT where eid=R.EID) as CIT_COUNT
			from (
				select eid, country_code, count(country_code) as country_per_eid  from NYEO2019_COUNTRY_CIT
				where  EID in  (select distinct eid from NYEO2019_SCOPUS_FILTER_DOC_ALL)
				group by eid, country_code
			) R
	  )
  )
group by country_code
;
COMMENT ON TABLE NYEO2019_RE_PBYCO_COUNTRY_F IS '@coreawin 201911 작업 : 전체 국가별 총 논문수 fractional counting (FILTER) ';
COMMENT ON COLUMN NYEO2019_RE_PBYCO_COUNTRY_F.PbyCo IS '총논문수 (fractional counting)';
COMMENT ON COLUMN NYEO2019_RE_PBYCO_COUNTRY_F.CbyCo IS '총인용수 (fractional counting)';


/**
@coreawin 20191102 각 논문들의 국가별 Fractional Counting
IRQ IRQ IRQ MYS 라면
각 논문의 국가에 대한  Fractional Counting 은
IRQ 는 0.75
MYS 는 0.25
이 테이블은 각 논문의 국가별 Fractional Counting을 계산한다.
*/

drop table NYEO2019_RE_CN_CIT_FC cascade constraints purge;
create TABLE NYEO2019_RE_CN_CIT_FC NOLOGGING AS
    select eid, country_code, country_per_eid/sumcountry_per_eid as fractional_count_country, CIT_COUNT * (country_per_eid/sumcountry_per_eid) as fractional_count_citcnt, CIT_COUNT
    from (
            select eid, country_code, country_per_eid, sum(country_per_eid) over (partition by eid) as sumcountry_per_eid, (select CIT_count from NYEO2019_SCOPUS_DOCUMENT where eid=R.EID) as CIT_COUNT
            from (
                select eid, country_code, count(country_code) as country_per_eid  from NYEO2019_COUNTRY_CIT
                --WHERE  EID IN ('0000320779', '0000572693', '0000767477')
                group by eid, country_code
            ) R
      )
--  where eid ='84878004884'
;

COMMENT ON TABLE NYEO2019_RE_CN_CIT_FC IS '@coreawin 20191102 각 논문들의 국가별 Fractional Counting';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.eid IS '논문 EID';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.country_code IS '국가';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.fractional_count_country IS '국가 Fractional Count';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.fractional_count_citcnt IS '피인용수 Fractional Count';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.CIT_COUNT IS '해당 논문의 피인용수';


/**
@coreawin 20191102 키워드별 국가별 논문수, Fractional Counting 수
키워드 | 국가 | 논문 수 | Fractional Couning
*/
--마-2.1 국가별 피인용수 fractional counting, 국가별 논문수 fractional counting
drop table NYEO2019_FILTER_KEY_CN_FC cascade constraints purge;
create TABLE NYEO2019_FILTER_KEY_CN_FC NOLOGGING AS
    SELECT keyword, country_code,  ROUND(SUM(FRACTIONAL_COUNT_COUNTRY), 9) AS  PbyKeybyCo, ROUND(SUM(FRACTIONAL_COUNT_CITCNT), 9) AS CbyKeybyCo FROM (
            SELECT DISTINCT fc.eid, doc.keyword, fc.country_code, fc.FRACTIONAL_COUNT_COUNTRY , fc.FRACTIONAL_COUNT_CITCNT
            FROM   NYEO2019_RE_CN_CIT_FC fc, NYEO2019_SCOPUS_FILTER_DOC_ALL doc
            WHERE
            fc.eid = doc.eid
            -- keyword ='BUILDING INFORMATION MODELLING'
            -- AND doc.eid IN ('84928747994', '85055504109', '84937839753' , '84949663451')

      )
      GROUP BY keyword, country_code
;
COMMENT ON TABLE NYEO2019_FILTER_KEY_CN_FC IS '@coreawin 20191102 키워드별 국가별 논문수, Fractional Counting 수 키워드 | 국가 | 논문 수 | Fractional Couning';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_FC.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_FC.country_code IS '국가';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_FC.PbyKeybyCo IS '키워드별 국가 논문 Fractional Count';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_FC.CbyKeybyCo IS '키워드별 국가 인용 Fractional Count';
create INDEX IDXNYEO_KEYCNFC_KEY ON NYEO2019_FILTER_KEY_CN_FC(keyword) NOLOGGING parallel 2;



/**
@coreawin 20191102 키워드별 국가별 기울기(SLOPE) 계산
키워드 | 국가 | SLOPE

CAGR 공식은 (마지막연도 건수 / 시작연도 건수)^(연도개수 분의 1) -1 입니다.
만약 2000년에서 2006년까지의 CAGR을 구하고자 한다면
(2006년의 논문수 / 2000년의 논문수)^(2006-2000) - 1 이 됩니다.
http://asproudofyou.tistory.com/27
그런데 만약 2000년과 2001년에 논문수가 0이면
(2006년 논문수 / 2002년 논문수)^(2006-2002) -1  이 되겠습니다.
*/

--바- 2.1 키워드별 국가별 기울기(SLOPE) 계산
drop table NYEO2019_FILTER_KEY_CN_SLOPE cascade constraints purge;
create TABLE NYEO2019_FILTER_KEY_CN_SLOPE NOLOGGING AS
    SELECT keyword, COUNTRY_CODE, ROUND(POWER((MAXYEAR/MINYEAR), 0.25) - 1, 4) AS slope FROM (
        SELECT KEYWORD, COUNTRY_CODE,
        MIN(docCnt) KEEP (DENSE_RANK FIRST ORDER BY PUBLICATION_YEAR asc)  AS MINYEAR,
        MAX(docCnt) KEEP (DENSE_RANK FIRST ORDER BY PUBLICATION_YEAR desc) AS MAXYEAR
        FROM (
            SELECT  KEYWORD, COUNTRY_CODE, PUBLICATION_YEAR, COUNT(DISTINCT eid) AS docCnt,
             rank() over (partition by KEYWORD, COUNTRY_CODE order by PUBLICATION_YEAR asc) as RANKING
            FROM NYEO2019_SCOPUS_FILTER_DOC_ALL
            --WHERE KEYWORD = 'RENEWABLE ENERGY'
            GROUP BY KEYWORD, COUNTRY_CODE, PUBLICATION_YEAR
            ORDER BY KEYWORD, COUNTRY_CODE, PUBLICATION_YEAR
        )
        GROUP BY   KEYWORD, COUNTRY_CODE
    )
;

COMMENT ON TABLE NYEO2019_FILTER_KEY_CN_SLOPE IS '@coreawin 20191102 키워드별 국가별 기울기(SLOPE) 계산';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_SLOPE.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_SLOPE.country_code IS '국가';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_SLOPE.slope IS '성장기울기';

--create INDEX IDXNYEO_KEYCNSLOPE_KEY ON NYEO2019_FILTER_KEY_CN_SLOPE(keyword) NOLOGGING parallel 2;
--create INDEX IDXNYEO_KEYCNSLOPE_CN ON NYEO2019_FILTER_KEY_CN_SLOPE(COUNTRY_CODE) NOLOGGING parallel 2;


--마-2.3 키워드별 fractional count 논문수 와 인용수
drop table NYEO2019_FILTER_KEY_CN_AFC cascade constraints purge;
create TABLE NYEO2019_FILTER_KEY_CN_AFC NOLOGGING AS
    SELECT keyword, ROUND(SUM(PbyKeybyCo), 4) AS pbykey, ROUND(SUM(CbyKeybyCo),4) as cbykey, SUM(PbyKeybyCo) AS pbykey, SUM(CbyKeybyCo) AS cbykey
    FROM NYEO2019_FILTER_KEY_CN_FC
    GROUP BY keyword
    ORDER BY keyword
;

COMMENT ON TABLE NYEO2019_FILTER_KEY_CN_AFC IS '@coreawin 20191102 키워드별 전체 fractional count 논문/피인용수 pbykey, cbykey';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_AFC.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_AFC.pbykey IS '키워드별 국가 논문 Fractional Count';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_AFC.cbykey IS '키워드별 국가 인용 Fractional Count';
create INDEX IDXNYEO_KEYCNAFC_KEY ON NYEO2019_FILTER_KEY_CN_AFC(keyword) NOLOGGING parallel 2;


--가-2. 중복을 제거한 전체 논문수를 구한다 (100등이상 10건 이상)
select count(distinct eid) from NYEO2019_SCOPUS_FILTER_DOC_ALL;
-- 결과 :7,601,871


--나-2. 중복을 제거한 전체 피인용수를 구한다  (100등이상 10건 이상)
select sum(CIT_COUNT) from NYEO2019_SCOPUS_DOCUMENT sd
where eid in (select distinct eid from NYEO2019_SCOPUS_FILTER_DOC_ALL);
--결과 : 44,455,740



/**
@coreawin 20191102  국가별 키워드 별 활동도 및 영향력
*/
--사- 2.1 키워드별 국가별 활동도 및 영향력 (
drop table NYEO2019_RE_ACT_INF cascade constraints purge;
create TABLE NYEO2019_RE_ACT_INF NOLOGGING AS
    SELECT
    country_code,
    keyword,
     DECODE(pbykey, 0, 0, ROUND(((pbykeybyco/pbykey) / (pbyco/7601871)), 4)) AS act,
    DECODE(cbykey,0, 0, ROUND(((cbykeybyco/cbykey) / (cbyco/44455740)), 4)) AS inf,
    slope
    FROM (
        SELECT slope.keyword, slope.COUNTRY_CODE, pbykeybyco, cbykeybyco,
       (SELECT pbyco FROM NYEO2019_RE_PBYCO_COUNTRY_F WHERE country_code = slope.country_code) AS pbyco,
       (SELECT cbyco FROM NYEO2019_RE_PBYCO_COUNTRY_F WHERE country_code = slope.country_code) AS cbyco,
       (SELECT pbykey FROM NYEO2019_FILTER_KEY_CN_AFC WHERE keyword = slope.keyword) AS pbykey,
       (SELECT cbykey FROM NYEO2019_FILTER_KEY_CN_AFC WHERE keyword = slope.keyword) AS cbykey,
       slope
        FROM NYEO2019_FILTER_KEY_CN_SLOPE slope, NYEO2019_FILTER_KEY_CN_FC fc
        WHERE slope.KEYWORD = fc.keyword AND slope.COUNTRY_CODE = fc.country_code
    ) R
    order by country_code, keyword
 ;

COMMENT ON TABLE NYEO2019_RE_ACT_INF IS '@coreawin 20191102 국가별 키워드 별 활동도 및 영향력';
COMMENT ON COLUMN NYEO2019_RE_ACT_INF.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_RE_ACT_INF.country_code IS '국가';
--create INDEX IDXNYEO_REACTINF_KEY ON NYEO2019_RE_ACT_INF(keyword) NOLOGGING parallel 2;
--create INDEX IDXNYEO_REACTINF_CN ON NYEO2019_RE_ACT_INF(COUNTRY_CODE) NOLOGGING parallel 2;



/* @coreawin 20191102. 연도별 키워드 발생건수
	KEYWORD | 연도 | 연도별건수
	상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.
*/
drop table NYEO2019_SCOPUS_AKMYDATA cascade constraints purge;
create TABLE NYEO2019_SCOPUS_AKMYDATA NOLOGGING AS
    SELECT keyword, publication_year, count(distinct eid) AS "키워드발생건수" FROM NYEO2019_SCOPUS_FILTER_DOC_ALL
        GROUP BY keyword, publication_year
  ;
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKMYDATA IS '@coreawin 20191102 [연도별 키워드 발생건수] 상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.';
create INDEX IDX_NYEO_AKMYDATA_KEYWORD ON NYEO2019_SCOPUS_AKMYDATA(keyword) nologging parallel 2;


/* @coreawin 20191102. 키워드 평균연도
	상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.
	KEYWORD | 총건수 | 평균연도(소수점4자리)
*/
drop table NYEO2019_SCOPUS_AKFMEANYEAR cascade constraints purge;
create TABLE NYEO2019_SCOPUS_AKFMEANYEAR NOLOGGING AS
SELECT keyword, SUM(키워드발생건수) AS TOTALCNT , TRUNC(SUM(power) / SUM(키워드발생건수), 4)  AS MEANYEAR  FROM (
	SELECT keyword, publication_year, 키워드발생건수, TO_NUMBER(키워드발생건수) * TO_NUMBER(publication_year)  AS POWER  FROM NYEO2019_SCOPUS_AKMYDATA
)
GROUP BY keyword
;
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKFMEANYEAR IS '@coreawin 20191102. [키워드 평균연도]	 상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.';
create INDEX IDX_NYEO_AKFMEANYEAR_KEYWORD ON NYEO2019_SCOPUS_AKFMEANYEAR(keyword) nologging parallel 2;



select * from NYEO2019_SCOPUS_FILTER_DOC_ALL;

-- 20191105
-- @coreawin 국가별 총 논문수.
SELECT country_code, count(DISTINCT eid) FROM NYEO2019_RE_CN_CIT_FC GROUP BY country_code ORDER BY country_code;

-- @coreawin 국가별 총 피인용수.
SELECT country_code, sum(DISTINCT c) FROM NYEO2019_RE_CN_CIT_FC GROUP BY country_code ORDER BY country_code;