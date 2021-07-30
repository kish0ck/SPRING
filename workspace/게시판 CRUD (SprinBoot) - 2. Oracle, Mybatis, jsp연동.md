# 게시판 CRUD (SprinBoot) - 2. Oracle, Mybatis, jsp 연동



### Oracle, Mybatis, jsp 연동

- [참고자료] https://private.tistory.com/53?category=753861

1. com.example.board.domain에 **BoardVO.java** 클래스 생성 후 작성

   ```java
   package com.example.board.domain;
   
   import java.util.Date;
   
   public class BoardVO {
   	
   	private int bno;
       private String subject;
       private String content;
       private String writer;
       private Date reg_date;
       
   	public int getBno() {
   		return bno;
   	}
   	public void setBno(int bno) {
   		this.bno = bno;
   	}
   	public String getSubject() {
   		return subject;
   	}
   	public void setSubject(String subject) {
   		this.subject = subject;
   	}
   	public String getContent() {
   		return content;
   	}
   	public void setContent(String content) {
   		this.content = content;
   	}
   	public String getWriter() {
   		return writer;
   	}
   	public void setWriter(String writer) {
   		this.writer = writer;
   	}
   	public Date getReg_date() {
   		return reg_date;
   	}
   	public void setReg_date(Date reg_date) {
   		this.reg_date = reg_date;
   	}
   
   }
   ```

   

2. com.example.board.mapper 에 **BoardMapper.java** 추가

   ```java
   package com.example.board.mapper;
   
   import java.util.List;
   
   import org.springframework.stereotype.Repository;
   
   import com.example.board.domain.BoardVO;
   
   @Repository("com.example.board.mapper.BoardMapper")
   public interface BoardMapper {
   	//게시글 개수  
       public int boardCount() throws Exception;
       
       //게시글 목록  
       public List<BoardVO> boardList() throws Exception;
       
       //게시글 상세
       public BoardVO boardDetail(int bno) throws Exception;
       
       //게시글 작성  
       public int boardInsert(BoardVO board) throws Exception;
       
       //게시글 수정  
       public int boardUpdate(BoardVO board) throws Exception;
       
       //게시글 삭제  
       public int boardDelete(int bno) throws Exception;
   }
   ```

    \* @Repository : 해당 클래스가 데이터베이스에 접근하는 클래스임을 명시



3. com.example.board.mapper에 **BoardMapper.xml** 생성 후 작성

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <!DOCTYPE mapper 
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
   "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
    
   <mapper namespace="com.example.board.mapper.BoardMapper">
    
        <select id="boardCount" resultType="int">
           SELECT COUNT(*)
             FROM BOARD_TEST
       </select>
       
       <select id="boardList" resultType="com.example.board.domain.BoardVO">
           SELECT *
             FROM BOARD_TEST
       </select>
       
       <select id="boardDetail" parameterType="int" resultType="com.example.board.domain.BoardVO">
           SELECT *
             FROM BOARD_TEST
            WHERE BNO = #{bno}
       </select>
       
       <insert id="boardInsert" parameterType="com.example.board.domain.BoardVO">
           INSERT INTO BOARD_TEST (BNO, SUBJECT, CONTENT, WRITER, REG_DATE)
           	 VALUES(#{bno}, #{subject}, #{content}, #{writer}, SYSDATE) 
       </insert>
       
       <update id="boardUpdate" parameterType="com.example.board.domain.BoardVO">
           UPDATE BOARD_TEST
              SET
           <if test="subject != null">
               SUBJECT = #{subject}
           </if>
           <if test="subject != null and content != null"> , </if>
           <if test="content != null">
               CONTENT = #{content}
           </if>
           WHERE BNO = #{bno}
       </update>
       
       <delete id="boardDelete" parameterType="int">
           DELETE FROM BOARD_TEST WHERE BNO = #{bno}
       </delete>
   
   </mapper>
   ```
   



4. BoardApplication에 @MapperScan 추가

   * **@MapperScan** : Mapper 인터페이스를 인식할 수 있도록 설정

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



5. JSP를 테스트 했던 클래스에 DB 연동이 잘 되었는지 테스트

   ```java
   package com.example.board;
   
   import javax.annotation.Resource;
   
   import org.springframework.stereotype.Controller;
   import org.springframework.web.bind.annotation.RequestMapping;
   
   import com.example.board.mapper.BoardMapper;
   
   
   /*
    java.sql.SQLException: 지원되지 않는 문자 집합(클래스 경로에 orai18n.jar 추가): KO16MSWIN949 
    https://eknote.tistory.com/2243
   */
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

   localhost:8080/test -> 테스트 성공

![image-20210729152405717](C:\Users\user\AppData\Roaming\Typora\typora-user-images\image-20210729152405717.png)



