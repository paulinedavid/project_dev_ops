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

instance.save()
