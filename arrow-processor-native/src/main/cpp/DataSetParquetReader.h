#ifndef SPARK_EXAMPLE_JNINATIVEPARQUETREADER_H
#define SPARK_EXAMPLE_JNINATIVEPARQUETREADER_H

#include <iostream>
#include <arrow/api.h>
#include <arrow/dataset/api.h>
#include <parquet/arrow/reader.h>
#include <arrow/util/iterator.h>

class DataSetParquetReader {
private:
    //keeping all this objects as shared_ptr field makes, sure that they are not deleted while the DataSetParquetReader is still active
    std::shared_ptr<arrow::MemoryPool> pool_;
    std::shared_ptr<arrow::RecordBatchIterator> recordBatchIter;
    std::shared_ptr<arrow::dataset::Dataset> dataset;
    std::shared_ptr<arrow::dataset::Scanner> scanner;
    std::shared_ptr<arrow::dataset::ScanTaskIterator> scan_task_it;
    std::shared_ptr<arrow::RecordBatch> batch;
public:
    DataSetParquetReader(const std::shared_ptr<arrow::MemoryPool> &memory_pool,
                         const std::string &file_name, const std::shared_ptr<arrow::Schema> &schema_file,
                         const std::shared_ptr<arrow::Schema> &schema_out, int num_rows);
    ~DataSetParquetReader() = default;
    std::shared_ptr<arrow::RecordBatch> ReadNext();
};

#endif //SPARK_EXAMPLE_JNINATIVEPARQUETREADER_H