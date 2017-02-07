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
data-path: /work/Dev/green-pipeline-web/data
parts:
    input:
        requires: []
        page: InputFile
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
            - parameter:
                name: path
                value: /work/Documents/FOUO/EntityExtraction/joint_ere
                type: hidden
            - parameter:
                name: classpath
                value: ere-11-08-2016_small.jar:lib/commons-io-2.4.jar:lib/commons-lang3-3.1.jar:lib/commons-math3-3.2.jar:lib/dom4j-1.6.1.jar:lib/edu.mit.jwi_2.2.3_jdk.jar:lib/foo:lib/grmm-deps.jar:lib/jaxen-1.1-beta-6.jar:lib/mallet-deps.jar:lib/mallet.jar:lib/opennlp-maxent-3.0.2-incubating.jar:lib/opennlp-tools-1.5.2-incubating.jar:lib/RadixTree-0.3.jar:lib/stanford-corenlp-3.3.1.jar:lib/stanford-corenlp-3.3.1-models.jar:lib/trove-3.0.3.jar
                type: hidden
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
            - parameter:
                name: path
                value: /work/Documents/FOUO/EntityExtraction/ENIE
                type: hidden
            - parameter:
                name: classpath
                value: bin:lib/nametagging.jar:lib/stanford-postagger.jar:lib/colt-nohep.jar:lib/dbparser.jar:lib/dom4j-1.6.1.jar:lib/javelin.jar:lib/jaxen-1.1.1.jar:lib/joda-time-1.6.jar:lib/jyaml-1.3.jar:lib/log4j.jar:lib/mallet.jar:lib/mallet_old.jar:lib/opennlp-maxent-3.0.1-incubating.jar:lib/opennlp-tools-1.5.1-incubating.jar:lib/pnuts.jar:lib/RadixTree-0.3.jar:lib/stanford-parser.jar:lib/lucene-core-3.0.2.jar:lib/weka.jar:lib/trove.jar:lib/indri.lib
                type: hidden
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
            - parameter:
                name: path
                value: /work/Documents/FOUO/EntityExtraction/joint_ere
                type: hidden
            - parameter:
                name: classpath
                value: ere-11-08-2016_small.jar:lib/commons-io-2.4.jar:lib/commons-lang3-3.1.jar:lib/commons-math3-3.2.jar:lib/dom4j-1.6.1.jar:lib/edu.mit.jwi_2.2.3_jdk.jar:lib/foo:lib/grmm-deps.jar:lib/jaxen-1.1-beta-6.jar:lib/mallet-deps.jar:lib/mallet.jar:lib/opennlp-maxent-3.0.2-incubating.jar:lib/opennlp-tools-1.5.2-incubating.jar:lib/RadixTree-0.3.jar:lib/stanford-corenlp-3.3.1.jar:lib/stanford-corenlp-3.3.1-models.jar:lib/trove-3.0.3.jar
                type: hidden
    visualize:
        output-dir: viz_out
        requires:
            - input
        page: Visualize
        multi-run: true
        parameters:
            - parameter:
                name: path
                value: /work/Documents/FOUO/EntityExtraction/green_pipeline/visualization
                type: hidden

```
