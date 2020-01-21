/*
@coreawin 2018-08-20
최근 5년간(2014-2018) 논문에서 발생하는 키워드들을 20개 과학기술분류의 하위분류(4자리코드)에 대하여 찾는다.
20190801_[1]공동연구_전체데이터 통계.sql
NYEO2019_SCOPUS_DOCUMENT 논문

*/

--가-1. 중복을 제거한 전체 논문수를 구한다 (전체)
select count(distinct eid) from NYEO2019_SCOPUS_DOCUMENT;
--  결과 : 12,776,499

--가-2. 중복을 제거한 전체 논문수를 구한다 (100등이상 10건 이상)
select count(distinct eid) from NYEO2019_SCOPUS_FILTERING_DOC;
-- 결과 : 3,567,982

--나-1. 중복을 제거한 전체 피인용수를 구한다 (전체)
select sum(CIT_COUNT) from NYEO2019_SCOPUS_DOCUMENT
--결과 : 69,442,227

--나-2. 중복을 제거한 전체 피인용수를 구한다  (100등이상 10건 이상)
select sum(CIT_COUNT) from NYEO2019_SCOPUS_DOCUMENT sd
where eid in (select distinct eid from NYEO2019_SCOPUS_FILTERING_DOC);
--결과 : 24,954,870



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

COMMENT ON TABLE NYEO2019_COUNTRY_CIT IS '@coreawin 201908 작업 : 논문 ID, 논문의 연도, 국가 정보, 피인용 수 수집 :  SCOPUS  국가정보 수집시 논문 1건의 기관의 국가정보의 중복을 허용하고 수집 (factional counting 계산을 위한)';
COMMENT ON COLUMN NYEO2019_COUNTRY_CIT.EID IS '문헌 EID';
COMMENT ON COLUMN NYEO2019_COUNTRY_CIT.PUBLICATION_YEAR IS '발행연도';
COMMENT ON COLUMN NYEO2019_COUNTRY_CIT.COUNTRY_CODE IS '국가코드';
COMMENT ON COLUMN NYEO2019_COUNTRY_CIT.CIT_COUNT IS '피인용 수';

create INDEX IDX_CC_CT ON NYEO2019_COUNTRY_CIT(COUNTRY_CODE) NOLOGGING parallel 2;
create INDEX IDX_CC_EID ON NYEO2019_COUNTRY_CIT(EID) NOLOGGING parallel 2;
create INDEX IDX_CC_YEAR ON NYEO2019_COUNTRY_CIT(PUBLICATION_YEAR) NOLOGGING parallel 2;
create INDEX IDX_CC_CITCNT ON NYEO2019_COUNTRY_CIT(CIT_COUNT) NOLOGGING parallel 2;

/*
@coreawin 201908 작업 : 100등이상 10건이상 키워드를 가진 데이터의 문서 기본 정보.
*/
drop table NYEO2019_SCOPUS_DOCUMENT_F cascade constraints purge;
 create TABLE NYEO2019_SCOPUS_DOCUMENT_F as
	 select EID,PUBLICATION_YEAR,TITLE,CITATION_TYPE,SOURCE_ID,DOI,CIT_COUNT,REF_COUNT from NYEO2019_SCOPUS_DOCUMENT
	 where eid in (select distinct eid from NYEO2019_SCOPUS_FILTERING_DOC);

/*
    (가)에서 국가별 총논문수(fractional counting)를 구한다.
 - 해당 논문이에 대해 참여국가의 fractional count만큼 가지는 것으로 한다.
 -  논문에 미국, 미국, 한국 인 저자인 경우 한국은 1/3의 논문수를 가진다.
*/
/*
  @coreawin 201908 작업 : 전체 국가별 총 논문수 fractional counting
*/
--다-1. 국가별 총 논문수 Fractional counting (전체)
drop table NYEO2019_RE_PBYCO_COUNTRY cascade constraints purge;
create TABLE NYEO2019_RE_PBYCO_COUNTRY NOLOGGING AS
select country_code, ROUND(sum(fractional_count_country), 4) as PbyCo,  ROUND(sum(fractional_count_citcnt),4) as CbyCo
from (
	select country_code, country_per_eid/sumcountry_per_eid as fractional_count_country, CIT_COUNT * (country_per_eid/sumcountry_per_eid) as fractional_count_citcnt, CIT_COUNT
	from (
			select eid, country_code, country_per_eid, sum(country_per_eid) over (partition by eid) as sumcountry_per_eid, (select CIT_count from NYEO2019_SCOPUS_DOCUMENT where eid=R.EID) as CIT_COUNT
			from (
				select eid, country_code, count(country_code) as country_per_eid  from NYEO2019_COUNTRY_CIT
				--WHERE  EID IN ('0000320779', '0000572693', '0000767477')
				group by eid, country_code
			) R
	  )
  )
group by country_code
;
COMMENT ON TABLE NYEO2019_RE_PBYCO_COUNTRY IS '@coreawin 201908 작업 : 전체 국가별 총 논문수 fractional counting ';
COMMENT ON COLUMN NYEO2019_RE_PBYCO_COUNTRY.PbyCo IS '총논문수 (fractional counting)';
COMMENT ON COLUMN NYEO2019_RE_PBYCO_COUNTRY.CbyCo IS '총인용수 (fractional counting)';


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
				where  EID in  (select distinct eid from NYEO2019_SCOPUS_FILTERING_DOC)
				group by eid, country_code
			) R
	  )
  )
group by country_code
;
COMMENT ON TABLE NYEO2019_RE_PBYCO_COUNTRY_F IS '@coreawin 201908 작업 : 전체 국가별 총 논문수 fractional counting (FILTER) ';
COMMENT ON COLUMN NYEO2019_RE_PBYCO_COUNTRY_F.PbyCo IS '총논문수 (fractional counting)';
COMMENT ON COLUMN NYEO2019_RE_PBYCO_COUNTRY_F.CbyCo IS '총인용수 (fractional counting)';




--라-1. [나-1]에서 국가별 총피인용수를 구한다. 각 국가별로 전체 논문수에서 차지하는 비율을 구한다. (fractional counting) 적용
--라-1.1 국가별 총 피인용수.
SELECT country_code, SUM(cit_count)
FROM (
	SELECT DISTINCT eid, country_code, CIT_COUNT
	 FROM NYEO2019_COUNTRY_CIT
)
GROUP BY country_code
order by country_code
;

--라-1.2 각 국가별로 전체 논문수에서 차지하는 비율을 구한다. (fractional counting) 적용
select country_code, PBYCO, ROUND((pbyco/12776499)*100, 8) AS pbyco_rate, cbyco, ROUND((cbyco/69442227)*100, 8) AS cbyco_rate from NYEO2019_RE_PBYCO_COUNTRY;

--라-2.2 각 국가별로 전체 논문수에서 차지하는 비율을 구한다. (fractional counting) 적용
select country_code, PBYCO, ROUND((pbyco/12776499)*100, 8) AS pbyco_rate, cbyco, ROUND((cbyco/69442227)*100, 8) AS cbyco_rate from NYEO2019_RE_PBYCO_COUNTRY_F;



-- @coreawin 190822 100등이상 10건 이상의 키워드를 가지는 문서 아이디만 추출한 테이블이다.
--3,567,982
drop table NYEO2019_SCOPUS_FILTERING_EID cascade constraints purge;
create TABLE NYEO2019_SCOPUS_FILTERING_EID NOLOGGING AS
    select distinct eid from NYEO2019_SCOPUS_FILTERING_DOC order by eid;

COMMENT ON TABLE NYEO2019_SCOPUS_FILTERING_EID IS '@coreawin 190822 100등이상 10건 이상의 키워드를 가지는 문서 아이디만 추출한 테이블이다.';
COMMENT ON COLUMN NYEO2019_SCOPUS_FILTERING_EID.eid IS '문서 아이디';
create INDEX IDXNYEO_SFE_EID ON NYEO2019_SCOPUS_FILTERING_EID(EID) NOLOGGING parallel 2;

/**
@coreawin 20190822 각 논문들의 국가별 Fractional Counting
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

COMMENT ON TABLE NYEO2019_RE_CN_CIT_FC IS '@coreawin 20190822 각 논문들의 국가별 Fractional Counting';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.eid IS '논문 EID';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.country_code IS '국가';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.fractional_count_country IS '국가 Fractional Count';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.fractional_count_citcnt IS '피인용수 Fractional Count';
COMMENT ON COLUMN NYEO2019_RE_CN_CIT_FC.CIT_COUNT IS '해당 논문의 피인용수';

/**
@coreawin 20190822 100등이내이며 출현빈도가 10건인  키워드들만 수집한다.
*/
drop table NYEO2019_SCOPUS_FILTERING_AKEY cascade constraints purge;
create TABLE NYEO2019_SCOPUS_FILTERING_AKEY NOLOGGING AS
    select   distinct keyword from NYEO2019_SCOPUS_AKEY_FILTERING
    order by keyword
;
COMMENT ON TABLE NYEO2019_SCOPUS_FILTERING_AKEY IS '@coreawin 20190822 Filtering 된 논문들에서 출현하는 키워드들만 수집한다.';
COMMENT ON COLUMN NYEO2019_SCOPUS_FILTERING_AKEY.keyword IS '키워드';
create INDEX IDXNYEO_SFE_KEY ON NYEO2019_SCOPUS_FILTERING_AKEY(keyword) NOLOGGING parallel 2;


/**
@coreawin 20190823 키워드별 국가별 논문수, Fractional Counting 수
키워드 | 국가 | 논문 수 | Fractional Couning
*/
--마-2.1 국가별 피인용수 fractional counting, 국가별 논문수 fractional counting
drop table NYEO2019_FILTER_KEY_CN_FC cascade constraints purge;
create TABLE NYEO2019_FILTER_KEY_CN_FC NOLOGGING AS
    SELECT keyword, country_code,  ROUND(SUM(FRACTIONAL_COUNT_COUNTRY), 9) AS  PbyKeybyCo, ROUND(SUM(FRACTIONAL_COUNT_CITCNT), 9) AS CbyKeybyCo FROM (
            SELECT DISTINCT fc.eid, doc.keyword, fc.country_code, fc.FRACTIONAL_COUNT_COUNTRY , fc.FRACTIONAL_COUNT_CITCNT
            FROM   NYEO2019_RE_CN_CIT_FC fc, NYEO2019_SCOPUS_FILTERING_DOC doc
            WHERE
            fc.eid = doc.eid
            -- keyword ='BUILDING INFORMATION MODELLING'
            -- AND doc.eid IN ('84928747994', '85055504109', '84937839753' , '84949663451')

      )
      GROUP BY keyword, country_code
;
COMMENT ON TABLE NYEO2019_FILTER_KEY_CN_FC IS '@coreawin 20190823 키워드별 국가별 논문수, Fractional Counting 수 키워드 | 국가 | 논문 수 | Fractional Couning';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_FC.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_FC.country_code IS '국가';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_FC.PbyKeybyCo IS '키워드별 국가 논문 Fractional Count';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_FC.CbyKeybyCo IS '키워드별 국가 인용 Fractional Count';
create INDEX IDXNYEO_KEYCNFC_KEY ON NYEO2019_FILTER_KEY_CN_FC(keyword) NOLOGGING parallel 2;

--마-2.2 키워드별 국가의 fractional count 논문수 와 인용수
SELECT * FROM NYEO2019_FILTER_KEY_CN_FC
ORDER BY keyword, country_code
;

/**
@coreawin 20190823 키워드별 전체 fractional count 논문/피인용수
pbykey, cbykey
*/
--마-2.3 키워드별 fractional count 논문수 와 인용수
drop table NYEO2019_FILTER_KEY_CN_AFC cascade constraints purge;
create TABLE NYEO2019_FILTER_KEY_CN_AFC NOLOGGING AS
    SELECT keyword, ROUND(SUM(PbyKeybyCo), 4) AS pbykey, ROUND(SUM(CbyKeybyCo),4) as cbykey, SUM(PbyKeybyCo) AS pbykey, SUM(CbyKeybyCo) AS cbykey
    FROM NYEO2019_FILTER_KEY_CN_FC
    GROUP BY keyword
    ORDER BY keyword
;

COMMENT ON TABLE NYEO2019_FILTER_KEY_CN_AFC IS '@coreawin 20190823 키워드별 전체 fractional count 논문/피인용수 pbykey, cbykey';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_AFC.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_AFC.pbykey IS '키워드별 국가 논문 Fractional Count';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_AFC.cbykey IS '키워드별 국가 인용 Fractional Count';
create INDEX IDXNYEO_KEYCNAFC_KEY ON NYEO2019_FILTER_KEY_CN_AFC(keyword) NOLOGGING parallel 2;


/**
@coreawin 20190823 키워드별 국가별 기울기(SLOPE) 계산
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
            FROM NYEO2019_SCOPUS_FILTERING_DOC
            --WHERE KEYWORD = 'RENEWABLE ENERGY'
            GROUP BY KEYWORD, COUNTRY_CODE, PUBLICATION_YEAR
            ORDER BY KEYWORD, COUNTRY_CODE, PUBLICATION_YEAR
        )
        GROUP BY   KEYWORD, COUNTRY_CODE
    )
;

COMMENT ON TABLE NYEO2019_FILTER_KEY_CN_SLOPE IS '@coreawin 20190823 키워드별 국가별 기울기(SLOPE) 계산';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_SLOPE.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_SLOPE.country_code IS '국가';
COMMENT ON COLUMN NYEO2019_FILTER_KEY_CN_SLOPE.slope IS '성장기울기';

create INDEX IDXNYEO_KEYCNSLOPE_KEY ON NYEO2019_FILTER_KEY_CN_SLOPE(keyword) NOLOGGING parallel 2;
create INDEX IDXNYEO_KEYCNSLOPE_CN ON NYEO2019_FILTER_KEY_CN_SLOPE(COUNTRY_CODE) NOLOGGING parallel 2;

/**
@coreawin 190823  국가별 키워드 별 활동도 및 영향력
*/
--사- 2.1 키워드별 국가별 활동도 및 영향력 (
drop table NYEO2019_RE_ACT_INF cascade constraints purge;
create TABLE NYEO2019_RE_ACT_INF NOLOGGING AS
    SELECT
    country_code,
    keyword,
     DECODE(pbykey, 0, 0, ROUND(((pbykeybyco/pbykey) / (pbyco/3567982)), 4)) AS act,
    DECODE(cbykey,0, 0, ROUND(((cbykeybyco/cbykey) / (cbyco/24954870)), 4)) AS inf,
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

COMMENT ON TABLE NYEO2019_RE_ACT_INF IS '@coreawin 20190823 국가별 키워드 별 활동도 및 영향력';
COMMENT ON COLUMN NYEO2019_RE_ACT_INF.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_RE_ACT_INF.country_code IS '국가';
create INDEX IDXNYEO_REACTINF_KEY ON NYEO2019_RE_ACT_INF(keyword) NOLOGGING parallel 2;
create INDEX IDXNYEO_REACTINF_CN ON NYEO2019_RE_ACT_INF(COUNTRY_CODE) NOLOGGING parallel 2;



--사- 2.2 키워드별 국가별 활동도 및 영향력 (전체)
-- @coreawin 190906  국가별 키워드 별 활동도 및 영향력
drop table NYEO2019_RE_ACT_IN cascade constraints purge;
create TABLE NYEO2019_RE_ACT_IN NOLOGGING AS
    SELECT
    country_code,
    keyword,
     DECODE(pbykey, 0, 0, ROUND(((pbykeybyco/pbykey) / (pbyco/12776499)), 4)) AS act,
    DECODE(cbykey,0, 0, ROUND(((cbykeybyco/cbykey) / (cbyco/69442227)), 4)) AS inf,
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

COMMENT ON TABLE NYEO2019_RE_ACT_IN IS '@coreawin 20190906 국가별 키워드 별 활동도 및 영향력';
COMMENT ON COLUMN NYEO2019_RE_ACT_IN.keyword IS '키워드';
COMMENT ON COLUMN NYEO2019_RE_ACT_IN.country_code IS '국가';
create INDEX IDXNYEO_REACTIN_KEY ON NYEO2019_RE_ACT_IN(keyword) NOLOGGING parallel 2;
create INDEX IDXNYEO_REACTIN_CN ON NYEO2019_RE_ACT_IN(COUNTRY_CODE) NOLOGGING parallel 2;
















