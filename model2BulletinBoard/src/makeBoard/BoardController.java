package makeBoard;

import java.io.File;	
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

@WebServlet("/board/*")
public class BoardController extends HttpServlet {
	
	// 글에 첨부한 이미지 저장 위치를 상수로 선언 
	private static String ARTICLE_IMAGE_REPO = "/home/jinbaek/workspace/article_image";
	
	private static final long serialVersionUID = 1L;
	
	BoardService boardService;
	ArticleVO articleVO;

	public void init(ServletConfig config) throws ServletException {
		boardService = new BoardService();
		articleVO = new ArticleVO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		doHandle(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		doHandle(request, response);
	}

	private void doHandle(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		String nextPage = "";
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		HttpSession session;
		String action = request.getPathInfo();
		//System.out.println("action:" + action);
		
		try {
			List<ArticleVO> articlesList = new ArrayList<ArticleVO>();
			if (action==null){	
				String _section=request.getParameter("section");
				String _pageNum=request.getParameter("pageNum");
				int section = Integer.parseInt(((_section==null)? "1":_section) );
				int pageNum = Integer.parseInt(((_pageNum==null)? "1":_pageNum));
				Map<String, Integer> pagingMap = new HashMap<String, Integer>();
				pagingMap.put("section", section);
				pagingMap.put("pageNum", pageNum);
				Map articlesMap=boardService.listArticles(pagingMap);
				articlesMap.put("section", section);
				articlesMap.put("pageNum", pageNum);
				request.setAttribute("articlesMap", articlesMap);
				nextPage = "/listArticles.jsp";
			
			} else if(action.equals("/listArticles.do")){  			
				String _section=request.getParameter("section");
				String _pageNum=request.getParameter("pageNum");
				int section = Integer.parseInt(((_section==null)? "1":_section) );
				int pageNum = Integer.parseInt(((_pageNum==null)? "1":_pageNum));
				Map pagingMap=new HashMap();
				pagingMap.put("section", section);
				pagingMap.put("pageNum", pageNum);
				Map articlesMap=boardService.listArticles(pagingMap);
				articlesMap.put("section", section);
				articlesMap.put("pageNum", pageNum);
				request.setAttribute("articlesMap", articlesMap);
				nextPage = "/listArticles.jsp";
			
			} else if(action.equals("/articleForm.do")) {
				nextPage = "/articleForm.jsp";
			
			}else if(action.equals("/addArticle.do")) {
				int articleNO = 0 ;
				
				Map<String,String> articleMap = upload(request,response);
				String title = articleMap.get("title");
				String content = articleMap.get("content");
				String imageFileName = articleMap.get("imageFileName");
				
				articleVO.setParentNO(0);
				articleVO.setId("hong");
				articleVO.setTitle(title);
				articleVO.setContent(content);
				articleVO.setImageFileName(imageFileName);
				
				// 테이블에 새 글을 추가한 후 새 글에 대한 글 번호를 가져온다 
				articleNO = boardService.addArticle(articleVO);
				
				// 파일을 첨부한 경우에만 수행 
				if(imageFileName != null && imageFileName.length() != 0) {
					
					// temp 폴더에 임시로 업로드된 파일 객체를 생성 
					File srcFile = new File(ARTICLE_IMAGE_REPO+"/"+"temp"+"/"+imageFileName);
					File destDir = new File(ARTICLE_IMAGE_REPO+"/"+articleNO);
					destDir.mkdirs(); // 리눅스 명령어와 동일 -  폴더 생성 
					
					// temp 폴더의 파일을 글 번호를 이름으로 하는 폴더로 이동시킨다 
					FileUtils.moveFileToDirectory(srcFile, destDir, true);
				}
				PrintWriter pw = response.getWriter();
				pw.print("<script>" 
				         +"  alert('새글을 추가했습니다.');" 
						 +" location.href='"+request.getContextPath()+"/board/listArticles.do';"
				         +"</script>");
				return;
				
			}else if(action.equals("/viewArticle.do")) {
				String articleNO = request.getParameter("articleNO");
				// articleNO에 대한 글 정보를 조회하고 article 속성으로 바인딩한다. 
				articleVO = boardService.viewArticle(Integer.parseInt(articleNO));
				request.setAttribute("article",articleVO );
				nextPage = "/viewArticle.jsp";
			
			}else if (action.equals("/modArticle.do")) {
				Map<String, String> articleMap = upload(request, response);
				int articleNO = Integer.parseInt(articleMap.get("articleNO"));
				articleVO.setArticleNO(articleNO);
				String title = articleMap.get("title");
				String content = articleMap.get("content");
				String imageFileName = articleMap.get("imageFileName");
				articleVO.setParentNO(0);
				articleVO.setId("hong");
				articleVO.setTitle(title);
				articleVO.setContent(content);
				articleVO.setImageFileName(imageFileName);
				boardService.modArticle(articleVO);
				if (imageFileName != null && imageFileName.length() != 0) {
					String originalFileName = articleMap.get("originalFileName");
					
					// 수정된 이미지 파일을 새로운 폴더로 이동
					File srcFile = new File(ARTICLE_IMAGE_REPO + "/" + "temp" + "/" + imageFileName);
					File destDir = new File(ARTICLE_IMAGE_REPO + "/" + articleNO);
					destDir.mkdirs();
					FileUtils.moveFileToDirectory(srcFile, destDir, true);
					;
					// 전송된 originalImagefileName을 이용해 기존의 파일을 삭제
					File oldFile = new File(ARTICLE_IMAGE_REPO + "/" + articleNO + "/" + originalFileName);
					oldFile.delete();
				}
				PrintWriter pw = response.getWriter();
				pw.print("<script>" + "  alert('글을 수정했습니다.');" + " location.href='" + request.getContextPath()
				+ "/board/viewArticle.do?articleNO=" + articleNO + "';" + "</script>");
				return;
			
			}else if(action.equals("/removeArticle.do")) {
				int articleNO = Integer.parseInt(request.getParameter("articleNO"));
				// articleNO 값에 대한 글을 삭제한 후 삭제된 부모글과 자식 글의 articleNO 목록을 가져온다 
				List<Integer> articleNOList = boardService.removeArticle(articleNO);
				
				// 삭제된 글들의 이미지 저장 폴더들을 삭제한다 
				for(int _articleNO : articleNOList) {
					File imgDir = new File(ARTICLE_IMAGE_REPO+"/"+_articleNO);
					if(imgDir.exists()) {
						FileUtils.deleteDirectory(imgDir);
					}
				}
				PrintWriter pw = response.getWriter();
				pw.print("<script>"+" alert('글을 삭제했습니다.');"+" location.href='"+request.getContextPath()+"/board/listArticles.do';"+"</script>");
				return ;
			
				// 답글창 요청시 부모 글 번호 parentNO 속성으로 세션에 저장
			}else if(action.equals("/replyForm.do")) {
				int parentNO = Integer.parseInt(request.getParameter("parentNO"));
				session = request.getSession();
				session.setAttribute("parentNO", parentNO);
				nextPage ="/replyForm.jsp";
				
				// 답글 전송 시 세션에 저장된 parentNO 가져와서 테이블에 추가 
			}else if (action.equals("/addReply.do")) {
				session = request.getSession();
				int parentNO = (Integer) session.getAttribute("parentNO");
				session.removeAttribute("parentNO");
				
				Map<String, String> articleMap = upload(request, response);
				String title = articleMap.get("title");
				String content = articleMap.get("content");
				String imageFileName = articleMap.get("imageFileName");
				articleVO.setParentNO(parentNO);
				articleVO.setId("lee");
				articleVO.setTitle(title);
				articleVO.setContent(content);
				articleVO.setImageFileName(imageFileName);
				int articleNO = boardService.addReply(articleVO); // 답글 테이블에 추가 
				
				// 답글에 첨부한 이미즈를 temp 폴더에서 답글 번호 폴더로 이동
				if (imageFileName != null && imageFileName.length() != 0) {
					File srcFile = new File(ARTICLE_IMAGE_REPO + "/" + "temp" + "/" + imageFileName);
					File destDir = new File(ARTICLE_IMAGE_REPO + "/" + articleNO);
					destDir.mkdirs();
					FileUtils.moveFileToDirectory(srcFile, destDir, true);
				}
				PrintWriter pw = response.getWriter();
				pw.print("<script>" + "  alert('답글을 추가했습니다.');" + " location.href='" + request.getContextPath()
						+ "/board/viewArticle.do?articleNO="+articleNO+"';" + "</script>");
				return;
				
			}else {
				nextPage = "/listArticles.jsp";
			}
			
			RequestDispatcher dispatch = request.getRequestDispatcher(nextPage);
			dispatch.forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//글쓰기 창에서 전송된 글 관련 정보를 Map에 key,value 쌍으로 저장 
	// 추가로 파일 첨부를 한 경우 파일 이름을 Map에 저장 한 후 첨부한 파일을 저장소에 업로드! 
	private Map<String,String> upload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		Map<String,String> articleMap = new HashMap<String,String>();
		
		String encoding = "utf-8";
		
		// 글 이미지 저장 폴더에 대해 파일 객체를 생성한다 
		File currentDirPath = new File(ARTICLE_IMAGE_REPO);
		
		//  파일 업로드 API 
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setRepository(currentDirPath); // 파일을 저장할 디렉터리를 설정 
		factory.setSizeThreshold(1024 * 1024); // 최대 업로드 가능한 파일 크기를 설정 
		
		// 파일 업로드 API 
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List items =  upload.parseRequest(request); // 전송된 매개변수를 List 객체로 얻는다 
			for(int i=0;i<items.size();i++) {
				FileItem fileItem = (FileItem) items.get(i);
				if(fileItem.isFormField()) { // FileItem 객체에 저장되어 있는 값이 파일데이터면 false, 일반데이터면 true를 반환한다. 
					System.out.println(fileItem.getFieldName()+"="+fileItem.getString(encoding));
					
					// 파일 업로드로 같이 전송된 새 글 관련 매개변수를 Map에 (key,value) 로 저장한 후 반환하고, 새 글과 관련된 title,content를 Map에 저장한다. 
					// getFieldName : input태그의 name 속성의 값을 반환 , getString : 파일 아이템 내용을 문자열로 반환 
					articleMap.put(fileItem.getFieldName(),fileItem.getString(encoding));
				}else {
					System.out.println("파라미터 이름:" + fileItem.getFieldName());
					System.out.println("파일 이름:" + fileItem.getName());
					System.out.println("파일 크기:" + fileItem.getSize()+"bytes");
					if(fileItem.getSize()>0) {
						int idx = fileItem.getFieldName().lastIndexOf("\\");
						if(idx== -1) {
							idx = fileItem.getName().lastIndexOf("/");
						}
						String fileName = fileItem.getName().substring(idx+1);
						
						// 익스 플로러에서 업로드 파일의 경로 제거 후 map에 파일명 저장 
						// 업로드된 파일의 파일 이름을 Map에 ("imageFileName" , "업로그파일이름") 로 저장한다. 
						articleMap.put(fileItem.getFieldName(), fileName);
						
						File uploadFile = new File(currentDirPath + "/temp/" + fileName);
						fileItem.write(uploadFile); // 업로드된 데이터를 인수로 지정된 파일에 쓴다.
					}//end if
				}//end if
			}//end for
		}catch(Exception e) {
			e.printStackTrace();
		}
		return articleMap;
	}
}