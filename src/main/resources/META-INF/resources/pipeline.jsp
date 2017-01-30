<!--
 * Copyright 2017 ArtisTech, Inc.
-->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <jsp:useBean scope="request" class="com.artistech.ee.beans.DataManager" id="dataBean" type="com.artistech.ee.beans.DataManager">
        <jsp:setProperty name="dataBean" property="*" />
    </jsp:useBean>
    <jsp:useBean scope="request" class="com.artistech.ee.beans.PipelineBean" id="pipelineBean" type="com.artistech.ee.beans.PipelineBean">
        <jsp:setProperty name="pipelineBean" property="*" />
    </jsp:useBean>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel='stylesheet' href='style.css' type='text/css'>
        <title><c:out value="${pipelineBean.name}" /></title>
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">
        <style type="text/css">
            .fieldset-auto-width {
                display: inline-block;
            }
        </style>
        <script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
        <script type="text/javascript">
            function onStepChange() {
                var currStep = $('#step').find(":selected").text();
                $('#step option').each(function ()
                {
                    $('#' + $(this).val() + '__div').hide();
                });
                $('#' + currStep + '__div').show();
            }
        </script>
    </head>
    <c:if test="${empty dataBean.data}">
        <c:set value="${[]}" var="specifed" />
    </c:if>
    <c:if test="${not empty dataBean.data}">
        <c:set value="${dataBean.data.currentPath}" var="specifed" />
    </c:if>

    <body onload="onStepChange()">
        <h1><c:out value="${pipelineBean.name}" /></h1>
        <c:out value="${pipelineBean.description}" />
        <h2>Configuration</h2>
        <c:if test="${not empty dataBean.data}">
            <c:forEach var="part" items="${dataBean.data.currentParts}">
                <div>
                    <fieldset class='fieldset-auto-width'>
                        <legend><c:out value="${part.name}" /></legend>
                        <ul>
                            <c:if test="${fn:length(part.parameters) eq 0}">
                                <li>No Parameters</li>
                                </c:if>
                                <c:forEach var="parameter" items="${part.parameters}">
                                <li><c:out value="${parameter.name}" />: <c:out value="${parameter.value}" /></li>
                                </c:forEach>
                        </ul>
                    </fieldset>
                </div>
            </c:forEach>
        </c:if>

        <select id="step" name="step" onchange="onStepChange()">
            <c:forEach var="step" items="${pipelineBean.getPartsAfter(specifed)}">
                <option value="${step.name}">${step.name}</option>
            </c:forEach>
        </select>

        <c:forEach var="step" items="${pipelineBean.getPartsAfter(specifed)}">
            <div id='<c:out value="${step.name}__div" />' style='display: none; border-width: 0; border-style : solid; border-color : black'>
                <form method="POST" action="PathBuild" enctype="multipart/form-data">
                    <c:if test="${fn:length(step.parameters) gt 0}">
                        <fieldset class='fieldset-auto-width'>
                            <legend><c:out value="${step.name}" /></legend>
                            <c:forEach var="parameter" items="${step.parameters}">
                                <div id='<c:out value="${step.name}__${parameter.name}__div" />' style='border-width: 0; border-style : solid; border-color : black'>
                                    <c:if test="${parameter.type == 'select'}">
                                        <label for="<c:out value="${step.name}__${parameter.name}" />"><c:out value="${parameter.name}" /></label>
                                        <select id="<c:out value="$${step.name}__${parameter.name}" />" name="<c:out value="${parameter.name}" />">
                                            <option value="${parameter.value}" selected>${parameter.value}</option>
                                            <c:forEach var="val" items="${parameter.values}">
                                                <c:if test="${val != parameter.value}">
                                                    <option value="${val}">${val}</option>
                                                </c:if>
                                            </c:forEach>
                                        </select>
                                    </c:if>
                                    <c:if test="${parameter.type == 'file'}">
                                        <label for="<c:out value="${step.name}__${parameter.name}" />"><c:out value="${parameter.name}" /></label>
                                        <input type="file" id="<c:out value="${step.name}__${parameter.name}" />" name="<c:out value="${step.name}__${parameter.name}" />"/>
                                    </c:if>
                                </div>
                            </c:forEach>
                        </fieldset>
                    </c:if>
                    <br />
                    <input type="hidden" name="pipeline_id" value="<c:out value="${param.pipeline_id}" />" />
                    <input type="hidden" name="step_name" value="<c:out value="${step.name}" />" />
                    <input type="submit" value="Add Step" />
                </form>
            </form>
        </div>
    </c:forEach>
    <c:if test="${not empty dataBean.data}">
        <!--This is assuming that currentParts(0) is the current page...-->
        <c:if test="${fn:length(dataBean.data.currentParts) gt 1}">
            <h2>Run Configuration</h2>
            <form method="POST" action="${dataBean.data.currentParts.get(1).page}" id="run" name="run" enctype="multipart/form-data">
                <input type="hidden" name="index" value="1" />
                <input type="hidden" name="pipeline_id" value="<c:out value="${param.pipeline_id}" />" />
                <input type="submit" value="<c:out value="${pipelineBean.name}" />" />
            </form>
        </c:if>
    </c:if>
</body>
</html>
