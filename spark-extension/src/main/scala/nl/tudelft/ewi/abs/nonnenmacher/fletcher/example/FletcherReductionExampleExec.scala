package org.apache.spark.sql

import nl.tudelft.ewi.abs.nonnenmacher.FletcherReductionProcessor
import nl.tudelft.ewi.abs.nonnenmacher.columnar.VectorSchemaRootUtil
import nl.tudelft.ewi.abs.nonnenmacher.utils.AutoCloseProcessingHelper._
import org.apache.arrow.vector.types.pojo.Schema
import org.apache.spark.TaskContext
import org.apache.spark.rdd.RDD
import nl.tudelft.ewi.abs.nonnenmacher.columnar.VectorSchemaRootUtil.{from, toBatch}
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.{Attribute, GenericInternalRow}
import org.apache.spark.sql.execution.metric.{SQLMetric, SQLMetrics}
import org.apache.spark.sql.execution.{SparkPlan, UnaryExecNode}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.ArrowUtils.toArrowField

import scala.collection.JavaConverters._

case class FletcherReductionExampleExec(out: Seq[Attribute], child: SparkPlan) extends UnaryExecNode {

  override def doExecute(): RDD[InternalRow] = {
    val aggregationTime = longMetric("aggregationTime")
    //    val processing = longMetric("processing")

    child.executeColumnar().mapPartitions { batches =>

      val inputSchema = toNotNullableArrowSchema(child.schema, conf.sessionLocalTimeZone)

      val fletcherReductionProcessor = new FletcherReductionProcessor(inputSchema)

      TaskContext.get().addTaskCompletionListener[Unit] { _ =>
        fletcherReductionProcessor.close()
      }

      var start: Long = 0
      var batchId:Long = 0
      batches
        .map { x => start = System.nanoTime(); x }
        .map { x => batchId=batchId+1;  println(s"Batch$batchId Num Rows:${x.numRows()}"); x}
        .map(VectorSchemaRootUtil.from)
        .mapAndAutoClose(fletcherReductionProcessor)
        .map(toRow)
        .map { x => aggregationTime += System.nanoTime() - start; x }
    }
  }

  private def toRow(res: Long): InternalRow = {
    val arr: Array[Any] = Array(res)
    new GenericInternalRow(arr)
  }

  override def output: Seq[Attribute] = out

  def toNotNullableArrowSchema(schema: StructType, timeZoneId: String): Schema = {
    new Schema(schema.map { field =>
      toArrowField(field.name, field.dataType, nullable = false, timeZoneId)
    }.asJava)
  }

  override lazy val metrics: Map[String, SQLMetric] = Map(
    "aggregationTime" -> SQLMetrics.createNanoTimingMetric(sparkContext, "time aggregating in [ns]"),
  )
}
