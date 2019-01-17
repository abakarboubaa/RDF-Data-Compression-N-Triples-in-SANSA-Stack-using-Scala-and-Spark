package net.sansa_stack.Data_Compression.spark.rdf


/*
 * @author Abakar Bouba
*/

/*
  Class consist of constant variables/property names
 */
object Constants {

  //Output directory names
  val TRIPLE_DIR="/triples"
  val OBJECT_DIR="/object"
  val PREDICATE_DIR="/predicate"
  val SUBJECT_DIR="/subject"

  //Spark table names
  val OBJECT_TABLE="object"
  val SUBJECT_TABLE="subject"
  val PREDICATE_TABLE="predicate"
  val TRIPLE_RAW_TABLE="tripleRaw"
  val TRIPLE_TABLE="triple_fact"

  val INDEX_COL="index"
  val NAME_COL="name"

  val SUBJECT_COL="s"
  val OBJECT_COL="o"
  val PREDICATE_COL="p"
}
