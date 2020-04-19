import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._

import org.apache.spark.sql._

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.clustering.GaussianMixture
import org.apache.spark.mllib.clustering.BisectingKMeans

import java.io._


object BigDataClustering {

  implicit val vectorEncoder: Encoder[Vector] = org.apache.spark.sql.Encoders.kryo[Vector]

  def main(args: Array[String]) {
    val spark = SparkSession.builder
      .appName("Simple Application")
      .getOrCreate()

    import spark.implicits._

    val in_file = spark.read.format("csv")
      .option("sep", ",")
      .option("inferSchema", "true")
      .option("header", "true")

      // possibly needed to change

      .load("hdfs://" + args(0) + ":9123/user/proteins_dataset/proteins_dataset_sample.csv")
      .na.drop().cache

    def makeShingles(string: String, shingle_length: Int): List[String] = {
      var shingles = List[String]()
      if (string == null || string.isEmpty) {
        // nothing
      }
      else if (string.length() <= shingle_length) {
        shingles = shingles :+ string
      }
      else {
        for (i <- 0 to (string.length() - shingle_length)) {
          shingles = string.substring(i, i + shingle_length) +: shingles
        }
      }

      return shingles
    }

    def makeSparseVectorsCount(string: String, shingles: Array[String],
                              shingle_length: Int): Vector = {
      val shingles_in = makeShingles(string, shingle_length)
      var ret_arr = Array[Int]()
      var ret_arr_2 = Array[Double]()

      var index = 0
      for (shingle <- shingles) {
        var exists = false
        for (input_shingle <- shingles_in) {
          if (input_shingle == shingle) {
            if (!exists) {
              ret_arr = ret_arr :+ index
              ret_arr_2 = ret_arr_2 :+ 1.0
              exists = true
            }
            else {
              val new_val = ret_arr_2.last + 1.0
              ret_arr_2 = ret_arr_2.dropRight(1) :+ (ret_arr_2.last + 1.0)
            }
          }
        }

        index = index + 1
      }

      return Vectors.sparse(shingles.length, ret_arr, ret_arr_2)
    }

    def makeSparseVectors01(string: String, shingles: Array[String],
                            shingle_length: Int): Vector = {
      val shingles_in = makeShingles(string, shingle_length)
      var ret_arr = Array[Int]()
      var ret_arr_2 = Array[Double]()

      var index = 0
      for (shingle <- shingles) {
        if (shingles_in contains shingle) {
          ret_arr = ret_arr :+ index
          ret_arr_2 = ret_arr_2 :+ 1.0
        }

        index = index + 1
      }

      return Vectors.sparse(shingles.length, ret_arr, ret_arr_2)
    }

    def applyKMeans(dataset: Dataset[Vector], is_cosine: Boolean): Array[Double] = {
      var ret_arr = Array[Double]()

      for (k <- 2 to 10) {
        val model = new KMeans()
          .setK(k)
          .setMaxIterations(2)
          .setDistanceMeasure(if (is_cosine) "cosine" else "euclidean")
        val clusters = model.run(dataset.rdd)

        val cost = clusters.computeCost(dataset.rdd)
        ret_arr = ret_arr :+ cost
      }

      return ret_arr
    }

  //  def applyGaussian(dataset: Dataset[Vector]): Unit = {
  //    for (k <- 2 to 10) {
  //      println("Gaussian k = " + k)
  //      val model = new GaussianMixture().setK(k).setMaxIterations(2)
  //      val clusters = model.run(dataset.rdd)
  //
  //      for (i <- 0 until clusters.k) {
  //        println("weight = " + clusters.weights(i) +
  //                ", mu = " + clusters.gaussians(i).mu +
  //                ", sigma = " + clusters.gaussians(i).sigma)
  //      }
  //    }
  //  }

    def applyBisectingKMeans(dataset: Dataset[Vector]): Array[Double] = {
      var ret_arr = Array[Double]()

      for (k <- 2 to 10) {
        val model = new BisectingKMeans()
          .setK(k)
          .setMaxIterations(2)
        val clusters = model.run(dataset.rdd)

        val cost = clusters.computeCost(dataset.rdd)
        ret_arr = ret_arr :+ cost
      }

      return ret_arr
    }

    def makeShinglesTriplets(row: Row): List[String] = {
      makeShingles(row.getString(7), 3)
    }

    val shingling_3 = spark.sparkContext
      .broadcast(in_file.flatMap(makeShinglesTriplets)
        .distinct().collect())

    def makeSparseVectors3(row : Row): Vector = {
      makeSparseVectors01(row.getString(7), shingling_3.value, 3)
    }

    def makeSparseVectorsCount3(row : Row): Vector = {
      makeSparseVectorsCount(row.getString(7), shingling_3.value, 3)
    }

    val converted_to_sparse_vectors_3 = in_file.map(makeSparseVectors3).cache
    val converted_to_sparse_vectors_3_count = in_file.map(makeSparseVectorsCount3).cache

    def print_(i : Any): Unit = { println(i) }

    converted_to_sparse_vectors_3.collect().foreach(print_)

    // K-means
    println("Computing k-means (euclidean) costs for 0/1 shingles...")
    val k_means_cost = applyKMeans(converted_to_sparse_vectors_3, is_cosine = false)
    println("K-means (euclidean) costs for 0/1 shingles:")
    k_means_cost.foreach(print_)

    println("Computing k-means (euclidean) costs for counted shingles...")
    val count_k_means_cost = applyKMeans(converted_to_sparse_vectors_3_count, is_cosine = false)
    println("K-means (euclidean) costs for counted shingles:")
    count_k_means_cost.foreach(print_)

    // Bisecting K-means
    println("Computing bisecting k-means (euclidean) costs for counted shingles...")
    val bkmeans_cost = applyBisectingKMeans(converted_to_sparse_vectors_3)
    println("Bisecting k-means (euclidean) costs for counted shingles:")
    bkmeans_cost.foreach(print_)

    println("Computing bisecting k-means (euclidean) costs for counted shingles...")
    val count_bkmeans_cost = applyBisectingKMeans(converted_to_sparse_vectors_3_count)
    println("Bisecting k-means (euclidean) costs for counted shingles:")
    count_bkmeans_cost.foreach(print_)

    // K-means with cosine distance measure
    println("Computing k-means (cosine) costs for counted shingles...")
    val k_means_cosine_cost = applyKMeans(converted_to_sparse_vectors_3, is_cosine = true)
    println("K-means (cosine) costs for counted shingles:")
    k_means_cosine_cost.foreach(print_)

    println("Computing gaussian (cosine) costs for counted shingles...")
    val count_k_means_cosine_cost = applyKMeans(converted_to_sparse_vectors_3_count, is_cosine = true)
    println("K-means (cosine) costs for counted shingles:")
    count_k_means_cosine_cost.foreach(print_)

//    // Gaussian mixture - java.lang.OutOfMemoryError
//    println("Computing gaussian (euclidean) parameters for 0/1 shingles:")
//    applyGaussian(converted_to_sparse_vectors_3)
//
//    println("Computing gaussian (euclidean) parameters for counted shingles:")
//    applyGaussian(converted_to_sparse_vectors_3_count)

    val file = new File("result.txt")

    val bw = new BufferedWriter(new FileWriter(file))

    def write_(i : Any): Unit = { bw.write(i.toString + "\n") }

    bw.write("K-means euclidean costs (from 2 to 10 clusters):\n")
    k_means_cost.foreach(write_)
    bw.write("\nK-means euclidean costs (from 2 to 10 clusters) for counted shingles:\n")
    count_k_means_cost.foreach(write_)
    bw.write("\nBisecting K-means euclidean costs (from 2 to 10 clusters):\n")
    bkmeans_cost.foreach(write_)
    bw.write("\nBisecting K-means euclidean costs (from 2 to 10 clusters) for counted shingles:\n")
    count_bkmeans_cost.foreach(write_)
    bw.write("\nK-means cosine costs (from 2 to 10 clusters):\n")
    k_means_cosine_cost.foreach(write_)
    bw.write("\nK-means cosine costs (from 2 to 10 clusters) for counted shingles:\n")
    count_k_means_cosine_cost.foreach(write_)

    bw.close()

    spark.stop()
  }
}
