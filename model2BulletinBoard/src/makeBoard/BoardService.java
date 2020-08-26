package makeBoard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardService {
	BoardDAO boardDAO;
	public BoardService() {
		boardDAO = new BoardDAO();
	}
	
	public Map listArticles(Map paginMap) {
		
		Map articlesMap = new HashMap();
		
		// 전달 된 pagingMap을 사용해 글 목록 조회 
		List<ArticleVO> articlesList = boardDAO.selectAllArticles(paginMap);
		
		// 테이블에 존재하는 전체 글 수를 조회 
		int totArticles = boardDAO.selectTotArticles();
		
		// 조회된 글 목록을 ArrayList에 저장한 후 다시 articlesMap에 저장 
		articlesMap.put("articlesList",articlesList);
		
		// 전체 글 수를 articlesMap에 저장 
		articlesMap.put("totArticles",totArticles);
		
		return articlesMap;
	}


	public List<ArticleVO> listArticles(){
		
		List<ArticleVO> articlesList = boardDAO.selectAllArticles();
		return articlesList;
	}
	
	public int addArticle(ArticleVO article) {
		return boardDAO.insertNewArticle(article);
	}
	
	public ArticleVO viewArticle(int articleNO) {
		ArticleVO article = null;
		article = boardDAO.selectArticle(articleNO);
		return article;
	}
	
	public void modArticle(ArticleVO article) {
		boardDAO.updateArticle(article);
	}
	
	// selectRemovedArticles()로 글 번호에 대한 글과 그 자식 글의 글 번호를 가져온다.
	// deleteArticle() 로 글 번호에 대한 글과 자식 글을 삭제하고 글 번호를 반환한다.
	public List<Integer> removeArticle(int articleNO) {
		List<Integer> articleNOList = boardDAO.selectRemovedArticles(articleNO);
		boardDAO.deleteArticle(articleNO);
		return articleNOList;
	}
	
	public int addReply(ArticleVO article) {
		return boardDAO.insertNewArticle(article);
	}

}
