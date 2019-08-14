/* 최근 10년 간 논문의 키워드 수집 */
drop table SCOPUS_2017Y_KEYWORD cascade constraints purge;
create TABLE  SCOPUS_2017Y_KEYWORD NOLOGGING AS
/* Index 키워드 수집 */
select distinct A.EID,  A.KEYWORD
from (
select A.*
from (
  select distinct A.EID, upper(trim(regexp_replace(A.KEYWORD,'(\(.*\))'))) as KEYWORD
  from SCOPUS_2014_AUTHOR_KEYWORD A
  where A.EID in (
    select B.EID
    from COLLECT_IK_AK_ID B
    where B.AK_EID is not null
  )
 ) A
 where A.KEYWORD is not null
) A;

create index idx_scopus_2017Y_k_eid on SCOPUS_2017Y_KEYWORD(eid);
create index idx_scopus_2017Y_k_kw on SCOPUS_2017Y_KEYWORD(KEYWORD);


-- 연도별-분야 키워드 통계를 추출 하기 위한 기본 데이터 수집 쿼리
drop table SCOPUS_2017Y_KEYWORD_BASE cascade constraints purge;
create TABLE SCOPUS_2017Y_KEYWORD_BASE NOLOGGING AS
/*
	EID : 논문 EID
	L_ASJC_CODE : ASJC 대분야
	PUBLICATION_YEAR : 발행연도
	KEYWORD : 키워드
*/
select A.EID, A.L_ASJC_CODE, A.PUBLICATION_YEAR, B.KEYWORD
from (
	select distinct A.EID, A.L_ASJC_CODE, A.PUBLICATION_YEAR
    from SCOPUS_2017_UNIQ_AFF_CT_EID A
) A
inner join SCOPUS_2017Y_KEYWORD B
on A.EID = B.EID
where B.KEYWORD is not null;

create INDEX IDX_SCOPUS_2017Y_KB_EID ON SCOPUS_2017Y_KEYWORD_BASE (EID);
create INDEX IDX_SCOPUS_2017Y_KB_ASJC ON SCOPUS_2017Y_KEYWORD_BASE (L_ASJC_CODE);
create INDEX IDX_SCOPUS_2017Y_KB_KW ON SCOPUS_2017Y_KEYWORD_BASE (KEYWORD);
create INDEX IDX_SCOPUS_2017Y_KB_PY ON SCOPUS_2017Y_KEYWORD_BASE (PUBLICATION_YEAR);

-- 최근 10년 간 분야별 키워드 랭킹
drop table SCOPUS_2017Y_KEYWORD_RANKING cascade constraints purge;
create TABLE SCOPUS_2017Y_KEYWORD_RANKING NOLOGGING AS

/*
	RANK() OVER (PARTITION BY A.L_ASJC_CODE ORDER BY DOC_CNT DESC) : 분야별 논문 건수가 많은 키워드에 대한 랭크
	KEYWORD : 키워드
	L_ASJC_CODE : ASJC 대분야
	DOC_CNT : 키워드
*/
select rank() over (partition by A.L_ASJC_CODE order by DOC_CNT desc) as RANKING, A.KEYWORD, A.L_ASJC_CODE, A.DOC_CNT
from (
    select A.KEYWORD, A.L_ASJC_CODE, count(distinct A.EID) as DOC_CNT
    from SCOPUS_2017Y_KEYWORD_BASE A
    where A.L_ASJC_CODE in ('1000', '1100',
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
    group by A.KEYWORD, A.L_ASJC_CODE
) A;
create INDEX IDX_SCOPUS_2017Y_KEY_R_RANKING ON SCOPUS_2017Y_KEYWORD_RANKING(RANKING);
create INDEX IDX_SCOPUS_2017Y_KEY_R_KW ON SCOPUS_2017Y_KEYWORD_RANKING(KEYWORD);
create INDEX IDX_SCOPUS_2017Y_KEY_R_ASJC ON SCOPUS_2017Y_KEYWORD_RANKING(L_ASJC_CODE);

-- 최근 5년 분야별 키워드 랭킹
drop table SCOPUS_2017Y_5Y_KEY_RANKING cascade constraints purge;
create TABLE SCOPUS_2017Y_5Y_KEY_RANKING NOLOGGING AS

/*
	RANK() OVER (PARTITION BY A.L_ASJC_CODE ORDER BY DOC_CNT DESC) : 분야별 논문 건수가 많은 키워드에 대한 랭크
	KEYWORD : 키워드
	L_ASJC_CODE : ASJC 대분야
	DOC_CNT : 키워드
*/
select rank() over (partition by A.L_ASJC_CODE order by DOC_CNT desc) as RANKING, A.KEYWORD, A.L_ASJC_CODE, A.DOC_CNT
from (
    select A.KEYWORD, A.L_ASJC_CODE, count(distinct A.EID) as DOC_CNT
    from SCOPUS_2017Y_KEYWORD_BASE A
    where A.L_ASJC_CODE in ('1000', '1100',
        '1300', '1500',
        '1600', '1700',
        '1800', '1900',
        '2100', '2200',
        '2300', '2400',
        '2500', '2600',
        '2700', '2800',
        '3000', '3100',
        '3400', '3500'
    ) and A.PUBLICATION_YEAR between '2012' and '2016'
    group by A.KEYWORD, A.L_ASJC_CODE
) A;
create INDEX IDX_SCOPUS_2017Y_KEY_5Y_R_RANK ON SCOPUS_2017Y_5Y_KEY_RANKING(RANKING);
create INDEX IDX_SCOPUS_2017Y_KEY_5Y_R_KW ON SCOPUS_2017Y_5Y_KEY_RANKING(KEYWORD);
create INDEX IDX_SCOPUS_2017Y_KEY_5Y_R_ASJC ON SCOPUS_2017Y_5Y_KEY_RANKING(L_ASJC_CODE);


-- 연도-분야별 키워드 통계
drop table SCOPUS_2017Y_KEYWORD_STATS cascade constraints purge;
create TABLE SCOPUS_2017Y_KEYWORD_STATS NOLOGGING AS
/*
	RANKING : 분야별 키워드 랭킹
	KEYWORD : 키워드
	L_ASJC_CODE : ASJC 대분야
	PUBLICATION_YEAR : 발행 연도
	DOC_CNT : PByKeyByYear -> 연도별 키워드 논문 건수
*/
select B.RANKING, A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR, A.DOC_CNT
from (
    select A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR, count(distinct EID) as DOC_CNT
    from SCOPUS_2017Y_KEYWORD_BASE A
    where A.L_ASJC_CODE in ('1000', '1100',
        '1300', '1500',
        '1600', '1700',
        '1800', '1900',
        '2100', '2200',
        '2300', '2400',
        '2500', '2600',
        '2700', '2800',
        '3000', '3100',
        '3400', '3500'
    ) and
    GROUP BY A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR
) A
INNER JOIN (
	/*
		분야별 1000위까지 순위의 키워드 추출
		RANKING : 분야별 키워드 랭킹
		KEYWORD : 키워드
		L_ASJC_CODE : ASJC 대분야
		PUBLICATION_YEAR : 발행 연도
		DOC_CNT : PByKeyByYear -> 연도별 키워드 논문 건수
	*/
    SELECT DISTINCT KEYWORD, L_ASJC_CODE
    FROM SCOPUS_2017Y_KEYWORD_RANKING
    WHERE RANKING <= 1000
    ORDER BY KEYWORD
) B
ON A.KEYWORD = B.KEYWORD and A.L_ASJC_CODE = B.L_ASJC_CODE;

create INDEX IDX_SCOPUS_2017Y_KEY_S_KW ON SCOPUS_2017Y_KEYWORD_STATS(KEYWORD);
create INDEX IDX_SCOPUS_2017Y_KEY_S_ASJC ON SCOPUS_2017Y_KEYWORD_STATS(L_ASJC_CODE);
create INDEX IDX_SCOPUS_2017Y_KEY_S_PY ON SCOPUS_2017Y_KEYWORD_STATS(PUBLICATION_YEAR);
create INDEX IDX_SCOPUS_2017Y_KEY_S_RANK ON SCOPUS_2017Y_KEYWORD_STATS(RANKING);


-- 최근 5년 키워드 통계
drop table SCOPUS_2017Y_5Y_KEY_STATS cascade constraints purge;
create TABLE SCOPUS_2017Y_5Y_KEY_STATS NOLOGGING AS
/*
	RANKING : 분야별 키워드 랭킹
	KEYWORD : 키워드
	L_ASJC_CODE : ASJC 대분야
	PUBLICATION_YEAR : 발행 연도
	DOC_CNT : PByKeyByYear -> 연도별 키워드 논문 건수
*/
select B.RANKING, A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR, A.DOC_CNT
from (
    select A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR, count(distinct EID) as DOC_CNT
    from SCOPUS_2017Y_KEYWORD_BASE A
    where A.L_ASJC_CODE in ('1000', '1100',
        '1300', '1500',
        '1600', '1700',
        '1800', '1900',
        '2100', '2200',
        '2300', '2400',
        '2500', '2600',
        '2700', '2800',
        '3000', '3100',
        '3400', '3500'
    ) and A.PUBLICATION_YEAR between '2012' and '2016'
    group by A.KEYWORD, A.L_ASJC_CODE, A.PUBLICATION_YEAR
) A
inner join (
	/*
		분야별 1000위까지 순위의 키워드 추출
		RANKING : 분야별 키워드 랭킹
		KEYWORD : 키워드
		L_ASJC_CODE : ASJC 대분야
		PUBLICATION_YEAR : 발행 연도
		DOC_CNT : PByKeyByYear -> 연도별 키워드 논문 건수
	*/
    select distinct KEYWORD, L_ASJC_CODE
    from SCOPUS_2017Y_5Y_KEY_RANKING
    where RANKING <= 1000
    order by KEYWORD
) B
on A.KEYWORD = B.KEYWORD and A.L_ASJC_CODE = B.L_ASJC_CODE;

create INDEX IDX_SCOPUS_2017Y_5Y_KEY_S_KW ON SCOPUS_2017Y_5Y_KEY_STATS(KEYWORD);
create INDEX IDX_SCOPUS_2017Y_5Y_KEY_S_AC ON SCOPUS_2017Y_5Y_KEY_STATS(L_ASJC_CODE);
create INDEX IDX_SCOPUS_2017Y_5Y_KEY_S_PY ON SCOPUS_2017Y_5Y_KEY_STATS(PUBLICATION_YEAR);
create INDEX IDX_SCOPUS_2017Y_5Y_KEY_S_RNK ON SCOPUS_2017Y_5Y_KEY_STATS(RANKING);

-- 연도별 키워드 통계 (PbyKey)
drop table SCOPUS_2017Y_KEY_Y_STATS cascade constraints purge;
create TABLE SCOPUS_2017Y_KEY_Y_STATS NOLOGGING AS
select A.KEYWORD, A.PUBLICATION_YEAR, A.DOC_CNT
from (
	/*
		분야별 1000위까지 순위의 키워드 추출
		KEYWORD : 키워드
		PUBLICATION_YEAR : ASJC 대분야
		DOC_CNT : PByKey -> 연도별 키워드 논문 건수
	*/
    select A.KEYWORD, A.PUBLICATION_YEAR, count(distinct EID) as DOC_CNT
    from SCOPUS_2017Y_KEYWORD_BASE A
    where A.L_ASJC_CODE in ('1000', '1100',
        '1300', '1500',
        '1600', '1700',
        '1800', '1900',
        '2100', '2200',
        '2300', '2400',
        '2500', '2600',
        '2700', '2800',
        '3000', '3100',
        '3400', '3500'
    ) and A.KEYWORD in (
	      select distinct KEYWORD
	      from SCOPUS_2017Y_KEYWORD_RANKING
	      where RANKING <= 1000
    ) 
    group by A.KEYWORD, A.PUBLICATION_YEAR
) A;

create INDEX IDX_SCOPUS_2017Y_KEY_Y_S_KW ON SCOPUS_2017Y_KEY_Y_STATS(KEYWORD);
create INDEX IDX_SCOPUS_2017Y_KEY_Y_S_PY ON SCOPUS_2017Y_KEY_Y_STATS(PUBLICATION_YEAR);

-- 최근 5년 연도별 키워드 통계
drop table SCOPUS_2017Y_KEY_5Y_STATS cascade constraints purge;
create TABLE SCOPUS_2017Y_KEY_5Y_STATS NOLOGGING AS
select A.KEYWORD, A.PUBLICATION_YEAR, A.DOC_CNT
from (
	/*
		분야별 1000위까지 순위의 키워드 추출
		KEYWORD : 키워드
		PUBLICATION_YEAR : ASJC 대분야
		DOC_CNT : PByKey -> 연도별 키워드 논문 건수
	*/
    select A.KEYWORD, A.PUBLICATION_YEAR, count(distinct EID) as DOC_CNT
    from SCOPUS_2017Y_KEYWORD_BASE A
    where A.L_ASJC_CODE in ('1000', '1100',
        '1300', '1500',
        '1600', '1700',
        '1800', '1900',
        '2100', '2200',
        '2300', '2400',
        '2500', '2600',
        '2700', '2800',
        '3000', '3100',
        '3400', '3500'
    ) and A.KEYWORD in (
	      select distinct KEYWORD
	      from SCOPUS_2017Y_5Y_KEY_RANKING
	      where RANKING <= 1000
    ) and A.PUBLICATION_YEAR between '2012' and '2016'
    group by A.KEYWORD, A.PUBLICATION_YEAR
) A;

create INDEX IDX_SCOPUS_2017Y_KEY_5Y_S_KW ON SCOPUS_2017Y_KEY_5Y_STATS(KEYWORD);
create INDEX IDX_SCOPUS_2017Y_KEY_5Y_S_PY ON SCOPUS_2017Y_KEY_5Y_STATS(PUBLICATION_YEAR);
