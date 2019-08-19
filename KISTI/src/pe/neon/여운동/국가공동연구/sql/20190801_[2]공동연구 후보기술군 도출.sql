/*
@coreawin 201908. 최근 5년간 논문에서 발생한 키워드의 존재 유무를 구축한다.
AK 저자키워드 IK 인덱스키워드.
*/
drop table NYE2019_SCOPUS_KEYWORD_INFO cascade constraints purge;
CREATE TABLE NYE2019_SCOPUS_KEYWORD_INFO NOLOGGING AS
    SELECT   /*+ PARALLEL (4) */ * from (
        SELECT eid, (select distinct eid FROM SCOPUS_2014_INDEX_KEYWORD ik where ik.eid = sd.eid) as IK_EID,
         (select distinct eid FROM SCOPUS_2014_AUTHOR_KEYWORD ak where ak.eid = sd.eid) as AK_EID
        FROM NYEO2019_SCOPUS_DOCUMENT sd
    ) where IK_EID is not null and AK_EID is not null;

COMMENT ON TABLE SCOPUS.NYE2019_SCOPUS_KEYWORD_INFO IS '@coreawin 201908. 최근 5년간 논문에서 발생한 키워드의 존재 유무를 구축한다. AK 저자키워드 IK 인덱스키워드.';
CREATE INDEX IDX_NYEO_SKI_EID ON NYE2019_SCOPUS_KEYWORD_INFO (eid) nologging parallel 2;
CREATE INDEX IDX_NYEO_SKI_PY ON NYE2019_SCOPUS_KEYWORD_INFO (IK_EID) nologging parallel 2;
CREATE INDEX IDX_NYEO_SKI_CT ON NYE2019_SCOPUS_KEYWORD_INFO (AK_EID) nologging parallel 2;

/*
@coreawin 201908. 최근 5년간 논문에서 발생한 저자 키워드를 구축한다.
키워드가 없는것은 제외한다.
*/
drop table SCOPUS_2017Y_KEYWORD cascade constraints purge;
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
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_A_KEYWORD IS '@coreawin 201908. 저자 키워드 정보를 수집한다. 키워드가 없는것은 제외한다.';
create index idx_nyeo_sak_eid on NYEO2019_SCOPUS_A_KEYWORD(eid) nologging parallel 2;
create index idx_nyeo_sak_kw on NYEO2019_SCOPUS_A_KEYWORD(KEYWORD) nologging parallel 2;


/*
@coreawin 201908. 최근 5년간 논문에서 발생한 인덱스 키워드를 구축한다.
키워드가 없는것은 제외한다.
*/
drop table NYEO2019_SCOPUS_I_KEYWORD cascade constraints purge;
create TABLE  NYEO2019_SCOPUS_I_KEYWORD NOLOGGING AS
select  /*+ PARALLEL (4) */ distinct A.EID,  A.KEYWORD
from (
select A.*
from (
  select distinct A.EID, upper(trim(regexp_replace(A.KEYWORD,'(\(.*\))'))) as KEYWORD
  from SCOPUS_2014_INDEX_KEYWORD A
  where A.EID in (
    select EID
    from NYE2019_SCOPUS_KEYWORD_INFO B
    where B.IK_EID is not null
  )
 ) A
 where A.KEYWORD is not null
) A;
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_I_KEYWORD IS '@coreawin 201908. 인덱스 키워드 정보를 수집한다. 키워드가 없는것은 제외한다.';
create index idx_nyeo_sak_eid on NYEO2019_SCOPUS_I_KEYWORD(eid) nologging parallel 2;
create index idx_nyeo_sak_kw on NYEO2019_SCOPUS_I_KEYWORD(KEYWORD) nologging parallel 2;


/*
@coreawin 201908. 연도별-분야 키워드 통계를 추출 하기 위한 기본 키워드 데이터 수집 쿼리
	EID : 논문 EID
	L_ASJC_CODE : ASJC 대분야
	ASJC_CODE : ASJC 분야
	PUBLICATION_YEAR : 발행연도
	KEYWORD : 키워드
 */
--drop table SCOPUS_2017Y_KEYWORD_BASE cascade constraints purge;
drop table NYEO2019_SCOPUS_KEYWORD_BASE cascade constraints purge;
create TABLE NYEO2019_SCOPUS_KEYWORD_BASE NOLOGGING AS
select  /*+ PARALLEL (4) */ A.EID, A.L_ASJC_CODE, A.ASJC_CODE, A.PUBLICATION_YEAR, B.KEYWORD
from (
	select distinct A.EID, A.L_ASJC_CODE, ASJC_CODE, A.PUBLICATION_YEAR
    from NYEO2019_SCOPUS_UNIQ_CT_EID A
) A
inner join NYEO2019_SCOPUS_A_KEYWORD B
on A.EID = B.EID
where B.KEYWORD is not null;

COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_KEYWORD_BASE IS '@coreawin 201908. 저자 키워드 정보를 수집한다. 키워드가 없는것은 제외한다.';
create INDEX IDX_NYEO_SKB_EID ON NYEO2019_SCOPUS_KEYWORD_BASE (EID) nologging parallel 2;
create INDEX IDX_NYEO_SKB_LASJC ON NYEO2019_SCOPUS_KEYWORD_BASE (L_ASJC_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SKB_ASJC ON NYEO2019_SCOPUS_KEYWORD_BASE (ASJC_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SKB_KW ON NYEO2019_SCOPUS_KEYWORD_BASE (KEYWORD) nologging parallel 2;
create INDEX IDX_NYEO_SKB_PY ON SCOPUS_2017Y_KEYWORD_BASE (PUBLICATION_YEAR) nologging parallel 2;


/*
@coreawin 201908. 연도별-분야 키워드 통계를 추출 하기 위한 저자 키워드 데이터 수집 쿼리 - 국가코드 포함
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

COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKEY_CN_BASE IS '@coreawin 201908. 저자 키워드 정보를 수집한다. 키워드가 없는것은 제외한다. 국가코드 포함.';
create INDEX IDX_NYEO_SKCB_EID ON NYEO2019_SCOPUS_AKEY_CN_BASE (EID) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_LASJC ON NYEO2019_SCOPUS_AKEY_CN_BASE (L_ASJC_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_CN ON NYEO2019_SCOPUS_AKEY_CN_BASE (COUNTRY_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_ASJC ON NYEO2019_SCOPUS_AKEY_CN_BASE (ASJC_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_KW ON NYEO2019_SCOPUS_AKEY_CN_BASE (KEYWORD) nologging parallel 2;
create INDEX IDX_NYEO_SKCB_PY ON NYEO2019_SCOPUS_AKEY_CN_BASE (PUBLICATION_YEAR) nologging parallel 2;


/*
   @coreawin 201908 국가-분야별 키워드 랭킹(RANK()함수 이용), 백분율 (RATIO_TO_REPORT() 함수 이용)
	분야별 키워드 랭킹 및 백분율
*/
drop table SCOPUS_2017Y_KEYWORD_RANKING cascade constraints purge;
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

COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKEY_CN_RANK IS '@coreawin 201908 국가-분야별 키워드 랭킹(RANK()함수 이용), 백분율 (RATIO_TO_REPORT() 함수 이용) 분야별 키워드 랭킹 및 백분율';
create INDEX IDX_NYEO_KRR_RANKING ON NYEO2019_SCOPUS_AKEY_CN_RANK(RANKING) nologging parallel 2;
create INDEX IDX_NYEO_KRR_RATIO ON NYEO2019_SCOPUS_AKEY_CN_RANK(RATIO) nologging parallel 2;
create INDEX IDX_NYEO_KRR_CN ON NYEO2019_SCOPUS_AKEY_CN_RANK(COUNTRY_CODE) nologging parallel 2;
create INDEX IDX_NYEO_KRR_KW ON NYEO2019_SCOPUS_AKEY_CN_RANK(KEYWORD) nologging parallel 2;
create INDEX IDX_NYEO_KRR_ASJC ON NYEO2019_SCOPUS_AKEY_CN_RANK(ASJC_CODE) nologging parallel 2;


/*위 테이블 검증로직 (샘플 데이터)*/
select   /*+ PARALLEL (8) */ RATIO_TO_REPORT(DOC_CNT) over (partition by A.COUNTRY_CODE, A.ASJC_CODE ) as RATIO,  rank() over (partition by A.COUNTRY_CODE, A.ASJC_CODE order by DOC_CNT desc) as RANKING, A.COUNTRY_CODE, A.KEYWORD, A.ASJC_CODE, A.DOC_CNT
from (
select   A.COUNTRY_CODE,
         A.KEYWORD,
         A.ASJC_CODE,
         count(distinct A.EID) as DOC_CNT
from     NYEO2019_SCOPUS_AKEY_CN_BASE A
WHERE    (KEYWORD      ='CANCER' OR KEYWORD ='ANGOLA')
AND      COUNTRY_CODE = 'AGO'
group by A.COUNTRY_CODE,
         A.ASJC_CODE,
         A.KEYWORD

) A
ORDER BY A.ASJC_CODE
;

/* 2019.08.09  다음 메일 수집조건 변경.
아래 조건으로 해 주시면 됩니다.
분야별 100개를 뽑되 분야별로 최소10번이상은 나와야 합니다.
(분야별 100개) and (분야별최소10번이상)
*/

drop table NYEO2019_SCOPUS_AKEY_FILTERING cascade constraints purge;
create TABLE NYEO2019_SCOPUS_AKEY_FILTERING NOLOGGING AS
    SELECT /*+ PARALLEL (8) */ *
    FROM   NYEO2019_SCOPUS_AKEY_CN_RANK
    WHERE   RANKING <= 100  AND DOC_CNT >=10;
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKEY_FILTERING IS '@coreawin 201908 분야별 100개를 뽑되 분야별로 최소10번이상은 나와야 합니다. 국가-분야별 키워드 랭킹(RANK()함수 이용), 백분율 (RATIO_TO_REPORT() 함수 이용) 분야별 키워드 랭킹 및 백분율';
create INDEX IDX_NYEO_SAF_RANKING ON NYEO2019_SCOPUS_AKEY_FILTERING(RANKING) nologging parallel 2;
create INDEX IDX_NYEO_SAF_RATIO ON NYEO2019_SCOPUS_AKEY_FILTERING(RATIO) nologging parallel 2;
create INDEX IDX_NYEO_SAF_CN ON NYEO2019_SCOPUS_AKEY_FILTERING(COUNTRY_CODE) nologging parallel 2;
create INDEX IDX_NYEO_SAF_KW ON NYEO2019_SCOPUS_AKEY_FILTERING(KEYWORD) nologging parallel 2;
create INDEX IDX_NYEO_SAF_ASJC ON NYEO2019_SCOPUS_AKEY_FILTERING(ASJC_CODE) nologging parallel 2;


/* @coreawin 2019.08.14  키워드별 국가별 연도별 건수 구하기
    수집 대상 ; 분야별 100개를 뽑되 분야별로 최소10번이상은 나와야 합니다.
 */
drop table NYEO2019_SCOPUS_AKEY_KCYDATA cascade constraints purge;
create TABLE NYEO2019_SCOPUS_AKEY_KCYDATA NOLOGGING AS
        SELECT  /*+ PARALLEL (8) */ COUNT(k.eid) as count, k.keyword, publication_year, af.COUNTRY_CODE FROM NYEO2019_SCOPUS_A_KEYWORD k, NYEO2019_SCOPUS_AKEY_CN_BASE sd, NYEO2019_SCOPUS_AKEY_FILTERING af
    WHERE
    af.keyword = k.keyword and k.EID = sd.EID
    GROUP BY
    k.keyword, af.country_code, publication_year
    ;
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKEY_KCYDATA IS '@coreawin [키워드별 국가별 연도별 건수 구하기 ] 201908 분야별 100개를 뽑되 분야별로 최소10번이상은 나와야 합니다. 국가-분야별 키워드 랭킹(RANK()함수 이용), 백분율 (RATIO_TO_REPORT() 함수 이용) 분야별 키워드 랭킹 및 백분율';
create INDEX IDX_NYEO_AKCYD_CNT ON NYEO2019_SCOPUS_AKEY_KCYDATA(COUNT) nologging parallel 2;
create INDEX IDX_NYEO_AKCYD_PY ON NYEO2019_SCOPUS_AKEY_KCYDATA(publication_year) nologging parallel 2;
create INDEX IDX_NYEO_AKCYD_CN ON NYEO2019_SCOPUS_AKEY_KCYDATA(COUNTRY_CODE) nologging parallel 2;
create INDEX IDX_NYEO_AKCYD_KW ON NYEO2019_SCOPUS_AKEY_KCYDATA(KEYWORD) nologging parallel 2;


  /*
'@coreawin 201908.. <br>
상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.
*/
  drop table NYEO2019_SCOPUS_FILTERING_DOC cascade constraints purge;
  create TABLE NYEO2019_SCOPUS_FILTERING_DOC NOLOGGING AS
	  SELECT eid, PUBLICATION_YEAR, cb.country_code, cb.keyword, cb.asjc_code FROM  NYEO2019_SCOPUS_AKEY_CN_BASE cb, NYEO2019_SCOPUS_AKEY_FILTERING target
	  where cb.keyword = target.KEYWORD  AND target.asjc_code = cb.ASJC_CODE AND target.country_code = cb.COUNTRY_CODE AND (RANKING <= 100  AND DOC_CNT >=10)
	  --AND cb.KEYWORD ='100% RENEWABLE ENERGY'
  ;
 COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_FILTERING_DOC IS '@coreawin 201908.상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들';


/* @coreawin 201908. 키워드별 총 건수 및 평균연도구하기
	KEYWORD | 총건수 | 평균연도(소수점4자리)
	@deprecated 사용하지 않는다.
*/
--drop table NYEO2019_SCOPUS_AKEY_MYEARDATA cascade constraints purge;
--create TABLE NYEO2019_SCOPUS_AKEY_MYEARDATA NOLOGGING AS
--	SELECT/*+ PARALLEL (8) */  keyword, SUM(count) AS TOTALCNT , TRUNC(SUM(power) / SUM(count), 4)  AS MEANYEAR
--  FROM (
--		SELECT keyword, count, publication_year, TO_NUMBER(count) * TO_NUMBER(publication_year) AS POWER, country_code
--		FROM   NYEO2019_SCOPUS_AKEY_KCYDATA
--	)
--	GROUP BY keyword ;

--COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKEY_MYEARDATA IS '@coreawin 201908 [키워드별 총 건수 및 평균연도구하기 ] 키워드별 국가별 연도별 건수 구하기 201908 분야별 100개를 뽑되 분야별로 최소10번이상은 나와야 합니다. 국가-분야별 키워드 랭킹(RANK()함수 이용), 백분율 (RATIO_TO_REPORT() 함수 이용) 분야별 키워드 랭킹 및 백분율';
--create INDEX IDX_NYEO_AKMYEAR_KEYWORD ON NYEO2019_SCOPUS_AKEY_MYEARDATA(keyword) nologging parallel 2;

/* @coreawin 201908. 연도별 키워드 발생건수
	KEYWORD | 연도 | 연도별건수
	상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.
*/
drop table NYEO2019_SCOPUS_AKMYDATA cascade constraints purge;
create TABLE NYEO2019_SCOPUS_AKMYDATA NOLOGGING AS
    SELECT keyword, publication_year, count(eid) AS "키워드발생건수" FROM NYEO2019_SCOPUS_FILTERING_DOC
        GROUP BY keyword, publication_year
  ;
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKMYDATA IS '@coreawin 201908 [연도별 키워드 발생건수] 상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.';
create INDEX IDX_NYEO_AKMYDATA_KEYWORD ON NYEO2019_SCOPUS_AKMYDATA(keyword) nologging parallel 2;


/* @coreawin 201908. 키워드 평균연도
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
COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKFMEANYEAR IS '@coreawin 201908. [키워드 평균연도]	 상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.';
create INDEX IDX_NYEO_AKFMEANYEAR_KEYWORD ON NYEO2019_SCOPUS_AKFMEANYEAR(keyword) nologging parallel 2;





/* @coreawin 201908. 키워드별 국가별 건수 <br>
	KEYWORD | 국가 | 건수 <br>
	수집 대상 ; 분야별 100개를 뽑되 분야별로 최소10번이상은 나와야 합니다. <br>
*/
drop table NYEO2019_SCOPUS_AKCYDATA cascade constraints purge;
create TABLE NYEO2019_SCOPUS_AKCYDATA NOLOGGING AS
SELECT keyword, country_code, COUNT(eid) AS "국가별키워드건수" FROM NYEO2019_SCOPUS_FILTERING_DOC  GROUP BY KEYWORD, country_code ;

COMMENT ON TABLE SCOPUS.NYEO2019_SCOPUS_AKCYDATA IS '@coreawin 201908 [국가별  키워드 발생건수] 상위 100위 및 문서 건수 10건 이상의 국가별 분류별 키워드에 해당하는 논문들.';
create INDEX IDX_NYEO_AKCYDATA_KEYWORD ON NYEO2019_SCOPUS_AKCYDATA(keyword) nologging parallel 2;