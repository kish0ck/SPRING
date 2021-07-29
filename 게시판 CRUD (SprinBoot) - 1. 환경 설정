# 게시판 CRUD (SprinBoot) - 1. 환경 설정

- 사용 기술 스택 : Java 1.8, maven, Oracle, mabatis,  jsp, jstl, jdbc

- 폴더구조

  ![image-20210728135914103](C:\Users\user\AppData\Roaming\Typora\typora-user-images\image-20210728135914103.png)

- DB Table

  ```sql
  CREATE TABLE BOARD_TEST
  (
      BNO         NUMBER(4) NOT NULL,
      SUBJECT     VARCHAR2(2000),
      CONTENT     VARCHAR2(2000),
      WRITER      VARCHAR2(10),
      REG_DATE    DATE
  );
  
  -- 데이터 추가 
  ```

   



### 환경설정

- [참고자료] https://private.tistory.com/52?category=753861

1. src/main/resources - application.properties에 작성

   application.properties (어플리케이션내의 설정 파일) : 스프링부트는 기본적으로 XML을 이용하지 않고, 문자열 등 특별한 설정이 필요한 경우에 사용 할 수 있는 application.properties 파일이 생성된다.

```properties
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp
 
spring.datasource.driver-class-name= oracle.jdbc.driver.OracleDriver
spring.datasource.url=jdbc:oracle:thin:@*데이터베이스 정보*
spring.datasource.username= *username*
spring.datasource.password= *password*
```

- prefix : 경로 지정 / suffix : 파일 확장자명

- JDBC 연동 에러 발생시 참고 블로그 :  https://eknote.tistory.com/2243

  

  2.테스트용 jsp 생성

  ```jsp
  <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
  <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
  <html>
  
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  	<title>test.jsp 테스트</title>
  </head>
  
  <body>
   
  	<h2> ! T E S T ! </h2>
   
  </body>
  
  </html>
  ```

  3.JSP 테스트를 위한 Controller 작성

  ```java
  package com.example.board;
  
  import javax.annotation.Resource;
  
  import org.springframework.stereotype.Controller;
  import org.springframework.web.bind.annotation.RequestMapping;
  
  import com.example.board.mapper.BoardMapper;
  
  
  @Controller
  public class JspTest {
  
  		@Resource(name="com.example.board.mapper.BoardMapper")
  		BoardMapper mBoardMapper;
  	
  		@RequestMapping("/test")
  		private String jspTest() throws Exception {
  			
  			System.out.println(mBoardMapper.boardCount());
  			
  			return "test";
  		}
  
  }
  ```

  4.저장 후 실행 

  ![image-20210728141343003](C:\Users\user\AppData\Roaming\Typora\typora-user-images\image-20210728141343003.png)

  5.com.example.BoardApplication.java에서 SqlSessionFactory Bean 생성

  ```java
  package com.example.board;
  
  import javax.sql.DataSource;
  
  import org.apache.ibatis.session.SqlSessionFactory;
  import org.mybatis.spring.SqlSessionFactoryBean;
  import org.mybatis.spring.annotation.MapperScan;
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.context.annotation.Bean;
  
  @MapperScan(value={"com.example.board.mapper"})
  @SpringBootApplication
  public class BoardApplication {
  
  	public static void main(String[] args) {
  		SpringApplication.run(BoardApplication.class, args);
  	}
  
  	
  	/*
       * SqlSessionFactory 설정 
       */
  	@Bean
      public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception{
          
          SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
          
          sessionFactory.setDataSource(dataSource);
          return sessionFactory.getObject();
      }
  
  }
  ```

   *** @Bean : 스프링에 필요한 객체를 생성**

   *** sqlSessionFactory() : MyBatis의 SqlSessionFactory를 반환해줍니다. 스프링부트가 실행할 때 DataSource객체를 이 메서드 실행 시 주입해서 결과를 만들고, 그 결과를 스프링내 빈으로 사용하게 됩니다.** 



6. 패키지 구조 설정

- 먼저 패키지 생성

(board 패키지 생성 후 board 패키지 내에 controller, domain, mapper, service 패키지 생성)

※ Tip) 구조 깔끔하게 확인 가능하다

![image-20210728142900898](C:\Users\user\AppData\Roaming\Typora\typora-user-images\image-20210728142900898.png)













[참고 자료] https://eknote.tistory.com/2243
