/**
* <P>
* A. 작업방법 기술검색식을 이용하여 먼저 SCOPUS, PATENT 다운로드를 시도한다.<br>
* 1. NEON_ECLIPSE_KISTI_SCOPUS_SEARCHER<br>
* 2. NEON_ECLIPSE_KISTI_PATENT_SEARCHER<br>
* _2021.Rnd package.ScopusSearcher_2021.java <br>
* </P>
*
* <P>
* B. 정량분석 지표를 추출한다. <br>
* pe.neon 프로젝트의 아래 클래스를 이용해 논문/특허 정량지표를 추출한다.<br>
* pe.neon.여운동._202101.Launcher4Patent  <br>
* pe.neon.여운동._202101.Launcher4Scopus<br>
* 결과물 :  <br>
* 	1. RESULT_SCOPUS_기술명_작업일.txt <br>
* 	2. RESULT_PATENT_기술명_작업일.txt<br>
* </P>
*
* <P>
* C. pe.eclipse.neon 프로젝트를 이용하여 경제적 특성 정보를 최종 취합한다.<br>
*  pe.eclipse.neon.yeo._2021.Extract경제적특성_2021
* </P>
*
* 안녕하세요 미소테크 박진현입니다. <br/>
*
* R&D PIE 시스템 데이터 관련 메일 드립니다.<br/>
* 우선 정리 완료된 4개 분과(전체 16개분과 중) 논문, 특허검색식 전달 해드립니다.<br/>
* 검색식 부분에 KISTI에서 검색시 결과 건수도 같이 표기 했습니다. 건수가 비슷하게 나오면 될듯합니다.<br/>
* 이 부분에 대한 작년과 동일한 작업 요청 드리겠습니다.<br/>
* (분과별 기술군별 논문/특허 결과, 10대 지표값 등)<br/>
* 일전 말씀드린바와 같이 아래와 같은 필드 추가 요청 드립니다.<br/>
*
* <논문><br/>
*
* - 피인용수: Number of Citation<br/>
* Check - 저자 국가 식별: Author Country<br/>
* Check - 저자명 식별: Author ID<br/>
* Check - 저자명: Author Name<br/>
* Check - 저자 통합 정보: Author Info<br/>
* Check - 기관명 국가 식별: Affiliation Country<br/>
* Check - 기관명 식별: Affiliation IDs<br/>
* Check - 기관명: Affiliation Name<br/>
* Check
*
* <특허><br/>
*
* - 출원인 국적: assignee-country<br/>
* check - 대표출원인(정제된 것?): app_gp<br/>
* check - 출원인 국적+명: assignee<br/>
* check - 피인용수; citation-count<br/>
* check - 등록번호:<br/>
* ==> KIND 항목이 Grant 인것 check
*
* @author coreawin
* @date 2021. 1. 9.
  */