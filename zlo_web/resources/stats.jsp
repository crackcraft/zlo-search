<%--
  User: Vovan
  Date: 20.01.2008
  Time: 4:08:47
--%>
<%@ include file="WEB-INF/jsp/import.jsp" %>
<%@ page contentType="text/html; charset=windows-1251" %>
<link rel="stylesheet" type="text/css" href="main.css" />

<jsp:useBean id="backendBean" class="info.xonix.zlo.web.BackendBean" scope="request" />
<jsp:setProperty name="backendBean" property="*" /> <%-- all from request properties --%>

<%@ include file="WEB-INF/jsp/setSite.jsp"%>
<sql:setDataSource dataSource="${site.dataSource}" />

<c:set var="byNick" value="${empty param['type'] or param['type'] == 'nick'}" />
<c:set var="period" value="${param['period'] == '2' ? 10 : param['period'] == '3' ? 30 : 2}" />

<c:choose>
    <c:when test="${byNick}">
        <sql:query var="res">
            select nick, reg, COUNT(*) cnt from messages
            where msgDate > NOW() - INTERVAL ? DAY
            group by nick
            order by cnt desc;
            <sql:param>${period}</sql:param>
        </sql:query>
    </c:when>
    <c:otherwise>
        <sql:query var="res">
            select host, COUNT(*) cnt from messages
            where msgDate > NOW() - INTERVAL ? DAY
            group by host
            order by cnt desc;
            <sql:param>${period}</sql:param>
        </sql:query>
    </c:otherwise>
</c:choose>

<sql:query var="resTotal">
    select COUNT(1) cnt from messages
    where msgDate > NOW() - INTERVAL ? DAY
    <sql:param>${period}</sql:param>
</sql:query>

<c:set var="title">
    ���������� ����� ${site.SITE_URL} �� <c:choose><c:when test="${byNick}">�����</c:when><c:otherwise>������</c:otherwise></c:choose>
    �� ��������� ${period} �����
</c:set>
<title>${title}</title>

<tiles:insertDefinition name="header.stats" />

<div align="center">
<h3>${title}</h3>

<form action="stats.jsp" method="get">
    ����: <jsp:getProperty name="backendBean" property="siteSelector" /><br/>
    ��:
    <input type="radio" name="type" value="nick" id="tn" <c:if test="${byNick}">checked="checked"</c:if> /><label for="tn">����</label>
    <input type="radio" name="type" value="host" id="th" <c:if test="${!byNick}">checked="checked"</c:if>/><label for="th">�����</label>
    �� ���������:
    <select name="period">
        <option value="1" <c:if test="${period == 2}">selected="selected"</c:if>>2-� �����</option>
        <option value="2" <c:if test="${period == 10}">selected="selected"</c:if>>10 �����</option>
        <option value="3" <c:if test="${period == 30}">selected="selected"</c:if>>30 �����</option>
    </select>
    <input type="submit" value="��������!" />
</form>
    <small>����� ��������� �� ���� ������: ${resTotal.rows[0].cnt}</small>
</div>

<table border="1" align="center">
    <tr><th>�</th><th><c:choose><c:when test="${byNick}">���</c:when><c:otherwise>����</c:otherwise></c:choose></th><th>����� ���������</th></tr>
    <% Integer i=0; %>
    <c:forEach var="row" items="${res.rows}">
        <% i++; %>
        <tr>
            <td><%= i %></td>
        <td><c:choose>
                <c:when test="${byNick}">
                    <tiles:insertDefinition name="nick">
                        <tiles:putAttribute name="reg" value="${row.reg}" />
                        <tiles:putAttribute name="nick" value="${row.nick}" />
                        <tiles:putAttribute name="site" value="${site}" />
                    </tiles:insertDefinition>
                </c:when>
                <c:otherwise>
                    <tiles:insertDefinition name="host">
                        <tiles:putAttribute name="host" value="${row.host}" />
                        <tiles:putAttribute name="site" value="${site}" />
                    </tiles:insertDefinition>
                </c:otherwise>
            </c:choose></td>
        <td>${row.cnt}</td>
        </tr>
    </c:forEach>
</table>

<tiles:insertDefinition name="ga" />
