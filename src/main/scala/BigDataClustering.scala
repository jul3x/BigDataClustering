import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._

import org.apache.spark.sql._

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.clustering.KMeans


object BigDataClustering {
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
        .setMaxIterations(3)
        .setDistanceMeasure(if (is_cosine) "cosine" else "euclidean")
      val clusters = model.run(dataset.rdd)

      val cost = clusters.computeCost(dataset.rdd)
      ret_arr = ret_arr :+ cost
    }

    return ret_arr
  }

  def applyGaussian(dataset: Dataset[Vector]): Array[Double] = {
    return Array[Double]()
  }

  def applyPIC(dataset: Dataset[Vector]): Array[Double] = {
    return Array[Double]()
  }

  implicit val vectorEncoder: Encoder[Vector] = org.apache.spark.sql.Encoders.kryo[Vector]

  def main(args: Array[String]) {
    val spark = SparkSession.builder
      .appName("Simple Application")
      .config("spark.master", "local") // TODO NEEDED TO CHANGE!
      .getOrCreate()

    import spark.implicits._

    val inFile = spark.read.format("csv")
      .option("sep", ",")
      .option("inferSchema", "true")
      .option("header", "true")
      .load("file:///home/julex/BigDataClustering/proteins_dataset_sample.csv")
      .na.drop().cache

    val shingling_3 = spark.sparkContext
      .broadcast(inFile.flatMap(row => makeShingles(row.getString(7), 3))
        .distinct().collect())

    val converted_to_sparse_vectors_3 = inFile
      .map(row => makeSparseVectors01(row.getString(7), shingling_3.value, 3)).cache
    val converted_to_sparse_vectors_3_count = inFile
      .map(row => makeSparseVectorsCount(row.getString(7), shingling_3.value, 3)).cache

    converted_to_sparse_vectors_3.collect().foreach(i => println(i))

    println("Computing k-means (euclidean) costs for 0/1 shingles:")
    val k_means_cost = applyKMeans(converted_to_sparse_vectors_3, is_cosine = false)
    println("K-means (euclidean) costs for 0/1 shingles:")
    k_means_cost.foreach(i => println(i))

    println("Computing k-means (euclidean) costs for counted shingles:")
    val count_k_means_cost = applyKMeans(converted_to_sparse_vectors_3_count, is_cosine = false)
    println("K-means (euclidean) costs for counted shingles:")
    count_k_means_cost.foreach(i => println(i))
//
//    val gaussian_cost = applyGaussian(converted_to_sparse_vectors_3)
//    val count_gaussian_cost = applyGaussian(converted_to_sparse_vectors_3_count)
//
//    val pic_cost = applyPIC(converted_to_sparse_vectors_3)
//    val count_pic_cost = applyPIC(converted_to_sparse_vectors_3_count)
//
//    val k_means_cosine_cost = applyKMeans(converted_to_sparse_vectors_3, is_cosine = true)
//    val count_k_means_cosine_cost = applyKMeans(converted_to_sparse_vectors_3_count, is_cosine = true)
//

    spark.stop()
  }
}