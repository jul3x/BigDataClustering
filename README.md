# BigDataClustering
First big task for Big Data course at MIMUW.  
Comparison of different methods and ways of clustering the data (human proteins depending on their amino-acids).    

Dataset in repository comes from: https://www.uniprot.org/uniprot/?query=proteome:UP000005640  

Due to collisions, many of ports of HDFS setup had to be changed. For more information visit `install.sh`.  

## Assumptions: 
   * Shingle length is set to 3.
   * Sequences are converted to sparse vectors because of their format. Vectors size is very big but majority of cells is set to 0. Sparse vectors are much more efficient in such cases.
   * Task compares ways of converting sequences to 0/1 for each shingle and shingle counts for each shingle.
   * There is comparison of two distance measures - Euclidean and cosine (only for K-Means since other methods does not support this type of distance measure).
   * Also, three clustering algorithms available in `mllib` are used: K-Means, Gaussian mixture and Bisecting K-Means.
   * Every clustering method is verified using comparison of computed costs of clusters and the best one is picked.

## Usage:
   ### Downloading
   * Run `1download.sh` script to download open-jdk, hadoop and spark.
   ### Installing hdfs
   * Run `2install.sh` and then `3copy.sh` scripts to install hadoop and copy spark dirs on desired computers (computers IPs should be described in ~/slaves_with_master and ~/slaves_no_master files).
   ### Running hdfs
   * Run `start.sh` to run hdfs on a cluster (key-based authentication required).
   ### Uploading dataset
   * Run `upload_to_hdfs.sh` to upload dataset to hdfs.
   ### Running application
   * First option:  
      * Run `build_app.sh` script to make *.jar file in target/scala-2.12 dir (bigdataclustering_2.12-0.1.jar)
      * Run `start_app.sh` script to run application on YARN with standard `proteins_dataset.csv`. Due to a very long runtime - dataset can be changed to prepared sample dataset `proteins_dataset_sample.csv` by changing filepath in `BigDataClustering.scala` file. Results are shown in stdout (possibly on workers logs).
   * Second option
      * Run `start_app_shell.sh` to run application using spark-shell command and type `BigDataClustering.main(Array("IP of Master Node"))`.
   ### End of work
   * Run `stop.sh` to close the whole hdfs.

## Results:
   * Unfortunately Gaussian mixture algorithm caused java.lang.OutOfMemoryError so it was removed from the original code.
