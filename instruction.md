# 미니 쇼핑몰 프로젝트 기술 지침 (Instruction)

## 1. 핵심 아키텍처 (Core Architecture)

- **Architecture**: **모놀리식 아키텍처 (Monolithic Architecture)**
    - **사유**: 프로젝트 초기 단계이며 1개월 내 빠른 프로토타입 개발을 목표로 하므로, 단일 애플리케이션 구조가 가장 효율적입니다. 모든 기능(상품, 장바구니, 주문)을 하나의 프로젝트 내에서 개발합니다.
    - **향후 확장**: 프로젝트가 성공적으로 성장하면, 각 기능을 **마이크로서비스 아키텍처(MSA)**로 전환하는 것을 고려할 수 있습니다. (예: 상품 서비스, 주문 서비스 분리)

---

## 2. 개발 언어 및 프레임워크 (Language & Framework)

- **Programming Language**: **Java** (Version 17 LTS 이상 권장)
- **Core Framework**: **Spring Boot**
    - **사유**: 내장 웹 서버(Tomcat)를 포함하고 있어 독립 실행이 가능하며, 방대한 라이브러리와 커뮤니티 지원을 통해 웹 애플리케이션을 빠르고 안정적으로 구축할 수 있습니다.

---

## 3. 프론트엔드 뷰 (Frontend View)
- **View Technology**: **Thymeleaf**(타임리프) + JavaScript
    - **사유**: Thymeleaf로 서버에서 초기 페이지를 렌더링하고, Vanilla JavaScript를 사용해 장바구니 수량 변경 시 총금액 실시간 업데이트와 같은 동적인 사용자 경험을 구현합니다. 이는 현대적인 웹사이트의 상호작용성을 모방하는 데 필수적입니다.

## 4. 스타일링 (CSS)
- **CSS Framework**: **Bootstrap 5**
- **Custom CSS**: `style.css`
    - **사유**: Bootstrap 5를 사용해 반응형 레이아웃과 기본 컴포넌트를 빠르게 구축하고, style.css 파일을 통해 프로젝트 고유의 세부 디자인을 적용하고 스타일을 조정합니다.
    
---

## 5. 데이터 관리 (Data Management)

- **Development Data Store**: **H2 인메모리 데이터베이스 (In-memory Database)**
    - **사유**: 프로젝트 초기부터 데이터베이스를 사용함으로써 더 현실적인 개발 경험을 쌓을 수 있습니다. H2는 별도의 설치 없이 의존성 추가만으로 동작하여 설정이 매우 간편하고, 애플리케이션을 재시작할 때마다 데이터가 초기화되어 테스트에 용이합니다. JSON 파일을 직접 다루는 것보다 데이터 추가, 수정, 삭제(CRUD) 로직을 훨씬 안정적이고 효율적으로 구현할 수 있습니다.
- **향후 전환**: 추후 운영 환경을 고려할 때, Spring Data JPA 덕분에 코드 변경을 최소화하면서 MySQL/PostgreSQL 같은 RDBMS로 손쉽게 전환할 수 있습니다.

---

## 6. API 명세 (API Specification)

- 개발 초기 단계에 핵심 기능에 대한 API 엔드포인트를 미리 정의하여 개발의 일관성을 유지합니다.
- API 예시:
    - 상품 전체 조회: `GET /api/products`
    - 상품 상세 조회: `GET /api/products/{productId}`
    - 장바구니에 상품 추가: `POST /api/cart/items`
    - 장바구니 조회: `GET /api/cart`
    - 주문하기: `POST /api/orders`

---

## 7. 개발 환경 (Development Environment)

- **IDE**: IntelliJ IDEA Community Edition 또는 VS Code with Java Extension Pack
- **Build Tool**: **Gradle** (또는 Maven)
- **Version Control**: **Git** & GitHub

---

## 8. 주요 의존성 (Key Dependencies - build.gradle)

- `spring-boot-starter-web`: 웹 애플리케이션 및 RESTful API 개발의 핵심

- `spring-boot-starter-thymeleaf`: Thymeleaf 템플릿 엔진 사용

- `spring-boot-starter-data-jpa`: JPA(Java Persistence API)를 사용하여 데이터베이스와 상호작용

- `com.h2database:h2`: H2 인메모리 데이터베이스 드라이버

- `org.projectlombok:lombok`: 반복적인 코드(Getter, Setter 등) 자동 생성을 통한 생산성 향상