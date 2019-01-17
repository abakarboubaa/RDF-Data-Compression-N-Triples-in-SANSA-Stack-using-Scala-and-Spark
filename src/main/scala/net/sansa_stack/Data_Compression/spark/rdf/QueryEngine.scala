package net.sansa_stack.Data_Compression.spark.rdf

import org.apache.spark.sql.{DataFrame, Row, SparkSession}

/*
 * @author Abakar Bouba
*/


/*
Class provides the methods to execute Spark SQL Code
 */
object QueryEngine {

  //Method executes the input SQL and returns the result Dataframe.
  def execute(spark:SparkSession,query:String)   ={
    spark.sqlContext.sql(query);
  }

}
