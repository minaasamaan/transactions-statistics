# Transactions statistics 

a RESTful API for our statistics. The main use case for the API is to calculate realtime statistics for the last 60 seconds of transactions.

The API has the following endpoints:

```
POST /transactions – called every time a transaction is made.
GET /statistics – returns the statistic based of the transactions of the last 60 seconds.
DELETE /transactions – deletes all transactions.
```
## High-level Design

- Resources Layer: this layer contains implementation for the rest endpoints, responsible for validating DTOs, transforming them to model objects and deliver them to the Managers Layer.
- Managers Layer: responsible for the business logic (both validations and processing). It should either return results to resource layer, or throw business exceptions that should be handled by Exception Handling Layer
- Exception Handling Layer: responsible for handling business exceptions, and logging server errors -if any-

## Usage

Use the following commands to work with this project

```bash

* Run: mvn spring-boot:run
* Install: mvn clean install
* Test: mvn integration-test; cat target/customReports/result.txt

```

### Transactions Manager

Defined by the interface `TransactionsManager` and implemented by `TransactionsManagerImpl`. 

`TransactionsManagerImpl` is an in-memory implementation for `TransactionsManager`, where all operations guaranteed to be:
 * Threadsafe, which means parallel calls should have consistent results. This is ensured by synchronizing
       on the BigDecimalSummaryStatistics 60 objects created upon the Manager object creation, one object per each second.
 * Operating in O(1) as a time & memory complexity, due to the fact that regardless of how many transactions we get,
      we accumulate them to 60 different buckets depends on which second of the last minute they belong to.
 * Statistics' values are `java.math.BigDecimal` and always contain exactly two decimal places and use
      `HALF_ROUND_UP` rounding.
      



