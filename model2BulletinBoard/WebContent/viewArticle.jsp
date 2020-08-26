<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    isELIgnored="false" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  request.setCharacterEncoding("UTF-8");
%> 
<c:set var="contextPath"  value="${pageContext.request.contextPath}"  />
<head>
   <meta charset="UTF-8">
   <title>글보기</title>
      <style>
     #tr_btn_modify{
       display:none;
     }
   </style>
   
   <script  src="http://code.jquery.com/jquery-latest.min.js"></script> 
   <script type="text/javascript">
      
   	   function backToList(obj){
	    obj.action="${contextPath}/board/listArticles.do";
	    obj.submit();
	  }
 		
		function fn_enable(obj){
			
		 <%-- 텍스트 박스의 id로 접근해서 disabled 속성을 false로 설정하면 수정이 가능해짐. --%>
		 document.getElementById("i_title").disabled=false;
 		 document.getElementById("i_content").disabled=false;
 		 document.getElementById("i_imageFileName").disabled=false;
 		 document.getElementById("tr_btn_modify").style.display="block";
 		 document.getElementById("tr_btn").style.display="none";
 		}
      
 	 function fn_modify_article(obj){
		 obj.action="${contextPath}/board/modArticle.do";
		 obj.submit();
	 }
	 
	 function fn_remove_article(url,articleNO){
		 var form = document.createElement("form");
		 form.setAttribute("method", "post");
		 form.setAttribute("action", url);
	     var articleNOInput = document.createElement("input");
	     articleNOInput.setAttribute("type","hidden");
	     articleNOInput.setAttribute("name","articleNO");
	     articleNOInput.setAttribute("value", articleNO);
		 
	     form.appendChild(articleNOInput);
	     document.body.appendChild(form);
		 form.submit();
	 
	 }
	 
	 function fn_reply_form(url, parentNO){
		 var form = document.createElement("form");
		 form.setAttribute("method", "post");
		 
		 <%--전달된 요청명을 <form> 태그의 action속성 값에 설정 --%>
		 form.setAttribute("action", url);
	     var parentNOInput = document.createElement("input");

	     <%--함수 호출시 전달된 articleNO 값을 <input>태그를 이용해서 컨트롤러에 전달 --%>
	     parentNOInput.setAttribute("type","hidden");
	     parentNOInput.setAttribute("name","parentNO");
	     parentNOInput.setAttribute("value", parentNO);
		 
	     form.appendChild(parentNOInput);
	     document.body.appendChild(form);
		 form.submit();
	 }
	 
	 
	 function readURL(input) {
	     if (input.files && input.files[0]) {
	         var reader = new FileReader();
	         reader.onload = function (e) {
	             $('#preview').attr('src', e.target.result);
	         }
	         reader.readAsDataURL(input.files[0]);
	     }
	 } 
    </script>
</head>
<body>
  <form name="frmArticle" method="post"  action="${contextPath}" enctype="multipart/form-data">
  <table  border="0" align="center" >
  <tr>
   <td width="150" align="center" bgcolor="#FF9933">
      글번호
   </td>
   <td >
    <input type="text"  value="${article.articleNO }"  disabled />
    <%-- 글 수정시 글 번호를 컨트롤러로 전송하기 위해 미리  히든 태그를 이용해 글 번호를 저장  --%>
    <input type="hidden" name="articleNO" value="${article.articleNO}"  />
   </td>
  </tr>
  <tr>
   <td width="150" align="center" bgcolor="#FF9933">
      작성자 아이디
   </td>
   <td >
    <input type="text" value="${article.id }" name="writer"  disabled />
   </td>
  </tr>
  <tr>
   <td width="150" align="center" bgcolor="#FF9933">
      글 제목 
   </td>
   <td>
    <input type="text" value="${article.title }"  name="title"  id="i_title" disabled />
   </td>   
  </tr>
  <tr>
   <td width="150" align="center" bgcolor="#FF9933">
      글 내용
   </td>
   <td>
   	<textarea rows="20" cols="60"  name="content"  id="i_content"  disabled />${article.content }</textarea>
   </td>  
  </tr>
 
<c:if test="${not empty article.imageFileName && article.imageFileName!='null' }">  
<tr>
   <td width="20%" align="center" bgcolor="#FF9933"  rowspan="2">
      이미지
   </td>
   <td> 
   <%-- 이미지 수정에 대비해 미리 원래 이미지 파일 이름을 히든 태그에 저장 --%> 
     <input  type= "hidden"   name="originalFileName" value="${article.imageFileName }" />
    <img src="${contextPath}/download.do?articleNO=${article.articleNO}&imageFileName=${article.imageFileName}" id="preview"  /><br>
       
   </td>   
  </tr>  
  <tr>
    <td>
       <input  type="file"  name="imageFileName " id="i_imageFileName"   disabled   onchange="readURL(this);"   />
    </td>
  </tr>
 </c:if>
  <tr>
	   <td width="20%" align="center" bgcolor="#FF9933">
	      등록일자
	   </td>
	   <td>
	    <input type=text value="<fmt:formatDate value="${article.writeDate}" />" disabled />
	   </td>   
  </tr>
  <tr   id="tr_btn_modify"  >
	   <td colspan="2"   align="center" >
	       <input type=button value="수정 반영하기"   onClick="fn_modify_article(frmArticle)"  >
           <input type=button value="취소"  onClick="backToList(frmArticle)">
	   </td>   
  </tr>
    
  <tr  id="tr_btn">
   <td colspan=2 align="center">
   
	  <input type=button value="수정하기" onClick="fn_enable(this.form)">
	  <input type=button value="삭제하기" onClick="fn_remove_article('${contextPath}/board/removeArticle.do', ${article.articleNO})">
	  <input type=button value="리스트로 돌아가기"  onClick="backToList(this.form)">
	  <input type=button value="답글쓰기"  onClick="fn_reply_form('${contextPath}/board/replyForm.do', ${article.articleNO})">
   </td>
  </tr>
 </table>
 </form>
</body>
</html>