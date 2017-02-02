<!--
 * Copyright 2017 ArtisTech, Inc.
-->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <jsp:useBean scope="request" class="com.artistech.ee.beans.DataManager" id="dataBean" type="com.artistech.ee.beans.DataManager">
        <jsp:setProperty name="dataBean" property="*" />
    </jsp:useBean>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Watch Process</title>
        <link rel='stylesheet' href='style.css' type='text/css'>
        <script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
        <script type='text/javascript'>
            var pipeline_id = "${dataBean.pipeline_id}";
            var scroll_lock = false;
            var max_console = 500;
            var proc_alive = true;

            function printConsole(message) {
                var console = document.getElementById("console");
                console.value += message;
                var spl = console.value.split("\n");
                if (spl.length > max_console + 1) {
                    spl = spl.splice(spl.length - (max_console + 1));
                }
                console.value = spl.join("\n");
                if (!scroll_lock) {
                    console.scrollTop = console.scrollHeight;
                }
            }

            function clearConsole() {
                var console = document.getElementById("console");
                console.value = '';
                console.scrollTop = console.scrollHeight;
            }

            function init() {
                $('#hub_link').bind('click', function (e) {
                    e.preventDefault();
                });
                getProcessOutput();
                getProcessStatus();
            }

            function getProcessOutput() {
                var formData = new FormData();
                formData.append('pipeline_id', pipeline_id);
                $.ajax({
                    url: 'ProcessOutput',
                    type: 'POST',
                    data: formData,
                    processData: false, // tell jQuery not to process the data
                    contentType: false, // tell jQuery not to set contentType
                    success: function (data) {
                        proc_output_callback(data);
                    }
                });
            }

            function proc_output_callback(data) {
                printConsole(data);
                if (proc_alive) {
                    setTimeout("getProcessOutput()", 250);
                }
            }

            function getProcessStatus() {
                var formData = new FormData();
                formData.append('pipeline_id', pipeline_id);
                $.ajax({
                    url: 'ProcessMonitor',
                    type: 'POST',
                    data: formData,
                    processData: false, // tell jQuery not to process the data
                    contentType: false, // tell jQuery not to set contentType
                    success: function (data) {
                        proc_monitor_callback(data);
                    }
                });
            }

            function proc_monitor_callback(data) {
                if (data === "false") {
                    $('#hub_link').unbind('click');
                    proc_alive = false;
                    printConsole("\n\n*********************\nProcess Complete!\n*********************");
                    document.continue_form.submit();  //go to the next step!!
                } else {
                    setTimeout("getProcessStatus()", 250);
                }
            }

            function kill_proc() {
                console.log("Killing Process");
                var formData = new FormData();
                formData.append('pipeline_id', pipeline_id);
                $.ajax({
                    url: 'KillProcess',
                    type: 'POST',
                    data: formData,
                    processData: false, // tell jQuery not to process the data
                    contentType: false, // tell jQuery not to set contentType
                    success: function (data) {
                        console.log("Killed Process");
                    }
                });
            }
        </script>
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">
    </head>
    <body onload="init();">
        <h1>Watch Process: <c:out value="${dataBean.data.pipelineParts.get(dataBean.data.pipelineIndex-1).name}" /></h1>
        <br />
        <textarea id="console" rows="50" cols="85"></textarea>
        <br />
        <c:set value="${fn:length(dataBean.data.currentPath)}" var="specifed" />
        <c:if test="${dataBean.data.pipelineIndex lt specifed}">
            <form name="continue_form" id="confinue_form" method="POST" action="${dataBean.data.pipelineParts.get(dataBean.data.pipelineIndex).page}" enctype="multipart/form-data">
            </c:if>
            <c:if test="${dataBean.data.pipelineIndex eq specifed}">
                <form name="continue_form" id="confinue_form" method="POST" action="hub.jsp">
                </c:if>
                <input type="hidden" id="pipeline_id" name="pipeline_id" value="${dataBean.pipeline_id}" />
            </form>
    </body>
</html>