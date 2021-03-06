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


