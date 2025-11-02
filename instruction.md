# 미니 쇼핑몰 프로젝트 백엔드 기술 지침 (Instruction)

이 문서는 쇼핑몰 프로젝트의 백엔드 API 서버 개발에 필요한 핵심 기술 스택, 아키텍처, 개발 범위, 환경 설정에 대한 지침을 제공한다.

본 프로젝트는 프론트엔드 뷰(View)를 렌더링하지 않으며, 오직 Headless API (JSON 데이터만 반환) 서버로만 동작한다.

## 1. 핵심 아키텍처 (Core Architecture)

- 아키텍처: 모놀리식 아키텍처 (Monolithic Architecture)
    - 선정 사유: 프로젝트 초기 단계이며 1개월 내 빠른 프로토타입 개발을 목표로 합니다. 단일 애플리케이션 구조가 개발 및 배포 효율이 가장 높다.
    - 향후 확장: 프로젝트 성장 시, 기능을 마이크로서비스 아키택처(MSA)로 점진적 전환을 고려한다.

- 설계 원칙: 계층형 아키텍처 (Layered Architecture)
    - Controller(API) - Service(비즈니스 로직) - Repository(DB 접근) 3계층 구조를 명확히 분리하여, 관심사를 분리하고 유지보수 성을 높인다.

---

## 2. 프로젝트 핵심 도메인 (Project Scope)

본 백엔드 서버는 다음 7가지 핵심 도메인에 대한 비즈니스 로직과 API를 구현한다.

1. 상품 관리(Product Management): 상품 등록/수정/삭제, 카테고리, 재고, 옵션 관리
2. 주문/결제 관리(Order & Payment Management): 주문 생성/처리, 결제 연동(PG), 주문 취소/환불
3. 회원/권한 관리(Member & Authorization): 회원 가입/로그인(인증), 회원 정보, 관리자 권한(인가) 관리
4. 시스템 및 데이터베이스(System & Database): API 구현, DB 관리, 로그/모니터링

---

## 3. 개발 언어 및 프레임워크 (Language & Framework)

- 프로그래밍 언어: Java (Version 17 LTS 이상 권장)
- 백엔드 코어 프레임워크: Spring Boot
    - 사유: 내장 웹 서버(Tomcat)를 포함하며, RESTful API 서버를 빠르고 안정적으로 구축하는 데 최적이다.

---

## 4. 데이터 관리 (Data Management)

- 데이터베이스 (개발): H2 인메모리 데이터베이스 (In-memory DB)
    - 사유: 설정이 매우 간편하고, 애플리케이션 재시작 시 데이터가 초기화되어 개발 및 테스트 환경에 매우 용이하다.

- 영속성 기술: Spring Data JPA
    - 사유: CRUD 로직을 안정적이고 효율적으로 구현하며, 추후 MySQL/PostgreSQL 같은 운영 환경 RDBMS로의 전환을 최소한의 코드 변경으로 가능하게 한다.

---

## 5. API 명세 (API Specification)

- 형식: 백엔드 서버는 JSON 형식의 데이터만 반환하는 RESTful API를 제공한다.
- 인증: 회원/권한 관리를 위해 JWT(JSON Web Token) 또는 세션 기반 인증을 도입한다.

| 도메인 | 기능 | HTTP 메서드 | 엔드포인트 | 목적 |
|---|---|---|---|---|
| 회원 | 회원 가입 | POST | /api/members/join | 신규 회원 정보를 등록합니다. |
| 회원 | 로그인 | POST | /api/members/login | 사용자 인증을 처리합니다. |
| 상품 | 상품 전체 조회 | GET | /api/products | 전체 상품 목록을 반환합니다. |
| 상품 | 상품 상세 조회 | GET | /api/products/{productId}	 | 특정 상품의 상세 정보를 반환합니다. |
| 주문 | 주문 생성 | POST |	/api/orders | 주문을 생성합니다. |
| 주문 | 주문 상세 조회 | GET | /api/orders/{orderId} | 특정 주문의 상세 내역을 반환합니다. |
| 관리자 | (상품) 상품 등록 | POST | /api/admin/products | (관리자) 새 상품을 등록합니다. |
| 관리자 | (주문) 주문 상태 변경 | PATCH | /api/admin/orders/{orderId}/status | (관리자) 주문 상태를 변경합니다. |

---

## 6. 개발 환경 및 도구 (Development Environment & Tools)

- IDE: IntelliJ IDEA Community Edition 또는 VS Code with Java Extension Pack
- 빌드 도구: Gradle
- 버전 관리: Git & GitHub

---

## 7. 주요 의존성 (Key Dependencies)

build.gradle 파일에 포함될 백엔드 핵심 의존성입니다.

- spring-boot-starter-web: RESTful API 개발 및 내장 웹 서버 실행의 핵심.
- spring-boot-starter-data-jpa: JPA(Java Persistence API)를 사용하여 데이터베이스와 상호작용.
- spring-boot-starter-security: 회원 가입, 로그인(인증) 및 API 권한(인가) 관리를 위함.
- com.h2database:h2: H2 인메모리 데이터베이스 드라이버.
- org.projectlombok:lombok: 반복적인 코드(Getter, Setter 등) 자동 생성을 통한 생산성 향상.