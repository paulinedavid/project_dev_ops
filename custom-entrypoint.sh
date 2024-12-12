#!/bin/bash
# Custom entrypoint script

# Perform any necessary operations as root here

# Execute the original entrypoint command (setup-sshd) with root privileges
/usr/local/bin/setup-sshd

# Now that setup-sshd has completed, switch to the jenkins user for any subsequent commands
exec su - jenkins -c "/usr/local/bin/jenkins-agent $@"