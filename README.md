<p style="text-align:center;">
  <img src="logoSK.svg" width="100" alt="logo">
</p>

<h1 style="text-align:center;">
  SkCsv library for Java 17+
</h1>
<h3 style="text-align:center;">
  Latest version : 1.0.0
</h3>

## Description

SkCsv is a library designed for easy data manipulation, simplifying the import and export processes. You don't need to 
worry about special characters or other complexities during parsing. The focus has been directed towards prioritizing 
speed and security. The level of abstraction allows you to forget that you're working with a CSV format. 

## How to install

### Using Maven

You can use the following Maven dependency :

```xml
<dependency>
  <groupId>io.github.sekelenao</groupId>
  <artifactId>SkCsv</artifactId>
  <version>{version}</version>
</dependency>
```

### Using Gradle

You can add the following Gradle dependency :

```groovy
implementation 'io.github.sekelenao:SkCsv:{version}'
```

### Using JAR files :

You can download the JAR files (including Javadoc and classes) [here](https://github.com/Sekelenao/SkCsv/tree/main/jars).

The next step is to consult the documentation of the build tool you are using to learn how to import dependencies correctly.

## SkCsv Library Architecture

### 1. Classes for CSV format manipulation

- **SkCsvRow**: Represents a row in a CSV file. Provides methods for manipulating and accessing row data.
- **SkCsv**: Represents a CSV file as a whole. Allows manipulation of file rows and operations such as adding, 
removing, importing and exporting data.

### 2. Classes for Record export

- **CsvColumn**: Annotation used to mark components of a record that should be exported to a CSV file.
  Annotated components will be included in the CSV export.
- **SkCsvRecords**: Provides utilities for exporting records (objects) to CSV files. 
Contains methods for managing object data export to a CSV file.

### 3. Utility Classes

- **SkCsvConfig**: Represents the configuration used for formatting CSV data, such as delimiter and quote.

## Documentation



## Examples

### Export records without modifying the previous code

Imagine we have a massive and critical banking application. We would like to export our data to a CSV, but because we're
wary of tampering with these precious records, we don't want to add any code inside or export sensitive data.

Therefore, we will add an annotation to designate the data that we want to export.

```java
public record BankAccount(@CsvColumn String bankName, @CsvColumn UUID uuid, @CsvColumn BigDecimal balance, int secretCode)
```

Let's imagine that within the application, we have a way to obtain these records as an Iterable, as shown below.

```java
import java.util.Iterator;

private static final Iterable<BankAccount> BANK_ACCOUNT_ITERABLE = 
        new Iterable<BankAccount>() {...};
```

In terms of lines of code, we'll only need a single line ! The parser will take care of everything as usual;
it uses the String.valueOf method for annotated types. Unlike the rest of the library, the absence of null values is
not guaranteed!

```java
public final class Main {
    
    public static void main(String[] args) throws IOException {
        SkCsvRecords.export(Paths.get("out.csv"), BANK_ACCOUNT_ITERABLE);
    }

}
```

To obtain a huge CSV file of 1_000_000 records in ~2 seconds :

```
OnlyBank;a78e71e4-4c6f-4fde-990e-21274c2bd809;57.44878667185084
OnlyBank;2ebf4749-77e3-4158-85c6-5423c5f0b791;80.68069692551795
OnlyBank;89bd2b50-1aa5-4bb0-bf19-15defb47a0ed;51.924661497651535
```
