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
        <link rel='stylesheet' href='dropzone.css' type='text/css'>
        <title>${pipelineBean.name}</title>
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">
        <style type="text/css">
            .fieldset-auto-width {
                display: inline-block;
            }
            table.configured {
                width: 400px;
            }
        </style>
        <!-- esprima required only for !!js/function -->
        <script type="text/javascript" src="js/esprima.js"></script>
        <script type="text/javascript" src="js/js-yaml.min.js"></script>
        <script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
        <script type="text/javascript" src="js/dropzone.js"></script>
        <script type="text/javascript">
            var yaml_config;
            var current_parts = [];
            var pipeline_id = '${param.pipeline_id}';
            var use_dropzone = true;

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
                $("table").find("tbody>tr:odd").css("background-color","#E5E4E2");
                $("table").find("tbody>tr:even").css("background-color","#BCC6CC");
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
                update_display(pipeline_id);
            }

            function addStep(id)
            {
                console.log("submitting: " + id);
                var oFormElement = document.getElementById(id);
                var formData = new FormData(oFormElement);
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
                update_display(pipeline_id);
            }

            /**
             * Build the current path.
             * 
             * @returns {undefined}
             */
            function update_configured_path() {
                var configured_tag = $("#configured_div");
                configured_tag.empty();
                configured_tag.append($('<h2>Configured Path</h2>'));

                current_parts.forEach(function (part) {
                    var div = $('<div></div>');
                    var fs = $('<fieldset></fieldset>').addClass("fieldset-auto-width");
                    var leg = $('<legend>' + part["name"] + '</legend>');
                    leg.appendTo(fs);
                    configured_tag.append(div);

                    var table = $('<table class="configured"></table>');
                    $('<thead><th>Parameter</th><th>Value</th></thead>').appendTo(table);
                    var body = $('<tbody></tbody>').appendTo(table);

                    var parameters = part["parameters"];
                    if (parameters.length > 0) {
                        parameters.forEach(function (param) {
                            $('<tr><td>' + param["name"] + '</td><td>' + param["value"] + '</td></tr>').appendTo(body);
                        });
                    } else {
                        $('<tr><td>None</td><td>NA</td></tr>').appendTo(body);
                    }
                    div.append(fs);
                    table.appendTo(fs);
                });
            }

            function update_display(pipeline_id) {
                console.log("update_display()");

                update_configured_path();

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
//                select.appendTo(parent_tag);

                var parent_tag2 = $('#part_config_div2');
                parent_tag2.empty();
                parent_tag2.append($('<h2>Next Step</h2>'));
                var place_hodler = $('<div></div>');
                parent_tag2.append(place_hodler);

                var parts_keys = Object.keys(yaml_config["parts"]);
                var files = [];
                parts_keys.forEach(function (elem) {
                    var step_div = $('<div id="' + elem + '__div" style="display: none; border-width: 0; border-style : solid; border-color : black"></div>');
                    var form = $('<form id="' + elem + '__form" action="PathBuild" method="POST" enctype="multipart/form-data"></form>');
                    var parameters = yaml_config["parts"][elem]["parameters"];

                    if (parameters.length > 0) {
                        var ff2 = $('<fieldset></fieldset>').addClass("fieldset-auto-width");
                        var leg2 = $('<legend>' + elem + '</legend>');
                        var table = $('<table></table>');
                        leg2.appendTo(ff2);
                        ff2.appendTo(form);
                        table.appendTo(ff2);

                        parameters.forEach(function (p) {
                            var param = p["parameter"];
                            switch (param["type"]) {
                                case 'file':
                                    var row = $('<tr></tr>').appendTo(table);
                                    var label = $('<label for="' + elem + '__' + param["name"] + '">' + param["name"] + '</label>');
                                    row.append($('<td colspan="2"></td>').append(label));
                                    var row = $('<tr></tr>').appendTo(table);
                                    var cell = $('<td colspan="2"></td>').appendTo(row);
                                    if (use_dropzone) {
                                        $("<div class='dropzone'></div>").dropzone({
                                            paramName: elem + "__" + param["name"],
                                            url: "PathBuild",
                                            uploadMultiple: true,
                                            init: function () {
                                                this.on("sending", function (file, xhr, formData) {
                                                    formData.append("pipeline_id", pipeline_id);
                                                    formData.append("step_name", elem);

                                                    xhr.addEventListener("load", function (evt) {
                                                        submit_callback(xhr.responseText);
                                                    });
                                                });
                                            }
                                        }).appendTo(cell);
                                    } else {
                                        var name = elem + '__' + param["name"];
                                        files.push(name);
                                        var f = buildFile(name);
                                        cell.append(f);
                                    }
                                    break;
                                case 'select':
                                    var row = $('<tr></tr>').appendTo(table);
                                    var label = $('<label for="' + elem + '__' + param["name"] + '">' + param["name"] + '</label>');
                                    row.append($('<td></td>').append(label));
                                    var name = elem + '__' + param["name"];
                                    var f = buildSelect(name, param["values"], param["value"]);
                                    row.append($('<td></td>').append(f));
                                    break;
                                case 'number':
                                    var row = $('<tr></tr>').appendTo(table);
                                    var label = $('<label for="' + elem + '__' + param["name"] + '">' + param["name"] + '</label>');
                                    row.append($('<td></td>').append(label));
                                    var name = elem + '__' + param["name"];
                                    var f = buildNumber(name, param["value"]);
                                    row.append($('<td></td>').append(f));
                                    break;
                                case 'boolean':
                                    var row = $('<tr></tr>').appendTo(table);
                                    var label = $('<label for="' + elem + '__' + param["name"] + '">' + param["name"] + '</label>');
                                    row.append($('<td></td>').append(label));
                                    var name = elem + '__' + param["name"];
                                    var f = buildBoolean(name, param["value"]);
                                    row.append($('<td></td>').append(f));
                                    break;
                                case 'hidden':
                                    var name = elem + '__' + param["name"];
                                    var f = buildHidden(name, param["value"]);
                                    f.appendTo(ff2);
                                    break;
                                default:
                                    console.log("unknown type: " + param["type"]);
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
                    select.append($('<option value="' + elem + '">' + elem + '</option>'));
                });
                if (can_do.length > 0) {
                    place_hodler.append(select);
                }

                /**
                 * Display the run form.
                 */
                var can_run = false;
                specified.forEach(function (elem) {
                    can_run = can_run || elem !== 'input';
                });
                if (can_run) {
                    $('#run_pipeline_div').show();
                    $('#run').attr('action', current_parts[0]["page"]);
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

            function buildNumber(id, value) {
                return $('<input id="' + id + '" name="' + id + '" type="number" value="' + value + '"></input>');
            }

            function buildBoolean(id, value) {
                return $('<input id="' + id + '" name="' + id + '" type="checkbox" ' + (value ? "checked" : "") + '></input>');
            }

            function buildFile(id) {
                return $('<input id="' + id + '" name="' + id + '" type="file" multiple="multiple"></input>');
            }
        </script>
    </head>
    <body onload="init()">
        <h1>${pipelineBean.name}</h1>
        ${pipelineBean.description}
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
