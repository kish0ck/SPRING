# JSON 응답과 요청 처리



## 1. Jackson 의존 설정

------

- Jackson은 자바 객체와 JSON 형식 문자열 간 변환을 처리하는 라이브러리로 다음과 같이 pom.xml에 의존을 추가

  ```xml
    <!-- pom.xml -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.9.4</version>
    </dependency>
    <!-- java8 date/time -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>2.9.4</version>
    </dependency>
  ```

  

## 2. @RestController로 JSON 형식 응답

------

Spring MVC에서 `JSON` 형식으로 데이터를 응답하는 방법은 @Controller 대신 `@RestController`를 사용

```java
    // RestMemberController.java
    // 기존의 @Controller 대신 새로운 어노테이션 사용
    @RestController
    public class RestMemberController {
        private MemberDao memberDao;
        private MemberRegisterService registerService;

        /*
            * 다음 두 매서드에서 기존의 String 형태의 뷰 이름을 리턴하는 것이 아니라,
            * 일반 객체를 리턴함
            */
        @GetMapping("/api/members")
        public List<Member> members() {
            return memberDao.selectAll();
        }
        
        @GetMapping("/api/members2/{id}")
        public Member member2(@PathVariable Long id, HttpServletResponse response) throws IOException {
            Member member = memberDao.selectById(id);
            if (member == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            return member;
        }
            ...
```

- `@RestController` 어노테이션을 붙인 경우 스프링 MVC는 요청 매핑 어노테이션을 붙인 객체가 리턴한 객체를
  `알맞은 형식`으로 변환해서 응답 데이터로 전송

- 이때 클래스 패스에 Jackson이 존재하면 JSON 형식의 문자열로 변환해서 응답

  > 스프링 4버전 이전에는 @RestController 어노테이션이 없기 때문에 다음과 같이 `@Controller`, `@ResponseBody`를 함께 사용

  ```java
  @Controller
  public class RestMemberController {
       private MemberDao memberDao;
       private MemberRegisterService registerService;
       
       @RequestMapping(path="/api/members", method = RequestMethod.GET)
       @ResponseBody
       public List<member> members(){
             return memberDao.selectAll();
       }
  }
  ```



#### 2-1. @JsonIgnore를 이용한 예외 처리

- 현재 구현된 응답 결과 `JSON`에는 비밀번호 같은 민감한 정보가 표기되므로 이를 `제외`해야 함

- 다음과 같이 @JasonIgnore 어노테이션을 이용하여 이를 처리

  ```java
    public class Member {
  
        private Long id;
        private String email;
        @JsonIgnore
        private String password;
        private String name;
        private LocalDateTime registerDateTime;
            ...
  ```

  

#### 2-2. 날짜 형식 변환 처리: @JsonFormat

- 앞선 코드를 보면 registerDateTime의 타입이 LocalDateTime으로써, 이는 다음과 같은 유닉스 타임 스태프로 날짜를 표기

  - “registerDateTime”: 1519870069000

- 숫자나 배열보다는 특정 형식으로 날짜를 표현하므로, 다음과 같이 @JasonFormat 어노테이션을 이용

  ```java
    public class Member {
      
        private Long id;
        private String email;
        @JsonIgnore
        private String password;
        private String name;
        @JsonFormat(shape = Shape.STRING)  // ISO-8601 형식으로 변환
        private LocalDateTime registerDateTime;
            ...
  ```

  - “registerDateTime”: “2019-09-30T11:07:49”

- ISO-8601 형식이 아닌 원하는 형식일 경우 다음과 같이 @JsonFormat의 pattern 속성을 이용

  ```java
    @JsonFormat(pattern = "yyyyMMddHHmmss")  
    private LocalDateTime registerDateTime;
  ```

  - “registerDateTime”: “20190930111323”

    

#### 2-3. 날짜 형식 변환처리: 기본 적용 설정

- 날짜를 지정하는 모든 형식의 앞선 어노테이션을 일일히 붙이는 것은 `비효율적`

- Spring MVC의 설정을 변경함으로 해결 가능

  ```java
    // MvcConfig.java
    public class MvcConfig implements WebMvcConfigurer {
        ...
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
            ObjectMapper objectMapper = Jackson2ObjectMapperBuilder // 스프링이 제공하는 클래스
                    .json()
                                    // 다음 매서드는 유닉스 타임스태프로 출력하는 기능을 비활성화(ISO-8601 사용)
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .build();
                   /*
                    * 미리 등록된 HttpMessageConverter에는 Jackson을 사용하는 것도 포함되어 있으므로,  
                    * 새로 생성한 HttpMessageConverter는 다음과 같이 인덱스 0에 위치(맨 앞)함
                    */
            converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
        }
    }
  ```

- `extendMessageConverters()` 매서드는 WebMvcConfigurer에 정의된 인터페이스로,
  `HttpMessageConverter`를 추가로 설정할 때 사용

- 새로 생성한 `ObjectMapper`를 사용하는 객체를 `converters`의 `첫 번째` 항목으로 등록하면 설정 완료

  

## 4. @RequestBody로 JSON 요청 처리

------

JSON 형식의 요청 데이터를 다음과 같이 커맨드 객체에 `@RequestBody` 어노테이션을 붙여 자바 객체로 변환

```java
    // RestMemberController.java
    @RestController
    public class RestMemberController {
        ...
        @PostMapping("/api/members")
        public ResponseEntity<Object> newMember(
                            // 다음 어노테이션을 붙임으로, JSON 형식의 문자열을 해당 자바 객체로 변환
                @RequestBody @Valid RegisterRequest regReq ) {
            try {
                Long newMemberId = registerService.regist(regReq);
                URI uri = URI.create("/api/members/" + newMemberId);
                return ResponseEntity.created(uri).build();
            } catch (DuplicateMemberException dupEx) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        ...
    }
```



#### 4-1. JSON 데이터의 날짜 형식 다루기

- 별도의 설정을 하지 않으면, JSON에서는 다음 패턴의 문자열을 LocalDateTime과 Date로 변환

  - `yyyy-MM-ddTHH:mm:ss`

- 특정 패턴은 @JsonFormat 어노테이션의 pattern 속성을 사용해 지정 가능

  ```java
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime birthDateTime;
  ```

- 해당 타입을 갖는 모든 속성에 적용하려면 다음과 같이 스프링 MVC 설정을 변경

  ```java
    // MvcConfig.java
    @Configuration
    @EnableWebMvc
    public class MvcConfig implements WebMvcConfigurer {
        ...
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
                    .json()
                    .featuresToEnable(SerializationFeature.INDENT_OUTPUTS)
                                // 다음 두 줄에 걸쳐 스프링 MVC 속성을 설정  
                    .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(formatter))
                    .simpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .build();
            converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
        }
    }
  ```

  

## 5. ResponseEntity로 객체 리턴하고 응답 코드 지정하기

------

지금까지는 상태 코드를 지정하기 위해 다음과 같이 `HttpSevletResponse`의 `setStatus()`, `sendError()` 매서드를 이용

```java
    @GetMapping("/api/members2/{id}")
    public Member member2(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Member member = memberDao.selectById(id);
        if (member == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        return member;
    }
```

하지만 위와 같이 404 응답을 하면 JSON 형식이 아닌 서버가 기존으로 제공하는 `HTML`을 응답 결과로 제공



#### 5-1. ResponseEntity를 이용한 응답 데이터 처리

- 앞선 문제점은 `ReponseEntity`를 이용하여 정상/비정상인 두 경우 `모두` 처리 가능

- 에러 상황일 때 응답으로 사용할 ErrorResponse 클래스를 다음과 같이 생성

  ```java
    // ErrorReponse.java
    public class ErrorResponse {
        private String message;
  
        public ErrorResponse(String message) {
            this.message = message;
        }
  
        public String getMessage() {
            return message;
        }
    }
  ```

- 앞선 클래스를 이용하여 다음과 같이 매서드를 새롭게 구성

  ```java
    // RestMemberController.java
    @RestController
    public class RestMemberController {
        ...
        @GetMapping("/api/members/{id}")
        public ResponseEntity<Object> member(@PathVariable Long id) {
            Member member = memberDao.selectById(id);
            if (member == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("no member"));
            }
            return ResponseEntity.ok(member);
        }
    	    ...
    }
  ```

  

- 스프링 MVC에서는 ReponseEntity()의 `body`로 지정한 객체를 이용해 변환을 처리

- ResponseEntity의 status로 지정한 값을 응답 상태 코드로 사용

  > ResponseEntity.status(상태코드).body(객체)
  > 결국, 위의 코드에서 member를 찾지 못한 에러가 발생시 다음과 같은 `JSON`형식의 데이터를 생성
  >
  > > `실행화면`
  > > {
  > > “member” : “no member”
  > > }
  > >
  > > 

#### 5-2. @ExceptionHandler 적용 매서드에서 ReponseEntity로 응답하기

- 앞선 코드처럼 member가 없는 에러가 여러 곳에서 발생 한다면 `코드 중복`이 발생

- 이를 @ExceptionHandler 어노테이션을 적용한 매서드에서 에러처리를 하도록 구현해 해결

  ```java
    // RestMemberController.java
    @GetMapping("/api/members3/{id}")
    public Member member3(@PathVariable Long id) {
        Member member = memberDao.selectById(id);
        if (member == null) {
            throw new MemberNotFoundException();
        }
        return member;
    }
  
    // 위 매서드에서 발생하는 에러는 다음 매서드가 JSON 형식으로 처리
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoData() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("no member"));
    }
  ```

- @RestControllerAdvice 어노테이션을 이용해 다음처럼 에러 처리 코드를 별도 클래스로 분리 가능

  ```java
    // ApiExceptionAdvice.java
    @RestControllerAdvice("controller")
    public class ApiExceptionAdvice {
  
        @ExceptionHandler(MemberNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoData() {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("no member"));
        }
        ...
    }
  ```

  

#### 5-3. @Valid 에러 결과를 JSON으로 응답하기

- `@Valid` 어노테이션을 붙인 커맨드 객체가 값 검증에 실패하면 400코드를 `HTML` 응답으로 전송

- 이를 해결하기 위해 다음과 같이 Errors 타입 파라미터를 추가해, 직접 에러 응답을 생성

  ```java
    @PostMapping("/api/members")
    public ResponseEntity<Object> newMember(
            @RequestBody @Valid RegisterRequest regReq,
            Errors errors) {
        // hasErrors()를 호출하여 검증 에러 존재를 판별
        if (errors.hasErrors()) {
            String errorCodes = errors.getAllErrors()
                    .stream()
                    .map(error -> error.getCodes()[0])
                    .collect(Collectors.joining(","));
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("errorCodes = " + errorCodes));
        }
        ...
  ```

- 앞선 코드에서 Errors 타입의 파라미터가 존재하지 않으면, MethodArgumentNotValidException

  이 발생하므로 다음과 같이 @ExceptionHandler 어노테이션을 이용해 분리 가능

  ```java
    // ApiExceptionAdvice.java
    @RestControllerAdvice("controller")
    public class ApiExceptionAdvice {
        ...
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleBindException(MethodArgumentNotValidException ex) {
            String errorCodes = ex.getBindingResult().getAllErrors()
                    .stream()
                    .map(error -> error.getCodes()[0])
                    .collect(Collectors.joining(","));
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("errorCodes = " + errorCodes));
        }
    }
  ```

