hdfs_dir="/tmp_local/hadoop.jp420564/hdfsdata"
cluster_dir="/tmp_local/hadoop.jp420564/cluster"


export JAVA_HOME=$cluster_dir/jdk-13.0.2
export HADOOP_INSTALL=$cluster_dir/hadoop-2.8.5
export HADOOP_PREFIX=$HADOOP_INSTALL

JAVA_HOME_=/tmp_local/hadoop.jp420564/cluster/jdk-13.0.2
HADOOP_INSTALL_=/tmp_local/hadoop.jp420564/cluster/hadoop-2.8.5

export PATH=$JAVA_HOME_/bin:$HADOOP_INSTALL_/bin:$HADOOP_INSTALL_/sbin:$PATH

