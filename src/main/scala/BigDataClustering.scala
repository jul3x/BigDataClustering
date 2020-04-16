import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.spark.rdd.RDD

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.ml.evaluation.Evaluator

object BigDataClustering {
  def makeShingles(string: String, shingle_length: Int) : List[String] = {
    var shingles = List[String]()
    if (string == null || string.isEmpty) {
      // nothing
    }
    else if (string.length() <= shingle_length) {
      shingles = shingles :+ string
    }
    else {
      for (i <- 0 to (string.length() - shingle_length)) {
        shingles = shingles :+ string.substring(i, i + shingle_length)
      }
    }

    shingles
  }

  def makeDenseVectors(string: String, shingles: Array[String],
                       shingle_length: Int): Vector = {
    val shingles_in = makeShingles(string, shingle_length)
    var ret_arr = Array.ofDim[Double](shingles.size)

    var index = 0
    for (shingle <- shingles) {
      if (shingles_in contains shingle) {
        ret_arr(index) = 1
      }
      else {
        ret_arr(index) = 0
      }

      index = index + 1
    }

    Vectors.dense(ret_arr)
  }

  def makeSparseVectors(string: String, shingles: Array[String],
                        shingle_length: Int): Vector = {
    var shingles_in = makeShingles(string, shingle_length)
    var ret_arr = Array[Int]()
    var ret_arr_2 = Array[Double]()

    var index = 0
    for (shingle <- shingles)
    {
      if (shingles_in contains shingle)
      {
        ret_arr = ret_arr :+ index
        ret_arr_2 = ret_arr_2 :+ 1.0
      }

      index = index + 1
    }

    return Vectors.sparse(shingles.size, ret_arr, ret_arr_2)
  }

  implicit val vectorEncoder: Encoder[Vector] = org.apache.spark.sql.Encoders.kryo[Vector]
  implicit val stringEncoder: Encoder[String] = org.apache.spark.sql.Encoders.kryo[String]

  def main(args: Array[String]) {
    val spark = SparkSession.builder
      .appName("Simple Application")
      .config("spark.master", "local") // TODO NEEDED TO CHANGE!
      .getOrCreate()

    val inFile = spark.read.format("csv")
      .option("sep", ",")
      .option("inferSchema", "true")
      .option("header", "true")
      .load("file:///home/julex/BigDataClustering/proteins_dataset_sample.csv")
      .cache

    val shingling_3 = spark.sparkContext
      .broadcast(inFile.flatMap(row => makeShingles(row.getString(4), 3))
      .distinct().collect())

    val converted_to_sparse_vectors_3 = inFile
      .map(row => makeSparseVectors(row.getString(4), shingling_3.value, 3))

    val kmeans = KMeans.train(converted_to_sparse_vectors_3.rdd, 3, 10)

    val squered_dist_cost = kmeans.computeCost(converted_to_sparse_vectors_3.rdd)
    println(squered_dist_cost)

    // # Make predictions
    //val predictions = kmeans.transform(converted_to_sparse_vectors_3.rdd)

    // # Evaluate clustering by computing Silhouette score
    // evaluator = ClusteringEvaluator()\
    //             .setFeaturesCol("features")\
    //             .setPredictionCol("prediction")\
    //             .setMetricName("silhouette")
    // silhouette_cost[k] = evaluator.evaluate(predictions)

    // converted_to_sparse_vectors_3.collect().foreach(i => println(i))

    spark.stop()
  }
}