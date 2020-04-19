source env.sh

hdfs dfs -mkdir /user
hdfs dfs -mkdir /user/proteins_dataset
hdfs dfs -put proteins_dataset.csv /proteins_dataset
hdfs dfs -put proteins_dataset_sample.csv /proteins_dataset


