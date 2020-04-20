# BigDataClustering
First big task for Big Data course at MIMUW.  
Comparison of different methods and ways of clustering the data (human proteins depending on their amino-acids).    

Dataset in repository comes from: https://www.uniprot.org/uniprot/?query=proteome:UP000005640  

Due to collisions, many of ports of HDFS setup had to be changed. For more information visit `2install.sh` and 
`src/main/scala/BigDataClustering.scala` file.  

## Assumptions: 
   * Shingle length is set to 3.
   * Sequences are converted to sparse vectors because of their format. Vectors size is very big but majority of cells is set to 0. Sparse vectors are much more efficient in such cases.
   * Task compares ways of converting sequences to 0/1 for each shingle and shingle counts for each shingle.
   * There is comparison of two distance measures - Euclidean and cosine (only for K-Means since other methods does not support this type of distance measure).
   * Also, three clustering algorithms available in `mllib` are used: K-Means, Gaussian mixture and Bisecting K-Means.
   * Every clustering method is verified by checking which cluster count is the best by using comparison of computed costs.
   * For each method from 2 to 10 clusters count are checked.

## Usage:
   ### Downloading
   * Run `1download.sh` script to download open-jdk, hadoop and spark.
   ### Installing hdfs
   * Run `2install.sh` and then `3copy.sh` scripts to install hadoop and copy spark dirs on desired computers (computers IPs should be described in `~/slaves_with_master`, `~/slaves_no_master` and `~/master` files).
   ### Running hdfs
   * Run `start.sh` to run hdfs on a cluster (key-based authentication required).
   ### Uploading dataset
   * Run `upload_to_hdfs.sh` to upload dataset to hdfs.
   ### Running application
   * First option:  
      * Run `build_app.sh` script to make *.jar file in target/scala-2.12 dir (bigdataclustering_2.12-0.1.jar)
      * Run `start_app.sh` script to run application on YARN with standard `proteins_dataset.csv`. Due to a long runtime - dataset can be changed to prepared sample dataset `proteins_dataset_sample.csv` by changing filepath in `BigDataClustering.scala` file. Results are shown in stdout (possibly on workers logs).
   * Second option
      * Run `start_app_shell.sh` to run application using spark-shell command and type `BigDataClustering.main(Array("IP of Master Node"))`.
   ### End of work
   * Run `stop.sh` to close the whole hdfs.

## Results:
   * Unfortunately Gaussian mixture algorithm caused java.lang.OutOfMemoryError so it was removed from the original code.
   
   * Output example:  
        ```
        Computing k-means (euclidean) costs for 0/1 shingles...
        K-means (euclidean) costs for 0/1 shingles:
        8466756.42960725
        8418020.433892556
        8321326.970934407
        8231526.038268545
        8361569.79441547
        8335749.0203573005
        8217844.19317322
        8204676.1906861365
        8370427.612212859
        Computing k-means (euclidean) costs for counted shingles...
        K-means (euclidean) costs for counted shingles:
        1.801213921965857E7
        1.787300835842771E7
        1.7316971384048596E7
        1.7095917256662734E7
        1.6657791783044666E7
        1.6715014759375498E7
        1.6419997698673233E7
        1.692574544923781E7
        1.6009777520938275E7
        Computing bisecting k-means (euclidean) costs for 0/1 shingles...
        Bisecting k-means (euclidean) costs for 0/1 shingles:
        8362755.129504653
        8317776.614795482
        8223331.110051981
        8215007.063725167
        8207122.8737928625
        8196281.251141204
        8166574.697060907
        8161129.035447272
        8159310.941948608
        Computing bisecting k-means (euclidean) costs for counted shingles...
        Bisecting k-means (euclidean) costs for counted shingles:
        1.8094580549627874E7
        1.798905665462321E7
        1.763822608129214E7
        1.7621113296520606E7
        1.756245680586507E7
        1.7528056661496162E7
        1.733614323005964E7
        1.732913343528849E7
        1.732601036493237E7
        Computing k-means (cosine) costs for 0/1 shingles...
        K-means (cosine) costs for 0/1 shingles:
        14147.352743106976
        14130.577650103449
        14157.023865510053
        14050.046292184237
        14069.252511027393
        14038.002039308603
        14068.76132366895
        14013.521169179003
        14080.760727434223
        Computing k-means (cosine) costs for counted shingles...
        K-means (cosine) costs for counted shingles:
        14210.910775767436
        14146.178298770017
        13907.983110036654
        13999.461249876933
        13916.92671255353
        13773.47497379797
        14137.667892707337
        13881.844165120827
        13645.68398892197
        ```
    
   * K-Means (euclidean distance measure) and 0/1 shingles the best cluster count is 9  
   * K-Means (euclidean distance measure) and counted shingles the best cluster count is 8 
   * Bisecting K-Means (euclidean distance measure) and 0/1 shingles the best cluster count is 10 
   * Bisecting K-Means (euclidean distance measure) and counted shingles the best cluster count is 10
   * K-Means (cosine distance measure) and 0/1 shingles the best cluster count is 9 
   * K-Means (cosine distance measure) and counted shingles the best cluster count is 10 

   
