//
// Created by Fabian Nonnenmacher on 07.05.20.
//

#ifndef SPARK_EXAMPLE_PLASMAPROCESSOR_H
#define SPARK_EXAMPLE_PLASMAPROCESSOR_H


#include "Iterator.h"
#include <plasma/client.h>
#include <iostream>

using namespace plasma;

class WriteToPlasmaProcessor : public BatchProcessor<std::shared_ptr<arrow::Buffer>, std::shared_ptr<ObjectID>> {
private:
    std::unique_ptr<PlasmaClient> client;
    std::shared_ptr<ObjectID> last_plasma_object = nullptr;
public:
    WriteToPlasmaProcessor();
    std::shared_ptr<ObjectID> process(std::shared_ptr<arrow::Buffer> batch) override;
    ~WriteToPlasmaProcessor() override;
};

class ReadFromPlasmaProcessor : public BatchProcessor<std::shared_ptr<ObjectID>, std::shared_ptr<arrow::Buffer>> {
private:
    std::unique_ptr<PlasmaClient> client;
public:
    ReadFromPlasmaProcessor();
    std::shared_ptr<arrow::Buffer> process(std::shared_ptr<ObjectID> objectId) override;
    ~ReadFromPlasmaProcessor() override;
};

#endif //SPARK_EXAMPLE_PLASMAPROCESSOR_H