# 스프링 MVC 시작하기

## 9장

> 간단한 스프링 MVC 예제
>

### 1. 프로젝트 생성

- webapp은 HTML, CSS, JS, JSP 등 웹 어플리케이션을 구현하는데 필요한 코드가 위치
- WEB-INF에는 web.xml 파일이 위치



### 2. 이클립스 톰캣 설정

- 책 page235~237 참조



### 3. 스프링 MVC를 위한 설정

- 스프링 MVC를 실행하는데 필요한 최소 설정
  - 스프링 MVC의 주요 설정(HandlerMapping, ViewResolver등)
  - 스프링의 DispatcherServlet 설정



### 3.1 스프링 MVC 설정

```java
package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}

}
```

- @EnableWebMvc : 스프링 MVC 설정을 활성화. 스프링 MVC를 사용하는데 필요한 다양한 설정을 생성한다. 내부적으로 다양한 빈 설정을 추가해준다. 이 설정을 직접하려면 수십 줄에 가까운 코드를 작성해야한다. 
- WebMvcConfigurer 인터페이스는 스프링 MVC의 개별 설정을 조정할 때 사용한다.



### 3.2 web.xml 파일에 DispatcherServlet 설정

- 스프링 MVC가 웹 요청을 처리하려면 DispatcherServlet을 통해서 웹 요청을 받아야한다. 이를 위해 web.xml 파일에 DispatcherServlet을 등록한다. src/main/webapp/WEB-INF 폴더에 web.xml 파일을 작성하면 된다.

```java
<?xml version="1.0" encoding="UTF-8"?>

<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
             http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	version="3.1">

	<servlet>
		<servlet-name>dispatcher</servlet-name>
		<servlet-class>
			org.springframework.web.servlet.DispatcherServlet
		</servlet-class>
		<init-param>
			<param-name>contextClass</param-name>
			<param-value>
				org.springframework.web.context.support.AnnotationConfigWebApplicationContext
			</param-value>
		</init-param>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>
				config.MvcConfig
				config.ControllerConfig
			</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>

	<servlet-mapping>
		<servlet-name>dispatcher</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>

	<filter>
		<filter-name>encodingFilter</filter-name>
		<filter-class>
			org.springframework.web.filter.CharacterEncodingFilter
		</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>UTF-8</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>encodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

</web-app>
```

- DispatcherServlet을 dispatcher라는 이름으로 등록
- DispatcherServlet은 초기화 과정에서 contextConfiguration 초기화 파라미터에 지정한 설정 파일을 이용해서 스프링 컨테이너를 초기화한다. MvcCofig 클래스만 작성하고 ControllerConfig 클래스는 아직 작성하지 않았는데 이 파일은 컨트롤러 구현 부분에서 작성할 것이다.



### 4. 코드 구현

- 클라이언트의 요청을 알맞게 컨트롤러
- 처리 결과를 보여줄 JSP



### 4.1 컨트롤러 구현

```java
package chap09;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController {

	@GetMapping("/hello")
	public String hello(Model model,
			@RequestParam(value = "name", required = false) String name) {
		model.addAttribute("greeting", "안녕하세요, " + name);
		return "hello";
	}
}
```

- 스프링 MVC 프레임워크에서 컨트롤러(Controller)란 간단히 설명하면 웹 요청을 처리하고 그 결과를 뷰에 전달하는 스프링 빈 객체이다. 스프링 컨트롤러로 사용될 클래스는 @Controller 애노테이션을 붙여야 하고, @GetMapping 애노테이션이나 @PostMapping 애노테이션과 같은 요청 매핑 애노테이션을 이용해서 처리할 경로를 지정해 주어야 한다.



### 4.2 JSP 구현

- 뷰 코드는 JSP를 이용해서 구현한다. 

```jsp
<%@ page contentType="text/html; charset=utf-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Hello</title>
  </head>
  <body>
    인사말: ${greeting}
  </body>
</html>
```

- HelloController의 hello() 메서드가 리턴한 뷰 이름은 "hello"였는데 JSP 파일의 이름을 보면 "hello.jsp"이다. 여기서 뭔가 관계가 있을 거라 유추해 볼 수 있는데 뷰 이름과 JSP파일과의 연결은 MvcConfig 클래스의 다음 설정을 통해서 이루어진다.

```java
@Override
public void configureViewResolvers(ViewResolverRegistry registry) {
	registry.jsp("/WEB-INF/view/", ".jsp");
}
```

registry.jsp() 코드는 JSP를 뷰 구현으로 사용할 수 있도록 해주는 설정이다. jsp() 메서드의 첫 번째 인자는 JSP 파일 경로를 찾을 때 사용할 접두어이며 두 번째 인자는 접미사이다. 뷰 이름의 앞과 뒤에 각각 접두어와 접미사를 붙여서 최종적으로 사용할 JSP 파일의 경로를 결정한다. 



### 5. 실행하기

톰캣



### 6. 정리

- 스프링 MVC 설정
- 웹 브라우저의 요청을 처리할 컨트롤러 구현
- 컨트롤러의 처리 결과를 보여줄 뷰 코드 구현

