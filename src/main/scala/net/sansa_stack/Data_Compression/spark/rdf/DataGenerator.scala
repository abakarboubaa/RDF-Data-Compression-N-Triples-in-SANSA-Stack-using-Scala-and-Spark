package net.sansa_stack.Data_Compression.spark.rdf

import java.io.{BufferedOutputStream, BufferedWriter, FileOutputStream}

import org.slf4j.LoggerFactory

import scala.util.Random


/*
 * @author Abakar Bouba
*/

/*
  Class generate N unique RDF records and store it in given output directory.
  Run:
  5000000
/home/abakar/IdeaProjects/RDF-Data-Compression-N-Triples-in-SANSA-Stack-using-Scala-and-Spark/src/main/resources/Input-Data/GENERATED

      scala DataGenerator 230400 D:\abakarb\src\main\resources\Input-Data\GENERATED
*/

object DataGenerator {

  val logger=LoggerFactory.getLogger(DataGenerator.getClass)
  //Generate Unique values
  def getRandomString(len:Int):String={
    Random.alphanumeric.take(len).mkString
  }

  //Generate Unique record for each linenumber
  def getUniqueRecord(i:Long):String={
    //Template for record
    //val templateRecord="<http://dbpedia.org/resource/##subject> <http://www.w3.org/2004/02/skos/core###predicate> <http://dbpedia.org/resource/Category:##object> ."
   val templateRecord="<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/##subject> <http://www.w3.org/1999/02/22-rdf-syntax-ns###predicate> <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/##object> ."
    templateRecord.replaceFirst("##subject", getRandomString(64)).replaceFirst("##predicate",getRandomString(32)+i).replaceFirst("##object",getRandomString(64))

  }

  def main(args: Array[String]): Unit = {
    if(args.length!=2){
      logger.error("DataGenerator <num_records> <output-dir>")
      System.exit(1)
    }

    val size=args(0).toLong  //Number of records to be created
    val outDir=args(1)  // Output Directory where records are created

    logger.info(s"Number of records to be generated: $size")
    logger.info(s"Writing Data into ${outDir}/DataSet.nt")

    //Creating file in output mode
    val outStream=new FileOutputStream(outDir+"/DataSet.nt");

    var sTime=System.currentTimeMillis()
    for(i <- 0L to size){
      //writing the a record to file
      outStream.write((getUniqueRecord(i)+"\n").getBytes)
    }
    logger.info(s"Writing finished in "+ (System.currentTimeMillis()-sTime) +" ms.")
    outStream.flush();
    outStream.close()
  }
}
