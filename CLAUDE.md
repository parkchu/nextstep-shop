# CLAUDE.md

이 저장소는 쇼핑몰 주문·할인·환불 도메인을 **Cucumber 기반 ATDD**로 개발하는 교육용 프로젝트다.
AI(Claude)는 이 문서의 규칙을 모든 작업에 우선 적용한다.

---

## 1. 핵심 원칙

- **무엇(What)과 완료 기준은 사람이, 어떻게(How)는 AI가 정한다.**
- 승인된 `.feature` 시나리오는 사람과 AI 사이의 **계약**이다. AI는 계약을 바꾸지 않는다.
- AI는 체크포인트마다 **멈추고 승인을 요청**한다. 사람이 `승인`이라고 답하기 전에는 다음 단계로 넘어가지 않는다.
- 요구사항이 모호하면 **가정하지 말고 질문한다.** 질문은 PO(강사)에게 전달할 수 있는 형태로 정리한다.

### 요구사항 문서

- 구현할 요구사항은 `docs/requirements/<기능>.md`에서 관리한다. (예: `cart.md` → `src/acceptance/resources/features/cart.feature`)
- 요구사항 문서는 **What**이므로 사람(PO)이 소유한다. AI는 요구사항 문서를 수정하지 않고, 빠진 점·모호한 점은 질문으로 제시한다.
- 요구사항 문서의 `전제`가 코드와 맞지 않으면(예: "이미 있다"는 기능이 코드에 없음) 진행 전에 보고한다.
- 기능을 마칠 때 요구사항 문서의 `완료 기준` 체크리스트를 항목별로 확인해 보고한다.

---

## 2. 현재 단계와 AI 활용 수준

학습자는 단계가 바뀔 때 아래 `현재 단계` 값을 직접 수정한다.

**현재 단계: 2단계**

| 단계 | AI가 할 수 있는 일 | AI가 하지 않는 일 |
| --- | --- | --- |
| 1단계 | 문법·라이브러리·Cucumber 설정에 대한 질문 답변, 오류 메시지 해석 | 코드·시나리오·테스트 생성. 요청받으면 "1단계에서는 직접 작성합니다"라고 답하고 힌트만 준다 |
| 2단계 | 레거시 코드 분석, 테스트 케이스 후보 제안, 사람이 정한 리팩터링 단계 실행 | 리팩터링 순서·단위 결정, 한 번에 여러 규칙 이동, 기존 동작 변경 |
| 3단계 | 시나리오·스텝 정의 초안, 설계 제안, 구현, 영향 범위 분석 | 체크포인트 승인 없이 진행, 금액 기대값을 검증 없이 확정 |

---

## 3. ATDD 워크플로우

시나리오는 **한 번에 1~2개씩** 진행한다. 각 체크포인트(✋)에서 AI는 멈추고 아래 형식으로 승인을 요청한다.

```
✋ 승인 요청 [체크포인트 N: 이름]
- 무엇을 했는지:
- 확인할 것:
- 결정이 필요한 것:
```

### 1) 인수 조건 도출 ✋
- 사람이 `docs/requirements/<기능>.md`로 요구사항을 제시한다.
- AI는 엣지 케이스, 경계값, 예외 상황, **모호한 점(PO 질문 후보)**을 목록으로 제안한다.
- 요구사항에 용어 사전(`docs/glossary.md`)에 없는 용어가 나오면 AI가 추가할 항목(정의·코드 이름·쓰지 않는 표현)을 제안한다.
- 사람이 인수 조건을 확정한다.

### 2) 시나리오 작성 ✋
- `.feature` 파일에 Given/When/Then 시나리오를 작성한다. (작성 규칙: 5장)
- AI는 승인 요청 전에 시나리오 문장을 용어 사전과 대조하고, 사전에 없는 단어나 "쓰지 않는 표현"이 있으면 보고한다.
- 사람이 검토하고 승인하면 이 시나리오가 계약이 된다.

### 3) 인수 테스트 Red ✋
- 스텝 정의를 작성하고 Cucumber를 실행한다.
- **올바른 이유로 실패하는지** 확인한다. (예: 404, 기대 금액 불일치 / 컴파일 오류·설정 오류는 올바른 실패가 아니다)
- 실패 메시지를 보고하고, 승인되면 `test(acceptance):` 커밋을 만든다.
- 이 커밋 이후 해당 `.feature` 파일과 스텝 정의의 검증 로직은 **잠긴다.**

### 4) 설계 ✋
- AI는 변경할 파일, 새로 만들 도메인 객체, 작업 순서를 제안한다.
- 사람이 계층 책임 배치(6장)를 확인하고 승인한다.

### 5) 워킹 스켈레톤
- Controller → Service → Repository를 가장 얇게 연결한다. 하드코딩 값도 허용한다.
- 인수 테스트가 "기능 미구현" 이유로 실패하는 상태까지 만든다.

### 6) 도메인 TDD (안쪽 루프)
- 비즈니스 규칙은 도메인 객체에 단위 테스트 → 최소 구현 → 리팩터링 순으로 구현한다.
- 도메인 객체는 Spring에 의존하지 않는다.

### 7) 계층 완성 및 Green ✋
- Service·Repository·Controller를 완성하고 전체 테스트를 실행한다.
- Green 보고 전에 반드시 잠긴 파일의 변경 여부를 확인하고 결과를 함께 보고한다.

```bash
git diff -M <Red 커밋 해시> -- ':(glob)src/**/features/**' ':(glob)src/**/acceptance/**'
```

> RTK 훅을 쓰는 환경에서는 `git diff` 출력이 요약되어 변경 줄이 보이지 않을 수 있다. 잠긴 파일 확인은 `rtk proxy git diff ...`로 원본 출력을 본다.

- 변경이 있으면 Green으로 보고하지 않는다. (7장 금지 규칙)

### 8) 리팩터링과 리뷰 ✋
- 중복 제거, 이름 정리를 제안·수행한다. 동작 변경은 섞지 않는다.
- 사람이 최종 리뷰한다.

---

## 4. 요구사항 변경 대응

PO가 정책 변경을 요청하면 아래 순서를 지킨다. **구현부터 고치지 않는다.**

1. AI가 영향 범위를 분석한다: 영향받는 시나리오, 도메인 객체, 무료 배송·환불 등 간접 영향.
2. 시나리오를 수정·추가하고 사람에게 승인받는다. ✋
3. 인수 테스트를 수정하고 Red를 확인한다. ✋
    - 기존 시나리오의 기대값이 바뀌면 커밋 메시지에 변경 사유를 적는다.
    - 예: `test(acceptance): 쿠폰-등급 할인 중복 불가 정책 반영 (PO 요청 10/22)`
4. 구현을 변경하고, **변경하지 않은 기능의 시나리오까지 전부** 통과하는지 확인한다.

---

## 5. 시나리오(.feature) 작성 규칙

- 위치: `src/acceptance/resources/features/<기능>.feature`
- 키워드는 영어(Given/When/Then/And/But), 문장은 한국어로 쓴다.
- **도메인 언어만** 사용한다. URL, HTTP 상태 코드, 테이블명, JSON 필드명은 쓰지 않는다.
- 용어는 `docs/glossary.md`(도메인 용어 사전)를 따른다. 사전은 사람이 확정하며, 시나리오나 코드에 실제로 나온 용어만 올린다.
- 클래스·메서드·필드·스텝 메서드·Cucumber 파라미터 타입 이름은 사전의 "코드 이름" 열을 따른다.
- 추상적인 표현 대신 **구체적인 값**을 쓴다. ("할인이 적용된다" ✗ → "결제 금액은 47,400원이다" ✓)
- 시나리오 하나는 규칙 하나를 검증한다.
- 같은 처리 경로(도메인 예외 → 거절 응답)를 쓰는 규칙은 **대표 시나리오 하나만** 인수 테스트로 검증하고, 나머지는 도메인 단위 테스트로 검증한다. 이 경우 체크포인트 2·3을 건너뛰고 설계(4) → 도메인 TDD(6) → `./gradlew check` 회귀 확인(7)으로 진행하며, 어떤 대표 시나리오가 경로를 검증하는지 의사결정 로그에 남긴다.
- 같은 규칙의 여러 값은 `Scenario Outline` + `Examples`로 표현한다.
- 진행 중인 시나리오에는 `@wip` 태그를 붙이고, Green이 되면 제거한다.

```gherkin
Feature: 쿠폰 적용 주문

  Background:
    Given 회원 "철수"는 골드 등급이다

  Scenario: 정액 쿠폰을 적용하면 등급 할인 후 금액에서 쿠폰 금액만큼 할인된다
    Given 가격이 56,000원인 "코트"가 등록되어 있다
      And "철수"는 6,000원 정액 쿠폰을 가지고 있다
    When "철수"가 쿠폰을 사용해 "코트" 1개를 주문한다
    Then 결제 금액은 47,400원이다

  Scenario Outline: 회원 등급에 따라 할인율이 다르다
    Given 회원 "<이름>"은 <등급> 등급이다
      And 가격이 10,000원인 "티셔츠"가 등록되어 있다
    When "<이름>"이 "티셔츠" 1개를 주문한다
    Then 상품 금액은 <금액>원이다

    Examples:
      | 이름 | 등급 | 금액   |
      | 영희 | 일반 | 10,000 |
      | 민수 | 실버 | 9,500  |
      | 철수 | 골드 | 9,000  |
```

---

## 6. 코드 구조와 계층 책임

### 기술 스택

- Java 21, Spring Boot 2.7.18 (JPA 패키지는 `jakarta.*`가 아니라 **`javax.*`**), Lombok
- 빌드: **Gradle 8.14.3** (`./gradlew`, Groovy DSL `build.gradle`). Maven이 아니다.
- 테스트: Cucumber 7.10.1 + JUnit Platform Suite, Testcontainers(PostgreSQL), RestAssured
- DB: PostgreSQL, 스키마는 Liquibase(`src/main/resources/db/changelog.xml`)로 관리한다.
- 인수 테스트 실행에는 Docker가 실행 중이어야 한다.

### 패키지 구조

기본 패키지는 `nextstep`이다.

```
src/main/java/nextstep/
  product/   cart/   order/   coupon/   point/   # 기능(도메인)별 패키지
    domain/        # 엔티티, 값 객체, 정책. 비즈니스 규칙은 모두 여기
    application/   # Service: 조회 → 도메인 호출 → 저장, 트랜잭션 경계
    ui/            # Controller, 요청/응답 DTO, 입력 검증, 예외 → 응답 변환
    infra/         # Repository 구현 (필요 시)
  common/          # 여러 기능이 함께 쓰는 것
    domain/        # BusinessRuleException 등 공통 도메인 예외
    ui/            # GlobalExceptionHandler, ErrorResponse

src/test/java/nextstep/                  # 단위 테스트 (test 태스크, ./gradlew test)
  <기능>/domain/                        # 도메인 단위 테스트

src/acceptance/                          # 인수 테스트 (acceptanceTest 태스크)
  java/nextstep/acceptance/
    CucumberSpringConfiguration.java   # @CucumberContextConfiguration + @SpringBootTest (유일)
    RunCucumberTest.java               # @Suite, @IncludeEngines("cucumber")
    steps/                             # 스텝 정의 (기능별 클래스)
    support/                           # API 호출 헬퍼, ScenarioContext, DB 정리
  resources/
    features/                          # .feature 파일
    cucumber.properties
```

> `src/acceptance`는 `build.gradle`의 별도 소스셋 `acceptance`다. 테스트 의존성을 그대로 물려받는다. 인수 테스트 클래스는 `nextstep.acceptance` 패키지에만 둔다. `test` 태스크는 `src/test`만, `acceptanceTest` 태스크는 `RunCucumberTest`만 실행하고, `check`는 둘 다 실행한다.

| 계층 | 책임 | 테스트 |
| --- | --- | --- |
| 도메인 | 할인·배송비·환불 등 비즈니스 규칙 | 단위 테스트 (Mock 거의 없음) |
| Service | 조율, 트랜잭션. **판단 로직이 생기면 도메인으로 옮긴다** | 필요 시 Fake Repository로 테스트 |
| Repository | 영속성, 커스텀 쿼리 | 커스텀 쿼리만 통합 테스트 |
| Controller | 요청 파싱, 검증, HTTP 상태, 에러 응답 | 필요 시 `@WebMvcTest` |
| 전체 | 시나리오 충족 | Cucumber 인수 테스트 |

---

## 7. 금지 규칙 (예외 없음)

1. **승인된 `.feature` 파일과 스텝 정의의 검증(Then) 로직을 수정하지 않는다.**
   테스트가 틀렸다고 판단되면 수정하지 말고 이유를 설명한 뒤 사람의 결정을 기다린다.
   단, Green 이후 `@wip` 태그 줄을 지우는 것은 예외다. 태그 제거는 별도 `test(acceptance):` 커밋으로 남긴다.
2. 테스트를 통과시키기 위해 `@Disabled`, `@ignore` 태그, 검증 완화(예: 금액 비교를 "0보다 크다"로 변경)를 쓰지 않는다.
3. Red를 확인하지 않고 구현을 시작하지 않는다.
4. 여러 시나리오를 한 번에 구현하지 않는다.
5. 금액 기대값(절사, 안분 등)을 계산 근거 없이 제시하지 않는다. 기대값을 제안할 때는 **계산 과정을 함께 보여 준다.**
6. 모호한 정책을 임의로 가정해 구현하지 않는다. 가정이 불가피하면 가정을 명시하고 승인받는다.
7. 2단계에서 레거시의 이상한 동작을 발견하면 고치지 않고 보고한다. (보존/수정은 PO가 결정)

---

## 8. 스텝 정의 작성 규칙

- 스텝 정의는 **짧게** 쓴다. HTTP 요청 생성·호출은 `support/` 헬퍼로 분리한다.
  ```java
  @When("{string}이(가) 쿠폰을 사용해 {string} {int}개를 주문한다")
  public void 쿠폰_주문(String 회원, String 상품, int 수량) {
      context.response = OrderApi.주문한다(context.회원(회원), context.상품(상품), 수량, context.쿠폰(회원));
  }
  ```
- 스텝 간 상태 공유는 `@ScenarioScope` 빈(`ScenarioContext`)을 사용한다. static 필드를 쓰지 않는다.
- 각 시나리오 시작 전 `@Before` 훅에서 DB를 초기화한다. 시나리오끼리 데이터를 공유하지 않는다.
- 인수 테스트는 API 경계(RestAssured)에서만 검증한다. Repository나 Service를 직접 호출해 검증하지 않는다.
- Given 단계의 데이터 준비는 가능하면 API로 한다. API가 없는 데이터만 헬퍼로 직접 저장한다.

---

## 9. 커밋 규칙

| 접두사 | 용도 |
| --- | --- |
| `test(acceptance):` | 시나리오·인수 테스트 추가/변경 (Red) |
| `test:` | 단위 테스트 |
| `feat:` | 기능 구현 |
| `refactor:` | 동작 변경 없는 구조 개선 |
| `build:` | 빌드·의존성·테스트 실행 환경 설정 |
| `docs:` | 의사결정 로그 등 문서 |

- 커밋에는 작업과 관련된 파일만 포함한다. (경로를 지정해 추가)
- Red 커밋과 Green 커밋을 분리한다.
- 리팩터링 커밋에는 동작 변경을 섞지 않는다.
- AI는 커밋 전에 커밋 메시지와 포함 파일 목록을 보여 주고 승인받는다.

---

## 10. 의사결정 로그

체크포인트를 통과할 때마다 AI는 `docs/decision-log.md`에 추가할 항목 초안을 제시하고, 사람이 확인 후 저장한다.

```markdown
## 2026-10-22 쿠폰 적용 주문 / 체크포인트 1: 인수 조건
- AI 제안: 쿠폰 최소 주문 금액 기준(할인 전/후)이 불명확함
- 결정: PO 확인 → 등급 할인 후 금액 기준
- 채택/수정/거절: 채택
- 이유: 기획 의도 확인 결과
```

---

## 11. 명령어

기본 JDK는 21로 맞춘다. 컴파일·테스트는 `build.gradle`의 toolchain 설정으로 항상 JDK 21에서 진행된다(설치된 JDK 21을 Gradle이 찾아 쓴다). 기본 JDK가 21이 아닌 환경에서는 `JAVA_HOME`을 21로 지정해 실행한다.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew test                                                       # 단위 테스트만 (Docker 불필요)
./gradlew check                                                      # 단위 + 인수 테스트 (전체)
./gradlew acceptanceTest                                             # 인수 테스트만
./gradlew acceptanceTest -Dcucumber.filter.tags="@wip"               # 진행 중 시나리오만
```

> Gradle은 명령행의 `-D` 프로퍼티를 테스트 JVM에 자동으로 넘기지 않는다. `acceptanceTest` 태스크가 `cucumber.`로 시작하는 프로퍼티만 골라 넘기므로 `cucumber.filter.tags`가 동작한다.
> Green 보고(체크포인트 7)와 요구사항 변경 후 회귀 확인(4장)은 반드시 `./gradlew check`로 한다.

---

## 12. AI 응답 방식

- 작업을 시작할 때 **현재 단계와 체크포인트**를 먼저 밝힌다. (예: "3단계 / 체크포인트 3: 인수 테스트 Red")
- 한 번에 큰 변경을 하지 않는다. 변경 파일이 5개를 넘으면 먼저 계획을 보여 주고 승인받는다.
- 학습자가 코드를 설명하지 못하면 머지하지 않는다는 원칙에 따라, 구현 후 핵심 설계 결정을 3줄 이내로 요약한다.
- 규칙과 학습자의 요청이 충돌하면 규칙을 알려 주고 학습자의 결정을 따른다. 단, 7장 금지 규칙은 예외 없이 지킨다.
