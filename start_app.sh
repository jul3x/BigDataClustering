export HADOOP_CONF_DIR=/tmp_local/hadoop.jp420564/cluster/hadoop-2.8.5/etc/hadoop/

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd $DIR

/tmp_local/hadoop.jp420564/cluster/spark-2.4.5-bin-hadoop2.7/bin/spark-submit \
    --class BigDataClustering \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 2g \
    --executor-memory 2g \
    --executor-cores 1 \
    target/scala-2.12/bigdataclustering_2.12-0.1.jar 
