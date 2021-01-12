package makeBoard;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class BoardDAO {
	private Connection con;
	private PreparedStatement pstmt;

	public BoardDAO() {}

	public List selectAllArticles(Map pagingMap){
		
		List articlesList = new ArrayList();
		int section = (Integer)pagingMap.get("section");
		int pageNum=(Integer)pagingMap.get("pageNum");
		try{
		   Connection con = connDB();
		   String query ="SELECT * FROM ( "
					+ "select ROWNUM  as recNum,"+"LVL,"
						+"articleNO,"
						+"parentNO,"
						+"title,"
						+"id,"
						+"writeDate"
			                  +" from (select LEVEL as LVL, "
							+"articleNO,"
							+"parentNO,"
							+"title,"
							+"id,"
							 +"writeDate"
						   +" from t_board" 
						   +" START WITH  parentNO=0"
						   +" CONNECT BY PRIOR articleNO = parentNO"
						  +"  ORDER SIBLINGS BY articleNO DESC)"
				+") "                        
				+" where recNum between(?-1)*100+(?-1)*10+1 and (?-1)*100+?*10";                       
		   System.out.println(query);
		   pstmt= con.prepareStatement(query);
		   pstmt.setInt(1, section);
		   pstmt.setInt(2, pageNum);
		   pstmt.setInt(3, section);
		   pstmt.setInt(4, pageNum);
		   ResultSet rs =pstmt.executeQuery();
		   while(rs.next()){
		      int level = rs.getInt("lvl");
		      int articleNO = rs.getInt("articleNO");
		      int parentNO = rs.getInt("parentNO");
		      String title = rs.getString("title");
		      String id = rs.getString("id");
		      Date writeDate= rs.getDate("writeDate");
		      ArticleVO article = new ArticleVO();
		      article.setLevel(level);
		      article.setArticleNO(articleNO);
		      article.setParentNO(parentNO);
		      article.setTitle(title);
		      article.setId(id);
		      article.setWriteDate(writeDate);
		      articlesList.add(article);	
		   } //end while
		   rs.close();
		   pstmt.close();
		   con.close();
	  }catch(Exception e){
	     e.printStackTrace();	
	  }
	  return articlesList;
    } 
	
	
	public List selectAllArticles() {
		List articlesList = new ArrayList();
		try {
			Connection con = connDB();
			String query = "SELECT LEVEL,articleNO,parentNO,title,content,id,writeDate" + " from t_board"
					+ " START WITH  parentNO=0" + " CONNECT BY PRIOR articleNO=parentNO"
					+ " ORDER SIBLINGS BY articleNO DESC";
			System.out.println(query);
			pstmt = con.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				int level = rs.getInt("level");
				int articleNO = rs.getInt("articleNO");
				int parentNO = rs.getInt("parentNO");
				String title = rs.getString("title");
				String content = rs.getString("content");
				String id = rs.getString("id");
				Date writeDate = rs.getDate("writeDate");
				ArticleVO article = new ArticleVO();
				article.setLevel(level);
				article.setArticleNO(articleNO);
				article.setParentNO(parentNO);
				article.setTitle(title);
				article.setContent(content);
				article.setId(id);
				article.setWriteDate(writeDate);
				articlesList.add(article);
			}
			rs.close();
			pstmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return articlesList;
	}

	
	private int getNewArticleNO() {
		try {
			Connection con = connDB();
			String query = "SELECT  max(articleNO) from t_board ";
			System.out.println(query);
			pstmt = con.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				return (rs.getInt(1) +1);
			}
			rs.close();
			pstmt.close();
			con.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int insertNewArticle(ArticleVO article) {
		int articleNO = getNewArticleNO();
		try {
			Connection con = connDB();
			int parentNO = article.getParentNO();
			String title = article.getTitle();
			String content = article.getContent();
			String id = article.getId();
			String imageFileName =  article.getImageFileName();
			String query = "INSERT INTO t_board (articleNO, parentNO, title, content, imageFileName, id)"
					+ " VALUES (?, ? ,?, ?, ?, ?)";
			System.out.println(query);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, articleNO);
			pstmt.setInt(2, parentNO);
			pstmt.setString(3, title);
			pstmt.setString(4, content);
			pstmt.setString(5, imageFileName);
			pstmt.setString(6, id);
			pstmt.executeUpdate();
			pstmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return articleNO;
	}
	
	public ArticleVO selectArticle(int articleNO){
		ArticleVO article=new ArticleVO();
		try{
		Connection con = connDB();
		String query = "select articleNO,parentNO,title,content, imageFileName,id,writeDate" + " from t_board"
				+ " where articleNO=?";
		System.out.println(query);
		pstmt = con.prepareStatement(query);
		pstmt.setInt(1, articleNO);
		ResultSet rs =pstmt.executeQuery();
		rs.next();
		int _articleNO =rs.getInt("articleNO");
		int parentNO=rs.getInt("parentNO");
		String title = rs.getString("title");
		String content =rs.getString("content");
	    String imageFileName = rs.getString("imageFileName"); 
		String id = rs.getString("id");
		Date writeDate = rs.getDate("writeDate");

		article.setArticleNO(_articleNO);
		article.setParentNO (parentNO);
		article.setTitle(title);
		article.setContent(content);
		article.setImageFileName(imageFileName);
		article.setId(id);
		article.setWriteDate(writeDate);
		rs.close();
		pstmt.close();
		con.close();
		}catch(Exception e){
		e.printStackTrace();	
		}
		return article;
		}
	
	
	public void updateArticle(ArticleVO article) {
		int articleNO = article.getArticleNO();
		String title = article.getTitle();
		String content = article.getContent();
		String imageFileName = article.getImageFileName();
		try {
			Connection con = connDB();
			String query = "update t_board  set title=?,content=?";
			if (imageFileName != null && imageFileName.length() != 0) {
				query += ",imageFileName=?";
			}
			query += " where articleNO=?";
			
			System.out.println(query);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, title);
			pstmt.setString(2, content);
			if (imageFileName != null && imageFileName.length() != 0) {
				pstmt.setString(3, imageFileName);
				pstmt.setInt(4, articleNO);
			} else {
				pstmt.setInt(3, articleNO);
			}
			pstmt.executeUpdate();
			pstmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void deleteArticle(int  articleNO) {
		try {
			Connection con = connDB();
			String query = "DELETE FROM t_board ";
			query += " WHERE articleNO in (";
			query += "  SELECT articleNO FROM  t_board ";
			query += " START WITH articleNO = ?";
			query += " CONNECT BY PRIOR  articleNO = parentNO )";
			System.out.println(query);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, articleNO);
			pstmt.executeUpdate();
			pstmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Integer> selectRemovedArticles(int  articleNO) {
		List<Integer> articleNOList = new ArrayList<Integer>();
		try {
			Connection con = connDB();
			String query = "SELECT articleNO FROM  t_board  ";
			query += " START WITH articleNO = ?";
			query += " CONNECT BY PRIOR  articleNO = parentNO";
			System.out.println(query);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, articleNO);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				articleNO = rs.getInt("articleNO");
				articleNOList.add(articleNO);
			}
			pstmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return articleNOList;
	}
	
	public int selectTotArticles() {
		try {
			Connection con =connDB();
			String query = "select count(articleNO) from t_board ";
			System.out.println(query);
			pstmt = con.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				return (rs.getInt(1));
			rs.close();
			pstmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	
	public Connection connDB() throws Exception{
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@________:ORCL", "Your_ID", "Your_password");			
			return con;
		}catch(Exception e) {
			throw e;
		}
	}
}
