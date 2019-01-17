package net.sansa_stack.Data_Compression.spark.rdf

import java.io.File
import org.apache.jena.graph
import org.apache.spark.sql._
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory


/*
 * @author Abakar Bouba
*/

/*
--input
/home/abakar/IdeaProjects/RDF-Data-Compression-N-Triples-in-SANSA-Stack-using-Scala-and-Spark/src/main/resources/Input-Data/Small/dbpedia/sample.nt
--query-dir
/home/abakar/IdeaProjects/RDF-Data-Compression-N-Triples-in-SANSA-Stack-using-Scala-and-Spark/src/main/resources/compressedQuery/dbpedia
--output
/home/abakar/IdeaProjects/RDF-Data-Compression-N-Triples-in-SANSA-Stack-using-Scala-and-Spark/src/main/resources/dbpedia_compressed


 OR

  --compressed-input D:\\abakarb\\src\\main\\resources\\Output-Data\\GENERATED_NEW\\
 */

object DataCompression {

  val logger=LoggerFactory.getLogger(DataCompression.getClass)

  def main(args: Array[String]) {
    logger.info(s"Validating the input argument ${args}")
    parser.parse(args, Config()) match {
      case Some(config) =>
        run(config.in, config.out,config.compressedInput,config.queryDir)
      case None =>
        println(parser.usage)
    }

}
  def run(input: String, output:String,compressedInput:String,queryDir:String): Unit = {

    println("======================================================")
    println("|              RDF Data Compression 2018-2019           |")
    println("=======================================================")

    //Initialized the spark session
    val spark = SparkSession.builder
      .appName(s"Data Compression  $input")
     // .master("local[*]")
      .master("spark://172.18.160.16:3090")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    spark.sparkContext.setLogLevel("INFO")

    //Initialize the SQL context to enable SQL based query on Spark RDDs
    val sql=spark.sqlContext;

    //Creating DataLoaded Object to Read and Parse data
    val dataLoader=new DataLoader(spark,input,compressedInput)
    val subjectDF:DataFrame = dataLoader.getSubjectDF
    val objectDF:DataFrame = dataLoader.getObjectDF
    val predicateDF:DataFrame = dataLoader.getPredicateDF
    val tripleFactTable:DataFrame = dataLoader.getTripleFactTable

    //Print the count of each DF
    logger.info("Total Records                : " +tripleFactTable.count());
    logger.info("Count in Subject dictionary  : " + subjectDF.count())
    logger.info("Count in Predicate dictionary: " + predicateDF.count())
    logger.info("Count in Object dictionary   : " + objectDF.count())

    if(output!=null && !output.isEmpty) {
      logger.info(s"Saving the compressed result into ${output}.")
      var sTime=System.currentTimeMillis()

      //Store Dictionaries (Dimension tables) and Fact table on HDFS
      tripleFactTable.write.mode(SaveMode.Overwrite).csv(output + Constants.TRIPLE_DIR)
      subjectDF.write.mode(SaveMode.Overwrite).csv(output + Constants.SUBJECT_DIR)
      objectDF.write.mode(SaveMode.Overwrite).csv(output + Constants.OBJECT_DIR)
      predicateDF.write.mode(SaveMode.Overwrite).csv(output + Constants.PREDICATE_DIR)
      logger.info(s" Writing completed in ${System.currentTimeMillis()-sTime} ms.")
    }


    val queryFile:File = new File(queryDir)

    println("---------------------------------------------------------------------")
    println(s"|${String.format("%20s","Query File")}  |  ${String.format("%20s","Result Count")}  |   ${String.format("%20s","Execution Time (ms)")} |")
    println("---------------------------------------------------------------------")
    if(queryFile.isDirectory){
      queryFile.listFiles().filter(_.isFile).foreach(queryFile =>
      {
        val query= scala.io.Source.fromFile(queryFile).mkString

        //Executing the Query
        val sTime=System.currentTimeMillis()
        val resultDF= QueryEngine.execute(spark,query);

        //Getting the result Query
        val resultCount=resultDF.count()
        println(s"|${String.format("%20s",queryFile.getName)}  |  ${String.format("%20s",resultCount+"")}  |  ${String.format("%20s",""+(System.currentTimeMillis()-sTime))} |")

      })
    }


    //stoping the Spark
    spark.stop

  }

  case class Config(in: String = "",out:String="",compressedInput:String="",queryDir:String="")

  val parser = new scopt.OptionParser[Config]("Data Compression") {

    head(" Data Compression")

    opt[String]('i', "input").optional().valueName("<path>").
      action((x, c) => c.copy(in = x)).
      text("path to file that contains the data (in N-Triples format)")

    opt[String]('c', "compressed-input").optional().valueName("<path>").
      action((x, c) => c.copy(compressedInput = x)).
      text("path to compressed dir that contains the data.")

    opt[String]("output").optional().valueName("<path>").
      action((x, c) => c.copy(out = x)).
      text("path to directory where compressed data to be written")

    opt[String]("query-dir").valueName("<path>").
      action((x, c) => c.copy(queryDir = x)).
      text("path to query Directory.")

    help("help").text("prints this usage text")
  }
}