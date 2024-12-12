#!groovy

import jenkins.model.*
import hudson.slaves.*
import hudson.model.*
import hudson.plugins.sshslaves.*


def jenkins = Jenkins.getInstance()

def agentName = "jenkins-agent"
def remoteFS = "/home/jenkins"
def label = "agent"
def numExecutors = 1
// usage : only build jobs with label expression matching this node
def usageMode = Node.Mode.NORMAL

// Launch nethode: Launch agent via ssh
ComputerLauncher launcher = new hudson.plugins.sshslaves.SSHLauncher(
  "jenkins-agent", // host
  22, // port
  "jenkins-ssh-key",  // credentialsId
  (String)null, // jvmOptions
  (String)null, // javaPath
  (String)null, // prefixStartSlaveCmd
  (String)null, // suffixStartSlaveCmd
  60, // launchTimeoutSeconds
  10, // maxNumRetries
  10, // retryWaitTime
  new hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy()
)

def agent = new DumbSlave(
    agentName,
    remoteFS,
    launcher
)

agent.setNumExecutors(numExecutors)
agent.setLabelString(label)
agent.setMode(usageMode)

jenkins.addNode(agent)
