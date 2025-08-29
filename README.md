# AI 가계부 

현재 **OpenAI API 활용 카테고리 자동 분류 가계부**의 MVP 구현 단계입니다.
실제 카드사 API 대신 임의 거래 데이터를 사용해 테스트 하였습니다.

## 기술 스택
- Language: Java 17
- Framework: Spring Boot 3.x
- Database: MySQL, Flyway (DB Migration), Redis (Cache)
- ORM: JPA + QueryDSL
- Authentication: Spring Security + JWT
- Documentation: Swagger UI
- Testing: JUnit 5, Testcontainers
- Build Tool: Gradle
- HTTP Client: WebClient

## 주요기능
1. **회원 관리**
    - 회원 CRUD (생성, 조회, 수정, 삭제)
    - JWT 기반 인증/인가 (로그인/로그아웃, 토큰 재발급)
2. **카드 관리**
    - 카드 CRD (생성, 조회, 삭제)
3. **카테고리 및 가맹점 등록**
    - 기본 카테고리 및 가맹점-카테고리 초기 데이터 INSERT
4. **카드 거래 내역 관리**
    - 가짜 카드사 거래 내역 테스트 데이터 삽입 API
    - 가짜 카드사 거래 내역 조회 API
5. **거래 내역 관리**
    - 등록한 카드의 당월 거래 내역을 조회
    - OpenAI API를 활용하여 카테고리 매핑 후 데이터 저장
    - 거래 내역 조회

## 고도화 기능
1. **카테고리별 통계 및 비율 조회**
   - 기간별 거래 내역을 카테고리 기준으로 합산 및 비율 계산
   - `@Cacheable` 적용으로 반복 조회 시 성능 최적화
2. **AI 기반 절약 방법 추천**
   - 카테고리별 통계 결과를 기반으로 OpenAI API 호출
   - JSON 응답을 파싱해 DTO로 변환
   - 카테고리별 맞춤 절약 방법 제공