<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>CHECK-공지사항 : '${keyword }' 검색결과</title>
<link rel="stylesheet" href="/resources/css/common.css">
<link rel="stylesheet" href="/resources/css/main/notice.css">
<link rel="stylesheet" as="style" crossorigin
	href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css" />
<script src="/resources/js/main/mobile.js"></script>
<script src="/resources/js/main/notice.js"></script>
</head>
<body>
	<div class="container flex">
		<c:set var="auth" value="${sessionScope.auth}" />
		<!-- 메인 사이드바 -->
		<c:choose>
			<c:when test="${auth.m_role eq 1 }">
				<jsp:include page="../common/side_main.jsp"></jsp:include>
			</c:when>
			<c:otherwise>
				<jsp:include page="../common/side_main_i.jsp"></jsp:include>
			</c:otherwise>
		</c:choose>
		<!-- 본문 -->
		<main class="contents bgf2f2f2">
			<div class="grid g20">
				<h3 class="f24">공지사항</h3>
				<!-- 서치바 -->
				<jsp:include page="./notice_searchbar.jsp"></jsp:include>
				<table class="tab notice mbe30">
					<tr>
						<td class="keyword"><b>'${keyword }'</b> <c:choose>
								<c:when test="${empty list }">
								에 대한 검색결과가 존재하지 않습니다.
								</c:when>
								<c:otherwise>
								에 대한 검색결과입니다.
								</c:otherwise>
							</c:choose></td>
					</tr>
					<c:forEach items="${list }" var="item">
						<tr class="item" data-id="${item.noticeId }">
							<td class="prefix">공지</td>
							<td class="title">${item.noticeTitle }</td>
							<td class="date">등록일: ${item.regdate }</td>
							<td class="toggle"></td>
						</tr>
					</c:forEach>
				</table>
				<ul class="pagination">
					<c:if test="${page ne 1 }">
						<a href="/notice/search?page=${page-1 }&keyword=${keyword}">
							<li class="page">이전</li>
						</a>
					</c:if>
					<c:forEach var="i" begin="1" end="${size }">
						<a href="/notice/search?page=${i}&keyword=${keyword}">
							<li class="page <c:if test="${i eq page }">selected</c:if>">${i}</li>
						</a>
					</c:forEach>
					<c:if test="${page ne size}">
						<a href="/notice/search?page=${page+1 }&keyword=${keyword}">
							<li class="page">다음</li>
						</a>
					</c:if>
				</ul>
			</div>
		</main>
	</div>
</body>

</html>