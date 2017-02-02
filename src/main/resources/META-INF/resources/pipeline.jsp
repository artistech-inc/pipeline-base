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
                            formData.append(input[0].id, $('#' + input[0].id)[0].files[jj]);
                        }
                    }
                });

//                if (files.length > 0) {
//                    for (var ii = 0; ii < files.length; ii++) {
//                        var fs = $('#' + files[ii]);
//                        for (var jj = 0; jj < fs[0].files.length; jj++) {
//                            formData.append(files[ii], $('#' + files[ii])[0].files[jj]);
//                        }
//                    }
//                }

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
                var configured_tag = document.getElementById('configured_div');
                while (configured_tag.firstChild) {
                    configured_tag.removeChild(configured_tag.firstChild);
                }

                /**
                 * build the current path....
                 */
                for (var ii = 0; ii < current_parts.length; ii++) {
                    var div = document.createElement('div');
                    var fs = document.createElement('fieldset');
                    var leg = document.createElement('legend');
                    leg.innerHTML = current_parts[ii]["name"];
                    fs.className = 'fieldset-auto-width';
                    div.appendChild(fs);
                    fs.appendChild(leg);
                    configured_tag.append(fs);
                    var br = document.createElement('br');
                    configured_tag.append(br);

                    var parameters = current_parts[ii]["parameters"];
                    var ul = document.createElement('ul');
                    if (parameters.length > 0) {
                        for (var jj = 0; jj < parameters.length; jj++) {
                            var param = parameters[jj];
                            var li = document.createElement('li');
                            li.innerHTML = param["name"] + ': ' + param["value"];
                            ul.appendChild(li);
                        }
                    } else {
                        var li = document.createElement('li');
                        li.innerHTML = 'No Parameters';
                        ul.appendChild(li);
                    }
                    fs.appendChild(ul);
                }

                /**
                 * Build the parameter forms.
                 */
                //need equivalend of getPartsAfter(specified) in javascript.
                var parent_tag = document.getElementById('part_config_div');
                while (parent_tag.firstChild) {
                    parent_tag.removeChild(parent_tag.firstChild);
                }
                var select = document.createElement('select');
                select.id = 'step';
                select.onchange = onStepChange;
                var specified = [];
                for (var ii = 0; ii < current_parts.length; ii++) {
                    specified.push(current_parts[ii].name);
                }
                parent_tag.appendChild(select);

                var parent_tag2 = document.getElementById('part_config_div2');
                var parts_keys = Object.keys(yaml_config["parts"]);
                var files = [];
                for (var ii = 0; ii < parts_keys.length; ii++) {
                    //select.options[select.options.length] = new Option(parts_keys[ii], parts_keys[ii]);
                    //<div id='${step.name}__div' style='display: none; border-width: 0; border-style : solid; border-color : black'>
                    var step_div = document.createElement('div');
                    step_div.style = 'display: none; border-width: 0; border-style : solid; border-color : black';
                    step_div.id = parts_keys[ii] + '__div';
                    //<form method="POST" action="PathBuild" enctype="multipart/form-data" id="${step.name}__form">
                    var form = document.createElement('form');
                    form.action = 'PathBuild';
                    form.method = 'POST';
                    form.enctype = 'multipart/form-data';
                    form.id = parts_keys[ii] + "__form";
                    var parameters = yaml_config["parts"][parts_keys[ii]]["parameters"];


                    if (parameters.length > 0) {
                        var ff2 = document.createElement('fieldset');
                        ff2.className = 'fieldset-auto-width';
                        var leg2 = document.createElement('legend');
                        leg2.innerHTML = parts_keys[ii];
                        form.appendChild(ff2);
                        ff2.appendChild(leg2);

                        for (var jj = 0; jj < parameters.length; jj++) {
                            //<div id='step.name}__$parameter.name}__div" />' style='border-width: 0; border-style : solid; border-color : black'>
                            var div3 = document.createElement('div');
                            div3.style = 'border-width: 0; border-style : solid; border-color : black';
                            ff2.appendChild(div3);
                            var param = parameters[jj]["parameter"];
                            if (param["type"] === 'file') {
                                var label = document.createElement('label');
                                label.innerHTML = param["name"];
                                var name = parts_keys[ii] + '__' + param["name"];
                                files.push(name);
                                label.for = name;
                                var f = buildFile(name);
                                div3.appendChild(label);
                                div3.appendChild(f);
                            } else if (param["type"] === 'select') {
                                var label = document.createElement('label');
                                label.innerHTML = param["name"];
                                var name = parts_keys[ii] + '__' + param["name"];
                                label.for = name;
                                var f = buildSelect(name, param["values"], param["value"]);
                                div3.appendChild(label);
                                div3.appendChild(f);
                            } else if (param["type"] === 'hidden') {
                                var name = parts_keys[ii] + '__' + param["name"];
                                var f = buildHidden(name, param["value"]);
                                div3.appendChild(f);
                            }
                        }
                    }

                    form.appendChild(document.createElement('br'));
                    var id_input = document.createElement('input');
                    id_input.type = 'hidden';
                    id_input.id = 'pipeline_id';
                    id_input.name = 'pipeline_id';
                    id_input.value = pipeline_id;
                    var step_input = document.createElement('input');
                    step_input.type = 'hidden';
                    step_input.id = 'step_name';
                    step_input.name = 'step_name';
                    step_input.value = parts_keys[ii];
                    var submit_input = document.createElement('input');
                    submit_input.type = 'button';
                    submit_input.value = 'Add Step';

                    var value = parts_keys[ii] + "__form";
                    submit_input.onclick = onclickGenerator(value);
                    form.appendChild(id_input);
                    form.appendChild(step_input);
                    form.appendChild(submit_input);
                    parent_tag2.appendChild(step_div);
                    step_div.appendChild(form);
                }

                /**
                 * Remove any parts that are currently unable to be performed
                 * due to un-met requirements.
                 */
                var can_do = [];
                for (var ii = 0; ii < parts_keys.length; ii++) {
                    var part = yaml_config["parts"][parts_keys[ii]];
                    var requires = part["requires"];
                    var sub = requires.diff(specified);
                    if (sub.length === 0) {
                        var multi = part["multi-run"];
                        if (typeof multi === 'undefined') {
                            multi = false;
                        }
                        if (multi || $.inArray(parts_keys[ii], specified) < 0) {
                            can_do.push(parts_keys[ii]);
                        }
                    }
                }
                /**
                 * Show only those parts with satisfied requirements.
                 */
                for (var ii = 0; ii < can_do.length; ii++) {
                    select.options[select.options.length] = new Option(can_do[ii], can_do[ii]);
                }

                /**
                 * Display the run form.
                 */
                if (current_parts.length > 1) {
                    $("#run_pipeline_div").show();
                    document.getElementById('run').action = current_parts[1]["page"];
                }

                /**
                 * This will hide/show appropriate part(s).
                 */
                onStepChange();
            }

            function onclickGenerator(id) {
                return function () {
                    addStep(id);
                };
            }

            function buildSelect(id, values, value) {
                var select = document.createElement('select');
                select.id = id;
                for (var ii = 0; ii < values.length; ii++) {
                    select.options[select.options.length] = new Option(values[ii], values[ii]);
                    if (values[ii] === value) {
                        select.options[select.options.length - 1].selected = true;
                    }
                }
                return select;
            }

            function buildHidden(id, value) {
                var hidden = document.createElement('input');
                hidden.id = id;
                hidden.type = 'hidden';
                hidden.value = value;
                return hidden;
            }

            function buildFile(id) {
                var file = document.createElement('input');
                file.id = id;
                file.type = 'file';
                file.multiple = 'multiple';
                return file;
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
