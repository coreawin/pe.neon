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
select country_code, trunc(sum(fractional_count_country), 4) as PbyCo,  trunc(sum(fractional_count_citcnt),4) as CbyCo
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
select country_code, trunc(sum(fractional_count_country), 4) as PbyCo,  trunc(sum(fractional_count_citcnt),4) as CbyCo
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
select country_code, PBYCO, TRUNC((pbyco/12776499)*100, 8) AS pbyco_rate, cbyco, TRUNC((cbyco/69442227)*100, 8) AS cbyco_rate from NYEO2019_RE_PBYCO_COUNTRY











-- 38,821,625 (전체),  12,670,754(Filter)
select count(*) from NYEO2019_SCOPUS_UNIQ_CT_EID where eid in (select distinct eid from NYEO2019_SCOPUS_FILTERING_DOC)

drop table SCOPUS_2017_AFF_CT_STAT_KOR cascade constraints purge;
drop table NYEO2019_SCOPUS_CT_STAT_ALL cascade constraints purge;
create TABLE NYEO2019_SCOPUS_CT_STAT_ALL as
select  /*+ PARALLEL (4) */  distinct
	U.EID,
	U.PUBLICATION_YEAR,
    U.L_ASJC_CODE as ASJC_CODE,
    U.COUNTRY_CODE,
    (1 * (F_CT_CNT.COUNTRY_CNT / F_CT_CNT.COUNRY_TOTAL_CNT)) as DOC_CNT,
    (U.CIT_COUNT * (F_CT_CNT.COUNTRY_CNT / F_CT_CNT.COUNRY_TOTAL_CNT)) as CIT_CNT
from NYEO2019_SCOPUS_UNIQ_CT_EID U
inner join (
    select STATS.EID, STATS.PUBLICATION_YEAR, STATS.L_ASJC_CODE as ASJC_CODE, STATS.COUNTRY_CODE, STATS.COUNRY_TOTAL_CNT, count (STATS.COUNTRY_CODE) as COUNTRY_CNT
    from (
        select COUNTRY_STATS.EID, COUNTRY_STATS.PUBLICATION_YEAR, COUNTRY_STATS.L_ASJC_CODE, COUNTRY_STATS.COUNTRY_CODE, S_STATS.CT_CNT as COUNRY_TOTAL_CNT
        from  NYEO2019_SCOPUS_AFF_CT_EID COUNTRY_STATS
        inner join (
			select SK.EID, SK.PUBLICATION_YEAR, SK.L_ASJC_CODE as ASJC_CODE, count(SK.COUNTRY_CODE) as CT_CNT
            from NYEO2019_SCOPUS_AFF_CT_EID SK
            group by SK.EID, SK.PUBLICATION_YEAR, SK.L_ASJC_CODE
        ) S_STATS
        on 	S_STATS.EID = COUNTRY_STATS.EID
        and S_STATS.PUBLICATION_YEAR = COUNTRY_STATS.PUBLICATION_YEAR
        and S_STATS.ASJC_CODE = COUNTRY_STATS.L_ASJC_CODE
    ) STATS
    group by STATS.EID, STATS.PUBLICATION_YEAR, STATS.L_ASJC_CODE, STATS.COUNTRY_CODE, STATS.COUNRY_TOTAL_CNT
) F_CT_CNT
on U.EID = F_CT_CNT.EID
and U.PUBLICATION_YEAR = F_CT_CNT.PUBLICATION_YEAR
and U.L_ASJC_CODE = F_CT_CNT.ASJC_CODE
and U.COUNTRY_CODE = F_CT_CNT.COUNTRY_CODE
--WHERE U.COUNTRY_CODE = 'KOR';
