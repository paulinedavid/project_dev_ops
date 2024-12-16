#!groovy

import jenkins.model.*
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition

def instance = Jenkins.getInstance()

def pipelineJobName = 'my-pipeline-job'
def pipelineJob = instance.getItem(pipelineJobName)

// charger le script de pipeline depuis un fichier externe
def pipelineScriptFromFile = new File('/usr/share/jenkins/ref/jobs/pipelineJob.groovy').text

if (pipelineJob == null) {
    pipelineJob = instance.createProject(WorkflowJob, pipelineJobName)
    def pipelineScript = pipelineScriptFromFile
    def flowDefinition = new CpsFlowDefinition(pipelineScript, true)
    pipelineJob.setDefinition(flowDefinition)
    pipelineJob.save()
}

def pipelineJobNameKub = 'my-pipeline-job-kubernetes'
def pipelineJobKub = instance.getItem(pipelineJobNameKub)

// charger le script de pipeline depuis un fichier externe
def pipelineScriptFromFileKub = new File('/usr/share/jenkins/ref/jobs/pipelineKubernetes.groovy').text

if (pipelineJobKub == null) {
    pipelineJobKub = instance.createProject(WorkflowJob, pipelineJobNameKub)
    def pipelineScriptKub = pipelineScriptFromFileKub
    def flowDefinitionKub = new CpsFlowDefinition(pipelineScriptKub, true)
    pipelineJobKub.setDefinition(flowDefinitionKub)
    pipelineJobKub.save()
}

instance.save()
