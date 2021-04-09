#! /usr/bin/bash

# Placeholder for clean-up script
# To be executed in the root of the build tree
# Requires at most one argument: the peer id
# Cleans the directory tree for storing 
#  both the chunks and the restored files of
#  either a single peer, in which case you may or not use the argument
#    or for all peers, in which case you 


# Check number input arguments
argc=$#

if [ "$argc" -eq 1 ]
then
	peer_id=$1
	rm -rf ../resources/peers/$peer_id
elif [ "$argc" -eq 0 ]
then
    rm -rf ../resources/peers/
else
	echo "Usage: $0 [<peer_id>]]"
	exit 1
fi

# Kill rmiregistry process (port=1099) if it is running
rmi_pid=$(lsof -i:1099 | grep rmi | awk 'NR==1 {print $2}')
if [ -n "$rmi_pid" ]
then
  kill $rmi_pid
fi

# Clean the directory tree for storing files
# For a crash course on shell commands check for example:
# Command line basic commands from GitLab Docs':	https://docs.gitlab.com/ee/gitlab-basics/command-line-commands.html
# For shell scripting try out the following tutorials of the Linux Documentation Project
# Bash Guide for Beginners: https://tldp.org/LDP/Bash-Beginners-Guide/html/index.html
# Advanced Bash Scripting: https://tldp.org/LDP/abs/html/

