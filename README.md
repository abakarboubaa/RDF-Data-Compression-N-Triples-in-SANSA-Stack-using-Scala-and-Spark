# RDF Compression and Query
`Master Thesis from Bonn University`
# Introduction
In this digital world, tons of data generated every day. To get the insight of data and leverage it for real-world problem solution requires scalable and distributed processing engine. SANSA Spark library provides scalable and highly efficient processing of RDF data. However, this approach requires the high memory and space requirement, which directly impacts the space, disc I/O and query latency time.

RDF Compression technique focuses on reducing the space requirement by eliminating duplicate records by ensuring the meaning of record intact. RDF Compression combines the vertical partitioning and star schema approaches to represent and store the data. It has a dimension table for each attribute, containing the unique values zipped with index. There is a single central fact table, contains the reference index numbers pointing to value in a dimension table. A dimension table stores only unique values that makes it small enough to persist and process in-memory.

# Architecture
It is observed that RDF dataset contains many duplicate records. Eliminating these duplicate records reduce the data size drastically, sometimes more than 80%. It is designed such that only a single copy of value will be stored in memory/disc, which is referred by a central table called fact table.  
!["Compaction Technique"](image/Architecture.PNG)

It is also possible to recreate actual dataset by reversing the above process .  


## Application Settings
### Deploy App on Cluster (Spark Submit Command)
```
./spark-submit --master [yarn/local[*]] \\
     -deploy-mode [client/cluster] \\
     --name RDF_COMPRESSION \\
     --class net.sansa_stack.Data_Compression.spark.rdf.DataCompression \\
     --jars <JAR_FILE_PATH> \\
     <JAR_FILE_PATH> <INPUT_ARGUMENTS>
 ```   
 Read here: [App Deploy](documentation/deployment.md)
Read here: [Scala & Spark](content/learn-scala-spark/README.md) 
#### Program Arguments
```
 --input D:/abakarb/src/main/resources/Input-Data/GENERATED/Dataset.nt
 --compressed-input D:/abakarb/src/main/resources/compressedData
 --output D:/abakarb/src/main/resources/Output-Data/GENERATED_NEW/
 --query-Dir D:/abakarb/src/main/resources/Output-Data/query/
```

Program takes either raw file or compressed directory as an input source. either --input and --compressed-input is require.
--query-Dir directory contains the list of query files which will be executed by program and result report will be printed.

##  Benchmarks
The datasets should be in N-Triples format.

### Compression Result
#### [LUBM](https://github.com/rvesse/lubm-uba) 
 |DataSet | Actual Size | Compress Size | Compression Ration | 
 |--------|-------------|---------------|--------|
 | DataSet 1   | 17.2 MB     | 3.3 MB        |   80%  |
 | DataSet 2  |      |         |  - %  |
 | DataSet 3   |      |         |  - %  |

#### [BSBM](https://sourceforge.net/projects/bsbmtools/files/bsbmtools/bsbmtools-0.2/bsbmtools-v0.2.zip/download) 
 |DataSet | Actual Size | Compress Size | Compression Ration | 
 |--------|-------------|---------------|--------|
 | DataSet 1   | 9.78 MB     | 3.3 MB        |               66%  |
 | DataSet 2  |      |         |  - %  |
 | DataSet 3   |      |         |  - %  |
 
#### [DBpedia](http://benchmark.dbpedia.org/)
 |DataSet | Actual Size | Compress Size | Compression Ration | 
 |--------|-------------|---------------|--------|
 | DaatSet 1   | 1.22 MB     | 614 KB | 50%  |
 | DataSet 2  |      |         |  - %  |
 | DataSet 3   |      |         |  - %  |


### Query Result
#### [LUBM](https://github.com/rvesse/lubm-uba) 
 |DataSet | SparQL Time (s) | SparQL Result Size | Compression Query Time (s) | Compression Query Result Size| 
 |--------|-------------|---------------|--------|--------|
 | Query 1   | 3.892     | 10634 | 3.375  |  10634  |
 | Query 2  |  6.941    |   4      |  10.1 | 4       |
 | Query 3   |  9.122   | 5916/4        |  9.851    |  5916/4       |

###### SparQL Query 1
```
SELECT ?author ?publication
WHERE {
	?publication :publicationAuthor ?author .
}
```

###### Compressed SQL Query 1
````
select subject.name as author, object.name as publication 
	from triple_fact join subject on triple_fact.s=subject.index  
		join object on triple_fact.o=object.index  
		join predicate on triple_fact.p=predicate.index AND predicate.name like '%#publicationAuthor%'
````

###### SparQL Query 2
````
SELECT *
WHERE {
        ?X :type :GraduateStudent .
        ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0> .
}
````
Compressed SQL Query 2
````
select subject.name as X
	from triple_fact join subject on triple_fact.s=subject.index  
		join predicate on triple_fact.p=predicate.index 
		join object on triple_fact.o=object.index  where
		(predicate.name like '%#type%' and object.name like '%#GraduateStudent%') OR
		( predicate.name like '%#takesCourse' AND object.name = 'http://www.Department0.University0.edu/GraduateCourse0' )
		group by subject.name having count(*)=2
````

###### SparQL Query 3
````
SELECT *
WHERE {
    { ?X :type :UndergraduateStudent . }
    UNION
    {
        ?X :type :GraduateStudent .
        ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0> .
    }
}
````

###### Compressed SQL Query 3
````
select subject.name as s
	from triple_fact join subject on triple_fact.s=subject.index  
		join predicate on triple_fact.p=predicate.index 
		join object on triple_fact.o=object.index  where
		(predicate.name like '%#type%' and object.name like '%#GraduateStudent%')

select subject.name
	from triple_fact   
		join predicate on triple_fact.p=predicate.index 
		join object on triple_fact.o=object.index 
		join subject on triple_fact.s=subject.index and subject.name != 'http://www.Department0.University0.edu/GraduateStudent101'	
		where 
		(predicate.name like '%#takesCourse%' and object.name = 'http://www.Department0.University0.edu/GraduateCourse0') OR 
		(predicate.name like '%#type%' and object.name like '%#GraduateStudent%')
		group by subject.name having count(*) > 1
````

#### [BSBM](https://sourceforge.net/projects/bsbmtools/files/bsbmtools/bsbmtools-0.2/bsbmtools-v0.2.zip/download) 
 
#### [DBpedia](http://benchmark.dbpedia.org/)


## SPARK Query Tutorials
Read here: [SPARK Query](https://spark.apache.org/sql/)


## Deploy App on Cluster
Read here: [App Deploy](documentation/deployment.md)

## Future Work
 - Apply compression algorithm like gzip/snappy/lzo on stored result to further optimize the space requirement.
 - Simply the query by providing abstract layer on top of compressed data
 - Add more SparQL operators
