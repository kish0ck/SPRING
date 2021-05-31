# MVC 4 : 날짜 값 변환, @PathVariable, 익셉션 처리



## 1. 날짜를 이용한 회원 검색 기능

------

- 회원가입 일자를 기준으로 검색하는 기능을 구현

  ```java
  // MemberDao.java
  public class MemberDao {
    ...
    public List<Member> selectByRegdate(LocalDateTime from, LocalDateTime to) {
        List<Member> results = jdbcTemplate.query(
                "select * from MEMBER where REGDATE between ? and ? " +
                        "order by REGDATE desc",
                memRowMapper,
                from, to);
        return results;
    }
  }
  ```

- `selectRegDate()` 매서드는 REGDATE 값이 두 파라미터로 전달받은 `from`과 `to` 사이에 있는 Member 목록을 구함

  

#### 1-1. 커맨드 객체 Date 타입 프로퍼티 변환 처리: @DateTimeFormat

- 검색을 위한 입력 폼은 다음처럼 from과 to인 `<input>` 태그를 정의

  ```jsp
    <input type="text" name="from" />
    <input type="text" name="to" />
  ```

- `<input>`에 입력한 문자열을 `LocalDateTime` 타입으로 변환이 필요 @DateTimeFormat 어노테이션을 적용해 다음처럼 변환

  ```java
    // ListCommand.java
    public class ListCommand {
            // 어노테이션을 적용하여 지정된 형식으로 변환
        @DateTimeFormat(pattern = "yyyyMMddHH")
        private LocalDateTime from;
        @DateTimeFormat(pattern = "yyyyMMddHH")
        private LocalDateTime to;
        ...
    }
  ```

- 컨트롤러 클래스는 별도 설정 없이 `ListCommand` 클래스를 커맨드 객체로 사용

  ```java
    // MemberListController.java
    @Controller
    public class MemberListController {
        ...
        @RequestMapping("/members")
        public String list(
                @ModelAttribute("cmd") ListCommand listCommand,
                Model model) {
                    // from과 to를 이용하여 member 목록을 구한 뒤, 뷰에 "members" 속성으로 전달
            if (listCommand.getFrom() != null && listCommand.getTo() != null) {
                List<Member> members = memberDao.selectByRegdate(
                        listCommand.getFrom(), listCommand.getTo());
                model.addAttribute("members", members);
            }
            return "member/memberList";
        }
  
    }
  ```

  

#### 1-2. 변환 에러 처리

- 만약 폼에서 from과 to에 입력형식인 “yyyyMMddHH”와 달리 “yyyyMMdd”가 입력되면 에러 발생

- 이를 처리해 알맞은 에러 메시지를 보여주기 위해 다음과 같이 `Errors` 타입 파라미터를 요청 어노테이션 적용 매서드에 추가

  ```java
    // MemberListController.java
    @Controller
    public class MemberListController {
        ...
        @RequestMapping("/members")
        public String list(
                @ModelAttribute("cmd") ListCommand listCommand,
                Errors errors, Model model) {
            if (errors.hasErrors()) {
                return "member/memberList";
            }
            if (listCommand.getFrom() != null && listCommand.getTo() != null) {
                List<Member> members = memberDao.selectByRegdate(
                        listCommand.getFrom(), listCommand.getTo());
                model.addAttribute("members", members);
            }
            return "member/memberList";
        }
  
    }
  ```

- 변환 에러가 발생하게 되면, 에러 코드로 `"typeMismatch"` 를 추가하므로 다음처럼 `메세지 프로퍼티 파일`을 통해 에러 메세지를 변경 가능

  ```jsp
    // label.properties
    ...
    typeMismatch.java.time.LocalDateTime = 잘못된 형식
  ```

  

## 2. @PathVariavle을 이용한 경로 변수 처리

------

다음은 ID가 10인 회원의 정보를 조회하기 위한 URL

- http://localhost:8080/sp5-hap14/members/10

  > 회원의 ID가 달라지면 경로의 마지막 부분이 달라짐

- 이렇듯 경로의 일부가 고정되어 있지 않고 달라질 때 다음처럼 `@PathVariable` 어노테이션을 통해 `가변 경로`

   처리 가능

  ```java
    // MemberDetailController.java
    @Controller
    public class MemberDetailController {
        ...
            // {}중괄호에 둘러쌓인 부분이 경로 변수
        @GetMapping("/members/{id}")
        public String detail(@PathVariable("id") Long memId, Model model) {
            Member member = memberDao.selectById(memId);
            if (member == null) {
                throw new MemberNotFoundException();
            }
            model.addAttribute("member", member);
            return "member/memberDetail";
        }
        ...
    }
  ```

  

## 3. 컨트롤러 익셉션 처리

------

- 알맞은 익셉션 처리를 하여 사용자에게 더 적합한 안내를 다음과 같이 제공

  > 익셉션 처리를 해주지 않으면 HTTP Status 400 같은 페이지가 사용자에게 보임
  >
  > 1. 컨트롤러에서 발생한 익셉션을 직접 처리하기 위해 `@ExceptionHandler` 어노테이션을 사용
  > 2. `@ControllerAdvice` 어노테이션을 이용해 공통 익셉션을 처리

#### 3-1. @Exception 어노테이션을 이용한 컨트롤러 익셉션 직접 처리

- 같은 컨트롤러에 `@ExceptionHandler` 어노테이션을 적용한 매서드가 존재하면 그 메서드가 `익셉션을 처리`

  ```java
    // MemberDetailController.java
    @Controller
    public class MemberDetailController {
        ...
        @GetMapping("/members/{id}")
        public String detail(@PathVariable("id") Long memId, Model model) {
            Member member = memberDao.selectById(memId);
            if (member == null) {
                throw new MemberNotFoundException();
            }
            model.addAttribute("member", member);
            return "member/memberDetail";
        }
  
            // 아래의 두 매서드는 각각 해당하는 타입의 익셉션을 처리하며 뷰 이름을 리턴
        @ExceptionHandler(TypeMismatchException.class)
        public String handleTypeMismatchException() {
            return "member/invalidId";
        }
  
        @ExceptionHandler(MemberNotFoundException.class)
        public String handleNotFoundException() {
            return "member/noMember";
        }
    }
  ```

  

#### 3-2. @ControllerAdvice 어노테이션을 이용한 공통 익셉션 처리

- `다수`의 컨트롤러에서 `동일` 타입의 익셉션이 발생하는 경우 사용

- `@ControllerAdvice` 어노테이션이 적용된 클래스는 지정한 범위의 컨트롤러에 `공통`으로 사용될 설정을 지정 가능

- `@ControllerAdvice` 어노테이션 적용 클래스가 동작하기 위해 해당 클래스를 스프링의 Bean으로 등록해야 함

  ```java
    // CommonExceptionHandler.java
    // spring 패키지와 그 하위 패키지에 속한 컨트롤러 클래스를 위한 공통 기능을 정의  
    @ControllerAdvice("spring")
    public class CommonExceptionHandler {
            // 처리하는 익셉션의 종류는 RuntimeException
        @ExceptionHandler(RuntimeException.class)
        public String handleRuntimeException() {
            return "error/commonException";
        }
    }
  ```

  

#### 3-3. @ExceptionHandler 적용 매서드의 우선 순위

- @ControllerAdvice 클래스에 있는 @ExceptionHandler 매서드와 컨트롤러 클래스에 있는 @ExceptionHandler 매서드 중,
  `컨트롤러` 클래스의 @ExceptionHandler 매서드가 `우선함`
- 컨트롤러 매서드를 실행하는 도중 익셉션이 발생하면 다음의 `순서`로 처리
  1. `같은 컨트롤러`에 위치한 @ExceptionHandler 매서드 중 해당 익셉션을 처리할 수 있는 매서드를 검색
  2. `@ControllerAdvice` 클래스에 위치한 @ExceptionHandler 매서드를 검색
