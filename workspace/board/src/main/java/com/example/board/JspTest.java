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
