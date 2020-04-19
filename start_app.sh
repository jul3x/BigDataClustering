source env.sh

export HADOOP_CONF_DIR=/tmp_local/hadoop.jp420564/cluster/hadoop-2.7.7/etc/hadoop/
export YARN_CONF_DIR=/tmp_local/hadoop.jp420564/cluster/hadoop-2.7.7/etc/hadoop/
export SPARK_LOCAL_DIRS=/tmp_local/hadoop.jp420564/cluster/spark_data
export LOCAL_DIRS=/tmp_local/hadoop.jp420564/cluster/spark_data

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd $DIR

/tmp_local/hadoop.jp420564/cluster/spark-2.4.5-bin-hadoop2.7/bin/spark-submit \
    --class BigDataClustering \
    --master yarn \
    --deploy-mode cluster \
    --conf java.io.tmpdir=/tmp_local/hadoop.jp420564/spark_data \
    --conf spark.local.dir=/tmp_local/hadoop.jp420564/spark_data \
    target/scala-2.12/bigdataclustering_2.12-0.1.jar 
