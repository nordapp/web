#
# TODO: The session0 contains the data of the SessionControl; remove the data by runtime.
#       Currently, this is never done. Leaks RAM !!!
#

# Install the shiro feature to karaf
feature:repo-add mvn:org.apache.shiro/shiro-features/1.2.3/xml/features

# Install the feature you need
# feature:install shiro-web shiro-ehcache shiro-quartz shiro-spring shiro-aspectj
feature:install shiro-web