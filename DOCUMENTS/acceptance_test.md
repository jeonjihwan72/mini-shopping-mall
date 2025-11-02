# 미니 쇼핑몰 프로젝트 인수 테스트 (Acceptance Test)

이 문서는 specification.md에 정의된 기능 요구사항(FR)이 정확하게 구현되었는지 검증하기 위한 인수 테스트 시나리오를 정의합니다. 모든 테스트 시나리오는 API 엔드포인트 호출을 기준으로 GWT(Given-When-Then) 형식을 따릅니다.

---

Given : 테스트 수행 전 시스템의 상태 (예: DB 데이터, 인증 상태)
When : 테스트를 위해 수행하는 동작 (예: API 호출)
Then : 동작 수행 후 기대하는 결과 (예: HTTP 상태 코드, DB 데이터 변경)

---

## 1. 회원 / 권한 (AC-MEMBER)

- 시나리오 1-1: (성공) 회원 가입
  - ID: AC-M-001 (from FR-M-001, FR-M-001-2, FR-M-001-3)
  - 시나리오: 사용자가 고유한 ID와 비밀번호, 주소로 회원가입을 시도한다.
  - Given:
    - username이 "testuser"인 회원이 DB에 존재하지 않는다.
  - When:
    - POST /api/members/join API를 다음 Body와 함께 호출한다:
  ```JSON
  {
    "username": "testuser",
    "password": "password123!",
    "address": "서울시 강남구"
  }
  ```
  - Then:
    - HTTP Status Code 201 Created를 반환한다.
    - 응답 Body에 {"username": "testuser"}가 포함된다.
    - DB의 Member 테이블에 username="testuser"인 레코드가 1개 생성된다.
    - 해당 레코드의 role은 USER (또는 ROLE_USER)이다.
    - 해당 레코드의 password는 해싱되어 "password123!"과 일치하지 않는다.

- 시나리오 1-2: (실패) 중복 ID 회원 가입
  - ID: AC-M-001-1 (from FR-M-001-1)
  - 시나리오: 이미 존재하는 ID로 회원가입을 시도한다.
  - Given:
    - username이 "existinguser"인 회원이 DB에 이미 존재한다.
  - When:
    - POST /api/members/join API를 다음 Body와 함께 호출한다:
  ```JSON
  {
    "username": "existinguser",
    "password": "newpassword456",
    "address": "부산시 해운대구"
  }
  ```
  - Then:
    - HTTP Status Code 409 Conflict를 반환한다.

- 시나리오 1-3: (성공) 로그인
  - ID: AC-M-002 (from FR-M-002, FR-M-002-1)
  - 시나리오: 가입된 사용자가 올바른 정보로 로그인을 시도한다.
  - Given:
    - username="testuser", password="password123!" (암호화됨)인 회원이 DB에 존재한다.
  - When:
    - POST /api/members/login API를 다음 Body와 함께 호출한다:
  ```JSON
  {
    "username": "testuser",
    "password": "password123!"
  }
  ```
  - Then:
    - HTTP Status Code 200 OK를 반환한다.
    - Response Body에 accessToken 필드가 포함된 JWT 토큰이 반환된다.

- 시나리오 1-4: (실패) 로그인 - 잘못된 비밀번호
  - ID: AC-M-002-2 (from FR-M-002-2)
  - 시나리오: 가입된 사용자가 잘못된 비밀번호로 로그인을 시도한다.
  - Given:
    - username="testuser"인 회원이 DB에 존재한다.
  - When:
    - POST /api/members/login API를 다음 Body와 함께 호출한다:
  ```JSON
  {
    "username": "testuser",
    "password": "wrong_password"
  }
  ```
  - Then:
    - HTTP Status Code 401 Unauthorized를 반환한다.

- 시나리오 1-5: (실패) 권한 - 일반 유저의 관리자 API 접근
  - ID: AC-M-003-1 (from FR-M-003-1)
  - 시나리오: ROLE_USER 권한을 가진 사용자가 관리자 전용 API(상품 등록)를 호출한다.
  - Given:
    - ROLE_USER 권한으로 로그인하여 유효한 인증 토큰(JWT)을 발급받았다.
  - When:
    - POST /api/admin/products API를 인증 헤더(JWT)를 포함하여 호출한다.
  - Then:
    - HTTP Status Code 403 Forbidden을 반환한다.

- 시나리오 1-6: (실패) 권한 - 인증되지 않은 사용자의 주문 API 접근
  - ID: AC-M-003-2 (from FR-M-003-2)
  - 시나리오: 로그인하지 않은 사용자(비회원)가 사용자 전용 API(주문 생성)를 호출한다.
  - Given:
    - 인증 토큰이 없다.
  - When:
    - POST /api/orders API를 호출한다.
  - Then:
    - HTTP Status Code 401 Unauthorized를 반환한다.

- 시나리오 1-7: (성공) 권한 - 공개 API 접근
  - ID: AC-M-003-3 (from FR-M-003-3)
  - 시나리오: 로그인하지 않은 사용자(비회원)가 공개 API(상품 목록 조회)를 호출한다.
  - Given:
    - 인증 토큰이 없다.
  - When:
    - GET /api/products API를 호출한다.
  - Then:
    - HTTP Status Code 200 OK를 반환한다.

---

## 2. 상품 (AC-PRODUCT)

- 시나리오 2-1: (성공) 관리자 상품 등록
  - ID: AC-P-001 (from FR-P-001)
  - 시나리오: ROLE_ADMIN 관리자가 새 상품을 등록한다.
  - Given:
    - ROLE_ADMIN 권한으로 로그인하여 유효한 인증 토큰(JWT)을 발급받았다.
  - When:
    - POST /api/admin/products API를 인증 헤더와 다음 Body를 포함하여 호출한다:
  ```JSON
  {
    "name": "새로운 티셔츠",
    "price": 20000,
    "stock": 100,
    "description": "아주 좋은 티셔츠"
  }
  ```
  - Then:
    - HTTP Status Code 201 Created를 반환한다.
    - 응답 Body로 ProductResponse DTO (ID 포함)가 반환된다.
    - DB의 Product 테이블에 "새로운 티셔츠" 상품 레코드가 생성된다.

- 시나리오 2-2: (성공) 상품 목록 조회 (페이징)
  - ID: AC-P-002 (from FR-P-002)
  - 시나리오: 누구나 상품 목록을 2페이지(10개씩) 조회한다.
  - Given:
    - DB에 총 25개의 Product가 등록되어 있다.
  - When:
    - GET /api/products?page=1&size=10 API를 호출한다. (page는 0-indexed)
  - Then:
    - HTTP Status Code 200 OK를 반환한다.
    - 응답 Body는 Page<ProductSimpleResponse> 형태이며, 10개의 상품 정보를 포함한다.
    - 응답 Body의 pageable 정보에 pageNumber: 1이 포함된다.

- 시나리오 2-3: (실패) 존재하지 않는 상품 상세 조회
  - ID: AC-P-003-1 (from FR-P-003-1)
  - 시나리오: 누구나 존재하지 않는 ID(예: 9999)로 상품 상세 조회를 시도한다.
  - Given:
    - DB에 productId=9999인 상품이 존재하지 않는다.
  - When:
    - GET /api/products/9999 API를 호출한다.
  - Then:
    - HTTP Status Code 404 Not Found를 반환한다.

- 시나리오 2-4: (실패) 상품 삭제 - 주문된 상품
  - ID: AC-P-005-1 (from FR-P-005-1)
  - 시나리오: 관리자가 이미 주문된 이력이 있는 상품을 삭제하려 시도한다.
  - Given:
    - ROLE_ADMIN 권한으로 로그인하여 유효한 인증 토큰(JWT)을 발급받았다.
    - productId=10인 상품이 OrderItem 테이블에서 1건 이상 참조되고 있다.
  - When:
    - DELETE /api/admin/products/10 API를 인증 헤더를 포함하여 호출한다.
  - Then:
    - HTTP Status Code 409 Conflict를 반환한다.
    - DB의 Product 테이블에서 productId=10인 상품이 삭제되지 않는다.

---

## 3. 주문 (AC-ORDER) - (핵심 시나리오)

- 시나리오 3-1: (성공) 주문 생성 및 재고 차감
  - ID: AC-O-001 (from FR-O-001 ~ FR-O-006)
  - 시나리오: ROLE_USER가 재고가 충분한 상품을 주문한다.
  - Given:
    - ROLE_USER("testuser")로 로그인하여 인증 토큰을 획득했다.
    - Product ID=1 ("티셔츠")의 stock은 100, price는 15000이다.
    - Product ID=2 ("바지")의 stock은 50, price는 30000이다.
  - When:
    - POST /api/orders API를 인증 헤더와 다음 Body를 포함하여 호출한다:
  ```JSON
  {
    "items": [
      { "productId": 1, "count": 2 },
      { "productId": 2, "count": 1 }
    ]
  }
  ```
  - Then:
    - HTTP Status Code 201 Created를 반환한다.
    - 응답 Body로 OrderResponse DTO가 반환된다.
    - (재고 차감 - FR-O-004) DB의 Product ID=1의 stock이 98로 변경된다.
    - (재고 차감 - FR-O-004) DB의 Product ID=2의 stock이 49로 변경된다.
    - (주문 저장 - FR-O-006) DB의 Order 테이블에 "testuser" 소유의 status="ORDERED" 및 totalPrice=60000 (150002 + 300001)인 주문이 생성된다.
    - (항목 저장 - FR-O-006) DB의 OrderItem 테이블에 orderPrice=15000, count=2 (상품1) 항목과 orderPrice=30000, count=1 (상품2) 항목이 생성된다.

- 시나리오 3-2: (실패) 재고 부족으로 인한 주문 실패
  - ID: AC-O-003-1 (from FR-O-003-1, FR-O-002)
  - 시나리오: ROLE_USER가 재고보다 많은 수량의 상품을 주문한다.
  - Given:
    - ROLE_USER로 로그인하여 인증 토큰을 획득했다.
    - Product ID=1 ("티셔츠")의 stock이 1이다.
  - When:
    - POST /api/orders API를 인증 헤더와 다음 Body를 포함하여 호출한다:
  ```JSON
  {
    "items": [{ "productId": 1, "count": 2 }]
  }
  ```
  - Then:
    - HTTP Status Code 409 Conflict를 반환한다.
    - 응답 Body에 "재고가 부족합니다" (또는 유사한) 메시지가 포함된다.
    - (트랜잭션 롤백 - FR-O-002) DB의 Product ID=1의 stock은 1로 변경되지 않는다.
    - (트랜잭션 롤백 - FR-O-002) Order 및 OrderItem 레코드가 생성되지 않는다.

- 시나리오 3-3: (실패) 타인의 주문 내역 상세 조회
  - ID: AC-O-007-2 (from FR-O-007-2)
  - 시나리오: ROLE_USER("userA")가 다른 사용자("userB")의 주문(orderId=100)을 조회하려 시도한다.
  - Given:
    - Order ID=100은 "userB"의 주문이다.
    - ROLE_USER("userA")로 로그인하여 인증 토큰을 획득했다.
  - When:
    - GET /api/orders/100 API를 인증 헤더를 포함하여 호출한다.
  - Then:
    - HTTP Status Code 403 Forbidden을 반환한다. (또는 404 Not Found - 구현 정책에 따라 다름)

- 시나리오 3-4: (성공) 관리자의 주문 취소 및 재고 복구
  - ID: AC-O-008 (from FR-O-008, FR-O-008-1, FR-O-008-2)
  - 시나리오: ROLE_ADMIN이 특정 주문을 취소하고, 해당 상품들의 재고를 복구한다.
  - Given:
    - ROLE_ADMIN으로 로그인하여 인증 토큰을 획득했다.
    - Order ID=50은 status="ORDERED"이다.
    - Order ID=50은 OrderItem으로 Product ID=1 (count=3)을 포함한다.
    - Product ID=1의 현재 stock은 10이다.
  - When:
    - PATCH /api/admin/orders/50/status API를 인증 헤더를 포함하여 호출한다.
  - Then:
    - HTTP Status Code 200 OK를 반환한다.
    - (상태 변경) DB의 Order ID=50의 status가 CANCELED로 변경된다.
    - (재고 복구 - FR-O-008-1) DB의 Product ID=1의 stock이 13으로 변경된다 (10 + 3).

---

## 4. 동시성 테스트 (AC-CONCURRENCY) - (고급 시나리오)

- 시나리오 4-1: (핵심) 재고 1개에 대한 동시 주문 (비관적 락 검증)
  - ID: AC-C-001 (from FR-O-004)
  - 시나리오: 재고가 1개 남은 상품에 대해 2명 이상의 사용자가 동시에 1개씩 주문을 시도한다.
  - Given:
    - Product ID=77 ("한정판 신발")의 stock이 1이다.
    - 사용자A, 사용자B가 각각 1개씩 주문(productId: 77, count: 1)을 준비한다.
  - When:
    - 사용자A와 사용자B가 POST /api/orders API를 거의 동시에 호출한다. (예: 100ms 이내)
  - Then:
    - (성공) 오직 1명의 사용자(예: 사용자A)만 HTTP Status Code 201 Created를 반환받는다.
    - (실패) 다른 1명(또는 그 이상)의 사용자(예: 사용자B)는 HTTP Status Code 409 Conflict (재고 부족)을 반환받는다.
    - (최종 데이터) Product ID=77의 최종 stock은 0이 된다. (절대로 -1이 되면 안 된다.)
    - (최종 데이터) Order 테이블에는 status="ORDERED"인 주문이 단 1개만 생성된다.