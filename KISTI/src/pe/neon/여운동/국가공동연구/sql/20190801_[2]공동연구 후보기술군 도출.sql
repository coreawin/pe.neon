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