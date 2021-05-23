# 스프링 입문5

## 4장

### 1. @Autowired를 이용한 의존 자동 주입

- 자동 주입 기능을 사용하면 스프링이 알아서 의존 객체를 찾아서 주입한다.

``` java
// 자동 주입 전
@Bean
public ChangePasswordService changePwdSvc() {
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao());	// 의존 주입 부분
    return pwdSvc;
}

// 자동 주입 후
public class ChangePasswordService {
	@Autowired
	private MemberDao memberDao;	// 의존 주입 필드

	public void changePassword(String email, String oldPwd, String newPwd) {
		Member member = memberDao.selectByEmail(email);
		if (member == null)
			throw new MemberNotFoundException();

		member.changePassword(oldPwd, newPwd);

		memberDao.update(member);
	}

	public void setMemberDao(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
}

@Bean
public ChangePasswordService changePwdSvc() {
    ChangePasswordService pwdSvc = new ChangePasswordService();  
    // pwdSvc.setMemberDao(memberDao());
    // 의존을 주입하지 않아도 스프링이 @Autowired가 붙은 필드에
    // 해당 타입의 빈 객체를 찾아서 주입한다.
    return pwdSvc;
}
```

- @Autowired 애노테이션은 메서드에도 붙일 수 있다.
- 스프링은 @Autowired 메서드가 붙은 메서드를 호출한 뒤 메서드 파라미터 타입에 해당하는 빈 객체를 찾아 인자로 주입한다.

``` java
public class MemberInfoPrinter {

	private MemberDao memDao;
	private MemberPrinter printer;

	public void printMemberInfo(String email) {
		Member member = memDao.selectByEmail(email);
		if (member == null) {
			System.out.println("데이터 없음\n");
			return;
		}
		printer.print(member);
		System.out.println();
	}

	@Autowired	// 붙인 부분 1
	public void setMemberDao(MemberDao memberDao) {
		this.memDao = memberDao;
	}
	@Autowired	// 붙인 부분 2
	public void setPrinter(MemberPrinter printer) {
		this.printer = printer;
	}

}

@Configuration
public class AppCtx {

    ... // 생략
	
	@Bean
	public MemberInfoPrinter infoPrinter() {
		MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
        // infoPrinter.setMemberDao(memberDao());
		// infoPrinter.setPrinter(memberPrinter());
		return infoPrinter;
	}
	... // 생략
}
```



### 2. 일치하는 빈이 없는 경우

- @Autowired 애노테이션을 적용한 대상에 일치하는 빈이 없으면 어떻게 될까?
- UnsatisfiedDependencyException 이 발생한다.
- @Autowired 애노테이션을 붙인 `MemberRegisterService`의 memberDao 필드에 주입할 MemberDao 빈이 존재하지 않아 에러가 발생했다.

``` java
@Configuration
public class AppCtx {

	// @Bean
	// public MemberDao memberDao() {
	//  return new MemberDao();
	// }
	
	@Bean
	public MemberRegisterService memberRegSvc() {
		return new MemberRegisterService(memberDao());
	}
    ... // 생략
}
```

### 2-1. 일치하는 빈이 두 개 이상인 경우

- @Autowired 애노테이션을 붙인 주입 대상에 일치하는 빈이 두 개 이상이면 어떻게 될까?
- `NoUniqueBeanDefinitionException : No qualifying bean of type 'spring.MemberPrinter' available: expected single matching bean but found2: memberPrinter1, memberPrinter2`
- 자동 주입을 하려면 해당 타입을 가진 빈이 어떤 빈이지 정확하게 한정 할 수 있어야 하는데 MemberPrinter 타입의 빈이 두개여서 어떤 빈을 자동 주입 대상으로 선택해야 할지 한정 할 수 없다.

``` java
@Configuration
public class AppCtx {
    
//	@Bean
//	public MemberPrinter memberPrinter() {
//		return new MemberPrinter();
//	}

	@Bean
	public MemberPrinter memberPrinter1() {
		return new MemberPrinter();
	}
	@Bean
	public MemberPrinter memberPrinter2() {
		return new MemberPrinter();
	}
    ... // 생략
}
```

#### 3. @Qualifier 애노테이션을 이용한 의존 객체 선택

- 자동 주입 가능한 빈이 두 개 이상이면 자동 주입할 빈을 지정할 수 있는 방법으로 @Qualifier 애토네이션을 사용하면 자동 주입 대상 빈을 한정할 수 있다.
- @Qualifier 애노테이션은 두 위치에서 사용 가능하다.
  - 1. @Bean 애노테이션을 붙인 빈 설정 메서드
  - 2. @Autowired 애노테이션에서 자동 주입할 빈을 한정 할때 사용

``` java
package chap04.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
... // 생략

@Configuration
public class AppCtx {
	
    ... // 생략

	@Bean
    @Qualifier("printer")	// 1번 @Bean 애노테이션이 붙은 메서드 위치
	public MemberPrinter memberPrinter1() {
		return new MemberPrinter();
	}
	@Bean
	public MemberPrinter memberPrinter2() {
		return new MemberPrinter();
	}
	
	... // 생략
}


public class MemberListPrinter {

	private MemberDao memberDao;
	private MemberPrinter printer;

	... // 생략

	@Autowired
    @Qualifier("printer")
	public void setPrinter(MemberPrinter printer) {
		this.printer = printer;
	}
}
```

#### 3-1. 빈 이름과 기본 한정자

- 빈 설정에 @Qualifier 애노테이션이 없으면 빈의 이름을 한정자로 지정한다.
- @Autowired 애노테이션도 @Qualifier 애노테이션이 없으면 필드나 파라미터 이름을 한정자로 사용한다.
  - 예를 들어 printer 필드에 일치하는 빈이 두 개 이상 존재하면 한정자로 필드 이름인 "printer"를 사용한다

``` java
@Configuration
public class AppCtx {
	@Bean
	public MemberPrinter printer() {
		return new MemberPrinter();
	}
	@Bean
	@Qualifier("mprinter")
	public MemberPrinter printer2() {
		return new MemberPrinter();
	}
}

// MemberPrinter 타입의 빈이 두 개 이상 존재하는 경우 한정자로 필드 이름인 "printer"를 사용한다.
public class MemberInfoPrinter2 {
    @Autowired
    private MemberPrinter printer;
}
	
```

### 4. 상위/하위(상속) 타입 관계와 자동 주입

- `MemberPrinter` 클래스를 상속받은 `MemberSummaryPrinter` 클래스를 만들었다.
- 한정자 @Qualifier 애노테이션을 제거한뒤 실행한 결과 앞서 `MemberPrinter` 타입 빈을 두 개 설정하고 실행했을 때와 동일한 익셉션이 발생했다.
- `MemberSummaryPrinter` 클래스는 `MemberPrinter` 타입에도 할당할 수 있으므로, 스프링 컨테이너는 `MemberPrinter` 타입 빈을 자동 주입해야 하는 @Autowired 애노테이션 태그를 만나면 `memberPrinter1` 빈과 `memberPrinter2` 타입 빈 중에서 어떤 빈을 주입해야 할지 알 수 없다.

``` java
public class MemberSummaryPrinter extends MemberPrinter{
    @Override
    public void print(Member member) {
        System.out.printf(
                "회원 정보 : 이메일=%s, 이름=%s\n",
                member.getEmail(), member.getName()
        );
    }
}

// 설정 정보 클래스
@Configuration
public class AppCtx {
	@Bean
	public MemberPrinter memberPrinter1() {
		return new MemberPrinter();
	}
	@Bean
	public MemberSummaryPrinter memberPrinter2() {
		return new MemberSummaryPrinter();
	}
}
```

### 5.@Autowired 애노테이션의 필수 여부

- `dateTimeFormatter` 필드가 null이면 날짜 형식 `%tF`로 출력하고  null이 아니면 dateTimeFormatter를 이용해서 날짜 형식을 맞춰 출력하도록 `print()` 메서드를 수정했다.

- setter 메서드는 @Autowired 애노테이션을 이용해서 자동 주입하도록 했다.

- `print()` 메서드는 dateTimeFormatter가 null인 경우에도 알맞게 동작한다. 즉, 반드시 `setDateFormmaterr()`를 통해서 의존 객체를 주입할 필요는 없다.

- @Autowired 애노테이션은 기본적으로 @Autowired 애노테이션을 붙인 타입에 해당하는 빈이 존재하지 않으면 익셉션을 발생한다. 따라서 `setDateFormatter()`메서드에서 필요로 하는 DateTImeFormatter 타입의 빈이 존재하지 않으면 익셉션이 발생한다.

- ##### 이처럼, 자동 주입할 대상이 필수가 아닌경우에는 @Autowired 애노테이션의 required 속성을 false로 지정하면 매칭되는 빈이 없어도 익셉션이 발생하지 않으며 자동 주입을 수행하지 않는다.

- ##### 두번째 방법으로는 자바8 의 Optional을 사용해도 된다

  - 자동 주입 대상 타입이 Optional인 경우, 일치하는 빈이 존재하지 않으면 값이 없는 Optional을 인자로 전달하고(익셉션이 발생하지 않는다.), 일치하는 빈이 존재하면 해당 빈을 값으로 갖는 Optional을 인자로 전달한다.

- ##### 세번째 방법으로는 @Nullable 애노테이션을 사용하는 것이다

  - @Nullable 애노테이션을 의존 주입 대상 파라미터에 붙이면, 스프링 컨테이너는 setter 메서드를 호출할 때 자동 주입할 빈이 존재하면 해당 빈을 인자로 전달하고, 존재하지 않으면 인자로 null을 전달한다.

- ##### required 와 @Nullable의 차이점

  - 빈이 존재하지 않으면 required는 setter 메서드를 호출하지 않고, @Nullable의 경우는 빈이 존재하지 않아도 메서드가 호출된다는 점이다.

``` java
public class MemberPrinter {
	private DateTimeFormatter dateTimeFormatter;

	public void print(Member member) {
		if(dateTimeFormatter == null) {
			System.out.printf(
					"회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n",
					member.getId(), member.getEmail(),
					member.getName(), member.getRegisterDateTime());
		}else {
			System.out.printf(
					"회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%s\n",
					member.getId(), member.getEmail(),
					member.getName(),
					dateTimeFormatter.format(member.getRegisterDateTime()));
		}
	}

	@Autowired(required = false) // <= required 속성을 false로
	public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
		this.dateTimeFormatter = dateTimeFormatter;
	}
    
    @Autowired // Optional을 사용한 경우
	public void setDateTimeFormatter(Optional<DateTimeFormatter> formatterOpt) {
        if(formatterOpt.isPresent()){
			this.dateTimeFormatter = formatterOpt.get();
        }else {
            this.dateTimeFormatter = null;
        }
	}
    
    @Autowired // @Nullable 애노테이션을 사용한 경우
	public void setDateTimeFormatter(@Nullable DateTimeFormatter dateTimeFormatter) {
		this.dateTimeFormatter = dateTimeFormatter;
	}
}
```

