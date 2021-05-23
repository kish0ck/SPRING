# 스프링 입문5

## 7장

> 데코레이터 패턴과 프록시 패턴 비교
>
> 참고 : https://sabarada.tistory.com/60

### AOP 프로그래밍

- 스프링 프레임워크 AOP 기능은 spring-aop 모듈이 제공하는데 spring-context 모듈을 의존 대상에 추가하면 spring-aop 모듈도 함께 의존 대상에 포함된다.
- aspectjweaver 모듈은 AOP를 설정하는데 필요한 애노테이션을 제공한다.



### 2. 프록시와 AOP

- 핵심 기능의 실행은 다른 객체에 위임하고 부가적인 기능을 제공하는 객체를 **프록시(proxy)**라고 한다.

> - 아래의 코드는 프록시(proxy)라기 보다는 데코레이터(decorator) 객체에 가깝다.
>
> - 프록시는 접근 제어 관점에 초점이 맞춰져 있다면, 데코레이터는 기능 추가와 확장에 초점이 맞춰져 있기 때문이다.
> - 기존 기능에 시간 측정 기능을 추가하고 있기 때문에 데코레이터에 가깝지만 스프링의 레퍼런스 문서에서 AOP를 설명할 때 프록시란 용어를 사용하고 있어 프록시를 사용했다.

- 프록시의 특징은 핵심 기능은 구현하지 않는다. 

- 대신 여러 객체에 공통으로 적용할 수 있는 기능을 구현한다.

- 이 예제에서 ExeTimeCalculator 클래스는 ImpeCalculator 객체와 RecCalculator 객체에 공통으로 적용되는 실행 시간 측정 기능을 구현하고 있다.

  - ImpeCalculator와 RecClaculator는 팩토리얼을 구한다는 핵심 기능 구현
  - ExeTimeCalculator는 실행 시간 측정이라는 공통 기능 구현

- #### 공통 기능 구현과 핵심 기능 구현을 분리하는 것이 AOP의 핵심

``` java
// 시간 측정을 위한 프록시 객체
public class ExeTimeCalculator implements Calculator{

    private Calculator delegate;

    public ExeTimeCalculator(Calculator delegate) {
        this.delegate = delegate;
    }

    @Override
    public long factorial(long num) {
        long start = System.nanoTime();
        long result = delegate.factorial(num);
        long end = System.nanoTime();
        System.out.printf("%s.factorial(%d) 실행 시간 = %d\n",delegate.getClass().getSimpleName(),num,(end-start));
        return result;
    }
}

// 반복문을 활용한 팩토리얼
public class ImpeCalculator implements Calculator{
    @Override
    public long factorial(long num) {
        long result = 1;
        for (long i =1; i <= num ; i++) {
            result *= i;
        }
        return result;
    }
}

// 재귀호출을 활용한 팩토리얼
public class RecCalculator implements Calculator{
    @Override
    public long factorial(long num) {
            if(num == 0)
                return 1;
            else
                return num * factorial(num -1);
    }
}
```



### 2.1 AOP

- AOP는 Aspect Oriented Programming의 약자로, 여러 객체에 공통으로 적용할 수 있는 기능을 분리해서 재사용성을 높여주는 프로그래밍 기법이다.
- AOP는 핵심 기능과 공통 기능의 구현을 분리함으로써 핵심 기능을 구현한 코드의 수정 없이 공통 기능을 적용할 수 있게 해준다.
- 핵심 기능에 공통 기능을 삽입하는 방법
  - 컴파일 시점에 코드에 공통 기능을 삽입하는 방법 ( AspectJ와 같은 AOP 전용 도구 필요)
  - 클래스 로딩 시점에 바이트 코드에 공통 기능을 삽입하는 방법 ( AspectJ와 같은 AOP 전용 도구 필요)
  - 런타임에 프록시 객체를 생성해서 공통 기능을 삽입하는 방법 ( 스프링이 제공하는 AOP 방식)

| 용어      | 의미                                                         |
| --------- | ------------------------------------------------------------ |
| Advice    | 언제 공통 관심 기능을 핵심 로직에 적용할 지를 정의한다. 예를 들어 '메서드를 호출하기 전'(언제)에 '트랜잭션 시작'(공통 기능) 기능을 적용한다는 것을 정의한다. |
| Joinpoint | Advice를 적용 가능한 지점을 의미한다. 메서드 호출, 필드 값 변경 등이 Joinpoint에 해당한다. 스프링은 프록시를 이용해서 AOP를 구현하기 때문에 메서드 호출에 대한 Joinpoint만 지원한다. |
| Pointcut  | Joinpoint의 부분 집합으로서 실제 Advice가 적용되는 Joinpoint를 나타낸다. 스프릉에서는 정규 표현식이나 AspectJ의 문법을 이용하여 Pointcut을 정의할 수 있다. |
| Weaving   | Advice를 핵심 로직 코드에 적용하는 것을 weaving이라고 한다.  |
| Aspect    | 여러 객체에 공통으로 적용되는 기능을 Aspect라고 한다. 트랜잭션이나 보안 등이 Aspect의 좋은 예이다. |



### 2.2 Advice의 종류

- 스프링은 프록시를 이용해서 메서드 호출 시점에 Aspect를 적용하기 때문에 구현 가능한 Advice의 종류는 아래 표와 같다.
- 이 중에서 널리 사용되는 것은 Around Advice이다.
  - 이유는 대상 객체의 메서드를 실행하기 전/후, 익셉션 발생 시점 등 다양한 시점에 원하는 기능을 삽입할 수 있기 때문이다.
  - 캐시 기능, 성능 모니터링 기능과 같은 Aspect를 구현할 때에는 Around Advice를 주로 이용한다.

| 종류                   | 설명                                                         |
| ---------------------- | ------------------------------------------------------------ |
| Before Advice          | 대상 객체의 메서드 호출 전에 공통 기능을 실행한다.           |
| After Returning Advice | 대상 객체의 메서드가 익셉션 없이 실행된 이후에 공통 기능을 실행한다. |
| After Throwing Advice  | 대상 객체의 메서드를 실행하는 도중 익셉션이 발생한 경우에 공통 기능을 실행한다. |
| After Advice           | 익셉션 발생 여부에 상관없이 대상 객체의 메서드 실행 후 공통 기능을 실행한다. ( try-catch-finally의 finally 블록과 비슷하다.) |
| Around Advice          | 대상 객체의 메서드 실행 전, 후 또는 익셉션 발생 시점에 공통 기능을 실행하는데 사용된다. |



### 3. 스프링 AOP 구현

- 스프링 AOP를 이용해서 공통 기능을 구현하고 적용하는 방법은 단순하다. 다음과 같은 절차만 따르면 된다.
  - Aspect를 사용할 클래스에 @Aspect 애노테이션을 붙인다.
  - @Pointcut 애노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.
  - 공통 기능을 구현한 메서드에 @Around 애노테이션을 적용한다.



- #### 공통 기능을 제공하는 Aspect 구현 클래스

  - @Pointcut은 공통 기능을 적용할 대상을 설정한다.
    - @pointcut 애노테이션의 값으로 사용할 수 있는 execution 명시자
  - @Around 애노테이션은 Around Advice를 설정한다.
    - @Around 애노테이션 값이 `publicTarget()`인데 이는 publicTarget() 메서드에 정의한 Pointcut에 공통 기능을 적용 한다는 의미.

``` java
@Aspect
public class ExeTimeAspect {

    @Pointcut("execution(public * chap07.chap7..*(..))")
    private void publicTarget() {

    }

    @Around("publicTarget()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            return result;
        }finally {
            long finish = System.nanoTime();
            Signature sig = joinPoint.getSignature();
            System.out.printf("%s.%s(%s) 실행 시간 : %d ns\n",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    sig.getName(), Arrays.toString(joinPoint.getArgs()),
                    (finish-start));
        }
    }
}
```

- #### 공통 기능을 적용하기 위한 스프링 설정 클래스

  - @Aspect 애노테이션을 붙인 클래스를 공통 기능으로 적용하려면 @EnableAspectJAutoProxy 애노테이션을 설정 클래스에 붙여야 한다.
  - 이 애노테이션을 추가하면 스프링은 @Aspect 애노테이션이 붙은 빈 객체를 찾아서 빈 객체의 @Pointcut 설정과 @Around 설정을 사용한다.

``` java
@Configuration
@EnableAspectJAutoProxy
public class AppCtx {

    @Bean
    public ExeTimeAspect exeTimeAspect() {
        return new ExeTimeAspect();
    }

    @Bean
    public Calculator calculator() {
        return new RecCalculator();
    }
}
```

- #### 공통 기능이 적용되는지 확인

  - (1) 번의 결과는 ExeTimeAspect 클래스의 measure() 메서드가 출력한 결과
  - (2) 번의 결과는 MainAspect에서 출력한 결과
  - (3) 번의 결과는 Calculator 타입의 RecCalculator 클래스가 아닌 $Proxy17로 출력된다.
    - 이 타입은 스프링이 생성한 프록시 타입이다. 아래의 흐름을 보자.

![1620108784930](..\img\AOP프록시 흐름.png)

``` java
public class MainAspect {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtx.class);

        Calculator cal = ctx.getBean("calculator",Calculator.class);
        long fiveFact = cal.factorial(5);
        System.out.println("cal.factorial(5) = " + fiveFact);
        System.out.println(cal.getClass().getName());
        ctx.close();
    }
}

// 콘설 출력 결과
// (1) RecCalculator.factorial([5]) 실행 시간 : 41900 ns
// (2) cal.factorial(5) = 120
// (3) com.sun.proxy.$Proxy19
```

### 3.2 ProceedingJoinPoint의 메서드

- 공통 기능 메서드는 대부분 파라미터로 전달받은 ProceedingJoinPoint의 proceed() 메서드만 호출하면 된다.
- 호출되는 대상 객체에 대한 정보, 실행되는 메서드에 대한 정보, 메서드를 호출할 때 전달된 인자에 대한 정보가 필요할 경우 ProceedingJoinPoint 인터페이스는 메서드를 제공한다.
  - Signature getSignature() : 호출되는 메서드에 대한 정보를 구한다.
  - Object getTarget() : 대상 객체를 구한다.
  - Object[] getArgs() : 파라미터 목록을 구한다.
- `org.aspectj.lang.Signature` 인터페이스는 다음 메서드를 제공한다.
  - String getName() : 호출되는 메서드의 이름을 구한다.
  - String toLongString() : 호출되는 메서드를 완전하게 표현한 문장을 구한다(메서드의 리턴타입, 파라미터 타입이 모두 표시된다).
  - String toShortString() : 호출되는 메서드를 축약해서 표현한 문장을 구한다(기본 구현은 메서드의 이름만을 구한다).



### 4. 프록시 생성 방식

- 스프링은 AOP를 위한 프록시 객체를 생성할 때 실제 생성할 빈 객체가 인터페이스를 상속하면 인터페이스를 이용해서 프록시를 생성한다. 따라서 다음과 같은 코드는 에러를 발생시킨다.

``` java
//수정 전
Calculator cal = ctx.getBean("calculator",Calculator.class);

// 수정 후
RecCalculator cal = ctx.getBean("calculator",RecCalculator.class);
```

- calculator 이름의 빈은 `Calculator` 인터페이스를 상속받은 프록시 객체로 getBean() 메서드에서 사용한 타입인 `RecCalculator`와는 다르다.
- calculator 빈의 실제 타입은 Calculator를 상속한 프록시 타입이므로 RecCalculator로 타입 변환을 할 수 없기 때문에 익셉션을 발생한다.

``` java
//AppCtx 파일의 19-22행
@Bean
public Calculator calculator(){
    return new RecCalculator();
}
```

- 위의 코드와 다르게 빈 객체가 인터페이스를 상속할 때 인터페이스가 아닌 클래스를 이용해서 프록시를 생성하고 싶은 경우 다음과 같이 설정하면 된다.
- @EnableAspectJAutoProxy 애노테이션의 proxyTargetClass 속성을 true로 지정하면 인터페이스가 아닌 자바 클래스를 상속받아 프록시를 생성한다.

``` java
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppCtx {
    ... // 생략
}
```



### 4.1 execution 명시자 표현식

- Aspect를 적용할 위치를 지정할 때 사용한 Pointcut 설정을 보면 execution 명시자를 사용하는데 이는 Advice를 적용할 메서드를 지정할 때 사용한다.
- 기본 형식은 아래와 같다.

> execution(수식어패턴? 리턴타입패턴 클래스이름패턴?메서드이름패턴(파라미터패턴))

- `수식어 패턴`은 생략 가능하며 public, protected 등이 온다.
  - 스프링 AOP는 public 메서드에만 적용할 수 있기 때문에 사실상 public만 의미 있다.
- `리턴타입패턴`은 리턴 타입을 명시한다.
- `클래스이름패턴` 과 `메서드이름패턴`은 클래스 이름 및 메서드 이름을 패턴으로 명시한다.
- `파라미터패턴`은 매칭될 파라미터에 대해서 명시한다.
- 각 패턴은 `*`을 이용하여 모든 값을 표현, `..`(점두개)을 이용하여 0개 이상이라는 의미를 표현할 수 있다.

| 예                                              | 설명                                                         |
| ----------------------------------------------- | ------------------------------------------------------------ |
| execution(public void set*(..))                 | 리턴 타입이 void이고, 메서드 이름이 set으로 시작하고, 파라미터가 0개 이상인 메서드 호출 파라미터 부분에 `..`을 사용하여 파라미터가 0개 이상인 것을 표현했다. |
| execution(* chap07.\*.*())                      | chap07 패키지의 타입에 속한 파라미터가 없는 모든 메서드 호출 |
| execution(* chap07..\*.\*(..))                  | chap07 패키지 및 하위 패키지에 있는, 파라미터가 0개 이상인 메서드 호출. 패키지 부분에 `..`을 사용하여 해당 패키지 또는 하위 패키지를 표현했다. |
| execution(Long chap07.Calculator.factorial(..)) | 리턴 타입이 long인 Calculator 타입의 factorial() 메서드 호출 |
| execution(* get\*(*))                           | 이름이 get으로 시작하고 파라미터가 한 개인 메서드 호출       |
| execution(* get\*(*,\*))                        | 이름이 get으로 시작하고 파라미터가 두 개인 메서드 호출       |
| execution(* read*(Integer, ..))                 | 메서드 이름이 read로 시작하고, 첫 번째 파라미터 터압이 integer이며, 한 개 이상의 파라미터를 갖는 메서드 호출 |



### 4.2 Advice 적용 순서

- 한 Pointcut에 여러 Advice를 적용할 수 있다.
- CacheAspect 클래스는 간단하게 캐시를 구현한 공통 기능이다.
  - (1). 첫 번째 인자를 Long 타입으로 구한다.
  - (2). Long 타입의 num(키값)이 cache에 존재하면 키에 해당하는 값을 구해서 리턴한다.
  - (3). 키값이 cache에 존재하지 않으면 프록시 대상 객체를 실행한다.
  - (4). 프록시 대상 객체를 실행한 결과를 cache에 추가한다.
  - (5). 프록시 대상 객체의 실행 결과를 리턴한다.
- @Around 값으로 cacheTarget() 메서드를 지정했고, @Pointcut 설정으로 첫 번째 인자가 long인 메서드를 대상으로 한다. 따라서 execute() 메서드는 앞서 작성한 Calculator의 factorial(long) 메서드에 적용된다.

``` java
@Aspect
public class CacheAspect {
    private Map<Long,Object> cache = new HashMap<>();

    @Pointcut("execution(public * chap07.chap7..*(long))")
    public void cacheTarget() {

    }

    @Around("cacheTarget()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        Long num = (Long) joinPoint.getArgs()[0];							// (1)
        if(cache.containsKey(num)) {										// (2) if문
            System.out.printf("CacheAspect: Cache에서 구함[%d]\n",num);
            return cache.get(num);
        }

        Object result = joinPoint.proceed();								// (3)
        cache.put(num,result);												// (4)
        System.out.printf("CacheAspect: Cache에 추가[%d]\n",num);
        return result;														// (5)
    }
}
```

- 아래 코드를 실행해보면 Advice의 적용 순서가 
  CacheAspect 프록시 -> ExeTimeAspect 프록시 -> 실제 대상 객체 로 실행되는걸 볼 수 있다.

  - Calculator 타입의 cal 빈은 실제로는 CacheAspect 프록시 객체이고 
    CacheASpect 프록시 객체의 대상 객체는 ExeTimeAspect의 프록시 객체이며 
    ExeTimeAspect 프록시의 대상 객체는 실제 대상 객체이다.

- #### 이처럼 어떤 Aspect가 먼저 적용될지는 스프링 프레임워크나 자바 버전에 따라 달라질 수 있기 때문에 적용 순서가 중요하다면 직접 순서를 지정해야 한다.

- @Aspect 애노테이션과 함께 @Order 애노테이션을 클래스에 붙이면 적용 순서를 결정할 수 있다.

``` java
public class MainAspectWithCache {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtxWithCache.class);

        Calculator cal = ctx.getBean("calculator",Calculator.class);
        cal.factorial(7);
        cal.factorial(7);
        cal.factorial(5);
        cal.factorial(5);
        ctx.close();
    }
}

// 콘솔 출력 결과
RecCalculator.factorial([7]) 실행 시간 : 29700 ns
CacheAspect: Cache에 추가[7]
CacheAspect: Cache에서 구함[7]
RecCalculator.factorial([5]) 실행 시간 : 12700 ns
CacheAspect: Cache에 추가[5]
CacheAspect: Cache에서 구함[5]

```

- 아래와 같이 ExeTimeAspect에 @Order 애노테이션 값을 1, CacheASpect에 @Order 애노테이션 값을 2로 적용해서 실행할 경우
- ExeTimeAspect 프록시 -> CacheAspect 프로시 -> 실제 대상 객체 의 순서로 변경 가능하다.

```java
@Aspect
@Order(1)
public class ExeTimeAspect {
    ... // 생략
}

@Aspect
@Order(2)
public class CacheAspect {
    ... // 생략
}

// 콘솔 출력 결과
CacheAspect: Cache에 추가[7]
RecCalculator.factorial([7]) 실행 시간 : 575500 ns
CacheAspect: Cache에서 구함[7]
RecCalculator.factorial([7]) 실행 시간 : 106100 ns
CacheAspect: Cache에 추가[5]
RecCalculator.factorial([5]) 실행 시간 : 96300 ns
CacheAspect: Cache에서 구함[5]
RecCalculator.factorial([5]) 실행 시간 : 248500 ns
```

### 4.3 @Around의 Pointcut 설정과 @Pointcut 재사용

- @Pointcut 애노테이션이 아닌 @Around 애노테이션에 execution 명시자를 직접 지정할 수 있다.

``` java
@Aspect
public class CacheAspect {
    @Around("execution(public * chap07..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        ... // 생략
    }
}
```

- 같은 Pointcut을 여러 Advice가 함께 사용한다면 공통 Pointcut을 재사용할 수도 있다.
- publicTarget() 메서드는 private인데 이 경우 같은 클래스에 있는 @Around 애노테이션에서만 해당 설정을 사용할 수 있다. 다른 클래스에 위치한 @Around 애노테이션에서 publicTarget() 메서드의 Pointcut을 사용하고 싶다면 publicTarget() 메서드를 public으로 바꾸면 된다.

``` java
@Aspect
public class ExeTimeAspect {
    //수정 전
    @Pointcut("execution(public * chap07..*(..))")
    private void publicTarget() {
        
    }
    
    //수정 후
    @Pointcut("execution(public * chap07..*(..))")
    public void publicTarget() {
        
    }
    
    @Around("publicTarget()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        ... // 생략
    }
}

// 다른 클래스의 Pointcut을 사용한 예제
@Aspect
public class CacheAspect {
    // 같은 패키지 내의 클래스의 경우 패키지 경로 제외해도 된다.
    // @Around("ExeTimeAspect.publicTarget()")
    @Around("aspect.ExeTimeAspect.publicTarget()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        ... // 생략
    }
}
```

