# 게시판 CRUD (SprinBoot) - 3. CRUD



### CRUD

- [참고자료] https://private.tistory.com/54?category=753861

1. **BoardMapper.java**, **BoardMapper.xml**에 내용 추가

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

2. controller 패키지에 **BoardController.java** 생성 후 작성

```java
package com.example.board.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.board.domain.BoardVO;
import com.example.board.mapper.BoardMapper;
import com.example.board.service.BoardService;

@Controller
public class BoardController {
    
	@Resource(name="com.example.board.service.BoardService")
	BoardService mBoardService;
	
    @RequestMapping("/list") //게시판 리스트 화면 호출  
    private String boardList(Model model) throws Exception{
        
        model.addAttribute("list", mBoardService.boardListService());
        
        return "list"; //생성할 jsp
    }
    
    @RequestMapping("/detail/{bno}") 
    private String boardDetail(@PathVariable int bno, Model model) throws Exception{
        
        model.addAttribute("detail", mBoardService.boardDetailService(bno));
        
        return "detail";
    }
    
    @RequestMapping("/insert") //게시글 작성폼 호출  
    private String boardInsertForm(){
        
        return "insert";
    }
    
    @RequestMapping("/insertProc")
    private String boardInsertProc(HttpServletRequest request) throws Exception{
        
    	BoardVO board = new BoardVO();
        board.setSubject(request.getParameter("subject"));
        board.setContent(request.getParameter("content"));
        board.setWriter(request.getParameter("writer"));
        
        mBoardService.boardInsertService(board);
        
        return "redirect:/list";

    }
    
    @RequestMapping("/update/{bno}") //게시글 수정폼 호출  
    private String boardUpdateForm(@PathVariable int bno, Model model) throws Exception{
        
        model.addAttribute("detail", mBoardService.boardDetailService(bno));
        
        return "update";
    }
    
    @RequestMapping("/updateProc")
    private String boardUpdateProc(HttpServletRequest request) throws Exception{
    	BoardVO board = new BoardVO();
        board.setSubject(request.getParameter("subject"));
        board.setContent(request.getParameter("content"));

        mBoardService.boardUpdateService(board);
        
        return "redirect:/detail/"+request.getParameter("bno");
    }
 
    @RequestMapping("/delete/{bno}")
    private String boardDelete(@PathVariable int bno) throws Exception{
        
        mBoardService.boardDeleteService(bno);
        
        return "redirect:/list";
    }

}
```



3. service 패키지에 **BoardService.java** 작성

```java
package com.example.board.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.example.board.domain.BoardVO;
import com.example.board.mapper.BoardMapper;

@Service("com.example.board.service.BoardService")
public class BoardService {
 
    @Resource(name="com.example.board.mapper.BoardMapper")
    BoardMapper mBoardMapper;
    
    public List<BoardVO> boardListService() throws Exception{
        
        return mBoardMapper.boardList();
    }
    
    public BoardVO boardDetailService(int bno) throws Exception{
        
        return mBoardMapper.boardDetail(bno);
    }
    
    public int boardInsertService(BoardVO board) throws Exception{
        
        return mBoardMapper.boardInsert(board);
    }
    
    public int boardUpdateService(BoardVO board) throws Exception{
        
        return mBoardMapper.boardUpdate(board);
    }
    
    public int boardDeleteService(int bno) throws Exception{
        
        return mBoardMapper.boardDelete(bno);
    }


}
```



4. webapp - WEB-INF - views에 jsp 파일 생성 후 작성

   **bootstrap.jsp**

   ```jsp
   <%@ page language="java" contentType="text/html; charset=UTF-8"
       pageEncoding="UTF-8"%>
   <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
   <html>
   <head>
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
   <!-- jQuery -->
   <script src="//code.jquery.com/jquery.min.js"></script>
   <!-- 합쳐지고 최소화된 최신 CSS -->
   <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
   <!-- 부가적인 테마 -->
   <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
   <!-- 합쳐지고 최소화된 최신 자바스크립트 -->
   <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
   
   ```

   **list.jsp**

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>List</title>
</head>
<body>
 
<h2> 게시글 목록 </h2>
 
<button class="btn btn-primary" onclick="location.href='/insert'">글쓰기</button>
 
<div class="container">
    <table class="table table-hover">
        <tr>
            <th>No</th>
            <th>Subject</th>
            <th>Writer</th>
            <th>Date</th>
        </tr>
          <c:forEach var="l" items="${list}">
              <tr onclick="location.href='/detail/${l.bno}'"> <!-- 이 부분 수정! -->
                  <td>${l.bno}</td>
                  <td>${l.subject}</td>
                  <td>${l.writer}</td>
                  <td>${l.reg_date}</td>
              </tr>
          </c:forEach>
          
    </table>
</div>
 
 
<%@ include file="bootstrap.jsp" %>
</body>
</html>
```

**insert.jsp**

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
 
<h2> 게시글 작성 </h2>
 
<div class="container">
    <form action="/insertProc" method="post">
      <div class="form-group">
        <label for="subject">제목</label>
        <input type="text" class="form-control" id="subject" name="subject" placeholder="제목을 입력하세요.">
      </div>
      <div class="form-group">
        <label for="writer">작성자</label>
        <input type="text" class="form-control" id="writer" name="writer" placeholder="내용을 입력하세요.">
      </div>
      <div class="form-group">
        <label for=content">내용</label>
        <textarea class="form-control" id="content" name="content" rows="3"></textarea>
      </div>
      <button type="button" class="btn btn-primary" onClick="location.href='/list'">리스트</button>
      <button type="submit" class="btn btn-primary">작성</button>
    </form>
</div>
<%@ include file="bootstrap.jsp" %>
</body>


</html>
```

**update.jsp**

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
 
<h2> 게시글 수정 </h2>
 
<div class="container">
    <form action="/updateProc" method="post">
      <div class="form-group">
        <label for="subject">제목</label>
        <input type="text" class="form-control" id="subject" name="subject" value="${detail.subject}">
      </div>
      <div class="form-group">
        <label for="content">내용</label>
        <textarea class="form-control" id="content" name="content" rows="3">${detail.content}</textarea>
      </div>
      <input type="hidden" name="bno" value="${bno}"/>
      <button type="button" class="btn btn-primary" onClick="location.href='/list'">리스트</button>
      <button type="submit" class="btn btn-primary">수정</button>
    </form>
</div>
 
<%@ include file="bootstrap.jsp" %>
</body>
</html>

```

**detail.jsp**

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Detail</title>
</head>
<body>
 
 
<h2> 게시글 상세 </h2>
 
<button class="btn btn-primary" onclick="location.href='/update/${detail.bno}'">수정</button> <!-- 추가 -->
<button class="btn btn-danger" onclick="location.href='/delete/${detail.bno}'">삭제</button>  <!-- 추가 -->
 
<div class="container">
    <form action="/insertProc" method="post">
      <div class="form-group">
        <label>제목</label>
        <p>${detail.subject}</p>
      </div>
      <div class="form-group">
        <label>작성자</label>
        <p>${detail.writer}</p>
      </div>
      <div class="form-group">
        <label>작성날짜</label>
        <p>${detail.reg_date}</p>
      </div>
      <div class="form-group">
        <label>내용</label>
        <p>${detail.content}</p>
      </div>
      <button type="button" class="btn btn-primary" onClick="location.href='/list'">리스트</button>
    </form>
</div>
 
 
<%@ include file="bootstrap.jsp" %>
</body>
</html>
```

