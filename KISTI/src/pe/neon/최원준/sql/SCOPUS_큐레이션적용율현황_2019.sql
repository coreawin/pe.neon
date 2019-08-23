/**
큐레이션 지표 추출을 위한 사전 데이터 준비 작업.
*/
-- 대상 문서 수집.
create table RQ2019_ARRECP as
	select eid, doi, citation_type, publication_year, source_id from SCOPUS_2014_DOCUMENT
		where citation_type in ('cp', 'ar', 're');
		
CREATE INDEX SIDX_RQ2019_ARRECPEID ON RQ2019_ARRECP ("EID") nologging;
CREATE INDEX SIDX_RQ2019_ARRECPCT ON RQ2019_ARRECP ("CITATION_TYPE") nologging;
CREATE INDEX SIDX_RQ2019_ARRECPDOI ON RQ2019_ARRECP ("DOI") nologging;
CREATE INDEX SIDX_RQ2019_ARRECPSID ON RQ2019_ARRECP (	"SOURCE_ID") nologging;
commit;

--통합학술논문 건수
create table NEON_20180604_ARRE as
	select * from RQ2019_ARRECP where citation_type in ('ar','re');
--통합학술대회논문 건수
create table NEON_20180604_CP as
	select * from RQ2019_ARRECP where citation_type in ('cp');
	
commit;	
CREATE INDEX SIDX_RQ2019_ARREEID ON NEON_20180604_ARRE (
	"EID"
) nologging;
CREATE INDEX SIDX_RQ2019_ARRECT ON NEON_20180604_ARRE (
	"CITATION_TYPE"
) nologging;
CREATE INDEX SIDX_RQ2019_ARRESID ON NEON_20180604_ARRE (
	"SOURCE_ID"
) nologging;
commit;			
CREATE INDEX SIDX_RQ2019_CPEID ON NEON_20180604_CP (
	"EID"
) nologging;
CREATE INDEX SIDX_RQ2019_CPCT ON NEON_20180604_CP (
	"CITATION_TYPE"
) nologging;
CREATE INDEX SIDX_RQ2019_CPSID ON NEON_20180604_CP (
	"SOURCE_ID"
) nologging;
commit;

--학술논문 대상 저자수 
create table NEON_20180604_TARRE_AUTHOR as 
	select distinct eid, author_id
		from SCOPUS_2014_AUTHOR 
		where eid in (select eid from NEON_20180604_ARRE);
	
--식별저자를 가지는 논문
create table NEON_20180604_ARRE_AU_1 as
	select distinct eid 
	from NEON_20180604_TARRE_AUTHOR
	where eid in (select eid from NEON_20180604_ARRE) and author_id is not null ;
	
CREATE INDEX SIDX_RQ2019_ARREA1EID ON NEON_20180604_ARRE_AU_1 (
	"EID"
) nologging;


--학술논문대상 식별기관 
create table NEON_20180604_TARRE_AFF as 
	select distinct eid, afid
		from SCOPUS_2014_AFFILIATION
		where eid in (select eid from NEON_20180604_ARRE);

--식별기관을 가지는 논문
create table RQ2019_ARRE_AFFI_1 as
	select distinct eid 
	from NEON_20180604_TARRE_AFF
	where eid in (select eid from NEON_20180604_ARRE) and afid is not null ;
	
CREATE INDEX SIDX_RQ2019_ARREAF1EID ON RQ2019_ARRE_AFFI_1 (
	"EID"
) nologging;	

--=======


--학술대회 대상 저자수 
drop table NEON_20180604_CP_AUTHOR  CASCADE CONSTRAINTS PURGE;
create table NEON_20180604_CP_AUTHOR as 
	select distinct eid, author_id
		from SCOPUS_2014_AUTHOR 
		where eid in (select eid from NEON_20180604_CP);
	
--학술대회 대상 식별저자를 가지는 논문
drop table NEON_20180604_CP_AU_1  CASCADE CONSTRAINTS PURGE;
create table NEON_20180604_CP_AU_1 as
	select distinct eid 
	from NEON_20180604_CP_AUTHOR
	where eid in (select eid from NEON_20180604_CP) and author_id is not null ;
	
CREATE INDEX SIDX_RQ2019_CPA1EID ON NEON_20180604_CP_AU_1 (
	"EID"
) nologging;

--학술대회 논문대상 식별기관 
drop table NEON_20180604_TCP_AFF  CASCADE CONSTRAINTS PURGE;
create table NEON_20180604_TCP_AFF as 
	select distinct eid, afid
		from SCOPUS_2014_AFFILIATION
		where eid in (select eid from NEON_20180604_CP);

--학술대회 식별기관을 가지는 논문
drop table RQ2019_CP_AFFI_1  CASCADE CONSTRAINTS PURGE;
create table RQ2019_CP_AFFI_1 as
	select distinct eid 
	from NEON_20180604_TCP_AFF
	where eid in (select eid from NEON_20180604_CP) and afid is not null ;
	
CREATE INDEX SIDX_RQ2019_CPAF1EID ON RQ2019_CP_AFFI_1 (
	"EID"
) nologging;	



--대회 식별저자수를 가지는 논문수.
select count(eid) as 대회식별저자수 from NEON_20180604_CP_AU_1;

-- 대회 식별기관을 가지는 논문수.
select count(eid) as 대회식별기관수 from RQ2019_CP_AFFI_1;

-- 식별기관을 가지는 논문수.
select count(eid) as 논문식별기관수 from RQ2019_ARRE_AFFI_1;

--식별저자수를 가지는 논문수.
select count(eid) as 논문식별저자수 from NEON_20180604_ARRE_AU_1;

-- 학술논문 종수 (저널의 수)
select count(distinct source_id) as 논문저널수 from NEON_20180604_ARRE;
-- 학술논문 논문수
select count(eid) as 논문수 from NEON_20180604_ARRE;

-- 학술대회 종수
select count(distinct source_id) as 대회저널수 from NEON_20180604_CP;
-- 학술대회 논문수
select count(eid) as 대회논문수 from NEON_20180604_CP;
			
--대회 DOI 건수
select count(distinct eid)  as 대회_DOI from NEON_20180604_CP where doi is not null;

--대회 과제정보연계 건수
select count(distinct eid) as 대회_과제건수 from SCOPUS_2014_GRANT where eid in (select eid from NEON_20180604_CP) and AGENCY_ID is not null;

--DOI 건수
select count(distinct eid)  as 논문DOI from NEON_20180604_ARRE where doi is not null;

--과제정보연계 건수
select count(distinct eid) as 논문_과제건수 from SCOPUS_2014_GRANT where eid in (select eid from NEON_20180604_ARRE) and AGENCY_ID is not null;
			

--학술 논문 모두 존재하는 경우
select count(distinct eid) as 논문_ALL from NEON_20180604_ARRE_AU_1 
where eid in (
	select distinct eid 
	from RQ2019_ARRE_AFFI_1
	where eid in (
		select distinct eid as 대회_과제정보 
		from SCOPUS_2014_GRANT 
		where eid in (
			select eid from NEON_20180604_ARRE where doi is not null
		) and AGENCY_ID is not null		
	)
);			
--학술 대회 모두 존재하는 경우
select count(distinct eid) as 대회_ALL from NEON_20180604_CP_AUTHOR 
where eid in (
	select distinct eid 
	from NEON_20180604_CP_AFFI_1
	where eid in (
		select distinct eid as 대회_과제정보 
		from SCOPUS_2014_GRANT 
		where eid in (
			select eid from NEON_20180604_CP where doi is not null
		) and AGENCY_ID is not null		
	)
);			



--모두 대상 저자수 
--drop table NEON_20180604_ALL_AUTHOR  CASCADE CONSTRAINTS PURGE;
create table NEON_20180604_ALL_AUTHOR as 
	select distinct eid, author_id
		from SCOPUS_2014_AUTHOR 
		where eid in (select eid from RQ2019_ARRECP);
	
--모두 대상 식별저자를 가지는 논문
--drop table NEON_20180604_ALL_AU_1  CASCADE CONSTRAINTS PURGE;
create table NEON_20180604_ALL_AU_1 as
	select distinct eid 
	from NEON_20180604_ALL_AUTHOR
	where eid in (select eid from RQ2019_ARRECP) and author_id is not null ;
	
CREATE INDEX SIDX_RQ2019_ALLA1EID ON NEON_20180604_ALL_AU_1 (
	"EID"
) nologging;

--모두 논문대상 식별기관 
--drop table NEON_20180604_ALL_AFF  CASCADE CONSTRAINTS PURGE;
create table NEON_20180604_ALL_AFF as 
	select distinct eid, afid
		from SCOPUS_2014_AFFILIATION
		where eid in (select eid from RQ2019_ARRECP);

--모두 식별기관을 가지는 논문
--drop table RQ2019_ALL_AFFI_1  CASCADE CONSTRAINTS PURGE;
create table RQ2019_ALL_AFFI_1 as
	select distinct eid 
	from NEON_20180604_ALL_AFF
	where eid in (select eid from RQ2019_ARRECP) and afid is not null ;
	
CREATE INDEX SIDX_RQ2019_ALLAF1EID ON RQ2019_ALL_AFFI_1 (
	"EID"
) nologging;	

-- 모두 존재하는 경우
select count(distinct eid) as _ALL from NEON_20180604_ALL_AU_1 
where eid in (
	select distinct eid 
	from RQ2019_ALL_AFFI_1
	where eid in (
		select distinct eid 
		from SCOPUS_2014_GRANT 
		where eid in (
			select eid from RQ2019_ARRECP where doi is not null
		) and AGENCY_ID is not null		
	)
);	
			
--=====================================			
인문 (Conference Paper) CP
/**인문 대상 테이블*/
create table RQ2019_I as
	select * from RQ2019_ARRECP 
	where eid in(
		select eid from SCOPUS_2014_FIRST_ASJC
			where ASJC_CODE in ('1000','1200','1400','1800','2000','3200','3300','3600')
		)
		
CREATE INDEX SIDX_RQ2019_IEID ON RQ2019_I (
	"EID"
) nologging;
CREATE INDEX SIDX_RQ2019_ICT ON RQ2019_I (
	"CITATION_TYPE"
) nologging;
CREATE INDEX SIDX_RQ2019_ISID ON RQ2019_I (
	"SOURCE_ID"
) nologging;
commit;		

-- 인문사회분야 학술논문 종수 (저널의 수)
select count(distinct source_id) from RQ2019_I where citation_type in ('ar', 're');
-- 인문사회분야 학술논문 논문수
select count(eid) from RQ2019_I where citation_type in ('ar', 're');

-- 인문사회분야 학술대회 종수
select count(distinct source_id) from RQ2019_I where citation_type in ('cp');
-- 인문사회분야 학술대회 논문수
select count(eid) from RQ2019_I where citation_type in ('cp');

과학 (Article + Review) AR, RE
1000
1100
1300
1500
1600
1700
1900
2100
2200
2300
2400
2500
2600
2700
2800
2900
3000
3100
3400
3500

/**과학 대상 테이블*/
select eid from SCOPUS_2014_FIRST_ASJC
where ASJC_CODE in ('1000', '1100', '1300', '1500', '1600', '1700', '1900', '2100' ,'2200', '2300', '2400', '2500', '2600', '2700', '2800', '2900', '3000', '3100', '3400','3500')

create table RQ2019_T as
	select * from RQ2019_ARRECP 
	where eid in(
		select eid from SCOPUS_2014_FIRST_ASJC
			where ASJC_CODE in ('1000', '1100', '1300', '1500', '1600', '1700', '1900', '2100' ,'2200', '2300', '2400', '2500', '2600', '2700', '2800', '2900', '3000', '3100', '3400','3500')
		);
		
CREATE INDEX SIDX_RQ2019_TCT ON RQ2019_T (
	"CITATION_TYPE"
) nologging;

commit;	

--학술논문 

create table RQ2019_T_ARRE as
	select * from RQ2019_T where citation_type in ('ar', 're');
CREATE INDEX SIDX_RQ2019_TARREEID ON RQ2019_T_ARRE (
	"EID"
) nologging;
CREATE INDEX SIDX_RQ2019_TARRECT ON RQ2019_T_ARRE (
	"CITATION_TYPE"
) nologging;
CREATE INDEX SIDX_RQ2019_TARREDOI ON RQ2019_T_ARRE (
	"DOI"
) nologging;
CREATE INDEX SIDX_RQ2019_TARRESID ON RQ2019_T_ARRE (
	"SOURCE_ID"
) nologging;
commit;	
	
create table RQ2019_T_CP as
	select * from RQ2019_T where citation_type in ('cp');

CREATE INDEX SIDX_RQ2019_TCPEID ON RQ2019_T_CP (
	"EID"
) nologging;
CREATE INDEX SIDX_RQ2019_TCPCT ON RQ2019_T_CP (
	"CITATION_TYPE"
) nologging;
CREATE INDEX SIDX_RQ2019_TCPDOI ON RQ2019_T_CP (
	"DOI"
) nologging;
CREATE INDEX SIDX_RQ2019_TCPSID ON RQ2019_T_CP (
	"SOURCE_ID"
) nologging;
commit;		

-- 과학기술분야 학술논문 종수 (저널의 수)
select count(distinct source_id) from RQ2019_T_ARRE;
-- 과학기술분야 학술논문 논문수
select count(eid) from RQ2019_T_ARRE;
-- 과학기술분야 학술대회 종수
select count(distinct source_id) from RQ2019_T_CP;
-- 과학기술분야 학술대회 논문수
select count(eid) from RQ2019_T_CP;


--과학기술 분야 학술논문 대상 저자수 
create table RQ2019_TARRE_AUTHOR as 
	select distinct eid, author_id
		from SCOPUS_2014_AUTHOR 
		where eid in (select eid from RQ2019_T_ARRE);

--총 저자수
	select count(distinct author_id) as 총저자수
	from RQ2019_TARRE_AUTHOR where eid in (select eid from RQ2019_T_ARRE);
	
--식별저자를 가지는 논문
create table RQ2019_T_AUTHOR_1 as
	select distinct eid 
	from RQ2019_TARRE_AUTHOR
	where eid in (select eid from RQ2019_T_ARRE) and author_id is not null ;
	
CREATE INDEX SIDX_RQ2019_TA1EID ON RQ2019_T_AUTHOR_1 (
	"EID"
) nologging;	

--식별저자수를 가지는 논문수.	
select count(eid) as 식별저자수 from RQ2019_T_AUTHOR_1;
	
--비식별저자를 가지는 논문	
select count(eid) as 비식별저자수 from RQ2019_T_ARRE where eid not in (select eid from RQ2019_T_AUTHOR_1);


--과학기술 분야 학술논문 대상 식별기관 
create table RQ2019_TARRE_AFF as 
	select distinct eid, afid
		from SCOPUS_2014_AFFILIATION
		where eid in (select eid from RQ2019_T_ARRE);

--총 기관수
	select count(distinct afid) as 총기관수
	from RQ2019_TARRE_AFF where eid in (select eid from RQ2019_T_ARRE);

--식별기관을 가지는 논문
create table RQ2019_T_AFFI_1 as
	select distinct eid 
	from RQ2019_TARRE_AFF
	where eid in (select eid from RQ2019_T_ARRE) and afid is not null ;
	
CREATE INDEX SIDX_RQ2019_TAF1EID ON RQ2019_T_AFFI_1 (
	"EID"
) nologging;	

-- 식별기관을 가지는 논문수.	
select count(eid) as 식별기관수 from RQ2019_T_AFFI_1;
	
--비식별기관을 가지는 논문	
select count(eid) as 비식별기관수 from RQ2019_T_ARRE where eid not in (select eid from RQ2019_T_AFFI_1);

--DOI 건수
select count(eid) as DOI건수 from RQ2019_T_ARRE where doi is not null;

--과제정보연계 건수
select count(distinct eid) as 과제정보연계건수 from SCOPUS_2014_GRANT where eid in (select eid from RQ2019_T_ARRE) and AGENCY_ID is not null;


--모두 존재하는 경우
select count(distinct eid) as 대회_ARREALL from RQ2019_TARRE_AUTHOR 
where eid in (
	select distinct eid 
	from RQ2019_TARRE_AFF
	where eid in (
		select distinct eid 
		from RQ2019_T_ARRE 
		where eid in(
			select distinct eid as 대회_과제정보 
			from SCOPUS_2014_GRANT 
			where eid in (
				select eid from RQ2019_T_ARRE
			) and AGENCY_ID is not null
		) and doi is not null
	)
)



========================= 학술대회 대상.

--학술대회 
-- 과학기술분야 학술대회 종수
select count(distinct source_id) from RQ2019_T_CP;
-- 과학기술분야 학술대회 논문수
select count(eid) from RQ2019_T_CP;


--과학기술 분야 학술논문 대상 저자수 
create table RQ2019_TCP_AUTHOR as 
	select distinct eid, author_id
		from SCOPUS_2014_AUTHOR 
		where eid in (select eid from RQ2019_T_CP);

--총 저자수
	select count(distinct author_id) as_대회_총저자수
	from RQ2019_TCP_AUTHOR where eid in (select eid from RQ2019_T_CP);
	
--식별저자를 가지는 논문
create table RQ2019_TCP_AUTHOR_1 as
	select distinct eid 
	from RQ2019_TCP_AUTHOR
	where eid in (select eid from RQ2019_T_CP) and author_id is not null ;
	
CREATE INDEX SIDX_RQ2019_TCP1EID ON RQ2019_TCP_AUTHOR_1 (
	"EID"
) nologging;	

--식별저자수를 가지는 논문수.	
select count(distinct eid)  as_대회_식별저자수 from RQ2019_TCP_AUTHOR_1;
	
--비식별저자를 가지는 논문	
select count(distinct eid)  as_대회_비식별저자수 from RQ2019_T_CP where eid not in (select eid from RQ2019_TCP_AUTHOR_1);


--과학기술 분야 학술논문 대상 식별기관 
create table RQ2019_TCP_AFF as 
	select distinct eid, afid
		from SCOPUS_2014_AFFILIATION
		where eid in (select eid from RQ2019_T_CP);

--총 기관수
	select count(distinct afid) as 대회_총기관수
	from RQ2019_TCP_AFF where eid in (select eid from RQ2019_T_CP);

--식별기관을 가지는 논문
create table RQ2019_TCP_AFFI_1 as
	select distinct eid 
	from RQ2019_TCP_AFF
	where eid in (select eid from RQ2019_T_CP) and afid is not null ;
	
CREATE INDEX SIDX_RQ2019_TCPF1EID ON RQ2019_TCP_AFFI_1 (
	"EID"
) nologging;	

--식별기관을 가지는 논문수.	
select count(distinct eid)  as 대회_식별기관수 from RQ2019_TCP_AFFI_1;
	
--비식별기관을 가지는 논문	
select count(distinct eid)  as 대회_비식별기관수 from RQ2019_T_CP where eid not in (select eid from RQ2019_TCP_AFFI_1);

--DOI 건수
select count(distinct eid)  as 대회_DOI from RQ2019_T_CP where doi is not null;

--과제정보연계 건수
select count(distinct eid) as 대회_과제정보연계건수 from SCOPUS_2014_GRANT where eid in (select eid from RQ2019_T_CP) and AGENCY_ID is not null;


--모두 존재하는 경우
select count(distinct eid) as 대회_ALL from RQ2019_TCP_AUTHOR_1 
where eid in (
	select distinct eid 
	from RQ2019_TCP_AFF
	where eid in (
		select distinct eid 
		from RQ2019_T_CP 
		where eid in(
			select distinct eid as 대회_과제정보 
			from SCOPUS_2014_GRANT 
			where eid in (
				select eid from RQ2019_T_CP
			) and AGENCY_ID is not null
		) and doi is not null
	)
)

;

select count(distint eid) as 참조문헌의_DOI
from NEON_20180604_ARRE
where eid in (
	select distinct refeid 
	from SCOPUS_2014_REFERENCE
	where eid in (select eid from NEON_20180604_ARRE)
) and doi is not null


select count(distint eid) as 대회_참조문헌의_DOI
from NEON_20180604_CP
where eid in (
	select distinct refeid 
	from SCOPUS_2014_REFERENCE
	where eid in (select eid from NEON_20180604_CP)
) and doi is not null







