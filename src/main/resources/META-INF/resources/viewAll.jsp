<!--
 * Copyright 2017 ArtisTech, Inc.
-->
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <jsp:useBean scope="request" class="com.artistech.ee.beans.DataManager" id="dataBean" type="com.artistech.ee.beans.DataManager">
        <jsp:setProperty name="dataBean" property="*" />
    </jsp:useBean>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>View All Data</title>
        <link rel='stylesheet' href='style.css' type='text/css'>
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">
    </head>
    <body>
        <c:if test="${not empty param.pipeline_id}">
            <h1>View All Data</h1>
            <c:forEach var="dataDir" items="${dataBean.getData(param.pipeline_id).keys}">
                <c:if test="${fn:length(dataBean.getData(param.pipeline_id).getFiles(dataDir)) gt 0}">
                    <ul><kbd><c:out value="${dataDir}"/></kbd> Files:
                        <c:forEach var="dataFile" items="${dataBean.getData(param.pipeline_id).getFiles(dataDir)}">
                            <li><a target="_blank" href="ViewRaw?stage=<c:out value="${dataDir}"/>&pipeline_id=<c:out value="${param.pipeline_id}"/>&file=<c:out value="${dataFile}"/>"><c:out value="${dataFile}"/></a></li>
                            </c:forEach>
                    </ul>
                </c:if>
            </c:forEach>
        </c:if>
        <c:if test="${empty param.pipeline_id}">
            <h1>View All Data</h1>
            <c:if test="${fn:length(dataBean.storedData) gt 0}">
                <ul>Input Files:
                    <c:forEach var="id" items="${dataBean.storedData}">
                        <li><a href="viewAll.jsp?pipeline_id=<c:out value="${id}"/>"><c:out value="${id}"/></a></li>
                        </c:forEach>
                </ul>
            </c:if>
        </c:if>
    </body>
</html>