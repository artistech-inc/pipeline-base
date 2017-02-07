# Pipeline Base Project

This is a base project from which all other pipeline projects should extend.
By creating a new maven web-app project and including this as a dependency, all base servlets and jsp pages are automatically added to the new web-app.

# Extending

Must specify a yaml file configuration to be placed in `src/main/resources/` named `pipeline.yml`.

## Example File from the Green Pipeline:

```yaml
name: "Green Pipeline Run View"
description: |
    The Green Pipeline allows a user to run joint_ere, enie, or both; merge the data if both are used; and generate visualization of the individual and merged output.
data-object: com.artistech.ee.beans.Data
parts:
    input:
        requires: []
        page: pipeline.jsp
        output-dir: input
        parameters:
            - parameter:
                name: SGM_File
                value: ""
                type: file
    joint_ere:
        output-dir: joint_ere_out
        requires:
            - input
        page: JointEre
        parameters:
            - parameter:
                name: tagger
                value: edu.rpi.jie.ere.joint.Tagger
                values:
                    - edu.rpi.jie.ere.joint.Tagger
                type: select
            - parameter:
                name: model
                value: models/joint/joint_model
                values:
                    - models/joint/joint_model
                type: select
    enie:
        output-dir: enie_out
        requires:
            - input
        page: ENIE
        parameters:
            - parameter:
                name: tagger
                value: cuny.blender.englishie.ace.IETagger
                values:
                    - cuny.blender.englishie.ace.IETagger
                type: select
            - parameter:
                name: property
                value: props/enie.property
                values:
                    - props/enie.property
                type: select
    merge:
        output-dir: merge_out
        requires:
            - input
            - joint_ere
            - enie
        page: Merge
        parameters:
            - parameter:
                name: combiner
                value: arl.workflow.combine.MergeEnieEre
                values:
                    - arl.workflow.combine.MergeEnieEre
                type: select
    visualize:
        output-dir: viz_out
        requires:
            - input
        page: Visualize
        parameters: []
        multi-run: true
```
