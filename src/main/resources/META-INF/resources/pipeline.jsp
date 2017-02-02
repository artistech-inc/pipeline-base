<!--
 * Copyright 2017 ArtisTech, Inc.
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <jsp:useBean scope="request" class="com.artistech.ee.beans.PipelineBean" id="pipelineBean" type="com.artistech.ee.beans.PipelineBean">
        <jsp:setProperty name="pipelineBean" property="*" />
    </jsp:useBean>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel='stylesheet' href='style.css' type='text/css'>
        <title>${pipelineBean.name}</title>
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">
        <style type="text/css">
            .fieldset-auto-width {
                display: inline-block;
            }
        </style>
        <!-- esprima required only for !!js/function -->
        <script type="text/javascript" src="js/esprima.js"></script>
        <script type="text/javascript" src="js/js-yaml.min.js"></script>
        <script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
        <script type="text/javascript">
            var yaml_config;
            var current_parts = [];
            var pipeline_id = '${param.pipeline_id}';

            Array.prototype.diff = function (a) {
                return this.filter(function (i) {
                    return a.indexOf(i) < 0;
                });
            };

            function onStepChange() {
                var currStep = $('#step').find(":selected").text();
                var parts_keys = Object.keys(yaml_config["parts"]);
                for (var ii = 0; ii < parts_keys.length; ii++) {
                    $('#' + parts_keys[ii] + '__div').hide();
                }
                $('#' + currStep + '__div').show();
            }

            function init() {
                console.log('init()');
                var formData = new FormData();

                $.ajax({
                    url: 'YamlConfig',
                    type: 'GET',
                    data: formData,
                    processData: false, // tell jQuery not to process the data
                    contentType: false, // tell jQuery not to set contentType
                    success: function (data) {
                        yaml_callback(data);
                    }
                });
            }

            function yaml_callback(data) {
                yaml_config = jsyaml.load(data);
                update_display();
            }

            function addStep(id)
            {
                console.log("submitting: " + id);
                var oFormElement = document.getElementById(id);
                var formData = new FormData(oFormElement);
                console.log(formData);
                $("form#" + id + " :input").each(function () {
                    var input = $(this);
                    if (input[0].type !== 'file') {
                        formData.append(input[0].id, input[0].value);
                    } else {
                        for (var jj = 0; jj < input[0].files.length; jj++) {
                            formData.append(input[0].id, input[0].files[jj]);
                        }
                    }
                });

                $.ajax({
                    url: 'PathBuild',
                    type: 'POST',
                    data: formData,
                    processData: false, // tell jQuery not to process the data
                    contentType: false, // tell jQuery not to set contentType
                    success: function (data) {
                        submit_callback(data);
                    }
                });

                return false;
            }

            function submit_callback(data) {
                current_parts = JSON.parse(data);
                update_display();
            }

            function update_display() {
                //use current_parts and yaml_config to do something...
                //build HTML/forms dynamically
                console.log("update_display()");
                var configured_tag = $("#configured_div");
                configured_tag.empty();

                /**
                 * build the current path....
                 */
                current_parts.forEach(function (part) {
                    var fs = $('<fieldset></fieldset>').addClass("fieldset-auto-width");
                    var leg = $('<legend>' + part["name"] + '</legend>');
                    leg.appendTo(fs);
                    fs.appendTo(configured_tag);

                    $('<br>').appendTo(configured_tag);
                    var parameters = part["parameters"];
                    var ul = $('<ul></ul>');
                    if (parameters.length > 0) {
                        parameters.forEach(function (param) {
                            $('<li>' + param["name"] + ': ' + param["value"] + '</li>').appendTo(ul);
                        });
                    } else {
                        $('<li>No Parameters</li>').appendTo(ul);
                    }
                    ul.appendTo(fs);
                });

                /**
                 * Build the parameter forms.
                 */
                var parent_tag = $('#part_config_div');
                parent_tag.empty();
                var select = $('<select id="step" onchange="onStepChange();"></select>');
                var specified = [];
                current_parts.forEach(function (part) {
                    specified.push(part.name);
                });
                select.appendTo(parent_tag);

                var parent_tag2 = $('#part_config_div2');
                parent_tag2.empty();

                var parts_keys = Object.keys(yaml_config["parts"]);
                var files = [];
                parts_keys.forEach(function (elem) {
                    var step_div = $('<div id="' + elem + '__div" style="display: none; border-width: 0; border-style : solid; border-color : black"></div>');
                    var form = $('<form id="' + elem + '__form" action="PathBuild" method="POST" enctype="multipart/form-data"></form>');
                    var parameters = yaml_config["parts"][elem]["parameters"];

                    if (parameters.length > 0) {
                        var ff2 = $('<fieldset></fieldset>').addClass("fieldset-auto-width");
                        var leg2 = $('<legend>' + elem + '</legend>');
                        leg2.appendTo(ff2);
                        ff2.appendTo(form);

                        parameters.forEach(function (p) {
                            var div3 = $('<div style="border-width: 0; border-style : solid; border-color : black"></div>');
                            div3.appendTo(ff2);
                            var param = p["parameter"];
                            if (param["type"] === 'file') {
                                var label = $('<label for="' + elem + '__' + param["name"] + '">' + param["name"] + '</label>');
                                var name = elem + '__' + param["name"];
                                files.push(name);
                                var f = buildFile(name);
                                label.appendTo(div3);
                                f.appendTo(div3);
                            } else if (param["type"] === 'select') {
                                var label = $('<label for="' + elem + '__' + param["name"] + '">' + param["name"] + '</label>');
                                var name = elem + '__' + param["name"];
                                var f = buildSelect(name, param["values"], param["value"]);
                                label.appendTo(div3);
                                f.appendTo(div3);
                            } else if (param["type"] === 'hidden') {
                                var name = elem + '__' + param["name"];
                                var f = buildHidden(name, param["value"]);
                                f.appendTo(div3);
                            }
                        });
                    }

                    form.append($('<br>'));
                    var id_input = buildHidden('pipeline_id', pipeline_id);
                    var step_input = buildHidden('step_name', elem);
                    var submit_input = $('<input type="button" value="Add Step" onclick="addStep(\'' + elem + '__form\');"></input>');

                    form.append(id_input);
                    form.append(step_input);
                    form.append(submit_input);
                    step_div.appendTo(parent_tag2);
                    form.appendTo(step_div);
                });

                /**
                 * Remove any parts that are currently unable to be performed
                 * due to un-met requirements.
                 */
                var can_do = [];
                parts_keys.forEach(function (elem) {
                    var part = yaml_config["parts"][elem];
                    var requires = part["requires"];
                    var sub = requires.diff(specified);
                    if (sub.length === 0) {
                        var multi = part["multi-run"];
                        if (typeof multi === 'undefined') {
                            multi = false;
                        }
                        if (multi || $.inArray(elem, specified) < 0) {
                            can_do.push(elem);
                        }
                    }
                });
                /**
                 * Show only those parts with satisfied requirements.
                 */
                can_do.forEach(function (elem) {
                    $('<option value="' + elem + '">' + elem + '</option>').appendTo(select);
                });

                /**
                 * Display the run form.
                 */
                if (current_parts.length > 1) {
                    $('#run_pipeline_div').show();
                    $('#run').attr('action', current_parts[1]["page"]);
                }

                /**
                 * This will hide/show appropriate part(s).
                 */
                onStepChange();
            }

            function buildSelect(id, values, value) {
                var select = $('<select id="' + id + '" name="' + id + '"></select>');
                select.id = id;
                values.forEach(function (val) {
                    var option = $('<option value="' + val + '">' + val + '</option>');
                    if (val === value) {
                        option.select();
                    }
                    select.append(option);
                });
                return select;
            }

            function buildHidden(id, value) {
                return $('<input id="' + id + '" name="' + id + '" type="hidden" value="' + value + '"></input>');
            }

            function buildFile(id) {
                return $('<input id="' + id + '" name="' + id + '" type="file" multiple="multiple"></input>');
            }
        </script>
    </head>
    <body onload="init()">
        <h1>${pipelineBean.name}</h1>
        ${pipelineBean.description}
        <h2>Configuration</h2>
        <div id="configured_div"></div>
        <div id="part_config_div"></div>
        <div id="part_config_div2"></div>
        <div id="run_pipeline_div" style="display: none">
            <h2>Run Configuration</h2>
            <form method="POST" action="" id="run" name="run" enctype="multipart/form-data">
                <input type="hidden" name="index" value="1" />
                <input type="hidden" name="pipeline_id" value="${param.pipeline_id}" />
                <input type="submit" value="${pipelineBean.name}" />
            </form>
        </div>
    </body>
</html>
