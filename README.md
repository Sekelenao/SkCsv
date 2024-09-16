<p align="center">
  <img src="logoSK.svg" width="100" alt="logo">
</p>

<h1 align="center">
  SkCsv library for Java 17+
</h1>
<h3 align="center">
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

Please note that this documentation provides an overview of the core functionalities and usage of the SkCsv library. 
While it covers the fundamental aspects to get you started, it does not encompass every feature available. For a complete
understanding of all the functionalities and detailed class descriptions, we encourage you to refer to the `Javadoc`. 


The documentation here aims to introduce the main concepts and classes to help you navigate and use the library effectively.
By familiarizing yourself with the basics, you'll be better equipped to explore and utilize the full range of capabilities
provided in the `Javadoc`.

### Create a Csv object with its content from Java

You can create a `CSV` object with is content from Java using `SkCsv` and `SkCsvRow` classes.

```java
var csv = new SkCsv(); // Empty CSV
var csv2 = new SkCsv(  // Csv Containing 5 rows
        new SkCsvRow("Project", "Language", "Made with heart ?"),
        new SkCsvRow("SkCsv", "Java", "YES !"),
        new SkCsvRow(),
        new SkCsvRow("Don\"t worry;", "It's working anyway\n :)"),
        new SkCsvRow("END", "")
);
```

Printing the second object will result to the following text :

```
Project;Language;Made with heart ?
SkCsv;Java;ALWAYS !

"Don""t worry;";"It's working anyway
 :)"
END;

```

### Import an existing CSV file and map it to a Java object

Now we have a `CSV` file with some languages and their descriptions, separated by a tab and quoted by double quotes.

```
Language	Creation date	File extension
Java	1995	.java
Python	1991	.py
Powershell	2006	.ps1
Ocaml	1996	.ml
Fortran	1957	.f90
C	1972	.c
Typescript	2012	.ts
```

To import this file, we can use:

```java
SkCsv.from(CSV_PATH, new SkCsvConfig('\t', '"'));
SkCsv.from(CSV_PATH, new SkCsvConfig('\t', '"'), StandardCharsets.UTF_8);
```

If the CSV uses the library's default format, which is a semicolon as a separator and double quotes for quoting, you can use:

```java
SkCsv.from(CSV_PATH);
SkCsv.from(CSV_PATH, StandardCharsets.UTF_8);
SkCsv.from(CSV_PATH, SkCsvConfig.SEMICOLON);
```

Printing any of these objects will result in :

```
Language;Creation date;File extension
Java;1995;.java
Python;1991;.py
Powershell;2006;.ps1
Ocaml;1996;.ml
Fortran;1957;.f90
C;1972;.c
Typescript;2012;.ts
```

### Export a Java SkCsv object as a file

To export a Java SkCsv object to a file using the default format, you can use the following code:
```java
var csv = SkCsv.from(CSV_PATH, new SkCsvConfig('\t', '"'));
var exportPath = Paths.get("new_csv.csv");
csv.export(exportPath);
```

If you need to apply a custom format, you can configure the SkCsv object before exporting:

```java
var csv = SkCsv.from(CSV_PATH, new SkCsvConfig('\t', '"'));
var exportPath = Paths.get("new_csv.csv");
csv.configure(SkCsvConfig.COMMA).export(exportPath);
```

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
