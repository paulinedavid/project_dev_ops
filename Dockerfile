FROM jenkins/jenkins:2.452.2-jdk17

USER root

# Install Docker CLI
RUN apt-get update && apt-get install -y lsb-release
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc https://download.docker.com/linux/debian/gpg
RUN echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.asc] https://download.docker.com/linux/debian $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
RUN apt-get update && apt-get install -y docker-ce-cli

# Install sudo
RUN apt-get install -y sudo

# Create docker group and add Jenkins user to it
RUN groupadd -f docker
RUN usermod -aG docker jenkins

# Add Jenkins user to sudoers
RUN echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

# Create directories
RUN mkdir -p /usr/share/jenkins/ref/plugins /usr/share/jenkins/ref/init.groovy.d /usr/share/jenkins/ref/jobs /usr/share/jenkins/ref/.ssh

# Copy files
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY jobs /usr/share/jenkins/ref/jobs
COPY init.groovy.d /usr/share/jenkins/ref/init.groovy.d
COPY .ssh /usr/share/jenkins/ref/.ssh

# Install Jenkins plugins
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

USER jenkins
