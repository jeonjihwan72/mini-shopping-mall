# 미니 쇼핑몰 프로젝트 인수 테스트 (Acceptance Test)

이 문서는 specification.md에 정의된 기능 요구사항(FR)이 정확하게 구현되었는지 검증하기 위한 인수 테스트 시나리오를 정의합니다. 모든 테스트 시나리오는 GWT(Given-When-Then) 형식을 따릅니다.

---

- `Given` : 현재 사용자의 상황 및 상태
- `When` : 사용자의 동작
- `Then` : 실행 동작

---

## 1. 회원 / 권한 (AC-MEMBER)

- 시나리오 1-1: (성공) 회원 가입
  - ID: AC-M-001 (from FR-M-001)
  - 시나리오: 사용자가 고유한 ID와 비밀번호로 회원가입을 시도한다.  
  - Given: username이 "testuser"인 회원이 DB에 존재하지 않는다.
  - When: POST /api/members/join (Body: { "username": "testuser", "password": "pw123", "address": "서울" })을 호출한다.
  - Then: 
    - HTTP Status Code 201 Created를 반환한다.
    - DB의 Member 테이블에 username="testuser"인 레코드가 1개 생성된다.
    - 해당 레코드의 role은 "ROLE_USER"이다.
    - 해당 레코드의 password는 해싱되어 "pw123"과 일치하지 않는다.

- 시나리오 1-2: (실패) 중복 ID 회원 가입
  - ID: AC-M-001-1 (from FR-M-001-1)
  - 시나리오: 이미 존재하는 ID로 회원가입을 시도한다.
  - Given: username이 "testuser"인 회원이 DB에 이미 존재한다.
  - When: POST /api/members/join (Body: { "username": "testuser", "password": "pw456" })을 호출한다.
  - Then: HTTP Status Code 409 Conflict를 반환한다.

- 시나리오 1-3: (성공) 로그인
  - ID: AC-M-002 (from FR-M-002)
  - 시나리오: 가입된 사용자가 올바른 정보로 로그인을 시도한다.
  - Given: username="testuser", password="pw123"(암호화됨)인 회원이 DB에 존재한다.
  - When: POST /api/members/login (Body: { "username": "testuser", "password": "pw123" })을 호출한다.
  - Then:
    - HTTP Status Code 200 OK를 반환한다.
    - Response Body에 JWT 토큰(또는 세션 ID)이 포함된다.

- 시나리오 1-4: (실패) 로그인 - 잘못된 비밀번호
  - ID: AC-M-002-2 (from FR-M-002-2)
  - 시나리오: 가입된 사용자가 잘못된 비밀번호로 로그인을 시도한다.
  - Given: username="testuser"인 회원이 DB에 존재한다.
  - When: POST /api/members/login (Body: { "username": "testuser", "password": "wrong_pw" })을 호출한다.
  - Then: HTTP Status Code 401 Unauthorized를 반환한다.

- 시나리오 1-5: (실패) 권한 - 일반 유저의 관리자 API 접근
  - ID: AC-M-003-1 (from FR-M-003-1)
  - 시나리오: ROLE_USER 권한을 가진 사용자가 관리자 전용 API(상품 등록)를 호출한다.
  - Given: ROLE_USER 권한으로 로그인하여 인증 토큰을 발급받았다.
  - When: POST /api/admin/products (인증 헤더 포함)를 호출한다.
  - Then: HTTP Status Code 403 Forbidden을 반환한다.

- 시나리오 1-6: (성공) 권한 - 관리자의 관리자 API 접근
  - ID: AC-M-003-1 (from FR-M-003-1)
  - 시나리오: ROLE_ADMIN 권한을 가진 사용자가 관리자 전용 API(상품 등록)를 호출한다.
  - Given: ROLE_ADMIN 권한으로 로그인하여 인증 토큰을 발급받았다.
  - When: POST /api/admin/products (Body: { "name": "새상품", ... }, 인증 헤더 포함)를 호출한다.
  - Then: HTTP Status Code 201 Created를 반환한다.

## 2. 상품 (AC-PRODUCT)

- 시나리오 2-1: (성공) 상품 목록 조회
  - ID: AC-P-002 (from FR-P-002)
  - 시나리오: 누구나 상품 목록을 조회한다.
  - Given: DB에 Product가 2개(상품A, 상품B) 등록되어 있다.
  - When: GET /api/products를 호출한다.
  - Then:
    - HTTP Status Code 200 OK를 반환한다.
    - Response Body가 상품 2개(상품A, 상품B)를 포함한 JSON 배열이다.

- 시나리오 2-2: (실패) 존재하지 않는 상품 상세 조회
  - ID: AC-P-003-1 (from FR-P-003-1)
  - 시나리오: 누구나 존재하지 않는 ID로 상품 상세 조회를 시도한다.
  - Given: DB에 productId=999인 상품이 존재하지 않는다.
  - When: GET /api/products/999를 호출한다.
  - Then: HTTP Status Code 404 Not Found를 반환한다.

## 3. 주문 (AC-ORDER) - (핵심 시나리오)

- 시나리오 3-1: (성공) 주문 생성 및 재고 차감
  - ID: AC-O-001 (from FR-O-001 ~ FR-O-006)
  - 시나리오: ROLE_USER가 재고가 충분한 상품을 주문한다.
  - Given:
    - ROLE_USER("testuser")로 로그인하여 인증 토큰을 획득했다.
    - Product ID=1 ("티셔츠")의 stock이 100이다.
  - When: POST /api/orders (인증 헤더 포함, Body: { "items": [{ "productId": 1, "count": 2 }] })를 호출한다.
  - Then:
    - HTTP Status Code 201 Created를 반환한다.
    - DB의 Product ID=1의 stock이 98로 변경된다.
    - DB의 Order 테이블에 "testuser" 소유의 status="ORDERED"인 주문이 생성된다.
    - DB의 OrderItem 테이블에 productId=1, count=2인 항목이 생성된다.

- 시나리오 3-2: (실패) 재고 부족으로 인한 주문 실패
  - ID: AC-O-003-1 (from FR-O-003-1)
  - 시나리오: ROLE_USER가 재고보다 많은 수량의 상품을 주문한다.
  - Given:
    - ROLE_USER로 로그인하여 인증 토큰을 획득했다.
    - Product ID=1 ("티셔츠")의 stock이 1이다.
  - When: POST /api/orders (인증 헤더 포함, Body: { "items": [{ "productId": 1, "count": 2 }] })를 호출한다.
  - Then:
    - HTTP Status Code 400 Bad Request (또는 409 Conflict)를 반환한다.
    - Response Body에 "재고가 부족합니다" 메시지가 포함된다.
    - DB의 Product ID=1의 stock은 1로 변경되지 않는다.
    - Order 및 OrderItem 레코드가 생성되지 않는다.

- 시나리오 3-3: (실패) 타인의 주문 내역 조회
  - ID: AC-O-007-2 (from FR-O-007-2)
  - 시나리오: ROLE_USER("userA")가 다른 사용자("userB")의 주문을 조회하려 시도한다.
  - Given:
    - Order ID=100은 "userB"의 주문이다.
    - ROLE_USER("userA")로 로그인하여 인증 토큰을 획득했다.
  - When: GET /api/orders/100 (인증 헤더 포함)을 호출한다.
  - Then: HTTP Status Code 403 Forbidden을 반환한다.

## 4. 동시성 테스트 (AC-CONCURRENCY) - (고급 시나리오)

- 시나리오 4-1: (핵심) 재고 1개에 대한 동시 주문
  - ID: AC-O-004-CONCURRENCY (from FR-O-004)
  - 시나리오: 재고가 1개 남은 상품에 대해 2명 이상의 사용자가 동시에 1개씩 주문을 시도한다. (Pessimistic Lock 검증)
  - Given:
    - Product ID=1 ("한정판 신발")의 stock이 1이다.
    - 사용자A, 사용자B가 각각 1개씩 주문을 준비한다.
  - When: 사용자A와 사용자B가 POST /api/orders (Body: { "items": [{ "productId": 1, "count": 1 }] })를 거의 동시에 호출한다.
  - Then:
    - 오직 1명의 사용자(예: 사용자A)만 201 Created를 반환받는다.
    - 다른 1명의 사용자(예: 사용자B)는 400 Bad Request (재고 부족)을 반환받는다.
    - (중요) Product ID=1의 최종 stock은 0이 된다. (절대로 -1이 되면 안 된다.)