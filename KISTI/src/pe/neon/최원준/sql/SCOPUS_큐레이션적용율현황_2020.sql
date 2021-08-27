/**
큐레이션 지표 추출을 위한 사전 데이터 준비 작업.
*/
-- 대상 문서 수집.
create table RQ2020_ARRECP as
	select eid, doi, citation_type, publication_year, source_id from SCOPUS_2014_DOCUMENT
		where citation_type in ('cp', 'ar', 're');
		
CREATE INDEX SIDX_RQ2020_ARRECPEID ON RQ2020_ARRECP ("EID") nologging;
CREATE INDEX SIDX_RQ2020_ARRECPCT ON RQ2020_ARRECP ("CITATION_TYPE") nologging;
CREATE INDEX SIDX_RQ2020_ARRECPDOI ON RQ2020_ARRECP ("DOI") nologging;
CREATE INDEX SIDX_RQ2020_ARRECPSID ON RQ2020_ARRECP (	"SOURCE_ID") nologging;
commit;

--통합학술논문 건수
create table RQ2020_ARRE as
	select * from RQ2020_ARRECP where citation_type in ('ar','re');
--통합학술대회논문 건수
create table RQ2020_CP as
	select * from RQ2020_ARRECP where citation_type in ('cp');

commit;
CREATE INDEX SIDX_RQ2020_ARREEID ON RQ2020_ARRE (
	"EID"
) nologging;
CREATE INDEX SIDX_RQ2020_ARRECT ON RQ2020_ARRE (
	"CITATION_TYPE"
) nologging;
CREATE INDEX SIDX_RQ2020_ARRESID ON RQ2020_ARRE (
	"SOURCE_ID"
) nologging;
commit;
CREATE INDEX SIDX_RQ2020_CPEID ON RQ2020_CP (
	"EID"
) nologging;
CREATE INDEX SIDX_RQ2020_CPCT ON RQ2020_CP (
	"CITATION_TYPE"
) nologging;
CREATE INDEX SIDX_RQ2020_CPSID ON RQ2020_CP (
	"SOURCE_ID"
) nologging;
commit;


--학술논문 대상 저자수
create table RQ2020_TARRE_AUTHOR as
	select distinct eid, author_id
		from SCOPUS_2014_AUTHOR
		where eid in (select eid from RQ2020_ARRE);

--식별저자를 가지는 논문
create table RQ2020_ARRE_AU_1 as
	select distinct eid
	from RQ2020_TARRE_AUTHOR
	where eid in (select eid from RQ2020_ARRE) and author_id is not null ;

CREATE INDEX SIDX_RQ2020_ARREA1EID ON RQ2020_ARRE_AU_1 (
	"EID"
) nologging;

--학술논문대상 식별기관
create table RQ2020_TARRE_AFF as
	select distinct eid, afid
		from SCOPUS_2014_AFFILIATION
		where eid in (select eid from RQ2020_ARRE);

--식별기관을 가지는 논문
create table RQ2020_ARRE_AFFI_1 as
	select distinct eid
	from RQ2020_TARRE_AFF
	where eid in (select eid from RQ2020_ARRE) and afid is not null ;

CREATE INDEX SIDX_RQ2020_ARREAF1EID ON RQ2020_ARRE_AFFI_1 (
	"EID"
) nologging;



--=======

--학술대회 대상 저자수
drop table RQ2020_CP_AUTHOR  CASCADE CONSTRAINTS PURGE;
create table RQ2020_CP_AUTHOR as
	select distinct eid, author_id
		from SCOPUS_2014_AUTHOR
		where eid in (select eid from RQ2020_CP);

--학술대회 대상 식별저자를 가지는 논문
drop table RQ2020_CP_AU_1  CASCADE CONSTRAINTS PURGE;
create table RQ2020_CP_AU_1 as
	select distinct eid
	from RQ2020_CP_AUTHOR
	where eid in (select eid from RQ2020_CP) and author_id is not null ;

CREATE INDEX SIDX_RQ2020_CPA1EID ON RQ2020_CP_AU_1 (
	"EID"
) nologging;

--학술대회 논문대상 식별기관
drop table RQ2020_TCP_AFF  CASCADE CONSTRAINTS PURGE;
create table RQ2020_TCP_AFF as
	select distinct eid, afid
		from SCOPUS_2014_AFFILIATION
		where eid in (select eid from RQ2020_CP);

--학술대회 식별기관을 가지는 논문
drop table RQ2020_CP_AFFI_1  CASCADE CONSTRAINTS PURGE;
create table RQ2020_CP_AFFI_1 as
	select distinct eid
	from RQ2020_TCP_AFF
	where eid in (select eid from RQ2020_CP) and afid is not null ;

CREATE INDEX SIDX_RQ2020_CPAF1EID ON RQ2020_CP_AFFI_1 (
	"EID"
) nologging;

-- 학술논문+학술대회 대상 저자수
drop table RQ2020_ALL_AUTHOR  CASCADE CONSTRAINTS PURGE;
create table RQ2020_ALL_AUTHOR as
	select distinct eid, author_id
		from SCOPUS_2014_AUTHOR
		where eid in (select eid from RQ2020_ARRECP);

--학술논문+학술대회 대상 식별저자를 가지는 논문
drop table RQ2020_ALL_AU_1  CASCADE CONSTRAINTS PURGE;
create table RQ2020_ALL_AU_1 as
	select distinct eid
	from RQ2020_ALL_AUTHOR
	where eid in (select eid from RQ2020_ARRECP) and author_id is not null ;

CREATE INDEX SIDX_RQ2020_ALLA1EID ON RQ2020_ALL_AU_1 (
	"EID"
) nologging;

--학술논문+학술대회 논문대상 식별기관
drop table RQ2020_ALL_AFF  CASCADE CONSTRAINTS PURGE;
create table RQ2020_ALL_AFF as
	select distinct eid, afid
		from SCOPUS_2014_AFFILIATION
		where eid in (select eid from RQ2020_ARRECP);

--학술논문+학술대회 식별기관을 가지는 논문
drop table RQ2020_ALL_AFFI_1  CASCADE CONSTRAINTS PURGE;
create table RQ2020_ALL_AFFI_1 as
	select distinct eid
	from RQ2020_ALL_AFF
	where eid in (select eid from RQ2020_ARRECP) and afid is not null ;

CREATE INDEX SIDX_RQ2020_ALLAF1EID ON RQ2020_ALL_AFFI_1 (
	"EID"
) nologging;



-- A. 학술논문 논문수 (34,943,404)
    select count(eid) from RQ2020_ARRE;
-- A0. 학술논문 종수 (저널의 수)
    select count(distinct source_id) as 논문저널수 from RQ2020_ARRE;
-- B. 학술논문 논문수 (7,799,397)
    select count(eid) from RQ2020_CP;
-- C. 학술논문 저자식별건수 (34,534,028 )
    select count(eid) from RQ2020_ARRE_AU_1;
-- E. 학술논문 기관식별건수 ( 32,431,794)
    select count(eid) from RQ2020_ARRE_AFFI_1;
--G. 학술논문 DOI 건수
    select count(distinct eid)  as 논문DOI from RQ2020_ARRE where doi is not null;
--I. 과제정보연계 논문 건수
    select count(distinct eid) as 논문_과제건수 from SCOPUS_2014_GRANT where eid in (select eid from RQ2020_ARRE) and AGENCY_ID is not null;
--K. 학술 논문 모두 존재하는 경우
    select count(distinct eid) as 논문_ALL from RQ2020_ARRE_AU_1
    where eid in (
        select distinct eid
        from RQ2020_ARRE_AFFI_1
        where eid in (
            select distinct eid as 대회_과제정보
            from SCOPUS_2014_GRANT
            where eid in (
                select eid from RQ2020_ARRE where doi is not null
            ) and AGENCY_ID is not null
        )
    );

-- C0. 학술학술대회 식별저자수를 가지는 논문수.
    select count(eid) as 대회식별저자수 from RQ2020_CP_AU_1;
-- E0. 학술대회 식별기관을 가지는 논문수.
    select count(eid) as 대회식별기관수 from RQ2020_CP_AFFI_1;
-- B0. 학술대회 종수
    select count(distinct source_id) as 대회저널수 from RQ2020_CP;
--G0. 학술대회 DOI 건수
    select count(distinct eid)  as 대회_DOI from RQ2020_CP where doi is not null;
--I0. 학술대회 과제정보연계 건수
    select count(distinct eid) as 대회_과제건수 from SCOPUS_2014_GRANT where eid in (select eid from RQ2020_CP) and AGENCY_ID is not null;
--K0. 학술 대회 모두 존재하는 경우
    select count(distinct eid) as 대회_ALL from RQ2020_CP_AUTHOR
    where eid in (
        select distinct eid
        from RQ2020_CP_AFFI_1
        where eid in (
            select distinct eid as 대회_과제정보
            from SCOPUS_2014_GRANT
            where eid in (
                select eid from RQ2020_CP where doi is not null
            ) and AGENCY_ID is not null
        )
    );
