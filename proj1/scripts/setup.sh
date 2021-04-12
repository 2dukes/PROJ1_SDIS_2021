
# Placeholder for setup script
# To be executed in the root of the build tree.
# It starts the rmiregistry if not yet running.

# Start rmiregistry (port=1099) if not running yet
rmi_pid=$(lsof -i:1099 | grep rmi | awk 'NR==1 {print $2}')
if [ -z "$rmi_pid" ]
then
  $(rmiregistry) &
fi