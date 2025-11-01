# 미니 쇼핑몰 백엔드 상세 명세서 (Specification)

이 문서는 instruction.md에 정의된 기술 지침을 바탕으로, '무엇을' 개발해야 하는지에 대한 상세 기능 요구사항(Requirement), 데이터 모델(Entity), API 명세(DTO)를 정의한다.

---

## 1. 프로젝트 목표 및 범위

- 목표: 1개월 내 핵심 기능(회원, 상품, 주문)을 갖춘 백엔드 API 서버 프로토타입을 완성한다.
- 핵심 범위 (In-Scope): 회원/권한, 상품 관리, 주문 관리(재고 차감 포함)
- 범위 외 (Out-of-Scope): 결제(PG 연동), 배송(운송장 연동), 프로모션, 고객 서비스 등은 초기 버전에서 제외하거나 최소한으로 구현한다.

## 2. 핵심 데이터 모델 (Entities)

JPA 엔티티로 관리될 핵심 데이터 모델이다. (편의상 Lombok 어노테이션 사용)

#### 2.1 Member (회원)

@Entity 
| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK | 회원 고유 ID |
| username | String | Not Null, Unique | 로그인 ID |
| password | String | Not Null | 해싱된 비밀번호 |
| address | String | - | 배송 주소 |
| role | String | Not Null | 사용자 권한(ROLE_USER, ROLE_ADMIN)|

#### 2.2 Product (상품)

@Entity
| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK | 상품 고유 ID |
| name | String | Not Null | 상품명 |
| price | Int | Not Null | 상품 가격 |
| stock | Int | Not Null | 재고 수량 |
| description | String | - | 상품 설명 |

#### 2.3 Order (주문)

@Entity
| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK | 주문 상품 고유 ID |
| member | Member | ManyToOne | 주문한 회원 |
| orderItems | List<OrderItem> | OneToMany | 주문 상품 목록 |
| totalPrice | Int | Not Null | 총 주문 금액 |
| status | String | Not Null | 주문 상태 (ORDERED, CANCELED) |

#### 2.4 OrderItem (주문 상품)

@Entity
| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK | 주문 상품 고유 ID |
| order | Order | ManyToOne | 연결된 주문 |
| product | Product | ManyToOne | 주문된 상품 |
| count | Int | Not Null | 주문 수량 |

## 3. 기능 요구사항 명세 (Functional Requirements)

다음 `FR-XXX` ID로 명명된 요구사항을 모두 만족하는 Controller, Service, Repository 코드를 생성해야 한다.

#### 3.1 회원/권한 (FR-MEMBER)

| ID | 명칭 | 중요도 | 상세 설명 |
|---|---|---|---|
| FR-M-001 | 회원 가입 | 상 | username, password, address를 입력받아 회원을 생성한다. (API: POST /api/members/join) |
| FR-M-001-1 | ID 고유성 | 상 | username 중복 시 409 Conflict 에러를 반환한다. |
| FR-M-001-2 | 비밀번호 암호화 | 상 | password는 Bcrypt로 해싱하여 DB에 저장한다. (spring-boot-starter-security의 PasswordEncoder 사용) |
| FR-M-001-3 | 기본 권한 | 상 | 회원가입 시, 기본 권하능로 ROLE_USER를 부여한다. |
| FR-M-002 | 로그인(인증) | 상 | username, password로 로그인을 요청한다. (API: POST /api/members/login) |
| FR-M-002-1 | 인증 성공 | 상 | 인증 성공 시, JWT 토큰을 발급하여 반환한다. (또는 세션 ID) |
| FR-M-002-2 | 인증 실패 | 상 | 자격 증명 실패 시 401 Unauthorized 에러를 반환한다. |
| FR-M-003 | 권한 관리(인가) | 상 | Spring Security를 사용하여 API 접근 권한을 제어한다. |
| FR-M-003-1 | 관리자 API | 상 | /api/admin/** 패턴의 API는 ROLE_ADMIN 권한만 호출할 수 있다. |
| FR-M-003-2 | 사용자 API | 상 | /api/orders/** 패턴의 API는 ROLE_USER 권한(로그인)이 필요하다. |
| FR-M-003-3 | 공개 API | 상 | GET /api/products/**, POST /api/members/join, POST /api/members/login 은 누구나 접근 가능하다.|

#### 3.2 상품 (FR-PRODUCT)

| ID | 명칭 | 중요도 | 상세 설명 |
|---|---|---|---|
| FR-P-001 | (관리자) 상품 등록 | 상 | 관리자(ROLE_ADMIN)가 name, price, stock, description을 입력받아 Product를 생성한다. (API: POST /api/admin/products) |
| FR-P-002 | 상품 목록 조회 | 상 | 모든 사용자가 상품 목록(간략 정보)을 조회할 수 있다. (API: GET /api/products) |
| FR-P-003 | 상품 상세 조회 | 상 | 모든 사용자가 productId로 상품 상세 정보를 조회할 수 있다. (API: GET /api/products/{productId}) |
| FR-P-003-1 | 조회 실패 | 중 | productId가 존재하지 않을 경우 404 Not Found 에러를 반환한다. |

#### 3.3 주문 (FR-ORDER)

| ID | 명칭 | 중요도 | 상세 설명 |
|---|---|---|---|
| FR-O-001 | 주문 생성 | 최상 | 로그인한 사용자(ROLE_USER)가 List<{productId, count}> 형식의 DTO로 주문을 요청한다. (API: POST /api/orders) |
| FR-O-002 | 단일 트랜잭션 | 최상 | (중요)FR-O-003부터 FR-O-006까지의 모든 과정은 단일 트랜잭션(@Transactional) 내에서 처리되어야 한다. |
| FR-O-003 | 재고 확인 | 최상 | 주문 요청된 모든 productId의 stock이 요청된 count보다 크거나 같은지 확인한다. |
| FR-O-003-1 | 재고 부족 예외 | 최상 | stock이 count보다 적은 상품이 하나라도 있으면, 트랜잭션 전체를 롤백하고 400 Bad Request (또는 409 Conflict) 에러를 반환한다. |
| FR-O-004 | 재고 차감(동시성) | 최상 | (중요) 재고 확인 및 차감 시, Pessimistic Lock(비관적 락)을 사용하여 Product 엔티티를 조회 및 수정한다.(JPA LockModeType.PESSIMISTIC_WRITE 사용) |
| FR-O-005 | 주문 총액 계산 | 상 | `totalPrice = (상품1 가격 * 수량1) + (상품2 가격 * 수량2) ...` 공식을 사용해 총액을 계산한다. |
| FR-O-006 | 주문/주문항목 저장 | 상 | Order를 생성(상태 ORDERED)하고, 요청된 items를 OrderItem으로 변환하여 Order와 연관시켜 모두 저장한다. |
| FR-O-007 | 주문 조회 | 중 | 로그인한 사용자가 자신의 주문 내역을 조회할 수 있다. (API: GET /api/orders) |
| FR-O-007-1 | 주문 상세 조회 | 중 | orderId로 특정 주문의 상세 내역(주문 상품 포함)을 조회할 수 있다. (API: GET /api/orders/{orderId}) |
| FR-O-007-2 | 조회 권한 | 중 | 사용자는 자신의 주문만 조회할 수 있어야 한다.(관리자는 모든 주문 조회 가능) |

## 4. 공통 에러 응답 형식

API 실패 시, @RestControllerAdvice를 사용하여 공통된 JSON 형식으로 응답을 통일한다.
```JSON
{
  "timestamp": "2025-11-01T21:30:00.000+09:00",
  "status": 404,
  "error": "Not Found",
  "message": "해당 상품을 찾을 수 없습니다. (ID: 999)"
}
```