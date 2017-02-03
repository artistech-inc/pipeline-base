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
        <link rel='stylesheet' type='text/css' href='style.css'>
        <link rel='stylesheet' type='text/css' href='css/google-code-prettify/prettify.css'>
        <link rel='stylesheet' type='text/css' href='css/layout.css'>
        <link rel='stylesheet' type='text/css' href='css/index.css'>

        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">

        <script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
        <script type="text/javascript" src="js/json2html.js"></script>
        <script type="text/javascript" src="js/jquery.json2html.js"></script>
        <script type="text/javascript" src="js/json-viz.js"></script>
    </head>
    <body>
        <c:if test="${not empty param.pipeline_id}">
            <h1>View All Data</h1>
            <c:forEach var="dataDir" items="${dataBean.getData(param.pipeline_id).runKeys}">
                <ul><kbd>${dataDir}</kbd> Files:
                    <c:forEach var="dataFile" items="${dataBean.getData(param.pipeline_id).getFiles(dataDir)}">
                        <li><a target="_blank" href="ViewRaw?stage=${dataDir}&pipeline_id=${param.pipeline_id}&file=${dataFile}">${dataFile}</a></li>
                        </c:forEach>
                </ul>
            </c:forEach>
            <c:forEach var="dataDir" items="${dataBean.getData(param.pipeline_id).keys}">
                <c:if test="${fn:length(dataBean.getData(param.pipeline_id).getFiles(dataDir)) gt 0}">
                    <ul><kbd>${dataDir}</kbd> Files:
                        <c:forEach var="dataFile" items="${dataBean.getData(param.pipeline_id).getFiles(dataDir)}">
                            <li><a target="_blank" href="ViewRaw?stage=${dataDir}&pipeline_id=${param.pipeline_id}&file=${dataFile}">${dataFile}</a></li>
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
                        <li><a href="viewAll.jsp?pipeline_id=${id.file.name}">${id.file.name}</a> (${id.date})
                            <div id="${id.file.name}"></div>
                            <script>
                                visualize(${id.config}, "${id.file.name}");
                            </script>
                        </li>
                    </c:forEach>
                </ul>
            </c:if>
        </c:if>
    </body>
</html>