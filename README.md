# BigDataClustering
First big task for Big Data course at MIMUW. 
Comparison of different methods and ways of clustering the data (human proteins depending on their amino-acids).

Dataset in repository comes from: https://www.uniprot.org/uniprot/?query=proteome:UP000005640

## Assumptions: 
   * Shingle length is set to 3. The reason is the fact that dataset contains many genes which names length is 3.
   * Sequences are converted to sparse vectors because of their format. Vectors size is very big but majority of cells is set to 0. Sparse vectors are much more efficient in such cases.
   * Task compares ways of converting sequences to 0/1 for each shingle and shingle counts for each shingle.
   * There is comparison of two distance measures - Euclidean and cosine (only for K-Means since other methods does not support this type of distance measure).
   * Also, three clustering algorithms available in `mllib` are used: K-Means, Gaussian mixture and Bisecting K-Means.
   * Every clustering method is verified using comparison of computed costs of clusters and the best one is picked.
   
## Results:
   * Unfortunately Gaussian mixture algorithm caused java.lang.OutOfMemoryError so it was removed from the original code.
   