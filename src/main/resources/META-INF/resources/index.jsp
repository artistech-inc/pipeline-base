<!--
 * Copyright 2017 ArtisTech, Inc.
-->
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <jsp:useBean scope="request" class="com.artistech.ee.beans.DataManager" id="dataBean" type="com.artistech.ee.beans.DataManager">
        <jsp:setProperty name="dataBean" property="*" />
    </jsp:useBean>
    <jsp:useBean scope="request" class="com.artistech.ee.beans.PipelineBean" id="pipelineBean" type="com.artistech.ee.beans.PipelineBean">
        <jsp:setProperty name="pipelineBean" property="*" />
    </jsp:useBean>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title><c:out value="${pipelineBean.name}" /></title>
        <link rel='stylesheet' href='style.css' type='text/css'>
        <script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
        <script type="text/javascript">
            function guid() {
                return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                    var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
                    return v.toString(16);
                });
            }
            function onload() {
                $("#pipeline_link").attr('href', 'pipeline.jsp?pipeline_id=' + guid());
                console.log($("#pipeline_link").attr('href'));
            }
        </script>
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">
    </head>
    <body onload="onload()">
        <h1><c:out value="${pipelineBean.name}" /></h1>
        Web app for processing through the green pipeline involving joint_ere_release and ENIE.
        <div>
            <h2>View Data</h2>
            <a href="viewAll.jsp">View Run Data</a>
        </div>
        <div>
            <h2>Run Data</h2>
            <a id="pipeline_link" href="pipeline.jsp">Run Data</a>
        </div>
    </body>
</html>