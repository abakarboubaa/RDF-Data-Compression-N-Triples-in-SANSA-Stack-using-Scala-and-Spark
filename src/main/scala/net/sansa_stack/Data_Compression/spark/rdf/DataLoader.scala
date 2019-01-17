package net.sansa_stack.Data_Compression.spark.rdf

import net.sansa_stack.Data_Compression.spark.rdf.{Constants, DataCompression, RecordSchema}
import org.apache.jena.riot.Lang
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.slf4j.LoggerFactory
import net.sansa_stack.rdf.spark.io._
import org.apache.jena.graph


/*
 * @author Abakar Bouba
*/

 class DataLoader(spark:SparkSession,input:String,compressedInput:String) {

  private val lang = Lang.NTRIPLES
  private val logger=LoggerFactory.getLogger("DataLoader")
  private var triples: RDD[graph.Triple]=null
  private var triplesDF:DataFrame=null
  private var subjectDF:DataFrame=null
  private var objectDF:DataFrame=null
  private var predicateDF:DataFrame=null
  private var tripleFactTable:DataFrame=null

  if(input!=null && !input.isEmpty){
    import spark.sqlContext.implicits._

    logger.info(s"Reading the data from input file ${input}.")

    //Read RDF dataset, vertical partition it and store it in Spark distributed RDD
    triples= spark.rdf(lang)(input)

    //Create DataFrame (DF similar to RDBMS Table) by imposing Schema on Triple RDD.
    triplesDF=spark.createDataFrame(triples.map(t=> Row(t.getSubject.toString,t.getObject.toString(),t.getPredicate.toString())),RecordSchema.tripleSchema)

    //Create the Dataframe for each dictionary.
    subjectDF= spark.createDataFrame(triples.map(_.getSubject.toString()).distinct().zipWithIndex().map(t=> Row(t._1,t._2)),RecordSchema.dictionarySchema).cache();
    objectDF = spark.createDataFrame(triples.map(_.getObject.toString()).distinct().zipWithIndex().map(t=> Row(t._1,t._2)),RecordSchema.dictionarySchema).cache();
    predicateDF = spark.createDataFrame(triples.map(_.getPredicate.toString()).distinct().zipWithIndex().map(t=> Row(t._1,t._2)),RecordSchema.dictionarySchema).cache();
    triplesDF.createOrReplaceTempView(Constants.TRIPLE_RAW_TABLE);

    //Registering the DataFrame as Table, This enables SQL queries on top of distributed DF.
    subjectDF.createOrReplaceTempView(Constants.SUBJECT_TABLE);
    objectDF.createOrReplaceTempView(Constants.OBJECT_TABLE);
    predicateDF.createOrReplaceTempView(Constants.PREDICATE_TABLE);

    //Creating Fact table from Subject,Predicate and Object index. Fact table contains unique ID of Subject/Object/Predicate
    tripleFactTable= spark.sqlContext.sql(s"select ${Constants.SUBJECT_TABLE}.${Constants.INDEX_COL} as ${Constants.SUBJECT_COL}, ${Constants.PREDICATE_TABLE}.${Constants.INDEX_COL} as ${Constants.PREDICATE_COL},${Constants.OBJECT_TABLE}.${Constants.INDEX_COL} as ${Constants.OBJECT_COL} from ${Constants.TRIPLE_RAW_TABLE} join ${Constants.SUBJECT_TABLE} on ${Constants.TRIPLE_RAW_TABLE}.${Constants.SUBJECT_COL}=${Constants.SUBJECT_TABLE}.${Constants.NAME_COL} " +
      s"join ${Constants.OBJECT_TABLE} on ${Constants.TRIPLE_RAW_TABLE}.${Constants.OBJECT_COL}=${Constants.OBJECT_TABLE}.${Constants.NAME_COL} " +
      s"join ${Constants.PREDICATE_TABLE} on ${Constants.TRIPLE_RAW_TABLE}.${Constants.PREDICATE_COL}=${Constants.PREDICATE_TABLE}.${Constants.NAME_COL}");

    tripleFactTable.createOrReplaceTempView(Constants.TRIPLE_TABLE)
  }
  else if(compressedInput!=null && !compressedInput.isEmpty){
    logger.info(s"Reading the Compressed Data from input file ${input}.")
    tripleFactTable=spark.read.schema(RecordSchema.tripleSchema).csv(compressedInput+Constants.TRIPLE_DIR)
    objectDF=spark.read.schema(RecordSchema.dictionarySchema).csv(compressedInput+Constants.OBJECT_DIR)
    subjectDF=spark.read.schema(RecordSchema.dictionarySchema).csv(compressedInput+Constants.SUBJECT_DIR)
    predicateDF=spark.read.schema(RecordSchema.dictionarySchema).csv(compressedInput+Constants.PREDICATE_DIR)

    //Registering the DataFrame as Table, This enables SQL queries on top of distributed DF.
    subjectDF.createOrReplaceTempView(Constants.SUBJECT_TABLE);
    objectDF.createOrReplaceTempView(Constants.OBJECT_TABLE);
    predicateDF.createOrReplaceTempView(Constants.PREDICATE_TABLE);
    tripleFactTable.createOrReplaceTempView(Constants.TRIPLE_TABLE)
  }
  else{
    logger.error(s"Invalid Input. Either Input File Path or Compressed File path is mandatory.")
    System.exit(1)
  }

  //Method returns the Subject Dictionary DF
  def getSubjectDF=subjectDF

  //Method returns the Object Dictionary DF
  def getObjectDF=objectDF

  //Method returns the Predicate Dictionary DF
  def getPredicateDF=predicateDF

  //Method returns the TripleFactTable Dictionary DF
  def getTripleFactTable=tripleFactTable

}
