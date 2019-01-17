package net.sansa_stack.Data_Compression.spark.rdf

import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}


/*
 * @author Abakar Bouba
*/

/*
Class Contains Schema related information for various DataFrames
 */
object RecordSchema {

  //Function returns the Triple Fact table schema.
  def tripleSchema:StructType = {
    StructType(
      Seq( StructField(name = Constants.SUBJECT_COL, dataType = StringType, nullable = false),
        StructField(name = Constants.OBJECT_COL, dataType = StringType, nullable = false),
        StructField(name = Constants.PREDICATE_COL, dataType = StringType, nullable = false)));
  }

  //Function returns the Dictionary Dataframe schema.
  def dictionarySchema={
    StructType(
      Seq( StructField(name = Constants.NAME_COL, dataType = StringType, nullable = false),
        StructField(name = Constants.INDEX_COL, dataType = LongType, nullable = false)));
  }
}
